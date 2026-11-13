package fr.grappe.app.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class WineSuggestion(val id: Int = 0, val wine: String = "", val winery: String = "", val location: String = "")

/** API externe indépendante de Supabase, appelée uniquement à la demande. */
class WineApi {
    private val client = HttpClient(Android) {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        install(HttpTimeout) { requestTimeoutMillis = 15000 }
        expectSuccess = true
    }
    suspend fun suggestions(): List<WineSuggestion> =
        client.get("https://api.sampleapis.com/wines/reds").body<List<WineSuggestion>>().take(6)
    fun close() { client.close() }
}
