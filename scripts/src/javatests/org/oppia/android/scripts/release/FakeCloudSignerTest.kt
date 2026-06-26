package org.oppia.android.scripts.release

import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.testing.assertThrows

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
  fun testSign_withNonExistentAab_throwsIllegalStateException() {
    val signer = FakeCloudSigner()
    val aab = tempFolder.root.toPath().resolve("missing.aab")
    val cert = tempFolder.newFile("cert.pem").toPath()
    val output = tempFolder.root.toPath().resolve("signed.aab")

    assertThrows<IllegalStateException> {
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
  fun testSign_multipleAabs_recordsSignedPathsInOrder() {
    val signer = FakeCloudSigner()
    val aab1 = tempFolder.newFile("first.aab").also { it.writeText("content1") }
    val aab2 = tempFolder.newFile("second.aab").also { it.writeText("content2") }
    val cert = tempFolder.newFile("cert.pem")
    val out1 = tempFolder.root.toPath().resolve("signed1.aab")
    val out2 = tempFolder.root.toPath().resolve("signed2.aab")

    signer.sign(aab1.toPath(), cert.toPath(), out1)
    signer.sign(aab2.toPath(), cert.toPath(), out2)

    assertThat(signer.signedPaths)
      .containsExactly(aab1.toPath(), aab2.toPath())
      .inOrder()
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
