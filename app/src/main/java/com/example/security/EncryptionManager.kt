package com.example.security

import android.content.Context
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object EncryptionManager {

    private const val ALGORITHM = "AES/CBC/PKCS5Padding"
    private const val KEY_SPEC_ALGORITHM = "AES"
    private const val IV_SIZE = 16

    /**
     * Derives a 256-bit AES key from a passphrase and salt using SHA-256.
     */
    fun deriveKey(passphrase: String, salt: String = "StealthVaultAudioSecureSalt2026"): SecretKeySpec {
        val digest = MessageDigest.getInstance("SHA-256")
        val combined = "$salt:$passphrase".toByteArray(Charsets.UTF_8)
        val keyBytes = digest.digest(combined)
        return SecretKeySpec(keyBytes, KEY_SPEC_ALGORITHM)
    }

    /**
     * Computes SHA-256 hex string for PIN comparison.
     */
    fun hashPin(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(pin.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Encrypts a source file to a destination file.
     * Prepend 16 bytes IV to the encrypted file.
     */
    fun encryptFile(sourceFile: File, destinationFile: File, secretKey: SecretKeySpec): Boolean {
        return try {
            val iv = ByteArray(IV_SIZE)
            SecureRandom().nextBytes(iv)
            val ivSpec = IvParameterSpec(iv)

            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)

            FileOutputStream(destinationFile).use { fos ->
                // Write IV first
                fos.write(iv)
                CipherOutputStream(fos, cipher).use { cos ->
                    FileInputStream(sourceFile).use { fis ->
                        fis.copyTo(cos)
                    }
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Decrypts an encrypted file to a temporary destination file.
     */
    fun decryptFile(encryptedFile: File, destinationFile: File, secretKey: SecretKeySpec): Boolean {
        return try {
            FileInputStream(encryptedFile).use { fis ->
                val iv = ByteArray(IV_SIZE)
                val readIv = fis.read(iv)
                if (readIv != IV_SIZE) return false

                val ivSpec = IvParameterSpec(iv)
                val cipher = Cipher.getInstance(ALGORITHM)
                cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)

                CipherInputStream(fis, cipher).use { cis ->
                    FileOutputStream(destinationFile).use { fos ->
                        cis.copyTo(fos)
                    }
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
