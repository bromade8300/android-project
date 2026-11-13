package fr.grappe.app.model

import kotlinx.serialization.Serializable
import java.util.UUID

/** Élément métier du CRUD : une dégustation appartient à un utilisateur. */
@Serializable
data class Tasting(
    val id: String = UUID.randomUUID().toString(),
    val ownerId: String = "demo",
    val author: String = "Thomas Martin",
    val wine: String = "",
    val vintage: String = "2021",
    val color: String = "Rouge",
    val region: String = "Saint-Émilion",
    val rating: Int = 4,
    val notes: String = "",
    val photoUri: String = "",
    val createdAt: Long = System.currentTimeMillis(),
)

data class UserProfile(
    val id: String,
    val name: String,
    val email: String,
    val photoUri: String = "",
)

/** Validation commune à l'ajout et à la modification, indépendante de l'interface. */
fun Tasting.validationError(): String? = when {
    wine.isBlank() -> "Indique le nom du vin."
    wine.length > 120 -> "Le nom du vin est limité à 120 caractères."
    vintage.toIntOrNull() !in 1900..2100 -> "Le millésime doit être compris entre 1900 et 2100."
    rating !in 1..5 -> "Choisis une note entre 1 et 5."
    notes.length > 2000 -> "Les notes sont limitées à 2 000 caractères."
    else -> null
}
