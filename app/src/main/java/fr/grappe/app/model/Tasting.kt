package fr.grappe.app.model

import kotlinx.serialization.Serializable
import java.util.UUID

/** Élément métier du CRUD : une dégustation appartient à un utilisateur. */
// @Serializable permet de convertir une fiche en JSON et de la relire. Une data class fournit notamment copy(), utile pour modifier quelques champs en gardant l’identifiant.
@Serializable
data class Tasting(
    // Les valeurs après = servent par défaut. Un nouveau vin reçoit un UUID, une chaîne qui identifie sa fiche.
    val id: String = UUID.randomUUID().toString(),
    val ownerId: String = "demo",
    val author: String = "Thomas Martin",
    val wine: String = "",
    val vintage: String = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR).toString(),
    val color: String = "Rouge",
    val region: String = "",
    val rating: Int = 4,
    val notes: String = "",
    val photoUri: String = "",
    val createdAt: Long = System.currentTimeMillis(),
)

// Le profil regroupe l’identité du compte et l’adresse de sa photo.
data class UserProfile(
    val id: String,
    val name: String,
    val email: String,
    val photoUri: String = "",
)

/** Validation commune à l'ajout et à la modification, indépendante de l'interface. */
// Cette fonction d’extension s’appelle comme si elle appartenait à Tasting. String? signifie qu’elle renvoie un message d’erreur ou null. when retient la première condition vraie.
fun Tasting.validationError(): String? = when {
    wine.isBlank() -> "Indique le nom du vin."
    wine.length > 120 -> "Le nom du vin est limité à 120 caractères."
    // toIntOrNull renvoie null si la saisie n’est pas un entier. 1900..2100 décrit l’intervalle inclusif.
    vintage.toIntOrNull() !in 1900..2100 -> "Le millésime doit être compris entre 1900 et 2100."
    rating !in 1..5 -> "Choisis une note entre 1 et 5."
    notes.length > 2000 -> "Les notes sont limitées à 2 000 caractères."
    else -> null
}
