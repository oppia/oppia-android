package org.oppia.android.scripts.release

import org.oppia.android.scripts.common.CommandExecutor
import org.oppia.android.scripts.common.CommandResult
import java.io.File
import java.io.FileInputStream
import java.nio.file.Path
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
 * **Provider wiring:** `jarsigner` runs as a separate JVM process and does not inherit the current
 * process's security providers. To give the subprocess access to the Cloud KMS keystore type, this
 * class resolves the jsign JAR from the current classpath and passes it to jarsigner via
 * `-providerPath`, together with `-providerClass net.jsign.jca.JsignJcaProvider` and
 * `-providerArg <keyRingPath>`.
 *
 * @param workingDir base directory used to resolve a relative [outputPath] passed to [sign] into a
 *     fully absolute path via `File(workingDir, outputPath).absoluteFile.normalize()`. When the
 *     caller always provides an absolute [outputPath] (as the release workflow does), this value
 *     has no effect on path resolution; it serves as a fallback root so the class never relies on
 *     the process's implicit CWD (which can be Bazel's ephemeral execroot under `bazel run`).
 * @param kmsKeyResourceName full Cloud KMS key resource name, e.g.
 *     `projects/<id>/locations/global/keyRings/<ring>/cryptoKeys/<key>/cryptoKeyVersions/<ver>`
 * @param gcpAccessToken GCP OAuth2 access token obtained via Workload Identity Federation
 * @param commandExecutor used to invoke `jarsigner` as a subprocess
 */
class CloudKmsSigner(
  private val workingDir: File,
  private val kmsKeyResourceName: String,
  private val gcpAccessToken: String,
  private val commandExecutor: CommandExecutor
) : CloudSigner {
  override fun sign(unsignedAabPath: Path, certPath: Path, outputPath: Path) {
    check(unsignedAabPath.toFile().exists()) {
      "Unsigned AAB not found at: ${unsignedAabPath.toAbsolutePath()}"
    }

    // Resolve the output path against the provided working directory so the resulting path is
    // always fully absolute and normalised. Path.resolve(Path) is used (rather than
    // File(workingDir, child)) because it has an unambiguous contract: if outputPath is already
    // absolute it is returned unchanged; if relative it is resolved against workingDir. This
    // mirrors the codebase pattern: File(repoRoot, relativePathStr).absoluteFile.normalize(),
    // while being robust to cases where outputPath starts out as an absolute Path.
    val absoluteOutputFile = workingDir.toPath().resolve(outputPath).normalize().toFile()
    val parentDir = checkNotNull(absoluteOutputFile.parentFile) {
      "Output file has no parent directory: $absoluteOutputFile"
    }.also {
      check(it.isDirectory) {
        "Expected parent directory of output file to exist as a directory: $it"
      }
    }

    // Extract key ring path from the full key resource name.
    // Format: projects/<id>/locations/<loc>/keyRings/<ring>/cryptoKeys/<key>/cryptoKeyVersions/<v>
    val keyRingPath = kmsKeyResourceName
      .split("/")
      .take(6) // projects/<id>/locations/<loc>/keyRings/<ring>
      .joinToString("/")

    // Copy unsigned AAB to output path, then sign in-place with jarsigner.
    // jarsigner is used instead of ApkSigner because AABs use JAR signing (v1 scheme), and
    // ApkSigner is designed for APK v2/v3 signing schemes.
    unsignedAabPath.toFile().copyTo(absoluteOutputFile, overwrite = true)

    // Resolve jarsigner using the running JVM's java.home rather than relying on PATH.
    // bazel run modifies the subprocess's PATH and may resolve a different (incompatible)
    // jarsigner than the JDK that's actually running this script.
    val javaHome = requireNotNull(System.getProperty("java.home")) { "java.home not set" }
    val jarsignerBin = java.io.File(javaHome, "bin/jarsigner").also {
      check(it.exists()) { "jarsigner not found at: ${it.absolutePath}. java.home=$javaHome" }
    }.absolutePath
    println("  jarsigner   : $jarsignerBin")

    // Pass the full classpath of the current JVM (populated by Bazel with all transitive deps)
    // to jarsigner's JVM. java.class.path contains relative paths (e.g. "scripts/foo.jar",
    // "..maven/net/jsign/...") that are relative to Bazel's execroot. jarsigner runs in
    // parentDir (the output directory) where these paths don't resolve. Fix: convert each
    // entry to an absolute path using the Kotlin binary's CWD before passing to jarsigner.
    val absoluteClasspath = checkNotNull(System.getProperty("java.class.path")) {
      "java.class.path is null; cannot pass full classpath to jarsigner"
    }.split(File.pathSeparator)
      .filter { it.isNotBlank() }
      .joinToString(File.pathSeparator) { entry ->
        val f = File(entry)
        if (f.isAbsolute) entry else f.absoluteFile.canonicalPath
      }
    println("  classpath    : $absoluteClasspath")

    // Extract the CryptoKey name (e.g. "oppia-android-signing-key") from the full resource path.
    // Format: projects/<id>/locations/<loc>/keyRings/<ring>/cryptoKeys/<key>/cryptoKeyVersions/<ver>
    // substringAfterLast("/") would return the VERSION NUMBER (e.g. "2"), not the key name.
    val keyParts = kmsKeyResourceName.split("/")
    val cryptoKeyIndex = keyParts.indexOf("cryptoKeys")
    check(cryptoKeyIndex >= 0 && cryptoKeyIndex + 1 < keyParts.size) {
      "Could not extract cryptoKey name from kms_key_resource_name: $kmsKeyResourceName"
    }
    val alias = keyParts[cryptoKeyIndex + 1] // e.g. "oppia-android-signing-key"

    // Jsign's GOOGLECLOUD keystore looks for a file named "<alias>.pem" in the working directory
    // to use as the certificate, since GCP KMS does not store X.509 certificates. Create it here
    // so jsign can discover the certificate without fetching from the keystore.
    val jsignCertFile = File(parentDir, "$alias.pem")
    certPath.toFile().copyTo(jsignCertFile, overwrite = true)

    val jarsignerResult: CommandResult = commandExecutor.executeCommand(
      workingDir = parentDir,
      command = jarsignerBin,
      // Jsign REQUIRES the -keystore parameter to be the GCP KeyRing path
      "-keystore", "NONE",
      "-storetype", "GOOGLECLOUD",
      "-storepass", gcpAccessToken,
      // We still pass certchain for jarsigner's internal manifest checks
      "-certchain", certPath.toAbsolutePath().toString(),
      "-sigalg", "SHA256withRSA",
      "-digestalg", "SHA-256",
      "-J-cp", "-J$absoluteClasspath",
      "-J--add-modules=java.sql",
      "-providerClass", "net.jsign.jca.JsignJcaProvider",
      "-providerArg", keyRingPath,
      "-signedjar", absoluteOutputFile.absolutePath,
      absoluteOutputFile.absolutePath,
      // The exact alias matching the newly created .pem file
      alias
    )

    check(jarsignerResult.exitCode == 0) {
      "jarsigner failed with exit code ${jarsignerResult.exitCode}.\n" +
        "stdout: ${jarsignerResult.output.joinToString("\n")}\n" +
        "stderr: ${jarsignerResult.errorOutput.joinToString("\n")}"
    }

    // Load the public certificate and validate that the signature embedded in the signed AAB
    // matches the expected certificate. Performed after jarsigner so cert loading is skipped if
    // jarsigner fails.
    val certificate = FileInputStream(certPath.toFile()).use { stream ->
      certFactory.generateCertificate(stream) as X509Certificate
    }
    verifyCertificateMatch(absoluteOutputFile.toPath(), certificate)
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
