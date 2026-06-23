package org.oppia.android.scripts.release

import java.nio.file.Path

/**
 * Abstracts AAB signing so that tests can avoid real KMS calls.
 *
 * The production implementation [CloudKmsSigner] invokes `jarsigner` configured with the Cloud
 * KMS JCA provider, sending only the AAB digest to KMS and receiving the signature back. The key
 * never leaves the HSM boundary.
 *
 * Test code uses [FakeCloudSigner], which writes a predictable stub AAB to [outputPath] without
 * making any network calls or requiring GCP credentials.
 */
interface CloudSigner {
  /**
   * Signs the unsigned AAB at [unsignedAabPath] using the certificate at [certPath] and writes the
   * signed output to [outputPath].
   *
   * @param unsignedAabPath path to the unsigned AAB produced by `bazel build`
   * @param certPath path to the public X.509 certificate PEM file (checked into
   *     `config/certificate/`)
   * @param outputPath destination path for the signed AAB
   * @throws CloudKmsAuthenticationException if KMS is unreachable or returns an auth error
   * @throws KeyVersionUnavailableException if the KMS key version is disabled or destroyed
   * @throws CertificateMismatchException if the cert in `META-INF/` does not match [certPath]
   * @throws java.io.FileNotFoundException if [unsignedAabPath] does not exist
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
