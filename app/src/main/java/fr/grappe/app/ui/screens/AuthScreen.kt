package fr.grappe.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.grappe.app.BuildConfig
import fr.grappe.app.ui.components.*
import fr.grappe.app.ui.theme.*

@Composable
fun AuthScreen(busy: Boolean, onDemo: () -> Unit, onAuthenticate: (String, String, String, Boolean) -> Unit) {
    var register by rememberSaveable { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    // Le mot de passe n'est pas conservé dans l'état sauvegardé de l'activité.
    var password by remember { mutableStateOf("") }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val tablet = maxWidth >= 700.dp
        Row(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(if (tablet) 48.dp else 28.dp),
            horizontalArrangement = Arrangement.spacedBy(48.dp), verticalAlignment = Alignment.CenterVertically) {
            if (tablet) Column(Modifier.weight(1f).background(Burgundy, RoundedCornerShape(20.dp)).padding(28.dp)) {
                Text("GRAPPE", style = MaterialTheme.typography.labelSmall, color = Paper)
                Spacer(Modifier.height(28.dp))
                BottleArt(Modifier.fillMaxWidth().height(280.dp))
                Spacer(Modifier.height(28.dp))
                Text("Les belles\nbouteilles.\nLes bonnes\npersonnes.", style = MaterialTheme.typography.headlineLarge, color = Paper)
                Spacer(Modifier.height(18.dp))
                Text("Votre carnet de dégustation,\nà savourer ensemble.", color = Paper.copy(alpha = .7f))
            }
            Column(Modifier.weight(1f).widthIn(max = 440.dp)) {
                Box(Modifier.background(Burgundy, RoundedCornerShape(12.dp)).padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text("G", fontFamily = FontFamily.Serif, fontSize = 34.sp, color = Paper)
                }
                Spacer(Modifier.height(24.dp))
                Text(if (register) "Votre histoire commence ici." else "Bienvenue sur Grappe.", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(10.dp))
                Text("Retrouvez les dégustations.\nPartagez les belles découvertes.", color = Muted)
                Spacer(Modifier.height(26.dp))
                if (BuildConfig.CLOUD_ENABLED) {
                    if (register) {
                        OutlinedTextField(name, { name = it }, label = { Text("Nom") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        Spacer(Modifier.height(12.dp))
                    }
                    OutlinedTextField(email, { email = it }, label = { Text("E-mail") }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(password, { password = it }, label = { Text("Mot de passe") }, singleLine = true,
                        visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        supportingText = { Text("8 caractères minimum") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(18.dp))
                    PrimaryButton(if (register) "Créer mon compte" else "Se connecter",
                        { onAuthenticate(name, email, password, register) }, Modifier.fillMaxWidth(), !busy)
                    TextButton({ register = !register }, enabled = !busy) {
                        Text(if (register) "Déjà un compte ? Se connecter" else "Pas encore de compte ? S'inscrire")
                    }
                } else {
                    Text("Un carnet, des souvenirs, des vins à découvrir.", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(24.dp))
                    PrimaryButton("Explorer la démo", onDemo, Modifier.fillMaxWidth(), !busy)
                    Spacer(Modifier.height(12.dp))
                    Text("Mode démo · données enregistrées sur cet appareil.", style = MaterialTheme.typography.bodyMedium, color = Muted)
                }
                Spacer(Modifier.height(30.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = .5f))
                Spacer(Modifier.height(16.dp))
                Text("Les belles découvertes se partagent.\nL'abus d'alcool est dangereux pour la santé. À consommer avec modération.",
                    style = MaterialTheme.typography.bodySmall, color = Muted)
            }
        }
    }
}
