package org.oppia.android.scripts.release

import java.io.FileNotFoundException
import java.nio.file.Path

/**
 * Test fake implementation of [CloudSigner] that copies [unsignedAabPath] to [outputPath] without
 * making any KMS calls or requiring GCP credentials.
 *
 * Used in [SignReleaseBinaryTest] to verify argument parsing, precondition checks, and error
 * handling without a real GCP project.
 *
 * @param shouldThrowAuthError if `true`, [sign] throws [CloudKmsAuthenticationException] to
 *     simulate a KMS authentication failure
 * @param shouldThrowKeyUnavailable if `true`, [sign] throws [KeyVersionUnavailableException] to
 *     simulate a disabled or destroyed key version
 * @param shouldThrowCertMismatch if `true`, [sign] throws [CertificateMismatchException] to
 *     simulate a certificate mismatch after signing
 */
class FakeCloudSigner(
  private val shouldThrowAuthError: Boolean = false,
  private val shouldThrowKeyUnavailable: Boolean = false,
  private val shouldThrowCertMismatch: Boolean = false
) : CloudSigner {

  /** The [unsignedAabPath] values passed to each [sign] call, in order. */
  val signedPaths = mutableListOf<Path>()

  override fun sign(unsignedAabPath: Path, certPath: Path, outputPath: Path) {
    if (!unsignedAabPath.toFile().exists()) {
      throw FileNotFoundException(
        "Unsigned AAB not found at: ${unsignedAabPath.toAbsolutePath()}"
      )
    }

    when {
      shouldThrowAuthError -> throw CloudKmsAuthenticationException(
        "Fake: KMS authentication failed for test."
      )
      shouldThrowKeyUnavailable -> throw KeyVersionUnavailableException(
        "Fake: KMS key version is disabled."
      )
    }

    // Simulate signing by copying the unsigned file to the output path.
    unsignedAabPath.toFile().copyTo(outputPath.toFile(), overwrite = true)
    signedPaths.add(unsignedAabPath)

    if (shouldThrowCertMismatch) {
      throw CertificateMismatchException(
        "Fake: Certificate in META-INF/ does not match the supplied cert."
      )
    }
  }
}
