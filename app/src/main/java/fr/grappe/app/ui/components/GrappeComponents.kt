package fr.grappe.app.ui.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import fr.grappe.app.ui.theme.*

@Composable
fun BrandHeader(eyebrow: String, title: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Outlined.WineBar, contentDescription = null, tint = Burgundy, modifier = Modifier.size(20.dp))
        Text("grappe", fontFamily = FontFamily.Serif, fontSize = 23.sp, color = Burgundy)
        Spacer(Modifier.weight(1f))
        Text("LES BELLES DÉCOUVERTES SE PARTAGENT.", style = MaterialTheme.typography.labelSmall,
            color = Muted, modifier = Modifier.widthIn(max = 180.dp))
    }
    Spacer(Modifier.height(26.dp))
    Eyebrow(eyebrow)
    Text(title, style = MaterialTheme.typography.headlineMedium)
    Spacer(Modifier.height(18.dp))
}

@Composable
fun Eyebrow(text: String) {
    Text(text.uppercase(), style = MaterialTheme.typography.labelSmall, color = Gold)
    Spacer(Modifier.height(5.dp))
}

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(onClick, modifier.heightIn(min = 48.dp), enabled = enabled, shape = RoundedCornerShape(10.dp)) {
        Text(text)
    }
}

@Composable
fun Avatar(name: String, modifier: Modifier = Modifier, photo: String = "") {
    Box(modifier.size(42.dp).clip(CircleShape).background(Color(0xFFE7D9D7)), contentAlignment = Alignment.Center) {
        if (photo.isNotBlank()) AsyncImage(photo, "Photo de profil", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        else Text(name.split(" ").filter { it.isNotBlank() }.take(2).map { it.first().uppercase() }.joinToString(""),
            color = Burgundy, fontFamily = FontFamily.Serif)
    }
}

/** Illustration vectorielle native : nette sur téléphone et tablette, sans image distante. */
@Composable
fun BottleArt(modifier: Modifier = Modifier, color: String = "Rouge") {
    Canvas(modifier.clip(RoundedCornerShape(14.dp)).background(Sage)) {
        val w = size.width; val h = size.height
        drawCircle(Color(0xFFDCDCCB), w * .42f, Offset(w * .18f, h * .22f))
        drawRect(Color(0xFFAAA68E), Offset(0f, h * .73f), Size(w, h * .27f))
        val bottle = when (color) { "Blanc" -> Color(0xFF899468); "Rosé" -> Color(0xFFB98277); else -> Color(0xFF354538) }
        drawRoundRect(bottle, Offset(w * .43f, h * .23f), Size(w * .16f, h * .68f), CornerRadius(w * .04f))
        drawRect(bottle, Offset(w * .48f, h * .08f), Size(w * .06f, h * .23f))
        drawRect(Burgundy, Offset(w * .48f, h * .07f), Size(w * .06f, h * .12f))
        drawRect(Paper, Offset(w * .43f, h * .46f), Size(w * .16f, h * .26f))
        drawLine(Burgundy, Offset(w * .46f, h * .53f), Offset(w * .56f, h * .53f), 3f)
        drawLine(Gold, Offset(w * .48f, h * .63f), Offset(w * .54f, h * .63f), 2f)
        val glassX = w * .76f; val glassY = h * .47f
        drawRoundRect(Paper, Offset(glassX, glassY), Size(w * .09f, h * .17f), CornerRadius(w * .035f), style = Stroke(2f))
        drawLine(Paper, Offset(glassX + w * .045f, glassY + h * .17f), Offset(glassX + w * .045f, h * .81f), 2f)
        drawLine(Paper, Offset(glassX, h * .81f), Offset(glassX + w * .09f, h * .81f), 2f)
    }
}

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
