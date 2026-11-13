package fr.grappe.app.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import fr.grappe.app.model.Tasting
import fr.grappe.app.ui.components.*
import fr.grappe.app.ui.theme.*

@Composable
fun EditorScreen(initial: Tasting, editing: Boolean, busy: Boolean, onSave: (Tasting) -> Unit, onCancel: () -> Unit) {
    var wine by rememberSaveable(initial.id) { mutableStateOf(initial.wine) }
    var vintage by rememberSaveable(initial.id) { mutableStateOf(initial.vintage) }
    var color by rememberSaveable(initial.id) { mutableStateOf(initial.color) }
    var region by rememberSaveable(initial.id) { mutableStateOf(initial.region) }
    var rating by rememberSaveable(initial.id) { mutableIntStateOf(initial.rating) }
    var notes by rememberSaveable(initial.id) { mutableStateOf(initial.notes) }
    var photo by rememberSaveable(initial.id) { mutableStateOf(initial.photoUri) }
    val context = LocalContext.current
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            photo = it.toString()
        }
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)) {
        BrandHeader("Vos coups de cœur", if (editing) "Un souvenir à préciser." else "Une nouvelle dégustation")
        BoxWithConstraints {
            val tablet = maxWidth >= 650.dp
            val photoPanel: @Composable (Modifier) -> Unit = { modifier ->
                Column(modifier) {
                    Box(Modifier.fillMaxWidth().height(if (tablet) 290.dp else 170.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(14.dp))
                        .clickable(enabled = !busy) { picker.launch(arrayOf("image/*")) }, contentAlignment = Alignment.Center) {
                        if (photo.isNotBlank()) AsyncImage(photo, "Changer la photo du vin", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        else Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Outlined.AddAPhoto, null, tint = Burgundy)
                            Spacer(Modifier.height(12.dp))
                            Text("Photographiez votre découverte", color = Burgundy)
                            Text("Bouteille, étiquette ou moment partagé", style = MaterialTheme.typography.bodySmall, color = Muted)
                        }
                    }
                    if (photo.isNotBlank()) TextButton({ photo = "" }) { Text("Retirer la photo") }
                    if (tablet) {
                        Spacer(Modifier.height(20.dp))
                        Text("Chaque vin a son histoire.", style = MaterialTheme.typography.titleLarge, color = Burgundy)
                        Text("Une robe, une émotion, un souvenir.\nPartagez le vôtre avec vos amis.", color = Muted)
                    }
                }
            }
            val fields: @Composable (Modifier) -> Unit = { modifier ->
                Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(wine, { wine = it }, label = { Text("Le vin") }, placeholder = { Text("Nom du domaine ou de la cuvée") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(vintage, { vintage = it.take(4) }, label = { Text("Millésime") }, singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                        var expanded by remember { mutableStateOf(false) }
                        Box(Modifier.weight(1f)) {
                            OutlinedButton({ expanded = true }, Modifier.fillMaxWidth().height(64.dp), shape = RoundedCornerShape(4.dp)) {
                                Text(color); Icon(Icons.Outlined.ExpandMore, null)
                            }
                            DropdownMenu(expanded, { expanded = false }) {
                                listOf("Rouge", "Blanc", "Rosé", "Effervescent").forEach { label ->
                                    DropdownMenuItem({ Text(label) }, { color = label; expanded = false })
                                }
                            }
                        }
                    }
                    OutlinedTextField(region, { region = it }, label = { Text("Région / appellation") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Column { Eyebrow("Votre note"); RatingStars(rating, { rating = it }) }
                    OutlinedTextField(notes, { notes = it }, label = { Text("Votre ressenti") },
                        placeholder = { Text("Arômes, accord, occasion… racontez-nous.") }, minLines = 3, modifier = Modifier.fillMaxWidth())
                    PrimaryButton(if (editing) "Enregistrer les modifications" else "Partager ma dégustation",
                        { onSave(initial.copy(wine = wine.trim(), vintage = vintage, color = color, region = region.trim(), rating = rating, notes = notes.trim(), photoUri = photo)) },
                        Modifier.fillMaxWidth(), !busy)
                    TextButton(onCancel, enabled = !busy, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("Annuler") }
                }
            }
            if (tablet) Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) { photoPanel(Modifier.weight(1f)); fields(Modifier.weight(1f)) }
            else Column(verticalArrangement = Arrangement.spacedBy(20.dp)) { photoPanel(Modifier.fillMaxWidth()); fields(Modifier.fillMaxWidth()) }
        }
    }
}
