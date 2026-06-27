package org.oppia.android.scripts.release

import net.jsign.jca.JsignJcaProvider
import org.oppia.android.scripts.common.CommandExecutor
import org.oppia.android.scripts.common.CommandResult
import java.io.FileInputStream
import java.nio.file.Path
import java.security.KeyStore
import java.security.Security
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.jar.JarFile

/**
 * Real implementation of [CloudSigner] that invokes `jarsigner` configured with the Cloud KMS JCA
 * provider (via the jsign library).
 *
 * The private key never leaves the KMS HSM boundary — only the AAB digest is sent to KMS and the
 * signature is received back. The GCP OAuth2 access token is passed in as [gcpAccessToken] and
 * should be obtained from `gcloud auth print-access-token`.
 *
 * @param kmsKeyResourceName full Cloud KMS key resource name, e.g.
 *     `projects/<id>/locations/global/keyRings/<ring>/cryptoKeys/<key>/cryptoKeyVersions/<ver>`
 * @param gcpAccessToken GCP OAuth2 access token obtained via Workload Identity Federation
 * @param commandExecutor used to invoke `jarsigner` as a subprocess
 */
class CloudKmsSigner(
  private val kmsKeyResourceName: String,
  private val gcpAccessToken: String,
  private val commandExecutor: CommandExecutor
) : CloudSigner {
  override fun sign(unsignedAabPath: Path, certPath: Path, outputPath: Path) {
    check(unsignedAabPath.toFile().exists()) {
      "Unsigned AAB not found at: ${unsignedAabPath.toAbsolutePath()}"
    }

    // Extract key ring path from the full key resource name.
    // Format: projects/<id>/locations/<loc>/keyRings/<ring>/cryptoKeys/<key>/cryptoKeyVersions/<v>
    val keyRingPath = kmsKeyResourceName
      .split("/")
      .take(6) // projects/<id>/locations/<loc>/keyRings/<ring>
      .joinToString("/")

    val keyAlias = kmsKeyResourceName

    // Register the Jsign JCA provider so that jarsigner can delegate signing to Cloud KMS.
    val provider = JsignJcaProvider(keyRingPath)
    Security.addProvider(provider)

    val keyStore = KeyStore.getInstance("GOOGLECLOUD", provider)
    keyStore.load(/* protectionParam= */ null, gcpAccessToken.toCharArray())

    // Validate KMS access: ensures the key exists and is reachable before invoking jarsigner.
    val key = try {
      keyStore.getKey(keyAlias, /* password= */ null)
    } catch (e: Exception) {
      throw CloudKmsAuthenticationException(
        "Failed to authenticate with Cloud KMS. " +
          "WIF identity may be misconfigured or the key resource name is incorrect: " +
          // kmsKeyResourceName is a structural path identifier, not key material — safe to log.
          kmsKeyResourceName,
        e
      )
    }
    if (key == null) {
      throw KeyVersionUnavailableException(
        "Key version not found in KMS keystore for alias: $keyAlias. " +
          "Check that the key version is enabled and not destroyed."
      )
    }

    // Load the public certificate from the repository.
    val certificate = FileInputStream(certPath.toFile()).use { stream ->
      certFactory.generateCertificate(stream) as X509Certificate
    }

    // Copy unsigned AAB to output path, then sign in-place with jarsigner.
    // jarsigner is used instead of ApkSigner because AABs use JAR signing (v1 scheme), and
    // ApkSigner is designed for APK v2/v3 signing schemes.
    unsignedAabPath.toFile().copyTo(outputPath.toFile(), overwrite = true)

    val signerName = "oppia-signer"
    val jarsignerResult: CommandResult = commandExecutor.executeCommand(
      workingDir = outputPath.toFile().parentFile,
      command = "jarsigner",
      "-keystore", "NONE",
      "-storetype", "GOOGLECLOUD",
      "-storepass", gcpAccessToken,
      "-certchain", certPath.toAbsolutePath().toString(),
      "-sigalg", "SHA256withRSA",
      "-digestalg", "SHA-256",
      "-signedjar", outputPath.toAbsolutePath().toString(),
      outputPath.toAbsolutePath().toString(),
      signerName
    )

    check(jarsignerResult.exitCode == 0) {
      "jarsigner failed with exit code ${jarsignerResult.exitCode}:\n" +
        jarsignerResult.errorOutput.joinToString("\n")
    }

    // Validate: verify the signing certificate in META-INF/ matches the expected certificate.
    verifyCertificateMatch(outputPath, certificate)
  }

  /**
   * Verifies that the certificate embedded in the signed AAB's `META-INF/` directory matches the
   * expected [expectedCert].
   *
   * @throws CertificateMismatchException if the certificates do not match
   */
  private fun verifyCertificateMatch(signedAabPath: Path, expectedCert: X509Certificate) {
    JarFile(signedAabPath.toFile(), true).use { jar ->
      val signerCerts = jar.manifest?.let { _ ->
        jar.entries().asSequence()
          .filter { it.name.startsWith("META-INF/") && it.name.endsWith(".RSA") }
          .flatMap { entry ->
            jar.getInputStream(entry).use { certFactory.generateCertificates(it) }.asSequence()
          }
          .filterIsInstance<X509Certificate>()
          .toList()
      }.orEmpty()

      val matched = signerCerts.any { it.encoded.contentEquals(expectedCert.encoded) }
      if (!matched) {
        throw CertificateMismatchException(
          "The certificate embedded in the signed AAB does not match the expected certificate " +
            "from config/certificate/. " +
            "Expected subject: ${expectedCert.subjectDN}. " +
            "Found ${signerCerts.size} certificate(s) in META-INF/."
        )
      }
    }
  }

  private companion object {
    private val certFactory by lazy { CertificateFactory.getInstance("X.509") }
  }
}
