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
import org.oppia.android.app.model.Fraction
import org.oppia.android.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.app.shim.ViewBindingShimModule
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
import org.oppia.android.domain.onboarding.ExpirationMetaDataRetrieverModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.loguploader.LogUploadWorkerModule
import org.oppia.android.domain.oppialogger.loguploader.WorkManagerConfigurationModule
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
import org.oppia.android.util.parser.GlideImageLoaderModule
import org.oppia.android.util.parser.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.ImageParsingModule
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
  fun testParser_submitTimeError_regularFraction_returnsValid() {
    val error = stringToFractionParser.getSubmitTimeError("1/2")
    assertThat(error).isEqualTo(StringToFractionParser.FractionParsingError.VALID)
  }

  @Test
  fun testParser_submitTimeError_regularNegativeFractionWithExtraSpaces_returnsValid() {
    val error = stringToFractionParser.getSubmitTimeError(" -1   / 2 ")
    assertThat(error).isEqualTo(StringToFractionParser.FractionParsingError.VALID)
  }

  @Test
  fun testParser_submitTimeError_atLengthLimit_returnsValid() {
    val error = stringToFractionParser.getSubmitTimeError("1234567/1234567")
    assertThat(error).isEqualTo(StringToFractionParser.FractionParsingError.VALID)
  }

  @Test
  fun testParser_submitTimeError_wholeNumber_returnsValid() {
    val error = stringToFractionParser.getSubmitTimeError("888")
    assertThat(error).isEqualTo(StringToFractionParser.FractionParsingError.VALID)
  }

  @Test
  fun testParser_submitTimeError_wholeNegativeNumber_returnsValid() {
    val error = stringToFractionParser.getSubmitTimeError("-777")
    assertThat(error).isEqualTo(StringToFractionParser.FractionParsingError.VALID)
  }

  @Test
  fun testParser_submitTimeError_mixedNumber_returnsValid() {
    val error = stringToFractionParser.getSubmitTimeError("11 22/33")
    assertThat(error).isEqualTo(StringToFractionParser.FractionParsingError.VALID)
  }

  @Test
  fun testParser_submitTimeError_returnsNumberTooLong() {
    val error = stringToFractionParser.getSubmitTimeError("0123456789")
    assertThat(error).isEqualTo(StringToFractionParser.FractionParsingError.NUMBER_TOO_LONG)

    val errorMessage = error.getErrorMessageFromStringRes(context)
    assertThat(errorMessage)
      .isEqualTo("None of the numbers in the fraction should have more than 7 digits.")
  }

  @Test
  fun testParser_submitTimeError_returnsInvalidFormat() {
    val error = stringToFractionParser.getSubmitTimeError("jdhfc")
    assertThat(error).isEqualTo(StringToFractionParser.FractionParsingError.INVALID_FORMAT)

    val errorMessage = error.getErrorMessageFromStringRes(context)
    assertThat(errorMessage)
      .isEqualTo("Please enter a valid fraction (e.g., 5/3 or 1 2/3)")
  }

  @Test
  fun testParser_submitTimeError_returnsDivisionByZero() {
    val error = stringToFractionParser.getSubmitTimeError("123/0")
    assertThat(error).isEqualTo(StringToFractionParser.FractionParsingError.DIVISION_BY_ZERO)

    val errorMessage = error.getErrorMessageFromStringRes(context)
    assertThat(errorMessage).isEqualTo("Please do not put 0 in the denominator")
  }

  @Test
  fun testParser_submitTimeError_ambiguousSpacing_returnsInvalidFormat() {
    val error = stringToFractionParser.getSubmitTimeError("1 2 3/4")
    assertThat(error).isEqualTo(StringToFractionParser.FractionParsingError.INVALID_FORMAT)

    val errorMessage = error.getErrorMessageFromStringRes(context)
    assertThat(errorMessage)
      .isEqualTo("Please enter a valid fraction (e.g., 5/3 or 1 2/3)")
  }

  @Test
  fun testParser_submitTimeError_emptyString_returnsInvalidFormat() {
    val error = stringToFractionParser.getSubmitTimeError("")
    assertThat(error).isEqualTo(StringToFractionParser.FractionParsingError.INVALID_FORMAT)

    val errorMessage = error.getErrorMessageFromStringRes(context)
    assertThat(errorMessage)
      .isEqualTo("Please enter a valid fraction (e.g., 5/3 or 1 2/3)")
  }

  @Test
  fun testParser_realTimeError_regularFraction_returnsValid() {
    val error = stringToFractionParser.getRealTimeAnswerError("2/3")
    assertThat(error).isEqualTo(StringToFractionParser.FractionParsingError.VALID)
  }

  @Test
  fun testParser_realTimeError_regularNegativeFraction_returnsValid() {
    val error = stringToFractionParser.getRealTimeAnswerError("-2/3")
    assertThat(error).isEqualTo(StringToFractionParser.FractionParsingError.VALID)
  }

  @Test
  fun testParser_realTimeError_wholeNumber_returnsValid() {
    val error = stringToFractionParser.getRealTimeAnswerError("4")
    assertThat(error).isEqualTo(StringToFractionParser.FractionParsingError.VALID)
  }

  @Test
  fun testParser_realTimeError_wholeNegativeNumber_returnsValid() {
    val error = stringToFractionParser.getRealTimeAnswerError("-4")
    assertThat(error).isEqualTo(StringToFractionParser.FractionParsingError.VALID)
  }

  @Test
  fun testParser_realTimeError_mixedNumber_returnsValid() {
    val error = stringToFractionParser.getRealTimeAnswerError("5 2/3")
    assertThat(error).isEqualTo(StringToFractionParser.FractionParsingError.VALID)
  }

  @Test
  fun testParser_realTimeError_mixedNegativeNumber_returnsValid() {
    val error = stringToFractionParser.getRealTimeAnswerError("-5 2/3")
    assertThat(error).isEqualTo(StringToFractionParser.FractionParsingError.VALID)
  }

  @Test
  fun testParser_realTimeError_returnsInvalidFormat() {
    val error = stringToFractionParser.getRealTimeAnswerError("/23/")
    assertThat(error).isEqualTo(StringToFractionParser.FractionParsingError.INVALID_FORMAT)

    val errorMessage = error.getErrorMessageFromStringRes(context)
    assertThat(errorMessage)
      .isEqualTo("Please enter a valid fraction (e.g., 5/3 or 1 2/3)")
  }

  @Test
  fun testParser_realTimeError_returnsInvalidChars() {
    val error = stringToFractionParser.getRealTimeAnswerError("abc")
    assertThat(error).isEqualTo(StringToFractionParser.FractionParsingError.INVALID_CHARS)

    val errorMessage = error.getErrorMessageFromStringRes(context)
    assertThat(errorMessage)
      .isEqualTo("Please only use numerical digits, spaces or forward slashes (/)")
  }

  @Test
  fun testParser_realTimeError_noNumerator_returnsInvalidFormat() {
    val error = stringToFractionParser.getRealTimeAnswerError("/3")
    assertThat(error).isEqualTo(StringToFractionParser.FractionParsingError.INVALID_FORMAT)

    val errorMessage = error.getErrorMessageFromStringRes(context)
    assertThat(errorMessage)
      .isEqualTo("Please enter a valid fraction (e.g., 5/3 or 1 2/3)")
  }

  @Test
  fun testParser_realTimeError_severalSlashes_returnsInvalidFormat() {
    val error = stringToFractionParser.getRealTimeAnswerError("1/3/8")
    assertThat(error).isEqualTo(StringToFractionParser.FractionParsingError.INVALID_FORMAT)

    val errorMessage = error.getErrorMessageFromStringRes(context)
    assertThat(errorMessage)
      .isEqualTo("Please enter a valid fraction (e.g., 5/3 or 1 2/3)")
  }

  @Test
  fun testParser_realTimeError_severalDashes_returnsInvalidFormat() {
    val error = stringToFractionParser.getRealTimeAnswerError("-1/-3")
    assertThat(error).isEqualTo(StringToFractionParser.FractionParsingError.INVALID_FORMAT)

    val errorMessage = error.getErrorMessageFromStringRes(context)
    assertThat(errorMessage)
      .isEqualTo("Please enter a valid fraction (e.g., 5/3 or 1 2/3)")
  }

  @Test
  fun testParser_parseFraction_divisionByZero() {
    val parseFraction = stringToFractionParser.parseFraction("8/0")
    val parseFractionFromString = stringToFractionParser.parseFractionFromString("8/0")
    val constructedFraction = Fraction.newBuilder().apply {
      numerator = 8
    }.build()
    assertThat(parseFractionFromString).isEqualTo(constructedFraction)
    assertThat(parseFraction).isEqualTo(constructedFraction)
  }

  @Test
  fun testParser_parseFraction_invalidFormat() {
    val parseFraction = stringToFractionParser.parseFraction("7 1/2 4/5")
    assertThat(parseFraction).isEqualTo(null)

    val exception = assertThrows(IllegalArgumentException::class) {
      stringToFractionParser.parseFractionFromString("7 1/2 4/5")
    }
    assertThat(exception).hasMessageThat().contains("Incorrectly formatted fraction: 7 1/2 4/5")
  }

  @Test
  fun testParser_parseFraction_invalidChars() {
    val parseFraction = stringToFractionParser.parseFraction("abc")
    assertThat(parseFraction).isEqualTo(null)

    val exception = assertThrows(IllegalArgumentException::class) {
      stringToFractionParser.parseFractionFromString("abc")
    }
    assertThat(exception).hasMessageThat().contains("Incorrectly formatted fraction: abc")
  }

  @Test
  fun testParser_parseFraction_parseRegularFraction() {
    val parseFractionFromString = stringToFractionParser.parseFractionFromString("1/2")
    val parseFraction = stringToFractionParser.parseFraction("1/2")
    val constructedFraction = Fraction.newBuilder().apply {
      numerator = 1
      denominator = 2
    }.build()
    assertThat(parseFractionFromString).isEqualTo(constructedFraction)
    assertThat(parseFraction).isEqualTo(constructedFraction)
  }

  @Test
  fun testParser_parseFraction_parseRegularNegativeFraction() {
    val parseFractionFromString = stringToFractionParser.parseFractionFromString("-8/4")
    val parseFraction = stringToFractionParser.parseFraction("-8/4")
    val constructedFraction = Fraction.newBuilder().apply {
      isNegative = true
      numerator = 8
      denominator = 4
    }.build()
    assertThat(parseFractionFromString).isEqualTo(constructedFraction)
    assertThat(parseFraction).isEqualTo(constructedFraction)
  }

  @Test
  fun testParser_parseFraction_parseWholeNumber() {
    val parseFractionFromString = stringToFractionParser.parseFractionFromString("7")
    val parseFraction = stringToFractionParser.parseFraction("7")
    val constructedFraction = Fraction.newBuilder().apply {
      wholeNumber = 7
      numerator = 0
      denominator = 1
    }.build()
    assertThat(parseFractionFromString).isEqualTo(constructedFraction)
    assertThat(parseFraction).isEqualTo(constructedFraction)
  }

  @Test
  fun testParser_parseFraction_parseWholeNegativeNumber() {
    val parseFractionFromString = stringToFractionParser.parseFractionFromString("-7")
    val parseFraction = stringToFractionParser.parseFraction("-7")
    val constructedFraction = Fraction.newBuilder().apply {
      isNegative = true
      wholeNumber = 7
      numerator = 0
      denominator = 1
    }.build()
    assertThat(parseFractionFromString).isEqualTo(constructedFraction)
    assertThat(parseFraction).isEqualTo(constructedFraction)
  }

  @Test
  fun testParser_parseFraction_parseMixedNumber() {
    val parseFractionFromString = stringToFractionParser.parseFractionFromString("1 3/4")
    val parseFraction = stringToFractionParser.parseFraction("1 3/4")
    val constructedFraction = Fraction.newBuilder().apply {
      wholeNumber = 1
      numerator = 3
      denominator = 4
    }.build()
    assertThat(parseFractionFromString).isEqualTo(constructedFraction)
    assertThat(parseFraction).isEqualTo(constructedFraction)
  }

  @Test
  fun testParser_parseFraction_parseNegativeMixedNumber() {
    val parseFractionFromString = stringToFractionParser.parseFractionFromString("-123 456/7")
    val parseFraction = stringToFractionParser.parseFraction("-123 456/7")
    val constructedFraction = Fraction.newBuilder().apply {
      isNegative = true
      wholeNumber = 123
      numerator = 456
      denominator = 7
    }.build()
    assertThat(parseFractionFromString).isEqualTo(constructedFraction)
    assertThat(parseFraction).isEqualTo(constructedFraction)
  }

  @Test
  fun testParser_parseFraction_parseLongMixedNumber() {
    val parseFractionFromString = stringToFractionParser
      .parseFractionFromString("1234567 1234567/1234567")
    val parseFraction = stringToFractionParser
      .parseFraction("1234567 1234567/1234567")
    val constructedFraction = Fraction.newBuilder().apply {
      wholeNumber = 1234567
      numerator = 1234567
      denominator = 1234567
    }.build()
    assertThat(parseFractionFromString).isEqualTo(constructedFraction)
    assertThat(parseFraction).isEqualTo(constructedFraction)
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @Singleton
  @Component(
    modules = [
      TestDispatcherModule::class, ApplicationModule::class, RobolectricModule::class,
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
      FirebaseLogUploaderModule::class, FakeOppiaClockModule::class
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
