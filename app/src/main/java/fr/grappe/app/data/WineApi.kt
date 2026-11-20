package fr.grappe.app.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// Ce modèle représente les champs utilisés dans la réponse JSON de SampleAPIs.
@Serializable
data class WineSuggestion(val id: Int = 0, val wine: String = "", val winery: String = "", val location: String = "")

/** API externe indépendante de Supabase, appelée uniquement à la demande. */
class WineApi {
    // Ktor effectue la requête HTTP, convertit le JSON et applique un délai maximum.
    private val client = HttpClient(Android) {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        install(HttpTimeout) { requestTimeoutMillis = 15000 }
        expectSuccess = true
    }
    // body décode la réponse en liste Kotlin. take(1) garde au maximum un exemple pour l’écran À propos.
    suspend fun suggestions(): List<WineSuggestion> =
        client.get("https://api.sampleapis.com/wines/reds").body<List<WineSuggestion>>().take(1)
    // Ferme les ressources du client HTTP quand le ViewModel est libéré.
    fun close() { client.close() }
}
