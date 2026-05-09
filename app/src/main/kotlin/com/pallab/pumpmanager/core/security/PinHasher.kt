package com.pallab.pumpmanager.core.security

import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PinHasher {
    private const val ITERATIONS = 120_000
    private const val KEY_LENGTH = 256
    private const val ALGORITHM = "PBKDF2WithHmacSHA256"

    fun hash(pin: String, salt: String): String {
        val spec = PBEKeySpec(pin.toCharArray(), salt.toByteArray(Charsets.UTF_8), ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance(ALGORITHM)
        val hash = factory.generateSecret(spec).encoded
        return "$salt:${hash.toHex()}"
    }

    fun verify(pin: String, stored: String): Boolean {
        if (stored.isBlank()) return false
        val salt = stored.substringBefore(":")
        return hash(pin, salt) == stored
    }

    private fun ByteArray.toHex() = joinToString("") { "%02x".format(it) }
}
