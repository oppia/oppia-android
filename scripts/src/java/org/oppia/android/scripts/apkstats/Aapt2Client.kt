package org.oppia.android.scripts.apkstats

import java.io.File
import org.oppia.android.scripts.common.CommandExecutor
import org.oppia.android.scripts.common.CommandExecutorImpl

class Aapt2Client(
  private val workingDirectoryPath: String,
  private val androidSdkPath: String,
  private val buildToolsVersion: String,
  private val commandExecutor: CommandExecutor = CommandExecutorImpl()
) {
  private val workingDirectory by lazy { File(workingDirectoryPath) }
  // Note that this pathing will not work by default on Windows (since executables end with '.exe').
  private val aapt2Path by lazy {
    File(androidSdkPath, "build-tools/$buildToolsVersion/aapt2").absolutePath
  }

  // CLI reference: https://developer.android.com/studio/command-line/apkanalyzer.

  fun dumpPermissions(inputApkPath: String): List<String> {
    return executeApkAnalyzerCommand("dump", "permissions", inputApkPath)
  }

  fun dumpResources(inputApkPath: String): List<String> {
    return executeApkAnalyzerCommand("dump", "resources", inputApkPath)
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
