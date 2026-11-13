package fr.grappe.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import fr.grappe.app.model.Tasting
import fr.grappe.app.ui.GrappeUiState
import fr.grappe.app.ui.components.*
import fr.grappe.app.ui.theme.*

@Composable
fun HomeScreen(state: GrappeUiState, onAdd: () -> Unit, onEdit: (Tasting) -> Unit,
               onDelete: (Tasting) -> Unit, onRefresh: () -> Unit, onDiscover: () -> Unit) {
    var filter by rememberSaveable { mutableStateOf("Découvertes") }
    var query by rememberSaveable { mutableStateOf("") }
    var toDelete by remember { mutableStateOf<Tasting?>(null) }
    val tastings = state.tastings.filter {
        (filter != "Mes avis" || it.ownerId == state.user?.id) &&
            (it.wine.contains(query, true) || it.region.contains(query, true))
    }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val tablet = maxWidth >= 700.dp
        Row(Modifier.fillMaxSize().padding(horizontal = if (tablet) 32.dp else 20.dp), horizontalArrangement = Arrangement.spacedBy(28.dp)) {
            LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(top = 24.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item { BrandHeader("Le fil des vins", "À votre santé.") }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Découvertes", "Mes avis").forEach { label ->
                                FilterChip(filter == label, { filter = label }, label = { Text(label) },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Burgundy, selectedLabelColor = Paper))
                            }
                        }
                        IconButton(onRefresh, enabled = !state.busy) { Icon(Icons.Outlined.Refresh, "Actualiser") }
                    }
                    OutlinedTextField(query, { query = it }, placeholder = { Text("Un vin, une région…") }, singleLine = true,
                        leadingIcon = { Icon(Icons.Outlined.Search, null) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    if (state.offline) Text("Hors ligne · dernières dégustations enregistrées", color = Burgundy, style = MaterialTheme.typography.bodySmall)
                }
                if (tastings.isEmpty()) item {
                    Column(Modifier.fillMaxWidth().padding(vertical = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.WineBar, null, Modifier.size(48.dp), tint = Burgundy)
                        Text("Le début d'une belle collection.", style = MaterialTheme.typography.titleLarge)
                        Text("Ajoutez votre première dégustation.", color = Muted)
                        Spacer(Modifier.height(16.dp))
                        PrimaryButton("Ajouter une dégustation", onAdd)
                    }
                }
                items(tastings, key = { it.id }) { tasting ->
                    TastingCard(tasting, tasting.ownerId == state.user?.id, { onEdit(tasting) }, { toDelete = tasting }, !state.busy)
                }
                if (!tablet) item { DiscoveryPanel(state, onAdd, onDiscover) }
            }
            if (tablet) LazyColumn(Modifier.width(260.dp), contentPadding = PaddingValues(top = 156.dp, bottom = 24.dp)) {
                item { DiscoveryPanel(state, onAdd, onDiscover) }
            }
        }
    }
    toDelete?.let { tasting ->
        AlertDialog(onDismissRequest = { toDelete = null }, title = { Text("Supprimer cette dégustation ?") },
            text = { Text("« ${tasting.wine} » sera retirée de votre carnet.") },
            confirmButton = { TextButton({ onDelete(tasting); toDelete = null }) { Text("Supprimer") } },
            dismissButton = { TextButton({ toDelete = null }) { Text("Annuler") } })
    }
}

@Composable
private fun TastingCard(tasting: Tasting, owned: Boolean, onEdit: () -> Unit, onDelete: () -> Unit, enabled: Boolean) {
    Card(colors = CardDefaults.cardColors(containerColor = Paper), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(tasting.author)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(tasting.author, style = MaterialTheme.typography.labelLarge)
                    Text("Une bouteille, un souvenir.", color = Muted, style = MaterialTheme.typography.bodySmall)
                }
                if (owned) {
                    IconButton(onEdit, enabled = enabled) { Icon(Icons.Outlined.Edit, "Modifier ${tasting.wine}", Modifier.size(20.dp), tint = Burgundy) }
                    IconButton(onDelete, enabled = enabled) { Icon(Icons.Outlined.DeleteOutline, "Supprimer ${tasting.wine}", Modifier.size(20.dp), tint = Muted) }
                }
            }
            if (tasting.photoUri.isBlank()) BottleArt(Modifier.fillMaxWidth().height(190.dp), tasting.color)
            else AsyncImage(tasting.photoUri, "Photo de ${tasting.wine}", Modifier.fillMaxWidth().height(190.dp), contentScale = ContentScale.Crop)
            Text("${tasting.color}   ·   ${tasting.region}   ·   ${tasting.vintage}", style = MaterialTheme.typography.bodySmall, color = Muted)
            Text(tasting.wine, style = MaterialTheme.typography.titleLarge)
            Row(verticalAlignment = Alignment.CenterVertically) {
                RatingStars(tasting.rating)
                Text("  ${tasting.rating}/5", color = Gold, style = MaterialTheme.typography.bodySmall)
            }
            if (tasting.notes.isNotBlank()) Text(tasting.notes, color = Muted)
        }
    }
}

@Composable
private fun DiscoveryPanel(state: GrappeUiState, onAdd: () -> Unit, onDiscover: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Column(Modifier.fillMaxWidth().background(Burgundy, RoundedCornerShape(16.dp)).padding(20.dp)) {
            Text("LE PLAISIR DU PARTAGE", style = MaterialTheme.typography.labelSmall, color = Paper.copy(alpha = .7f))
            Spacer(Modifier.height(12.dp))
            Text("Votre prochaine\nbelle découverte.", style = MaterialTheme.typography.headlineMedium, color = Paper)
            Spacer(Modifier.height(10.dp))
            Text("Chaque bouteille mérite qu'on s'en souvienne.", color = Paper.copy(alpha = .75f))
            Spacer(Modifier.height(16.dp))
            FilledTonalButton(onAdd, shape = RoundedCornerShape(10.dp)) { Text("Ajouter une dégustation") }
        }
        Text("Ailleurs dans les vignes", style = MaterialTheme.typography.titleLarge)
        Text("Des idées de vins rouges issues du catalogue SampleAPIs.", color = Muted)
        OutlinedButton(onDiscover, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) { Text("Découvrir des vins") }
        state.suggestions.forEach { wine ->
            Column {
                Text(wine.wine, style = MaterialTheme.typography.titleMedium)
                Text("${wine.winery} · ${wine.location}", style = MaterialTheme.typography.bodySmall, color = Muted)
            }
        }
    }
}
