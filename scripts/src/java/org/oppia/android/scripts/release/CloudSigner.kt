package org.oppia.android.scripts.release

import java.nio.file.Path

/**
 * Abstracts AAB signing so that production and test code share the same call site.
 *
 * @see CloudKmsSigner for the production implementation using Cloud KMS HSM via jarsigner.
 * @see FakeCloudSigner for the test double used in unit tests.
 */
interface CloudSigner {
  /**
   * Signs the unsigned AAB at [unsignedAabPath] using the certificate at [certPath] and writes
   * the signed output to [outputPath].
   */
  fun sign(unsignedAabPath: Path, certPath: Path, outputPath: Path)
}

/** Thrown when Cloud KMS is unreachable or returns an authentication error. */
class CloudKmsAuthenticationException(
  message: String,
  cause: Throwable? = null
) : Exception(message, cause)

/** Thrown when the requested KMS key version is disabled or destroyed. */
class KeyVersionUnavailableException(
  message: String,
  cause: Throwable? = null
) : Exception(message, cause)

/**
 * Thrown when the signing certificate found in the signed AAB's `META-INF/` directory does not
 * match the certificate supplied via [CloudSigner.sign].
 */
class CertificateMismatchException(
  message: String,
  cause: Throwable? = null
) : Exception(message, cause)
