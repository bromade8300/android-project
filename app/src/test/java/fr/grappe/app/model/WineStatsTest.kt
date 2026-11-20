package fr.grappe.app.model

import org.junit.Assert.*
import org.junit.Test

// assertEquals compare la valeur attendue au résultat du calcul. WineStats est une data class, donc ses champs servent à la comparaison.
class WineStatsTest {
    @Test fun `un carnet vide ne produit pas de moyenne invalide`() {
        assertEquals(WineStats(0, 0, null), wineStats(emptyList(), "demo"))
    }

    @Test fun `les statistiques ignorent les autres comptes et normalisent les regions`() {
        // Les deux formes de Bordeaux doivent fusionner, la région vide doit être ignorée et le vin d’un autre compte doit être exclu du calcul.
        val wines = listOf(
            Tasting(ownerId = "demo", region = " Bordeaux ", rating = 2),
            Tasting(ownerId = "demo", region = "bordeaux", rating = 4),
            Tasting(ownerId = "demo", region = "  ", rating = 3),
            Tasting(ownerId = "autre", region = "Loire", rating = 5),
        )
        assertEquals(WineStats(3, 1, 3.0), wineStats(wines, "demo"))
    }
}
