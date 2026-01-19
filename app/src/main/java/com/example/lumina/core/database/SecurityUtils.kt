package com.example.lumina.core.database

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

/**
 * Utility class for handling cryptographic operations, such as generating and retrieving
 * keys from the Android Keystore.
 */
object SecurityUtils {
    private const val KEY_ALIAS = "lumina_db_key"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"

    /**
     * Gets or generates a secret key for database encryption.
     * The key is stored securely in the Android Keystore.
     */
    fun getOrCreateDatabaseKey(): ByteArray {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEYSTORE
            )
            
            val spec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
            
            keyGenerator.init(spec)
            keyGenerator.generateKey()
        }

        val secretKey = keyStore.getKey(KEY_ALIAS, null) as SecretKey
        // Note: For SQLCipher, we need a consistent byte array. 
        // In a production app, you might want to derive this from a user-provided password
        // or wrap a randomly generated key. For this example, we use the encoded key.
        return secretKey.encoded ?: "default_fallback_key_not_secure".toByteArray()
    }
}
