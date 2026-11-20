package fr.grappe.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.grappe.app.model.Tasting
import fr.grappe.app.ui.screens.*
import fr.grappe.app.ui.theme.*

// Une enum énumère les destinations possibles de la navigation.
private enum class Destination { Home, Editor, Profile, About }

// @Composable identifie une fonction qui décrit un morceau de l’interface Compose.
@Composable
fun GrappeApp(viewModel: GrappeViewModel) {
    // Compose observe le StateFlow quand l’écran est actif. by donne un accès direct à la valeur observée.
    val state by viewModel.state.collectAsStateWithLifecycle()
    var destination by rememberSaveable { mutableStateOf(Destination.Home) }
    var editing by rememberSaveable { mutableStateOf(false) }
    // Le brouillon est sérialisé pour survivre à une rotation pendant la modification.
    var draftJson by rememberSaveable { mutableStateOf("") }
    val snackbar = remember { SnackbarHostState() }

    // On réutilise la fiche en modification. En création, on prépare un brouillon avec un nouvel identifiant et le compte actif.
    fun openEditor(tasting: Tasting? = null) {
        val user = state.user ?: return
        val draft = tasting ?: Tasting(ownerId = user.id, author = user.name)
        draftJson = kotlinx.serialization.json.Json.encodeToString(Tasting.serializer(), draft)
        editing = tasting != null
        destination = Destination.Editor
    }

    // Cet effet se relance quand le message change. Il affiche une bannière temporaire puis efface le message.
    LaunchedEffect(state.message) {
        state.message?.let { snackbar.showSnackbar(it); viewModel.clearMessage() }
    }
    LaunchedEffect(state.user?.id) { if (state.user == null) destination = Destination.Home }
    // Le bouton Retour ramène au carnet quand une autre destination est ouverte et qu’aucune action n’est en cours.
    BackHandler(enabled = state.user != null && destination != Destination.Home) {
        if (!state.busy) destination = Destination.Home
    }
    GrappeTheme {
        // Scaffold structure la page avec une barre de navigation et un emplacement pour les messages.
        Scaffold(containerColor = Cream, snackbarHost = { SnackbarHost(snackbar) }, bottomBar = {
            if (state.user != null) NavigationBar(containerColor = Paper, tonalElevation = 0.dp) {
                NavigationBarItem(destination == Destination.Home, { if (!state.busy) destination = Destination.Home },
                    icon = { Icon(Icons.Outlined.Home, null) }, label = { Text("Mes vins") })
                NavigationBarItem(destination == Destination.Editor, { if (!state.busy) openEditor() },
                    icon = { Icon(Icons.Outlined.AddCircle, null) }, label = { Text("Ajouter") })
                NavigationBarItem(destination == Destination.Profile || destination == Destination.About, { if (!state.busy) destination = Destination.Profile },
                    icon = { Icon(Icons.Outlined.PersonOutline, null) }, label = { Text("Profil") })
            }
        }) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).imePadding()) {
                if (state.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
                // L’absence de profil affiche la connexion. Sinon when choisit l’écran et :: transmet une méthode du ViewModel comme action.
                if (state.user == null) AuthScreen(state.busy, viewModel::enterDemo, viewModel::authenticate)
                else when (destination) {
                    Destination.Home -> HomeScreen(state, { openEditor(it) }, viewModel::delete, viewModel::refresh)
                    Destination.Editor -> {
                        val initial = remember(draftJson) { kotlinx.serialization.json.Json.decodeFromString(Tasting.serializer(), draftJson) }
                        EditorScreen(initial, editing, state.busy,
                            { viewModel.save(it) { destination = Destination.Home } }, { destination = Destination.Home })
                    }
                    Destination.Profile -> ProfileScreen(state, viewModel::updateProfile, viewModel::changePassword,
                        { destination = Destination.About }, viewModel::signOut)
                    Destination.About -> AboutScreen(state, viewModel::discoverWines) { destination = Destination.Profile }
                }
            }
        }
    }
}
