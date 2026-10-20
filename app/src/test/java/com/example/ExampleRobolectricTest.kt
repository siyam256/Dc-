package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.VaultPreferences
import com.example.security.EncryptionManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Stealth Voice", appName)
  }

  @Test
  fun `test default PIN verification`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = VaultPreferences(context)
    assertTrue(prefs.verifyPin("1234"))
  }

  @Test
  fun `test encryption and decryption roundtrip`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val key = EncryptionManager.deriveKey("testpassphrase")

    val originalText = "Stealth audio secret recording byte stream"
    val srcFile = File(context.cacheDir, "test_plain.txt").apply {
      writeText(originalText)
    }
    val encFile = File(context.cacheDir, "test_enc.bin")
    val decFile = File(context.cacheDir, "test_dec.txt")

    val encOk = EncryptionManager.encryptFile(srcFile, encFile, key)
    assertTrue("Encryption should succeed", encOk)
    assertTrue("Encrypted file should exist", encFile.exists())

    val decOk = EncryptionManager.decryptFile(encFile, decFile, key)
    assertTrue("Decryption should succeed", decOk)
    assertEquals(originalText, decFile.readText())

    srcFile.delete()
    encFile.delete()
    decFile.delete()
  }
}
