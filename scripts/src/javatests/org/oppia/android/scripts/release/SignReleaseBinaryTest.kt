package org.oppia.android.scripts.release

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.testing.assertThrows
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream

/**
 * Tests for [SignReleaseBinary].
 *
 * All tests that exercise error paths in [signAndValidate] expect a [SecurityException] because
 * Bazel's test runner installs a SecurityManager that converts [System.exit] calls into a
 * SecurityException (this is the same pattern used across other script tests in this codebase).
 */
class SignReleaseBinaryTest {
  @field:[Rule JvmField] val tempFolder = TemporaryFolder()

  private lateinit var outContent: ByteArrayOutputStream
  private lateinit var errContent: ByteArrayOutputStream
  private lateinit var originalOut: PrintStream
  private lateinit var originalErr: PrintStream

  @Before
  fun setUp() {
    outContent = ByteArrayOutputStream()
    errContent = ByteArrayOutputStream()
    originalOut = System.out
    originalErr = System.err
    System.setOut(PrintStream(outContent))
    System.setErr(PrintStream(errContent))
  }

  @After
  fun tearDown() {
    System.setOut(originalOut)
    System.setErr(originalErr)
  }

  @Test
  fun testMain_noArguments_throwsIllegalArgumentWithUsage() {
    val exception = assertThrows<IllegalArgumentException> { main() }

    assertThat(exception).hasMessageThat().contains("Usage:")
    assertThat(exception).hasMessageThat().contains("sign_release_binary")
  }

  @Test
  fun testMain_oneArgument_throwsIllegalArgumentWithUsage() {
    val exception = assertThrows<IllegalArgumentException> { main("only-one") }

    assertThat(exception).hasMessageThat().contains("Usage:")
  }

  @Test
  fun testMain_threeArguments_throwsIllegalArgumentWithUsage() {
    val exception = assertThrows<IllegalArgumentException> { main("a", "b", "c") }

    assertThat(exception).hasMessageThat().contains("Usage:")
  }

  @Test
  fun testMain_fiveArguments_throwsIllegalArgumentWithUsage() {
    val exception =
      assertThrows<IllegalArgumentException> { main("a", "b", "c", "d", "e") }

    assertThat(exception).hasMessageThat().contains("Usage:")
  }

  @Test
  fun testMain_blankUnsignedAabPath_throwsIllegalArgument() {
    val certFile = tempFolder.newFile("cert.pem")

    val exception = assertThrows<IllegalArgumentException> {
      main("   ", VALID_KMS_KEY, certFile.absolutePath, "output.aab")
    }

    assertThat(exception).hasMessageThat().contains("unsigned_aab_path must not be blank")
  }

  @Test
  fun testMain_blankKmsKeyResourceName_throwsIllegalArgument() {
    val certFile = tempFolder.newFile("cert.pem")

    val exception = assertThrows<IllegalArgumentException> {
      main("input.aab", "  ", certFile.absolutePath, "output.aab")
    }

    assertThat(exception).hasMessageThat().contains("kms_key_resource_name must not be blank")
  }

  @Test
  fun testMain_blankCertPemPath_throwsIllegalArgument() {
    val exception = assertThrows<IllegalArgumentException> {
      main("input.aab", VALID_KMS_KEY, "  ", "output.aab")
    }

    assertThat(exception).hasMessageThat().contains("cert_pem_path must not be blank")
  }

  @Test
  fun testMain_blankOutputAabPath_throwsIllegalArgument() {
    val certFile = tempFolder.newFile("cert.pem")

    val exception = assertThrows<IllegalArgumentException> {
      main("input.aab", VALID_KMS_KEY, certFile.absolutePath, "  ")
    }

    assertThat(exception).hasMessageThat().contains("output_aab_path must not be blank")
  }

  @Test
  fun testMain_certFileDoesNotExist_throwsIllegalArgument() {
    val exception = assertThrows<IllegalArgumentException> {
      main("input.aab", VALID_KMS_KEY, "/nonexistent/cert.pem", "output.aab")
    }

    assertThat(exception).hasMessageThat().contains("Certificate PEM file not found")
  }

  @Test
  fun testMain_malformedKmsResourceName_throwsIllegalArgument() {
    val certFile = tempFolder.newFile("cert.pem")

    val exception = assertThrows<IllegalArgumentException> {
      main("input.aab", "not-a-valid-kms-resource-name", certFile.absolutePath, "output.aab")
    }

    assertThat(exception).hasMessageThat().contains("does not match the expected Cloud KMS")
  }

  @Test
  fun testMain_validKmsResourceNameWithNumericVersion_doesNotThrowOnFormat() {
    // Verify the regex accepts version numbers > 1 (regression check).
    val certFile = tempFolder.newFile("cert.pem")
    val kmsKeyV3 =
      "projects/my-proj/locations/global/keyRings/ring/cryptoKeys/key/cryptoKeyVersions/3"

    // KMS format validation passes → execution proceeds until jarsigner fails on the missing AAB,
    // which triggers System.exit(1) inside signAndValidate → SecurityException.
    val exception = assertThrows<SecurityException> {
      main("nonexistent.aab", kmsKeyV3, certFile.absolutePath, "output.aab")
    }

    // Error is about the missing AAB, NOT about the KMS key format.
    assertThat(exception).hasMessageThat().contains("System.exit()")
    assertThat(errContent.toString()).doesNotContain("does not match the expected Cloud KMS")
    assertThat(errContent.toString()).contains("Unsigned AAB not found")
  }

  @Test
  fun testSignAndValidate_validInputs_outputFileIsCreated() {
    val unsignedAab =
      tempFolder.newFile("unsigned.aab").also { it.writeText("fake-aab-content") }
    val certPem = tempFolder.newFile("cert.pem")
    val outputAab = File(tempFolder.root, "signed.aab")

    signAndValidate(FakeCloudSigner(), unsignedAab.toPath(), certPem.toPath(), outputAab.toPath())

    assertThat(outputAab.exists()).isTrue()
  }

  @Test
  fun testSignAndValidate_validInputs_stdoutContainsSigningComplete() {
    val unsignedAab =
      tempFolder.newFile("unsigned.aab").also { it.writeText("fake-aab-content") }
    val certPem = tempFolder.newFile("cert.pem")
    val outputAab = File(tempFolder.root, "signed.aab")

    signAndValidate(FakeCloudSigner(), unsignedAab.toPath(), certPem.toPath(), outputAab.toPath())

    assertThat(outContent.toString()).contains("Signing complete")
  }

  @Test
  fun testSignAndValidate_validInputs_signerReceivesUnsignedAabPath() {
    val unsignedAab =
      tempFolder.newFile("unsigned.aab").also { it.writeText("fake-aab-content") }
    val certPem = tempFolder.newFile("cert.pem")
    val outputAab = File(tempFolder.root, "signed.aab")
    val fakeSigner = FakeCloudSigner()

    signAndValidate(fakeSigner, unsignedAab.toPath(), certPem.toPath(), outputAab.toPath())

    assertThat(fakeSigner.signedPaths).hasSize(1)
    assertThat(fakeSigner.signedPaths.first()).isEqualTo(unsignedAab.toPath())
  }

  @Test
  fun testSignAndValidate_missingUnsignedAab_exitsAndLogsError() {
    val nonExistentAab = File(tempFolder.root, "missing.aab")
    val certPem = tempFolder.newFile("cert.pem")
    val outputAab = File(tempFolder.root, "signed.aab")

    val exception = assertThrows<SecurityException> {
      signAndValidate(
        FakeCloudSigner(),
        nonExistentAab.toPath(),
        certPem.toPath(),
        outputAab.toPath()
      )
    }

    assertThat(exception).hasMessageThat().contains("System.exit()")
    assertThat(errContent.toString()).contains("Unsigned AAB not found")
  }

  @Test
  fun testSignAndValidate_kmsAuthenticationFails_exitsAndLogsError() {
    val unsignedAab =
      tempFolder.newFile("unsigned.aab").also { it.writeText("fake-aab-content") }
    val certPem = tempFolder.newFile("cert.pem")
    val outputAab = File(tempFolder.root, "signed.aab")

    val exception = assertThrows<SecurityException> {
      signAndValidate(
        FakeCloudSigner(shouldThrowAuthError = true),
        unsignedAab.toPath(),
        certPem.toPath(),
        outputAab.toPath()
      )
    }

    assertThat(exception).hasMessageThat().contains("System.exit()")
    assertThat(errContent.toString()).contains("authentication failed")
  }

  @Test
  fun testSignAndValidate_keyVersionUnavailable_exitsAndLogsError() {
    val unsignedAab =
      tempFolder.newFile("unsigned.aab").also { it.writeText("fake-aab-content") }
    val certPem = tempFolder.newFile("cert.pem")
    val outputAab = File(tempFolder.root, "signed.aab")

    val exception = assertThrows<SecurityException> {
      signAndValidate(
        FakeCloudSigner(shouldThrowKeyUnavailable = true),
        unsignedAab.toPath(),
        certPem.toPath(),
        outputAab.toPath()
      )
    }

    assertThat(exception).hasMessageThat().contains("System.exit()")
    assertThat(errContent.toString()).contains("unavailable")
  }

  @Test
  fun testSignAndValidate_certificateMismatchAfterSigning_exitsAndLogsError() {
    val unsignedAab =
      tempFolder.newFile("unsigned.aab").also { it.writeText("fake-aab-content") }
    val certPem = tempFolder.newFile("cert.pem")
    val outputAab = File(tempFolder.root, "signed.aab")

    val exception = assertThrows<SecurityException> {
      signAndValidate(
        FakeCloudSigner(shouldThrowCertMismatch = true),
        unsignedAab.toPath(),
        certPem.toPath(),
        outputAab.toPath()
      )
    }

    assertThat(exception).hasMessageThat().contains("System.exit()")
    assertThat(errContent.toString()).contains("mismatch")
  }

  private companion object {
    /** A syntactically valid Cloud KMS key version resource name for use in tests. */
    private const val VALID_KMS_KEY =
      "projects/my-project/locations/global/keyRings/release-ring/" +
        "cryptoKeys/release-key/cryptoKeyVersions/1"
  }
}
