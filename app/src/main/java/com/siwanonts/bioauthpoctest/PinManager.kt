package com.siwanonts.bioauthpoctest

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import androidx.core.content.edit

object KeyStorePinManager {

    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "my_pin_encryption_key"
    private const val PREF_FILE_NAME = "keystore_pin_prefs"
    private const val PREF_ENCRYPTED_HASH = "encrypted_pin_hash"
    private const val PREF_ENCRYPTION_IV = "encryption_iv"

    private const val TRANSFORMATION = "AES/GCM/NoPadding"

    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
        load(null)
    }

    // Gets the secret key from the KeyStore, or creates it if it doesn't exist.
    private fun getOrCreateSecretKey(): SecretKey {
        val existingKey = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
        return existingKey ?: generateSecretKey()
    }

    private fun generateSecretKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val parameterSpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()
        keyGenerator.init(parameterSpec)
        return keyGenerator.generateKey()
    }

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
    }

    // Hashes the PIN (same as before)
    private fun hashPin(pin: String): String {
        val bytes = pin.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    fun savePin(context: Context, pin: String) {
        val pinHash = hashPin(pin)

        // Encrypt the hash
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
        val encryptedData = cipher.doFinal(pinHash.toByteArray())
        val iv = cipher.iv

        // Save the encrypted data and the IV
        getSharedPreferences(context).edit {
            putString(PREF_ENCRYPTED_HASH, Base64.encodeToString(encryptedData, Base64.DEFAULT))
                .putString(PREF_ENCRYPTION_IV, Base64.encodeToString(iv, Base64.DEFAULT))
        }
    }

    fun checkPin(context: Context, enteredPin: String): Boolean {
        val prefs = getSharedPreferences(context)
        val encryptedHashString = prefs.getString(PREF_ENCRYPTED_HASH, null)
        val ivString = prefs.getString(PREF_ENCRYPTION_IV, null)

        if (encryptedHashString == null || ivString == null) return false

        try {
            // Decrypt the stored hash
            val encryptedData = Base64.decode(encryptedHashString, Base64.DEFAULT)
            val iv = Base64.decode(ivString, Base64.DEFAULT)
            val secretKey = getOrCreateSecretKey()

            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
            val decryptedData = cipher.doFinal(encryptedData)
            val storedPinHash = String(decryptedData, Charsets.UTF_8)

            // Compare with the hash of the entered PIN
            val enteredPinHash = hashPin(enteredPin)
            return storedPinHash == enteredPinHash
        } catch (e: Exception) {
            // Decryption failed
            e.printStackTrace()
            return false
        }
    }

    fun isPinSet(context: Context): Boolean {
        return getSharedPreferences(context).contains(PREF_ENCRYPTED_HASH)
    }
}