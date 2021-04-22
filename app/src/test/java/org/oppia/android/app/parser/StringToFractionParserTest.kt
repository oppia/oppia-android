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
  fun testParser_submitTimeError_returnsValid() {
    val error = stringToFractionParser.getSubmitTimeError("1/2")
      .getErrorMessageFromStringRes(context)
    assertThat(error).isEqualTo(null)
  }

  @Test
  fun testParser_submitTimeError_extraSpaces_returnsValid() {
    val error = stringToFractionParser.getSubmitTimeError(" 1   / 2 ")
      .getErrorMessageFromStringRes(context)
    assertThat(error).isEqualTo(null)
  }

  @Test
  fun testParser_submitTimeError_tooLong_returnsNumberTooLong() {
    val error = stringToFractionParser.getSubmitTimeError("0123456789")
      .getErrorMessageFromStringRes(context)
    assertThat(error)
      .isEqualTo("None of the numbers in the fraction should have more than 7 digits.")
  }

  @Test
  fun testParser_submitTimeError_atLengthLimit_returnsValid() {
    val error = stringToFractionParser.getSubmitTimeError("1234567/1234567")
      .getErrorMessageFromStringRes(context)
    assertThat(error).isEqualTo(null)
  }

  @Test
  fun testParser_submitTimeError_returnsDivisionByZero() {
    val error = stringToFractionParser.getSubmitTimeError("123/0")
      .getErrorMessageFromStringRes(context)
    assertThat(error).isEqualTo("Please do not put 0 in the denominator")
  }

  @Test
  fun testParser_submitTimeError_returnsInvalidFormat() {
    val error = stringToFractionParser.getSubmitTimeError("")
      .getErrorMessageFromStringRes(context)
    assertThat(error).isEqualTo("Please enter a valid fraction (e.g., 5/3 or 1 2/3)")
  }

  @Test
  fun testParser_realTimeError_noNumerator_returnsInvalidFormat() {
    val error = stringToFractionParser.getSubmitTimeError("/3")
      .getErrorMessageFromStringRes(context)
    assertThat(error).isEqualTo("Please enter a valid fraction (e.g., 5/3 or 1 2/3)")
  }

  @Test
  fun testParser_realTimeError_severalSlashes_returnsInvalidFormat() {
    val error = stringToFractionParser.getSubmitTimeError("1/3/8")
      .getErrorMessageFromStringRes(context)
    assertThat(error).isEqualTo("Please enter a valid fraction (e.g., 5/3 or 1 2/3)")
  }

  @Test
  fun testParser_realTimeError_severalDashes_returnsInvalidFormat() {
    val error = stringToFractionParser.getSubmitTimeError("-1/-3")
      .getErrorMessageFromStringRes(context)
    assertThat(error).isEqualTo("Please enter a valid fraction (e.g., 5/3 or 1 2/3)")
  }

  @Test
  fun testParser_realTimeError_ambiguousSpacing_returnsInvalidFormat() {
    val error = stringToFractionParser.getSubmitTimeError("1 2 3/4")
      .getErrorMessageFromStringRes(context)
    assertThat(error).isEqualTo("Please enter a valid fraction (e.g., 5/3 or 1 2/3)")
  }

  @Test
  fun testParser_parseFractionFromString_throwsException() {
    val exception = assertThrows(IllegalArgumentException::class) {
      stringToFractionParser.parseFractionFromString("abc")
    }
    assertThat(exception)
      .hasMessageThat()
      .contains("Incorrectly formatted fraction: abc")
  }

  @Test
  fun testParser_parseFraction_parseRegularFraction() {
    val parseFraction = stringToFractionParser.parseFractionFromString("1/2")
    val constructedFraction = Fraction.newBuilder()
      .setNumerator(1)
      .setDenominator(2).build()
    assertThat(parseFraction).isEqualTo(constructedFraction)
  }

  @Test
  fun testParser_parseFraction_parseRegularNegativeFraction() {
    val parseFraction = stringToFractionParser.parseFractionFromString("-8/4")
    val constructedFraction = Fraction.newBuilder()
      .setIsNegative(true)
      .setNumerator(8)
      .setDenominator(4).build()
    assertThat(parseFraction).isEqualTo(constructedFraction)
  }

  @Test
  fun testParser_parseFraction_parseWholeNumber() {
    val parseFraction = stringToFractionParser.parseFractionFromString("7")
    val constructedFraction = Fraction.newBuilder()
      .setWholeNumber(7)
      .setNumerator(0)
      .setDenominator(1).build()
    assertThat(parseFraction).isEqualTo(constructedFraction)
  }

  @Test
  fun testParser_parseFraction_parseWholeNegativeNumber() {
    val parseFraction = stringToFractionParser.parseFractionFromString("-7")
    val constructedFraction = Fraction.newBuilder()
      .setIsNegative(true)
      .setWholeNumber(7)
      .setNumerator(0)
      .setDenominator(1).build()
    assertThat(parseFraction).isEqualTo(constructedFraction)
  }

  @Test
  fun testParser_parseFraction_parseMixedNumber() {
    val parseFraction = stringToFractionParser.parseFractionFromString("1 3/4")
    val constructedFraction = Fraction.newBuilder()
      .setWholeNumber(1)
      .setNumerator(3)
      .setDenominator(4).build()
    assertThat(parseFraction).isEqualTo(constructedFraction)
  }

  @Test
  fun testParser_parseFraction_parseNegativeMixedNumber() {
    val parseFraction = stringToFractionParser.parseFractionFromString("-123 456/7")
    val constructedFraction = Fraction.newBuilder()
      .setIsNegative(true)
      .setWholeNumber(123)
      .setNumerator(456)
      .setDenominator(7).build()
    assertThat(parseFraction).isEqualTo(constructedFraction)
  }

  @Test
  fun testParser_parseFraction_parseLongMixedNumber() {
    val parseFraction = stringToFractionParser
      .parseFractionFromString("1234567 1234567/1234567")
    val constructedFraction = Fraction.newBuilder()
      .setWholeNumber(1234567)
      .setNumerator(1234567)
      .setDenominator(1234567).build()
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
