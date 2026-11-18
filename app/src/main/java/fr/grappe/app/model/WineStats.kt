package fr.grappe.app.model

/** Trois statistiques calculées sur les seuls vins du propriétaire. */
data class WineStats(val count: Int, val regions: Int, val average: Double?)

fun wineStats(tastings: List<Tasting>, ownerId: String): WineStats {
    val mine = tastings.filter { it.ownerId == ownerId }
    return WineStats(
        count = mine.size,
        regions = mine.map { it.region.trim().lowercase(java.util.Locale.ROOT) }.filter { it.isNotEmpty() }.distinct().size,
        average = if (mine.isEmpty()) null else mine.map { it.rating }.average(),
    )
}
