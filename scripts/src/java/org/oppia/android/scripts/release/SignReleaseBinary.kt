package org.oppia.android.scripts.release

import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Script that signs an unsigned AAB using Cloud KMS via Java's built-in `jarsigner` with the
 * Cloud KMS keystore provider. The signing key never leaves the HSM.
 *
 * Parses command-line arguments, delegates signing to [CloudSigner.sign], and validates the signed
 * output by verifying the certificate in META-INF/. Fails with a non-zero exit code if the
 * [GCP_ACCESS_TOKEN_ENV] environment variable is missing, the KMS key is unavailable, the
 * certificate doesn't match, or the unsigned AAB doesn't exist.
 *
 * Usage:
 * ```
 * bazel run //scripts:sign_release_binary -- \
 *     <unsigned_aab_path> <kms_key_resource_name> <cert_pem_path> <output_aab_path>
 * ```
 *
 * Arguments:
 * - unsigned_aab_path: path to the unsigned AAB produced by bazel build.
 * - kms_key_resource_name: full KMS key resource name
 *       (e.g. projects/<id>/locations/global/keyRings/<ring>/cryptoKeys/<key>/cryptoKeyVersions/<ver>).
 * - cert_pem_path: path to the public certificate PEM file (checked into config/certificate/).
 * - output_aab_path: destination path for the signed AAB.
 */
fun main(vararg args: String) {
  require(args.size == 4) {
    "Usage: sign_release_binary <unsigned_aab_path> <kms_key_resource_name> " +
      "<cert_pem_path> <output_aab_path>\n" +
      "Got ${args.size} argument(s): ${args.toList()}"
  }

  val unsignedAabPath = Paths.get(args[0])
  val kmsKeyResourceName = args[1]
  val certPemPath = Paths.get(args[2])
  val outputAabPath = Paths.get(args[3])

  require(args[0].isNotBlank()) { "unsigned_aab_path must not be blank." }
  require(kmsKeyResourceName.isNotBlank()) { "kms_key_resource_name must not be blank." }
  require(args[2].isNotBlank()) { "cert_pem_path must not be blank." }
  require(args[3].isNotBlank()) { "output_aab_path must not be blank." }

  require(certPemPath.toFile().exists()) {
    "Certificate PEM file not found at: ${certPemPath.toAbsolutePath()}. " +
      "Expected the public certificate checked into config/certificate/."
  }

  require(KMS_RESOURCE_NAME_REGEX.matches(kmsKeyResourceName)) {
    "kms_key_resource_name does not match the expected Cloud KMS key version format. " +
      "Expected: projects/<id>/locations/<loc>/keyRings/<ring>/cryptoKeys/<key>" +
      "/cryptoKeyVersions/<ver>. Got: $kmsKeyResourceName"
  }

  val gcpAccessToken = checkNotNull(System.getenv(GCP_ACCESS_TOKEN_ENV)?.toCharArray()) {
    "Missing required environment variable '$GCP_ACCESS_TOKEN_ENV'. " +
      "Save the output of 'gcloud auth print-access-token' to the '$GCP_ACCESS_TOKEN_ENV' " +
      "environment variable before invoking this script."
  }

  println("=== Sign Release Binary via Cloud KMS ===")
  println("  Unsigned AAB : ${unsignedAabPath.toAbsolutePath()}")
  println("  KMS key      : $kmsKeyResourceName")
  println("  Certificate  : ${certPemPath.toAbsolutePath()}")
  println("  Output AAB   : ${outputAabPath.toAbsolutePath()}")
  println()

  ScriptBackgroundCoroutineDispatcher().use { scriptBgDispatcher ->
    val commandExecutor = CommandExecutorImpl(scriptBgDispatcher)
    val signer = CloudKmsSigner(
      kmsKeyResourceName = kmsKeyResourceName,
      gcpAccessToken = gcpAccessToken,
      commandExecutor = commandExecutor
    )
    signAndValidate(signer, unsignedAabPath, certPemPath, outputAabPath)
  }
}

/**
 * Checks that the unsigned AAB exists, then invokes [signer] to sign it, printing a success
 * message on completion. Any exceptions from the signer propagate to the caller.
 *
 * This is a separate function so tests can inject a [FakeCloudSigner] without invoking [main].
 */
fun signAndValidate(
  signer: CloudSigner,
  unsignedAabPath: Path,
  certPemPath: Path,
  outputAabPath: Path
) {
  check(unsignedAabPath.toFile().exists()) {
    "Unsigned AAB not found at: ${unsignedAabPath.toAbsolutePath()}"
  }
  signer.sign(unsignedAabPath, certPemPath, outputAabPath)
  println("Signing complete. Signed AAB: ${outputAabPath.toAbsolutePath()}")
}

/**
 * Regex matching a full Cloud KMS asymmetric key version resource name.
 *
 * Format: `projects/<id>/locations/<loc>/keyRings/<ring>/cryptoKeys/<key>/cryptoKeyVersions/<ver>`
 */
private val KMS_RESOURCE_NAME_REGEX = Regex(
  """^projects/[^/]+/locations/[^/]+/keyRings/[^/]+/cryptoKeys/[^/]+/cryptoKeyVersions/\d+$"""
)

private const val GCP_ACCESS_TOKEN_ENV = "GCP_ACCESS_TOKEN"
