package org.oppia.android.scripts.coverage

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
import java.io.File
import java.util.concurrent.TimeUnit

/** Tests for [CoverageRunner]. */
class CoverageRunnerTest {
  @field:[Rule JvmField] val tempFolder = TemporaryFolder()

  private val scriptBgDispatcher by lazy { ScriptBackgroundCoroutineDispatcher() }
  private val longCommandExecutor by lazy { initializeCommandExecutorWithLongProcessWaitTime() }

  private lateinit var coverageRunner: CoverageRunner
  private lateinit var testBazelWorkspace: TestBazelWorkspace
  private lateinit var bazelTestTarget: String

  private lateinit var sourceContent: String
  private lateinit var testContent: String

  @Before
  fun setUp() {
    coverageRunner = CoverageRunner(File(tempFolder.root.absolutePath), scriptBgDispatcher, longCommandExecutor)
    bazelTestTarget = "//:testTarget"
    testBazelWorkspace = TestBazelWorkspace(tempFolder)

    sourceContent =
      """
      package com.example
      
      class AddNums {
      
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

    testContent =
      """
      package com.example
      
      import org.junit.Assert.assertEquals
      import org.junit.Test
      
      class AddNumsTest {
      
          @Test
          fun testSumNumbers() {
              assertEquals(AddNums.sumNumbers(0, 1), 1)
              assertEquals(AddNums.sumNumbers(3, 4), 7)         
              assertEquals(AddNums.sumNumbers(0, 0), "Both numbers are zero")
          }
      }
      """.trimIndent()
  }

  @After
  fun tearDown() {
    scriptBgDispatcher.close()
  }

  @Test
  fun testRetrieveCoverageDataForTestTarget_emptyDirectory_throwsException() {
    val exception = assertThrows<IllegalStateException>() {
      coverageRunner.retrieveCoverageDataForTestTarget(bazelTestTarget)
    }

    assertThat(exception).hasMessageThat().contains("not invoked from within a workspace")
  }

  @Test
  fun testRetrieveCoverageDataForTestTarget_invalidTestTarget_throwsException() {
    testBazelWorkspace.initEmptyWorkspace()

    val exception = assertThrows<IllegalStateException>() {
      coverageRunner.retrieveCoverageDataForTestTarget(bazelTestTarget)
    }

    assertThat(exception).hasMessageThat().contains("Expected non-zero exit code")
    assertThat(exception).hasMessageThat().contains("no such package")
  }

  @Test
  fun testRetrieveCoverageDataForTestTarget_coverageRetrievalFailed_throwsException() {
//    val coverageFilePath = "${tempFolder}/bazel-out/k8-fastbuild/testlogs" +
//      "/coverage/test/java/com/example/AddNumsTest/coverage.dat"
    val pattern = Regex(".*bazel-out/k8-fastbuild/testlogs/coverage/test/java/com/example/AddNumsTest/coverage.dat")

    testBazelWorkspace.initEmptyWorkspace()
    testBazelWorkspace.addSourceAndTestFileWithContent(
      filename = "AddNums",
      testFilename = "AddNumsTest",
      sourceContent = sourceContent,
      testContent = testContent,
      sourceSubpackage = "coverage/main/java/com/example",
      testSubpackage = "coverage/test/java/com/example"
    )

    val exception = assertThrows<IllegalStateException>() {
      runBlocking {
        launch {
          coverageRunner.retrieveCoverageDataForTestTarget("//coverage/test/java/com/example:AddNumsTest")
        }

        launch {
          do {
//            val dir = tempFolder.root.absolutePath.split('/')
            val dir2 = tempFolder.root.absolutePath.substringBeforeLast('/')
//            val dir2List = File(dir2).listFiles()

            // Traverse the directory top-down
            File(dir2).walkTopDown().forEach { file ->
              if (file.isFile && pattern.matches(file.absolutePath)) {
                println("Found file: ${file.absolutePath}")

                // Check if the file exists (should always be true if the path matched)
                if (file.exists()) {
                  println("File exists. Writing to the file...")

                  // Write to the file
                  file.writeText("")

                  println("Write operation completed.")
                  return@launch
                } else {
                  delay(1)
                }
              }
            }

/*//            val filer = File(tempFolder.root.absolutePath).absoluteFile
//            val files = File(tempFolder.root.absolutePath).listFiles() ?: emptyArray()
//            println("Files: $filer")

//            val matchingFiles = files.filter { file ->
//              pattern.matches(file.absolutePath)
//            }
//
//            if (matchingFiles.isNotEmpty()) {
//              for (file in matchingFiles) {
//                file.writeText("")
//                println("Processed: ${file.absolutePath}")
//                return@launch
//              }
//            }
//
//            /*File(coverageFilePath).takeIf { it.exists() }?.apply {
//              writeText("")*/
              return@launch
//            }*/
//    assertThat(files).isEqualTo("hi")
//    assertThat(dir2List).isEqualTo("hi")
//            delay(1)
          } while (true)
        }
      }
    }

    assertThat(exception).hasMessageThat().contains("Failed to retrieve coverage result")
  }

  @Test
  fun testRetrieveCoverageDataForTestTarget_coverageDataMissing_throwsException() {
    val coverageFilePath = "${tempFolder.root}/bazel-out/k8-fastbuild/testlogs" +
      "/coverage/test/java/com/example/AddNumsTest/coverage.dat"

    testBazelWorkspace.initEmptyWorkspace()
    testBazelWorkspace.addSourceAndTestFileWithContent(
      filename = "AddNums",
      testFilename = "AddNumsTest",
      sourceContent = sourceContent,
      testContent = testContent,
      sourceSubpackage = "coverage/main/java/com/example",
      testSubpackage = "coverage/test/java/com/example"
    )

    val exception = assertThrows<IllegalArgumentException>() {
      runBlocking {
        launch {
          coverageRunner.retrieveCoverageDataForTestTarget("//coverage/test/java/com/example:AddNumsTest")
        }

        launch {
          do {
            File(coverageFilePath).takeIf { it.exists() }?.apply {
              writeText("SF: coverage/test/java/com/example/IncorrectCoverageFile.kt")
              return@launch
            }
            delay(1)
          } while (true)
        }
      }
    }

    assertThat(exception).hasMessageThat().contains("Coverage data not found")
  }

  @Test
  fun testRetrieveCoverageDataForTestTarget_validSampleTestTarget_returnsCoverageData() {
    testBazelWorkspace.initEmptyWorkspace()
    testBazelWorkspace.addSourceAndTestFileWithContent(
      filename = "AddNums",
      testFilename = "AddNumsTest",
      sourceContent = sourceContent,
      testContent = testContent,
      sourceSubpackage = "coverage/main/java/com/example",
      testSubpackage = "coverage/test/java/com/example"
    )

    val result = coverageRunner.retrieveCoverageDataForTestTarget(
      "//coverage/test/java/com/example:AddNumsTest"
    )

    val expectedResult = CoverageReport.newBuilder()
      .setBazelTestTarget("//coverage/test/java/com/example:AddNumsTest")
      .setFilePath("coverage/main/java/com/example/AddNums.kt")
      .setFileSha1Hash("cdb04b7e8a1c6a7adaf5807244b1a524b4f4bb44")
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
