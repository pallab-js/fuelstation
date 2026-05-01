package com.yourcompany.pumpmanager.core.security

import java.security.MessageDigest

object PinHasher {
    /** Returns a storable string in the format "salt:hash". */
    fun hash(pin: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest("$salt$pin".toByteArray(Charsets.UTF_8))
        val hash = hashBytes.joinToString("") { "%02x".format(it) }
        return "$salt:$hash"
    }

    /** Verifies a plain PIN against a stored "salt:hash" string. */
    fun verify(pin: String, stored: String): Boolean {
        val salt = stored.substringBefore(":")
        return hash(pin, salt) == stored
    }
}
