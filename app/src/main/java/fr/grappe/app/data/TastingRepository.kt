package fr.grappe.app.data

import fr.grappe.app.model.Tasting

/** Le ViewModel connaît ce contrat, sans dépendre du stockage choisi. */
interface TastingRepository {
    suspend fun load(ownerId: String): List<Tasting>
    suspend fun save(tasting: Tasting)
    suspend fun delete(id: String, ownerId: String)
}
