package fr.grappe.app.ui

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import fr.grappe.app.BuildConfig
import fr.grappe.app.data.*
import fr.grappe.app.model.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

// Cette photo de l’état regroupe ce que les écrans observent. Les propriétés val sont en lecture seule et copy() produit un nouvel état.
data class GrappeUiState(
    val user: UserProfile? = null,
    val tastings: List<Tasting> = emptyList(),
    val busy: Boolean = false,
    val message: String? = null,
    val offline: Boolean = false,
    val suggestions: List<WineSuggestion> = emptyList(),
)

/** Une seule source de vérité pour les écrans. Les opérations réseau restent dans data/. */
class GrappeViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences = application.getSharedPreferences("profile", 0)
    private val cache = LocalTastingRepository(application, seedDemo = !BuildConfig.CLOUD_ENABLED)
    private val supabase = SupabaseService(SecureSessionStore(application))
    private val cloud = SupabaseTastingRepository(supabase)
    // BuildConfig est généré à la compilation. Le type TastingRepository permet d’utiliser l’implémentation locale ou cloud via les mêmes méthodes.
    private val repository: TastingRepository = if (BuildConfig.CLOUD_ENABLED) cloud else cache
    private val wineApi = WineApi()
    // Le StateFlow contient la valeur courante et signale ses changements. Le ViewModel modifie _state et expose une version en lecture seule.
    private val _state = MutableStateFlow(GrappeUiState())
    val state = _state.asStateFlow()

    // Le bloc init s’exécute à la création du ViewModel. Il restaure une session disponible puis charge le carnet.
    init {
        if (BuildConfig.CLOUD_ENABLED) {
            supabase.session?.user?.profile()?.let { user ->
                _state.update { it.copy(user = user) }; refresh()
            }
        } else if (preferences.getBoolean("demo_connected", false)) enterDemo()
    }

    // Le profil de démo utilise les préférences de l’appareil et l’identifiant fixe demo.
    fun enterDemo() {
        if (BuildConfig.CLOUD_ENABLED) return
        val user = UserProfile("demo", preferences.getString("name", "Thomas Martin")!!,
            "thomas@grappe.demo", preferences.getString("photo", "").orEmpty())
        preferences.edit().putBoolean("demo_connected", true).apply()
        _state.update { it.copy(user = user) }
        refresh()
    }

    // require contrôle les données saisies. Selon register, on crée un compte ou on ouvre une session existante.
    fun authenticate(name: String, email: String, password: String, register: Boolean) = action {
        check(BuildConfig.CLOUD_ENABLED) { "Le mode démo n'utilise pas de mot de passe. Choisis « Explorer la démo »." }
        require(android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) { "Indique une adresse e-mail valide." }
        require(password.length >= 8) { "Le mot de passe doit contenir au moins 8 caractères." }
        if (register) require(name.isNotBlank()) { "Indique ton nom." }
        val user = if (register) supabase.signUp(name.trim(), email.trim(), password) else supabase.signIn(email.trim(), password)
        if (user == null) {
            notify("Compte créé. Confirme ton adresse e-mail, puis connecte-toi.")
        } else {
            _state.update { it.copy(user = user, tastings = emptyList()) }
            loadTastings(user)
        }
    }

    // ?.let lance le chargement uniquement si un utilisateur est présent. it désigne cet utilisateur.
    fun refresh() = action { _state.value.user?.let { loadTastings(it) } }

    private suspend fun loadTastings(user: UserProfile) {
        // Le cache permet la lecture hors ligne ; les écritures cloud exigent le réseau.
        // Le cache s’affiche en premier. En cloud, une lecture réussie remplace le cache par la liste issue du serveur.
        val cached = cache.load(user.id).filter { it.ownerId == user.id }
        _state.update { it.copy(tastings = cached) }
        try {
            val tastings = repository.load(user.id).filter { it.ownerId == user.id }
            if (BuildConfig.CLOUD_ENABLED) cache.replace(user.id, tastings)
            _state.update { it.copy(tastings = tastings, offline = false) }
        } catch (error: Exception) {
            if (error is CancellationException) throw error
            _state.update { it.copy(offline = true) }
            notify("Lecture du cache hors ligne. ${error.message ?: "Réseau indisponible."}")
        }
    }

    // Cette action vérifie le propriétaire et valide la fiche, puis recharge la liste après la sauvegarde. onSaved est une fonction appelée en cas de succès.
    fun save(tasting: Tasting, onSaved: () -> Unit) = action {
        // ?: return@action quitte uniquement le bloc de l’action lorsque la session est absente.
        val user = _state.value.user ?: return@action
        require(tasting.ownerId == user.id) { "Tu ne peux modifier que tes dégustations." }
        tasting.validationError()?.let { error(it) }
        val photo = uploadIfNeeded(tasting.photoUri, user.id)
        repository.save(tasting.copy(author = user.name, photoUri = photo))
        loadTastings(user)
        notify("Dégustation enregistrée.")
        onSaved()
    }

    // La confirmation se fait dans l’écran. Ici, on vérifie le propriétaire avant de supprimer puis de recharger la liste.
    fun delete(tasting: Tasting) = action {
        val user = _state.value.user ?: return@action
        require(tasting.ownerId == user.id) { "Tu ne peux supprimer que tes dégustations." }
        repository.delete(tasting.id, user.id)
        loadTastings(user)
        notify("Dégustation supprimée.")
    }

    // Le profil est mis à jour dans Supabase ou dans les préférences de démo, puis publié dans l’état de l’écran.
    fun updateProfile(name: String, photoUri: String, onSaved: () -> Unit) = action {
        require(name.isNotBlank() && name.length <= 80) { "Indique un nom de 1 à 80 caractères." }
        val user = _state.value.user ?: return@action
        val photo = uploadIfNeeded(photoUri, user.id)
        val updated = if (BuildConfig.CLOUD_ENABLED) supabase.updateProfile(name.trim(), photo)
        else {
            preferences.edit().putString("name", name.trim()).putString("photo", photo).apply()
            user.copy(name = name.trim(), photoUri = photo)
        }
        _state.update { it.copy(user = updated) }
        notify("Profil mis à jour."); onSaved()
    }

    // Le changement de mot de passe est disponible en cloud et impose au moins huit caractères.
    fun changePassword(password: String) = action {
        check(BuildConfig.CLOUD_ENABLED) { "Le mode démo n'a pas de mot de passe." }
        require(password.length >= 8) { "Choisis au moins 8 caractères." }
        supabase.updatePassword(password)
        notify("Mot de passe modifié.")
    }

    // Place les suggestions de l’API dans l’état affiché par À propos.
    fun discoverWines() = action {
        _state.update { it.copy(suggestions = wineApi.suggestions()) }
        if (_state.value.suggestions.isEmpty()) notify("Aucune suggestion disponible pour le moment.")
    }

    // Réinitialise la session et l’état affiché. Les préférences de démo conservent les données du carnet.
    fun signOut() = action {
        if (BuildConfig.CLOUD_ENABLED) {
            try { supabase.signOut() } catch (error: Exception) {
                if (error is CancellationException) throw error
            }
        }
        preferences.edit().putBoolean("demo_connected", false).apply()
        _state.value = GrappeUiState(busy = true)
    }

    // L’interface efface le message après l’avoir affiché pour éviter de le représenter.
    fun clearMessage() { _state.update { it.copy(message = null) } }
    fun notify(message: String) { _state.update { it.copy(message = message) } }

    // Seule une URI content: sélectionnée sur l’appareil en mode cloud déclenche un envoi.
    private suspend fun uploadIfNeeded(uri: String, ownerId: String): String {
        if (!BuildConfig.CLOUD_ENABLED || !uri.startsWith("content:")) return uri
        val bytes = withContext(Dispatchers.IO) {
            val resolver = getApplication<Application>().contentResolver
            // On lit d’abord les dimensions seules, puis on décode une image échantillonnée et on la convertit en JPEG.
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(Uri.parse(uri)).use { BitmapFactory.decodeStream(it, null, options) }
            require(options.outWidth > 0) { "Impossible de lire cette image." }
            options.inJustDecodeBounds = false
            options.inSampleSize = 1
            while (maxOf(options.outWidth, options.outHeight) / options.inSampleSize > 1600) options.inSampleSize *= 2
            val bitmap = resolver.openInputStream(Uri.parse(uri)).use { BitmapFactory.decodeStream(it, null, options) }
                ?: error("Impossible de lire cette image.")
            ByteArrayOutputStream().use { output ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, output)
                bitmap.recycle()
                output.toByteArray()
            }
        }
        return cloud.uploadPhoto(ownerId, bytes)
    }

    // Ce bloc commun lance une coroutine liée au ViewModel, expose la progression et traduit les erreurs en messages.
    private fun action(block: suspend () -> Unit) {
        if (_state.value.busy) return
        viewModelScope.launch {
            _state.update { it.copy(busy = true) }
            try { block() }
            catch (error: Exception) {
                if (error is CancellationException) throw error
                notify(error.message ?: "Une erreur est survenue. Réessaie.")
            // finally réactive les actions à la fin. L’annulation de la coroutine est propagée.
            } finally { _state.update { it.copy(busy = false) } }
        }
    }

    // Android appelle cette méthode à la fin de vie du ViewModel, qui ferme alors les clients HTTP.
    override fun onCleared() { supabase.close(); wineApi.close() }
}
