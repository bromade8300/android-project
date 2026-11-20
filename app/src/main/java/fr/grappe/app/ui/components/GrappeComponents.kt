package fr.grappe.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import fr.grappe.app.ui.theme.*

// Bouton commun aux écrans. () -> Unit représente une action sans argument ni valeur de retour.
@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(onClick, modifier.heightIn(min = 48.dp), enabled = enabled, shape = RoundedCornerShape(10.dp)) {
        Text(text)
    }
}

// Affiche la photo lorsqu’une adresse existe et utilise le nom pour le visuel de secours.
@Composable
fun Avatar(name: String, modifier: Modifier = Modifier, photo: String = "") {
    Box(modifier.size(42.dp).clip(CircleShape).background(Color(0xFFE7D9D7)), contentAlignment = Alignment.Center) {
        if (photo.isNotBlank()) AsyncImage(photo, "Photo de profil", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        else Text(name.split(" ").filter { it.isNotBlank() }.take(2).map { it.first().uppercase() }.joinToString(""),
            color = Burgundy, fontFamily = FontFamily.Serif)
    }
}

// Les valeurs de 1 à 5 déterminent les étoiles remplies. La fonction onRate est facultative et permet de rendre les étoiles interactives.
@Composable
fun RatingStars(rating: Int, onRate: ((Int) -> Unit)? = null) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        (1..5).forEach { value ->
            val icon = if (value <= rating) Icons.Outlined.Star else Icons.Outlined.StarBorder
            if (onRate == null) Icon(icon, null, Modifier.size(17.dp), tint = Gold)
            else IconButton({ onRate(value) }) { Icon(icon, "Noter $value sur 5", tint = Gold) }
        }
    }
}
