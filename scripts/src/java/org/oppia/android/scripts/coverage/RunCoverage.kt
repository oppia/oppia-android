package org.oppia.android.scripts.coverage

import org.oppia.android.scripts.common.BazelClient
import org.oppia.android.scripts.common.CommandExecutor
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
import org.oppia.android.scripts.proto.TestFileExemptions
import java.util.concurrent.TimeUnit
import java.io.File
import java.io.FileInputStream

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

  val rootDirectory = File(repoRoot).absoluteFile

  val testFileExemptiontextProto = "scripts/assets/test_file_exemptions"

  // A list of all the files to be exempted for this check.
  val testFileExemptionList = loadTestFileExemptionsProto(testFileExemptiontextProto)
    .getExemptedFilePathList()

  val isExempted = testFileExemptionList.contains(filePath)
  if (isExempted) {
    println("This file is exempted from having a test file. Hence No coverage!")
    return
  }

  ScriptBackgroundCoroutineDispatcher().use { scriptBgDispatcher ->
    val processTimeout: Long = args.find { it.startsWith("processTimeout=") }
      ?.substringAfter("=")
      ?.toLongOrNull() ?: 5

    val commandExecutor: CommandExecutor = CommandExecutorImpl(
      scriptBgDispatcher, processTimeout = processTimeout, processTimeoutUnit = TimeUnit.MINUTES
    )

    val bazelClient = BazelClient(rootDirectory, commandExecutor)

    val testFilePath = findTestFile(repoRoot, filePath)
    val testTargets = bazelClient.retrieveBazelTargets(testFilePath)

    for (testTarget in testTargets) {
      RunCoverageForTestTarget(
        rootDirectory,
        testTarget.substringBeforeLast(".kt"),
        commandExecutor,
        scriptBgDispatcher
      ).runCoverage()
    }
  }
}

private fun findTestFile(repoRoot: String, filePath: String): List<String> {
  val file = File(filePath)
  val parts = file.parent.split(File.separator)
  val testFiles = mutableListOf<String>()

  if (parts.isNotEmpty() && parts[0] == "scripts") {
    val testFilePath = filePath.replace("/java/", "/javatests/").replace(".kt", "Test.kt")
    if (File(testFilePath).exists()) {
      testFiles.add(testFilePath)
    }
  } else if (parts.isNotEmpty() && parts[0] == "app") {
    val sharedTestFilePath = filePath.replace("/main/", "/sharedTest/").replace(".kt", "Test.kt")
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
