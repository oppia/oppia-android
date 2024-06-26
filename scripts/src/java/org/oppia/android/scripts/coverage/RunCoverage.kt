package org.oppia.android.scripts.coverage

import kotlinx.coroutines.runBlocking
import org.oppia.android.scripts.common.BazelClient
import org.oppia.android.scripts.common.CommandExecutor
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
import org.oppia.android.scripts.proto.TestFileExemptions
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Entry point function for running coverage analysis for a source file.
 *
 * Usage:
 *   bazel run //scripts:run_coverage_for_test_target -- <path_to_root> <relative_path_to_file>
 *
 * Arguments:
 * - path_to_root: directory path to the root of the Oppia Android repository.
 * - relative_path_to_file: the relative path to the file to analyse coverage
 *
 * Example:
 *     bazel run //scripts:run_coverage -- $(pwd)
 *     utility/src/main/java/org/oppia/android/util/parser/math/MathModel.kt
 * Example with custom process timeout:
 *     bazel run //scripts:run_coverage -- $(pwd)
 *     utility/src/main/java/org/oppia/android/util/parser/math/MathModel.kt processTimeout=10
 *
 */
fun main(vararg args: String) {
  val repoRoot = args[0]
  val filePath = args[1]

  if (!File(repoRoot, filePath).exists()) {
    error("File doesn't exist.")
  }

  ScriptBackgroundCoroutineDispatcher().use { scriptBgDispatcher ->
    val processTimeout: Long = args.find { it.startsWith("processTimeout=") }
      ?.substringAfter("=")
      ?.toLongOrNull() ?: 5

    val commandExecutor: CommandExecutor = CommandExecutorImpl(
      scriptBgDispatcher, processTimeout = processTimeout, processTimeoutUnit = TimeUnit.MINUTES
    )

    println(RunCoverage(repoRoot, filePath, commandExecutor, scriptBgDispatcher).execute())
  }
}

/**
 * Class responsible for executing coverage on a given file.
 *
 * @param repoRoot the root directory of the repository
 * @param filePath the relative path to the file to analyse coverage
 * @param commandExecutor executes the specified command in the specified working directory
 * @param scriptBgDispatcher the [ScriptBackgroundCoroutineDispatcher] to be used for running the coverage command
 */
class RunCoverage(
  private val repoRoot: String,
  private val filePath: String,
  private val commandExecutor: CommandExecutor,
  private val scriptBgDispatcher: ScriptBackgroundCoroutineDispatcher
) {
  private val bazelClient by lazy { BazelClient(File(repoRoot), commandExecutor) }

  private val rootDirectory = File(repoRoot).absoluteFile
  private val testFileExemptionTextProto = "scripts/assets/test_file_exemptions"

  /**
   * Executes coverage analysis for the specified file.
   *
   * Loads test file exemptions and checks if the specified file is exempted. If exempted,
   * prints a message indicating no coverage analysis is performed. Otherwise, initializes
   * a Bazel client, finds potential test file paths, retrieves Bazel targets, and initiates
   * coverage analysis for each test target found.
   *
   * @return a list of lists containing coverage data for each requested test target, if
   *     the file is exempted from having a test file, an empty list is returned
   */
  fun execute(): List<List<String>> {
    val testFileExemptionList = loadTestFileExemptionsProto(testFileExemptionTextProto)
      .getExemptedFilePathList()

    if (filePath in testFileExemptionList) {
      println("This file is exempted from having a test file; skipping coverage check.")
      return emptyList()
    }

    val testFilePaths = findTestFile(repoRoot, filePath)
    val testTargets = bazelClient.retrieveBazelTargets(testFilePaths)

    return testTargets.mapNotNull { testTarget ->
      val coverageData = runCoverageForTarget(testTarget)
      if (coverageData == null) {
        println("Coverage data for $testTarget is null")
      }
      coverageData
    }
  }

  private fun runCoverageForTarget(testTarget: String): List<String>? {
    return runBlocking {
      CoverageRunner(rootDirectory, scriptBgDispatcher, commandExecutor)
        .runWithCoverageAsync(testTarget.removeSuffix(".kt"))
        .await()
    }
  }
}

private fun findTestFile(repoRoot: String, filePath: String): List<String> {
  val possibleTestFilePaths = when {
    filePath.startsWith("scripts/") -> {
      listOf(filePath.replace("/java/", "/javatests/").replace(".kt", "Test.kt"))
    }
    filePath.startsWith("app/") -> {
      listOf(
        filePath.replace("/main/", "/sharedTest/").replace(".kt", "Test.kt"),
        filePath.replace("/main/", "/test/").replace(".kt", "Test.kt"),
        filePath.replace("/main/", "/test/").replace(".kt", "LocalTest.kt")
      )
    }
    else -> {
      listOf(filePath.replace("/main/", "/test/").replace(".kt", "Test.kt"))
    }
  }

  val repoRootFile = File(repoRoot).absoluteFile

  return possibleTestFilePaths
    .map { File(repoRootFile, it) }
    .filter(File::exists)
    .map { it.relativeTo(repoRootFile).path }
}

private fun loadTestFileExemptionsProto(testFileExemptiontextProto: String): TestFileExemptions {
  return File("$testFileExemptiontextProto.pb").inputStream().use { stream ->
    TestFileExemptions.newBuilder().also { builder ->
      builder.mergeFrom(stream)
    }.build()
  }
}
