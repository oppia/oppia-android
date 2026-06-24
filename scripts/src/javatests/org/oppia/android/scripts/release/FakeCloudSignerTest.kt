package org.oppia.android.scripts.release

import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.testing.assertThrows
import java.io.FileNotFoundException

/**
 * Tests for [FakeCloudSigner].
 *
 * Verifies that the test double correctly simulates all [CloudSigner] contract paths: successful
 * signing, file-not-found, auth failure, key unavailability, and certificate mismatch. These
 * tests make it safe to rely on [FakeCloudSigner] in other test suites (e.g. [SignReleaseBinaryTest]).
 */
class FakeCloudSignerTest {
  @field:[Rule JvmField] val tempFolder = TemporaryFolder()

  @Test
  fun testSign_withNonExistentAab_throwsFileNotFoundException() {
    val signer = FakeCloudSigner()
    val aab = tempFolder.root.toPath().resolve("missing.aab")
    val cert = tempFolder.newFile("cert.pem").toPath()
    val output = tempFolder.root.toPath().resolve("signed.aab")

    assertThrows<FileNotFoundException> {
      signer.sign(aab, cert, output)
    }
  }

  @Test
  fun testSign_copiesAabContentToOutputPath() {
    val signer = FakeCloudSigner()
    val aab = tempFolder.newFile("unsigned.aab")
    aab.writeText("fake-aab-content")
    val cert = tempFolder.newFile("cert.pem")
    val output = tempFolder.root.toPath().resolve("signed.aab")

    signer.sign(aab.toPath(), cert.toPath(), output)

    assertThat(output.toFile().readText()).isEqualTo("fake-aab-content")
  }

  @Test
  fun testSign_recordsSignedPath() {
    val signer = FakeCloudSigner()
    val aab = tempFolder.newFile("unsigned.aab")
    val cert = tempFolder.newFile("cert.pem")
    val output = tempFolder.root.toPath().resolve("signed.aab")

    signer.sign(aab.toPath(), cert.toPath(), output)

    assertThat(signer.signedPaths).containsExactly(aab.toPath())
  }

  @Test
  fun testSign_withShouldThrowAuthError_throwsCloudKmsAuthenticationException() {
    val signer = FakeCloudSigner(shouldThrowAuthError = true)
    val aab = tempFolder.newFile("unsigned.aab")
    val cert = tempFolder.newFile("cert.pem")
    val output = tempFolder.root.toPath().resolve("signed.aab")

    assertThrows<CloudKmsAuthenticationException> {
      signer.sign(aab.toPath(), cert.toPath(), output)
    }
  }

  @Test
  fun testSign_withShouldThrowKeyUnavailable_throwsKeyVersionUnavailableException() {
    val signer = FakeCloudSigner(shouldThrowKeyUnavailable = true)
    val aab = tempFolder.newFile("unsigned.aab")
    val cert = tempFolder.newFile("cert.pem")
    val output = tempFolder.root.toPath().resolve("signed.aab")

    assertThrows<KeyVersionUnavailableException> {
      signer.sign(aab.toPath(), cert.toPath(), output)
    }
  }

  @Test
  fun testSign_withCertMismatch_stillCopiesFileBeforeThrowing() {
    val signer = FakeCloudSigner(shouldThrowCertMismatch = true)
    val aab = tempFolder.newFile("unsigned.aab").toPath()
    val cert = tempFolder.newFile("cert.pem").toPath()
    val output = tempFolder.root.toPath().resolve("signed.aab")

    assertThrows<CertificateMismatchException> {
      signer.sign(aab, cert, output)
    }

    assertThat(output.toFile().exists()).isTrue()
  }
}
