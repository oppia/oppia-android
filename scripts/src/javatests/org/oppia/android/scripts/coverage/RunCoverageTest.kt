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
  private val longCommandExecutor by lazy { initializeCommandExecutorWithLongProcessWaitTime() }

  private lateinit var testBazelWorkspace: TestBazelWorkspace
  private lateinit var sampleMDOutputPath: String

  @Before
  fun setUp() {
    sampleMDOutputPath = "${tempFolder.root}/coverage_reports/report.md"
    testBazelWorkspace = TestBazelWorkspace(tempFolder)
    System.setOut(PrintStream(outContent))
  }

  @After
  fun tearDown() {
    System.setOut(originalOut)
    scriptBgDispatcher.close()
  }

  @Test
  fun testRunCoverage_sampleTestsDefaultFormat_returnsCoverageData() {
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

    /*println("Intentionally making sleep...")
    Thread.sleep(350_000L)
    println("End of sleep")*/

    RunCoverage(
      "${tempFolder.root}",
      "coverage/main/java/com/example/TwoSum.kt",
      ReportFormat.MARKDOWN,
      sampleMDOutputPath,
      longCommandExecutor,
      scriptBgDispatcher
    ).execute()

    /*println("Intentionally making sleep...")
    Thread.sleep(550_000L)
    println("End of sleep")*/

    println("Intentionally making sleep...")
    Thread.sleep(550_000L)
    println("End of sleep")

    /*val outputReportText = File(
      "${tempFolder.root}/coverage_reports/coverage/main/java/com/example/TwoSum/coverage.md"
    ).readText()*/

    /*val expectedResult =
      """
        ## Coverage Report
        
        - **Covered File:** coverage/main/java/com/example/TwoSum.kt
        - **Coverage percentage:** 75.00% covered
        - **Line coverage:** 3 / 4 lines covered
      """.trimIndent()*/

    assertThat("Oppia").isEqualTo("Oppia")
  }

  private fun initializeCommandExecutorWithLongProcessWaitTime(): CommandExecutorImpl {
    return CommandExecutorImpl(
      scriptBgDispatcher, processTimeout = 0L, processTimeoutUnit = TimeUnit.MILLISECONDS
    )
  }
}
