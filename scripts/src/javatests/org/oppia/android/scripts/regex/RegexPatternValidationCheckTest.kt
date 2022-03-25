package org.oppia.android.scripts.regex

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.testing.assertThrows
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream

/** Tests for the regex pattern validation check (see [main]). */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
class RegexPatternValidationCheckTest {
  private val outContent: ByteArrayOutputStream = ByteArrayOutputStream()
  private val originalOut: PrintStream = System.out
  private val activitiesPlacementErrorMessage =
    "Activities cannot be placed outside the app or testing module."
  private val nestedResourceSubdirectoryErrorMessage =
    "Only one level of subdirectories under res/ should be maintained (further subdirectories " +
      "aren't supported by the project configuration)."
  private val supportLibraryUsageErrorMessage =
    "AndroidX should be used instead of the support library"
  private val coroutineWorkerUsageErrorMessage =
    "For stable tests, prefer using ListenableWorker with an Oppia-managed dispatcher."
  private val announceForAccessibilityUsageErrorMessage =
    "Please use AccessibilityService instead."
  private val announceForAccessibilityForViewUsageErrorMessage =
    "When using announceForAccessibility, please add an exempt file in " +
      "file_content_validation_checks.textproto."
  private val settableFutureUsageErrorMessage =
    "SettableFuture should only be used in pre-approved locations since it's easy to potentially " +
      "mess up & lead to a hanging ListenableFuture."
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
  private val oppiaCantBeTranslatedErrorMessage =
    "Oppia should never used directly in a string (since it shouldn't be translated). Instead, " +
      "use a parameter & insert the string retrieved from app_name."
  private val untranslatableStringsGoInSpecificFileErrorMessage =
    "Untranslatable strings should go in untranslated_strings.xml, instead."
  private val translatableStringsGoInMainFileErrorMessage =
    "All strings outside strings.xml must be marked as not translatable, or moved to strings.xml."
  private val translatablePluralsGoInMainFileErrorMessage =
    "All plurals outside strings.xml must be marked as not translatable, or moved to strings.xml."
  private val importingAndroidBidiFormatterErrorMessage =
    "Do not use Android's BidiFormatter directly. Instead, use AndroidX's BidiFormatter for" +
      " KitKat compatibility."
  private val importingAndroidXBidiFormatterErrorMessage =
    "Do not use AndroidX's BidiFormatter directly. Instead, use the wrapper utility" +
      " OppiaBidiFormatter so that tests can verify that formatting actually occurs on select" +
      " strings."
  private val useStringFormattingFunctionInKotlinOrJavaErrorMessage =
    "String formatting and resource retrieval should go through AppLanguageResourceHandler," +
      " OppiaLocale.DisplayLocale, or OppiaLocale.MachineLocale depending on the context (see" +
      " each class's documentation for details on when each should be used)."
  private val useCaseInsensitiveOperationErrorMessage =
    "Case-insensitive string operations should be performed using MachineLocale."
  private val useStringFormattingFunctionInXmlErrorMessage =
    "String formatting and resource retrieval in layouts should go through" +
      " AppLanguageResourceHandler."
  private val useDatabindingStringOperationsErrorMessage =
    "String formatting and quantity string building shouldn't be done directly through" +
      " databinding. Instead, pass in AppLanguageResourceHandler from the view model or call a" +
      " new function through the view model to compute the string. Both should use the handler's" +
      " locale-safe formatting/quantity string methods."
  private val useDatabindingPluralsErrorMessage =
    "String plurals shouldn't be constructed directly through databinding. Instead, pass in" +
      " AppLanguageResourceHandler from the view model or call a new function through the view" +
      " model to compute the string. Both should use the handler's locale-safe" +
      " formatting/quantity string methods."
  private val useNonStringTypeSpecifiersErrorMessage =
    "Only string type specifiers should use for strings (to avoid runtime errors due to" +
      " bidirectional wrapping requirements)."
  private val subclassedActivityErrorMessage =
    "Activity should never be subclassed. Use AppCompatActivity, instead."
  private val subclassedAppCompatActivityErrorMessage =
    "Never subclass AppCompatActivity directly. Instead, use InjectableAppCompatActivity."
  private val subclassedDialogFragmentErrorMessage =
    "DialogFragment should never be subclassed. Use InjectableDialogFragment, instead."
  private val androidActivityConfigChangesErrorMessage =
    "Never explicitly handle configuration changes. Instead, use saved instance states for" +
      " retaining state across rotations. For other types of configuration changes, follow up" +
      " with the developer mailing list with how to proceed if you think this is a legitimate case."
  private val nonCompatDrawableUsedErrorMessage =
    "Drawable start/end/top/bottom & image source should use the compat versions, instead, e.g.:" +
      " app:drawableStartCompat or app:srcCompat, to ensure that vector drawables can load" +
      " properly in SDK <21 environments."
  private val useJava8OptionalErrorMessage =
    "Prefer using com.google.common.base.Optional (Guava's Optional) since desugaring has some" +
      " incompatibilities between Bazel & KitKat builds."
  private val useJavaCalendarErrorMessage =
    "Don't use Calendar directly. Instead, use OppiaClock and/or OppiaLocale for" +
      " calendar-specific operations."
  private val useJavaDateErrorMessage =
    "Don't use Date directly. Instead, perform date-based operations using OppiaLocale."
  private val useJavaTextErrorMessage =
    "Don't perform date/time formatting directly. Instead, use OppiaLocale."
  private val useJavaLocaleErrorMessage =
    "Don't use Locale directly. Instead, use LocaleController, or OppiaLocale & its subclasses."
  private val doNotUseKotlinDelegatesErrorMessage =
    "Don't use Delegates; use a lateinit var or nullable primitive var default-initialized to" +
      " null, instead. Delegates uses reflection internally, have a non-trivial initialization" +
      " cost, and can cause breakages on KitKat devices. See #3939 for more context."
  private val doNotUseProtoLibrary = "Don't use proto_library. Use oppia_proto_library instead."
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
      File name/path violation: $activitiesPlacementErrorMessage
      - data/src/main/TestActivity.kt

      $wikiReferenceNote
      """.trimIndent()
    )
  }

  @Test
  fun testFileNamePattern_appResources_stringsFile_fileNamePatternIsCorrect() {
    tempFolder.newFolder("testfiles", "app", "src", "main", "res", "values")
    tempFolder.newFile("testfiles/app/src/main/res/values/strings.xml")

    runScript()

    assertThat(outContent.toString().trim()).isEqualTo(REGEX_CHECK_PASSED_OUTPUT_INDICATOR)
  }

  @Test
  fun testFileNamePattern_appResources_subValuesDir_stringsFile_fileNamePatternIsNotCorrect() {
    tempFolder.newFolder("testfiles", "app", "src", "main", "res", "values", "subdir")
    tempFolder.newFile("testfiles/app/src/main/res/values/subdir/strings.xml")

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim()).isEqualTo(
      """
      File name/path violation: $nestedResourceSubdirectoryErrorMessage
      - app/src/main/res/values/subdir/strings.xml

      $wikiReferenceNote
      """.trimIndent()
    )
  }

  @Test
  fun testFileNamePattern_domainResources_subValuesDir_stringsFile_fileNamePatternIsNotCorrect() {
    tempFolder.newFolder("testfiles", "domain", "src", "main", "res", "drawable", "subdir")
    tempFolder.newFile("testfiles/domain/src/main/res/drawable/subdir/example.png")

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim()).isEqualTo(
      """
      File name/path violation: $nestedResourceSubdirectoryErrorMessage
      - domain/src/main/res/drawable/subdir/example.png
      
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
        TestFile.kt:1: $supportLibraryUsageErrorMessage
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
        TestFile.kt:1: $coroutineWorkerUsageErrorMessage
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_announceForAccessibilityUsageErrorMessage_fileContentIsNotCorrect() {
    val prohibitedContent = "announceForAccessibility("
    val fileContainsSupportLibraryImport = tempFolder.newFile("testfiles/TestFile.kt")
    fileContainsSupportLibraryImport.writeText(prohibitedContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        TestFile.kt:1: $announceForAccessibilityUsageErrorMessage
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_announceForAccessibilityForViewUsageErrorMessage_fileContentIsNotCorrect() {
    val prohibitedContent = "announceForAccessibilityForView("
    val fileContainsSupportLibraryImport = tempFolder.newFile("testfiles/TestFile.kt")
    fileContainsSupportLibraryImport.writeText(prohibitedContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        TestFile.kt:1: $announceForAccessibilityForViewUsageErrorMessage
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
        TestFile.kt:1: $settableFutureUsageErrorMessage
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
        test_layout.xml:1: $androidGravityLeftErrorMessage
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
        test_layout.xml:1: $androidGravityRightErrorMessage
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
        test_layout.xml:1: $androidLayoutGravityLeftErrorMessage
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
        test_layout.xml:1: $androidLayoutGravityRightErrorMessage
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
        test_layout.xml:1: $androidGenericStartEndRtlErrorMessage
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
        test_layout.xml:1: $androidGenericStartEndRtlErrorMessage
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
        test_layout.xml:1: $androidGenericStartEndRtlErrorMessage
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
        test_layout.xml:1: $androidGenericStartEndRtlErrorMessage
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
        test_layout.xml:1: $androidGenericStartEndRtlErrorMessage
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
        test_layout.xml:1: $androidGenericStartEndRtlErrorMessage
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
        test_layout.xml:1: $androidGenericStartEndRtlErrorMessage
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
        test_layout.xml:1: $androidGenericStartEndRtlErrorMessage
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
        test_layout.xml:1: $androidGenericStartEndRtlErrorMessage
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
        test_layout.xml:1: $androidGenericStartEndRtlErrorMessage
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
        test_layout.xml:1: $androidGenericStartEndRtlErrorMessage
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
        test_layout.xml:1: $androidGenericStartEndRtlErrorMessage
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
        test_layout.xml:1: $androidGenericStartEndRtlErrorMessage
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
        test_layout.xml:1: $androidGenericStartEndRtlErrorMessage
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
        test_layout.xml:1: $androidGenericStartEndRtlErrorMessage
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
        test_layout.xml:1: $androidGenericStartEndRtlErrorMessage
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
        test_layout.xml:1: $androidGenericStartEndRtlErrorMessage
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
        test_layout.xml:1: $androidGenericStartEndRtlErrorMessage
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
        test_layout.xml:1: $androidBarrierDirectionLeftErrorMessage
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
        test_layout.xml:1: $androidBarrierDirectionRightErrorMessage
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
        test_layout.xml:1: $androidDragDirectionLeftErrorMessage
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
        test_layout.xml:1: $androidDragDirectionRightErrorMessage
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
        test_layout.xml:1: $androidTouchAnchorSideLeftErrorMessage
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
        test_layout.xml:1: $androidTouchAnchorSideRightErrorMessage
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_oppiaInString_inPrimaryStringsFile_fileContentIsNotCorrect() {
    val prohibitedContent = "<string name=\"test\">String with Oppia in it</string>"
    tempFolder.newFolder("testfiles", "app", "src", "main", "res", "values")
    val stringFilePath = "app/src/main/res/values/strings.xml"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        $stringFilePath:1: $oppiaCantBeTranslatedErrorMessage
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_untranslatableString_inPrimaryStringsFile_fileContentIsNotCorrect() {
    val prohibitedContent = "<string name=\"test\" translatable=\"false\">Something</string>"
    tempFolder.newFolder("testfiles", "app", "src", "main", "res", "values")
    val stringFilePath = "app/src/main/res/values/strings.xml"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        $stringFilePath:1: $untranslatableStringsGoInSpecificFileErrorMessage
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_untranslatableString_inUntranslatedStringsFile_fileContentIsCorrect() {
    val prohibitedContent = "<string name=\"test\" translatable=\"false\">Something</string>"
    tempFolder.newFolder("testfiles", "app", "src", "main", "res", "values")
    val stringFilePath = "app/src/main/res/values/untranslated_strings.xml"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    runScript()

    assertThat(outContent.toString().trim()).isEqualTo(REGEX_CHECK_PASSED_OUTPUT_INDICATOR)
  }

  @Test
  fun testFileContent_translatableString_outsidePrimaryStringsFile_fileContentIsNotCorrect() {
    val prohibitedContent = "<string name=\"test\">Translatable</string>"
    tempFolder.newFolder("testfiles", "app", "src", "main", "res", "values")
    val stringFilePath = "app/src/main/res/values/untranslated_strings.xml"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        $stringFilePath:1: $translatableStringsGoInMainFileErrorMessage
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_translatablePlural_outsidePrimaryStringsFile_fileContentIsNotCorrect() {
    val prohibitedContent = "<plurals name=\"test\">"
    tempFolder.newFolder("testfiles", "app", "src", "main", "res", "values")
    val stringFilePath = "app/src/main/res/values/untranslated_strings.xml"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        $stringFilePath:1: $translatablePluralsGoInMainFileErrorMessage
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_androidBidiFormatterImport_fileContentIsNotCorrect() {
    val prohibitedContent = "import android.text.BidiFormatter"
    tempFolder.newFolder("testfiles", "data", "src", "main")
    val stringFilePath = "data/src/main/SomeController.kt"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        $stringFilePath:1: $importingAndroidBidiFormatterErrorMessage
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_androidXBidiFormatterImport_fileContentIsNotCorrect() {
    val prohibitedContent = "import androidx.core.text.BidiFormatter"
    tempFolder.newFolder("testfiles", "data", "src", "main")
    val stringFilePath = "data/src/main/SomeController.kt"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        $stringFilePath:1: $importingAndroidXBidiFormatterErrorMessage
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_untranslatableString_outsidePrimaryStringsFile_fileContentIsCorrect() {
    val prohibitedContent = "<string name=\"test\">Translatable</string>"
    tempFolder.newFolder("testfiles", "app", "src", "main", "res", "values")
    val stringFilePath = "app/src/main/res/values/strings.xml"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    runScript()

    assertThat(outContent.toString().trim()).isEqualTo(REGEX_CHECK_PASSED_OUTPUT_INDICATOR)
  }

  @Test
  fun testFileContent_translatableString_inPrimaryStringsFile_fileContentIsCorrect() {
    val prohibitedContent = "<string name=\"test\">Translatable</string>"
    tempFolder.newFolder("testfiles", "app", "src", "main", "res", "values")
    val stringFilePath = "app/src/main/res/values/strings.xml"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    runScript()

    assertThat(outContent.toString().trim()).isEqualTo(REGEX_CHECK_PASSED_OUTPUT_INDICATOR)
  }

  @Test
  fun testFileContent_translatableString_inTranslatedPrimaryStringsFile_fileContentIsCorrect() {
    val prohibitedContent = "<string name=\"test\">Translatable</string>"
    tempFolder.newFolder("testfiles", "app", "src", "main", "res", "values-ar")
    val stringFilePath = "app/src/main/res/values-ar/strings.xml"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    runScript()

    assertThat(outContent.toString().trim()).isEqualTo(REGEX_CHECK_PASSED_OUTPUT_INDICATOR)
  }

  @Test
  fun testFileContent_stringFormattingFunctions_inKotlin_fileContentIsNotCorrect() {
    val prohibitedContent =
      """
        fun exampleFunction() {
          String.format("a string %s", "with this")
          resources.getString(R.string.some_string)
          resources.getStringArray(R.array.some_string_array)
          resources.getQuantityString(R.plurals.plural_string, 1, "parameter")
          resources.getQuantityText(R.plurals.plural_string, 1)
          "Uppercase string".toLowerCase()
          "Uppercase string".lowercase() // Kotlin 1.5 method.
          "lowercase string".toUpperCase()
          "lowercase string".uppercase() // Kotlin 1.5 method.
          "uncapitalized".capitalize() // Kotlin-only
          "Capitalized".decapitalize() // Kotlin-only
        }
      """.trimIndent()
    tempFolder.newFolder("testfiles", "data", "src", "main")
    val stringFilePath = "data/src/main/SomeController.kt"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    // Verify that all patterns are properly detected & prohibited.
    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        $stringFilePath:2: $useStringFormattingFunctionInKotlinOrJavaErrorMessage
        $stringFilePath:3: $useStringFormattingFunctionInKotlinOrJavaErrorMessage
        $stringFilePath:4: $useStringFormattingFunctionInKotlinOrJavaErrorMessage
        $stringFilePath:5: $useStringFormattingFunctionInKotlinOrJavaErrorMessage
        $stringFilePath:6: $useStringFormattingFunctionInKotlinOrJavaErrorMessage
        $stringFilePath:7: $useStringFormattingFunctionInKotlinOrJavaErrorMessage
        $stringFilePath:8: $useStringFormattingFunctionInKotlinOrJavaErrorMessage
        $stringFilePath:9: $useStringFormattingFunctionInKotlinOrJavaErrorMessage
        $stringFilePath:10: $useStringFormattingFunctionInKotlinOrJavaErrorMessage
        $stringFilePath:11: $useStringFormattingFunctionInKotlinOrJavaErrorMessage
        $stringFilePath:12: $useStringFormattingFunctionInKotlinOrJavaErrorMessage
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_stringFormattingFunctions_inJava_fileContentIsNotCorrect() {
    val prohibitedContent =
      """
        void exampleFunction() {
          String.format("a string %s", "with this");
          resources.getString(R.string.some_string);
          resources.getStringArray(R.array.some_string_array);
          resources.getQuantityString(R.plurals.plural_string, 1, "parameter");
          resources.getQuantityText(R.plurals.plural_string, 1);
          "Uppercase string".toLowerCase();
          "lowercase string".toUpperCase();
        }
      """.trimIndent()
    tempFolder.newFolder("testfiles", "data", "src", "main")
    val stringFilePath = "data/src/main/SomeController.java"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    // Verify that all patterns are properly detected & prohibited.
    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        $stringFilePath:2: $useStringFormattingFunctionInKotlinOrJavaErrorMessage
        $stringFilePath:3: $useStringFormattingFunctionInKotlinOrJavaErrorMessage
        $stringFilePath:4: $useStringFormattingFunctionInKotlinOrJavaErrorMessage
        $stringFilePath:5: $useStringFormattingFunctionInKotlinOrJavaErrorMessage
        $stringFilePath:6: $useStringFormattingFunctionInKotlinOrJavaErrorMessage
        $stringFilePath:7: $useStringFormattingFunctionInKotlinOrJavaErrorMessage
        $stringFilePath:8: $useStringFormattingFunctionInKotlinOrJavaErrorMessage
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_ignoreCase_inKotlin_fileContentIsNotCorrect() {
    val prohibitedContent =
      """
        fun exampleFunction() {
          "This strING".startsWith("this", ignoreCase = true)
          "This strING".endsWith("string", ignoreCase = true)
          "This strING".equals("this string", ignoreCase = true)
        }
      """.trimIndent()
    tempFolder.newFolder("testfiles", "data", "src", "main")
    val stringFilePath = "data/src/main/SomeController.kt"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    // Verify that all patterns are properly detected & prohibited.
    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        $stringFilePath:2: $useCaseInsensitiveOperationErrorMessage
        $stringFilePath:3: $useCaseInsensitiveOperationErrorMessage
        $stringFilePath:4: $useCaseInsensitiveOperationErrorMessage
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_stringFormattingFunctions_inXml_fileContentIsNotCorrect() {
    val prohibitedContent =
      """
        <TextView
          android:text="@{String.format(@string/example_string, viewModel.newVar}" />
        <TextView
          android:text="@{resources.getString(R.string.example_string)}" />
        <TextView
          android:text="@{resources.getStringArray(R.array.example_string_array)}" />
      """.trimIndent()
    tempFolder.newFolder("testfiles", "app", "src", "main", "res", "values")
    val stringFilePath = "app/src/main/res/values/strings.xml"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    // Verify that all patterns are properly detected & prohibited.
    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        $stringFilePath:2: $useStringFormattingFunctionInXmlErrorMessage
        $stringFilePath:4: $useStringFormattingFunctionInXmlErrorMessage
        $stringFilePath:6: $useStringFormattingFunctionInXmlErrorMessage
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_stringFormatting_inXml_usingDatabinding_fileContentIsNotCorrect() {
    val prohibitedContent =
      """
        <TextView
          android:text="@{@string/example_str(viewModel.newVar)}" />
      """.trimIndent()
    tempFolder.newFolder("testfiles", "app", "src", "main", "res", "values")
    val stringFilePath = "app/src/main/res/values/strings.xml"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        $stringFilePath:2: $useDatabindingStringOperationsErrorMessage
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_stringPlurals_inXml_usingDatabinding_fileContentIsNotCorrect() {
    val prohibitedContent =
      """
        <TextView
          android:text="@{@plurals/example_plural(viewModel.count, viewModel.otherVar)}" />
      """.trimIndent()
    tempFolder.newFolder("testfiles", "app", "src", "main", "res", "values")
    val stringFilePath = "app/src/main/res/values/strings.xml"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        $stringFilePath:2: $useDatabindingPluralsErrorMessage
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_nonStringTypeAndPositionalSpecifiers_fileContentIsNotCorrect() {
    val prohibitedContent =
      """
        <string name="passing_str1">String with %s string arg</string>
        <string name="failing_str2">String with %d int arg</string>
        <string name="passing_str3">String with %1${"$"}s string positional arg</string>
        <string name="failing_str4">String with %1${"$"}d int positional arg</string>
        <string name="failing_str5">String with %1${"$"}s and %d args</string>
        <string name="failing_str6">%f arg at front</string>
        <string name="passing_str7">%s arg at front</string>
      """.trimIndent()
    tempFolder.newFolder("testfiles", "app", "src", "main", "res", "values")
    val stringFilePath = "app/src/main/res/values/strings.xml"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    // Verify that all patterns are properly detected & prohibited.
    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        $stringFilePath:2: $useNonStringTypeSpecifiersErrorMessage
        $stringFilePath:4: $useNonStringTypeSpecifiersErrorMessage
        $stringFilePath:5: $useNonStringTypeSpecifiersErrorMessage
        $stringFilePath:6: $useNonStringTypeSpecifiersErrorMessage
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_stringTypeAndPositionalSpecifiers_fileContentIsCorrect() {
    val prohibitedContent =
      """
        <string name="passing_str1">String with %s string arg</string>
        <string name="passing_str2">String with %1${"$"}s string positional arg</string>
        <string name="passing_str3">%s arg at front</string>
      """.trimIndent()
    tempFolder.newFolder("testfiles", "app", "src", "main", "res", "values")
    val stringFilePath = "app/src/main/res/values/strings.xml"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    runScript()

    assertThat(outContent.toString().trim()).isEqualTo(REGEX_CHECK_PASSED_OUTPUT_INDICATOR)
  }

  @Test
  fun testFileContent_subclassedActivity_fileContentIsNotCorrect() {
    val prohibitedContent = "class SomeActivity: Activity() {}"
    tempFolder.newFolder("testfiles", "app", "src", "main")
    val stringFilePath = "app/src/main/SomeActivity.kt"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        $stringFilePath:1: $subclassedActivityErrorMessage
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_subclassedAppCompatActivity_fileContentIsNotCorrect() {
    val prohibitedContent = "class SomeActivity: AppCompatActivity() {}"
    tempFolder.newFolder("testfiles", "app", "src", "main")
    val stringFilePath = "app/src/main/SomeActivity.kt"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        $stringFilePath:1: $subclassedAppCompatActivityErrorMessage
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_subclassedDialogFragment_fileContentIsNotCorrect() {
    val prohibitedContent = "class SomeDialogFragment: DialogFragment() {}"
    tempFolder.newFolder("testfiles", "app", "src", "main")
    val stringFilePath = "app/src/main/SomeDialogFragment.kt"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        $stringFilePath:1: $subclassedDialogFragmentErrorMessage
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_activityDeclarationInManifest_withConfigChanges_fileContentIsNotCorrect() {
    val prohibitedContent =
      """
        <?xml version="1.0" encoding="utf-8"?>
        <manifest package="org.oppia.android">
          <application android:name=".app.application.OppiaApplication">
            <activity
              android:name=".app.ExampleActivity"
              android:configChanges="orientation" />
          </application>
        </manifest>
      """.trimIndent()
    tempFolder.newFolder("testfiles", "app", "src", "main")
    val stringFilePath = "app/src/main/AndroidManifest.xml"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        $stringFilePath:6: $androidActivityConfigChangesErrorMessage
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_javaCalendarImport_fileContentIsNotCorrect() {
    val prohibitedContent = "import java.util.Calendar"
    tempFolder.newFolder("testfiles", "domain", "src", "main")
    val stringFilePath = "domain/src/main/SomeController.kt"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        $stringFilePath:1: $useJavaCalendarErrorMessage
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_javaDateImport_fileContentIsNotCorrect() {
    val prohibitedContent = "import java.util.Date"
    tempFolder.newFolder("testfiles", "domain", "src", "main")
    val stringFilePath = "domain/src/main/SomeController.kt"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        $stringFilePath:1: $useJavaDateErrorMessage
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_javaTextImports_fileContentIsNotCorrect() {
    val prohibitedContent =
      """
      import java.text.DateFormat
      import java.text.SimpleDateFormat
      import java.text.ParseException
      """.trimIndent()
    tempFolder.newFolder("testfiles", "domain", "src", "main")
    val stringFilePath = "domain/src/main/SomeController.kt"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        $stringFilePath:1: $useJavaTextErrorMessage
        $stringFilePath:2: $useJavaTextErrorMessage
        $stringFilePath:3: $useJavaTextErrorMessage
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_javaLocaleImport_fileContentIsNotCorrect() {
    val prohibitedContent = "import java.util.Locale"
    tempFolder.newFolder("testfiles", "domain", "src", "main")
    val stringFilePath = "domain/src/main/SomeController.kt"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        $stringFilePath:1: $useJavaLocaleErrorMessage
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_kotlinDelegatesImport_fileContentIsNotCorrect() {
    val prohibitedContent = "kotlin.properties.Delegates"
    tempFolder.newFolder("testfiles", "domain", "src", "main")
    val stringFilePath = "domain/src/main/SomeController.kt"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        $stringFilePath:1: $doNotUseKotlinDelegatesErrorMessage
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_nonCompatDrawables_fileContentIsNotCorrect() {
    val prohibitedContent =
      """
        android:drawableStart="@drawable/example_drawable"
        app:drawableStartCompat="@drawable/example_drawable"
        android:drawableEnd="@drawable/example_drawable"
        app:drawableEndCompat="@drawable/example_drawable"
        android:drawableTop="@drawable/example_drawable"
        app:drawableTopCompat="@drawable/example_drawable"
        android:drawableBottom="@drawable/example_drawable"
        app:drawableBottomCompat="@drawable/example_drawable"
        android:src="@drawable/example_drawable"
        app:srcCompat="@drawable/example_drawable"
      """.trimIndent()
    val fileContainsSupportLibraryImport = "test_layout.xml"
    tempFolder.newFile("testfiles/$fileContainsSupportLibraryImport").writeText(prohibitedContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        $fileContainsSupportLibraryImport:1: $nonCompatDrawableUsedErrorMessage
        $fileContainsSupportLibraryImport:3: $nonCompatDrawableUsedErrorMessage
        $fileContainsSupportLibraryImport:5: $nonCompatDrawableUsedErrorMessage
        $fileContainsSupportLibraryImport:7: $nonCompatDrawableUsedErrorMessage
        $fileContainsSupportLibraryImport:9: $nonCompatDrawableUsedErrorMessage
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_java8OptionalImport_fileContentIsNotCorrect() {
    val prohibitedContent = "import java.util.Optional"
    tempFolder.newFolder("testfiles", "data", "src", "main")
    val stringFilePath = "data/src/main/SomeController.kt"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        $stringFilePath:1: $useJava8OptionalErrorMessage
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_buildFileUsesProtoLibrary_fileContentIsNotCorrect() {
    val prohibitedContent = "proto_library("
    tempFolder.newFolder("testfiles", "domain", "src", "main")
    val stringFilePath = "domain/src/main/BUILD"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        $stringFilePath:1: $doNotUseProtoLibrary
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_buildBazelFileUsesProtoLibrary_fileContentIsNotCorrect() {
    val prohibitedContent = "proto_library("
    tempFolder.newFolder("testfiles", "domain", "src", "main")
    val stringFilePath = "domain/src/main/BUILD.bazel"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        $stringFilePath:1: $doNotUseProtoLibrary
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
      File name/path violation: $activitiesPlacementErrorMessage
      - data/src/main/TestActivity.kt

      data/src/main/TestActivity.kt:1: $supportLibraryUsageErrorMessage
      $wikiReferenceNote
      """.trimIndent()
    )
  }

  /** Runs the regex_pattern_validation_check. */
  private fun runScript() {
    main(File(tempFolder.root, "testfiles").absolutePath)
  }

  private companion object {
    private const val REGEX_CHECK_PASSED_OUTPUT_INDICATOR: String = "REGEX PATTERN CHECKS PASSED"
    private const val REGEX_CHECK_FAILED_OUTPUT_INDICATOR: String = "REGEX PATTERN CHECKS FAILED"
  }
}
