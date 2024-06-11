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
      package com.example;

      public class Collatz {
      
        public static int getCollatzFinal(int n) {
          if (n == 1) {
            return 1;
          }
          if (n % 2 == 0) {
            return getCollatzFinal(n / 2);
          } else {
            return getCollatzFinal(n * 3 + 1);
          }
        }
      }
    """.trimIndent()

    val testContent = """
      package com.example;
      
      import static org.junit.Assert.assertEquals;
      import org.junit.Test;
      
      public class TestCollatz {
      
        @Test
        public void testGetCollatzFinal() {
          assertEquals(Collatz.getCollatzFinal(1), 1);
          assertEquals(Collatz.getCollatzFinal(5), 1);
          assertEquals(Collatz.getCollatzFinal(10), 1);
          assertEquals(Collatz.getCollatzFinal(21), 1);
        }
      }
    """.trimIndent()

    testBazelWorkspace.addSampleSourceAndTestFile(
      filename = "Collatz",
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