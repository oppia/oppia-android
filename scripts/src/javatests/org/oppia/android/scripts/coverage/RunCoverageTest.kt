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
      sourceSubpackage = "coverage/main/java/com/example",
      testSubpackage = "coverage/test/java/com/example"
    )

    val result = RunCoverage(
      "${tempFolder.root}",
      "coverage/main/java/com/example/TwoSum.kt",
      longCommandExecutor,
      scriptBgDispatcher
    ).execute()

    val expectedResultList = mutableListOf(
      listOf(
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
    )

    assertThat(result).isEqualTo(expectedResultList)
  }

  @Test
  fun testRunCoverage_validScriptPathSampleTestFile_returnsCoverageData() {
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
      sourceSubpackage = "scripts/java/com/example",
      testSubpackage = "scripts/javatests/com/example"
    )

    val result = RunCoverage(
      "${tempFolder.root}",
      "scripts/java/com/example/TwoSum.kt",
      longCommandExecutor,
      scriptBgDispatcher
    ).execute()

    val expectedResultList = mutableListOf(
      listOf(
        "SF:scripts/java/com/example/TwoSum.kt",
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
    )

    assertThat(result).isEqualTo(expectedResultList)
  }

  @Test
  fun testRunCoverage_validAppPathSampleTestFile_returnsCoverageData() {
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
      sourceSubpackage = "app/main/java/com/example",
      testSubpackage = "app/test/java/com/example"
    )

    val result = RunCoverage(
      "${tempFolder.root}",
      "app/main/java/com/example/TwoSum.kt",
      longCommandExecutor,
      scriptBgDispatcher
    ).execute()

    val expectedResultList = mutableListOf(
      listOf(
        "SF:app/main/java/com/example/TwoSum.kt",
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
    )

    assertThat(result).isEqualTo(expectedResultList)
  }

  @Test
  fun testRunCoverage_validMultiSampleTestFile_returnsCoverageData() {
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

    val expectedResultList = mutableListOf(
      listOf(
        "SF:app/main/java/com/example/TwoSum.kt",
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
      ),listOf(
        "SF:app/main/java/com/example/TwoSum.kt",
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
    )

    assertThat(result).isEqualTo(expectedResultList)
  }

  private fun initializeCommandExecutorWithLongProcessWaitTime(): CommandExecutorImpl {
    return CommandExecutorImpl(
      scriptBgDispatcher, processTimeout = 5, processTimeoutUnit = TimeUnit.MINUTES
    )
  }
}
