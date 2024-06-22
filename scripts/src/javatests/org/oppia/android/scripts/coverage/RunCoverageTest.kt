package org.oppia.android.scripts.coverage

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
import org.oppia.android.scripts.proto.BranchCoverage
import org.oppia.android.scripts.proto.CoverageReport
import org.oppia.android.scripts.proto.CoveredFile
import org.oppia.android.scripts.proto.CoveredLine
import org.oppia.android.scripts.proto.FunctionCoverage
import org.oppia.android.scripts.testing.TestBazelWorkspace
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.util.concurrent.TimeUnit

/** Tests for [RunCoverage]. */
class RunCoverageTest {
  @field:[Rule JvmField] val tempFolder = TemporaryFolder()

  private val outContent: ByteArrayOutputStream = ByteArrayOutputStream()
  private val originalOut: PrintStream = System.out

  private val scriptBgDispatcher by lazy { ScriptBackgroundCoroutineDispatcher() }
  private val commandExecutor by lazy { CommandExecutorImpl(scriptBgDispatcher) }
  private val longCommandExecutor by lazy { initializeCommandExecutorWithLongProcessWaitTime() }

  private lateinit var testBazelWorkspace: TestBazelWorkspace
  private lateinit var sampleFilePath: String

  @Before
  fun setUp() {
    sampleFilePath = "/path/to/Sample.kt"
    testBazelWorkspace = TestBazelWorkspace(tempFolder)
    System.setOut(PrintStream(outContent))
  }

  @After
  fun tearDown() {
    System.setOut(originalOut)
    scriptBgDispatcher.close()
  }

  @Test
  fun testRunCoverage_testFileExempted_noCoverage() {
    val exemptedFilePath = "app/src/main/java/org/oppia/android/app/activity/ActivityComponent.kt"

    RunCoverage(
      "${tempFolder.root}",
      exemptedFilePath,
      commandExecutor,
      scriptBgDispatcher
    ).execute()

    assertThat(outContent.toString())
      .isEqualTo("This file is exempted from having a test file. Hence No coverage!\n")
  }

  @Test
  fun testRunCoverage_ScriptsPath_returnTestFilePath() {
    val rootFolderPath = tempFolder.root.absolutePath
    val expectedTestFilePath = "scripts/javatests/sample/ExampleTest.kt"
    val file = File(rootFolderPath, expectedTestFilePath)

    file.parentFile?.mkdirs()
    file.createNewFile()

    val expectedTestFilePaths = listOf(expectedTestFilePath)

    val result = RunCoverage(
      rootFolderPath,
      sampleFilePath,
      commandExecutor,
      scriptBgDispatcher
    ).findTestFile(rootFolderPath, "scripts/java/sample/Example.kt")

    assertEquals(expectedTestFilePaths, result)
  }

  @Test
  fun testRunCoverage_AppPath_returnSharedTestFilePath() {
    val rootFolderPath = tempFolder.root.absolutePath
    val expectedSharedTestFilePath = "app/sharedTest/sample/ExampleTest.kt"
    val file = File(rootFolderPath, expectedSharedTestFilePath)

    file.parentFile?.mkdirs()
    file.createNewFile()

    val expectedSharedTestFilePaths = listOf(expectedSharedTestFilePath)

    val result = RunCoverage(
      rootFolderPath,
      sampleFilePath,
      commandExecutor,
      scriptBgDispatcher
    ).findTestFile(rootFolderPath, "app/main/sample/Example.kt")

    assertEquals(expectedSharedTestFilePaths, result)
  }

  @Test
  fun testRunCoverage_AppPath_returnLocalTestFilePath() {
    val rootFolderPath = tempFolder.root.absolutePath
    val expectedLocalTestFilePath = "app/test/sample/ExampleTest.kt"
    val file = File(rootFolderPath, expectedLocalTestFilePath)

    file.parentFile?.mkdirs()
    file.createNewFile()

    val expectedLocalTestFilePaths = listOf(expectedLocalTestFilePath)

    val result = RunCoverage(
      rootFolderPath,
      sampleFilePath,
      commandExecutor,
      scriptBgDispatcher
    ).findTestFile(rootFolderPath, "app/main/sample/Example.kt")

    assertEquals(expectedLocalTestFilePaths, result)
  }

  @Test
  fun testRunCoverage_AppPath_returnSharedAndLocalTestFilePath() {
    val rootFolderPath = tempFolder.root.absolutePath
    val expectedLocalTestFilePath = "app/test/sample/ExampleTest.kt"
    val expectedSharedTestFilePath = "app/sharedTest/sample/ExampleTest.kt"

    val sharedFile = File(rootFolderPath, expectedSharedTestFilePath)
    sharedFile.parentFile?.mkdirs()
    sharedFile.createNewFile()

    val localFile = File(rootFolderPath, expectedLocalTestFilePath)
    localFile.parentFile?.mkdirs()
    localFile.createNewFile()

    val expectedLocalAndSharedTestFilePaths = listOf(
      expectedSharedTestFilePath,
      expectedLocalTestFilePath
    )

    val result = RunCoverage(
      rootFolderPath,
      sampleFilePath,
      commandExecutor,
      scriptBgDispatcher
    ).findTestFile(rootFolderPath, "app/main/sample/Example.kt")

    assertEquals(expectedLocalAndSharedTestFilePaths, result)
  }

  @Test
  fun testRunCoverage_AppPath_returnDefaultTestFilePath() {
    val rootFolderPath = tempFolder.root.absolutePath
    val expectedLocalTestFilePath = "util/test/sample/ExampleTest.kt"
    val file = File(rootFolderPath, expectedLocalTestFilePath)

    file.parentFile?.mkdirs()
    file.createNewFile()

    val expectedLocalTestFilePaths = listOf(expectedLocalTestFilePath)

    val result = RunCoverage(
      rootFolderPath,
      sampleFilePath,
      commandExecutor,
      scriptBgDispatcher
    ).findTestFile(rootFolderPath, "util/main/sample/Example.kt")

    assertEquals(expectedLocalTestFilePaths, result)
  }

  @Test
  fun testRunCoverage_validSampleTestFile_returnsCoverageData() {
    testBazelWorkspace.initEmptyWorkspace()

    val sourceContent =
      """
      package com.example
      
      class TwoSum {
      
          companion object {
              fun sumNumbers(a: Int, b: Int): Any {
                  return if (a ==0 && b == 0) {
                      "Both numbers are zero"
                  } else {
                      a + b
                  }
              }
          }
      }
      """.trimIndent()

    val testContent =
      """
      package com.example
      
      import org.junit.Assert.assertEquals
      import org.junit.Test
      
      class TwoSumTest {
      
          @Test
          fun testSumNumbers() {
              assertEquals(TwoSum.sumNumbers(0, 1), 1)
              assertEquals(TwoSum.sumNumbers(3, 4), 7)         
              assertEquals(TwoSum.sumNumbers(0, 0), "Both numbers are zero")
          }
      }
      """.trimIndent()

    testBazelWorkspace.addSourceAndTestFileWithContent(
      filename = "TwoSum",
      sourceContent = sourceContent,
      testContent = testContent,
      subpackage = "coverage"
    )

    val result = RunCoverage(
      "${tempFolder.root}",
      "coverage/main/java/com/example/TwoSum.kt",
      longCommandExecutor,
      scriptBgDispatcher
    ).execute()

    val expectedCoveredFile = CoveredFile.newBuilder()
      .setFilePath("coverage/main/java/com/example/TwoSum.kt")
      .setFileSha1Hash("f6fb075e115775f6729615a79f0e7e34fe9735b5")
      .addCoveredLine(CoveredLine.newBuilder().setLineNumber(3).setCoverage(CoveredLine.Coverage.NONE).build())
      .addCoveredLine(CoveredLine.newBuilder().setLineNumber(7).setCoverage(CoveredLine.Coverage.FULL).build())
      .addCoveredLine(CoveredLine.newBuilder().setLineNumber(8).setCoverage(CoveredLine.Coverage.FULL).build())
      .addCoveredLine(CoveredLine.newBuilder().setLineNumber(10).setCoverage(CoveredLine.Coverage.FULL).build())
      .setLinesFound(4)
      .setLinesHit(3)
      .addFunctionCoverage(FunctionCoverage.newBuilder()
        .setLineNumber(7)
        .setFunctionName("com/example/TwoSum\$Companion::sumNumbers (II)Ljava/lang/Object;")
        .setExecutionCount(1)
        .setCoverage(FunctionCoverage.Coverage.FULL).build())
      .addFunctionCoverage(FunctionCoverage.newBuilder()
        .setLineNumber(3)
        .setFunctionName("com/example/TwoSum::<init> ()V")
        .setExecutionCount(0)
        .setCoverage(FunctionCoverage.Coverage.NONE).build())
      .setFunctionsFound(2)
      .setFunctionsHit(1)
      .addBranchCoverage(BranchCoverage.newBuilder()
        .setLineNumber(7)
        .setBlockNumber(0)
        .setBranchNumber(0)
        .setHitCount(1)
        .setCoverage(BranchCoverage.Coverage.FULL).build())
      .addBranchCoverage(BranchCoverage.newBuilder()
        .setLineNumber(7)
        .setBlockNumber(0)
        .setBranchNumber(1)
        .setHitCount(1)
        .setCoverage(BranchCoverage.Coverage.FULL).build())
      .addBranchCoverage(BranchCoverage.newBuilder()
        .setLineNumber(7)
        .setBlockNumber(0)
        .setBranchNumber(2)
        .setHitCount(1)
        .setCoverage(BranchCoverage.Coverage.FULL).build())
      .addBranchCoverage(BranchCoverage.newBuilder()
        .setLineNumber(7)
        .setBlockNumber(0)
        .setBranchNumber(3)
        .setHitCount(1)
        .setCoverage(BranchCoverage.Coverage.FULL).build())
      .setBranchesFound(4)
      .setBranchesHit(4)
      .build()

    val expectedResult = CoverageReport.newBuilder()
      .setBazelTestTarget("//coverage/test/java/com/example:TwoSumTest")
      .addCoveredFile(expectedCoveredFile)
      .build()

    val expectedResultList = mutableListOf(expectedResult)

    assertThat(result).isEqualTo(expectedResultList)
  }

  private fun initializeCommandExecutorWithLongProcessWaitTime(): CommandExecutorImpl {
    return CommandExecutorImpl(
      scriptBgDispatcher, processTimeout = 5, processTimeoutUnit = TimeUnit.MINUTES
    )
  }
}
