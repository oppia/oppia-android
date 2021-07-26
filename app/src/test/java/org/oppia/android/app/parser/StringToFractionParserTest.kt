package org.oppia.android.app.parser

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.Component
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.application.ActivityComponentFactory
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.devoptions.DeveloperOptionsModule
import org.oppia.android.app.devoptions.DeveloperOptionsStarterModule
import org.oppia.android.app.model.Fraction
import org.oppia.android.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.topic.PracticeTabModule
import org.oppia.android.domain.classify.InteractionsModule
import org.oppia.android.domain.classify.rules.continueinteraction.ContinueModule
import org.oppia.android.domain.classify.rules.dragAndDropSortInput.DragDropSortInputModule
import org.oppia.android.domain.classify.rules.fractioninput.FractionInputModule
import org.oppia.android.domain.classify.rules.imageClickInput.ImageClickInputModule
import org.oppia.android.domain.classify.rules.itemselectioninput.ItemSelectionInputModule
import org.oppia.android.domain.classify.rules.multiplechoiceinput.MultipleChoiceInputModule
import org.oppia.android.domain.classify.rules.numberwithunits.NumberWithUnitsRuleModule
import org.oppia.android.domain.classify.rules.numericinput.NumericInputRuleModule
import org.oppia.android.domain.classify.rules.ratioinput.RatioInputModule
import org.oppia.android.domain.classify.rules.textinput.TextInputRuleModule
import org.oppia.android.domain.exploration.lightweightcheckpointing.ExplorationStorageModule
import org.oppia.android.domain.onboarding.ExpirationMetaDataRetrieverModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.loguploader.LogUploadWorkerModule
import org.oppia.android.domain.oppialogger.loguploader.WorkManagerConfigurationModule
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.assertThrows
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.parser.html.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.image.GlideImageLoaderModule
import org.oppia.android.util.parser.image.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [StringToFractionParser]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = StringToFractionParserTest.TestApplication::class, qualifiers = "port-xxhdpi")
class StringToFractionParserTest {

  @Inject
  lateinit var context: Context

  private lateinit var stringToFractionParser: StringToFractionParser

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    stringToFractionParser = StringToFractionParser()
  }

  @Test
  fun testSubmitTimeError_regularFraction_returnsValid() {
    val error = stringToFractionParser.getSubmitTimeError("1/2")
    assertThat(error).isEqualTo(StringToFractionParser.FractionParsingError.VALID)
  }

  @Test
  fun testSubmitTimeError_regularNegativeFractionWithExtraSpaces_returnsValid() {
    val error = stringToFractionParser.getSubmitTimeError(" -1   / 2 ")
    assertThat(error).isEqualTo(StringToFractionParser.FractionParsingError.VALID)
  }

  @Test
  fun testSubmitTimeError_atLengthLimit_returnsValid() {
    val error = stringToFractionParser.getSubmitTimeError("1234567/1234567")
    assertThat(error).isEqualTo(StringToFractionParser.FractionParsingError.VALID)
  }

  @Test
  fun testSubmitTimeError_wholeNumber_returnsValid() {
    val error = stringToFractionParser.getSubmitTimeError("888")
    assertThat(error).isEqualTo(StringToFractionParser.FractionParsingError.VALID)
  }

  @Test
  fun testSubmitTimeError_wholeNegativeNumber_returnsValid() {
    val error = stringToFractionParser.getSubmitTimeError("-777")
    assertThat(error).isEqualTo(StringToFractionParser.FractionParsingError.VALID)
  }

  @Test
  fun testSubmitTimeError_mixedNumber_returnsValid() {
    val error = stringToFractionParser.getSubmitTimeError("11 22/33")
    assertThat(error).isEqualTo(StringToFractionParser.FractionParsingError.VALID)
  }

  @Test
  fun testSubmitTimeError_validMixedNumber_noErrorMessage() {
    val errorMessage = stringToFractionParser.getSubmitTimeError("11 22/33")
      .getErrorMessageFromStringRes(context)
    assertThat(errorMessage).isNull()
  }

  @Test
  fun testSubmitTimeError_tenDigitNumber_returnsNumberTooLong() {
    val error = stringToFractionParser.getSubmitTimeError("0123456789")
    assertThat(error).isEqualTo(StringToFractionParser.FractionParsingError.NUMBER_TOO_LONG)
  }

  @Test
  fun testSubmitTimeError_tenDigitNumber_numberTooLong_hasRelevantErrorMessage() {
    val errorMessage = stringToFractionParser.getSubmitTimeError("0123456789")
      .getErrorMessageFromStringRes(context)
    assertThat(errorMessage)
      .isEqualTo("None of the numbers in the fraction should have more than 7 digits.")
  }

  @Test
  fun testSubmitTimeError_nonDigits_returnsInvalidFormat() {
    val error = stringToFractionParser.getSubmitTimeError("jdhfc")
    assertThat(error).isEqualTo(StringToFractionParser.FractionParsingError.INVALID_FORMAT)
  }

  @Test
  fun testSubmitTimeError_nonDigits_invalidFormat_hasRelevantErrorMessage() {
    val errorMessage = stringToFractionParser.getSubmitTimeError("jdhfc")
      .getErrorMessageFromStringRes(context)
    assertThat(errorMessage)
      .isEqualTo("Please enter a valid fraction (e.g., 5/3 or 1 2/3)")
  }

  @Test
  fun testSubmitTimeError_divisionByZero_returnsDivisionByZero() {
    val error = stringToFractionParser.getSubmitTimeError("123/0")
    assertThat(error).isEqualTo(StringToFractionParser.FractionParsingError.DIVISION_BY_ZERO)
  }

  @Test
  fun testSubmitTimeError_divisionByZero_hasRelevantErrorMessage() {
    val errorMessage = stringToFractionParser.getSubmitTimeError("123/0")
      .getErrorMessageFromStringRes(context)
    assertThat(errorMessage).isEqualTo("Please do not put 0 in the denominator")
  }

  @Test
  fun testSubmitTimeError_ambiguousSpacing_returnsInvalidFormat() {
    val error = stringToFractionParser.getSubmitTimeError("1 2 3/4")
    assertThat(error).isEqualTo(StringToFractionParser.FractionParsingError.INVALID_FORMAT)
  }

  @Test
  fun testSubmitTimeError_ambiguousSpacing_invalidFormat_hasRelevantErrorMessage() {
    val errorMessage = stringToFractionParser.getSubmitTimeError("1 2 3/4")
      .getErrorMessageFromStringRes(context)
    assertThat(errorMessage)
      .isEqualTo("Please enter a valid fraction (e.g., 5/3 or 1 2/3)")
  }

  @Test
  fun testSubmitTimeError_emptyString_returnsInvalidFormat() {
    val error = stringToFractionParser.getSubmitTimeError("")
    assertThat(error).isEqualTo(StringToFractionParser.FractionParsingError.INVALID_FORMAT)
  }

  @Test
  fun testSubmitTimeError_emptyString_invalidFormat_hasRelevantErrorMessage() {
    val errorMessage = stringToFractionParser.getSubmitTimeError("")
      .getErrorMessageFromStringRes(context)
    assertThat(errorMessage)
      .isEqualTo("Please enter a valid fraction (e.g., 5/3 or 1 2/3)")
  }

  @Test
  fun testRealTimeError_regularFraction_returnsValid() {
    val error = stringToFractionParser.getRealTimeAnswerError("2/3")
    assertThat(error).isEqualTo(StringToFractionParser.FractionParsingError.VALID)
  }

  @Test
  fun testRealTimeError_regularNegativeFraction_returnsValid() {
    val error = stringToFractionParser.getRealTimeAnswerError("-2/3")
    assertThat(error).isEqualTo(StringToFractionParser.FractionParsingError.VALID)
  }

  @Test
  fun testRealTimeError_wholeNumber_returnsValid() {
    val error = stringToFractionParser.getRealTimeAnswerError("4")
    assertThat(error).isEqualTo(StringToFractionParser.FractionParsingError.VALID)
  }

  @Test
  fun testRealTimeError_wholeNegativeNumber_returnsValid() {
    val error = stringToFractionParser.getRealTimeAnswerError("-4")
    assertThat(error).isEqualTo(StringToFractionParser.FractionParsingError.VALID)
  }

  @Test
  fun testRealTimeError_mixedNumber_returnsValid() {
    val error = stringToFractionParser.getRealTimeAnswerError("5 2/3")
    assertThat(error).isEqualTo(StringToFractionParser.FractionParsingError.VALID)
  }

  @Test
  fun testRealTimeError_mixedNegativeNumber_returnsValid() {
    val error = stringToFractionParser.getRealTimeAnswerError("-5 2/3")
    assertThat(error).isEqualTo(StringToFractionParser.FractionParsingError.VALID)
  }

  @Test
  fun testRealTimeError_validRegularFraction_noErrorMessage() {
    val errorMessage = stringToFractionParser.getRealTimeAnswerError("2/3")
      .getErrorMessageFromStringRes(context)
    assertThat(errorMessage).isNull()
  }

  @Test
  fun testRealTimeError_nonDigits_returnsInvalidChars() {
    val error = stringToFractionParser.getRealTimeAnswerError("abc")
    assertThat(error).isEqualTo(StringToFractionParser.FractionParsingError.INVALID_CHARS)
  }

  @Test
  fun testRealTimeError_nonDigits_invalidChars_hasRelevantErrorMessage() {
    val errorMessage = stringToFractionParser.getRealTimeAnswerError("abc")
      .getErrorMessageFromStringRes(context)
    assertThat(errorMessage)
      .isEqualTo("Please only use numerical digits, spaces or forward slashes (/)")
  }

  @Test
  fun testRealTimeError_noNumerator_returnsInvalidFormat() {
    val error = stringToFractionParser.getRealTimeAnswerError("/3")
    assertThat(error).isEqualTo(StringToFractionParser.FractionParsingError.INVALID_FORMAT)
  }

  @Test
  fun testRealTimeError_noNumerator_invalidFormat_hasRelevantErrorMessage() {
    val errorMessage = stringToFractionParser.getRealTimeAnswerError("/3")
      .getErrorMessageFromStringRes(context)
    assertThat(errorMessage)
      .isEqualTo("Please enter a valid fraction (e.g., 5/3 or 1 2/3)")
  }

  @Test
  fun testRealTimeError_severalSlashes_invalidFormat_returnsInvalidFormat() {
    val error = stringToFractionParser.getRealTimeAnswerError("1/3/8")
    assertThat(error).isEqualTo(StringToFractionParser.FractionParsingError.INVALID_FORMAT)
  }

  @Test
  fun testRealTimeError_severalSlashes_invalidFormat_hasRelevantErrorMessage() {
    val errorMessage = stringToFractionParser.getRealTimeAnswerError("1/3/8")
      .getErrorMessageFromStringRes(context)
    assertThat(errorMessage)
      .isEqualTo("Please enter a valid fraction (e.g., 5/3 or 1 2/3)")
  }

  @Test
  fun testRealTimeError_severalDashes_returnsInvalidFormat() {
    val error = stringToFractionParser.getRealTimeAnswerError("-1/-3")
    assertThat(error).isEqualTo(StringToFractionParser.FractionParsingError.INVALID_FORMAT)
  }

  @Test
  fun testRealTimeError_severalDashes_invalidFormat_hasRelevantErrorMessage() {
    val errorMessage = stringToFractionParser.getRealTimeAnswerError("-1/-3")
      .getErrorMessageFromStringRes(context)
    assertThat(errorMessage)
      .isEqualTo("Please enter a valid fraction (e.g., 5/3 or 1 2/3)")
  }

  @Test
  fun testParseFraction_divisionByZero_returnsFraction() {
    val parseFraction = stringToFractionParser.parseFraction("8/0")
    val parseFractionFromString = stringToFractionParser.parseFractionFromString("8/0")
    val expectedFraction = Fraction.newBuilder().apply {
      numerator = 8
      denominator = 0
    }.build()
    assertThat(parseFractionFromString).isEqualTo(expectedFraction)
    assertThat(parseFraction).isEqualTo(expectedFraction)
  }

  @Test
  fun testParseFraction_multipleFractions_failsWithError() {
    val parseFraction = stringToFractionParser.parseFraction("7 1/2 4/5")
    assertThat(parseFraction).isEqualTo(null)

    val exception = assertThrows(IllegalArgumentException::class) {
      stringToFractionParser.parseFractionFromString("7 1/2 4/5")
    }
    assertThat(exception).hasMessageThat().contains("Incorrectly formatted fraction: 7 1/2 4/5")
  }

  @Test
  fun testParseFraction_nonDigits_failsWithError() {
    val parseFraction = stringToFractionParser.parseFraction("abc")
    assertThat(parseFraction).isEqualTo(null)

    val exception = assertThrows(IllegalArgumentException::class) {
      stringToFractionParser.parseFractionFromString("abc")
    }
    assertThat(exception).hasMessageThat().contains("Incorrectly formatted fraction: abc")
  }

  @Test
  fun testParseFraction_regularFraction_returnsFraction() {
    val parseFractionFromString = stringToFractionParser.parseFractionFromString("1/2")
    val parseFraction = stringToFractionParser.parseFraction("1/2")
    val expectedFraction = Fraction.newBuilder().apply {
      numerator = 1
      denominator = 2
    }.build()
    assertThat(parseFractionFromString).isEqualTo(expectedFraction)
    assertThat(parseFraction).isEqualTo(expectedFraction)
  }

  @Test
  fun testParseFraction_regularNegativeFraction_returnsFraction() {
    val parseFractionFromString = stringToFractionParser.parseFractionFromString("-8/4")
    val parseFraction = stringToFractionParser.parseFraction("-8/4")
    val expectedFraction = Fraction.newBuilder().apply {
      isNegative = true
      numerator = 8
      denominator = 4
    }.build()
    assertThat(parseFractionFromString).isEqualTo(expectedFraction)
    assertThat(parseFraction).isEqualTo(expectedFraction)
  }

  @Test
  fun testParseFraction_wholeNumber_returnsFraction() {
    val parseFractionFromString = stringToFractionParser.parseFractionFromString("7")
    val parseFraction = stringToFractionParser.parseFraction("7")
    val expectedFraction = Fraction.newBuilder().apply {
      wholeNumber = 7
      numerator = 0
      denominator = 1
    }.build()
    assertThat(parseFractionFromString).isEqualTo(expectedFraction)
    assertThat(parseFraction).isEqualTo(expectedFraction)
  }

  @Test
  fun testParseFraction_wholeNegativeNumber_returnsFraction() {
    val parseFractionFromString = stringToFractionParser.parseFractionFromString("-7")
    val parseFraction = stringToFractionParser.parseFraction("-7")
    val expectedFraction = Fraction.newBuilder().apply {
      isNegative = true
      wholeNumber = 7
      numerator = 0
      denominator = 1
    }.build()
    assertThat(parseFractionFromString).isEqualTo(expectedFraction)
    assertThat(parseFraction).isEqualTo(expectedFraction)
  }

  @Test
  fun testParseFraction_mixedNumber_returnsFraction() {
    val parseFractionFromString = stringToFractionParser.parseFractionFromString("1 3/4")
    val parseFraction = stringToFractionParser.parseFraction("1 3/4")
    val expectedFraction = Fraction.newBuilder().apply {
      wholeNumber = 1
      numerator = 3
      denominator = 4
    }.build()
    assertThat(parseFractionFromString).isEqualTo(expectedFraction)
    assertThat(parseFraction).isEqualTo(expectedFraction)
  }

  @Test
  fun testParseFraction_negativeMixedNumber_returnsFraction() {
    val parseFractionFromString = stringToFractionParser.parseFractionFromString("-123 456/7")
    val parseFraction = stringToFractionParser.parseFraction("-123 456/7")
    val expectedFraction = Fraction.newBuilder().apply {
      isNegative = true
      wholeNumber = 123
      numerator = 456
      denominator = 7
    }.build()
    assertThat(parseFractionFromString).isEqualTo(expectedFraction)
    assertThat(parseFraction).isEqualTo(expectedFraction)
  }

  @Test
  fun testParseFraction_longMixedNumber_returnsFraction() {
    val parseFractionFromString = stringToFractionParser
      .parseFractionFromString("1234567 1234567/1234567")
    val parseFraction = stringToFractionParser
      .parseFraction("1234567 1234567/1234567")
    val expectedFraction = Fraction.newBuilder().apply {
      wholeNumber = 1234567
      numerator = 1234567
      denominator = 1234567
    }.build()
    assertThat(parseFractionFromString).isEqualTo(expectedFraction)
    assertThat(parseFraction).isEqualTo(expectedFraction)
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @Singleton
  @Component(
    modules = [
      TestDispatcherModule::class, ApplicationModule::class, RobolectricModule::class,
      PlatformParameterModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      GcsResourceModule::class, GlideImageLoaderModule::class, ImageParsingModule::class,
      HtmlParserEntityTypeModule::class, QuestionModule::class, TestLogReportingModule::class,
      AccessibilityTestModule::class, LogStorageModule::class, CachingTestModule::class,
      PrimeTopicAssetsControllerModule::class, ExpirationMetaDataRetrieverModule::class,
      ViewBindingShimModule::class, RatioInputModule::class,
      ApplicationStartupListenerModule::class, LogUploadWorkerModule::class,
      WorkManagerConfigurationModule::class, HintsAndSolutionConfigModule::class,
      FirebaseLogUploaderModule::class, FakeOppiaClockModule::class, PracticeTabModule::class,
      DeveloperOptionsStarterModule::class, DeveloperOptionsModule::class,
      ExplorationStorageModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

    fun inject(stringToFractionParserTest: StringToFractionParserTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerStringToFractionParserTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(stringToFractionParserTest: StringToFractionParserTest) {
      component.inject(stringToFractionParserTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
