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

  private fun initializeCommandExecutorWithLongProcessWaitTime(): CommandExecutorImpl {
    return CommandExecutorImpl(
      scriptBgDispatcher, processTimeout = 5, processTimeoutUnit = TimeUnit.MINUTES
    )
  }
}
