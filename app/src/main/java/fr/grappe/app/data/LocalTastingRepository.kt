package fr.grappe.app.data

import android.content.Context
import fr.grappe.app.model.Tasting
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Stockage du mode démo et cache de lecture du mode cloud. Aucun mot de passe ici. */
class LocalTastingRepository(context: Context, private val seedDemo: Boolean = true) : TastingRepository {
    // Les préférences privées contiennent une chaîne JSON par propriétaire. Le parseur accepte des champs supplémentaires pour relire les données existantes.
    private val preferences = context.getSharedPreferences("tastings", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    // override indique que la méthode implémente le contrat du dépôt. Dispatchers.IO exécute la lecture dans un contexte adapté aux entrées-sorties.
    override suspend fun load(ownerId: String): List<Tasting> = withContext(Dispatchers.IO) {
        val saved = preferences.getString(ownerId, null)
        // Une liste enregistrée, même vide, prime sur les données d’exemple.
        if (saved != null) json.decodeFromString(saved)
        else if (seedDemo && ownerId == "demo") demoTastings else emptyList()
    }

    // commit() attend le résultat de l’écriture. check signale son échec si Android renvoie false.
    suspend fun replace(ownerId: String, tastings: List<Tasting>) = withContext(Dispatchers.IO) {
        check(preferences.edit().putString(ownerId, json.encodeToString(tastings)).commit()) {
            "Impossible d'enregistrer les dégustations sur cet appareil."
        }
    }

    override suspend fun save(tasting: Tasting) {
        // On retire l’ancienne fiche du même identifiant, puis + ajoute la nouvelle version à la liste.
        val updated = load(tasting.ownerId).filterNot { it.id == tasting.id } + tasting
        replace(tasting.ownerId, updated.sortedByDescending { it.createdAt })
    }

    // On réécrit la liste du compte en retirant la fiche sélectionnée.
    override suspend fun delete(id: String, ownerId: String) {
        replace(ownerId, load(ownerId).filterNot { it.id == id })
    }
}

// Ce jeu de données est utilisé au premier chargement du carnet de démo.
private val demoTastings = listOf(
    Tasting(id = "demo-bordeaux", wine = "Château des Rives", notes = "Un beau dimanche en rouge.\nDes fruits noirs, une belle rondeur… et surtout une très belle compagnie.", createdAt = 3),
    Tasting(id = "demo-loire", wine = "Les Hauts de Loire", vintage = "2023", color = "Blanc", region = "Vouvray", rating = 5, notes = "De la fraîcheur, des fleurs blanches et le goût des retrouvailles.", createdAt = 2),
    Tasting(id = "demo-provence", wine = "Domaine des Oliviers", vintage = "2024", color = "Rosé", region = "Provence", rating = 4, notes = "Une bouteille à partager quand le soleil s'invite à table.", createdAt = 1),
)
