package com.example.lumina.core.database

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.security.KeyStore
import javax.crypto.SecretKey

/**
 * Instrumented test class for [SecurityUtils].
 * Verifies key generation, retrieval consistency, and exposes Keystore hardware-backed boundaries.
 */
@RunWith(AndroidJUnit4::class)
class SecurityUtilsTest {

    private val KEY_ALIAS = "lumina_db_key"
    private val ANDROID_KEYSTORE = "AndroidKeyStore"
    private lateinit var keyStore: KeyStore

    @Before
    fun setUp() {
        keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        // Start with a clean slate for SEC01 (first execution scenario)
        cleanKeystoreAlias()
    }

    @After
    fun tearDown() {
        // Clean up key after test run to prevent side effects
        cleanKeystoreAlias()
    }

    private fun cleanKeystoreAlias() {
        if (keyStore.containsAlias(KEY_ALIAS)) {
            keyStore.deleteEntry(KEY_ALIAS)
        }
    }

    /**
     * [SEC01] Test: Generate database encryption key (First execution)
     * Verifies that when the key is requested for the first time, it is successfully
     * generated and initialized within the Android Keystore.
     */
    @Test
    fun testSEC01_GenerateDatabaseKey_FirstExecution() {
        // 1. Ensure KeyStore does not contain the key initially
        assertFalse("KeyStore should not contain the alias on a clean start", keyStore.containsAlias(KEY_ALIAS))

        // 2. Trigger first-run key generation
        val dbKey = SecurityUtils.getOrCreateDatabaseKey()

        // 3. Verify alias exists in KeyStore now
        assertTrue("KeyStore must generate and save the alias", keyStore.containsAlias(KEY_ALIAS))
        assertNotNull("Generated key must not be null", dbKey)

        // 4. Verify cryptographic entry specs
        val entry = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
        assertNotNull("Keystore entry should be a SecretKeyEntry", entry)
        
        val secretKey = entry?.secretKey
        assertNotNull("Keystore SecretKey must be valid", secretKey)
        assertEquals("AES", secretKey?.algorithm)
    }

    /**
     * [SEC02] Test: Retrieve existing database encryption key (Subsequent boot)
     * Verifies that subsequent requests do not re-generate the key, returning
     * a consistent byte array for decrypting existing database files.
     */
    @Test
    fun testSEC02_RetrieveExistingKey_SubsequentBoot() {
        // 1. Generate key for the first time
        val firstKey = SecurityUtils.getOrCreateDatabaseKey()
        assertTrue("KeyStore must now contain the alias", keyStore.containsAlias(KEY_ALIAS))

        // 2. Retrieve key again representing a subsequent boot/execution
        val secondKey = SecurityUtils.getOrCreateDatabaseKey()

        // 3. Assert keys match byte-for-byte
        assertArrayEquals("Subsequent key retrieval must yield the exact same key bytes", firstKey, secondKey)
    }

    /**
     * [SEC03 - CRITICAL SECURITY VULNERABILITY AUDIT]
     * In Android KeyStore, key material for keys generated with KeyGenParameterSpec is protected
     * by the Secure Hardware (TEE/StrongBox) and is intentionally non-exportable.
     * Therefore, secretKey.encoded ALWAYS returns null, causing getOrCreateDatabaseKey()
     * to fallback to "default_fallback_key_not_secure".toByteArray() on ALL devices.
     *
     * This test asserts that the database key is NOT using the insecure static fallback.
     * NOTE: This test WILL fail under the current codebase implementation, proving the vulnerability.
     */
    @Test
    fun testSEC03_DatabaseKeyIsSecure_NoInsecureFallback() {
        // 1. Generate key
        val dbKey = SecurityUtils.getOrCreateDatabaseKey()
        
        // 2. Identify the insecure fallback byte array
        val insecureFallbackBytes = "default_fallback_key_not_secure".toByteArray()

        // 3. Verify that we are NOT running on a hardcoded fallback
        assertNotEquals(
            "CRITICAL SECURITY FAILURE: SecurityUtils is using the hardcoded static fallback key! " +
            "Android KeyStore secretKey.encoded returned null because hardware-backed keys are non-exportable. " +
            "Use a PBKDF2/scrypt derived key or a wrapped random key instead.",
            insecureFallbackBytes,
            dbKey
        )
    }
}
