package fr.grappe.app.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Burgundy = Color(0xFF601D30)
val Cream = Color(0xFFF0EBE2)
val Paper = Color(0xFFFBF8F2)
val Ink = Color(0xFF39332E)
val Muted = Color(0xFF847C72)
val Gold = Color(0xFFA48737)
val Sage = Color(0xFFBDC1A6)

private val colors = lightColorScheme(
    primary = Burgundy, onPrimary = Paper, primaryContainer = Color(0xFFE9DADC),
    background = Cream, onBackground = Ink,
    surface = Paper, onSurface = Ink, surfaceVariant = Color(0xFFE5DED4),
    onSurfaceVariant = Muted, outline = Color(0xFFD4C9BB), secondary = Gold,
    secondaryContainer = Color(0xFFE9DADC), onSecondaryContainer = Burgundy,
)

// Le thème applique une palette et des styles de texte communs au contenu Compose reçu en paramètre.
@Composable
fun GrappeTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = colors, typography = Typography(
        headlineLarge = TextStyle(fontFamily = FontFamily.Serif, fontSize = 34.sp, lineHeight = 39.sp),
        headlineMedium = TextStyle(fontFamily = FontFamily.Serif, fontSize = 28.sp, lineHeight = 34.sp),
        titleLarge = TextStyle(fontFamily = FontFamily.Serif, fontSize = 22.sp, lineHeight = 28.sp),
        titleMedium = TextStyle(fontFamily = FontFamily.Serif, fontSize = 18.sp, lineHeight = 24.sp),
        bodyLarge = TextStyle(fontSize = 15.sp, lineHeight = 23.sp),
        bodyMedium = TextStyle(fontSize = 13.sp, lineHeight = 20.sp),
        labelSmall = TextStyle(fontSize = 10.sp, letterSpacing = 1.3.sp, fontWeight = FontWeight.Medium),
    ), content = content)
}
