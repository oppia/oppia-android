package org.oppia.android.scripts.coverage

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
import org.oppia.android.scripts.testing.TestBazelWorkspace
import org.oppia.android.scripts.proto.BranchCoverage
import org.oppia.android.scripts.proto.CoverageReport
import org.oppia.android.scripts.proto.CoveredFile
import org.oppia.android.scripts.proto.CoveredLine
import org.oppia.android.scripts.proto.FunctionCoverage
import org.oppia.android.testing.assertThrows
import java.util.concurrent.TimeUnit

/** Tests for [RunCoverageForTestTarget]. */
class RunCoverageForTestTargetTest {
  @field:[Rule JvmField] val tempFolder = TemporaryFolder()

  private val scriptBgDispatcher by lazy { ScriptBackgroundCoroutineDispatcher() }
  private val commandExecutor by lazy { CommandExecutorImpl(scriptBgDispatcher) }
  private val longCommandExecutor by lazy { initializeCommandExecutorWithLongProcessWaitTime() }

  private lateinit var testBazelWorkspace: TestBazelWorkspace
  private lateinit var bazelTestTarget: String

  @Before
  fun setUp() {
    bazelTestTarget = "//:testTarget"
    testBazelWorkspace = TestBazelWorkspace(tempFolder)
  }

  @After
  fun tearDown() {
    scriptBgDispatcher.close()
  }

  @Test
  fun testRunCoverageForTestTarget_emptyDirectory_throwsException() {
    val exception = assertThrows<IllegalStateException>() {
      RunCoverageForTestTarget(
        tempFolder.root,
        bazelTestTarget,
        commandExecutor,
        scriptBgDispatcher
      ).runCoverage()
    }

    assertThat(exception).hasMessageThat().contains("not invoked from within a workspace")
  }

  @Test
  fun testRunCoverageForTestTarget_invalidTestTarget_throwsException() {
    testBazelWorkspace.initEmptyWorkspace()

    val exception = assertThrows<IllegalStateException>() {
      RunCoverageForTestTarget(
        tempFolder.root,
        bazelTestTarget,
        commandExecutor,
        scriptBgDispatcher
      ).runCoverage()
    }

    assertThat(exception).hasMessageThat().contains("Expected non-zero exit code")
    assertThat(exception).hasMessageThat().contains("no such package")
  }

  @Test
  fun testRunCoverageForTestTarget_validSampleTestTarget_returnsCoverageDataPath() {
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

    val result = RunCoverageForTestTarget(
      tempFolder.root,
      "//coverage/test/java/com/example:TwoSumTest",
      longCommandExecutor,
      scriptBgDispatcher
    ).runCoverage()


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

    assertThat(result).isEqualTo(expectedResult)
  }

  private fun initializeCommandExecutorWithLongProcessWaitTime(): CommandExecutorImpl {
    return CommandExecutorImpl(
      scriptBgDispatcher, processTimeout = 5, processTimeoutUnit = TimeUnit.MINUTES
    )
  }
}
