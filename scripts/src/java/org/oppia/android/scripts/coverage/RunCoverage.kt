package org.oppia.android.scripts.coverage

import org.oppia.android.scripts.common.BazelClient
import org.oppia.android.scripts.common.CommandExecutor
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
import org.oppia.android.scripts.proto.TestFileExemptions
import java.io.File
import java.io.FileInputStream
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

  ScriptBackgroundCoroutineDispatcher().use { scriptBgDispatcher ->
    val processTimeout: Long = args.find { it.startsWith("processTimeout=") }
      ?.substringAfter("=")
      ?.toLongOrNull() ?: 5

    val commandExecutor: CommandExecutor = CommandExecutorImpl(
      scriptBgDispatcher, processTimeout = processTimeout, processTimeoutUnit = TimeUnit.MINUTES
    )

    RunCoverage(repoRoot, filePath, commandExecutor, scriptBgDispatcher).execute()
  }
}

/**
 * Class responsible for executing coverage on a given file.
 *
 * @param repoRoot the root directory of the repository
 * @param filePath the relative path to the file to analyse coverage
 * @param commandExecutor Executes the specified command in the specified working directory
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
   */
  fun execute(): MutableList<List<String>> {
    var coverageDataList = mutableListOf<List<String>>()
    val testFileExemptionList = loadTestFileExemptionsProto(testFileExemptionTextProto)
      .getExemptedFilePathList()

    val isExempted = testFileExemptionList.contains(filePath)
    if (isExempted) {
      println("This file is exempted from having a test file. Hence No coverage!")
      return mutableListOf()
    }

    val testFilePaths = findTestFile(repoRoot, filePath)
    val testTargets = bazelClient.retrieveBazelTargets(testFilePaths)

    for (testTarget in testTargets) {
      val coverageData = RunCoverageForTestTarget(
        rootDirectory,
        testTarget.substringBeforeLast(".kt"),
        commandExecutor,
        scriptBgDispatcher
      ).runCoverage()!!
      coverageDataList.add(coverageData)
    }
    println("Coverage Data List: $coverageDataList")
    return coverageDataList
  }

  /**
   * Finds potential test file paths corresponding to a given source file path within a repository.
   *
   * @param repoRoot the root directory of the repository
   * @param filePath The file path of the source file for which the test files are to be found.
   * @return A list of potential test file paths that exist in the repository.
   */
  fun findTestFile(repoRoot: String, filePath: String): List<String> {
    val file = File(filePath)
    val parts = file.parent.split(File.separator)
    val testFiles = mutableListOf<String>()

    if (parts.isNotEmpty() && parts[0] == "scripts") {
      val testFilePath = filePath.replace("/java/", "/javatests/").replace(".kt", "Test.kt")
      if (File(repoRoot, testFilePath).exists()) {
        testFiles.add(testFilePath)
      }
    } else if (parts.isNotEmpty() && parts[0] == "app") {
      val sharedTestFilePath = filePath.replace("/main/", "/test/").replace(".kt", "Test.kt")
      val testFilePath = filePath.replace("/main/", "/test/").replace(".kt", "Test.kt")
      val localTestFilePath = filePath.replace("/main/", "/test/").replace(".kt", "LocalTest.kt")

      if (File(repoRoot, sharedTestFilePath).exists()) {
        testFiles.add(sharedTestFilePath)
      }
      if (File(repoRoot, testFilePath).exists()) {
        testFiles.add(testFilePath)
      }
      if (File(repoRoot, localTestFilePath).exists()) {
        testFiles.add(localTestFilePath)
      }
    } else {
      val defaultTestFilePath = filePath.replace("/main/", "/test/").replace(".kt", "Test.kt")
      if (File(repoRoot, defaultTestFilePath).exists()) {
        testFiles.add(defaultTestFilePath)
      }
    }
    return testFiles
  }

  private fun loadTestFileExemptionsProto(testFileExemptiontextProto: String): TestFileExemptions {
    val protoBinaryFile = File("$testFileExemptiontextProto.pb")
    val builder = TestFileExemptions.getDefaultInstance().newBuilderForType()

    // This cast is type-safe since proto guarantees type consistency from mergeFrom(),
    // and this method is bounded by the generic type T.
    @Suppress("UNCHECKED_CAST")
    val protoObj: TestFileExemptions =
      FileInputStream(protoBinaryFile).use {
        builder.mergeFrom(it)
      }.build() as TestFileExemptions
    return protoObj
  }
}
