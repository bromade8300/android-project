package fr.grappe.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.grappe.app.ui.GrappeApp

/** Point d'entrée Android ; l'interface et son état sont définis dans ui/. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Autorise l’interface à s’étendre derrière les barres système. Le contenu Compose gère ses marges.
        enableEdgeToEdge()
        // setContent installe l’interface Compose. viewModel() récupère le ViewModel associé à l’activité.
        setContent { GrappeApp(viewModel()) }
    }
}
