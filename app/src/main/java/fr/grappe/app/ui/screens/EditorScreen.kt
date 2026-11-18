package fr.grappe.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import fr.grappe.app.model.Tasting
import fr.grappe.app.ui.components.*

/** Un formulaire commun à la création et à la modification. */
@Composable
fun EditorScreen(initial: Tasting, editing: Boolean, busy: Boolean, onSave: (Tasting) -> Unit, onCancel: () -> Unit) {
    var wine by rememberSaveable(initial.id) { mutableStateOf(initial.wine) }
    var vintage by rememberSaveable(initial.id) { mutableStateOf(initial.vintage) }
    var color by rememberSaveable(initial.id) { mutableStateOf(initial.color) }
    var region by rememberSaveable(initial.id) { mutableStateOf(initial.region) }
    var rating by rememberSaveable(initial.id) { mutableIntStateOf(initial.rating) }
    var notes by rememberSaveable(initial.id) { mutableStateOf(initial.notes) }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(Modifier.widthIn(max = 600.dp).fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(if (editing) "Modifier le vin" else "Ajouter un vin", style = MaterialTheme.typography.headlineMedium)
            OutlinedTextField(wine, { wine = it }, label = { Text("Nom du vin") }, singleLine = true, enabled = !busy, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(vintage, { vintage = it.take(4) }, label = { Text("Millésime") }, singleLine = true,
                    enabled = !busy, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                var expanded by remember { mutableStateOf(false) }
                Box(Modifier.weight(1f)) {
                    OutlinedButton({ expanded = true }, enabled = !busy, modifier = Modifier.fillMaxWidth().height(64.dp)) { Text(color) }
                    DropdownMenu(expanded, { expanded = false }) {
                        listOf("Rouge", "Blanc", "Rosé", "Effervescent").forEach { label ->
                            DropdownMenuItem({ Text(label) }, { color = label; expanded = false })
                        }
                    }
                }
            }
            OutlinedTextField(region, { region = it }, label = { Text("Région (facultatif)") }, singleLine = true, enabled = !busy, modifier = Modifier.fillMaxWidth())
            Text("Ma note : $rating/5", style = MaterialTheme.typography.titleMedium)
            RatingStars(rating, { if (!busy) rating = it })
            OutlinedTextField(notes, { notes = it }, label = { Text("Notes (facultatif)") }, minLines = 2, enabled = !busy, modifier = Modifier.fillMaxWidth())
            PrimaryButton("Enregistrer", {
                onSave(initial.copy(wine = wine.trim(), vintage = vintage, color = color,
                    region = region.trim(), rating = rating, notes = notes.trim()))
            }, Modifier.fillMaxWidth(), !busy)
            TextButton(onCancel, enabled = !busy, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("Annuler") }
        }
    }
}
