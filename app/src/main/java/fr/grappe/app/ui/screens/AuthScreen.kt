package fr.grappe.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import fr.grappe.app.BuildConfig
import fr.grappe.app.ui.components.*
import fr.grappe.app.ui.theme.*

// L’écran choisit entre l’entrée en démo et le formulaire de compte selon la variante compilée.
@Composable
fun AuthScreen(busy: Boolean, onDemo: () -> Unit, onAuthenticate: (String, String, String, Boolean) -> Unit) {
    // register sélectionne l’inscription ou la connexion. Le nom et l’e-mail survivent à une recréation de l’écran.
    var register by rememberSaveable { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    // Le mot de passe n'est pas conservé dans l'état sauvegardé de l'activité.
    var password by remember { mutableStateOf("") }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(Modifier.widthIn(max = 480.dp).fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Grappe", style = MaterialTheme.typography.headlineLarge, color = Burgundy)
            Text("Ma bibliothèque des vins bus", color = Muted)
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
                // Le bouton transmet les champs au ViewModel via la fonction reçue. !busy le désactive pendant une opération.
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
        }
    }
}
