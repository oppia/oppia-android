package org.oppia.android.scripts.apkstats

import org.oppia.android.scripts.common.CommandExecutor
import org.oppia.android.scripts.common.CommandExecutorImpl
import java.io.File
import java.util.zip.ZipFile

/**
 * General utility for interfacing with bundletool in the local system at the specified working
 * directory path.
 *
 * @property commandExecutor the [CommandExecutor] to use when accessing bundletool
 */
class BundleToolClient(
  private val workingDirectoryPath: String,
  private val commandExecutor: CommandExecutor = CommandExecutorImpl()
) {
  private val workingDirectory by lazy { File(workingDirectoryPath) }

  // CLI reference: https://developer.android.com/studio/command-line/bundletool.

  /**
   * Builds & extracts configuration-specific APKs corresponding to the specified Android app
   * bundle.
   *
   * @param inputBundlePath the AAB from which to extract APKs
   * @param outputApksListPath the destination .apks file intermediary used to extract the APKs
   * @param outputApkDirPath the destination directory in which the extracted APKs should be written
   * @return the list of [File]s where each corresponds to one of the computed APKs
   */
  fun buildApks(
    inputBundlePath: String,
    outputApksListPath: String,
    outputApkDirPath: String
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

  /**
   * Builds and returns the file to a universal APK built in the specified output directory path and
   * built according to the specified Android app bundle.
   */
  fun buildUniversalApk(inputBundlePath: String, outputApkPath: String): File {
    return buildApkList(inputBundlePath, "$outputApkPath.apks", "--mode=universal").use { zipFile ->
      zipFile.extractTo("universal.apk", outputApkPath)
    }
  }

  private fun buildApkList(
    inputBundlePath: String,
    outputApksListPath: String,
    vararg additionalArgs: String
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
        computeAbsoluteClasspath(),
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
    private val currentDirectory by lazy { File(".") }

    private fun ZipFile.extractTo(entryName: String, destPath: String): File {
      val destFile = File(destPath)
      destFile.outputStream().use { outputStream ->
        getInputStream(getEntry(entryName)).copyTo(outputStream)
      }
      return destFile
    }

    private fun computeAbsoluteClasspath(): String {
      val classpath = System.getProperty("java.class.path") ?: "."
      val classpathComponents = classpath.split(":")
      return classpathComponents.map {
        it.convertToAbsolutePath()
      }.filterNot {
        it.isAndroidDependencyToOmit()
      }.joinToString(":")
    }

    private fun String.convertToAbsolutePath(): String {
      return File(currentDirectory, this).absolutePath
    }

    private fun String.isAndroidDependencyToOmit(): Boolean {
      // This is a hacky way to work around the classpath actually pulling in two versions of
      // Guava: Android & JRE. Bundle tool requires the JRE version, and there's no obvious way to
      // separate out the Maven dependencies without risking duplicate versions & automatic conflict
      // resolution.
      return File(this).name.let { name ->
        "guava" in name && "android" in name
      }
    }
  }
}
