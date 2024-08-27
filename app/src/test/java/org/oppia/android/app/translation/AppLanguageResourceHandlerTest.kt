package org.oppia.android.app.translation

import android.app.Application
import android.content.Context
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.activity.ActivityComponentFactory
import org.oppia.android.app.activity.ActivityIntentFactoriesModule
import org.oppia.android.app.activity.route.ActivityRouterModule
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.application.testing.TestingBuildFlavorModule
import org.oppia.android.app.devoptions.DeveloperOptionsModule
import org.oppia.android.app.devoptions.DeveloperOptionsStarterModule
import org.oppia.android.app.model.AppLanguageSelection
import org.oppia.android.app.model.AudioLanguage
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.testing.activity.TestActivity
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
import org.oppia.android.data.backends.gae.NetworkConfigProdModule
import org.oppia.android.data.backends.gae.NetworkModule
import org.oppia.android.domain.classify.InteractionsModule
import org.oppia.android.domain.classify.rules.algebraicexpressioninput.AlgebraicExpressionInputModule
import org.oppia.android.domain.classify.rules.continueinteraction.ContinueModule
import org.oppia.android.domain.classify.rules.dragAndDropSortInput.DragDropSortInputModule
import org.oppia.android.domain.classify.rules.fractioninput.FractionInputModule
import org.oppia.android.domain.classify.rules.imageClickInput.ImageClickInputModule
import org.oppia.android.domain.classify.rules.itemselectioninput.ItemSelectionInputModule
import org.oppia.android.domain.classify.rules.mathequationinput.MathEquationInputModule
import org.oppia.android.domain.classify.rules.multiplechoiceinput.MultipleChoiceInputModule
import org.oppia.android.domain.classify.rules.numberwithunits.NumberWithUnitsRuleModule
import org.oppia.android.domain.classify.rules.numericexpressioninput.NumericExpressionInputModule
import org.oppia.android.domain.classify.rules.numericinput.NumericInputRuleModule
import org.oppia.android.domain.classify.rules.ratioinput.RatioInputModule
import org.oppia.android.domain.classify.rules.textinput.TextInputRuleModule
import org.oppia.android.domain.exploration.ExplorationProgressModule
import org.oppia.android.domain.exploration.ExplorationStorageModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionProdModule
import org.oppia.android.domain.onboarding.testing.ExpirationMetaDataRetrieverTestModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.oppialogger.analytics.CpuPerformanceSnapshotterModule
import org.oppia.android.domain.oppialogger.logscheduler.MetricLogSchedulerModule
import org.oppia.android.domain.oppialogger.loguploader.LogReportWorkerModule
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.assertThrows
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.Iteration
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.Parameter
import org.oppia.android.testing.junit.OppiaParameterizedTestRunner.SelectRunnerPlatform
import org.oppia.android.testing.junit.ParameterizedRobolectricTestRunner
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.locale.testing.LocaleTestModule
import org.oppia.android.util.locale.testing.TestOppiaBidiFormatter
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.networking.NetworkConnectionDebugUtilModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.parser.html.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.image.GlideImageLoaderModule
import org.oppia.android.util.parser.image.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tests for [AppLanguageResourceHandler].
 *
 * Note that many of these tests are derived from display locale's tests since the handler mostly
 * calls forward to that class.
 */
// FunctionName: test names are conventionally named with underscores.
// SameParameterValue: tests should have specific context included/excluded for readability.
@Suppress("FunctionName", "SameParameterValue")
@RunWith(OppiaParameterizedTestRunner::class)
@SelectRunnerPlatform(ParameterizedRobolectricTestRunner::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = AppLanguageResourceHandlerTest.TestApplication::class)
class AppLanguageResourceHandlerTest {
  @get:Rule val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @get:Rule
  var activityRule =
    ActivityScenarioRule<TestActivity>(
      TestActivity.createIntent(ApplicationProvider.getApplicationContext())
    )

  @Inject lateinit var context: Context
  @Inject lateinit var wrapperChecker: TestOppiaBidiFormatter.Checker
  @Inject lateinit var appLanguageLocaleHandler: AppLanguageLocaleHandler
  @Inject lateinit var translationController: TranslationController
  @Inject lateinit var monitorFactory: DataProviderTestMonitor.Factory

  @Parameter lateinit var lang: String
  @Parameter lateinit var expectedDisplayText: String

  private val audioLanguage by lazy { AudioLanguage.valueOf(lang) }
  private val oppiaLanguage by lazy { OppiaLanguage.valueOf(lang) }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testFormatInLocaleWithWrapping_formatStringWithArgs_returnsCorrectlyFormattedString() {
    updateAppLanguageTo(OppiaLanguage.ENGLISH)
    val handler = retrieveAppLanguageResourceHandler()

    val formatted = handler.formatInLocaleWithWrapping("Test with %s and %s", "string", "11")

    assertThat(formatted).isEqualTo("Test with string and 11")
  }

  @Test
  fun testFormatInLocaleWithWrapping_properlyWrapsArguments() {
    updateAppLanguageTo(OppiaLanguage.ENGLISH)
    val handler = retrieveAppLanguageResourceHandler()

    handler.formatInLocaleWithWrapping("Test with %s and %s", "string", "11")

    // Verify that both arguments were wrapped.
    assertThat(wrapperChecker.getAllWrappedUnicodeTexts()).containsExactly("string", "11")
  }

  @Test
  fun testFormatInLocaleWithoutWrapping_formatStringWithArgs_returnsCorrectlyFormattedString() {
    updateAppLanguageTo(OppiaLanguage.ENGLISH)
    val handler = retrieveAppLanguageResourceHandler()

    val formatted = handler.formatInLocaleWithoutWrapping("Test with %s and %s", "string", "11")

    assertThat(formatted).isEqualTo("Test with string and 11")
  }

  @Test
  fun testFormatInLocaleWithoutWrapping_doesNotWrapArguments() {
    updateAppLanguageTo(OppiaLanguage.ENGLISH)
    val handler = retrieveAppLanguageResourceHandler()

    handler.formatInLocaleWithoutWrapping("Test with %s and %s", "string", "11")

    // Verify that none of the arguments were wrapped.
    assertThat(wrapperChecker.getAllWrappedUnicodeTexts()).isEmpty()
  }

  @Test
  fun testCapitalizeForHumans_capitalizedString_returnsSameString() {
    updateAppLanguageTo(OppiaLanguage.ENGLISH)
    val handler = retrieveAppLanguageResourceHandler()

    val capitalized = handler.capitalizeForHumans("Title String")

    assertThat(capitalized).isEqualTo("Title String")
  }

  @Test
  fun testCapitalizeForHumans_uncapitalizedString_returnsCapitalized() {
    updateAppLanguageTo(OppiaLanguage.ENGLISH)
    val handler = retrieveAppLanguageResourceHandler()

    val capitalized = handler.capitalizeForHumans("lowercased string")

    assertThat(capitalized).isEqualTo("Lowercased string")
  }

  @Test
  fun testCapitalizeForHumans_englishLocale_localeSensitiveCharAtStart_returnsConvertedCase() {
    updateAppLanguageTo(OppiaLanguage.ENGLISH)
    val handler = retrieveAppLanguageResourceHandler()

    val capitalized = handler.capitalizeForHumans("igloo")

    assertThat(capitalized).isEqualTo("Igloo")
  }

  @Test
  fun testCapitalizeForHumans_turkishLocale_localeSensitiveCharAtStart_returnsIncorrectCase() {
    // Set the language to Turkish to verify that the handler actually uses the user-specified
    // locale correctly.
    updateAppLanguageToSystem(TURKEY_TURKISH_LOCALE)
    val handler = retrieveAppLanguageResourceHandler()

    val capitalized = handler.capitalizeForHumans("igloo")

    // Note that the starting letter differs when being capitalized with a Turkish context (as
    // compared with the English version of this test). See https://stackoverflow.com/a/11063161 for
    // context on how casing behaviors differ based on Locales in Java.
    assertThat(capitalized).isEqualTo("İgloo")
  }

  @Test
  fun testGetStringInLocale_validId_returnsResourceStringForId() {
    updateAppLanguageTo(OppiaLanguage.ENGLISH)
    val handler = retrieveAppLanguageResourceHandler()

    val str = handler.getStringInLocale(R.string.test_basic_string)

    assertThat(str).isEqualTo("Basic string")
  }

  @Test
  fun testGetStringInLocale_nonExistentId_throwsException() {
    updateAppLanguageTo(OppiaLanguage.ENGLISH)
    val handler = retrieveAppLanguageResourceHandler()

    assertThrows<Resources.NotFoundException>() { handler.getStringInLocale(-1) }
  }

  @Test
  fun testGetStringInLocaleWithWrapping_formatStringResourceWithArgs_returnsFormattedString() {
    updateAppLanguageToSystem(HEBREW_LOCALE)
    val handler = retrieveAppLanguageResourceHandler()

    val str = handler.getStringInLocaleWithWrapping(
      R.string.test_string_with_arg_hebrew, "123 Some Street, Mountain View, CA"
    )

    // This is based on the example here:
    // https://developer.android.com/training/basics/supporting-devices/languages#FormatTextExplanationSolution.
    assertThat(str)
      .isEqualTo("האם התכוונת ל \u200F\u202A123 Some Street, Mountain View, CA\u202C\u200F")
  }

  @Test
  fun testGetStringInLocaleWithWrapping_properlyWrapsArguments() {
    updateAppLanguageToSystem(HEBREW_LOCALE)
    val handler = retrieveAppLanguageResourceHandler()

    handler.getStringInLocaleWithWrapping(
      R.string.test_string_with_arg_hebrew, "123 Some Street, Mountain View, CA"
    )

    // Verify that the argument was wrapped.
    assertThat(wrapperChecker.getAllWrappedUnicodeTexts())
      .containsExactly("123 Some Street, Mountain View, CA")
  }

  @Test
  fun testGetStringInLocaleWithWrapping_nonExistentId_throwsException() {
    updateAppLanguageTo(OppiaLanguage.ENGLISH)
    val handler = retrieveAppLanguageResourceHandler()

    assertThrows<Resources.NotFoundException>() { handler.getStringInLocaleWithWrapping(-1) }
  }

  @Test
  fun testGetStringInLocaleWithoutWrapping_formatStringResourceWithArgs_returnsFormattedString() {
    updateAppLanguageToSystem(HEBREW_LOCALE)
    val handler = retrieveAppLanguageResourceHandler()

    val str = handler.getStringInLocaleWithoutWrapping(
      R.string.test_string_with_arg_hebrew, "123 Some Street, Mountain View, CA"
    )

    // This is based on the example here:
    // https://developer.android.com/training/basics/supporting-devices/languages#FormatTextExplanationSolution.
    // Note that the string is formatted, but due to no bidirectional wrapping the address ends up
    // incorrectly formatted.
    assertThat(str).isEqualTo("האם התכוונת ל 123 Some Street, Mountain View, CA")
  }

  @Test
  fun testGetStringInLocaleWithoutWrapping_doesNotWrapArguments() {
    updateAppLanguageToSystem(HEBREW_LOCALE)
    val handler = retrieveAppLanguageResourceHandler()

    handler.getStringInLocaleWithoutWrapping(
      R.string.test_string_with_arg_hebrew, "123 Some Street, Mountain View, CA"
    )

    // Verify that no arguments were wrapped.
    assertThat(wrapperChecker.getAllWrappedUnicodeTexts()).isEmpty()
  }

  @Test
  fun testGetStringInLocaleWithoutWrapping_nonExistentId_throwsException() {
    updateAppLanguageTo(OppiaLanguage.ENGLISH)
    val handler = retrieveAppLanguageResourceHandler()

    assertThrows<Resources.NotFoundException>() {
      handler.getStringInLocaleWithoutWrapping(-1)
    }
  }

  @Test
  fun testGetStringArrayInLocale_validId_returnsArrayAsStringList() {
    updateAppLanguageTo(OppiaLanguage.ENGLISH)
    val handler = retrieveAppLanguageResourceHandler()

    val strList = handler.getStringArrayInLocale(R.array.test_str_array)

    assertThat(strList).containsExactly("Basic string", "Basic string2").inOrder()
  }

  @Test
  fun testGetStringArrayInLocale_nonExistentId_throwsException() {
    updateAppLanguageTo(OppiaLanguage.ENGLISH)
    val handler = retrieveAppLanguageResourceHandler()

    assertThrows<Resources.NotFoundException>() { handler.getStringArrayInLocale(-1) }
  }

  @Test
  fun testGetQuantityStringInLocale_validId_oneItem_returnsQuantityStringForSingleItem() {
    updateAppLanguageTo(OppiaLanguage.ENGLISH)
    val handler = retrieveAppLanguageResourceHandler()

    val str = handler.getQuantityStringInLocale(R.plurals.test_plural_string_no_args, 1)

    assertThat(str).isEqualTo("1 item")
  }

  @Test
  fun testGetQuantityStringInLocale_validId_twoItems_returnsQuantityStringForMultipleItems() {
    updateAppLanguageTo(OppiaLanguage.ENGLISH)
    val handler = retrieveAppLanguageResourceHandler()

    val str = handler.getQuantityStringInLocale(R.plurals.test_plural_string_no_args, 2)

    // Note that the 'other' case covers most scenarios in English (per
    // https://issuetracker.google.com/issues/36917255).
    assertThat(str).isEqualTo("2 items")
  }

  @Test
  fun testGetQuantityStringInLocale_nonExistentId_throwsException() {
    updateAppLanguageTo(OppiaLanguage.ENGLISH)
    val handler = retrieveAppLanguageResourceHandler()

    assertThrows<Resources.NotFoundException>() { handler.getQuantityStringInLocale(-1, 0) }
  }

  @Test
  fun testGetQuantityStringInLocaleWithWrapping_formatStrResourceWithArgs_returnsFormattedStr() {
    updateAppLanguageTo(OppiaLanguage.ENGLISH)
    val handler = retrieveAppLanguageResourceHandler()

    val str = handler.getQuantityStringInLocaleWithWrapping(
      R.plurals.test_plural_string_with_args, 2, "Two"
    )

    assertThat(str).isEqualTo("Two items")
  }

  @Test
  fun testGetQuantityStringInLocaleWithWrapping_properlyWrapsArguments() {
    updateAppLanguageTo(OppiaLanguage.ENGLISH)
    val handler = retrieveAppLanguageResourceHandler()

    handler.getQuantityStringInLocaleWithWrapping(R.plurals.test_plural_string_with_args, 2, "Two")

    // Verify that the argument was wrapped.
    assertThat(wrapperChecker.getAllWrappedUnicodeTexts()).containsExactly("Two")
  }

  @Test
  fun testGetQuantityStringInLocaleWithWrapping_nonExistentId_throwsException() {
    updateAppLanguageTo(OppiaLanguage.ENGLISH)
    val handler = retrieveAppLanguageResourceHandler()

    assertThrows<Resources.NotFoundException>() {
      handler.getQuantityStringInLocaleWithWrapping(-1, 0)
    }
  }

  @Test
  fun testGetQuantityStringInLocaleWithoutWrapping_formatStrResourceWithArgs_returnsFormattedStr() {
    updateAppLanguageTo(OppiaLanguage.ENGLISH)
    val handler = retrieveAppLanguageResourceHandler()

    val str = handler.getQuantityStringInLocaleWithoutWrapping(
      R.plurals.test_plural_string_with_args, 2, "Two"
    )

    assertThat(str).isEqualTo("Two items")
  }

  @Test
  fun testGetQuantityStringInLocaleWithoutWrapping_doesNotWrapArguments() {
    updateAppLanguageTo(OppiaLanguage.ENGLISH)
    val handler = retrieveAppLanguageResourceHandler()

    handler.getQuantityStringInLocaleWithoutWrapping(
      R.plurals.test_plural_string_with_args, 2, "Two"
    )

    // Verify that no arguments were wrapped.
    assertThat(wrapperChecker.getAllWrappedUnicodeTexts()).isEmpty()
  }

  @Test
  fun testGetQuantityStringInLocaleWithoutWrapping_nonExistentId_throwsException() {
    updateAppLanguageTo(OppiaLanguage.ENGLISH)
    val handler = retrieveAppLanguageResourceHandler()

    assertThrows<Resources.NotFoundException>() {
      handler.getQuantityStringInLocaleWithoutWrapping(-1, 0)
    }
  }

  @Test
  fun testFormatLong_forLargeLong_returnsStringWithExactDigits() {
    updateAppLanguageTo(OppiaLanguage.ENGLISH)
    val handler = retrieveAppLanguageResourceHandler()

    val formattedString = handler.formatLong(123456789)

    assertThat(formattedString.filter { it.isDigit() }).isEqualTo("123456789")
  }

  @Test
  fun testFormatLong_forDouble_returnsStringWithExactDigits() {
    updateAppLanguageTo(OppiaLanguage.ENGLISH)
    val handler = retrieveAppLanguageResourceHandler()

    val formattedString = handler.formatDouble(454545456.123)

    val digitsOnly = formattedString.filter { it.isDigit() }
    assertThat(digitsOnly).contains("454545456")
    assertThat(digitsOnly).contains("123")
  }

  @Test
  fun testFormatLong_forDouble_returnsStringWithPeriodsOrCommas() {
    updateAppLanguageTo(OppiaLanguage.ENGLISH)
    val handler = retrieveAppLanguageResourceHandler()

    val formattedString = handler.formatDouble(123456789.123)

    // Depending on formatting, commas and/or periods are used for large doubles.
    assertThat(formattedString).containsMatch("[,.]")
  }

  @Test
  fun testToHumanReadableString_forInt_returnsStringWithExactNumberInEnglish() {
    updateAppLanguageTo(OppiaLanguage.ENGLISH)
    val handler = retrieveAppLanguageResourceHandler()

    val formattedString = handler.toHumanReadableString(1)

    assertThat(formattedString).contains("1")
  }

  @Test
  fun testComputeDateString_forFixedTime_returnMonthDayYearParts() {
    updateAppLanguageTo(OppiaLanguage.ENGLISH)
    val handler = retrieveAppLanguageResourceHandler()

    val dateString = handler.computeDateString(MORNING_UTC_TIMESTAMP_MILLIS)

    assertThat(dateString.extractNumbers()).containsExactly("24", "2019")
    assertThat(dateString).contains("Apr")
  }

  // This test is breaking the "don't parameterize output" principle out of convenience since it's
  // testing functionality that's expected to go away with later language selection work.
  // TODO(#3793): Remove this once OppiaLanguage is used as the source of truth.
  @Test
  @Iteration("hi", "lang=HINDI_AUDIO_LANGUAGE", "expectedDisplayText=हिन्दी")
  @Iteration("pr-pt", "lang=BRAZILIAN_PORTUGUESE_LANGUAGE", "expectedDisplayText=Português")
  @Iteration("ar", "lang=ARABIC_LANGUAGE", "expectedDisplayText=العربية")
  @Iteration("pcm", "lang=NIGERIAN_PIDGIN_LANGUAGE", "expectedDisplayText=Naijá")
  @Iteration("unsp", "lang=AUDIO_LANGUAGE_UNSPECIFIED", "expectedDisplayText=English")
  @Iteration("none", "lang=NO_AUDIO", "expectedDisplayText=English")
  @Iteration("unknown", "lang=UNRECOGNIZED", "expectedDisplayText=English")
  @Iteration("en", "lang=ENGLISH_AUDIO_LANGUAGE", "expectedDisplayText=English")
  fun testComputeLocalizedDisplayName_englishLocale_forAllLanguages_hasTheExpectedOutput() {
    updateAppLanguageTo(OppiaLanguage.ENGLISH)
    val handler = retrieveAppLanguageResourceHandler()

    val displayText = handler.computeLocalizedDisplayName(audioLanguage)

    // The display name is localized to that language rather than the current locale (English).
    assertThat(displayText).isEqualTo(expectedDisplayText)
  }

  @Test
  @Iteration("unknown", "lang=LANGUAGE_UNSPECIFIED", "expectedDisplayText=English")
  @Iteration("ar", "lang=ARABIC", "expectedDisplayText=العربية")
  @Iteration("en", "lang=ENGLISH", "expectedDisplayText=English")
  @Iteration("hi", "lang=HINDI", "expectedDisplayText=हिन्दी")
  @Iteration("hi-en", "lang=HINGLISH", "expectedDisplayText=हिन्दी")
  @Iteration("pt", "lang=PORTUGUESE", "expectedDisplayText=Português")
  @Iteration("pr-pt", "lang=BRAZILIAN_PORTUGUESE", "expectedDisplayText=Português")
  @Iteration("sw", "lang=SWAHILI", "expectedDisplayText=Kiswahili")
  @Iteration("pcm", "lang=NIGERIAN_PIDGIN", "expectedDisplayText=Naijá")
  fun testComputeLocalizedDisplayName_englishLocale_forAllDisplayLanguages_hasTheExpectedOutput() {
    updateAppLanguageTo(OppiaLanguage.ENGLISH)
    val handler = retrieveAppLanguageResourceHandler()

    val displayText = handler.computeLocalizedDisplayName(oppiaLanguage)

    assertThat(displayText).isEqualTo(expectedDisplayText)
  }

  @Test
  fun testComputeDateTimeString_forFixedTime_returnsMinHourMonthDayYearParts() {
    updateAppLanguageTo(OppiaLanguage.ENGLISH)
    val handler = retrieveAppLanguageResourceHandler()

    val dateTimeString = handler.computeDateTimeString(MORNING_UTC_TIMESTAMP_MILLIS)

    assertThat(dateTimeString.extractNumbers()).containsExactly("22", "8", "24", "2019")
    assertThat(dateTimeString).contains("Apr")
  }

  private fun updateAppLanguageTo(language: OppiaLanguage) {
    updateAndSetAppLanguage(
      AppLanguageSelection.newBuilder().apply {
        selectedLanguage = language
      }.build()
    )
  }

  private fun updateAppLanguageToSystem(locale: Locale) {
    forceDefaultLocale(locale)
    updateAndSetAppLanguage(
      AppLanguageSelection.newBuilder().apply {
        useSystemLanguageOrAppDefault = true
      }.build()
    )
  }

  private fun forceDefaultLocale(locale: Locale) {
    context.applicationContext.resources.configuration.setLocale(locale)
    Locale.setDefault(locale)
  }

  private fun updateAndSetAppLanguage(appLanguageSelection: AppLanguageSelection) {
    // First, update the app language in the controller.
    val updateProvider =
      translationController.updateAppLanguage(ProfileId.getDefaultInstance(), appLanguageSelection)
    monitorFactory.waitForNextSuccessfulResult(updateProvider)

    // Second, compute the new display locale.
    val localeProvider = translationController.getAppLanguageLocale(ProfileId.getDefaultInstance())
    val displayLocale = monitorFactory.waitForNextSuccessfulResult(localeProvider)

    // Third, update the singleton to use the new display locale.
    appLanguageLocaleHandler.updateLocale(displayLocale)
  }

  private fun retrieveAppLanguageResourceHandler(): AppLanguageResourceHandler {
    lateinit var handler: AppLanguageResourceHandler
    activityRule.scenario.onActivity { activity ->
      handler = activity.appLanguageResourceHandler
    }
    return handler
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      RobolectricModule::class, TestDispatcherModule::class, ApplicationModule::class,
      PlatformParameterModule::class, LoggerModule::class, ContinueModule::class,
      FractionInputModule::class, ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      GcsResourceModule::class, GlideImageLoaderModule::class, ImageParsingModule::class,
      HtmlParserEntityTypeModule::class, QuestionModule::class, TestLogReportingModule::class,
      AccessibilityTestModule::class, LogStorageModule::class, CachingTestModule::class,
      ExpirationMetaDataRetrieverTestModule::class,
      ViewBindingShimModule::class, RatioInputModule::class, NetworkConfigProdModule::class,
      ApplicationStartupListenerModule::class, HintsAndSolutionConfigModule::class,
      LogReportWorkerModule::class, WorkManagerConfigurationModule::class,
      FirebaseLogUploaderModule::class, FakeOppiaClockModule::class,
      DeveloperOptionsStarterModule::class, DeveloperOptionsModule::class,
      ExplorationStorageModule::class, NetworkModule::class, HintsAndSolutionProdModule::class,
      NetworkConnectionUtilDebugModule::class, NetworkConnectionDebugUtilModule::class,
      AssetModule::class, LocaleTestModule::class, ActivityRecreatorTestModule::class,
      ActivityIntentFactoriesModule::class, PlatformParameterSingletonModule::class,
      NumericExpressionInputModule::class, AlgebraicExpressionInputModule::class,
      MathEquationInputModule::class, SplitScreenInteractionModule::class,
      LoggingIdentifierModule::class, ApplicationLifecycleModule::class,
      SyncStatusModule::class, MetricLogSchedulerModule::class, TestingBuildFlavorModule::class,
      ActivityRouterModule::class,
      CpuPerformanceSnapshotterModule::class, ExplorationProgressModule::class,
      TestAuthenticationModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(appLanguageResourceHandlerTest: AppLanguageResourceHandlerTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerAppLanguageResourceHandlerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(appLanguageResourceHandlerTest: AppLanguageResourceHandlerTest) {
      component.inject(appLanguageResourceHandlerTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }

  private companion object {
    // Date & time: Wed Apr 24 2019 08:22:03 GMT.
    private const val MORNING_UTC_TIMESTAMP_MILLIS = 1556094123000

    private val TURKEY_TURKISH_LOCALE = Locale("tr", "TR")
    private val HEBREW_LOCALE = Locale("he", "US")

    private fun String.extractNumbers(): List<String> =
      "\\d+".toRegex().findAll(this).flatMap { it.groupValues }.toList()
  }
}
