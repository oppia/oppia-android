package org.oppia.android.scripts.coverage

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import kotlinx.coroutines.async
import kotlinx.coroutines.CoroutineScope
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
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
  fun testRunCoverage_emptyDirectory_throwsException() {
    val exception = assertThrows<IllegalStateException>() {
      runBlocking {
        coverageRunner.runWithCoverageAsync(bazelTestTarget).await()
      }
    }

    assertThat(exception).hasMessageThat().contains("not invoked from within a workspace")
  }

  @Test
  fun testRunCoverage_invalidTestTarget_throwsException() {
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
  fun testRunCoverage_validSampleTestTarget_returnsCoverageData() {
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

    val result = runBlocking {
      coverageRunner.runWithCoverageAsync(
        "//coverage/test/java/com/example:test"
      ).await()
    }
    val expectedResult = listOf(
      "SF:coverage/main/java/com/example/TwoSum.kt",
      "FN:7,com/example/TwoSum${'$'}Companion::sumNumbers (II)Ljava/lang/Object;",
      "FN:3,com/example/TwoSum::<init> ()V",
      "FNDA:1,com/example/TwoSum${'$'}Companion::sumNumbers (II)Ljava/lang/Object;",
      "FNDA:0,com/example/TwoSum::<init> ()V",
      "FNF:2",
      "FNH:1",
      "BRDA:7,0,0,1",
      "BRDA:7,0,1,1",
      "BRDA:7,0,2,1",
      "BRDA:7,0,3,1",
      "BRF:4",
      "BRH:4",
      "DA:3,0",
      "DA:7,1",
      "DA:8,1",
      "DA:10,1",
      "LH:3",
      "LF:4",
      "end_of_record"
    )

    assertThat(result).isEqualTo(expectedResult)
  }

  private fun initializeCommandExecutorWithLongProcessWaitTime(): CommandExecutorImpl {
    return CommandExecutorImpl(
      scriptBgDispatcher, processTimeout = 5, processTimeoutUnit = TimeUnit.MINUTES
    )
  }
}
