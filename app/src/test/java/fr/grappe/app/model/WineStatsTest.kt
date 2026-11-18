package fr.grappe.app.model

import org.junit.Assert.*
import org.junit.Test

class WineStatsTest {
    @Test fun `un carnet vide ne produit pas de moyenne invalide`() {
        assertEquals(WineStats(0, 0, null), wineStats(emptyList(), "demo"))
    }

    @Test fun `les statistiques ignorent les autres comptes et normalisent les regions`() {
        val wines = listOf(
            Tasting(ownerId = "demo", region = " Bordeaux ", rating = 2),
            Tasting(ownerId = "demo", region = "bordeaux", rating = 4),
            Tasting(ownerId = "demo", region = "  ", rating = 3),
            Tasting(ownerId = "autre", region = "Loire", rating = 5),
        )
        assertEquals(WineStats(3, 1, 3.0), wineStats(wines, "demo"))
    }
}
