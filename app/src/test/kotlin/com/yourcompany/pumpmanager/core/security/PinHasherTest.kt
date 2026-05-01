package com.yourcompany.pumpmanager.core.security

import org.junit.Assert.*
import org.junit.Test

class PinHasherTest {

    @Test
    fun `same pin and salt produces same hash`() {
        val h1 = PinHasher.hash("1234", "salt-abc")
        val h2 = PinHasher.hash("1234", "salt-abc")
        assertEquals(h1, h2)
    }

    @Test
    fun `different salts produce different hashes`() {
        val h1 = PinHasher.hash("1234", "salt-1")
        val h2 = PinHasher.hash("1234", "salt-2")
        assertNotEquals(h1, h2)
    }

    @Test
    fun `verify returns true for correct pin`() {
        val stored = PinHasher.hash("1234", "my-salt")
        assertTrue(PinHasher.verify("1234", stored))
    }

    @Test
    fun `verify returns false for wrong pin`() {
        val stored = PinHasher.hash("1234", "my-salt")
        assertFalse(PinHasher.verify("9999", stored))
    }

    @Test
    fun `hash format contains salt prefix`() {
        val stored = PinHasher.hash("1234", "my-salt")
        assertTrue(stored.startsWith("my-salt:"))
    }
}
