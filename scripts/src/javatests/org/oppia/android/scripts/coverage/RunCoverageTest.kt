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
import org.oppia.android.testing.assertThrows
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.util.concurrent.TimeUnit

/** Tests for [RunCoverage]. */
class RunCoverageTest {
  private val MIN_THRESHOLD: Int = 10
  @field:[Rule JvmField] val tempFolder = TemporaryFolder()

  private val outContent: ByteArrayOutputStream = ByteArrayOutputStream()
  private val originalOut: PrintStream = System.out

  private val scriptBgDispatcher by lazy { ScriptBackgroundCoroutineDispatcher() }
  private val longCommandExecutor by lazy { initializeCommandExecutorWithLongProcessWaitTime() }

  private lateinit var testBazelWorkspace: TestBazelWorkspace
  private lateinit var coverageDir: String
  private lateinit var markdownOutputPath: String
  private lateinit var htmlOutputPath: String

  private lateinit var addSourceContent: String
  private lateinit var addTestContent: String

  @Before
  fun setUp() {
    coverageDir = "/coverage_reports"
    markdownOutputPath = "${tempFolder.root}/coverage_reports/report.md"
    htmlOutputPath = "${tempFolder.root}/coverage_reports/report.html"
    testBazelWorkspace = TestBazelWorkspace(tempFolder)

    addSourceContent =
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

    addTestContent =
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
    System.setOut(PrintStream(outContent))
    testBazelWorkspace.initEmptyWorkspace()
    val sampleFile = File(tempFolder.root.absolutePath, "file.kt")
    sampleFile.createNewFile()
    main(tempFolder.root.absolutePath, "file.kt")

    assertThat(outContent.toString().trim()).contains(
      "No appropriate test file found for file.kt"
    )
  }

  @Test
  fun testRunCoverage_invalidFormat_throwsException() {
    testBazelWorkspace.initEmptyWorkspace()
    val exception = assertThrows<IllegalArgumentException>() {
      main(tempFolder.root.absolutePath, "file.kt", "--format=PDF")
    }

    assertThat(exception).hasMessageThat().contains("Unsupported report format")
  }

  @Test
  fun testRunCoverage_ignoreCaseMarkdownArgument_generatesCoverageReport() {
    val filePath = "coverage/main/java/com/example/AddNums.kt"

    testBazelWorkspace.initEmptyWorkspace()
    testBazelWorkspace.addSourceAndTestFileWithContent(
      filename = "AddNums",
      testFilename = "AddNumsTest",
      sourceContent = addSourceContent,
      testContent = addTestContent,
      sourceSubpackage = "coverage/main/java/com/example",
      testSubpackage = "coverage/test/java/com/example"
    )

    main(
      "${tempFolder.root}",
      filePath,
      "--format=Markdown",
      "--processTimeout=10"
    )

    val outputFilePath = "${tempFolder.root}" +
      "$coverageDir/CoverageReport.md"

    assertThat(File(outputFilePath).exists()).isTrue()
  }

  @Test
  fun testRunCoverage_ignoreCaseHtmlArgument_generatesCoverageReport() {
    val filePath = "coverage/main/java/com/example/AddNums.kt"

    testBazelWorkspace.initEmptyWorkspace()
    testBazelWorkspace.addSourceAndTestFileWithContent(
      filename = "AddNums",
      testFilename = "AddNumsTest",
      sourceContent = addSourceContent,
      testContent = addTestContent,
      sourceSubpackage = "coverage/main/java/com/example",
      testSubpackage = "coverage/test/java/com/example"
    )

    main(
      "${tempFolder.root}",
      filePath,
      "--format=Html",
      "--processTimeout=10"
    )

    val outputFilePath = "${tempFolder.root}" +
      "$coverageDir/${filePath.removeSuffix(".kt")}/coverage.html"

    assertThat(File(outputFilePath).exists()).isTrue()
  }

  @Test
  fun testRunCoverage_reorderedArguments_generatesCoverageReport() {
    val filePath = "coverage/main/java/com/example/AddNums.kt"

    testBazelWorkspace.initEmptyWorkspace()
    testBazelWorkspace.addSourceAndTestFileWithContent(
      filename = "AddNums",
      testFilename = "AddNumsTest",
      sourceContent = addSourceContent,
      testContent = addTestContent,
      sourceSubpackage = "coverage/main/java/com/example",
      testSubpackage = "coverage/test/java/com/example"
    )

    main(
      "${tempFolder.root}",
      filePath,
      "--processTimeout=10",
      "--format=MARKDOWN"
    )

    val outputFilePath = "${tempFolder.root}" +
      "$coverageDir/CoverageReport.md"

    assertThat(File(outputFilePath).exists()).isTrue()
  }

  @Test
  fun testRunCoverage_testFileExempted_skipsCoverage() {
    val exemptedFile = "app/src/main/java/org/oppia/android/app/activity/ActivityComponent.kt"
    System.setOut(PrintStream(outContent))
    val exemptedFilePathList = listOf(exemptedFile)

    RunCoverage(
      "${tempFolder.root}",
      exemptedFilePathList,
      ReportFormat.MARKDOWN,
      longCommandExecutor,
      scriptBgDispatcher
    ).execute()

    assertThat(outContent.toString().trim()).contains(
      "Exempted File: $exemptedFile"
    )
  }

  @Test
  fun testRunCoverage_sampleTestsDefaultFormat_generatesCoverageReport() {
    val filePath = "coverage/main/java/com/example/AddNums.kt"

    testBazelWorkspace.initEmptyWorkspace()
    testBazelWorkspace.addSourceAndTestFileWithContent(
      filename = "AddNums",
      testFilename = "AddNumsTest",
      sourceContent = addSourceContent,
      testContent = addTestContent,
      sourceSubpackage = "coverage/main/java/com/example",
      testSubpackage = "coverage/test/java/com/example"
    )

    main(
      "${tempFolder.root}",
      filePath,
    )

    val outputReportText = File(
      "${tempFolder.root}" +
        "$coverageDir/${filePath.removeSuffix(".kt")}/coverage.html"
    ).readText()

    val expectedResult = getExpectedHtmlText(filePath)

    assertThat(outputReportText).isEqualTo(expectedResult)
  }

  @Test
  fun testRunCoverage_sampleTestsMarkdownFormat_generatesCoverageReport() {
    val filePathList = listOf("coverage/main/java/com/example/AddNums.kt")

    testBazelWorkspace.initEmptyWorkspace()
    testBazelWorkspace.addSourceAndTestFileWithContent(
      filename = "AddNums",
      testFilename = "AddNumsTest",
      sourceContent = addSourceContent,
      testContent = addTestContent,
      sourceSubpackage = "coverage/main/java/com/example",
      testSubpackage = "coverage/test/java/com/example"
    )

    RunCoverage(
      "${tempFolder.root}",
      filePathList,
      ReportFormat.MARKDOWN,
      longCommandExecutor,
      scriptBgDispatcher
    ).execute()

    val outputReportText = File(
      "${tempFolder.root}" +
        "$coverageDir/CoverageReport.md"
    ).readText().trimEnd()

    val expectedResult = getExpectedMarkdownText(filePathList.get(0))

    assertThat(outputReportText).isEqualTo(expectedResult)
  }

  @Test
  fun testRunCoverage_withMultipleFilesMarkdownFormat_generatesCoverageReport() {
    val oppiaDevelopGitHubLink = "https://github.com/oppia/oppia-android/tree/develop"
    val filePathList = listOf(
      "coverage/main/java/com/example/AddNums.kt",
      "coverage/main/java/com/example/SubNums.kt"
    )

    val subSourceContent =
      """
      package com.example
      
      class SubNums {
        companion object {
          fun subNumbers(a: Int, b: Int): Any {
            return if (a == 0 && b == 0) {
                "Both numbers are zero"
            } else {
                a - b
            }
          }
        }
      }
      """.trimIndent()

    val subTestContent =
      """
      package com.example
      
      import org.junit.Assert.assertEquals
      import org.junit.Test
      
      class SubNumsTest {
        @Test
        fun testSubNumbers() {
          assertEquals(SubNums.subNumbers(1, 0), 1)
          assertEquals(SubNums.subNumbers(4, 3), 1)         
        }
      }
      """.trimIndent()

    testBazelWorkspace.initEmptyWorkspace()

    testBazelWorkspace.addSourceAndTestFileWithContent(
      filename = "AddNums",
      testFilename = "AddNumsTest",
      sourceContent = addSourceContent,
      testContent = addTestContent,
      sourceSubpackage = "coverage/main/java/com/example",
      testSubpackage = "coverage/test/java/com/example"
    )

    testBazelWorkspace.addSourceAndTestFileWithContent(
      filename = "SubNums",
      testFilename = "SubNumsTest",
      sourceContent = subSourceContent,
      testContent = subTestContent,
      sourceSubpackage = "coverage/main/java/com/example",
      testSubpackage = "coverage/test/java/com/example"
    )

    RunCoverage(
      "${tempFolder.root}",
      filePathList,
      ReportFormat.MARKDOWN,
      longCommandExecutor,
      scriptBgDispatcher
    ).execute()

    for (file in filePathList) {
      val outputReportText = File(
        "${tempFolder.root}" +
          "$coverageDir/CoverageReport.md"
      ).readText().trimEnd()

      val expectedResult = buildString {
        appendLine("## Coverage Report")
        appendLine()
        appendLine("- Number of files assessed: 2")
        appendLine()
        appendLine("<details>")
        appendLine("<summary>Succeeded Coverages</summary><br>")
        appendLine()
        appendLine("| File | Coverage | Lines Hit | Status | Min Required |")
        appendLine("|------|:--------:|----------:|:------:|:------------:|")
        appendLine(
          "| [${filePathList.get(0).substringAfterLast("/")}]" +
            "($oppiaDevelopGitHubLink/${filePathList.get(0)}) | 75.00% | 3 / 4 | " +
            ":white_check_mark: | $MIN_THRESHOLD% |"
        )
        appendLine(
          "| [${filePathList.get(1).substringAfterLast("/")}]" +
            "($oppiaDevelopGitHubLink/${filePathList.get(1)}) | 50.00% | 2 / 4 | " +
            ":white_check_mark: | $MIN_THRESHOLD% |"
        )
        appendLine("</details>")
      }.trimEnd()

      assertThat(outputReportText).isEqualTo(expectedResult)
    }
  }

  // add check failure later
  /*@Test
  fun testRunCoverage_withCoverageStatusFail_throwsException() {
    val filePathList = listOf(
      "coverage/main/java/com/example/AddNums.kt",
      "coverage/main/java/com/example/LowTestNums.kt"
    )

    val lowTestSourceContent =
      """
      package com.example
      
      class LowTestNums {
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

    val lowTestTestContent =
      """
      package com.example
      
      import org.junit.Assert.assertEquals
      import org.junit.Test
      
      class LowTestNumsTest {
        @Test
        fun testSumNumbers() {
          assertEquals(1, 1)
        }
      }
      """.trimIndent()

    testBazelWorkspace.initEmptyWorkspace()
    testBazelWorkspace.addSourceAndTestFileWithContent(
      filename = "AddNums",
      testFilename = "AddNumsTest",
      sourceContent = addSourceContent,
      testContent = addTestContent,
      sourceSubpackage = "coverage/main/java/com/example",
      testSubpackage = "coverage/test/java/com/example"
    )

    testBazelWorkspace.addSourceAndTestFileWithContent(
      filename = "LowTestNums",
      testFilename = "LowTestNumsTest",
      sourceContent = lowTestSourceContent,
      testContent = lowTestTestContent,
      sourceSubpackage = "coverage/main/java/com/example",
      testSubpackage = "coverage/test/java/com/example"
    )

    val exception = assertThrows<IllegalStateException>() {
      RunCoverage(
        "${tempFolder.root}",
        filePathList,
        ReportFormat.MARKDOWN,
        longCommandExecutor,
        scriptBgDispatcher
      ).execute()
    }

    assertThat(exception).hasMessageThat().contains(
      "Coverage Analysis Failed as minimum coverage threshold not met!"
    )
  }*/

  @Test
  fun testRunCoverage_withSuccessFiles_generatesFinalCoverageReport() {
    val filePathList = listOf(
      "coverage/main/java/com/example/AddNums.kt"
    )

    testBazelWorkspace.initEmptyWorkspace()
    testBazelWorkspace.addSourceAndTestFileWithContent(
      filename = "AddNums",
      testFilename = "AddNumsTest",
      sourceContent = addSourceContent,
      testContent = addTestContent,
      sourceSubpackage = "coverage/main/java/com/example",
      testSubpackage = "coverage/test/java/com/example"
    )

    RunCoverage(
      "${tempFolder.root}",
      filePathList,
      ReportFormat.MARKDOWN,
      longCommandExecutor,
      scriptBgDispatcher
    ).execute()

    val outputReportText = File(
      "${tempFolder.root}" +
        "$coverageDir/CoverageReport.md"
    ).readText().trimEnd()

    val expectedResult = getExpectedMarkdownText(filePathList.get(0))

    assertThat(outputReportText).isEqualTo(expectedResult)
  }

  @Test
  fun testRunCoverage_withFailureFiles_generatesFinalCoverageReport() {
    val oppiaDevelopGitHubLink = "https://github.com/oppia/oppia-android/tree/develop"
    val filePathList = listOf(
      "coverage/main/java/com/example/LowTestNums.kt"
    )

    val lowTestSourceContent =
      """
      package com.example
      
      class LowTestNums {
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

    val lowTestTestContent =
      """
      package com.example
      
      import org.junit.Assert.assertEquals
      import org.junit.Test
      
      class LowTestNumsTest {
        @Test
        fun testSumNumbers() {
          assertEquals(1, 1)
        }
      }
      """.trimIndent()

    testBazelWorkspace.initEmptyWorkspace()
    testBazelWorkspace.addSourceAndTestFileWithContent(
      filename = "LowTestNums",
      testFilename = "LowTestNumsTest",
      sourceContent = lowTestSourceContent,
      testContent = lowTestTestContent,
      sourceSubpackage = "coverage/main/java/com/example",
      testSubpackage = "coverage/test/java/com/example"
    )

//    val exception = assertThrows<IllegalStateException>() {
    RunCoverage(
      "${tempFolder.root}",
      filePathList,
      ReportFormat.MARKDOWN,
      longCommandExecutor,
      scriptBgDispatcher
    ).execute()
//    }

    // add once coverage failure exception is thrown
    /*assertThat(exception).hasMessageThat().contains(
      "Coverage Analysis Failed as minimum coverage threshold not met!"
    )*/

    val outputReportText = File(
      "${tempFolder.root}" +
        "$coverageDir/CoverageReport.md"
    ).readText().trimEnd()

    val expectedResult = buildString {
      appendLine("## Coverage Report")
      appendLine()
      appendLine("- Number of files assessed: 1")
      appendLine()
      appendLine("| File | Coverage | Lines Hit | Status | Min Required |")
      appendLine("|------|:--------:|----------:|:------:|:------------:|")
      appendLine(
        "| [${filePathList.get(0).substringAfterLast("/")}]" +
          "($oppiaDevelopGitHubLink/${filePathList.get(0)}) | 0.00% | 0 / 4 | " +
          ":x: | $MIN_THRESHOLD% |"
      )
    }.trimEnd()

    assertThat(outputReportText).isEqualTo(expectedResult)
  }

  @Test
  fun testRunCoverage_withSuccessAndFailureFiles_generatesFinalCoverageReport() {
    val oppiaDevelopGitHubLink = "https://github.com/oppia/oppia-android/tree/develop"
    val filePathList = listOf(
      "coverage/main/java/com/example/AddNums.kt",
      "coverage/main/java/com/example/LowTestNums.kt"
    )

    val lowTestSourceContent =
      """
      package com.example
      
      class LowTestNums {
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

    val lowTestTestContent =
      """
      package com.example
      
      import org.junit.Assert.assertEquals
      import org.junit.Test
      
      class LowTestNumsTest {
        @Test
        fun testSumNumbers() {
          assertEquals(1, 1)
        }
      }
      """.trimIndent()

    testBazelWorkspace.initEmptyWorkspace()

    testBazelWorkspace.addSourceAndTestFileWithContent(
      filename = "AddNums",
      testFilename = "AddNumsTest",
      sourceContent = addSourceContent,
      testContent = addTestContent,
      sourceSubpackage = "coverage/main/java/com/example",
      testSubpackage = "coverage/test/java/com/example"
    )

    testBazelWorkspace.addSourceAndTestFileWithContent(
      filename = "LowTestNums",
      testFilename = "LowTestNumsTest",
      sourceContent = lowTestSourceContent,
      testContent = lowTestTestContent,
      sourceSubpackage = "coverage/main/java/com/example",
      testSubpackage = "coverage/test/java/com/example"
    )

//    val exception = assertThrows<IllegalStateException>() {
    RunCoverage(
      "${tempFolder.root}",
      filePathList,
      ReportFormat.MARKDOWN,
      longCommandExecutor,
      scriptBgDispatcher
    ).execute()
//    }

    /*assertThat(exception).hasMessageThat().contains(
      "Coverage Analysis Failed as minimum coverage threshold not met!"
    )*/

    val outputReportText = File(
      "${tempFolder.root}" +
        "$coverageDir/CoverageReport.md"
    ).readText().trimEnd()

    val expectedResult = buildString {
      appendLine("## Coverage Report")
      appendLine()
      appendLine("- Number of files assessed: 2")
      appendLine()
      appendLine("| File | Coverage | Lines Hit | Status | Min Required |")
      appendLine("|------|:--------:|----------:|:------:|:------------:|")
      appendLine(
        "| [${filePathList.get(1).substringAfterLast("/")}]" +
          "($oppiaDevelopGitHubLink/${filePathList.get(1)}) | 0.00% | 0 / 4 | " +
          ":x: | $MIN_THRESHOLD% |"
      )
      appendLine()
      appendLine("<details>")
      appendLine("<summary>Succeeded Coverages</summary><br>")
      appendLine()
      appendLine("| File | Coverage | Lines Hit | Status | Min Required |")
      appendLine("|------|:--------:|----------:|:------:|:------------:|")
      appendLine(
        "| [${filePathList.get(0).substringAfterLast("/")}]" +
          "($oppiaDevelopGitHubLink/${filePathList.get(0)}) | 75.00% | 3 / 4 | " +
          ":white_check_mark: | $MIN_THRESHOLD% |"
      )
      appendLine("</details>")
    }.trimEnd()

    assertThat(outputReportText).isEqualTo(expectedResult)
  }

  @Test
  fun testRunCoverage_withSuccessAndAnomalyFiles_generatesFinalCoverageReport() {
    val oppiaDevelopGitHubLink = "https://github.com/oppia/oppia-android/tree/develop"
    val filePathList = listOf(
      "coverage/main/java/com/example/AddNums.kt",
      "app/src/main/java/org/oppia/android/app/activity/ActivityComponent.kt"
    )

    testBazelWorkspace.initEmptyWorkspace()
    testBazelWorkspace.addSourceAndTestFileWithContent(
      filename = "AddNums",
      testFilename = "AddNumsTest",
      sourceContent = addSourceContent,
      testContent = addTestContent,
      sourceSubpackage = "coverage/main/java/com/example",
      testSubpackage = "coverage/test/java/com/example"
    )

    RunCoverage(
      "${tempFolder.root}",
      filePathList,
      ReportFormat.MARKDOWN,
      longCommandExecutor,
      scriptBgDispatcher
    ).execute()

    val outputReportText = File(
      "${tempFolder.root}" +
        "$coverageDir/CoverageReport.md"
    ).readText()

    val expectedResult = buildString {
      appendLine("## Coverage Report")
      appendLine()
      appendLine("- Number of files assessed: 2")
      appendLine()
      appendLine("<details>")
      appendLine("<summary>Succeeded Coverages</summary><br>")
      appendLine()
      appendLine("| File | Coverage | Lines Hit | Status | Min Required |")
      appendLine("|------|:--------:|----------:|:------:|:------------:|")
      appendLine(
        "| [${filePathList.get(0).substringAfterLast("/")}]" +
          "($oppiaDevelopGitHubLink/${filePathList.get(0)}) | 75.00% | 3 / 4 | " +
          ":white_check_mark: | $MIN_THRESHOLD% |"
      )
      appendLine("</details>")
      appendLine()
      appendLine("### Test File Exempted Cases")
      appendLine(
        "- [${filePathList.get(1).substringAfterLast("/")}]" +
          "($oppiaDevelopGitHubLink/${filePathList.get(1)})"
      )
    }.trimEnd()

    assertThat(outputReportText).isEqualTo(expectedResult)
  }

  @Test
  fun testRunCoverage_withFailureAndAnomalyFiles_generatesFinalCoverageReport() {
    val oppiaDevelopGitHubLink = "https://github.com/oppia/oppia-android/tree/develop"
    val filePathList = listOf(
      "coverage/main/java/com/example/LowTestNums.kt",
      "app/src/main/java/org/oppia/android/app/activity/ActivityComponent.kt"
    )

    val lowTestSourceContent =
      """
      package com.example
      
      class LowTestNums {
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

    val lowTestTestContent =
      """
      package com.example
      
      import org.junit.Assert.assertEquals
      import org.junit.Test
      
      class LowTestNumsTest {
        @Test
        fun testSumNumbers() {
          assertEquals(1, 1)
        }
      }
      """.trimIndent()

    testBazelWorkspace.initEmptyWorkspace()

    testBazelWorkspace.addSourceAndTestFileWithContent(
      filename = "LowTestNums",
      testFilename = "LowTestNumsTest",
      sourceContent = lowTestSourceContent,
      testContent = lowTestTestContent,
      sourceSubpackage = "coverage/main/java/com/example",
      testSubpackage = "coverage/test/java/com/example"
    )

//    val exception = assertThrows<IllegalStateException>() {
    RunCoverage(
      "${tempFolder.root}",
      filePathList,
      ReportFormat.MARKDOWN,
      longCommandExecutor,
      scriptBgDispatcher
    ).execute()
//    }

    /*assertThat(exception).hasMessageThat().contains(
      "Coverage Analysis Failed as minimum coverage threshold not met!"
    )*/

    val outputReportText = File(
      "${tempFolder.root}" +
        "$coverageDir/CoverageReport.md"
    ).readText()

    val expectedResult = buildString {
      appendLine("## Coverage Report")
      appendLine()
      appendLine("- Number of files assessed: 2")
      appendLine()
      appendLine("| File | Coverage | Lines Hit | Status | Min Required |")
      appendLine("|------|:--------:|----------:|:------:|:------------:|")
      appendLine(
        "| [${filePathList.get(0).substringAfterLast("/")}]" +
          "($oppiaDevelopGitHubLink/${filePathList.get(0)}) | 0.00% | 0 / 4 | " +
          ":x: | $MIN_THRESHOLD% |"
      )
      appendLine()
      appendLine("### Test File Exempted Cases")
      appendLine(
        "- [${filePathList.get(1).substringAfterLast("/")}]" +
          "($oppiaDevelopGitHubLink/${filePathList.get(1)})"
      )
    }.trimEnd()

    assertThat(outputReportText).isEqualTo(expectedResult)
  }

  @Test
  fun testRunCoverage_withSuccessFailureAndAnomalyFiles_generatesFinalCoverageReport() {
    val oppiaDevelopGitHubLink = "https://github.com/oppia/oppia-android/tree/develop"
    val filePathList = listOf(
      "coverage/main/java/com/example/AddNums.kt",
      "coverage/main/java/com/example/LowTestNums.kt",
      "app/src/main/java/org/oppia/android/app/activity/ActivityComponent.kt"
    )

    val lowTestSourceContent =
      """
      package com.example
      
      class LowTestNums {
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

    val lowTestTestContent =
      """
      package com.example
      
      import org.junit.Assert.assertEquals
      import org.junit.Test
      
      class LowTestNumsTest {
        @Test
        fun testSumNumbers() {
          assertEquals(1, 1)
        }
      }
      """.trimIndent()

    testBazelWorkspace.initEmptyWorkspace()

    testBazelWorkspace.addSourceAndTestFileWithContent(
      filename = "AddNums",
      testFilename = "AddNumsTest",
      sourceContent = addSourceContent,
      testContent = addTestContent,
      sourceSubpackage = "coverage/main/java/com/example",
      testSubpackage = "coverage/test/java/com/example"
    )

    testBazelWorkspace.addSourceAndTestFileWithContent(
      filename = "LowTestNums",
      testFilename = "LowTestNumsTest",
      sourceContent = lowTestSourceContent,
      testContent = lowTestTestContent,
      sourceSubpackage = "coverage/main/java/com/example",
      testSubpackage = "coverage/test/java/com/example"
    )

//    val exception = assertThrows<IllegalStateException>() {
    RunCoverage(
      "${tempFolder.root}",
      filePathList,
      ReportFormat.MARKDOWN,
      longCommandExecutor,
      scriptBgDispatcher
    ).execute()
//    }

    /*assertThat(exception).hasMessageThat().contains(
      "Coverage Analysis Failed as minimum coverage threshold not met!"
    )*/

    val outputReportText = File(
      "${tempFolder.root}" +
        "$coverageDir/CoverageReport.md"
    ).readText()

    val expectedResult = buildString {
      appendLine("## Coverage Report")
      appendLine()
      appendLine("- Number of files assessed: 3")
      appendLine()
      appendLine("| File | Coverage | Lines Hit | Status | Min Required |")
      appendLine("|------|:--------:|----------:|:------:|:------------:|")
      appendLine(
        "| [${filePathList.get(1).substringAfterLast("/")}]" +
          "($oppiaDevelopGitHubLink/${filePathList.get(1)}) | 0.00% | 0 / 4 | " +
          ":x: | $MIN_THRESHOLD% |"
      )
      appendLine()
      appendLine("<details>")
      appendLine("<summary>Succeeded Coverages</summary><br>")
      appendLine()
      appendLine("| File | Coverage | Lines Hit | Status | Min Required |")
      appendLine("|------|:--------:|----------:|:------:|:------------:|")
      appendLine(
        "| [${filePathList.get(0).substringAfterLast("/")}]" +
          "($oppiaDevelopGitHubLink/${filePathList.get(0)}) | 75.00% | 3 / 4 | " +
          ":white_check_mark: | $MIN_THRESHOLD% |"
      )
      appendLine("</details>")
      appendLine()
      appendLine("### Test File Exempted Cases")
      appendLine(
        "- [${filePathList.get(2).substringAfterLast("/")}]" +
          "($oppiaDevelopGitHubLink/${filePathList.get(2)})"
      )
    }.trimEnd()

    assertThat(outputReportText).isEqualTo(expectedResult)
  }

  @Test
  fun testRunCoverage_scriptTestsMarkdownFormat_generatesCoverageReport() {
    val filePathList = listOf("scripts/java/com/example/AddNums.kt")

    testBazelWorkspace.initEmptyWorkspace()
    testBazelWorkspace.addSourceAndTestFileWithContent(
      filename = "AddNums",
      testFilename = "AddNumsTest",
      sourceContent = addSourceContent,
      testContent = addTestContent,
      sourceSubpackage = "scripts/java/com/example",
      testSubpackage = "scripts/javatests/com/example"
    )

    RunCoverage(
      "${tempFolder.root}",
      filePathList,
      ReportFormat.MARKDOWN,
      longCommandExecutor,
      scriptBgDispatcher
    ).execute()

    val outputReportText = File(
      "${tempFolder.root}" +
        "$coverageDir/CoverageReport.md"
    ).readText().trimEnd()

    val expectedResult = getExpectedMarkdownText(filePathList.get(0))

    assertThat(outputReportText).isEqualTo(expectedResult)
  }

  @Test
  fun testRunCoverage_appTestsMarkdownFormat_generatesCoverageReport() {
    val filePathList = listOf("app/main/java/com/example/AddNums.kt")

    testBazelWorkspace.initEmptyWorkspace()
    testBazelWorkspace.addSourceAndTestFileWithContent(
      filename = "AddNums",
      testFilename = "AddNumsTest",
      sourceContent = addSourceContent,
      testContent = addTestContent,
      sourceSubpackage = "app/main/java/com/example",
      testSubpackage = "app/test/java/com/example"
    )

    RunCoverage(
      "${tempFolder.root}",
      filePathList,
      ReportFormat.MARKDOWN,
      longCommandExecutor,
      scriptBgDispatcher
    ).execute()

    val outputReportText = File(
      "${tempFolder.root}" +
        "$coverageDir/CoverageReport.md"
    ).readText().trimEnd()

    val expectedResult = getExpectedMarkdownText(filePathList.get(0))

    assertThat(outputReportText).isEqualTo(expectedResult)
  }

  @Test
  fun testRunCoverage_localTestsMarkdownFormat_generatesCoverageReport() {
    val filePathList = listOf("app/main/java/com/example/AddNums.kt")

    testBazelWorkspace.initEmptyWorkspace()
    val addTestContentLocal =
      """
      package com.example
      
      import org.junit.Assert.assertEquals
      import org.junit.Test
      
      class AddNumsLocalTest {
      
          @Test
          fun testSumNumbers() {
              assertEquals(AddNums.sumNumbers(0, 1), 1)
              assertEquals(AddNums.sumNumbers(3, 4), 7)         
              assertEquals(AddNums.sumNumbers(0, 0), "Both numbers are zero")
          }
      }
      """.trimIndent()

    testBazelWorkspace.addSourceAndTestFileWithContent(
      filename = "AddNums",
      testFilename = "AddNumsLocalTest",
      sourceContent = addSourceContent,
      testContent = addTestContentLocal,
      sourceSubpackage = "app/main/java/com/example",
      testSubpackage = "app/test/java/com/example"
    )

    RunCoverage(
      "${tempFolder.root}",
      filePathList,
      ReportFormat.MARKDOWN,
      longCommandExecutor,
      scriptBgDispatcher
    ).execute()

    val outputReportText = File(
      "${tempFolder.root}" +
        "$coverageDir/CoverageReport.md"
    ).readText().trimEnd()

    val expectedResult = getExpectedMarkdownText(filePathList.get(0))

    assertThat(outputReportText).isEqualTo(expectedResult)
  }

  @Test
  fun testRunCoverage_sharedTestsMarkdownFormat_generatesCoverageReport() {
    val filePathList = listOf("app/main/java/com/example/AddNums.kt")

    testBazelWorkspace.initEmptyWorkspace()
    testBazelWorkspace.addSourceAndTestFileWithContent(
      filename = "AddNums",
      testFilename = "AddNumsTest",
      sourceContent = addSourceContent,
      testContent = addTestContent,
      sourceSubpackage = "app/main/java/com/example",
      testSubpackage = "app/sharedTest/java/com/example"
    )

    RunCoverage(
      "${tempFolder.root}",
      filePathList,
      ReportFormat.MARKDOWN,
      longCommandExecutor,
      scriptBgDispatcher
    ).execute()

    val outputReportText = File(
      "${tempFolder.root}" +
        "$coverageDir/CoverageReport.md"
    ).readText().trimEnd()

    val expectedResult = getExpectedMarkdownText(filePathList.get(0))

    assertThat(outputReportText).isEqualTo(expectedResult)
  }

  @Test
  fun testRunCoverage_sharedAndLocalTestsMarkdownFormat_generatesCoverageReport() {
    val oppiaDevelopGitHubLink = "https://github.com/oppia/oppia-android/tree/develop"
    val filePathList = listOf("app/main/java/com/example/AddNums.kt")

    testBazelWorkspace.initEmptyWorkspace()

    val addTestContentShared =
      """
      package com.example
      
      import org.junit.Assert.assertEquals
      import org.junit.Test
      
      class AddNumsTest {
      
          @Test
          fun testSumNumbers() {
              assertEquals(AddNums.sumNumbers(0, 1), 1)       
          }
      }
      """.trimIndent()

    val addTestContentLocal =
      """
      package com.example
      
      import org.junit.Assert.assertEquals
      import org.junit.Test
      
      class AddNumsLocalTest {
      
          @Test
          fun testSumNumbers() {
              assertEquals(AddNums.sumNumbers(0, 1), 1)
              assertEquals(AddNums.sumNumbers(3, 4), 7)         
          }
      }
      """.trimIndent()

    testBazelWorkspace.addMultiLevelSourceAndTestFileWithContent(
      filename = "AddNums",
      sourceContent = addSourceContent,
      testContentShared = addTestContentShared,
      testContentLocal = addTestContentLocal,
      subpackage = "app"
    )

    RunCoverage(
      "${tempFolder.root}",
      filePathList,
      ReportFormat.MARKDOWN,
      longCommandExecutor,
      scriptBgDispatcher
    ).execute()

    val outputReportText = File(
      "${tempFolder.root}" +
        "$coverageDir/CoverageReport.md"
    ).readText().trimEnd()

    val expectedResult = buildString {
      appendLine("## Coverage Report")
      appendLine()
      appendLine("- Number of files assessed: 1")
      appendLine()
      appendLine("<details>")
      appendLine("<summary>Succeeded Coverages</summary><br>")
      appendLine()
      appendLine("| File | Coverage | Lines Hit | Status | Min Required |")
      appendLine("|------|:--------:|----------:|:------:|:------------:|")
      appendLine(
        "| [${filePathList.get(0).substringAfterLast("/")}]" +
          "($oppiaDevelopGitHubLink/${filePathList.get(0)}) | 50.00% | 2 / 4 | " +
          ":white_check_mark: | $MIN_THRESHOLD% |"
      )
      appendLine("</details>")
    }.trimEnd()

    assertThat(outputReportText).isEqualTo(expectedResult)
  }

  @Test
  fun testRunCoverage_withMultipleFilesHtmlFormat_generatesCoverageReport() {
    val filePathList = listOf(
      "coverage/main/java/com/example/AddNums.kt",
      "coverage/main/java/com/example/SubNums.kt"
    )

    val subSourceContent =
      """
      package com.example
      
      class SubNums {
        companion object {
          fun subNumbers(a: Int, b: Int): Any {
            return if (a == 0 && b == 0) {
                "Both numbers are zero"
            } else {
                a - b
            }
          }
        }
      }
      """.trimIndent()

    val subTestContent =
      """
      package com.example
      
      import org.junit.Assert.assertEquals
      import org.junit.Test
      
      class SubNumsTest {
        @Test
        fun testSubNumbers() {
          assertEquals(SubNums.subNumbers(1, 0), 1)
          assertEquals(SubNums.subNumbers(4, 3), 1)         
          assertEquals(SubNums.subNumbers(0, 0), "Both numbers are zero")
        }
      }
      """.trimIndent()

    testBazelWorkspace.initEmptyWorkspace()

    testBazelWorkspace.addSourceAndTestFileWithContent(
      filename = "AddNums",
      testFilename = "AddNumsTest",
      sourceContent = addSourceContent,
      testContent = addTestContent,
      sourceSubpackage = "coverage/main/java/com/example",
      testSubpackage = "coverage/test/java/com/example"
    )

    testBazelWorkspace.addSourceAndTestFileWithContent(
      filename = "SubNums",
      testFilename = "SubNumsTest",
      sourceContent = subSourceContent,
      testContent = subTestContent,
      sourceSubpackage = "coverage/main/java/com/example",
      testSubpackage = "coverage/test/java/com/example"
    )

    RunCoverage(
      "${tempFolder.root}",
      filePathList,
      ReportFormat.HTML,
      longCommandExecutor,
      scriptBgDispatcher
    ).execute()

    for (file in filePathList) {
      val outputReportText = File(
        "${tempFolder.root}" +
          "$coverageDir/${file.removeSuffix(".kt")}/coverage.html"
      ).readText()
      val expectedResult = getExpectedHtmlText(file)

      assertThat(outputReportText).isEqualTo(expectedResult)
    }
  }

  @Test
  fun testRunCoverage_sampleTestsHtmlFormat_generatesCoverageReport() {
    val filePathList = listOf("coverage/main/java/com/example/AddNums.kt")

    testBazelWorkspace.initEmptyWorkspace()
    testBazelWorkspace.addSourceAndTestFileWithContent(
      filename = "AddNums",
      testFilename = "AddNumsTest",
      sourceContent = addSourceContent,
      testContent = addTestContent,
      sourceSubpackage = "coverage/main/java/com/example",
      testSubpackage = "coverage/test/java/com/example"
    )

    RunCoverage(
      "${tempFolder.root}",
      filePathList,
      ReportFormat.HTML,
      longCommandExecutor,
      scriptBgDispatcher
    ).execute()

    val outputReportText = File(
      "${tempFolder.root}" +
        "$coverageDir/${filePathList.get(0).removeSuffix(".kt")}/coverage.html"
    ).readText()

    val expectedResult = getExpectedHtmlText(filePathList.get(0))

    assertThat(outputReportText).isEqualTo(expectedResult)
  }

  @Test
  fun testRunCoverage_scriptTestsHtmlFormat_generatesCoverageReport() {
    val filePathList = listOf("scripts/java/com/example/AddNums.kt")

    testBazelWorkspace.initEmptyWorkspace()
    testBazelWorkspace.addSourceAndTestFileWithContent(
      filename = "AddNums",
      testFilename = "AddNumsTest",
      sourceContent = addSourceContent,
      testContent = addTestContent,
      sourceSubpackage = "scripts/java/com/example",
      testSubpackage = "scripts/javatests/com/example"
    )

    RunCoverage(
      "${tempFolder.root}",
      filePathList,
      ReportFormat.HTML,
      longCommandExecutor,
      scriptBgDispatcher
    ).execute()

    val outputReportText = File(
      "${tempFolder.root}" +
        "$coverageDir/${filePathList.get(0).removeSuffix(".kt")}/coverage.html"
    ).readText()

    val expectedResult = getExpectedHtmlText(filePathList.get(0))

    assertThat(outputReportText).isEqualTo(expectedResult)
  }

  @Test
  fun testRunCoverage_appTestsHtmlFormat_generatesCoverageReport() {
    val filePathList = listOf("app/main/java/com/example/AddNums.kt")

    testBazelWorkspace.initEmptyWorkspace()
    testBazelWorkspace.addSourceAndTestFileWithContent(
      filename = "AddNums",
      testFilename = "AddNumsTest",
      sourceContent = addSourceContent,
      testContent = addTestContent,
      sourceSubpackage = "app/main/java/com/example",
      testSubpackage = "app/test/java/com/example"
    )

    RunCoverage(
      "${tempFolder.root}",
      filePathList,
      ReportFormat.HTML,
      longCommandExecutor,
      scriptBgDispatcher
    ).execute()

    val outputReportText = File(
      "${tempFolder.root}" +
        "$coverageDir/${filePathList.get(0).removeSuffix(".kt")}/coverage.html"
    ).readText()

    val expectedResult = getExpectedHtmlText(filePathList.get(0))

    assertThat(outputReportText).isEqualTo(expectedResult)
  }

  @Test
  fun testRunCoverage_localTestsHtmlFormat_generatesCoverageReport() {
    val filePathList = listOf("app/main/java/com/example/AddNums.kt")

    testBazelWorkspace.initEmptyWorkspace()
    val addTestContentLocal =
      """
      package com.example
      
      import org.junit.Assert.assertEquals
      import org.junit.Test
      
      class AddNumsLocalTest {
      
          @Test
          fun testSumNumbers() {
              assertEquals(AddNums.sumNumbers(0, 1), 1)
              assertEquals(AddNums.sumNumbers(3, 4), 7)         
              assertEquals(AddNums.sumNumbers(0, 0), "Both numbers are zero")
          }
      }
      """.trimIndent()

    testBazelWorkspace.addSourceAndTestFileWithContent(
      filename = "AddNums",
      testFilename = "AddNumsLocalTest",
      sourceContent = addSourceContent,
      testContent = addTestContentLocal,
      sourceSubpackage = "app/main/java/com/example",
      testSubpackage = "app/test/java/com/example"
    )

    RunCoverage(
      "${tempFolder.root}",
      filePathList,
      ReportFormat.HTML,
      longCommandExecutor,
      scriptBgDispatcher
    ).execute()

    val outputReportText = File(
      "${tempFolder.root}" +
        "$coverageDir/${filePathList.get(0).removeSuffix(".kt")}/coverage.html"
    ).readText()

    val expectedResult = getExpectedHtmlText(filePathList.get(0))

    assertThat(outputReportText).isEqualTo(expectedResult)
  }

  @Test
  fun testRunCoverage_sharedTestsHtmlFormat_generatesCoverageReport() {
    val filePathList = listOf("app/main/java/com/example/AddNums.kt")

    testBazelWorkspace.initEmptyWorkspace()
    testBazelWorkspace.addSourceAndTestFileWithContent(
      filename = "AddNums",
      testFilename = "AddNumsTest",
      sourceContent = addSourceContent,
      testContent = addTestContent,
      sourceSubpackage = "app/main/java/com/example",
      testSubpackage = "app/sharedTest/java/com/example"
    )

    RunCoverage(
      "${tempFolder.root}",
      filePathList,
      ReportFormat.HTML,
      longCommandExecutor,
      scriptBgDispatcher
    ).execute()

    val outputReportText = File(
      "${tempFolder.root}" +
        "$coverageDir/${filePathList.get(0).removeSuffix(".kt")}/coverage.html"
    ).readText()

    val expectedResult = getExpectedHtmlText(filePathList.get(0))

    assertThat(outputReportText).isEqualTo(expectedResult)
  }

  @Test
  fun testRunCoverage_sharedAndLocalTestsHtmlFormat_generatesCoverageReport() {
    val filePathList = listOf("app/main/java/com/example/AddNums.kt")

    testBazelWorkspace.initEmptyWorkspace()

    val addTestContentShared =
      """
      package com.example
      
      import org.junit.Assert.assertEquals
      import org.junit.Test
      
      class AddNumsTest {
      
          @Test
          fun testSumNumbers() {
              assertEquals(AddNums.sumNumbers(0, 1), 1)       
          }
      }
      """.trimIndent()

    val addTestContentLocal =
      """
      package com.example
      
      import org.junit.Assert.assertEquals
      import org.junit.Test
      
      class AddNumsLocalTest {
      
          @Test
          fun testSumNumbers() {
              assertEquals(AddNums.sumNumbers(0, 1), 1)
              assertEquals(AddNums.sumNumbers(3, 4), 7)         
          }
      }
      """.trimIndent()

    testBazelWorkspace.addMultiLevelSourceAndTestFileWithContent(
      filename = "AddNums",
      sourceContent = addSourceContent,
      testContentShared = addTestContentShared,
      testContentLocal = addTestContentLocal,
      subpackage = "app"
    )

    RunCoverage(
      "${tempFolder.root}",
      filePathList,
      ReportFormat.HTML,
      longCommandExecutor,
      scriptBgDispatcher
    ).execute()

    val outputReportText = File(
      "${tempFolder.root}" +
        "$coverageDir/${filePathList.get(0).removeSuffix(".kt")}/coverage.html"
    ).readText()

    val expectedResult =
      """
    <!DOCTYPE html>
    <html lang="en">
    <head>
      <meta charset="UTF-8">
      <meta name="viewport" content="width=device-width, initial-scale=1.0">
      <title>Coverage Report</title>
      <style>
        body {
          font-family: Arial, sans-serif;
          font-size: 12px;
          line-height: 1.6;
          padding: 20px;
        }
        table {
          width: 100%;
          border-collapse: collapse;
          margin-bottom: 20px;
        }
        th, td {
          padding: 8px;
          text-align: left;
          white-space: pre-wrap;
          border-bottom: 1px solid #e3e3e3;
        }
        .line-number-col {
          width: 4%;
        }
        .source-code-col {
          width: 96%;
        }
        .covered-line, .not-covered-line, .uncovered-line {
          white-space: pre-wrap;
        }
        .covered-line {
          background-color: #c8e6c9; /* Light green */
        }
        .not-covered-line {
          background-color: #ffcdd2; /* Light red */
        }
        .uncovered-line {
          background-color: #f7f7f7; /* light gray */
        }
        .coverage-summary {
          margin-bottom: 20px;
        }
        h2 {
          text-align: center;
        }
        ul {
          list-style-type: none;
          padding: 0;
          text-align: center;
        }
        .summary-box {
          border: 1px solid #ccc;
          border-radius: 8px;
          padding: 10px;
          margin-bottom: 20px;
          display: flex;
          justify-content: space-between;
          align-items: flex-start;
        }
        .summary-left {
          text-align: left;
        }
        .summary-right {
          text-align: right;
        }
        .legend {
          display: flex;
          align-items: center;
        }
        .legend-item {
          width: 20px;
          height: 10px;
          margin-right: 5px;
          border-radius: 2px;
          display: inline-block;
        }
        .legend .covered {
          background-color: #c8e6c9; /* Light green */
        }
        .legend .not-covered {
          margin-left: 4px;
          background-color: #ffcdd2; /* Light red */
        }
        @media screen and (max-width: 768px) {
          body {
            padding: 10px;
          }
          table {
            width: auto;
          }
        }
      </style>
    </head>
    <body>
      <h2>Coverage Report</h2>
      <div class="summary-box">
        <div class="summary-left">
          <strong>Covered File:</strong> ${filePathList.get(0)} <br>
          <div class="legend">
            <div class="legend-item covered"></div>
            <span>Covered</span>
            <div class="legend-item not-covered"></div>
            <span>Uncovered</span>
          </div>
        </div>
        <div class="summary-right">
          <div><strong>Coverage percentage:</strong> 50.00%</div>
          <div><strong>Line coverage:</strong> 2 / 4 covered</div>
        </div>
      </div>
      <table>
        <thead>
          <tr>
            <th class="line-number-col">Line No</th>
            <th class="source-code-col">Source Code</th>
          </tr>
        </thead>
        <tbody><tr>
      <td class="line-number-row">   1</td>
      <td class="uncovered-line">package com.example</td>
    </tr><tr>
      <td class="line-number-row">   2</td>
      <td class="uncovered-line"></td>
    </tr><tr>
      <td class="line-number-row">   3</td>
      <td class="not-covered-line">class AddNums {</td>
    </tr><tr>
      <td class="line-number-row">   4</td>
      <td class="uncovered-line">  companion object {</td>
    </tr><tr>
      <td class="line-number-row">   5</td>
      <td class="uncovered-line">    fun sumNumbers(a: Int, b: Int): Any {</td>
    </tr><tr>
      <td class="line-number-row">   6</td>
      <td class="covered-line">      return if (a == 0 && b == 0) {</td>
    </tr><tr>
      <td class="line-number-row">   7</td>
      <td class="not-covered-line">          "Both numbers are zero"</td>
    </tr><tr>
      <td class="line-number-row">   8</td>
      <td class="uncovered-line">      } else {</td>
    </tr><tr>
      <td class="line-number-row">   9</td>
      <td class="covered-line">          a + b</td>
    </tr><tr>
      <td class="line-number-row">  10</td>
      <td class="uncovered-line">      }</td>
    </tr><tr>
      <td class="line-number-row">  11</td>
      <td class="uncovered-line">    }</td>
    </tr><tr>
      <td class="line-number-row">  12</td>
      <td class="uncovered-line">  }</td>
    </tr><tr>
      <td class="line-number-row">  13</td>
      <td class="uncovered-line">}</td>
    </tr>    </tbody>
      </table>
    </body>
    </html>
      """.trimIndent()

    assertThat(outputReportText).isEqualTo(expectedResult)
  }

  private fun getExpectedMarkdownText(filePath: String): String {
    val oppiaDevelopGitHubLink = "https://github.com/oppia/oppia-android/tree/develop"
    val filename = filePath.substringAfterLast("/")

    val markdownText = buildString {
      appendLine("## Coverage Report")
      appendLine()
      appendLine("- Number of files assessed: 1")
      appendLine()
      appendLine("<details>")
      appendLine("<summary>Succeeded Coverages</summary><br>")
      appendLine()
      appendLine("| File | Coverage | Lines Hit | Status | Min Required |")
      appendLine("|------|:--------:|----------:|:------:|:------------:|")
      appendLine(
        "| [$filename]($oppiaDevelopGitHubLink/$filePath) | 75.00% | " +
          "3 / 4 | :white_check_mark: | $MIN_THRESHOLD% |"
      )
      appendLine("</details>")
    }.trimEnd()

    return markdownText
  }

  private fun getExpectedHtmlText(filePath: String): String {
    val htmlText =
      """
    <!DOCTYPE html>
    <html lang="en">
    <head>
      <meta charset="UTF-8">
      <meta name="viewport" content="width=device-width, initial-scale=1.0">
      <title>Coverage Report</title>
      <style>
        body {
          font-family: Arial, sans-serif;
          font-size: 12px;
          line-height: 1.6;
          padding: 20px;
        }
        table {
          width: 100%;
          border-collapse: collapse;
          margin-bottom: 20px;
        }
        th, td {
          padding: 8px;
          text-align: left;
          white-space: pre-wrap;
          border-bottom: 1px solid #e3e3e3;
        }
        .line-number-col {
          width: 4%;
        }
        .source-code-col {
          width: 96%;
        }
        .covered-line, .not-covered-line, .uncovered-line {
          white-space: pre-wrap;
        }
        .covered-line {
          background-color: #c8e6c9; /* Light green */
        }
        .not-covered-line {
          background-color: #ffcdd2; /* Light red */
        }
        .uncovered-line {
          background-color: #f7f7f7; /* light gray */
        }
        .coverage-summary {
          margin-bottom: 20px;
        }
        h2 {
          text-align: center;
        }
        ul {
          list-style-type: none;
          padding: 0;
          text-align: center;
        }
        .summary-box {
          border: 1px solid #ccc;
          border-radius: 8px;
          padding: 10px;
          margin-bottom: 20px;
          display: flex;
          justify-content: space-between;
          align-items: flex-start;
        }
        .summary-left {
          text-align: left;
        }
        .summary-right {
          text-align: right;
        }
        .legend {
          display: flex;
          align-items: center;
        }
        .legend-item {
          width: 20px;
          height: 10px;
          margin-right: 5px;
          border-radius: 2px;
          display: inline-block;
        }
        .legend .covered {
          background-color: #c8e6c9; /* Light green */
        }
        .legend .not-covered {
          margin-left: 4px;
          background-color: #ffcdd2; /* Light red */
        }
        @media screen and (max-width: 768px) {
          body {
            padding: 10px;
          }
          table {
            width: auto;
          }
        }
      </style>
    </head>
    <body>
      <h2>Coverage Report</h2>
      <div class="summary-box">
        <div class="summary-left">
          <strong>Covered File:</strong> $filePath <br>
          <div class="legend">
            <div class="legend-item covered"></div>
            <span>Covered</span>
            <div class="legend-item not-covered"></div>
            <span>Uncovered</span>
          </div>
        </div>
        <div class="summary-right">
          <div><strong>Coverage percentage:</strong> 75.00%</div>
          <div><strong>Line coverage:</strong> 3 / 4 covered</div>
        </div>
      </div>
      <table>
        <thead>
          <tr>
            <th class="line-number-col">Line No</th>
            <th class="source-code-col">Source Code</th>
          </tr>
        </thead>
        <tbody><tr>
      <td class="line-number-row">   1</td>
      <td class="uncovered-line">package com.example</td>
    </tr><tr>
      <td class="line-number-row">   2</td>
      <td class="uncovered-line"></td>
    </tr><tr>
      <td class="line-number-row">   3</td>
      <td class="not-covered-line">class ${getExpectedClassName(filePath)} {</td>
    </tr><tr>
      <td class="line-number-row">   4</td>
      <td class="uncovered-line">  companion object {</td>
    </tr><tr>
      <td class="line-number-row">   5</td>
      <td class="uncovered-line">    fun ${getExpectedFuncName(filePath)}(a: Int, b: Int): Any {</td>
    </tr><tr>
      <td class="line-number-row">   6</td>
      <td class="covered-line">      return if (a == 0 && b == 0) {</td>
    </tr><tr>
      <td class="line-number-row">   7</td>
      <td class="covered-line">          "Both numbers are zero"</td>
    </tr><tr>
      <td class="line-number-row">   8</td>
      <td class="uncovered-line">      } else {</td>
    </tr><tr>
      <td class="line-number-row">   9</td>
      <td class="covered-line">          ${getExpectedLogic(filePath)}</td>
    </tr><tr>
      <td class="line-number-row">  10</td>
      <td class="uncovered-line">      }</td>
    </tr><tr>
      <td class="line-number-row">  11</td>
      <td class="uncovered-line">    }</td>
    </tr><tr>
      <td class="line-number-row">  12</td>
      <td class="uncovered-line">  }</td>
    </tr><tr>
      <td class="line-number-row">  13</td>
      <td class="uncovered-line">}</td>
    </tr>    </tbody>
      </table>
    </body>
    </html>
      """.trimIndent()

    return htmlText
  }

  private fun getExpectedClassName(filePath: String): String {
    return filePath.substringAfterLast("/").removeSuffix(".kt")
  }

  private fun getExpectedFuncName(filePath: String): String {
    when {
      filePath.endsWith("AddNums.kt") -> return "sumNumbers"
      filePath.endsWith("SubNums.kt") -> return "subNumbers"
      else -> return ""
    }
  }

  private fun getExpectedLogic(filePath: String): String {
    when {
      filePath.endsWith("AddNums.kt") -> return "a + b"
      filePath.endsWith("SubNums.kt") -> return "a - b"
      else -> return ""
    }
  }

  private fun initializeCommandExecutorWithLongProcessWaitTime(): CommandExecutorImpl {
    return CommandExecutorImpl(
      scriptBgDispatcher, processTimeout = 5, processTimeoutUnit = TimeUnit.MINUTES
    )
  }
}
