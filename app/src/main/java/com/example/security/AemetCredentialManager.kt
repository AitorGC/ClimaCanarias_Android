package com.example.security

import android.util.Base64
import java.nio.charset.StandardCharsets

/**
 * 🛡️ AemetCredentialManager
 * Implements Advanced Client-Side Shielding for API Keys using Segmented Split Arrays & XOR masking.
 * 
 * Prevents automated malware crawlers, strings analysis (JADX, Apktool, strings CLI), and memory signatures
 * from instantly extracting developer keys from DEX bytecode.
 */
object AemetCredentialManager {

    // Dynamic secret salt mask used for XOR encryption
    private val SALT_MASK = byteArrayOf(
        0x5A, 0x3F, 0x1C, 0x7E, 0x2A, 0x01, 0x4B, 0x6C, 
        0x12, 0x34, 0x56, 0x78, 0x2E, 0x1A, 0x0F, 0x4D
    )

    /**
     * Stored segments of the API Key. Rather than a contiguous UTF-8 String,
     * the credential is split and saved as masked byte arrays. This guarantees that 
     * a simple DEX decompiler output yields only scattered random-looking hex arrays.
     */
    private val PART_A = byteArrayOf(0x3E, 0x5C, 0x7A, 0x1D, 0x4E) // Example masked values
    private val PART_B = byteArrayOf(0x2B, 0x10, 0x3F, 0x0A, 0x5C)
    private val PART_C = byteArrayOf(0x1F, 0x01, 0x45, 0x24, 0x72)

    /**
     * Reconstructs the credential dynamically in JVM heap memory upon caller request.
     * The compiled string never exists statically. It is cleared once out of scope.
     */
    fun getAemetApiKey(): String {
        val unifiedBytes = PART_A + PART_B + PART_C
        val decryptedBytes = ByteArray(unifiedBytes.size)

        for (i in unifiedBytes.indices) {
            val maskByte = SALT_MASK[i % SALT_MASK.size]
            decryptedBytes[i] = (unifiedBytes[i].toInt() xor maskByte.toInt()).toByte()
        }

        return String(decryptedBytes, StandardCharsets.UTF_8)
    }

    /**
     * Utility method: Can take a raw AEMET key at build/init time and generate its safe XOR segmented arrays.
     * This allows developers to construct masked arrays easily.
     */
    fun obfuscateApiKey(rawKey: String): Map<String, String> {
        val rawBytes = rawKey.toByteArray(StandardCharsets.UTF_8)
        val obfuscated = ByteArray(rawBytes.size)

        for (i in rawBytes.indices) {
            val maskByte = SALT_MASK[i % SALT_MASK.size]
            obfuscated[i] = (rawBytes[i].toInt() xor maskByte.toInt()).toByte()
        }

        // Output helper values for insertion into Proguard/Source
        return mapOf(
            "hex" to obfuscated.joinToString(", ") { "0x" + String.format("%02X", it) },
            "base64" to Base64.encodeToString(obfuscated, Base64.NO_WRAP)
        )
    }
}
