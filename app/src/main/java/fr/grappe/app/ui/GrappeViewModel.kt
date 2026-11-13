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
    private val repository: TastingRepository = if (BuildConfig.CLOUD_ENABLED) cloud else cache
    private val wineApi = WineApi()
    private val _state = MutableStateFlow(GrappeUiState())
    val state = _state.asStateFlow()

    init {
        if (BuildConfig.CLOUD_ENABLED) {
            supabase.session?.user?.profile()?.let { user ->
                _state.update { it.copy(user = user) }; refresh()
            }
        } else if (preferences.getBoolean("demo_connected", false)) enterDemo()
    }

    fun enterDemo() {
        if (BuildConfig.CLOUD_ENABLED) return
        val user = UserProfile("demo", preferences.getString("name", "Thomas Martin")!!,
            "thomas@grappe.demo", preferences.getString("photo", "").orEmpty())
        preferences.edit().putBoolean("demo_connected", true).apply()
        _state.update { it.copy(user = user) }
        refresh()
    }

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

    fun refresh() = action { _state.value.user?.let { loadTastings(it) } }

    private suspend fun loadTastings(user: UserProfile) {
        // Le cache permet la lecture hors ligne ; les écritures cloud exigent le réseau.
        val cached = cache.load(user.id)
        _state.update { it.copy(tastings = cached) }
        try {
            val tastings = repository.load(user.id)
            if (BuildConfig.CLOUD_ENABLED) cache.replace(user.id, tastings)
            _state.update { it.copy(tastings = tastings, offline = false) }
        } catch (error: Exception) {
            if (error is CancellationException) throw error
            _state.update { it.copy(offline = true) }
            notify("Lecture du cache hors ligne. ${error.message ?: "Réseau indisponible."}")
        }
    }

    fun save(tasting: Tasting, onSaved: () -> Unit) = action {
        val user = _state.value.user ?: return@action
        require(tasting.ownerId == user.id) { "Tu ne peux modifier que tes dégustations." }
        tasting.validationError()?.let { error(it) }
        val photo = uploadIfNeeded(tasting.photoUri, user.id)
        repository.save(tasting.copy(author = user.name, photoUri = photo))
        loadTastings(user)
        notify("Dégustation enregistrée.")
        onSaved()
    }

    fun delete(tasting: Tasting) = action {
        val user = _state.value.user ?: return@action
        require(tasting.ownerId == user.id) { "Tu ne peux supprimer que tes dégustations." }
        repository.delete(tasting.id, user.id)
        loadTastings(user)
        notify("Dégustation supprimée.")
    }

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

    fun changePassword(password: String) = action {
        check(BuildConfig.CLOUD_ENABLED) { "Le mode démo n'a pas de mot de passe." }
        require(password.length >= 8) { "Choisis au moins 8 caractères." }
        supabase.updatePassword(password)
        notify("Mot de passe modifié.")
    }

    fun discoverWines() = action {
        _state.update { it.copy(suggestions = wineApi.suggestions()) }
        if (_state.value.suggestions.isEmpty()) notify("Aucune suggestion disponible pour le moment.")
    }

    fun signOut() = action {
        if (BuildConfig.CLOUD_ENABLED) {
            try { supabase.signOut() } catch (error: Exception) {
                if (error is CancellationException) throw error
            }
        }
        preferences.edit().putBoolean("demo_connected", false).apply()
        _state.value = GrappeUiState(busy = true)
    }

    fun clearMessage() { _state.update { it.copy(message = null) } }
    fun notify(message: String) { _state.update { it.copy(message = message) } }

    private suspend fun uploadIfNeeded(uri: String, ownerId: String): String {
        if (!BuildConfig.CLOUD_ENABLED || !uri.startsWith("content:")) return uri
        val bytes = withContext(Dispatchers.IO) {
            val resolver = getApplication<Application>().contentResolver
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

    private fun action(block: suspend () -> Unit) {
        if (_state.value.busy) return
        viewModelScope.launch {
            _state.update { it.copy(busy = true) }
            try { block() }
            catch (error: Exception) {
                if (error is CancellationException) throw error
                notify(error.message ?: "Une erreur est survenue. Réessaie.")
            } finally { _state.update { it.copy(busy = false) } }
        }
    }

    override fun onCleared() { supabase.close(); wineApi.close() }
}
