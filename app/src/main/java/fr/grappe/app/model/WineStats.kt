package fr.grappe.app.model

/** Trois statistiques calculées sur les seuls vins du propriétaire. */
data class WineStats(val count: Int, val regions: Int, val average: Double?)

// Le calcul dépend uniquement de la liste et de l’identifiant reçus. Aucun accès réseau ni stockage.
fun wineStats(tastings: List<Tasting>, ownerId: String): WineStats {
    // filter garde les éléments qui respectent la condition. it désigne ici chaque dégustation.
    val mine = tastings.filter { it.ownerId == ownerId }
    return WineStats(
        count = mine.size,
        // map transforme chaque région. On retire les espaces autour, uniformise la casse, élimine les valeurs vides puis supprime les doublons.
        regions = mine.map { it.region.trim().lowercase(java.util.Locale.ROOT) }.filter { it.isNotEmpty() }.distinct().size,
        // Double? autorise null pour un carnet vide. Sinon average() calcule la moyenne des notes.
        average = if (mine.isEmpty()) null else mine.map { it.rating }.average(),
    )
}
