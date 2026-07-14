package com.example.security

import android.util.Base64
import com.example.BuildConfig
import java.nio.charset.StandardCharsets

/**
 * 🛡️ AemetCredentialManager
 * Implements Advanced Client-Side Shielding for API Keys.
 * 
 * ⚠️ ADVERTENCIA DE SEGURIDAD CRÍTICA:
 * Guardar claves de API en el código fuente (incluso ofuscadas mediante XOR o NDK) es inseguro.
 * Las APKs de Android se pueden decompilar con facilidad.
 * 
 * ✅ MEJOR PRÁCTICA (Implementada aquí):
 * La clave de API real se almacena de forma segura en el archivo `.env` local (añadido a `.gitignore`),
 * gestionado a través del panel de "Secrets" de AI Studio. El plugin "Secrets Gradle Plugin"
 * inyecta este valor en `BuildConfig.AEMET_API_KEY` durante la compilación.
 */
object AemetCredentialManager {

    // Máscara de sal secreta dinámica para cifrado/descifrado XOR opcional en memoria
    private val SALT_MASK = byteArrayOf(
        0x5A, 0x3F, 0x1C, 0x7E, 0x2A, 0x01, 0x4B, 0x6C, 
        0x12, 0x34, 0x56, 0x78, 0x2E, 0x1A, 0x0F, 0x4D
    )

    /**
     * Obtiene la clave de API de AEMET de forma segura a través de BuildConfig,
     * evitando que esté expuesta públicamente en el repositorio de GitHub.
     */
    fun getAemetApiKey(): String {
        val apiKey = BuildConfig.AEMET_API_KEY
        if (apiKey == "YOUR_AEMET_API_KEY" || apiKey.isBlank()) {
            return ""
        }
        return apiKey
    }

    /**
     * Utilidad de ofuscación opcional en tiempo de ejecución:
     * Toma una clave cruda y genera arreglos ofuscados por XOR si se requiere doble capa en memoria.
     */
    fun obfuscateApiKey(rawKey: String): Map<String, String> {
        val rawBytes = rawKey.toByteArray(StandardCharsets.UTF_8)
        val obfuscated = ByteArray(rawBytes.size)

        for (i in rawBytes.indices) {
            val maskByte = SALT_MASK[i % SALT_MASK.size]
            obfuscated[i] = (rawBytes[i].toInt() xor maskByte.toInt()).toByte()
        }

        return mapOf(
            "hex" to obfuscated.joinToString(", ") { "0x" + String.format("%02X", it) },
            "base64" to Base64.encodeToString(obfuscated, Base64.NO_WRAP)
        )
    }

    /**
     * Descifra dinámicamente un arreglo ofuscado mediante XOR en memoria.
     */
    fun decryptXorKey(obfuscatedBytes: ByteArray): String {
        val decryptedBytes = ByteArray(obfuscatedBytes.size)
        for (i in obfuscatedBytes.indices) {
            val maskByte = SALT_MASK[i % SALT_MASK.size]
            decryptedBytes[i] = (obfuscatedBytes[i].toInt() xor maskByte.toInt()).toByte()
        }
        return String(decryptedBytes, StandardCharsets.UTF_8)
    }
}

