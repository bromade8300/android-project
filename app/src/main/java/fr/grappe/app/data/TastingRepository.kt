package fr.grappe.app.data

import fr.grappe.app.model.Tasting

/** Le ViewModel connaît ce contrat, sans dépendre du stockage choisi. */
interface TastingRepository {
    // Une interface définit les opérations et leurs types. suspend permet à une opération d’attendre le disque ou le réseau dans une coroutine.
    suspend fun load(ownerId: String): List<Tasting>
    suspend fun save(tasting: Tasting)
    suspend fun delete(id: String, ownerId: String)
}
