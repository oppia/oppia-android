package org.oppia.android.scripts.apkstats

import org.oppia.android.scripts.common.CommandExecutor
import org.oppia.android.scripts.common.CommandExecutorImpl
import java.io.File

/**
 * General utility for interfacing with AAPT2 in the local system at the specified working directory
 * path and contained within the specified Android SDK (per the given path).
 *
 * Note that in order for binary dependencies to utilize this client, they must add a 'data'
 * dependency on the AAPT2 binary included as part of the Android SDK, e.g.:
 *
 * ```bazel
 * data = ["@androidsdk//:aapt2_binary"]
 * ```
 *
 * @property buildToolsVersion the version of Android build tools installed & that should be used.
 *     This value should be coordinated with the build system used by the APKs accessed by this
 *     utility.
 * @property commandExecutor the [CommandExecutor] to use when accessing AAPT2
 */
class Aapt2Client(
  private val workingDirectoryPath: String,
  private val buildToolsVersion: String,
  private val commandExecutor: CommandExecutor = CommandExecutorImpl()
) {
  private val workingDirectory by lazy { File(workingDirectoryPath) }
  // Note that this pathing will not work by default on Windows (since executables end with '.exe').
  private val aapt2Path by lazy {
    File("external/androidsdk", "build-tools/$buildToolsVersion/aapt2").absolutePath
  }

  // CLI reference: https://developer.android.com/studio/command-line/apkanalyzer.

  /** Returns the permissions dump as reported by AAPT2 for the specified APK. */
  fun dumpPermissions(inputApkPath: String): List<String> {
    return executeApkAnalyzerCommand("dump", "permissions", inputApkPath)
  }

  /** Returns the resources dump as reported by AAPT2 for the specified APK. */
  fun dumpResources(inputApkPath: String): List<String> {
    return executeApkAnalyzerCommand("dump", "resources", inputApkPath)
  }

  /**
   * Returns badging information, that is, high-level details like supported locales and densities,
   * from the specified APK's manifest.
   */
  fun dumpBadging(inputApkPath: String): List<String> {
    return executeApkAnalyzerCommand("dump", "badging", inputApkPath)
  }

  private fun executeApkAnalyzerCommand(vararg arguments: String): List<String> {
    val result = commandExecutor.executeCommand(workingDirectory, aapt2Path, *arguments)
    check(result.exitCode == 0) {
      "Expected non-zero exit code (not ${result.exitCode}) for command: ${result.command}." +
        "\nStandard output:\n${result.output.joinToString("\n")}" +
        "\nError output:\n${result.errorOutput.joinToString("\n")}"
    }
    return result.output
  }
}
