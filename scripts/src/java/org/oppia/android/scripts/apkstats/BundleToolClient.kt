package org.oppia.android.scripts.apkstats

import java.io.File
import java.util.zip.ZipFile
import org.oppia.android.scripts.common.CommandExecutor
import org.oppia.android.scripts.common.CommandExecutorImpl

class BundleToolClient(
  private val workingDirectoryPath: String,
  private val commandExecutor: CommandExecutor = CommandExecutorImpl()
) {
  private val workingDirectory by lazy { File(workingDirectoryPath) }

  // CLI reference: https://developer.android.com/studio/command-line/bundletool.

  fun buildApks(
    inputBundlePath: String, outputApksListPath: String, outputApkDirPath: String
  ): List<File> {
    val destDir = File(outputApkDirPath)
    return buildApkList(inputBundlePath, outputApksListPath).use { zipFile ->
      val apkEntries =
        zipFile.entries()
          .asSequence()
          .filter { !it.isDirectory && it.name.endsWith(".apk", ignoreCase = true) }
      return@use apkEntries.map { entry ->
        val outputApkFile = File(destDir, entry.name.substringAfter('/'))
        zipFile.extractTo(entry.name, outputApkFile.absolutePath)
      }.toList()
    }
  }

  fun buildUniversalApk(inputBundlePath: String, outputApkPath: String): File {
    return buildApkList(inputBundlePath, "$outputApkPath.apks", "--mode=universal").use { zipFile ->
      zipFile.extractTo("universal.apk", outputApkPath)
    }
  }

  private fun buildApkList(
    inputBundlePath: String, outputApksListPath: String, vararg additionalArgs: String
  ): ZipFile {
    executeBundleToolCommand(
      "build-apks", "--bundle=$inputBundlePath", "--output=$outputApksListPath", *additionalArgs
    )
    return ZipFile(File(outputApksListPath))
  }

  private fun executeBundleToolCommand(vararg arguments: String): List<String> {
    // Reference for retaining the classpath: https://stackoverflow.com/a/4330928. Note that this
    // approach is needed vs. using reflection since bundle tool seems to not allow multiple
    // subsequent calls (so each call must be in a new process).
    val result =
      commandExecutor.executeCommand(
        workingDirectory,
        "java",
        "-classpath",
        System.getProperty("java.class.path") ?: ".",
        "com.android.tools.build.bundletool.BundleToolMain",
        *arguments
      )
    check(result.exitCode == 0) {
      "Expected non-zero exit code (not ${result.exitCode}) for command: ${result.command}." +
        "\nStandard output:\n${result.output.joinToString("\n")}" +
        "\nError output:\n${result.errorOutput.joinToString("\n")}"
    }
    return result.output
  }

  private companion object {
    private fun ZipFile.extractTo(entryName: String, destPath: String): File {
      val destFile = File(destPath)
      destFile.outputStream().use { outputStream ->
        getInputStream(getEntry(entryName)).copyTo(outputStream)
      }
      return destFile
    }
  }
}
