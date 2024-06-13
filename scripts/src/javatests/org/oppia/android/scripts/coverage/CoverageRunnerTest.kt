package org.oppia.android.scripts.coverage

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
import org.oppia.android.scripts.testing.TestBazelWorkspace
import org.oppia.android.testing.assertThrows

/** Tests for [CoverageRunner] */
class CoverageRunnerTest {
  @field:[Rule JvmField] val tempFolder = TemporaryFolder()

  private val scriptBgDispatcher by lazy { ScriptBackgroundCoroutineDispatcher() }

  private lateinit var coverageRunner: CoverageRunner
  private lateinit var testBazelWorkspace: TestBazelWorkspace
  private lateinit var bazelTestTarget: String

  @Before
  fun setUp() {
    coverageRunner = CoverageRunner(tempFolder.root, scriptBgDispatcher)
    bazelTestTarget = "//:testTarget"
    testBazelWorkspace = TestBazelWorkspace(tempFolder)
  }

  @After
  fun tearDown() {
    scriptBgDispatcher.close()
  }

  @Test
  fun testParseCoverageDataFile_invalidData_returnsNull() {
    // Result data from coverage execution that doesn't contain path to coverage data file [coverage.dat]
    val invalidResultData = listOf("data1", "data2", "data3")

    val parsedData = coverageRunner.parseCoverageDataFile(invalidResultData)
    // Return Null when the coverage data file path is not found
    assertThat(parsedData).isNull()
  }

  @Test
  fun testParseCoverageDataFile_validData_returnsNull() {
    // Result data from coverage execution that contains path to coverage data file [coverage.dat]
    val validResultData = listOf(
      "//package/test/example:test   PASSED in 0.4s",
      "/path/.cache/bazel/4654367352564/sandbox/__main__/__tmp/coverage/package/test/coverage.dat",
      "Executed 1 out of 1 test: 1 test  passes."
    )
    val expectedResultParsedData =
      "/path/.cache/bazel/4654367352564/sandbox/__main__/__tmp/coverage/package/test/coverage.dat"

    val parsedData = coverageRunner.parseCoverageDataFile(validResultData)
    assertThat(parsedData).isEqualTo(expectedResultParsedData)
  }

  @Test
  fun testRunCoverage_emptyDirectory_throwsException() {
    val exception = assertThrows<IllegalStateException>() {
      coverageRunner.getCoverage(bazelTestTarget)
    }

    assertThat(exception).hasMessageThat().contains("not invoked from within a workspace")
  }

  @Test
  fun testRunCoverage_invalidTestTarget_throwsException() {
    testBazelWorkspace.initEmptyWorkspace()

    val exception = assertThrows<IllegalStateException>() {
      coverageRunner.getCoverage(bazelTestTarget)
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

    val result = coverageRunner.getCoverage("//coverage/test/java/com/example:test")

    // Check that the test has "PASSED"
    val containsPassedValue = result.any { it.contains("PASSED") }
    assert(containsPassedValue) { "The test is not 'PASSED'" }

    // Check if the coverage.dat file is generated
    val containsCoverageData = result.any { it.contains("coverage.dat") }
    assert(containsCoverageData) { "The coverage.dat is not generated" }
  }
}
