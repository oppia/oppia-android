package org.oppia.android.scripts.coverage

import com.google.common.truth.Truth.assertThat
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
  fun testRunCoverage_invalidFile_throwsException() {
    testBazelWorkspace.initEmptyWorkspace()
    val exception = assertThrows<IllegalStateException>() {
      main(tempFolder.root.absolutePath, "file.kt")
    }

    assertThat(exception).hasMessageThat().contains("File doesn't exist")
  }

  @Test
  fun testRunCoverage_missingTestFileNotExempted_throwsException() {
    testBazelWorkspace.initEmptyWorkspace()
    val exception = assertThrows<IllegalStateException>() {
      val sampleFile = File(tempFolder.root.absolutePath, "file.kt")
      sampleFile.createNewFile()
      main(tempFolder.root.absolutePath, "file.kt")
    }

    assertThat(exception).hasMessageThat().contains("No appropriate test file found")
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
      .isEqualTo("This file is exempted from having a test file; skipping coverage check.\n")
  }

  @Test
  fun testRunCoverage_sampleTests_returnsCoverageData() {
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
      testFilename = "TwoSumTest",
      sourceContent = sourceContent,
      testContent = testContent,
      sourceSubpackage = "coverage/main/java/com/example",
      testSubpackage = "coverage/test/java/com/example"
    )

    val result = RunCoverage(
      "${tempFolder.root}",
      "coverage/main/java/com/example/TwoSum.kt",
      longCommandExecutor,
      scriptBgDispatcher
    ).execute()

    val expectedResult = listOf(
      CoverageReport.newBuilder()
        .setBazelTestTarget("//coverage/test/java/com/example:TwoSumTest")
        .setFilePath("coverage/main/java/com/example/TwoSum.kt")
        .setFileSha1Hash("f6fb075e115775f6729615a79f0e7e34fe9735b5")
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
    )

    assertThat(result).isEqualTo(expectedResult)
  }

  @Test
  fun testRunCoverage_scriptTests_returnsCoverageData() {
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
      sourceSubpackage = "scripts/java/com/example",
      testSubpackage = "scripts/javatests/com/example"
    )

    val result = RunCoverage(
      "${tempFolder.root}",
      "scripts/java/com/example/TwoSum.kt",
      longCommandExecutor,
      scriptBgDispatcher
    ).execute()

    val expectedResult = listOf(
      CoverageReport.newBuilder()
        .setBazelTestTarget("//scripts/javatests/com/example:TwoSumTest")
        .setFilePath("scripts/java/com/example/TwoSum.kt")
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
    )

    assertThat(result).isEqualTo(expectedResult)
  }

  @Test
  fun testRunCoverage_appTests_returnsCoverageData() {
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
      testFilename = "TwoSumTest",
      sourceContent = sourceContent,
      testContent = testContent,
      sourceSubpackage = "app/main/java/com/example",
      testSubpackage = "app/test/java/com/example"
    )

    val result = RunCoverage(
      "${tempFolder.root}",
      "app/main/java/com/example/TwoSum.kt",
      longCommandExecutor,
      scriptBgDispatcher
    ).execute()

    val expectedResult = listOf(
      CoverageReport.newBuilder()
        .setBazelTestTarget("//app/test/java/com/example:TwoSumTest")
        .setFilePath("app/main/java/com/example/TwoSum.kt")
        .setFileSha1Hash("f6fb075e115775f6729615a79f0e7e34fe9735b5")
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
    )

    assertThat(result).isEqualTo(expectedResult)
  }

  @Test
  fun testRunCoverage_localTests_returnsCoverageData() {
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
      
      class TwoSumLocalTest {
      
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
      testFilename = "TwoSumLocalTest",
      sourceContent = sourceContent,
      testContent = testContent,
      sourceSubpackage = "app/main/java/com/example",
      testSubpackage = "app/test/java/com/example"
    )

    val result = RunCoverage(
      "${tempFolder.root}",
      "app/main/java/com/example/TwoSum.kt",
      longCommandExecutor,
      scriptBgDispatcher
    ).execute()

    val expectedResult = listOf(
      CoverageReport.newBuilder()
        .setBazelTestTarget("//app/test/java/com/example:TwoSumLocalTest")
        .setFilePath("app/main/java/com/example/TwoSum.kt")
        .setFileSha1Hash("f6fb075e115775f6729615a79f0e7e34fe9735b5")
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
    )

    assertThat(result).isEqualTo(expectedResult)
  }

  @Test
  fun testRunCoverage_sharedTests_returnsCoverageData() {
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
      testFilename = "TwoSumTest",
      sourceContent = sourceContent,
      testContent = testContent,
      sourceSubpackage = "app/main/java/com/example",
      testSubpackage = "app/sharedTest/java/com/example"
    )

    val result = RunCoverage(
      "${tempFolder.root}",
      "app/main/java/com/example/TwoSum.kt",
      longCommandExecutor,
      scriptBgDispatcher
    ).execute()

    val expectedResult = listOf(
      CoverageReport.newBuilder()
        .setBazelTestTarget("//app/sharedTest/java/com/example:TwoSumTest")
        .setFilePath("app/main/java/com/example/TwoSum.kt")
        .setFileSha1Hash("f6fb075e115775f6729615a79f0e7e34fe9735b5")
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
    )

    assertThat(result).isEqualTo(expectedResult)
  }

  @Test
  fun testRunCoverage_sharedAndLocalTests_returnsCoverageData() {
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

    val testContentShared =
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

    val testContentLocal =
      """
      package com.example
      
      import org.junit.Assert.assertEquals
      import org.junit.Test
      
      class TwoSumLocalTest {
      
          @Test
          fun testSumNumbers() {
              assertEquals(TwoSum.sumNumbers(0, 1), 1)
              assertEquals(TwoSum.sumNumbers(3, 4), 7)         
              assertEquals(TwoSum.sumNumbers(0, 0), "Both numbers are zero")
          }
      }
      """.trimIndent()

    testBazelWorkspace.addMultiLevelSourceAndTestFileWithContent(
      filename = "TwoSum",
      sourceContent = sourceContent,
      testContentShared = testContentShared,
      testContentLocal = testContentLocal,
      subpackage = "app"
    )

    val result = RunCoverage(
      "${tempFolder.root}",
      "app/main/java/com/example/TwoSum.kt",
      longCommandExecutor,
      scriptBgDispatcher
    ).execute()

    val expectedResult = listOf(
      CoverageReport.newBuilder()
        .setBazelTestTarget("//app/sharedTest/java/com/example:TwoSumTest")
        .setFilePath("app/main/java/com/example/TwoSum.kt")
        .setFileSha1Hash("f6fb075e115775f6729615a79f0e7e34fe9735b5")
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
        .build(),
      CoverageReport.newBuilder()
        .setBazelTestTarget("//app/test/java/com/example:TwoSumLocalTest")
        .setFilePath("app/main/java/com/example/TwoSum.kt")
        .setFileSha1Hash("f6fb075e115775f6729615a79f0e7e34fe9735b5")
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
    )

    assertThat(result).isEqualTo(expectedResult)
  }

  private fun initializeCommandExecutorWithLongProcessWaitTime(): CommandExecutorImpl {
    return CommandExecutorImpl(
      scriptBgDispatcher, processTimeout = 5, processTimeoutUnit = TimeUnit.MINUTES
    )
  }
}
