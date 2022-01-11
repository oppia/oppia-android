package org.oppia.android.scripts.apkstats

import java.io.File
import org.oppia.android.scripts.common.CommandExecutor
import org.oppia.android.scripts.common.CommandExecutorImpl

class ApkAnalyzerClient(
  private val workingDirectoryPath: String,
  private val androidSdkPath: String,
  private val commandExecutor: CommandExecutor = CommandExecutorImpl()
) {
  private val workingDirectory by lazy { File(workingDirectoryPath) }
  // Note that this pathing will not work by default on Windows (since executables end with '.exe').
  private val apkAnalyzerPath by lazy { File(androidSdkPath, "tools/bin/apkanalyzer").absolutePath }

  // CLI reference: https://developer.android.com/studio/command-line/apkanalyzer.

  fun computeFileSize(inputApkPath: String): String {
    return executeApkAnalyzerCommand("apk", "file-size", inputApkPath).first()
  }

  fun computeDownloadSize(inputApkPath: String): String {
    return executeApkAnalyzerCommand("apk", "download-size", inputApkPath).first()
  }

  fun computeFeatures(inputApkPath: String): List<String> {
    return executeApkAnalyzerCommand("apk", "features", inputApkPath)
  }

  fun compare(inputApkPath1: String, inputApkPath2: String): List<String> {
    return executeApkAnalyzerCommand(
      "apk",
      "compare",
      "--different-only",
      "--files-only",
      "--patch-size",
      inputApkPath1,
      inputApkPath2
    )
  }

  fun computeDexReferencesList(inputApkPath: String): List<String> {
    return executeApkAnalyzerCommand("dex", "references", inputApkPath)
  }

  private fun executeApkAnalyzerCommand(vararg arguments: String): List<String> {
    val result = commandExecutor.executeCommand(workingDirectory, apkAnalyzerPath, *arguments)
    check(result.exitCode == 0) {
      "Expected non-zero exit code (not ${result.exitCode}) for command: ${result.command}." +
        "\nStandard output:\n${result.output.joinToString("\n")}" +
        "\nError output:\n${result.errorOutput.joinToString("\n")}"
    }
    return result.output
  }
}
