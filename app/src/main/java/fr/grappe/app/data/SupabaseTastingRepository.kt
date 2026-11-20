package fr.grappe.app.data

import fr.grappe.app.model.Tasting
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** DTO réseau séparé du modèle métier : les noms SQL ne contaminent pas les écrans. */
@Serializable
private data class TastingRow(
    val id: String,
    // @SerialName relie le nom Kotlin au nom attendu dans le JSON de Supabase.
    @SerialName("owner_id") val ownerId: String,
    val author: String,
    val wine: String,
    val vintage: String,
    val color: String,
    val region: String,
    val rating: Int,
    val notes: String,
    @SerialName("photo_uri") val photoUri: String,
    @SerialName("created_at") val createdAt: Long,
) {
    // Convertit une ligne reçue du serveur en fiche utilisée par l’application.
    fun toModel() = Tasting(id, ownerId, author, wine, vintage, color, region, rating, notes, photoUri, createdAt)
}

class SupabaseTastingRepository(private val service: SupabaseService) : TastingRepository {
    // GET lit les lignes du propriétaire, de la plus récente à la plus ancienne. Les autorisations sont définies séparément côté serveur.
    override suspend fun load(ownerId: String): List<Tasting> {
        val token = service.accessToken()
        val response = service.client.get(service.endpoint("/rest/v1/tastings")) {
            with(service) { headers(token) }
            parameter("select", "*"); parameter("order", "created_at.desc")
            parameter("owner_id", "eq.$ownerId")
        }
        service.ensureSuccess(response.status)
        return response.body<List<TastingRow>>().map { it.toModel() }
    }

    // Le POST demande à Supabase de fusionner la ligne si son identifiant existe déjà. La même opération sert à l’ajout et à la modification.
    override suspend fun save(tasting: Tasting) {
        val token = service.accessToken()
        val response = service.client.post(service.endpoint("/rest/v1/tastings")) {
            with(service) { headers(token) }
            header("Prefer", "resolution=merge-duplicates,return=minimal")
            setBody(with(tasting) { TastingRow(id, ownerId, author, wine, vintage, color, region, rating, notes, photoUri, createdAt) })
        }
        service.ensureSuccess(response.status)
    }

    // DELETE cible la combinaison de l’identifiant de la fiche et de celui du propriétaire.
    override suspend fun delete(id: String, ownerId: String) {
        val token = service.accessToken()
        val response = service.client.delete(service.endpoint("/rest/v1/tastings")) {
            with(service) { headers(token) }
            parameter("id", "eq.$id"); parameter("owner_id", "eq.$ownerId")
        }
        service.ensureSuccess(response.status)
    }

    // Envoie les octets JPEG dans le dossier du compte et renvoie l’URL publique de la photo.
    suspend fun uploadPhoto(ownerId: String, bytes: ByteArray): String {
        val path = "$ownerId/${java.util.UUID.randomUUID()}.jpg"
        val token = service.accessToken()
        val response = service.client.post(service.endpoint("/storage/v1/object/photos/$path")) {
            with(service) { headers(token) }
            contentType(ContentType.Image.JPEG); setBody(bytes)
        }
        service.ensureSuccess(response.status)
        return service.endpoint("/storage/v1/object/public/photos/$path")
    }
}
