
package org.oppia.android.scripts.release

import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.scripts.common.CommandExecutor
import org.oppia.android.scripts.common.CommandResult
import org.oppia.android.testing.assertThrows
import java.io.File
import java.io.FileInputStream
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream

/**
 * Tests for [CloudKmsSigner].
 *
 * Covers the precondition-failure paths that do not require real GCP credentials (missing AAB
 * file), and verifies that `jarsigner` is invoked with the correct arguments — including the
 * jsign JCA provider flags and the full KMS resource name as the key alias — by injecting a
 * [CommandExecutor] that captures command invocations.
 */
// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
class CloudKmsSignerTest {
  @field:[Rule JvmField] val tempFolder = TemporaryFolder()

  private val kmsKeyResourceName =
    "projects/p/locations/global/keyRings/r/cryptoKeys/k/cryptoKeyVersions/1"
  private val expectedKeyRingPath = "projects/p/locations/global/keyRings/r"

  /**
   * A [CommandExecutor] stub that fails loudly if called. Used in tests that target code paths
   * which exit before [CommandExecutor.executeCommand] is ever reached.
   */
  private val unusedCommandExecutor = object : CommandExecutor {
    override fun executeCommand(
      workingDir: File,
      command: String,
      vararg arguments: String,
      includeErrorOutput: Boolean,
      inputLines: Sequence<String>
    ): CommandResult = error("CommandExecutor should not be called in these tests.")
  }

  @Test
  fun testSign_withNonExistentAabPath_throwsIllegalStateWithAabPath() {
    val signer = CloudKmsSigner(
      workingDir = tempFolder.root,
      kmsKeyResourceName = kmsKeyResourceName,
      gcpAccessToken = "fake-token",
      commandExecutor = unusedCommandExecutor
    )
    val nonExistentAab = tempFolder.root.toPath().resolve("missing.aab")
    val cert = tempFolder.newFile("cert.pem").toPath()
    val output = tempFolder.root.toPath().resolve("signed.aab")

    val exception = assertThrows<IllegalStateException> {
      signer.sign(nonExistentAab, cert, output)
    }

    assertThat(exception).hasMessageThat().contains("missing.aab")
  }

  @Test
  fun testSign_withValidAab_invokesJarsignerWithGoogleCloudStoretype() {
    val capturedArgs = mutableListOf<String>()
    val signer = CloudKmsSigner(
      workingDir = tempFolder.root,
      kmsKeyResourceName = kmsKeyResourceName,
      gcpAccessToken = "fake-token",
      commandExecutor = capturingExecutorReturning(exitCode = 1, capturedArgs)
    )
    val unsignedAab = tempFolder.newFile("unsigned.aab")
    val cert = tempFolder.newFile("cert.pem").toPath()
    val output = tempFolder.root.toPath().resolve("signed.aab")

    // jarsigner "fails" (exitCode = 1) so the test throws before cert verification is reached.
    val exception = assertThrows<IllegalStateException> {
      signer.sign(unsignedAab.toPath(), cert, output)
    }

    assertThat(exception).hasMessageThat().contains("exit code 1")
    assertThat(capturedArgs).contains("-storetype")
    assertThat(capturedArgs).contains("GOOGLECLOUD")
  }

  @Test
  fun testSign_withValidAab_invokesJarsignerWithJsignProviderClass() {
    val capturedArgs = mutableListOf<String>()
    val signer = CloudKmsSigner(
      workingDir = tempFolder.root,
      kmsKeyResourceName = kmsKeyResourceName,
      gcpAccessToken = "fake-token",
      commandExecutor = capturingExecutorReturning(exitCode = 1, capturedArgs)
    )
    val unsignedAab = tempFolder.newFile("unsigned.aab")
    val cert = tempFolder.newFile("cert.pem").toPath()
    val output = tempFolder.root.toPath().resolve("signed.aab")

    val exception = assertThrows<IllegalStateException> {
      signer.sign(unsignedAab.toPath(), cert, output)
    }

    assertThat(exception).hasMessageThat().contains("exit code 1")
    assertThat(capturedArgs).contains("-providerClass")
    assertThat(capturedArgs).contains("net.jsign.jca.JsignJcaProvider")
  }

  @Test
  fun testSign_withValidAab_invokesJarsignerWithKeyRingPathAsProviderArg() {
    val capturedArgs = mutableListOf<String>()
    val signer = CloudKmsSigner(
      workingDir = tempFolder.root,
      kmsKeyResourceName = kmsKeyResourceName,
      gcpAccessToken = "fake-token",
      commandExecutor = capturingExecutorReturning(exitCode = 1, capturedArgs)
    )
    val unsignedAab = tempFolder.newFile("unsigned.aab")
    val cert = tempFolder.newFile("cert.pem").toPath()
    val output = tempFolder.root.toPath().resolve("signed.aab")

    val exception = assertThrows<IllegalStateException> {
      signer.sign(unsignedAab.toPath(), cert, output)
    }

    assertThat(exception).hasMessageThat().contains("exit code 1")
    assertThat(capturedArgs).contains("-providerArg")
    assertThat(capturedArgs).contains(expectedKeyRingPath)
  }

  @Test
  fun testSign_withValidAab_invokesJarsignerWithFullKmsResourceNameAsAlias() {
    val capturedArgs = mutableListOf<String>()
    val signer = CloudKmsSigner(
      workingDir = tempFolder.root,
      kmsKeyResourceName = kmsKeyResourceName,
      gcpAccessToken = "fake-token",
      commandExecutor = capturingExecutorReturning(exitCode = 1, capturedArgs)
    )
    val unsignedAab = tempFolder.newFile("unsigned.aab")
    val cert = tempFolder.newFile("cert.pem").toPath()
    val output = tempFolder.root.toPath().resolve("signed.aab")

    val exception = assertThrows<IllegalStateException> {
      signer.sign(unsignedAab.toPath(), cert, output)
    }

    assertThat(exception).hasMessageThat().contains("exit code 1")
    // The last argument to jarsigner must be the full KMS resource name (the key alias), not an
    // arbitrary name like "oppia-signer".
    assertThat(capturedArgs.last()).isEqualTo(kmsKeyResourceName)
  }

  @Test
  fun testSign_withValidAab_invokesJarsignerWithAccessTokenAsStorePass() {
    val capturedArgs = mutableListOf<String>()
    val signer = CloudKmsSigner(
      workingDir = tempFolder.root,
      kmsKeyResourceName = kmsKeyResourceName,
      gcpAccessToken = "my-gcp-token",
      commandExecutor = capturingExecutorReturning(exitCode = 1, capturedArgs)
    )
    val unsignedAab = tempFolder.newFile("unsigned.aab")
    val cert = tempFolder.newFile("cert.pem").toPath()
    val output = tempFolder.root.toPath().resolve("signed.aab")

    val exception = assertThrows<IllegalStateException> {
      signer.sign(unsignedAab.toPath(), cert, output)
    }

    assertThat(exception).hasMessageThat().contains("exit code 1")
    assertThat(capturedArgs).contains("-storepass")
    assertThat(capturedArgs).contains("my-gcp-token")
  }

  @Test
  fun testSign_withJarsignerFailure_throwsWithExitCode() {
    val signer = CloudKmsSigner(
      workingDir = tempFolder.root,
      kmsKeyResourceName = kmsKeyResourceName,
      gcpAccessToken = "fake-token",
      commandExecutor = capturingExecutorReturning(exitCode = 2, mutableListOf())
    )
    val unsignedAab = tempFolder.newFile("unsigned.aab")
    val cert = tempFolder.newFile("cert.pem").toPath()
    val output = tempFolder.root.toPath().resolve("signed.aab")

    val exception = assertThrows<IllegalStateException> {
      signer.sign(unsignedAab.toPath(), cert, output)
    }

    assertThat(exception).hasMessageThat().contains("exit code 2")
  }

  @Test
  fun testSign_withJarsignerFailure_errorMessageContainsJarsignerStderr() {
    val signer = CloudKmsSigner(
      workingDir = tempFolder.root,
      kmsKeyResourceName = kmsKeyResourceName,
      gcpAccessToken = "fake-token",
      commandExecutor = capturingExecutorReturning(exitCode = 1, mutableListOf())
    )
    val unsignedAab = tempFolder.newFile("unsigned.aab")
    val cert = tempFolder.newFile("cert.pem").toPath()
    val output = tempFolder.root.toPath().resolve("signed.aab")

    val exception = assertThrows<IllegalStateException> {
      signer.sign(unsignedAab.toPath(), cert, output)
    }

    assertThat(exception).hasMessageThat().contains("simulated jarsigner failure")
  }

  @Test
  fun testSign_withValidAab_copiesAabToOutputPathBeforeJarsignerCheck() {
    val signer = CloudKmsSigner(
      workingDir = tempFolder.root,
      kmsKeyResourceName = kmsKeyResourceName,
      gcpAccessToken = "fake-token",
      commandExecutor = capturingExecutorReturning(exitCode = 1, mutableListOf())
    )
    val unsignedAab =
      tempFolder.newFile("unsigned.aab").also { it.writeText("fake-aab-content") }
    val cert = tempFolder.newFile("cert.pem").toPath()
    val output = tempFolder.root.toPath().resolve("signed.aab")

    // jarsigner fails, but the AAB must have been copied to the output path before the check.
    assertThrows<IllegalStateException> {
      signer.sign(unsignedAab.toPath(), cert, output)
    }

    assertThat(output.toFile().exists()).isTrue()
    assertThat(output.toFile().readText()).isEqualTo("fake-aab-content")
  }

  @Test
  fun testSign_withValidAab_invokesCommandNamedJarsigner() {
    val capturedArgs = mutableListOf<String>()
    val signer = CloudKmsSigner(
      workingDir = tempFolder.root,
      kmsKeyResourceName = kmsKeyResourceName,
      gcpAccessToken = "fake-token",
      commandExecutor = capturingExecutorReturning(exitCode = 1, capturedArgs)
    )
    val unsignedAab = tempFolder.newFile("unsigned.aab")
    val cert = tempFolder.newFile("cert.pem").toPath()
    val output = tempFolder.root.toPath().resolve("signed.aab")

    assertThrows<IllegalStateException> {
      signer.sign(unsignedAab.toPath(), cert, output)
    }

    assertThat(capturedArgs.first()).isEqualTo("jarsigner")
  }

  @Test
  fun testSign_withValidAab_invokesJarsignerWithNoneKeystore() {
    val capturedArgs = mutableListOf<String>()
    val signer = CloudKmsSigner(
      workingDir = tempFolder.root,
      kmsKeyResourceName = kmsKeyResourceName,
      gcpAccessToken = "fake-token",
      commandExecutor = capturingExecutorReturning(exitCode = 1, capturedArgs)
    )
    val unsignedAab = tempFolder.newFile("unsigned.aab")
    val cert = tempFolder.newFile("cert.pem").toPath()
    val output = tempFolder.root.toPath().resolve("signed.aab")

    assertThrows<IllegalStateException> {
      signer.sign(unsignedAab.toPath(), cert, output)
    }

    assertThat(capturedArgs).contains("-keystore")
    assertThat(capturedArgs).contains("NONE")
  }

  @Test
  fun testSign_withValidAab_invokesJarsignerWithSha256SignatureAlgorithm() {
    val capturedArgs = mutableListOf<String>()
    val signer = CloudKmsSigner(
      workingDir = tempFolder.root,
      kmsKeyResourceName = kmsKeyResourceName,
      gcpAccessToken = "fake-token",
      commandExecutor = capturingExecutorReturning(exitCode = 1, capturedArgs)
    )
    val unsignedAab = tempFolder.newFile("unsigned.aab")
    val cert = tempFolder.newFile("cert.pem").toPath()
    val output = tempFolder.root.toPath().resolve("signed.aab")

    assertThrows<IllegalStateException> {
      signer.sign(unsignedAab.toPath(), cert, output)
    }

    assertThat(capturedArgs).contains("-sigalg")
    assertThat(capturedArgs).contains("SHA256withRSA")
  }

  @Test
  fun testSign_withValidAab_invokesJarsignerWithSha256DigestAlgorithm() {
    val capturedArgs = mutableListOf<String>()
    val signer = CloudKmsSigner(
      workingDir = tempFolder.root,
      kmsKeyResourceName = kmsKeyResourceName,
      gcpAccessToken = "fake-token",
      commandExecutor = capturingExecutorReturning(exitCode = 1, capturedArgs)
    )
    val unsignedAab = tempFolder.newFile("unsigned.aab")
    val cert = tempFolder.newFile("cert.pem").toPath()
    val output = tempFolder.root.toPath().resolve("signed.aab")

    assertThrows<IllegalStateException> {
      signer.sign(unsignedAab.toPath(), cert, output)
    }

    assertThat(capturedArgs).contains("-digestalg")
    assertThat(capturedArgs).contains("SHA-256")
  }

  @Test
  fun testSign_withValidAab_invokesJarsignerWithCertChainPath() {
    val capturedArgs = mutableListOf<String>()
    val signer = CloudKmsSigner(
      workingDir = tempFolder.root,
      kmsKeyResourceName = kmsKeyResourceName,
      gcpAccessToken = "fake-token",
      commandExecutor = capturingExecutorReturning(exitCode = 1, capturedArgs)
    )
    val unsignedAab = tempFolder.newFile("unsigned.aab")
    val cert = tempFolder.newFile("cert.pem").toPath()
    val output = tempFolder.root.toPath().resolve("signed.aab")

    assertThrows<IllegalStateException> {
      signer.sign(unsignedAab.toPath(), cert, output)
    }

    assertThat(capturedArgs).contains("-certchain")
    assertThat(capturedArgs).contains(cert.toAbsolutePath().toString())
  }

  @Test
  fun testSign_withValidAab_invokesJarsignerWithSignedJarOutputPath() {
    val capturedArgs = mutableListOf<String>()
    val signer = CloudKmsSigner(
      workingDir = tempFolder.root,
      kmsKeyResourceName = kmsKeyResourceName,
      gcpAccessToken = "fake-token",
      commandExecutor = capturingExecutorReturning(exitCode = 1, capturedArgs)
    )
    val unsignedAab = tempFolder.newFile("unsigned.aab")
    val cert = tempFolder.newFile("cert.pem").toPath()
    val output = tempFolder.root.toPath().resolve("signed.aab")

    assertThrows<IllegalStateException> {
      signer.sign(unsignedAab.toPath(), cert, output)
    }

    assertThat(capturedArgs).contains("-signedjar")
    assertThat(capturedArgs).contains(output.toAbsolutePath().toString())
  }

  @Test
  fun testSign_withJarsignerSuccessAndEmptyCert_throwsAfterExitCodeCheck() {
    // When jarsigner exits with 0, CloudKmsSigner proceeds to load the certificate.
    // An empty cert file causes the CertificateFactory to fail, which confirms that
    // the code path past the exitCode check (lines 85+) is reached.
    val signer = CloudKmsSigner(
      workingDir = tempFolder.root,
      kmsKeyResourceName = kmsKeyResourceName,
      gcpAccessToken = "fake-token",
      commandExecutor = capturingExecutorReturning(exitCode = 0, mutableListOf())
    )
    val unsignedAab = tempFolder.newFile("unsigned.aab")
    val cert = tempFolder.newFile("cert.pem").toPath() // empty — not a valid X.509 cert
    val output = tempFolder.root.toPath().resolve("signed.aab")

    // The exception is thrown during cert loading, not during jarsigner exit-code check.
    val exception = assertThrows<Exception> {
      signer.sign(unsignedAab.toPath(), cert, output)
    }

    assertThat(exception).hasMessageThat().doesNotContain("exit code")
  }

  @Test
  fun testSign_withOutputInNonExistentSubdirectory_throwsWithDirectoryPath() {
    val signer = CloudKmsSigner(
      workingDir = tempFolder.root,
      kmsKeyResourceName = kmsKeyResourceName,
      gcpAccessToken = "fake-token",
      commandExecutor = unusedCommandExecutor
    )
    val unsignedAab = tempFolder.newFile("unsigned.aab")
    val cert = tempFolder.newFile("cert.pem").toPath()
    // "nonexistent-dir/signed.aab" — parent is tempFolder.root/nonexistent-dir which does not exist.
    val output = tempFolder.root.toPath().resolve("nonexistent-dir").resolve("signed.aab")

    val exception = assertThrows<IllegalStateException> {
      signer.sign(unsignedAab.toPath(), cert, output)
    }

    assertThat(exception).hasMessageThat().contains("nonexistent-dir")
  }

  @Test
  fun testSign_withJarsignerSuccessAndSignedJarWithNoRsaEntries_throwsCertMismatch() {
    // After jarsigner exits 0 the code tries to verify the cert embedded in the signed AAB.
    // A JAR with no META-INF/*.RSA entries means no signer certs → CertificateMismatchException.
    val unsignedAab = tempFolder.newFile("unsigned.aab")
    val certFile = tempFolder.newFile("cert.pem").also { it.writeText(TEST_CERT_PEM) }
    val output = tempFolder.root.toPath().resolve("signed.aab")
    // The "unsigned" AAB is a valid JAR so JarFile can open it after the copy-to-output step.
    createMinimalJar(unsignedAab)

    val signer = CloudKmsSigner(
      workingDir = tempFolder.root,
      kmsKeyResourceName = kmsKeyResourceName,
      gcpAccessToken = "fake-token",
      commandExecutor = capturingExecutorReturning(exitCode = 0, mutableListOf())
    )

    val exception = assertThrows<CertificateMismatchException> {
      signer.sign(unsignedAab.toPath(), certFile.toPath(), output)
    }

    assertThat(exception).hasMessageThat().contains("META-INF")
  }

  @Test
  fun testSign_withJarsignerSuccessAndJarWithMatchingCert_completesNormally() {
    // When the JAR's META-INF/CERT.RSA DER bytes match the expected cert, sign() should return
    // normally without throwing CertificateMismatchException.
    val certFile = tempFolder.newFile("cert.pem").also { it.writeText(TEST_CERT_PEM) }
    val certDer = FileInputStream(certFile).use { stream ->
      (CertificateFactory.getInstance("X.509").generateCertificate(stream) as X509Certificate)
        .encoded
    }
    val unsignedAab = tempFolder.newFile("unsigned.aab")
    createMinimalJar(unsignedAab, certDer)
    val output = tempFolder.root.toPath().resolve("signed.aab")

    val signer = CloudKmsSigner(
      workingDir = tempFolder.root,
      kmsKeyResourceName = kmsKeyResourceName,
      gcpAccessToken = "fake-token",
      commandExecutor = capturingExecutorReturning(exitCode = 0, mutableListOf())
    )

    // Should complete without any exception — cert in the JAR matches expected cert.
    signer.sign(unsignedAab.toPath(), certFile.toPath(), output)
  }

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------

  /**
   * Creates a minimal valid JAR at [target]. If [rsaDerBytes] are provided they are written
   * into a `META-INF/CERT.RSA` entry so that [verifyCertificateMatch] can find them.
   */
  private fun createMinimalJar(target: File, rsaDerBytes: ByteArray? = null) {
    JarOutputStream(target.outputStream()).use { jar ->
      jar.putNextEntry(JarEntry("META-INF/MANIFEST.MF"))
      jar.write("Manifest-Version: 1.0\n\n".toByteArray())
      jar.closeEntry()
      if (rsaDerBytes != null) {
        jar.putNextEntry(JarEntry("META-INF/CERT.RSA"))
        jar.write(rsaDerBytes)
        jar.closeEntry()
      }
    }
  }

  /** Returns a [CommandExecutor] that records all arguments and returns [exitCode]. */
  private fun capturingExecutorReturning(
    exitCode: Int,
    capturedArgs: MutableList<String>
  ) = object : CommandExecutor {
    override fun executeCommand(
      workingDir: File,
      command: String,
      vararg arguments: String,
      includeErrorOutput: Boolean,
      inputLines: Sequence<String>
    ): CommandResult {
      capturedArgs.add(command)
      capturedArgs.addAll(arguments.toList())
      return CommandResult(
        exitCode = exitCode,
        output = emptyList(),
        errorOutput = if (exitCode != 0) listOf("simulated jarsigner failure") else emptyList(),
        command = listOf(command) + arguments
      )
    }
  }

  private companion object {
    /**
     * A self-signed RSA-1024 certificate generated solely for test coverage. Not used for any
     * real signing — only to exercise [CloudKmsSigner.verifyCertificateMatch].
     */
    private val TEST_CERT_PEM = """
      -----BEGIN CERTIFICATE-----
      MIIBrzCCARgCCQCwCsiWNlS1pjANBgkqhkiG9w0BAQsFADAcMRowGAYDVQQDDBFP
      cHBpYUNvdmVyYWdlVGVzdDAeFw0yNjA3MDEwNzMyMzFaFw0zNjA2MjgwNzMyMzFa
      MBwxGjAYBgNVBAMMEU9wcGlhQ292ZXJhZ2VUZXN0MIGfMA0GCSqGSIb3DQEBAQUA
      A4GNADCBiQKBgQDDSAJDKOtO+jxNh/OqPdXR3ZoBd7vaURWybWPjbGbedC8ZOvXO
      FCRqBYnJGA/opCYUvpGXp5HLsTrAr24AuD0hE3NGwlXHGlRHkyw7c+HoCwGtPxj6
      rQPVFqXtViHzUTOLDmXLpGGtJWAdVulHjvM0Z4/MQK1HiTTtH4XAFWzzuQIDAQAB
      MA0GCSqGSIb3DQEBCwUAA4GBAFjnaHIeKwE0hdnYXkMZ6XLyiswXz3YzdRKNxxkd
      4KWVsHH7VoHROQthWzomP+vTFGS/v8zJVze7sd32hpNpoM7bOL8eJOsnjvO/V0yx
      Xls9D5O3WF+U63uJGkg8CofYcvP2EIP+0WA1ewjKV79Dig+iUwiaLmBS51QPlBFa
      zGZr
      -----END CERTIFICATE-----
    """.trimIndent()
  }

}
