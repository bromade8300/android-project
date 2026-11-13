package fr.grappe.app.model

import org.junit.Assert.*
import org.junit.Test

class TastingTest {
    private val valid = Tasting(wine = "Château des Rives")

    @Test fun `une degustation valide peut etre enregistree`() {
        assertNull(valid.validationError())
    }

    @Test fun `un nom vide est refuse`() {
        assertNotNull(valid.copy(wine = "   ").validationError())
    }

    @Test fun `les millesimes hors limites et non numeriques sont refuses`() {
        listOf("", "abcd", "1899", "2101").forEach {
            assertNotNull("millésime $it", valid.copy(vintage = it).validationError())
        }
        listOf("1900", "2100").forEach { assertNull(valid.copy(vintage = it).validationError()) }
    }

    @Test fun `la note reste entre un et cinq`() {
        assertNotNull(valid.copy(rating = 0).validationError())
        assertNotNull(valid.copy(rating = 6).validationError())
    }

    @Test fun `les notes trop longues sont refusees`() {
        assertNotNull(valid.copy(notes = "x".repeat(2001)).validationError())
    }
}
