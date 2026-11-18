package fr.grappe.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.grappe.app.model.Tasting
import fr.grappe.app.ui.GrappeUiState
import fr.grappe.app.ui.theme.*

/** La bibliothèque affiche uniquement les vins de l'utilisateur connecté. */
@Composable
fun HomeScreen(state: GrappeUiState, onEdit: (Tasting) -> Unit,
               onDelete: (Tasting) -> Unit, onRefresh: () -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var toDelete by remember { mutableStateOf<Tasting?>(null) }
    val wines = state.tastings.filter {
        it.ownerId == state.user?.id &&
            (it.wine.contains(query, true) || it.region.contains(query, true))
    }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(Modifier.widthIn(max = 760.dp).fillMaxSize().padding(horizontal = 20.dp),
            contentPadding = PaddingValues(vertical = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Mes vins", Modifier.weight(1f), style = MaterialTheme.typography.headlineMedium)
                    IconButton(onRefresh, enabled = !state.busy) { Icon(Icons.Outlined.Refresh, "Actualiser") }
                }
                Text("Les vins que j’ai bus", color = Muted)
            }
            item {
                OutlinedTextField(query, { query = it }, label = { Text("Rechercher un vin ou une région") },
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Outlined.Search, null) })
                if (state.offline) Text("Hors ligne · carnet enregistré sur cet appareil", color = Burgundy)
            }
            if (wines.isEmpty()) item {
                Text(if (query.isBlank()) "Votre carnet est vide. Touchez Ajouter pour enregistrer un vin."
                    else "Aucun vin ne correspond à cette recherche.", Modifier.padding(vertical = 24.dp), color = Muted)
            }
            items(wines, key = { it.id }) { wine ->
                Card(colors = CardDefaults.cardColors(containerColor = Paper)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(wine.wine, style = MaterialTheme.typography.titleLarge)
                        Text(listOf(wine.color, wine.vintage, wine.region).filter { it.isNotBlank() }.joinToString(" · "), color = Muted)
                        Text("${wine.rating}/5", color = Burgundy, style = MaterialTheme.typography.titleMedium)
                        if (wine.notes.isNotBlank()) Text(wine.notes)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            IconButton({ onEdit(wine) }, enabled = !state.busy) { Icon(Icons.Outlined.Edit, "Modifier ${wine.wine}") }
                            IconButton({ toDelete = wine }, enabled = !state.busy) { Icon(Icons.Outlined.DeleteOutline, "Supprimer ${wine.wine}") }
                        }
                    }
                }
            }
        }
    }
    toDelete?.let { wine ->
        AlertDialog(onDismissRequest = { toDelete = null }, title = { Text("Supprimer ce vin ?") },
            text = { Text("« ${wine.wine} » sera retiré du carnet.") },
            confirmButton = { TextButton({ onDelete(wine); toDelete = null }, enabled = !state.busy) { Text("Supprimer") } },
            dismissButton = { TextButton({ toDelete = null }) { Text("Annuler") } })
    }
}
