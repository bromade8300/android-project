package fr.grappe.app.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import fr.grappe.app.model.wineStats
import fr.grappe.app.BuildConfig
import fr.grappe.app.ui.GrappeUiState
import fr.grappe.app.ui.components.*
import fr.grappe.app.ui.theme.*

@Composable
fun ProfileScreen(state: GrappeUiState, onProfile: (String, String, () -> Unit) -> Unit,
                  onPassword: (String) -> Unit, onAbout: () -> Unit, onSignOut: () -> Unit) {
    val user = state.user ?: return
    var edit by rememberSaveable { mutableStateOf(false) }
    var name by rememberSaveable(user.name) { mutableStateOf(user.name) }
    var photo by rememberSaveable(user.photoUri) { mutableStateOf(user.photoUri) }
    var passwordDialog by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            photo = it.toString()
        }
    }
    val stats = wineStats(state.tastings, user.id)
    Column(Modifier.fillMaxSize().wrapContentWidth(Alignment.CenterHorizontally).widthIn(max = 600.dp).verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Mon profil", style = MaterialTheme.typography.headlineMedium)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Avatar(user.name, Modifier.size(64.dp), user.photoUri)
            Column { Text(user.name, style = MaterialTheme.typography.titleLarge); Text(user.email, color = Muted) }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            ProfileStat("${stats.count}", "vins bus")
            ProfileStat(stats.regions.toString(), "régions")
            ProfileStat(stats.average?.let { String.format(java.util.Locale.FRANCE, "%.1f", it) } ?: "—", "note moyenne")
        }
        OutlinedButton({ edit = !edit }, Modifier.fillMaxWidth(), enabled = !state.busy) { Text("Modifier mon profil") }
        if (edit) {
            OutlinedTextField(name, { name = it }, label = { Text("Nom") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Avatar(name, photo = photo)
                TextButton({ picker.launch(arrayOf("image/*")) }) { Text("Choisir une photo") }
            }
            PrimaryButton("Enregistrer mon profil", { onProfile(name, photo) { edit = false } }, Modifier.fillMaxWidth(), !state.busy)
        }
        HorizontalDivider()
        Text("Mon compte", style = MaterialTheme.typography.titleLarge)
        if (BuildConfig.CLOUD_ENABLED) TextButton({ passwordDialog = true }) {
            Icon(Icons.Outlined.Lock, null); Spacer(Modifier.width(12.dp)); Text("Modifier mon mot de passe")
        }
        TextButton(onAbout) { Icon(Icons.Outlined.Info, null); Spacer(Modifier.width(12.dp)); Text("À propos de Grappe") }
        OutlinedButton(onSignOut, Modifier.fillMaxWidth(), enabled = !state.busy) { Text("Se déconnecter") }
    }
    if (passwordDialog) AlertDialog(onDismissRequest = { passwordDialog = false; password = "" },
        title = { Text("Nouveau mot de passe") },
        text = { OutlinedTextField(password, { password = it }, label = { Text("8 caractères minimum") }, visualTransformation = PasswordVisualTransformation()) },
        confirmButton = { TextButton({ onPassword(password); passwordDialog = false; password = "" }) { Text("Modifier") } },
        dismissButton = { TextButton({ passwordDialog = false; password = "" }) { Text("Annuler") } })
}

@Composable
private fun ProfileStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineMedium, color = Burgundy)
        Text(label, style = MaterialTheme.typography.bodySmall, color = Muted)
    }
}

@Composable
fun AboutScreen(state: GrappeUiState, onDiscover: () -> Unit, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().wrapContentWidth(Alignment.CenterHorizontally).widthIn(max = 600.dp).verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Text("À propos de Grappe", style = MaterialTheme.typography.headlineMedium)
        Text("Grappe est une bibliothèque personnelle des vins bus : un nom, un millésime, une couleur et une note.")
        Text("Vos données", style = MaterialTheme.typography.titleLarge)
        Text(if (BuildConfig.CLOUD_ENABLED)
            "Le compte est géré par Supabase Auth. La bibliothèque affiche uniquement vos vins. Seul l'auteur peut les modifier ou les supprimer. Les photos déposées dans le cloud sont publiques. Les dernières dégustations restent consultables hors ligne sur cet appareil."
            else "Cette version de démonstration conserve les dégustations et le profil sur cet appareil. Elle ne crée aucun compte en ligne.")
        Text("Catalogue de découverte", style = MaterialTheme.typography.titleLarge)
        Text("Les suggestions de vins proviennent de SampleAPIs. Elles sont chargées uniquement lorsque vous les demandez.")
        OutlinedButton(onDiscover, enabled = !state.busy) { Text("Charger un exemple de vin (API)") }
        state.suggestions.forEach { wine ->
            Text("${wine.wine} · ${wine.winery}", style = MaterialTheme.typography.bodyMedium)
        }
        Text("Projet pédagogique · version ${BuildConfig.VERSION_NAME}", color = Muted)
        Text("L'abus d'alcool est dangereux pour la santé. À consommer avec modération.", style = MaterialTheme.typography.bodySmall, color = Muted)
        PrimaryButton("Revenir à mon carnet", onBack)
    }
}
