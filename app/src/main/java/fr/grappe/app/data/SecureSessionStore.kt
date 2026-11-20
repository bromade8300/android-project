package fr.grappe.app.data

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Les jetons Supabase sont chiffrés avec une clé non exportable du Keystore Android. */
class SecureSessionStore(context: Context) {
    private val preferences = context.getSharedPreferences("session", Context.MODE_PRIVATE)
    private val alias = "grappe-session"

    // as? tente une conversion de type et renvoie null si elle échoue. let réutilise la clé si elle existe, sinon le Keystore en génère une.
    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(alias, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
            init(KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build())
        }.generateKey()
    }

    // On chiffre la session puis on conserve le vecteur d’initialisation et le texte chiffré. Base64 permet de représenter ces octets sous forme de texte.
    fun write(value: String) {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.ENCRYPT_MODE, key()) }
        val encrypted = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
        preferences.edit().putString("value", encode(cipher.iv) + ":" + encode(encrypted)).apply()
    }

    // runCatching capture les erreurs de lecture ou de déchiffrement. getOrNull renvoie null en cas d’échec.
    fun read(): String? = runCatching {
        val parts = preferences.getString("value", null)?.split(":") ?: return null
        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply {
            init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, decode(parts[0])))
        }
        String(cipher.doFinal(decode(parts[1])), Charsets.UTF_8)
    }.getOrNull()

    // Supprime la session enregistrée dans les préférences de ce fichier.
    fun clear() { preferences.edit().clear().apply() }
    private fun encode(bytes: ByteArray) = Base64.encodeToString(bytes, Base64.NO_WRAP)
    private fun decode(value: String) = Base64.decode(value, Base64.NO_WRAP)
}
