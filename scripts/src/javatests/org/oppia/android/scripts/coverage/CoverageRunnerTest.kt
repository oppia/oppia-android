package org.oppia.android.scripts.coverage

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
import org.oppia.android.scripts.proto.Coverage
import org.oppia.android.scripts.proto.CoverageReport
import org.oppia.android.scripts.proto.CoveredLine
import org.oppia.android.scripts.testing.TestBazelWorkspace
import org.oppia.android.testing.assertThrows
import java.util.concurrent.TimeUnit

/** Tests for [CoverageRunner]. */
class CoverageRunnerTest {
  @field:[Rule JvmField] val tempFolder = TemporaryFolder()

  private val scriptBgDispatcher by lazy { ScriptBackgroundCoroutineDispatcher() }
  private val longCommandExecutor by lazy { initializeCommandExecutorWithLongProcessWaitTime() }

  private lateinit var coverageRunner: CoverageRunner
  private lateinit var testBazelWorkspace: TestBazelWorkspace
  private lateinit var bazelTestTarget: String

  @Before
  fun setUp() {
    coverageRunner = CoverageRunner(tempFolder.root, scriptBgDispatcher, longCommandExecutor)
    bazelTestTarget = "//:testTarget"
    testBazelWorkspace = TestBazelWorkspace(tempFolder)
  }

  @After
  fun tearDown() {
    scriptBgDispatcher.close()
  }

  @Test
  fun testRunWithCoverageAsync_emptyDirectory_throwsException() {
    val exception = assertThrows<IllegalStateException>() {
      runBlocking {
        coverageRunner.runWithCoverageAsync(bazelTestTarget).await()
      }
    }

    assertThat(exception).hasMessageThat().contains("not invoked from within a workspace")
  }

  @Test
  fun testRunWithCoverageAsync_invalidTestTarget_throwsException() {
    testBazelWorkspace.initEmptyWorkspace()

    val exception = assertThrows<IllegalStateException>() {
      runBlocking {
        coverageRunner.runWithCoverageAsync(bazelTestTarget).await()
      }
    }

    assertThat(exception).hasMessageThat().contains("Expected non-zero exit code")
    assertThat(exception).hasMessageThat().contains("no such package")
  }

  @Test
  fun testRunWithCoverageAsync_validSampleTestTarget_returnsCoverageData() {
    testBazelWorkspace.initEmptyWorkspace()

    val sourceContent =
      """
      package com.example
      
      class TwoSum {
      
          companion object {
              fun sumNumbers(a: Int, b: Int): Any {
                  return if (a == 0 && b == 0) {
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
      testFilename = "TwoSumTest",
      sourceContent = sourceContent,
      testContent = testContent,
      sourceSubpackage = "coverage/main/java/com/example",
      testSubpackage = "coverage/test/java/com/example"
    )

    val result = runBlocking {
      coverageRunner.runWithCoverageAsync(
        "//coverage/test/java/com/example:TwoSumTest"
      ).await()
    }

    val expectedResult = CoverageReport.newBuilder()
      .setBazelTestTarget("//coverage/test/java/com/example:TwoSumTest")
      .setFilePath("coverage/main/java/com/example/TwoSum.kt")
      .setFileSha1Hash("1020b8f405555b3f4537fd07b912d3fb9ffa3354")
      .addCoveredLine(
        CoveredLine.newBuilder()
          .setLineNumber(3)
          .setCoverage(Coverage.NONE)
          .build()
      )
      .addCoveredLine(
        CoveredLine.newBuilder()
          .setLineNumber(7)
          .setCoverage(Coverage.FULL)
          .build()
      )
      .addCoveredLine(
        CoveredLine.newBuilder()
          .setLineNumber(8)
          .setCoverage(Coverage.FULL)
          .build()
      )
      .addCoveredLine(
        CoveredLine.newBuilder()
          .setLineNumber(10)
          .setCoverage(Coverage.FULL)
          .build()
      )
      .setLinesFound(4)
      .setLinesHit(3)
      .build()

    assertThat(result).isEqualTo(expectedResult)
  }

  private fun initializeCommandExecutorWithLongProcessWaitTime(): CommandExecutorImpl {
    return CommandExecutorImpl(
      scriptBgDispatcher, processTimeout = 5, processTimeoutUnit = TimeUnit.MINUTES
    )
  }
}
