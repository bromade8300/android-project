package fr.grappe.app.data

import fr.grappe.app.BuildConfig
import fr.grappe.app.model.UserProfile
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*

@Serializable
data class SupabaseUser(val id: String, val email: String = "", val user_metadata: JsonObject = JsonObject(emptyMap())) {
    // ?. accède à une valeur seulement si elle existe. ?: fournit le nom par défaut quand la métadonnée est absente.
    fun profile() = UserProfile(id, user_metadata["name"]?.jsonPrimitive?.content ?: "Amateur de découvertes", email,
        user_metadata["avatar_url"]?.jsonPrimitive?.content.orEmpty())
}

// Le jeton d’accès accompagne les requêtes authentifiées. Le jeton de renouvellement permet d’en obtenir un nouveau.
@Serializable
data class SupabaseSession(
    val access_token: String,
    val refresh_token: String,
    val expires_at: Long = 0,
    val expires_in: Long = 3600,
    val user: SupabaseUser,
)

/** Petit adaptateur HTTP : Auth, PostgREST et Storage passent tous par Ktor. */
class SupabaseService(private val sessionStore: SecureSessionStore) {
    // Ktor utilise cette configuration pour lire et écrire le JSON. Les champs inconnus sont ignorés.
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    val client = HttpClient(Android) {
        install(ContentNegotiation) { json(json) }
        install(HttpTimeout) { requestTimeoutMillis = 15000; connectTimeoutMillis = 10000 }
    }
    private val refreshMutex = Mutex()
    // La session est relue depuis le stockage chiffré. private set réserve sa modification à ce service.
    var session: SupabaseSession? = sessionStore.read()?.let { runCatching { json.decodeFromString<SupabaseSession>(it) }.getOrNull() }
        private set
    val configured get() = BuildConfig.SUPABASE_URL.startsWith("https://") && BuildConfig.SUPABASE_KEY.isNotBlank()

    // Échange les identifiants contre une session, la mémorise puis retourne le profil.
    suspend fun signIn(email: String, password: String): UserProfile {
        val response = client.post(endpoint("/auth/v1/token?grant_type=password")) {
            headers(); setBody(buildJsonObject { put("email", email); put("password", password) })
        }
        ensureSuccess(response.status)
        remember(response.body())
        return session!!.user.profile()
    }

    // Le nom est envoyé dans les métadonnées du compte. UserProfile? permet de signaler qu’une confirmation par e-mail est nécessaire.
    suspend fun signUp(name: String, email: String, password: String): UserProfile? {
        val response = client.post(endpoint("/auth/v1/signup")) {
            headers(); setBody(buildJsonObject {
                put("email", email); put("password", password)
                putJsonObject("data") { put("name", name) }
            })
        }
        ensureSuccess(response.status)
        val payload = response.body<JsonObject>()
        // Avec confirmation e-mail, Supabase crée l'utilisateur mais n'ouvre pas encore de session.
        if (payload["access_token"] == null || payload["access_token"] is JsonNull) return null
        remember(json.decodeFromJsonElement(payload))
        return session!!.user.profile()
    }

    // Le Mutex évite deux renouvellements simultanés. On réutilise le jeton si sa durée restante dépasse une minute.
    suspend fun accessToken(): String = refreshMutex.withLock {
        val current = session ?: error("Connecte-toi pour continuer.")
        if (current.expires_at > System.currentTimeMillis() / 1000 + 60) return@withLock current.access_token
        val response = client.post(endpoint("/auth/v1/token?grant_type=refresh_token")) {
            headers(); setBody(buildJsonObject { put("refresh_token", current.refresh_token) })
        }
        ensureSuccess(response.status)
        remember(response.body())
        session!!.access_token
    }

    // Met à jour les métadonnées du compte puis remplace le profil de la session mémorisée.
    suspend fun updateProfile(name: String, photoUrl: String): UserProfile {
        val token = accessToken()
        val response = client.put(endpoint("/auth/v1/user")) {
            headers(token); setBody(buildJsonObject { putJsonObject("data") { put("name", name); put("avatar_url", photoUrl) } })
        }
        ensureSuccess(response.status)
        val user = response.body<SupabaseUser>()
        remember(session!!.copy(user = user))
        return user.profile()
    }

    // La modification du mot de passe utilise le jeton d’accès du compte actif.
    suspend fun updatePassword(password: String) {
        val token = accessToken()
        val response = client.put(endpoint("/auth/v1/user")) {
            headers(token); setBody(buildJsonObject { put("password", password) })
        }
        ensureSuccess(response.status)
    }

    suspend fun signOut() {
        // La session locale est effacée même lorsque le réseau ne répond pas.
        try {
            session?.let { client.post(endpoint("/auth/v1/logout")) { headers(it.access_token) } }
        } finally { session = null; sessionStore.clear() }
    }

    // Vérifie la configuration puis assemble l’URL du projet et le chemin de l’API.
    fun endpoint(path: String): String {
        check(configured) { "Configure supabase.url et supabase.key dans local.properties, puis recompile le mode cloud." }
        return BuildConfig.SUPABASE_URL.trimEnd('/') + path
    }

    // Cette fonction d’extension ajoute la clé publique et, si disponible, le jeton utilisateur à la requête.
    fun HttpRequestBuilder.headers(token: String? = null) {
        header("apikey", BuildConfig.SUPABASE_KEY)
        token?.let { bearerAuth(it) }
        contentType(ContentType.Application.Json)
    }

    // Les codes HTTP de 200 à 299 indiquent un succès. check transforme les autres codes en exception avec un message.
    fun ensureSuccess(status: HttpStatusCode) {
        check(status.value in 200..299) {
            when (status.value) {
                400, 401 -> "Connexion refusée. Vérifie tes identifiants et la confirmation de ton e-mail."
                403 -> "Accès refusé. Vérifie les règles Supabase du projet."
                429 -> "Trop de tentatives. Réessaie dans quelques instants."
                else -> "Le service est indisponible (HTTP ${status.value}). Réessaie plus tard."
            }
        }
    }

    // Calcule la date d’expiration si seule la durée est fournie, puis enregistre la session au format JSON.
    private fun remember(value: SupabaseSession) {
        session = if (value.expires_at == 0L) value.copy(expires_at = System.currentTimeMillis() / 1000 + value.expires_in) else value
        sessionStore.write(json.encodeToString(session!!))
    }

    fun close() { client.close() }
}
