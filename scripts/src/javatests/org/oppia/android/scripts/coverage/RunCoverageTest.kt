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
import java.io.File
import java.util.concurrent.TimeUnit

/** Tests for [RunCoverage]. */
class RunCoverageTest {
  @field:[Rule JvmField] val tempFolder = TemporaryFolder()

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
  fun testRunCoverage_missingTestFileNotExempted_generatesFailureReport() {
    val oppiaDevelopGitHubLink = "https://github.com/oppia/oppia-android/tree/develop"
    val sampleFile = "file.kt"
    testBazelWorkspace.initEmptyWorkspace()
    tempFolder.newFile(sampleFile)
    val exception = assertThrows<IllegalStateException>() {
      main(
        tempFolder.root.absolutePath,
        sampleFile,
        "--format=Markdown"
      )
    }

    assertThat(exception).hasMessageThat()
      .contains("Coverage Analysis$BOLD$RED FAILED$RESET")

    val outputReportText = File(
      "${tempFolder.root}" +
        "$coverageDir/CoverageReport.md"
    ).readText()

    val failureMessage =
      "No appropriate test file found for $sampleFile"

    val expectedMarkdown = buildString {
      append("## Coverage Report\n\n")
      append("- Number of files assessed: 1\n")
      append("- Coverage Analysis: **FAIL** :x:\n\n")
      append("### Failure Cases\n")
      append("| File | Failure Reason |\n")
      append("|------|----------------|\n")
      append("| [$sampleFile]($oppiaDevelopGitHubLink/$sampleFile) | $failureMessage |")
    }

    assertThat(outputReportText).isEqualTo(expectedMarkdown)
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
    val oppiaDevelopGitHubLink = "https://github.com/oppia/oppia-android/tree/develop"
    val exemptedFile = "app/src/main/java/org/oppia/android/app/activity/ActivityComponent.kt"
    val exemptedFilePathList = listOf(exemptedFile)

    RunCoverage(
      "${tempFolder.root}",
      exemptedFilePathList,
      ReportFormat.MARKDOWN,
      longCommandExecutor,
      scriptBgDispatcher
    ).execute()

    val outputReportText = File(
      "${tempFolder.root}" +
        "$coverageDir/CoverageReport.md"
    ).readText()

    val expectedResult = buildString {
      append("## Coverage Report\n\n")
      append("- Number of files assessed: 1\n")
      append("- Coverage Analysis: **PASS** :white_check_mark:\n\n")
      append("### Test File Exempted Cases\n")
      append(
        "- [${exemptedFilePathList.get(0).substringAfterLast("/")}]" +
          "($oppiaDevelopGitHubLink/${exemptedFilePathList.get(0)})"
      )
    }

    assertThat(outputReportText).isEqualTo(expectedResult)
  }

  @Test
  fun testRunCoverage_withNonKotlinFileInput_analyzeOnlyKotlinFiles() {
    val kotlinFilePath = "coverage/main/java/com/example/AddNums.kt"
    val nonKotlinFilePath1 = "screen.xml"
    val nonKotlinFilePath2 = "coverage.txt"
    val nonKotlinFilePath3 = "report.md"

    tempFolder.newFile("screen.xml")
    tempFolder.newFile("coverage.txt")
    tempFolder.newFile("report.md")

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
      kotlinFilePath,
      nonKotlinFilePath1,
      nonKotlinFilePath2,
      nonKotlinFilePath3
    )

    val outputReportText = File(
      "${tempFolder.root}" +
        "$coverageDir/${kotlinFilePath.removeSuffix(".kt")}/coverage.html"
    ).readText()

    val expectedResult = getExpectedHtmlText(kotlinFilePath)

    assertThat(outputReportText).isEqualTo(expectedResult)
  }

  @Test
  fun testRunCoverage_withTestFileInput_mapsToSourceFileAndGeneratesCoverageReport() {
    val testFilePath = "coverage/test/java/com/example/AddNumsTest.kt"
    val sourceFilePath = testFilePath.replace("/test/", "/main/").replace("Test.kt", ".kt")

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
      testFilePath,
    )

    val outputReportText = File(
      "${tempFolder.root}" +
        "$coverageDir/${sourceFilePath.removeSuffix(".kt")}/coverage.html"
    ).readText()

    val expectedResult = getExpectedHtmlText(sourceFilePath)

    assertThat(outputReportText).isEqualTo(expectedResult)
  }

  @Test
  fun testRunCoverage_withIncorrectPackageStructure_generatesFailureReport() {
    val filePathList = listOf(
      "coverage/example/AddNums.kt",
    )

    testBazelWorkspace.initEmptyWorkspace()
    testBazelWorkspace.addSourceAndTestFileWithContent(
      filename = "AddNums",
      testFilename = "AddNumsTest",
      sourceContent = addSourceContent,
      testContent = addTestContent,
      sourceSubpackage = "coverage/example",
      testSubpackage = "coverage/example"
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

    assertThat(exception).hasMessageThat()
      .contains("Coverage Analysis$BOLD$RED FAILED$RESET")

    val outputReportText = File(
      "${tempFolder.root}" +
        "$coverageDir/CoverageReport.md"
    ).readText()

    val failureMessage = "Coverage retrieval failed for the test target: " +
      "//coverage/example:AddNumsTest"

    val expectedMarkdown = buildString {
      append("## Coverage Report\n\n")
      append("- Number of files assessed: 1\n")
      append("- Coverage Analysis: **FAIL** :x:\n\n")
      append("### Failure Cases\n")
      append("| File | Failure Reason |\n")
      append("|------|----------------|\n")
      append("| //coverage/example:AddNumsTest | $failureMessage |")
    }

    assertThat(outputReportText).isEqualTo(expectedMarkdown)
  }

  @Test
  fun testRunCoverage_withNoDepsToSourceFile_generatesFailureReport() {
    val filePathList = listOf(
      "coverage/main/java/com/example/SubNums.kt",
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

    val subTestFile = tempFolder.newFile("coverage/test/java/com/example/SubNumsTest.kt")
    subTestFile.writeText(
      """
      package com.example
      
      import org.junit.Assert.assertEquals
      import org.junit.Test
      import com.example.AddNums
      
      class SubNumsTest {
      
          @Test
          fun testSubNumbers() {
              assertEquals(AddNums.sumNumbers(0, 1), 1)
              assertEquals(AddNums.sumNumbers(3, 4), 7)         
              assertEquals(AddNums.sumNumbers(0, 0), "Both numbers are zero")
          }
      }
      """.trimIndent()
    )

    val testBuildFile = File(tempFolder.root, "coverage/test/java/com/example/BUILD.bazel")
    testBuildFile.appendText(
      """
      kt_jvm_test(
          name = "SubNumsTest",
          srcs = ["SubNumsTest.kt"],
          deps = [
            "//coverage/main/java/com/example:addnums",
            "@maven//:junit_junit",
          ],
          visibility = ["//visibility:public"],
          test_class = "com.example.SubNumsTest",
      )
      """.trimIndent()
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

    assertThat(exception).hasMessageThat()
      .contains("Coverage Analysis$BOLD$RED FAILED$RESET")

    val outputReportText = File(
      "${tempFolder.root}" +
        "$coverageDir/CoverageReport.md"
    ).readText()

    val failureMessage = "Source File: SubNums.kt not found in the coverage data"

    val expectedMarkdown = buildString {
      append("## Coverage Report\n\n")
      append("- Number of files assessed: 1\n")
      append("- Coverage Analysis: **FAIL** :x:\n\n")
      append("### Failure Cases\n")
      append("| File | Failure Reason |\n")
      append("|------|----------------|\n")
      append("| //coverage/test/java/com/example:SubNumsTest | $failureMessage |")
    }

    assertThat(outputReportText).isEqualTo(expectedMarkdown)
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
    ).readText()

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
      ).readText()

      val expectedResult = buildString {
        append("## Coverage Report\n\n")
        append("- Number of files assessed: 2\n")
        append("- Coverage Analysis: **PASS** :white_check_mark:\n\n")
        append("<details>\n")
        append("<summary>Succeeded Coverages</summary><br>\n\n")
        append("| File | Coverage | Lines Hit | Status | Min Required |\n")
        append("|------|:--------:|----------:|:------:|:------------:|\n")
        append(
          "| [${filePathList.get(0).substringAfterLast("/")}]" +
            "($oppiaDevelopGitHubLink/${filePathList.get(0)}) | 75.00% | 3 / 4 | " +
            ":white_check_mark: | $MIN_THRESHOLD% |\n"
        )
        append(
          "| [${filePathList.get(1).substringAfterLast("/")}]" +
            "($oppiaDevelopGitHubLink/${filePathList.get(1)}) | 50.00% | 2 / 4 | " +
            ":white_check_mark: | $MIN_THRESHOLD% |\n"
        )
        append("</details>")
      }

      assertThat(outputReportText).isEqualTo(expectedResult)
    }
  }

  @Test
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

    assertThat(exception).hasMessageThat()
      .contains("Coverage Analysis$BOLD$RED FAILED$RESET")
  }

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
    ).readText()

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

    val exception = assertThrows<IllegalStateException>() {
      RunCoverage(
        "${tempFolder.root}",
        filePathList,
        ReportFormat.MARKDOWN,
        longCommandExecutor,
        scriptBgDispatcher
      ).execute()
    }

    assertThat(exception).hasMessageThat()
      .contains("Coverage Analysis$BOLD$RED FAILED$RESET")

    val outputReportText = File(
      "${tempFolder.root}" +
        "$coverageDir/CoverageReport.md"
    ).readText()

    val expectedResult = buildString {
      append("## Coverage Report\n\n")
      append("- Number of files assessed: 1\n")
      append("- Coverage Analysis: **FAIL** :x:\n\n")
      append("| File | Coverage | Lines Hit | Status | Min Required |\n")
      append("|------|:--------:|----------:|:------:|:------------:|\n")
      append(
        "| [${filePathList.get(0).substringAfterLast("/")}]" +
          "($oppiaDevelopGitHubLink/${filePathList.get(0)}) | 0.00% | 0 / 4 | " +
          ":x: | $MIN_THRESHOLD% |"
      )
    }

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

    val exception = assertThrows<IllegalStateException>() {
      RunCoverage(
        "${tempFolder.root}",
        filePathList,
        ReportFormat.MARKDOWN,
        longCommandExecutor,
        scriptBgDispatcher
      ).execute()
    }

    assertThat(exception).hasMessageThat()
      .contains("Coverage Analysis$BOLD$RED FAILED$RESET")

    val outputReportText = File(
      "${tempFolder.root}" +
        "$coverageDir/CoverageReport.md"
    ).readText()

    val expectedResult = buildString {
      append("## Coverage Report\n\n")
      append("- Number of files assessed: 2\n")
      append("- Coverage Analysis: **FAIL** :x:\n\n")
      append("| File | Coverage | Lines Hit | Status | Min Required |\n")
      append("|------|:--------:|----------:|:------:|:------------:|\n")
      append(
        "| [${filePathList.get(1).substringAfterLast("/")}]" +
          "($oppiaDevelopGitHubLink/${filePathList.get(1)}) | 0.00% | 0 / 4 | " +
          ":x: | $MIN_THRESHOLD% |\n\n"
      )
      append("<details>\n")
      append("<summary>Succeeded Coverages</summary><br>\n\n")
      append("| File | Coverage | Lines Hit | Status | Min Required |\n")
      append("|------|:--------:|----------:|:------:|:------------:|\n")
      append(
        "| [${filePathList.get(0).substringAfterLast("/")}]" +
          "($oppiaDevelopGitHubLink/${filePathList.get(0)}) | 75.00% | 3 / 4 | " +
          ":white_check_mark: | $MIN_THRESHOLD% |\n"
      )
      append("</details>")
    }

    assertThat(outputReportText).isEqualTo(expectedResult)
  }

  @Test
  fun testRunCoverage_withSuccessAndExemptedFiles_generatesFinalCoverageReport() {
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
      append("## Coverage Report\n\n")
      append("- Number of files assessed: 2\n")
      append("- Coverage Analysis: **PASS** :white_check_mark:\n\n")
      append("<details>\n")
      append("<summary>Succeeded Coverages</summary><br>\n\n")
      append("| File | Coverage | Lines Hit | Status | Min Required |\n")
      append("|------|:--------:|----------:|:------:|:------------:|\n")
      append(
        "| [${filePathList.get(0).substringAfterLast("/")}]" +
          "($oppiaDevelopGitHubLink/${filePathList.get(0)}) | 75.00% | 3 / 4 | " +
          ":white_check_mark: | $MIN_THRESHOLD% |\n"
      )
      append("</details>\n\n")
      append("### Test File Exempted Cases\n")
      append(
        "- [${filePathList.get(1).substringAfterLast("/")}]" +
          "($oppiaDevelopGitHubLink/${filePathList.get(1)})"
      )
    }

    assertThat(outputReportText).isEqualTo(expectedResult)
  }

  @Test
  fun testRunCoverage_withFailureAndExemptedFiles_generatesFinalCoverageReport() {
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

    val exception = assertThrows<IllegalStateException>() {
      RunCoverage(
        "${tempFolder.root}",
        filePathList,
        ReportFormat.MARKDOWN,
        longCommandExecutor,
        scriptBgDispatcher
      ).execute()
    }

    assertThat(exception).hasMessageThat()
      .contains("Coverage Analysis$BOLD$RED FAILED$RESET")

    val outputReportText = File(
      "${tempFolder.root}" +
        "$coverageDir/CoverageReport.md"
    ).readText()

    val expectedResult = buildString {
      append("## Coverage Report\n\n")
      append("- Number of files assessed: 2\n")
      append("- Coverage Analysis: **FAIL** :x:\n\n")
      append("| File | Coverage | Lines Hit | Status | Min Required |\n")
      append("|------|:--------:|----------:|:------:|:------------:|\n")
      append(
        "| [${filePathList.get(0).substringAfterLast("/")}]" +
          "($oppiaDevelopGitHubLink/${filePathList.get(0)}) | 0.00% | 0 / 4 | " +
          ":x: | $MIN_THRESHOLD% |\n\n"
      )
      append("### Test File Exempted Cases\n")
      append(
        "- [${filePathList.get(1).substringAfterLast("/")}]" +
          "($oppiaDevelopGitHubLink/${filePathList.get(1)})"
      )
    }

    assertThat(outputReportText).isEqualTo(expectedResult)
  }

  @Test
  fun testRunCoverage_withSuccessFailureAndExemptedFiles_generatesFinalCoverageReport() {
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

    val exception = assertThrows<IllegalStateException>() {
      RunCoverage(
        "${tempFolder.root}",
        filePathList,
        ReportFormat.MARKDOWN,
        longCommandExecutor,
        scriptBgDispatcher
      ).execute()
    }

    assertThat(exception).hasMessageThat()
      .contains("Coverage Analysis$BOLD$RED FAILED$RESET")

    val outputReportText = File(
      "${tempFolder.root}" +
        "$coverageDir/CoverageReport.md"
    ).readText()

    val expectedResult = buildString {
      append("## Coverage Report\n\n")
      append("- Number of files assessed: 3\n")
      append("- Coverage Analysis: **FAIL** :x:\n\n")
      append("| File | Coverage | Lines Hit | Status | Min Required |\n")
      append("|------|:--------:|----------:|:------:|:------------:|\n")
      append(
        "| [${filePathList.get(1).substringAfterLast("/")}]" +
          "($oppiaDevelopGitHubLink/${filePathList.get(1)}) | 0.00% | 0 / 4 | " +
          ":x: | $MIN_THRESHOLD% |\n\n"
      )
      append("<details>\n")
      append("<summary>Succeeded Coverages</summary><br>\n\n")
      append("| File | Coverage | Lines Hit | Status | Min Required |\n")
      append("|------|:--------:|----------:|:------:|:------------:|\n")
      append(
        "| [${filePathList.get(0).substringAfterLast("/")}]" +
          "($oppiaDevelopGitHubLink/${filePathList.get(0)}) | 75.00% | 3 / 4 | " +
          ":white_check_mark: | $MIN_THRESHOLD% |\n"
      )
      append("</details>\n\n")
      append("### Test File Exempted Cases\n")
      append(
        "- [${filePathList.get(2).substringAfterLast("/")}]" +
          "($oppiaDevelopGitHubLink/${filePathList.get(2)})"
      )
    }

    assertThat(outputReportText).isEqualTo(expectedResult)
  }

  @Test
  fun testRunCoverage_withSuccessFailureMissingTestAndExemptedFiles_generatesFinalReport() {
    val oppiaDevelopGitHubLink = "https://github.com/oppia/oppia-android/tree/develop"
    val filePathList = listOf(
      "coverage/main/java/com/example/AddNums.kt",
      "coverage/main/java/com/example/LowTestNums.kt",
      "app/src/main/java/org/oppia/android/app/activity/ActivityComponent.kt",
      "file.kt"
    )

    tempFolder.newFile("file.kt")

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

    assertThat(exception).hasMessageThat()
      .contains("Coverage Analysis$BOLD$RED FAILED$RESET")

    val outputReportText = File(
      "${tempFolder.root}" +
        "$coverageDir/CoverageReport.md"
    ).readText()

    val failureMessage =
      "No appropriate test file found for file.kt"

    val expectedResult = buildString {
      append("## Coverage Report\n\n")
      append("- Number of files assessed: 4\n")
      append("- Coverage Analysis: **FAIL** :x:\n\n")
      append("### Failure Cases\n")
      append("| File | Failure Reason |\n")
      append("|------|----------------|\n")
      append("| [file.kt]($oppiaDevelopGitHubLink/file.kt) | $failureMessage |\n\n")
      append("| File | Coverage | Lines Hit | Status | Min Required |\n")
      append("|------|:--------:|----------:|:------:|:------------:|\n")
      append(
        "| [${filePathList.get(1).substringAfterLast("/")}]" +
          "($oppiaDevelopGitHubLink/${filePathList.get(1)}) | 0.00% | 0 / 4 | " +
          ":x: | $MIN_THRESHOLD% |\n\n"
      )
      append("<details>\n")
      append("<summary>Succeeded Coverages</summary><br>\n\n")
      append("| File | Coverage | Lines Hit | Status | Min Required |\n")
      append("|------|:--------:|----------:|:------:|:------------:|\n")
      append(
        "| [${filePathList.get(0).substringAfterLast("/")}]" +
          "($oppiaDevelopGitHubLink/${filePathList.get(0)}) | 75.00% | 3 / 4 | " +
          ":white_check_mark: | $MIN_THRESHOLD% |\n"
      )
      append("</details>\n\n")
      append("### Test File Exempted Cases\n")
      append(
        "- [${filePathList.get(2).substringAfterLast("/")}]" +
          "($oppiaDevelopGitHubLink/${filePathList.get(2)})"
      )
    }

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
    ).readText()

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
    ).readText()

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
    ).readText()

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
    ).readText()

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
    ).readText()

    val expectedResult = buildString {
      append("## Coverage Report\n\n")
      append("- Number of files assessed: 1\n")
      append("- Coverage Analysis: **PASS** :white_check_mark:\n\n")
      append("<details>\n")
      append("<summary>Succeeded Coverages</summary><br>\n\n")
      append("| File | Coverage | Lines Hit | Status | Min Required |\n")
      append("|------|:--------:|----------:|:------:|:------------:|\n")
      append(
        "| [${filePathList.get(0).substringAfterLast("/")}]" +
          "($oppiaDevelopGitHubLink/${filePathList.get(0)}) | 50.00% | 2 / 4 | " +
          ":white_check_mark: | $MIN_THRESHOLD% |\n"
      )
      append("</details>")
    }

    assertThat(outputReportText).isEqualTo(expectedResult)
  }

  @Test
  fun testRunCoverage_withMultipleTestsForFile_analysingSameFile() {
    val oppiaDevelopGitHubLink = "https://github.com/oppia/oppia-android/tree/develop"
    val filePathList = listOf("app/main/java/com/example/AddNums.kt")

    testBazelWorkspace.initEmptyWorkspace()

    val testContent1 =
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

    val testContent2 =
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
      testContentShared = testContent1,
      testContentLocal = testContent2,
      subpackage = "app"
    )

    // Both the test files will correspond to one single source file
    // therefore no error would be thrown while aggregating the coverage reports.
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
      append("## Coverage Report\n\n")
      append("- Number of files assessed: 1\n")
      append("- Coverage Analysis: **PASS** :white_check_mark:\n\n")
      append("<details>\n")
      append("<summary>Succeeded Coverages</summary><br>\n\n")
      append("| File | Coverage | Lines Hit | Status | Min Required |\n")
      append("|------|:--------:|----------:|:------:|:------------:|\n")
      append(
        "| [${filePathList.get(0).substringAfterLast("/")}]" +
          "($oppiaDevelopGitHubLink/${filePathList.get(0)}) | 50.00% | 2 / 4 | " +
          ":white_check_mark: | $MIN_THRESHOLD% |\n"
      )
      append("</details>")
    }

    assertThat(outputReportText).isEqualTo(expectedResult)
  }

  /*@Test
  fun testRunCoverage_withMultipleTestsHittingSameLine_calculatesCoverageReportCorrectly() {
    val oppiaDevelopGitHubLink = "https://github.com/oppia/oppia-android/tree/develop"
    val filePathList = listOf("app/main/java/com/example/AddNums.kt")

    testBazelWorkspace.initEmptyWorkspace()

    val testContent1 =
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

    val testContent2 =
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
      testContentShared = testContent1,
      testContentLocal = testContent2,
      subpackage = "app"
    )

    // Both the test files will correspond to one single source file
    // therefore no error would be thrown while aggregating the coverage reports.
    RunCoverage(
      "${tempFolder.root}",
      filePathList,
      ReportFormat.MARKDOWN,
      longCommandExecutor,
      scriptBgDispatcher,
      protoOutputPath
    ).execute()

    val outputReportText = File(
      "${tempFolder.root}" +
        "$coverageDir/CoverageReport.md"
    ).readText()

    val expectedResult = buildString {
      append("## Coverage Report\n\n")
      append("- Number of files assessed: 1\n")
      append("- Coverage Analysis: **PASS** :white_check_mark:\n\n")
      append("<details>\n")
      append("<summary>Succeeded Coverages</summary><br>\n\n")
      append("| File | Coverage | Lines Hit | Status | Min Required |\n")
      append("|------|:--------:|----------:|:------:|:------------:|\n")
      append(
        "| [${filePathList.get(0).substringAfterLast("/")}]" +
          "($oppiaDevelopGitHubLink/${filePathList.get(0)}) | 50.00% | 2 / 4 | " +
          ":white_check_mark: | $MIN_THRESHOLD% |\n"
      )
      append("</details>")
    }

    assertThat(outputReportText).isEqualTo(expectedResult)
  }*/

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

  @Test
  fun testRunCoverage_outputProtoPathProvided_savesCoverageContainerProto() {
    val sampleFile = "file.kt"
    testBazelWorkspace.initEmptyWorkspace()
    tempFolder.newFile(sampleFile)
    val exception = assertThrows<IllegalStateException>() {
      main(
        tempFolder.root.absolutePath,
        sampleFile,
        "--format=Markdown",
        "--protoOutputPath=${tempFolder.root}/report.proto64"
      )
    }

    assertThat(exception).hasMessageThat()
      .contains("Coverage Analysis$BOLD$RED FAILED$RESET")
    val outputFilePath = "${tempFolder.root.absolutePath}/report.proto64"

    assertThat(File(outputFilePath).exists()).isTrue()
  }

  private fun getExpectedMarkdownText(filePath: String): String {
    val oppiaDevelopGitHubLink = "https://github.com/oppia/oppia-android/tree/develop"
    val filename = filePath.substringAfterLast("/")

    val markdownText = buildString {
      append("## Coverage Report\n\n")
      append("- Number of files assessed: 1\n")
      append("- Coverage Analysis: **PASS** :white_check_mark:\n\n")
      append("<details>\n")
      append("<summary>Succeeded Coverages</summary><br>\n\n")
      append("| File | Coverage | Lines Hit | Status | Min Required |\n")
      append("|------|:--------:|----------:|:------:|:------------:|\n")
      append(
        "| [$filename]($oppiaDevelopGitHubLink/$filePath) | 75.00% | " +
          "3 / 4 | :white_check_mark: | $MIN_THRESHOLD% |\n"
      )
      append("</details>")
    }

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
