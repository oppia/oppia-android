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
      "mess up & lead to a hanging ListenableFuture. If using a Deferred, convert it to a " +
      "ListenableFuture using asListenableFuture()."
  private val androidLayoutIncludeTagErrorMessage =
    "Remove <include .../> tag from layouts and instead use the widget directly, e.g. AppBarLayout."
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
  private val subclassedInjectableAppCompatActivityErrorMessage =
    "Never subclass InjectableAppCompatActivity directly. Instead, use " +
      "InjectableSystemLocalizedAppCompatActivity or InjectableAutoLocalizedAppCompatActivity."
  private val subclassedDialogFragmentErrorMessage =
    "DialogFragment should never be subclassed. Use InjectableDialogFragment, instead."
  private val androidActivityConfigChangesErrorMessage =
    "Never explicitly handle configuration changes. Instead, use saved instance states for" +
      " retaining state across rotations. For other types of configuration changes, follow up" +
      " with the developer mailing list with how to proceed if you think this is a legitimate case."
  private val androidManifestFirebaseAnalyticsEnabledErrorMessage =
    "Firebase analytics collection should always be explicitly deactivated in develop."
  private val androidManifestFirebaseCrashlyticsEnabledErrorMessage =
    "Firebase crashlytics collection should always be explicitly deactivated in develop."
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
  private val screenNameNotPresentErrorMessage =
    "Please add a Screen Name for this activity. To do this, add a value in the ScreenName enum " +
      "of screens.proto and add that name to your activity using " +
      "Intent.decorateWithScreenName(value) on the activity creation intent."
  private val screenNameTestNotPresentErrorMessage = "You've not added a test for verifying the " +
    "presence of a screen name for this activity. To do this, add a test named " +
    "testActivity_createIntent_verifyScreenNameInIntent and verify that an appropriate screen " +
    "name has been added to the activity's intent."
  private val doNotUseProtoLibrary = "Don't use proto_library. Use oppia_proto_library instead."
  private val parameterizedTestRunnerRequiresException =
    "To use OppiaParameterizedTestRunner, please add an exemption to" +
      " file_content_validation_checks.textproto and add an explanation for your use case in your" +
      " PR description. Note that parameterized tests should only be used in special" +
      " circumstances where a single behavior can be tested across multiple inputs, or for" +
      " especially large test suites that can be trivially reduced."
  private val doNotUseClipboardManager =
    "Don't use Android's ClipboardManager directly. Instead, use ClipboardController."
  private val doesNotHaveColorSuffixOrSnakeCasing =
    "All color declarations in component_color.xml and color_palette.xml should end with _color" +
      " suffix following snake_case naming convention."
  private val hasColorKeywordOrNoSnakeCasing =
    "All color declarations in color_defs.xml should be named using snake_case convention and" +
      " not contain color keyword."
  private val hasHexColorValue =
    "Hex color declarations should only be in color_defs.xml and not in component_colors.xml" +
      " or color_palette.xml"
  private val doesNotHaveRawColorDeclaration =
    "color_defs.xml should only have raw hex color declarations."
  private val doesNotStartWithComponentColor =
    "All colors in component_colors.xml must start with 'component_color_'."
  private val doesNotStartWithColorPalette =
    "All colors in color_palette.xml must start with 'color_palette_'."
  private val doesNotStartWithColorDefs =
    "All colors in color_defs.xml must start with 'color_def_'."
  private val doesNotReferenceColorFromColorPalette =
    "Only colors from color_palette.xml may be used in component_colors.xml."
  private val doesNotReferenceColorFromColorDefs =
    "Only colors from color_defs.xml may be used in color_palette.xml."
  private val doesNotReferenceColorFromComponentColorInLayouts =
    "Only colors from component_colors.xml may be used in layouts."
  private val doesNotReferenceColorFromComponentColorInDrawables =
    "Only colors from component_colors.xml may be used in drawables except vector assets."
  private val doesNotReferenceColorFromComponentColorInKotlinFiles =
    "Only colors from component_colors.xml may be used in Kotlin Files (Activities, Fragments, " +
      "Views and Presenters)."
  private val doesNotUseWorkManagerGetInstance =
    "Use AnalyticsStartupListener to retrieve an instance of WorkManager rather than fetching one" +
      " using getInstance (as the latter may create a WorkManager if one isn't already present, " +
      "and the application may intend to disable it)."
  private val doesNotUsePostOrPostDelayed =
    "Prefer avoiding post() and postDelayed() methods as they can can lead to subtle and " +
      "difficult-to-debug crashes. Prefer using LifecycleSafeTimerFactory for most cases when " +
      "an operation needs to run at a future time. For cases when state needs to be synchronized " +
      "with a view, use doOnPreDraw or doOnLayout instead. For more context on the underlying " +
      "issue, see: https://betterprogramming.pub/stop-using-post-postdelayed-in-your" +
      "-android-views-9d1c8eeaadf2."
  private val badKdocShouldFitOnOneLine =
    "Badly formatted KDoc. KDocs should either fit entirely on one line, e.g. \"/** My KDoc. */\"" +
      " or on multiple lines with the \"/**\" by itself. See other KDocs in the codebase for" +
      " references."
  private val badSingleLineKdocShouldHaveSpacesAfterOpening =
    "Badly formatted KDoc. Single-line KDocs should have one space after the \"/**\" and no other" +
      " characters."
  private val badSingleLineKdocShouldHaveExactlyOneSpaceAfterOpening =
    "Badly formatted KDoc. Single-line KDocs should have exactly one space after the \"/**\"."
  private val badSingleLineKdocShouldHaveSpacesBeforeEnding =
    "Badly formatted KDoc. Single-line KDocs should always end with a single space before the" +
      " final \"*/\"."
  private val badSingleLineKdocShouldHaveExactlyOneSpaceBeforeEnding =
    "Badly formatted KDoc. Single-line KDocs should always end with exactly one space before the" +
      " final \"*/\"."
  private val badKdocOrBlockCommentShouldEndWithCorrectEnding =
    "Badly formatted KDoc or block comment. KDocs and block comments should only" +
      " end with \"*/\". Multiple asterisks or whitespace between asterisks are not allowed."
  private val badKdocParamsAndPropertiesShouldHaveNameFollowing =
    "Badly formatted KDoc param or property at-clause: the name of the parameter or property" +
      " should immediately follow the at-clause without any additional linking with brackets."
  private val badSingleLineKdocShouldEndWithPunctuation =
    "Badly formatted KDoc. Single-line KDocs should end with punctuation."
  private val activityTestRuleShouldNotBeUsed =
    "ActivityTestRule is deprecated since it operates test activities in sometimes unsafe" +
      " situations. Use ActivityScenario, instead."
  private val activityScenarioRuleShouldNotBeUsed =
    "ActivityScenarioRule can result in order dependence when static state leaks across tests" +
      " (such as static module variables), and can make staging much more difficult for platform" +
      " parameters. Use ActivityScenario directly, instead."
  private val referenceComputeIfAbsent =
    "computeIfAbsent won't desugar and requires Java 8 support (SDK 24+). Suggest using an atomic" +
      " Kotlin-specific solution, instead."
  private val cdataShouldNotBeUsed =
    "CDATA isn't handled by Translatewiki correctly. Use escaped HTML, instead."
  private val wikiReferenceNote =
    "Refer to https://github.com/oppia/oppia-android/wiki/Static-Analysis-Checks" +
      "#regexpatternvalidation-check for more details on how to fix this."

  @field:[Rule JvmField] val tempFolder = TemporaryFolder()

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
    val requiredContent = "decorateWithScreenName(TEST_ACTIVITY)"
    tempFolder.newFolder("testfiles", "app", "src", "main")
    val tempFile = tempFolder.newFile("testfiles/app/src/main/TestActivity.kt")
    tempFile.writeText(requiredContent)

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
    val requiredContent = "decorateWithScreenName(TEST_ACTIVITY)"
    tempFolder.newFolder("testfiles", "data", "src", "main")
    val tempFile = tempFolder.newFile("testfiles/data/src/main/TestActivity.kt")
    tempFile.writeText(requiredContent)

    val exception = assertThrows<Exception>() { runScript() }

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

    val exception = assertThrows<Exception>() { runScript() }

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

    val exception = assertThrows<Exception>() { runScript() }

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

    val exception = assertThrows<Exception>() { runScript() }

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

    val exception = assertThrows<Exception>() { runScript() }

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

    val exception = assertThrows<Exception>() { runScript() }

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

    val exception = assertThrows<Exception>() { runScript() }

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

    val exception = assertThrows<Exception>() { runScript() }

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
  fun testFileContent_androidLayoutIncludeTag_fileContentIsNotCorrect() {
    val prohibitedContent = "<include"
    val fileContainsSupportLibraryImport = tempFolder.newFile("testfiles/test_layout.xml")
    fileContainsSupportLibraryImport.writeText(prohibitedContent)

    val exception = assertThrows<Exception>() { runScript() }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        test_layout.xml:1: $androidLayoutIncludeTagErrorMessage
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_androidGravityLeft_fileContentIsNotCorrect() {
    val prohibitedContent = "android:gravity=\"left\""
    val fileContainsSupportLibraryImport = tempFolder.newFile("testfiles/test_layout.xml")
    fileContainsSupportLibraryImport.writeText(prohibitedContent)

    val exception = assertThrows<Exception>() { runScript() }

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

    val exception = assertThrows<Exception>() { runScript() }

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

    val exception = assertThrows<Exception>() { runScript() }

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

    val exception = assertThrows<Exception>() { runScript() }

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

    val exception = assertThrows<Exception>() { runScript() }

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

    val exception = assertThrows<Exception>() { runScript() }

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

    val exception = assertThrows<Exception>() { runScript() }

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

    val exception = assertThrows<Exception>() { runScript() }

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

    val exception = assertThrows<Exception>() { runScript() }

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

    val exception = assertThrows<Exception>() { runScript() }

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

    val exception = assertThrows<Exception>() { runScript() }

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

    val exception = assertThrows<Exception>() { runScript() }

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

    val exception = assertThrows<Exception>() { runScript() }

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

    val exception = assertThrows<Exception>() { runScript() }

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

    val exception = assertThrows<Exception>() { runScript() }

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

    val exception = assertThrows<Exception>() { runScript() }

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

    val exception = assertThrows<Exception>() { runScript() }

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

    val exception = assertThrows<Exception>() { runScript() }

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

    val exception = assertThrows<Exception>() { runScript() }

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

    val exception = assertThrows<Exception>() { runScript() }

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

    val exception = assertThrows<Exception>() { runScript() }

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

    val exception = assertThrows<Exception>() { runScript() }

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

    val exception = assertThrows<Exception>() { runScript() }

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

    val exception = assertThrows<Exception>() { runScript() }

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

    val exception = assertThrows<Exception>() { runScript() }

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

    val exception = assertThrows<Exception>() { runScript() }

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

    val exception = assertThrows<Exception>() { runScript() }

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

    val exception = assertThrows<Exception>() { runScript() }

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

    val exception = assertThrows<Exception>() { runScript() }

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

    val exception = assertThrows<Exception>() { runScript() }

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

    val exception = assertThrows<Exception>() { runScript() }

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

    val exception = assertThrows<Exception>() { runScript() }

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

    val exception = assertThrows<Exception>() { runScript() }

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

    val exception = assertThrows<Exception>() { runScript() }

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

    val exception = assertThrows<Exception>() { runScript() }

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

    val exception = assertThrows<Exception>() { runScript() }

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

    val exception = assertThrows<Exception>() { runScript() }

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

    val exception = assertThrows<Exception>() { runScript() }

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

    val exception = assertThrows<Exception>() { runScript() }

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

    val exception = assertThrows<Exception>() { runScript() }

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

    val exception = assertThrows<Exception>() { runScript() }

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
    val requiredContent = "decorateWithScreenName(TEST_ACTIVITY)"
    val prohibitedContent = "class SomeActivity: Activity() {}"
    tempFolder.newFolder("testfiles", "app", "src", "main")
    val stringFilePath = "app/src/main/SomeActivity.kt"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent + requiredContent)

    val exception = assertThrows<Exception>() { runScript() }

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
    val requiredContent = "decorateWithScreenName(TEST_ACTIVITY)"
    val prohibitedContent = "class SomeActivity: AppCompatActivity() {}"
    tempFolder.newFolder("testfiles", "app", "src", "main")
    val stringFilePath = "app/src/main/SomeActivity.kt"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent + requiredContent)

    val exception = assertThrows<Exception>() { runScript() }

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
  fun testFileContent_subclassedInjectableAppCompatActivity_fileContentIsNotCorrect() {
    val requiredContent = "decorateWithScreenName(TEST_ACTIVITY)"
    val prohibitedContent = "class SomeActivity: InjectableAppCompatActivity() {}"
    tempFolder.newFolder("testfiles", "app", "src", "main")
    val stringFilePath = "app/src/main/SomeActivity.kt"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent + requiredContent)

    val exception = assertThrows<Exception>() { runScript() }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        $stringFilePath:1: $subclassedInjectableAppCompatActivityErrorMessage
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

    val exception = assertThrows<Exception>() { runScript() }

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
            <meta-data android:name="firebase_crashlytics_collection_enabled" android:value="false" />
            <meta-data android:name="firebase_analytics_collection_deactivated" android:value="true" />
            <activity
              android:name=".app.ExampleActivity"
              android:configChanges="orientation" />
          </application>
        </manifest>
      """.trimIndent()
    tempFolder.newFolder("testfiles", "app", "src", "main")
    val stringFilePath = "app/src/main/AndroidManifest.xml"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    val exception = assertThrows<Exception>() { runScript() }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        $stringFilePath:8: $androidActivityConfigChangesErrorMessage
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_manifestWithFirebaseCrashlyticsEnabled_fileContentIsNotCorrect() {
    val prohibitedContent =
      """
        <?xml version="1.0" encoding="utf-8"?>
        <manifest package="org.oppia.android">
          <application android:name=".app.application.OppiaApplication">
            <meta-data android:name="firebase_crashlytics_collection_enabled" android:value="true" />
            <meta-data android:name="firebase_analytics_collection_deactivated" android:value="true" />
          </application>
        </manifest>
      """.trimIndent()
    tempFolder.newFolder("testfiles", "app", "src", "main")
    val stringFilePath = "app/src/main/AndroidManifest.xml"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    val exception = assertThrows<Exception>() { runScript() }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        $stringFilePath: $androidManifestFirebaseCrashlyticsEnabledErrorMessage
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_manifestWithFirebaseAnalyticsEnabled_fileContentIsNotCorrect() {
    val prohibitedContent =
      """
        <?xml version="1.0" encoding="utf-8"?>
        <manifest package="org.oppia.android">
          <application android:name=".app.application.OppiaApplication">
            <meta-data android:name="firebase_crashlytics_collection_enabled" android:value="false" />
            <meta-data android:name="firebase_analytics_collection_deactivated" android:value="false" />
          </application>
        </manifest>
      """.trimIndent()
    tempFolder.newFolder("testfiles", "app", "src", "main")
    val stringFilePath = "app/src/main/AndroidManifest.xml"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    val exception = assertThrows<Exception>() { runScript() }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        $stringFilePath: $androidManifestFirebaseAnalyticsEnabledErrorMessage
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

    val exception = assertThrows<Exception>() { runScript() }

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

    val exception = assertThrows<Exception>() { runScript() }

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

    val exception = assertThrows<Exception>() { runScript() }

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

    val exception = assertThrows<Exception>() { runScript() }

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

    val exception = assertThrows<Exception>() { runScript() }

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

    val exception = assertThrows<Exception>() { runScript() }

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
  fun testFileContent_kotlinTestUsesParameterizedTestRunner_fileContentIsNotCorrect() {
    val prohibitedContent =
      """
      import org.oppia.android.testing.junit.OppiaParameterizedTestRunner
      @RunWith(OppiaParameterizedTestRunner::class)
      """.trimIndent()
    tempFolder.newFolder("testfiles", "domain", "src", "test")
    val stringFilePath = "domain/src/test/SomeTest.kt"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    val exception = assertThrows<Exception>() { runScript() }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        $stringFilePath:1: $parameterizedTestRunnerRequiresException
        $stringFilePath:2: $parameterizedTestRunnerRequiresException
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

    val exception = assertThrows<Exception>() { runScript() }

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

    val exception = assertThrows<Exception>() { runScript() }

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

    val exception = assertThrows<Exception>() { runScript() }

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
  fun testFileContent_clipboardManagerImport_fileContentIsNotCorrect() {
    val prohibitedContent = "import android.content.ClipboardManager"
    tempFolder.newFolder("testfiles", "domain", "src", "main")
    val stringFilePath = "domain/src/main/SomeController.kt"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    val exception = assertThrows<Exception>() { runScript() }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        $stringFilePath:1: $doNotUseClipboardManager
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFilenameAndContent_useProhibitedFileName_useProhibitedFileContent_multipleFailures() {
    val requiredContent = "decorateWithScreenName(TEST_ACTIVITY)"
    val prohibitedContent = "import android.support.v7.app"
    tempFolder.newFolder("testfiles", "data", "src", "main")
    val prohibitedFile = tempFolder.newFile("testfiles/data/src/main/TestActivity.kt")
    prohibitedFile.writeText(prohibitedContent + requiredContent)

    val exception = assertThrows<Exception>() { runScript() }

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

  @Test
  fun testFileContent_colorPalette_doesNotHaveColorSuffixOrSnakeCasing_fileContentIsNotCorrect() {
    val prohibitedContent =
      """
      <color name="color_palette_background_color">@color/color_def_oppia_light_yellow</color>
      <color name="color_palette_darkBackgroundColor">@color/color_def_mid_grey_30</color>
      <color name="color_palette_description_text_color">@color/color_def_accessible_light_grey</color>
      <color name="color_palette_text_input_background">@color/color_def_white</color>
      <color name="color_palette_DarkText">@color/color_def_black_87</color>
      <color name="color_palette_error_color">@color/color_def_oppia_red</color>
      <color name="color_palette_CONTAINER_BACKGROUND_COLOR">@color/color_def_white</color>
      <color name="color_palette_toolbar">@color/color_def_oppia_green</color>
      """.trimIndent()
    tempFolder.newFolder("testfiles", "app", "src", "main", "res", "values")
    val stringFilePath = "app/src/main/res/values/color_palette.xml"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    val exception = assertThrows<Exception>() { runScript() }

    // Verify that all patterns are properly detected & prohibited.
    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        $stringFilePath:2: $doesNotHaveColorSuffixOrSnakeCasing
        $stringFilePath:4: $doesNotHaveColorSuffixOrSnakeCasing
        $stringFilePath:5: $doesNotHaveColorSuffixOrSnakeCasing
        $stringFilePath:7: $doesNotHaveColorSuffixOrSnakeCasing
        $stringFilePath:8: $doesNotHaveColorSuffixOrSnakeCasing
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_colorPalette_hasColorSuffixAndSnakeCasing_fileContentIsCorrect() {
    val prohibitedContent =
      """
        <color name="color_palette_toolbar_color">@color/color_def_oppia_green</color>
        <color name="color_palette_status_bar_color">@color/color_def_dark_green</color>
        <color name="color_palette_action_bar_color">@color/color_def_oppia_green</color>
      """.trimIndent()
    tempFolder.newFolder("testfiles", "app", "src", "main", "res", "values")
    val stringFilePath = "app/src/main/res/values/color_palette.xml"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    runScript()

    assertThat(outContent.toString().trim()).isEqualTo(REGEX_CHECK_PASSED_OUTPUT_INDICATOR)
  }

  @Test
  fun testFileContent_colorDefs_hasColorKeywordOrNoSnakeCasing_fileContentIsNotCorrect() {
    val prohibitedContent =
      """
      <color name="color_def_oppia_metallic_blue_color">#2B5F73</color>
      <color name="color_def_oppia_light_black_color">#24282B</color>
      <color name="color_def_oppiaDarkGrey">#4D4D4D</color>
      <color name="color_def_oppia_pink">#FF938F</color>
      <color name="color_def_oppia_grayish_black_color">#32363B</color>
      """.trimIndent()
    tempFolder.newFolder("testfiles", "app", "src", "main", "res", "values")
    val stringFilePath = "app/src/main/res/values/color_defs.xml"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    val exception = assertThrows<Exception>() { runScript() }

    // Verify that all patterns are properly detected & prohibited.
    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        $stringFilePath:1: $hasColorKeywordOrNoSnakeCasing
        $stringFilePath:2: $hasColorKeywordOrNoSnakeCasing
        $stringFilePath:3: $hasColorKeywordOrNoSnakeCasing
        $stringFilePath:5: $hasColorKeywordOrNoSnakeCasing
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_colorDefs_doesNotHaveColorKeywordHasSnakeCasing_fileContentIsCorrect() {
    val prohibitedContent =
      """
        <color name="color_def_oppia_dark_grey">#4D4D4D</color>
        <color name="color_def_oppia_pink">#FF938F</color>
        <color name="color_def_oppia_grayish_black">#32363B</color>
      """.trimIndent()
    tempFolder.newFolder("testfiles", "app", "src", "main", "res", "values")
    val stringFilePath = "app/src/main/res/values/color_defs.xml"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    runScript()

    assertThat(outContent.toString().trim()).isEqualTo(REGEX_CHECK_PASSED_OUTPUT_INDICATOR)
  }

  @Test
  fun testFileContent_componentColors_hasHexColorValue_fileContentIsNotCorrect() {
    val prohibitedContent =
      """
        <color name="component_color_admin_controls_options_highlighted_background_color">@color/color_palette_highlighted_background_color</color>
        <color name="component_color_admin_controls_sub_heading_color">#6B0086FB</color>
        <color name="component_color_admin_controls_switch_description_color">#FFFFFF</color>
        <color name="component_color_admin_controls_menu_options_text_color">@color/color_palette_dark_text_color</color>
      """.trimIndent()
    tempFolder.newFolder("testfiles", "app", "src", "main", "res", "values")
    val stringFilePath = "app/src/main/res/values/component_colors.xml"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    val exception = assertThrows<Exception>() { runScript() }

    // Verify that all patterns are properly detected & prohibited.
    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        $stringFilePath:2: $hasHexColorValue
        $stringFilePath:3: $hasHexColorValue
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_componentColors_doesNotHaveHexColorValue_fileContentIsCorrect() {
    val prohibitedContent =
      """
        <color name="component_color_shared_primary_dark_text_color">@color/color_palette_dark_text_color</color>
        <color name="component_color_add_profile_activity_switch_description_color">@color/color_palette_description_text_color</color>
        <color name="component_color_add_profile_activity_layout_background_color">@color/color_palette_background_color</color>
      """.trimIndent()
    tempFolder.newFolder("testfiles", "app", "src", "main", "res", "values")
    val stringFilePath = "app/src/main/res/values/component_colors.xml"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    runScript()

    assertThat(outContent.toString().trim()).isEqualTo(REGEX_CHECK_PASSED_OUTPUT_INDICATOR)
  }

  @Test
  fun testFileContent_colorDefs_doesNotHaveRawColorDeclaration_fileContentIsNotCorrect() {
    val prohibitedContent =
      """
       <color name="color_def_oppia_metallic_blue">@color/color_name</color>
       <color name="color_def_oppia_light_black">#24282B</color>
       <color name="color_def_oppia_dark_grey">#4D4D4D</color>
       <color name="color_def_oppia_pink">@color/another_color_name</color>
       <color name="color_def_oppia_grayish_black">#32363B</color>
      """.trimIndent()
    tempFolder.newFolder("testfiles", "app", "src", "main", "res", "values")
    val stringFilePath = "app/src/main/res/values/color_defs.xml"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    val exception = assertThrows<Exception>() { runScript() }

    // Verify that all patterns are properly detected & prohibited.
    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        $stringFilePath:1: $doesNotHaveRawColorDeclaration
        $stringFilePath:4: $doesNotHaveRawColorDeclaration
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_colorDefs_hasRawColorDeclaration_fileContentIsCorrect() {
    val prohibitedContent =
      """
        <color name="color_def_oppia_silver">#C4C4C4</color>
        <color name="color_def_oppia_turquoise">#3bd1c4</color>
        <color name="color_def_oppia_bangladesh_green">#03635B</color>
      """.trimIndent()
    tempFolder.newFolder("testfiles", "app", "src", "main", "res", "values")
    val stringFilePath = "app/src/main/res/values/color_defs.xml"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    runScript()

    assertThat(outContent.toString().trim()).isEqualTo(REGEX_CHECK_PASSED_OUTPUT_INDICATOR)
  }

  @Test
  fun testFileContent_componentColors_startsWithComponentColors_fileContentIsCorrect() {
    val prohibitedContent =
      """
        <color name="component_color_shared_text_view_heading_text_color">@color/color_palette_highlighted_text_color</color>
        <color name="component_color_shared_text_input_layout_text_color">@color/color_palette_primary_text_color</color>
        <color name="component_color_shared_text_input_layout_stroke_color">@color/color_palette_primary_text_color</color>
        <color name="component_color_shared_text_input_edit_text_text_color">@color/color_palette_primary_text_color</color>
      """.trimIndent()
    tempFolder.newFolder("testfiles", "app", "src", "main", "res", "values")
    val stringFilePath = "app/src/main/res/values/component_colors.xml"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    runScript()

    assertThat(outContent.toString().trim()).isEqualTo(REGEX_CHECK_PASSED_OUTPUT_INDICATOR)
  }

  @Test
  fun testFileContent_componentColors_doesNotStartWithComponentColors_fileContentIsNotCorrect() {
    val prohibitedContent =
      """
        <color name="shared_text_view_heading_text_color">@color/color_palette_highlighted_text_color</color>
        <color name="shared_text_input_layout_text_color">@color/color_palette_primary_text_color</color>
        <color name="component_color_shared_text_input_layout_stroke_color">@color/color_palette_primary_text_color</color>
        <color name="shared_text_input_edit_text_text_color">@color/color_palette_primary_text_color</color>
      """.trimIndent()
    tempFolder.newFolder("testfiles", "app", "src", "main", "res", "values")
    val stringFilePath = "app/src/main/res/values/component_colors.xml"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    val exception = assertThrows<Exception>() { runScript() }

    // Verify that all patterns are properly detected & prohibited.
    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        $stringFilePath:1: $doesNotStartWithComponentColor
        $stringFilePath:2: $doesNotStartWithComponentColor
        $stringFilePath:4: $doesNotStartWithComponentColor
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_colorPalette_startsWithColorPalette_fileContentIsCorrect() {
    val prohibitedContent =
      """
        <color name="color_palette_primary_color">@color/color_def_oppia_green</color>
        <color name="color_palette_primary_dark_color">@color/color_def_dark_green</color>
        <color name="color_palette_accent_color">@color/color_def_oppia_dark_blue</color>
        <color name="color_palette_primary_text_color">@color/color_def_accessible_grey</color>
      """.trimIndent()
    tempFolder.newFolder("testfiles", "app", "src", "main", "res", "values")
    val stringFilePath = "app/src/main/res/values/color_palette.xml"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    runScript()

    assertThat(outContent.toString().trim()).isEqualTo(REGEX_CHECK_PASSED_OUTPUT_INDICATOR)
  }

  @Test
  fun testFileContent_colorPalette_doesNotStartWithColorPalette_fileContentIsNotCorrect() {
    val prohibitedContent =
      """
        <color name="primary_color">@color/color_def_oppia_green</color>
        <color name="color_palette_primary_dark_color">@color/color_def_dark_green</color>
        <color name="color_palette_accent_color">@color/color_def_oppia_dark_blue</color>
        <color name="primary_text_color">@color/color_def_accessible_grey</color>
      """.trimIndent()
    tempFolder.newFolder("testfiles", "app", "src", "main", "res", "values")
    val stringFilePath = "app/src/main/res/values/color_palette.xml"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    val exception = assertThrows<Exception>() { runScript() }

    // Verify that all patterns are properly detected & prohibited.
    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        $stringFilePath:1: $doesNotStartWithColorPalette
        $stringFilePath:4: $doesNotStartWithColorPalette
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_colorDefs_startsWithColorDefs_fileContentIsCorrect() {
    val prohibitedContent =
      """
        <color name="color_def_oppia_green">#00645C</color>
        <color name="color_def_dark_green">#003933</color>
        <color name="color_def_oppia_light_green">#F0FFFF</color>
      """.trimIndent()
    tempFolder.newFolder("testfiles", "app", "src", "main", "res", "values")
    val stringFilePath = "app/src/main/res/values/color_defs.xml"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    runScript()

    assertThat(outContent.toString().trim()).isEqualTo(REGEX_CHECK_PASSED_OUTPUT_INDICATOR)
  }

  @Test
  fun testFileContent_colorDefs_doesNotStartWithColorDef_fileContentIsNotCorrect() {
    val prohibitedContent =
      """
        <color name="color_def_oppia_green">#00645C</color>
        <color name="color_def_dark_green">#003933</color>
        <color name="oppia_dark_blue">#2D4A9D</color>
        <color name="color_def_oppia_light_green">#F0FFFF</color>
        <color name="oppia_light_yellow">#FFFFF0</color>
      """.trimIndent()
    tempFolder.newFolder("testfiles", "app", "src", "main", "res", "values")
    val stringFilePath = "app/src/main/res/values/color_defs.xml"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    val exception = assertThrows<Exception>() { runScript() }

    // Verify that all patterns are properly detected & prohibited.
    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        $stringFilePath:3: $doesNotStartWithColorDefs
        $stringFilePath:5: $doesNotStartWithColorDefs
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_componentColors_referencesColorFromColorPalette_fileContentIsCorrect() {
    val prohibitedContent =
      """
        <color name="component_color_admin_auth_secondary_text_color">@color/color_palette_description_text_color</color>
        <color name="component_color_admin_auth_layout_background_color">@color/color_palette_background_color</color>
        <color name="component_color_admin_auth_activity_toolbar_color">@color/color_palette_toolbar_color</color>
      """.trimIndent()
    tempFolder.newFolder("testfiles", "app", "src", "main", "res", "values")
    val stringFilePath = "app/src/main/res/values/component_colors.xml"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    runScript()

    assertThat(outContent.toString().trim()).isEqualTo(REGEX_CHECK_PASSED_OUTPUT_INDICATOR)
  }

  @Test
  fun testFileContent_componentColors_includesNonPaletteColorReferences_fileContentIsNotCorrect() {
    val prohibitedContent =
      """
        <color name="component_color_admin_auth_secondary_text_color">@color/color_palette_description_text_color</color>
        <color name="component_color_admin_auth_layout_background_color">@color/color_defs_background_color</color>
        <color name="component_color_admin_auth_activity_toolbar_color">@color/color_palette_toolbar_color</color>
      """.trimIndent()
    tempFolder.newFolder("testfiles", "app", "src", "main", "res", "values")
    val stringFilePath = "app/src/main/res/values/component_colors.xml"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    val exception = assertThrows<Exception>() { runScript() }

    // Verify that all patterns are properly detected & prohibited.
    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        $stringFilePath:2: $doesNotReferenceColorFromColorPalette
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_xmlLayouts_includesNonColorComponentReferences_fileContentIsNotCorrect() {
    val prohibitedContent =
      """
        android:textColor="@color/component_color_shared_primary_text_color"
        android:textColor="@color/color_defs_shared_primary_text_color"
        android:textColor="@color/color_palette_primary_text_color"
        android:textColor="#003933"
        android:background="@color/component_color_shared_primary_text_color"
        android:background="@color/color_defs_shared_primary_text_color"
        android:background="@color/color_palette_primary_text_color"
        android:background="#003933"
        app:tint="@color/component_color_shared_primary_text_color"
        app:tint="@color/color_defs_shared_primary_text_color"
        app:tint="@color/color_palette_primary_text_color"
        app:tint="#003933"
        app:strokeColor="@color/component_color_shared_primary_text_color"
        app:strokeColor="@color/color_defs_shared_primary_text_color"
        app:strokeColor="@color/color_palette_primary_text_color"
        app:strokeColor="#003933"
        app:cardBackgroundColor="@color/component_color_shared_primary_text_color"
        app:cardBackgroundColor="@color/color_defs_shared_primary_text_color"
        app:cardBackgroundColor="@color/color_palette_primary_text_color"
        app:cardBackgroundColor="#003933"
        android:background="@color/component_color_shared_primary_text_color"
        android:background="@color/color_defs_shared_primary_text_color"
        android:background="@color/color_palette_primary_text_color"
        android:background="#003933"
      """.trimIndent()
    tempFolder.newFolder("testfiles", "app", "src", "main", "res", "layout")
    val stringFilePath = "app/src/main/res/layout/test_layout.xml"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    val exception = assertThrows<Exception>() { runScript() }

    // Verify that all patterns are properly detected & prohibited.
    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        $stringFilePath:2: $doesNotReferenceColorFromComponentColorInLayouts
        $stringFilePath:3: $doesNotReferenceColorFromComponentColorInLayouts
        $stringFilePath:4: $doesNotReferenceColorFromComponentColorInLayouts
        $stringFilePath:6: $doesNotReferenceColorFromComponentColorInLayouts
        $stringFilePath:7: $doesNotReferenceColorFromComponentColorInLayouts
        $stringFilePath:8: $doesNotReferenceColorFromComponentColorInLayouts
        $stringFilePath:10: $doesNotReferenceColorFromComponentColorInLayouts
        $stringFilePath:11: $doesNotReferenceColorFromComponentColorInLayouts
        $stringFilePath:12: $doesNotReferenceColorFromComponentColorInLayouts
        $stringFilePath:14: $doesNotReferenceColorFromComponentColorInLayouts
        $stringFilePath:15: $doesNotReferenceColorFromComponentColorInLayouts
        $stringFilePath:16: $doesNotReferenceColorFromComponentColorInLayouts
        $stringFilePath:18: $doesNotReferenceColorFromComponentColorInLayouts
        $stringFilePath:19: $doesNotReferenceColorFromComponentColorInLayouts
        $stringFilePath:20: $doesNotReferenceColorFromComponentColorInLayouts
        $stringFilePath:22: $doesNotReferenceColorFromComponentColorInLayouts
        $stringFilePath:23: $doesNotReferenceColorFromComponentColorInLayouts
        $stringFilePath:24: $doesNotReferenceColorFromComponentColorInLayouts
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_xmlDrawables_includesNonColorComponentReferences_fileContentIsNotCorrect() {
    val prohibitedContent =
      """
        android:color="@color/component_color_shared_primary_text_color"
        android:color="@color/color_defs_shared_primary_text_color"
        android:color="@color/color_palette_primary_text_color"
        android:color="#003933"
      """.trimIndent()
    tempFolder.newFolder("testfiles", "app", "src", "main", "res", "drawable")
    val stringFilePath = "app/src/main/res/drawable/test_layout.xml"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    val exception = assertThrows<Exception>() { runScript() }

    // Verify that all patterns are properly detected & prohibited.
    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        $stringFilePath:2: $doesNotReferenceColorFromComponentColorInDrawables
        $stringFilePath:3: $doesNotReferenceColorFromComponentColorInDrawables
        $stringFilePath:4: $doesNotReferenceColorFromComponentColorInDrawables
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_kotlinFiles_includesNonColorComponentReferences_fileContentIsNotCorrect() {
    val prohibitedContent =
      """
        decorateWithScreenName(HOME_ACTIVITY)
        R.color.component_color_shared_activity_status_bar_color
        R.color.color_def_avatar_background_1
        R.color.color_palette_primary_color
      """.trimIndent()

    tempFolder.newFolder(
      "testfiles",
      "app",
      "src",
      "main",
      "java",
      "org",
      "oppia",
      "android",
      "app"
    )

    val stringFilePath1 = "app/src/main/java/org/oppia/android/app/HomeActivity.kt"
    val stringFilePath2 = "app/src/main/java/org/oppia/android/app/TestFileActivityPresenter.kt"
    val stringFilePath3 = "app/src/main/java/org/oppia/android/app/TestFileFragment.kt"
    val stringFilePath4 = "app/src/main/java/org/oppia/android/app/TestFileFragmentPresenter.kt"
    val stringFilePath5 = "app/src/main/java/org/oppia/android/app/TestFileView.kt"
    val stringFilePath6 = "app/src/main/java/org/oppia/android/app/TestFileViewPresenter.kt"

    tempFolder.newFile("testfiles/$stringFilePath1").writeText(prohibitedContent)
    tempFolder.newFile("testfiles/$stringFilePath2").writeText(prohibitedContent)
    tempFolder.newFile("testfiles/$stringFilePath3").writeText(prohibitedContent)
    tempFolder.newFile("testfiles/$stringFilePath4").writeText(prohibitedContent)
    tempFolder.newFile("testfiles/$stringFilePath5").writeText(prohibitedContent)
    tempFolder.newFile("testfiles/$stringFilePath6").writeText(prohibitedContent)

    val exception = assertThrows<Exception>() { runScript() }

    // Verify that all patterns are properly detected & prohibited.
    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        $stringFilePath1:3: $doesNotReferenceColorFromComponentColorInKotlinFiles
        $stringFilePath1:4: $doesNotReferenceColorFromComponentColorInKotlinFiles
        $stringFilePath2:3: $doesNotReferenceColorFromComponentColorInKotlinFiles
        $stringFilePath2:4: $doesNotReferenceColorFromComponentColorInKotlinFiles
        $stringFilePath3:3: $doesNotReferenceColorFromComponentColorInKotlinFiles
        $stringFilePath3:4: $doesNotReferenceColorFromComponentColorInKotlinFiles
        $stringFilePath4:3: $doesNotReferenceColorFromComponentColorInKotlinFiles
        $stringFilePath4:4: $doesNotReferenceColorFromComponentColorInKotlinFiles
        $stringFilePath5:3: $doesNotReferenceColorFromComponentColorInKotlinFiles
        $stringFilePath5:4: $doesNotReferenceColorFromComponentColorInKotlinFiles
        $stringFilePath6:3: $doesNotReferenceColorFromComponentColorInKotlinFiles
        $stringFilePath6:4: $doesNotReferenceColorFromComponentColorInKotlinFiles
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_colorPalette_referencesColorFromColorDefs_fileContentIsCorrect() {
    val prohibitedContent =
      """
        <color name="color_palette_text_input_background_color">@color/color_def_white</color>
        <color name="color_palette_dark_text_color">@color/color_def_black_87</color>
        <color name="color_palette_error_color">@color/color_def_oppia_red</color>
        <color name="color_palette_container_background_color">@color/color_def_white</color>
      """.trimIndent()
    tempFolder.newFolder("testfiles", "app", "src", "main", "res", "values")
    val stringFilePath = "app/src/main/res/values/color_palette.xml"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    runScript()

    assertThat(outContent.toString().trim()).isEqualTo(REGEX_CHECK_PASSED_OUTPUT_INDICATOR)
  }

  @Test
  fun testFileContent_colorPalette_doesNotReferenceColorFromColorDefs_fileContentIsNotCorrect() {
    val prohibitedContent =
      """
        <color name="color_palette_description_text_color">@color/color_def_accessible_light_grey</color>
        <color name="color_palette_text_input_background_color">@color/blue</color>
        <color name="color_palette_dark_text_color">@color/component_color_black_87</color>
        <color name="color_palette_error_color">@color/color_def_oppia_red</color>
      """.trimIndent()
    tempFolder.newFolder("testfiles", "app", "src", "main", "res", "values")
    val stringFilePath = "app/src/main/res/values/color_palette.xml"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    val exception = assertThrows<Exception>() { runScript() }

    // Verify that all patterns are properly detected & prohibited.
    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        $stringFilePath:2: $doesNotReferenceColorFromColorDefs
        $stringFilePath:3: $doesNotReferenceColorFromColorDefs
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testScreenNamePresence_emptyActivityFile_screenNameIsNotPresent() {
    tempFolder.newFolder("testfiles", "app", "src", "main", "activity")
    val stringFilePath = "app/src/main/activity/HomeActivity.kt"
    tempFolder.newFile("testfiles/$stringFilePath")

    val exception = assertThrows<Exception>() { runScript() }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        $stringFilePath: $screenNameNotPresentErrorMessage
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testScreenNamePresence_activityFileWithScreenName_screenNameIsPresent() {
    val requiredContent = "decorateWithScreenName(HOME_ACTIVITY)"
    tempFolder.newFolder("testfiles", "app", "src", "main", "activity")
    val stringFilePath = "app/src/main/activity/HomeActivity.kt"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(requiredContent)

    runScript()

    assertThat(outContent.toString().trim()).isEqualTo(REGEX_CHECK_PASSED_OUTPUT_INDICATOR)
  }

  @Test
  fun testScreenNameTestPresence_activityTestWithoutScreenNameTest_screenNameTestIsNotPresent() {
    tempFolder.newFolder("testfiles", "app", "src", "main", "activity")
    val stringFilePath = "app/src/main/activity/HomeActivityTest.kt"
    tempFolder.newFile("testfiles/$stringFilePath")

    val exception = assertThrows<Exception>() { runScript() }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        $stringFilePath: $screenNameTestNotPresentErrorMessage
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testScreenNameTestPresence_activityTestWithScreenNameTest_screenNameTestIsPresent() {
    val requiredContent = "testActivity_createIntent_verifyScreenNameInIntent()"
    tempFolder.newFolder("testfiles", "app", "src", "main")
    val stringFilePath = "app/src/main/HomeActivityTest.kt"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(requiredContent)

    runScript()

    assertThat(outContent.toString().trim()).isEqualTo(REGEX_CHECK_PASSED_OUTPUT_INDICATOR)
  }

  @Test
  fun testFileContent_referenceGetInstance_fileContentIsNotCorrect() {
    val prohibitedContent =
      """
        val workManager = WorkManager.getInstance(context)
      """.trimIndent()
    tempFolder.newFolder("testfiles", "app", "src", "main", "java", "org", "oppia", "android")
    val stringFilePath = "app/src/main/java/org/oppia/android/SomeInitializer.kt"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    val exception = assertThrows<Exception>() { runScript() }

    // Verify that all patterns are properly detected & prohibited.
    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        $stringFilePath:1: $doesNotUseWorkManagerGetInstance
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_postDelayedUsed_fileContentIsNotCorrect() {
    val prohibitedContent =
      """
        binding.view.postDelayed({ binding.view.visibility = View.GONE }, 1000)
      """.trimIndent()
    tempFolder.newFolder("testfiles", "app", "src", "main", "java", "org", "oppia", "android")
    val stringFilePath = "app/src/main/java/org/oppia/android/TestPresenter.kt"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    val exception = assertThrows<Exception>() { runScript() }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)

    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        $stringFilePath:1: $doesNotUsePostOrPostDelayed
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_postUsed_withParenthesis_fileContentIsNotCorrect() {
    val prohibitedContent =
      """
        binding.view.post({ binding.view.visibility = View.GONE })
      """.trimIndent()
    tempFolder.newFolder("testfiles", "app", "src", "main", "java", "org", "oppia", "android")
    val stringFilePath = "app/src/main/java/org/oppia/android/TestPresenter.kt"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    val exception = assertThrows<Exception>() { runScript() }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)

    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        $stringFilePath:1: $doesNotUsePostOrPostDelayed
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_postUsed_withCurlyBraces_fileContentIsNotCorrect() {
    val prohibitedContent =
      """
        binding.view.post { binding.view.visibility = View.GONE }
      """.trimIndent()
    tempFolder.newFolder("testfiles", "app", "src", "main", "java", "org", "oppia", "android")
    val stringFilePath = "app/src/main/java/org/oppia/android/TestPresenter.kt"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    val exception = assertThrows<Exception>() { runScript() }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)

    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        $stringFilePath:1: $doesNotUsePostOrPostDelayed
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_kdocNotFittingLineFormatting_fileContentIsNotCorrect() {
    val prohibitedContent =
      """
        /** Content here.
         */
        /** Correct KDoc. */
        /**
         * Correct KDoc.
         */
      """.trimIndent()
    tempFolder.newFolder("testfiles", "app", "src", "main", "java", "org", "oppia", "android")
    val stringFilePath = "app/src/main/java/org/oppia/android/TestPresenter.kt"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    val exception = assertThrows<Exception>() { runScript() }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        $stringFilePath:1: $badKdocShouldFitOnOneLine
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_singleLineKdocWithExtraCharactersAfterStart_fileContentIsNotCorrect() {
    val prohibitedContent =
      """
        /**Content here. */
        /*** Content here. */
        /** Correct KDoc. */
      """.trimIndent()
    tempFolder.newFolder("testfiles", "app", "src", "main", "java", "org", "oppia", "android")
    val stringFilePath = "app/src/main/java/org/oppia/android/TestPresenter.kt"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    val exception = assertThrows<Exception>() { runScript() }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        $stringFilePath:1: $badSingleLineKdocShouldHaveSpacesAfterOpening
        $stringFilePath:2: $badSingleLineKdocShouldHaveSpacesAfterOpening
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_singleLineKdocWithExtraSpacesAfterStart_fileContentIsNotCorrect() {
    val prohibitedContent =
      """
        /**  Content here. */
        /**   Content here. */
        /** Correct KDoc. */
      """.trimIndent()
    tempFolder.newFolder("testfiles", "app", "src", "main", "java", "org", "oppia", "android")
    val stringFilePath = "app/src/main/java/org/oppia/android/TestPresenter.kt"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    val exception = assertThrows<Exception>() { runScript() }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        $stringFilePath:1: $badSingleLineKdocShouldHaveExactlyOneSpaceAfterOpening
        $stringFilePath:2: $badSingleLineKdocShouldHaveExactlyOneSpaceAfterOpening
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_multipleCommentTypesWithExtraCharactersBeforeEnd_fileContentIsNotCorrect() {
    val prohibitedContent =
      """
        /** Content here.*/
        /** Content here. **/
        /** Correct KDoc. */

        /*
         * Incorrect block comment.
         **/
        /*
         * Correct block comment.
         */
        /**
         * Incorrect KDoc comment.
         **/
        /**
         * Correct KDoc comment.
         */
      """.trimIndent()
    tempFolder.newFolder("testfiles", "app", "src", "main", "java", "org", "oppia", "android")
    val stringFilePath = "app/src/main/java/org/oppia/android/TestPresenter.kt"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    val exception = assertThrows<Exception>() { runScript() }

    // Two patterns are combined in this check because they slightly overlap in affected cases (e.g.
    // line 2 fails due to three different checks), and one pattern is subequently needed for the
    // test to pass (punctuation).
    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        $stringFilePath:1: $badSingleLineKdocShouldHaveSpacesBeforeEnding
        $stringFilePath:2: $badSingleLineKdocShouldHaveSpacesBeforeEnding
        $stringFilePath:2: $badKdocOrBlockCommentShouldEndWithCorrectEnding
        $stringFilePath:7: $badKdocOrBlockCommentShouldEndWithCorrectEnding
        $stringFilePath:13: $badKdocOrBlockCommentShouldEndWithCorrectEnding
        $stringFilePath:2: $badSingleLineKdocShouldEndWithPunctuation
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_kdocWithInvalidEndingSequences_failsValidation() {
    val prohibitedContent =
      """
      /**
       * Incorrect KDoc comment.
       * */
       /**
       * Incorrect KDoc comment.
       **/
       /**
         * Correct KDoc comment.
         */
      """.trimIndent()
    tempFolder.newFolder("testfiles", "app", "src", "main", "java", "org", "oppia", "android")
    val stringFilePath = "app/src/main/java/org/oppia/android/TestPresenter.kt"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    val exception = assertThrows<Exception>() { runScript() }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
      $stringFilePath:3: $badKdocOrBlockCommentShouldEndWithCorrectEnding
      $stringFilePath:6: $badKdocOrBlockCommentShouldEndWithCorrectEnding
      $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_singleLineKdocWithExtraSpacesBeforeEnd_fileContentIsNotCorrect() {
    val prohibitedContent =
      """
        /** Content here.  */
        /** Content here.   */
        /** Correct KDoc. */
      """.trimIndent()
    tempFolder.newFolder("testfiles", "app", "src", "main", "java", "org", "oppia", "android")
    val stringFilePath = "app/src/main/java/org/oppia/android/TestPresenter.kt"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    val exception = assertThrows<Exception>() { runScript() }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        $stringFilePath:1: $badSingleLineKdocShouldHaveExactlyOneSpaceBeforeEnding
        $stringFilePath:2: $badSingleLineKdocShouldHaveExactlyOneSpaceBeforeEnding
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_kdocWithPropertiesAndParameters_withLinksAfter_fileContentIsNotCorrect() {
    val prohibitedContent =
      """
        /**
         * Summary fragment.
         *
         * @property [invalid] and explanation
         * @property valid and explanation
         * @param [invalid] and explanation
         * @param valid and explanation
         */
      """.trimIndent()
    tempFolder.newFolder("testfiles", "app", "src", "main", "java", "org", "oppia", "android")
    val stringFilePath = "app/src/main/java/org/oppia/android/TestPresenter.kt"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    val exception = assertThrows<Exception>() { runScript() }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        $stringFilePath:4: $badKdocParamsAndPropertiesShouldHaveNameFollowing
        $stringFilePath:6: $badKdocParamsAndPropertiesShouldHaveNameFollowing
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_singleLineKdocDoesNotEndWithPunctuation_fileContentIsNotCorrect() {
    val prohibitedContent =
      """
        /** Content here */
        /** Correct KDoc. */
        /** Correct KDoc! */
      """.trimIndent()
    tempFolder.newFolder("testfiles", "app", "src", "main", "java", "org", "oppia", "android")
    val stringFilePath = "app/src/main/java/org/oppia/android/TestPresenter.kt"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    val exception = assertThrows<Exception>() { runScript() }

    // 'Punctuation' currently assumes a period.
    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        $stringFilePath:1: $badSingleLineKdocShouldEndWithPunctuation
        $stringFilePath:3: $badSingleLineKdocShouldEndWithPunctuation
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_referencesActivityTestRule_fileContentIsNotCorrect() {
    val prohibitedContent =
      """
      import androidx.test.rule.ActivityTestRule
      """.trimIndent()
    tempFolder.newFolder("testfiles", "app", "src", "test", "java", "org", "oppia", "android")
    val stringFilePath = "app/src/test/java/org/oppia/android/PresenterTest.kt"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    val exception = assertThrows<Exception>() { runScript() }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        $stringFilePath:1: $activityTestRuleShouldNotBeUsed
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_referencesActivityScenarioRule_fileContentIsNotCorrect() {
    val prohibitedContent =
      """
      import androidx.test.ext.junit.rules.ActivityScenarioRule
      """.trimIndent()
    tempFolder.newFolder("testfiles", "app", "src", "test", "java", "org", "oppia", "android")
    val stringFilePath = "app/src/test/java/org/oppia/android/PresenterTest.kt"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    val exception = assertThrows<Exception>() { runScript() }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        $stringFilePath:1: $activityScenarioRuleShouldNotBeUsed
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_includesReferenceToComputeIfAbsent_fileContentIsNotCorrect() {
    val prohibitedContent =
      """
        someMap.computeIfAbsent(key) { createOtherValue() }
      """.trimIndent()
    tempFolder.newFolder("testfiles", "app", "src", "main", "java", "org", "oppia", "android")
    val stringFilePath = "app/src/main/java/org/oppia/android/TestPresenter.kt"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    val exception = assertThrows<Exception>() { runScript() }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        $stringFilePath:1: $referenceComputeIfAbsent
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFileContent_includesCdataContentInStringsXml_fileContentIsNotCorrect() {
    val prohibitedContent =
      """
      <string name="test"><![CDATA[<p>Some nested HTML.</p>]]></string>
      """.trimIndent()
    tempFolder.newFolder("testfiles", "app", "src", "main", "res", "values")
    val stringFilePath = "app/src/main/res/values/strings.xml"
    tempFolder.newFile("testfiles/$stringFilePath").writeText(prohibitedContent)

    val exception = assertThrows<Exception>() { runScript() }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        $stringFilePath:1: $cdataShouldNotBeUsed
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
