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
    val mine = state.tastings.filter { it.ownerId == user.id }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        BrandHeader("Les bons moments restent", "Mon carnet")
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Avatar(user.name, Modifier.size(64.dp), user.photoUri)
            Column { Text(user.name, style = MaterialTheme.typography.titleLarge); Text(user.email, color = Muted) }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            ProfileStat("${mine.size}", "dégustations")
            ProfileStat(mine.map { it.region }.filter { it.isNotBlank() }.distinct().size.toString(), "régions")
            ProfileStat(if (mine.isEmpty()) "—" else String.format(java.util.Locale.FRANCE, "%.1f", mine.map { it.rating }.average()), "note moyenne")
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
        Text("Mes dernières découvertes", style = MaterialTheme.typography.titleLarge)
        if (mine.isEmpty()) Text("Votre carnet attend sa première bouteille.", color = Muted)
        mine.take(3).forEach { tasting ->
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                BottleArt(Modifier.width(60.dp).height(78.dp), tasting.color)
                Column {
                    Text(tasting.wine, style = MaterialTheme.typography.titleMedium)
                    Text("${tasting.region} · ${tasting.vintage}", color = Muted)
                    RatingStars(tasting.rating)
                }
            }
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
fun AboutScreen(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        BrandHeader("Le goût du partage", "À propos de Grappe")
        BottleArt(Modifier.fillMaxWidth().height(200.dp))
        Text("Les belles découvertes se partagent.", style = MaterialTheme.typography.titleLarge, color = Burgundy)
        Text("Grappe est un carnet de dégustation : gardez une trace des vins, de leurs arômes et des moments qui les accompagnent.")
        Text("Vos données", style = MaterialTheme.typography.titleLarge)
        Text(if (BuildConfig.CLOUD_ENABLED)
            "Le compte est géré par Supabase Auth. Les dégustations sont partagées entre les utilisateurs connectés. Seul l'auteur peut les modifier ou les supprimer. Les photos déposées dans le cloud sont publiques. Les dernières dégustations restent consultables hors ligne sur cet appareil."
            else "Cette version de démonstration conserve les dégustations et le profil sur cet appareil. Elle ne crée aucun compte en ligne.")
        Text("Catalogue de découverte", style = MaterialTheme.typography.titleLarge)
        Text("Les suggestions de vins proviennent de SampleAPIs. Elles sont chargées uniquement lorsque vous les demandez.")
        Text("Projet pédagogique · version ${BuildConfig.VERSION_NAME}", color = Muted)
        Text("L'abus d'alcool est dangereux pour la santé. À consommer avec modération.", style = MaterialTheme.typography.bodySmall, color = Muted)
        PrimaryButton("Revenir à mon carnet", onBack)
    }
}
