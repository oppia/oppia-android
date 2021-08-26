package org.oppia.android.scripts.regex

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.testing.assertThrows
import java.io.ByteArrayOutputStream
import java.io.PrintStream

/** Tests for [RegexPatternValidationCheck]. */
class RegexPatternValidationCheckTest {
  private val outContent: ByteArrayOutputStream = ByteArrayOutputStream()
  private val originalOut: PrintStream = System.out
  private val REGEX_CHECK_PASSED_OUTPUT_INDICATOR: String = "REGEX PATTERN CHECKS PASSED"
  private val REGEX_CHECK_FAILED_OUTPUT_INDICATOR: String = "REGEX PATTERN CHECKS FAILED"
  private val supportLibraryUsageErrorMessage =
    "AndroidX should be used instead of the support library"
  private val coroutineWorkerUsageErrorMessage =
    "For stable tests, prefer using ListenableWorker with an Oppia-managed dispatcher."
  private val settableFutureUsageErrorMessage =
    "SettableFuture should only be used in pre-approved locations since it's easy to potentially" +
      " mess up & lead to a hanging ListenableFuture."
  private val androidGravityLeftErrorMessage =
    "Use android:gravity=\"start\", instead, for proper RTL support"
  private val androidGravityRightErrorMessage =
    "Use android:gravity=\"end\", instead, for proper RTL support"
  private val androidLayoutGravityLeftErrorMessage =
    "Use android:layout_gravity=\"start\", instead, for proper RTL support"
  private val androidLayoutGravityRightErrorMessage =
    "Use android:layout_gravity=\"end\", instead, for proper RTL support"
  private val androidGenericStartEndRtlErrorMessage =
    "Use start/end versions of layout properties, instead, for proper RTL support"
  private val androidBarrierDirectionLeftErrorMessage =
    "Use app:barrierDirection=\"start\", instead, for proper RTL support"
  private val androidBarrierDirectionRightErrorMessage =
    "Use app:barrierDirection=\"end\", instead, for proper RTL support"
  private val androidDragDirectionLeftErrorMessage =
    "Use motion:dragDirection=\"start\", instead, for proper RTL support"
  private val androidDragDirectionRightErrorMessage =
    "Use motion:dragDirection=\"end\", instead, for proper RTL support"
  private val androidTouchAnchorSideLeftErrorMessage =
    "Use motion:touchAnchorSide=\"start\", instead, for proper RTL support"
  private val androidTouchAnchorSideRightErrorMessage =
    "Use motion:touchAnchorSide=\"end\", instead, for proper RTL support"
  private val wikiReferenceNote =
    "Refer to https://github.com/oppia/oppia-android/wiki/Static-Analysis-Checks" +
      "#regexpatternvalidation-check for more details on how to fix this."

  @Rule
  @JvmField
  var tempFolder = TemporaryFolder()

  @Before
  fun setUp() {
    tempFolder.newFolder("testfiles")
    System.setOut(PrintStream(outContent))
  }

  @After
  fun restoreStreams() {
    System.setOut(originalOut)
  }

  @Test
  fun testFileNamePattern_activityInAppModule_fileNamePatternIsCorrect() {
    tempFolder.newFolder("testfiles", "app", "src", "main")
    tempFolder.newFile("testfiles/app/src/main/TestActivity.kt")

    runScript()

    assertThat(outContent.toString().trim()).isEqualTo(REGEX_CHECK_PASSED_OUTPUT_INDICATOR)
  }

  @Test
  fun testFileNamePattern_activityInTestingModule_fileNamePatternIsCorrect() {
    tempFolder.newFolder("testfiles", "testing", "src", "main")
    tempFolder.newFile("testfiles/testing/src/main/TestActivity.kt")

    runScript()

    assertThat(outContent.toString().trim()).isEqualTo(REGEX_CHECK_PASSED_OUTPUT_INDICATOR)
  }

  @Test
  fun testFileNamePattern_activityInDataModule_fileNamePatternIsNotCorrect() {
    tempFolder.newFolder("testfiles", "data", "src", "main")
    tempFolder.newFile("testfiles/data/src/main/TestActivity.kt")

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim()).isEqualTo(
      """
      File name/path violation: Activities cannot be placed outside the app or testing module
      - ${retrieveTestFilesPath()}/data/src/main/TestActivity.kt
      
      $wikiReferenceNote
      """.trimIndent()
    )
  }

  @Test
  fun testFileContent_emptyFile_fileContentIsCorrect() {
    tempFolder.newFile("testfiles/TestFile.kt")

    runScript()

    assertThat(outContent.toString().trim()).isEqualTo(REGEX_CHECK_PASSED_OUTPUT_INDICATOR)
  }

  @Test
  fun testFileContent_supportLibraryImport_fileContentIsNotCorrect() {
    val prohibitedContent = "import android.support.v7.app"
    val fileContainsSupportLibraryImport = tempFolder.newFile("testfiles/TestFile.kt")
    fileContainsSupportLibraryImport.writeText(prohibitedContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        ${retrieveTestFilesPath()}/TestFile.kt:1: $supportLibraryUsageErrorMessage
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_coroutineWorker_fileContentIsNotCorrect() {
    val prohibitedContent = ") : CoroutineWorker(context, params) {"
    val fileContainsSupportLibraryImport = tempFolder.newFile("testfiles/TestFile.kt")
    fileContainsSupportLibraryImport.writeText(prohibitedContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        ${retrieveTestFilesPath()}/TestFile.kt:1: $coroutineWorkerUsageErrorMessage
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_settableFuture_fileContentIsNotCorrect() {
    val prohibitedContent = "SettableFuture.create<Result>()"
    val fileContainsSupportLibraryImport = tempFolder.newFile("testfiles/TestFile.kt")
    fileContainsSupportLibraryImport.writeText(prohibitedContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        ${retrieveTestFilesPath()}/TestFile.kt:1: $settableFutureUsageErrorMessage
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_androidGravityLeft_fileContentIsNotCorrect() {
    val prohibitedContent = "android:gravity=\"left\""
    val fileContainsSupportLibraryImport = tempFolder.newFile("testfiles/test_layout.xml")
    fileContainsSupportLibraryImport.writeText(prohibitedContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        ${retrieveTestFilesPath()}/test_layout.xml:1: $androidGravityLeftErrorMessage
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_androidGravityRight_fileContentIsNotCorrect() {
    val prohibitedContent = "android:gravity=\"right\""
    val fileContainsSupportLibraryImport = tempFolder.newFile("testfiles/test_layout.xml")
    fileContainsSupportLibraryImport.writeText(prohibitedContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        ${retrieveTestFilesPath()}/test_layout.xml:1: $androidGravityRightErrorMessage
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_androidLayoutGravityLeft_fileContentIsNotCorrect() {
    val prohibitedContent = "android:layout_gravity=\"left\""
    val fileContainsSupportLibraryImport = tempFolder.newFile("testfiles/test_layout.xml")
    fileContainsSupportLibraryImport.writeText(prohibitedContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        ${retrieveTestFilesPath()}/test_layout.xml:1: $androidLayoutGravityLeftErrorMessage
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_androidLayoutGravityRight_fileContentIsNotCorrect() {
    val prohibitedContent = "android:layout_gravity=\"right\""
    val fileContainsSupportLibraryImport = tempFolder.newFile("testfiles/test_layout.xml")
    fileContainsSupportLibraryImport.writeText(prohibitedContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        ${retrieveTestFilesPath()}/test_layout.xml:1: $androidLayoutGravityRightErrorMessage
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_androidPaddingLeft_fileContentIsNotCorrect() {
    val prohibitedContent = "android:paddingLeft=\"16dp\""
    val fileContainsSupportLibraryImport = tempFolder.newFile("testfiles/test_layout.xml")
    fileContainsSupportLibraryImport.writeText(prohibitedContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        ${retrieveTestFilesPath()}/test_layout.xml:1: $androidGenericStartEndRtlErrorMessage
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_androidPaddingRight_fileContentIsNotCorrect() {
    val prohibitedContent = "android:paddingRight=\"16dp\""
    val fileContainsSupportLibraryImport = tempFolder.newFile("testfiles/test_layout.xml")
    fileContainsSupportLibraryImport.writeText(prohibitedContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        ${retrieveTestFilesPath()}/test_layout.xml:1: $androidGenericStartEndRtlErrorMessage
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_androidDrawableLeft_fileContentIsNotCorrect() {
    val prohibitedContent = "android:drawableLeft=\"@android:color/transparent\""
    val fileContainsSupportLibraryImport = tempFolder.newFile("testfiles/test_layout.xml")
    fileContainsSupportLibraryImport.writeText(prohibitedContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        ${retrieveTestFilesPath()}/test_layout.xml:1: $androidGenericStartEndRtlErrorMessage
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_androidDrawableRight_fileContentIsNotCorrect() {
    val prohibitedContent = "android:drawableRight=\"@android:color/transparent\""
    val fileContainsSupportLibraryImport = tempFolder.newFile("testfiles/test_layout.xml")
    fileContainsSupportLibraryImport.writeText(prohibitedContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        ${retrieveTestFilesPath()}/test_layout.xml:1: $androidGenericStartEndRtlErrorMessage
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_androidLayoutAlignLeft_fileContentIsNotCorrect() {
    val prohibitedContent = "android:layout_alignLeft=\"@+id/another_view\""
    val fileContainsSupportLibraryImport = tempFolder.newFile("testfiles/test_layout.xml")
    fileContainsSupportLibraryImport.writeText(prohibitedContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        ${retrieveTestFilesPath()}/test_layout.xml:1: $androidGenericStartEndRtlErrorMessage
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_androidLayoutAlignRight_fileContentIsNotCorrect() {
    val prohibitedContent = "android:layout_alignRight=\"@+id/another_view\""
    val fileContainsSupportLibraryImport = tempFolder.newFile("testfiles/test_layout.xml")
    fileContainsSupportLibraryImport.writeText(prohibitedContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        ${retrieveTestFilesPath()}/test_layout.xml:1: $androidGenericStartEndRtlErrorMessage
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_androidLayoutMarginLeft_fileContentIsNotCorrect() {
    val prohibitedContent = "android:layout_marginLeft=\"16dp\""
    val fileContainsSupportLibraryImport = tempFolder.newFile("testfiles/test_layout.xml")
    fileContainsSupportLibraryImport.writeText(prohibitedContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        ${retrieveTestFilesPath()}/test_layout.xml:1: $androidGenericStartEndRtlErrorMessage
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_androidLayoutMarginRight_fileContentIsNotCorrect() {
    val prohibitedContent = "android:layout_marginRight=\"16dp\""
    val fileContainsSupportLibraryImport = tempFolder.newFile("testfiles/test_layout.xml")
    fileContainsSupportLibraryImport.writeText(prohibitedContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        ${retrieveTestFilesPath()}/test_layout.xml:1: $androidGenericStartEndRtlErrorMessage
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_androidLayoutAlignParentLeft_fileContentIsNotCorrect() {
    val prohibitedContent = "android:layout_alignParentLeft=\"true\""
    val fileContainsSupportLibraryImport = tempFolder.newFile("testfiles/test_layout.xml")
    fileContainsSupportLibraryImport.writeText(prohibitedContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        ${retrieveTestFilesPath()}/test_layout.xml:1: $androidGenericStartEndRtlErrorMessage
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_androidLayoutAlignParentRight_fileContentIsNotCorrect() {
    val prohibitedContent = "android:layout_alignParentRight=\"true\""
    val fileContainsSupportLibraryImport = tempFolder.newFile("testfiles/test_layout.xml")
    fileContainsSupportLibraryImport.writeText(prohibitedContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        ${retrieveTestFilesPath()}/test_layout.xml:1: $androidGenericStartEndRtlErrorMessage
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_androidLayoutToLeftOf_fileContentIsNotCorrect() {
    val prohibitedContent = "android:layout_toLeftOf=\"@+id/another_view\""
    val fileContainsSupportLibraryImport = tempFolder.newFile("testfiles/test_layout.xml")
    fileContainsSupportLibraryImport.writeText(prohibitedContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        ${retrieveTestFilesPath()}/test_layout.xml:1: $androidGenericStartEndRtlErrorMessage
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_androidLayoutToRightOf_fileContentIsNotCorrect() {
    val prohibitedContent = "android:layout_toRightOf=\"@+id/another_view\""
    val fileContainsSupportLibraryImport = tempFolder.newFile("testfiles/test_layout.xml")
    fileContainsSupportLibraryImport.writeText(prohibitedContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        ${retrieveTestFilesPath()}/test_layout.xml:1: $androidGenericStartEndRtlErrorMessage
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_androidLayoutConstraintLeftToLeftOf_fileContentIsNotCorrect() {
    val prohibitedContent = "android:layout_constraintLeft_toLeftOf=\"@+id/another_view\""
    val fileContainsSupportLibraryImport = tempFolder.newFile("testfiles/test_layout.xml")
    fileContainsSupportLibraryImport.writeText(prohibitedContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        ${retrieveTestFilesPath()}/test_layout.xml:1: $androidGenericStartEndRtlErrorMessage
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_androidLayoutConstraintLeftToRightOf_fileContentIsNotCorrect() {
    val prohibitedContent = "android:layout_constraintLeft_toRightOf=\"@+id/another_view\""
    val fileContainsSupportLibraryImport = tempFolder.newFile("testfiles/test_layout.xml")
    fileContainsSupportLibraryImport.writeText(prohibitedContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        ${retrieveTestFilesPath()}/test_layout.xml:1: $androidGenericStartEndRtlErrorMessage
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_androidLayoutConstraintRightToLeftOf_fileContentIsNotCorrect() {
    val prohibitedContent = "android:layout_constraintRight_toLeftOf=\"@+id/another_view\""
    val fileContainsSupportLibraryImport = tempFolder.newFile("testfiles/test_layout.xml")
    fileContainsSupportLibraryImport.writeText(prohibitedContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        ${retrieveTestFilesPath()}/test_layout.xml:1: $androidGenericStartEndRtlErrorMessage
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_androidLayoutConstraintRightToRightOf_fileContentIsNotCorrect() {
    val prohibitedContent = "android:layout_constraintRight_toRightOf=\"@+id/another_view\""
    val fileContainsSupportLibraryImport = tempFolder.newFile("testfiles/test_layout.xml")
    fileContainsSupportLibraryImport.writeText(prohibitedContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        ${retrieveTestFilesPath()}/test_layout.xml:1: $androidGenericStartEndRtlErrorMessage
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_androidLayoutGoneMarginLeft_fileContentIsNotCorrect() {
    val prohibitedContent = "android:layout_goneMarginLeft=\"16dp\""
    val fileContainsSupportLibraryImport = tempFolder.newFile("testfiles/test_layout.xml")
    fileContainsSupportLibraryImport.writeText(prohibitedContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        ${retrieveTestFilesPath()}/test_layout.xml:1: $androidGenericStartEndRtlErrorMessage
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_androidLayoutGoneMarginRight_fileContentIsNotCorrect() {
    val prohibitedContent = "android:layout_goneMarginRight=\"16dp\""
    val fileContainsSupportLibraryImport = tempFolder.newFile("testfiles/test_layout.xml")
    fileContainsSupportLibraryImport.writeText(prohibitedContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        ${retrieveTestFilesPath()}/test_layout.xml:1: $androidGenericStartEndRtlErrorMessage
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_appBarrierDirectionLeft_fileContentIsNotCorrect() {
    val prohibitedContent = "app:barrierDirection=\"left\""
    val fileContainsSupportLibraryImport = tempFolder.newFile("testfiles/test_layout.xml")
    fileContainsSupportLibraryImport.writeText(prohibitedContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        ${retrieveTestFilesPath()}/test_layout.xml:1: $androidBarrierDirectionLeftErrorMessage
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_appBarrierDirectionRight_fileContentIsNotCorrect() {
    val prohibitedContent = "app:barrierDirection=\"right\""
    val fileContainsSupportLibraryImport = tempFolder.newFile("testfiles/test_layout.xml")
    fileContainsSupportLibraryImport.writeText(prohibitedContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        ${retrieveTestFilesPath()}/test_layout.xml:1: $androidBarrierDirectionRightErrorMessage
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_appDragDirectionLeft_fileContentIsNotCorrect() {
    val prohibitedContent = "motion:dragDirection=\"left\""
    val fileContainsSupportLibraryImport = tempFolder.newFile("testfiles/test_layout.xml")
    fileContainsSupportLibraryImport.writeText(prohibitedContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        ${retrieveTestFilesPath()}/test_layout.xml:1: $androidDragDirectionLeftErrorMessage
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_appDragDirectionRight_fileContentIsNotCorrect() {
    val prohibitedContent = "motion:dragDirection=\"right\""
    val fileContainsSupportLibraryImport = tempFolder.newFile("testfiles/test_layout.xml")
    fileContainsSupportLibraryImport.writeText(prohibitedContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        ${retrieveTestFilesPath()}/test_layout.xml:1: $androidDragDirectionRightErrorMessage
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_appTouchAnchorSideLeft_fileContentIsNotCorrect() {
    val prohibitedContent = "motion:touchAnchorSide=\"left\""
    val fileContainsSupportLibraryImport = tempFolder.newFile("testfiles/test_layout.xml")
    fileContainsSupportLibraryImport.writeText(prohibitedContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        ${retrieveTestFilesPath()}/test_layout.xml:1: $androidTouchAnchorSideLeftErrorMessage
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_appTouchAnchorSideRight_fileContentIsNotCorrect() {
    val prohibitedContent = "motion:touchAnchorSide=\"right\""
    val fileContainsSupportLibraryImport = tempFolder.newFile("testfiles/test_layout.xml")
    fileContainsSupportLibraryImport.writeText(prohibitedContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        ${retrieveTestFilesPath()}/test_layout.xml:1: $androidTouchAnchorSideRightErrorMessage
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFilenameAndContent_useProhibitedFileName_useProhibitedFileContent_multipleFailures() {
    tempFolder.newFolder("testfiles", "data", "src", "main")
    val prohibitedFile = tempFolder.newFile("testfiles/data/src/main/TestActivity.kt")
    val prohibitedContent = "import android.support.v7.app"
    prohibitedFile.writeText(prohibitedContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim()).isEqualTo(
      """
      File name/path violation: Activities cannot be placed outside the app or testing module
      - ${retrieveTestFilesPath()}/data/src/main/TestActivity.kt
      
      ${retrieveTestFilesPath()}/data/src/main/TestActivity.kt:1: AndroidX should be used instead of the support library
      $wikiReferenceNote
      """.trimIndent()
    )
  }

  /** Retrieves the absolute path of testfiles directory. */
  private fun retrieveTestFilesPath(): String {
    return "${tempFolder.root}/testfiles"
  }

  /** Runs the regex_pattern_validation_check. */
  private fun runScript() {
    main(retrieveTestFilesPath())
  }
}
