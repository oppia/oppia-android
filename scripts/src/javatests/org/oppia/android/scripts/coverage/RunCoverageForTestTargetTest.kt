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

/** Tests for [RunCoverageForTestTarget] */
class RunCoverageForTestTargetTest {
  @field:[Rule JvmField] val tempFolder = TemporaryFolder()

  private val scriptBgDispatcher by lazy { ScriptBackgroundCoroutineDispatcher() }

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
        bazelTestTarget
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
        bazelTestTarget
      ).runCoverage()
    }

    assertThat(exception).hasMessageThat().contains("Expected non-zero exit code")
    assertThat(exception).hasMessageThat().contains("no such package")
  }

  @Test
  fun testRunCoverageForTestTarget_validSampleTestTarget_returnsCoverageDataPath() {
    testBazelWorkspace.initEmptyWorkspace()

    val sourceContent = """
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

    val testContent = """
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
      "//coverage/test/java/com/example:test"
    ).runCoverage()

    // Check if the coverage.dat file is generated and parsed as result
    val parsedCoverageDataPath = result?.endsWith("coverage.dat")
    assert(parsedCoverageDataPath!!) { "The coverage.dat is not generated" }
  }
}