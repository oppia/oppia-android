package org.oppia.android.scripts.coverage

import org.oppia.android.scripts.common.BazelClient
import org.oppia.android.scripts.common.CommandExecutor
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
import org.oppia.android.scripts.proto.TestFileExemptions
import java.util.concurrent.TimeUnit
import java.io.File
import java.io.FileInputStream

fun main(vararg args: String) {
  val repoRoot = args[0]
  val rootDirectory = File(repoRoot).absoluteFile
  val targetPath = args[1]
  val filePath = args[2]

  ScriptBackgroundCoroutineDispatcher().use { scriptBgDispatcher ->
    val commandExecutor: CommandExecutor = CommandExecutorImpl(
      scriptBgDispatcher, processTimeout = 5, processTimeoutUnit = TimeUnit.MINUTES
    )

    val bazelClient = BazelClient(rootDirectory, commandExecutor)

    println("Repo root: $repoRoot")
    println("Targetpath: $targetPath")
    println("Filepath: $filePath")

    val testFileExemptiontextProto = "scripts/assets/test_file_exemptions"

    // A list of all the files to be exempted for this check.
    // TODO(#3436): Develop a mechanism for permanently exempting files which do not ever need tests.
    val testFileExemptionList = loadTestFileExemptionsProto(testFileExemptiontextProto)
      .getExemptedFilePathList()

//  println("test file exemption list: $testFileExemptionList")

    val isExempted = testFileExemptionList.contains(filePath)
    if (isExempted) {
      println("This file is exempted from having a test file. Hence No coverage!")
      return
    }

    val testFilePath = findTestFile(repoRoot, filePath)
    println("Test File paths list: $testFilePath")

    val result = bazelClient.retrieveBazelTargets(testFilePath)
    println("Result from Retrieve Bazel Target; $result")

    val testResults = listOf(
      "//utility/src/test/java/org/oppia/android/util/parser/math:MathModelTest",
      "//utility/src/test/java/org/oppia/android/util/math:FloatExtensionsTest")

    //.substringBeforeLast(".kt")

    for (r in testResults) {
      RunCoverageForTestTarget(
        rootDirectory,
        r,
        commandExecutor,
        scriptBgDispatcher
      ).runCoverage()
    }
  }


//  ScriptBackgroundCoroutineDispatcher().use { scriptBgDispatcher ->
//    val commandExecutor: CommandExecutor = CommandExecutorImpl(
//      scriptBgDispatcher, processTimeout = 5, processTimeoutUnit = TimeUnit.MINUTES
//    )
    /*RunCoverageForTestTarget(
      repoRoot,
      targetPath,
      commandExecutor,
      scriptBgDispatcher
    ).runCoverage()*/
//  }
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

    println("Shared: $sharedTestFilePath")
    println("Test: $testFilePath")
    println("LocalTest: $localTestFilePath")

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