package org.oppia.android.scripts.apkstats

import org.oppia.android.scripts.common.CommandExecutor
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
import java.io.File
import java.util.zip.ZipFile

/**
 * General utility for interfacing with bundletool in the local system at the specified working
 * directory path.
 *
 * @property workingDirectoryPath the path to the working directory in which instances of bundletool
 *     should be executed
 * @param scriptBgDispatcher the [ScriptBackgroundCoroutineDispatcher] to be used for running the
 *     bundletool command
 * @property commandExecutor the [CommandExecutor] to use when accessing bundletool
 */
class BundleToolClient(
  private val workingDirectoryPath: String,
  scriptBgDispatcher: ScriptBackgroundCoroutineDispatcher,
  private val commandExecutor: CommandExecutor = CommandExecutorImpl(scriptBgDispatcher)
) {
  private val workingDirectory by lazy { File(workingDirectoryPath) }
  private val bundleToolJarPath by lazy {
    File("external/android_bundletool/jar/downloaded.jar").absolutePath
  }

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
    // Execute bundletool via its standalone fat JAR in a separate process since bundletool does
    // not seem to support multiple in-process invocations and sharing the parent JVM classpath
    // causes dependency version conflicts (e.g. with protobuf-java and Android Guava).
    val result =
      commandExecutor.executeCommand(
        workingDirectory,
        "java",
        "-jar",
        bundleToolJarPath,
        *arguments
      )
    check(result.exitCode == 0) {
      "Expected zero exit code (not ${result.exitCode}) for command: ${result.command}." +
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
