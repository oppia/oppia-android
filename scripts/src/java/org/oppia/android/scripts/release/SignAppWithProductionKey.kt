package org.oppia.android.scripts.release

import java.io.File
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.common.GitClient

fun main(vararg args: String) {
  val repoPath = "${args[0]}/"
  val minSdkVersion = args[1].toInt()

  val repoRoot = File(repoPath)
}

/**
 * @param repoPath - path to the repository.
 * @param baseDevelopBranchReference - reference to the base develop branch.
 * @param minSdkVersion - minimum SDK version to be used in the release.
 * @param pathToApkBinary - path to the APK binary to be used in the release.
 * @param pathToApkSigningKey - path to the APK signing key to be used in the release.
 * @param pathToApkSigner - path to the APK signer to be used in the release.
 */
/* This class is used to sign the release binaries. */
class SignAppWithProductionKey(
  val workingDirectory: File,
  val baseDevelopBranchReference: String,
  val minSdkVersion: Int,
  val pathToApkBinary: File,
  val pathToKeyStore: File,
  val pathToApkSigner: File
) {
  private val commandExecutor by lazy {
    CommandExecutorImpl()
  }
  private val gitClient by lazy {
    GitClient(workingDirectory, baseDevelopBranchReference)
  }

  /**
   * Method for getting the key store available to other processes
   * and closing it if requirements are satisfied.
   * @param show - boolean value to either show or hide the keystore.
   */
  private fun showAndHideKeyStore(show: Boolean) {
    when (show) {
      true -> commandExecutor.executeCommand(
        pathToKeyStore,
        "git",
        "secret", "hide", "-d"
      )
      false -> commandExecutor.executeCommand(
        pathToKeyStore,
        "git",
        "secret", "reveal"
      )
    }
  }

  /* Method for signing the apk binary with production key so that  it can be relased on the play-store. */
  private fun signApk() {
    commandExecutor.executeCommand(
      workingDirectory,
      pathToApkSigner.absolutePath,
      "sign",
      "$minSdkVersion",
      pathToKeyStore.absolutePath,
      pathToApkBinary.absolutePath
    )
  }

  /* Method for validating the signing of the apk binary. */
  private fun validateSigningApk() {
    commandExecutor.executeCommand(
      workingDirectory,
      "unzip",
      pathToApkBinary.absolutePath
    )

    val binaryVerification = commandExecutor.executeCommand(
      workingDirectory,
      "jarsigner",
      "-verify",
      "-verbose",
      "-keystore",
      pathToKeyStore.absolutePath,
      "-storepass",
      "android",
      "-keypass",
      "android",
      pathToApkBinary.absolutePath
    )
    if (binaryVerification.exitCode != 0) {
      throw Exception("Binary verification failed ${binaryVerification.errorOutput}")
    }
  }
}