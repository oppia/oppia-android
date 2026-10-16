package org.oppia.android.app.parser

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.Component
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.activity.ActivityComponentFactory
import org.oppia.android.app.activity.route.ActivityRouterModule
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.application.testing.TestingBuildFlavorModule
import org.oppia.android.app.devoptions.DeveloperOptionsModule
import org.oppia.android.app.devoptions.DeveloperOptionsStarterModule
import org.oppia.android.app.parser.StringToNumberParser.NumericInputParsingError
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.testing.activity.TestActivity
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
import org.oppia.android.data.backends.gae.NetworkConfigProdModule
import org.oppia.android.data.backends.gae.RetrofitModule
import org.oppia.android.data.backends.gae.RetrofitServiceModule
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
import org.oppia.android.domain.onboarding.ExpirationMetaDataRetrieverModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.oppialogger.analytics.CpuPerformanceSnapshotterModule
import org.oppia.android.domain.oppialogger.logscheduler.MetricLogSchedulerModule
import org.oppia.android.domain.oppialogger.loguploader.LogReportWorkerModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.platformparameter.TestPlatformParameterModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.networking.NetworkConnectionDebugUtilModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.parser.html.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.image.GlideImageLoaderModule
import org.oppia.android.util.parser.image.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [StringToNumberParser]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = StringToNumberParserTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class StringToNumberParserTest {
  @get:Rule
  val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  private lateinit var stringToNumberParser: StringToNumberParser

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    stringToNumberParser = StringToNumberParser()
  }

  @Test
  fun testRealTimeError_withStandardNotation_validInteger_returnsValid() {
    val error = stringToNumberParser.getRealTimeAnswerError("123", allowExponentialNotation = false)

    assertThat(error).isEqualTo(NumericInputParsingError.VALID)
  }

  @Test
  fun testRealTimeError_withStandardNotation_validDecimal_returnsValid() {
    val error =
      stringToNumberParser.getRealTimeAnswerError("123.45", allowExponentialNotation = false)

    assertThat(error).isEqualTo(NumericInputParsingError.VALID)
  }

  @Test
  fun testRealTimeError_withStandardNotation_negativeInteger_returnsValid() {
    val error =
      stringToNumberParser.getRealTimeAnswerError("-123", allowExponentialNotation = false)

    assertThat(error).isEqualTo(NumericInputParsingError.VALID)
  }

  @Test
  fun testRealTimeError_withStandardNotation_negativeDecimal_returnsValid() {
    val error =
      stringToNumberParser.getRealTimeAnswerError("-123.45", allowExponentialNotation = false)

    assertThat(error).isEqualTo(NumericInputParsingError.VALID)
  }

  @Test
  fun testRealTimeError_withStandardNotation_justMinusSign_returnsValid() {
    val error = stringToNumberParser.getRealTimeAnswerError("-", allowExponentialNotation = false)

    assertThat(error).isEqualTo(NumericInputParsingError.VALID)
  }

  @Test
  fun testRealTimeError_withStandardNotation_startingWithDot_returnsStartingWithFloatingPoint() {
    val error = stringToNumberParser.getRealTimeAnswerError(".5", allowExponentialNotation = false)

    assertThat(error).isEqualTo(NumericInputParsingError.STARTING_WITH_FLOATING_POINT)
  }

  @Test
  fun testRealTimeError_withStandardNotation_multipleDots_returnsInvalidFormat() {
    val error =
      stringToNumberParser.getRealTimeAnswerError("1.2.3", allowExponentialNotation = false)

    assertThat(error).isEqualTo(NumericInputParsingError.INVALID_FORMAT)
  }

  @Test
  fun testRealTimeError_withStandardNotation_negativeSignNotAtStart_returnsInvalidFormat() {
    val error =
      stringToNumberParser.getRealTimeAnswerError("12-3", allowExponentialNotation = false)

    assertThat(error).isEqualTo(NumericInputParsingError.INVALID_FORMAT)
  }

  @Test
  fun testRealTimeError_withStandardNotation_multipleNegativeSigns_returnsInvalidFormat() {
    val error =
      stringToNumberParser.getRealTimeAnswerError("--123", allowExponentialNotation = false)

    assertThat(error).isEqualTo(NumericInputParsingError.INVALID_FORMAT)
  }

  @Test
  fun testRealTimeError_withStandardNotation_containingLetters_returnsInvalidFormat() {
    val error =
      stringToNumberParser.getRealTimeAnswerError("12a3", allowExponentialNotation = false)

    assertThat(error).isEqualTo(NumericInputParsingError.INVALID_FORMAT)
  }

  @Test
  fun testRealTimeError_withStandardNotation_containingExponent_returnsInvalidFormat() {
    val error =
      stringToNumberParser.getRealTimeAnswerError("1.8e3", allowExponentialNotation = false)

    assertThat(error).isEqualTo(NumericInputParsingError.INVALID_FORMAT)
  }

  @Test
  fun testRealTimeError_withStandardNotation_containingUpperExponent_returnsInvalidFormat() {
    val error =
      stringToNumberParser.getRealTimeAnswerError("1.8E3", allowExponentialNotation = false)

    assertThat(error).isEqualTo(NumericInputParsingError.INVALID_FORMAT)
  }

  @Test
  fun testRealTimeError_withExponentialNotation_validExp_returnsValid() {
    val error =
      stringToNumberParser.getRealTimeAnswerError("1.8e3", allowExponentialNotation = true)

    assertThat(error).isEqualTo(NumericInputParsingError.VALID)
  }

  @Test
  fun testRealTimeError_withExponentialNotation_validUpperExp_returnsValid() {
    val error =
      stringToNumberParser.getRealTimeAnswerError("1.8E3", allowExponentialNotation = true)

    assertThat(error).isEqualTo(NumericInputParsingError.VALID)
  }

  @Test
  fun testRealTimeError_withExponentialNotation_validNegativeExp_returnsValid() {
    val error =
      stringToNumberParser.getRealTimeAnswerError("1.8e-3", allowExponentialNotation = true)

    assertThat(error).isEqualTo(NumericInputParsingError.VALID)
  }

  @Test
  fun testRealTimeError_withExponentialNotation_validNegativeBaseAndExp_returnsValid() {
    val error =
      stringToNumberParser.getRealTimeAnswerError("-1.8e-3", allowExponentialNotation = true)

    assertThat(error).isEqualTo(NumericInputParsingError.VALID)
  }

  @Test
  fun testRealTimeError_withExponentialNotation_validIntegerBaseExp_returnsValid() {
    val error = stringToNumberParser.getRealTimeAnswerError("2e5", allowExponentialNotation = true)

    assertThat(error).isEqualTo(NumericInputParsingError.VALID)
  }

  @Test
  fun testRealTimeError_withExponentialNotation_inProgressTypingExp_returnsValid() {
    val error = stringToNumberParser.getRealTimeAnswerError("1.8e", allowExponentialNotation = true)

    assertThat(error).isEqualTo(NumericInputParsingError.VALID)
  }

  @Test
  fun testRealTimeError_withExponentialNotation_inProgressTypingNegativeExp_returnsValid() {
    val error =
      stringToNumberParser.getRealTimeAnswerError("1.8e-", allowExponentialNotation = true)

    assertThat(error).isEqualTo(NumericInputParsingError.VALID)
  }

  @Test
  fun testRealTimeError_withExponentialNotation_startsWithExp_returnsInvalidFormat() {
    val error = stringToNumberParser.getRealTimeAnswerError("e5", allowExponentialNotation = true)

    assertThat(error).isEqualTo(NumericInputParsingError.INVALID_FORMAT)
  }

  @Test
  fun testRealTimeError_withExponentialNotation_startsWithUpperExp_returnsInvalidFormat() {
    val error = stringToNumberParser.getRealTimeAnswerError("E5", allowExponentialNotation = true)

    assertThat(error).isEqualTo(NumericInputParsingError.INVALID_FORMAT)
  }

  @Test
  fun testRealTimeError_withExponentialNotation_startsWithNegativeExp_returnsInvalidFormat() {
    val error = stringToNumberParser.getRealTimeAnswerError("-e5", allowExponentialNotation = true)

    assertThat(error).isEqualTo(NumericInputParsingError.INVALID_FORMAT)
  }

  @Test
  fun testRealTimeError_withExponentialNotation_startsWithDot_returnsFloatingPointError() {
    val error =
      stringToNumberParser.getRealTimeAnswerError(".5e3", allowExponentialNotation = true)

    assertThat(error).isEqualTo(NumericInputParsingError.STARTING_WITH_FLOATING_POINT)
  }

  @Test
  fun testRealTimeError_withExponentialNotation_decimalInExponent_returnsInvalidFormat() {
    val error =
      stringToNumberParser.getRealTimeAnswerError("1e2.5", allowExponentialNotation = true)

    assertThat(error).isEqualTo(NumericInputParsingError.INVALID_FORMAT)
  }

  @Test
  fun testRealTimeError_withExponentialNotation_multipleExponents_returnsInvalidFormat() {
    val error =
      stringToNumberParser.getRealTimeAnswerError("1e2e3", allowExponentialNotation = true)

    assertThat(error).isEqualTo(NumericInputParsingError.INVALID_FORMAT)
  }

  @Test
  fun testRealTimeError_withExponentialNotation_multipleDecimals_returnsInvalidFormat() {
    val error =
      stringToNumberParser.getRealTimeAnswerError("1.2.3e4", allowExponentialNotation = true)

    assertThat(error).isEqualTo(NumericInputParsingError.INVALID_FORMAT)
  }

  @Test
  fun testRealTimeError_withExponentialNotation_minusNotInAllowedPositions_returnsInvalidFormat() {
    val error =
      stringToNumberParser.getRealTimeAnswerError("1-e2", allowExponentialNotation = true)

    assertThat(error).isEqualTo(NumericInputParsingError.INVALID_FORMAT)
  }

  @Test
  fun testRealTimeError_withExponentialNotation_doubleMinusAtStart_returnsInvalidFormat() {
    val error =
      stringToNumberParser.getRealTimeAnswerError("--1e2", allowExponentialNotation = true)

    assertThat(error).isEqualTo(NumericInputParsingError.INVALID_FORMAT)
  }

  @Test
  fun testRealTimeError_withExponentialNotation_doubleMinusAfterExp_returnsInvalidFormat() {
    val error =
      stringToNumberParser.getRealTimeAnswerError("1e--2", allowExponentialNotation = true)

    assertThat(error).isEqualTo(NumericInputParsingError.INVALID_FORMAT)
  }

  @Test
  fun testRealTimeError_withExponentialNotation_moreThanTwoMinuses_returnsInvalidFormat() {
    val error =
      stringToNumberParser.getRealTimeAnswerError("-1e-2-3", allowExponentialNotation = true)

    assertThat(error).isEqualTo(NumericInputParsingError.INVALID_FORMAT)
  }

  @Test
  fun testSubmitTimeError_blankString_returnsEmptyInput() {
    val error = stringToNumberParser.getSubmitTimeError("", allowExponentialNotation = false)

    assertThat(error).isEqualTo(NumericInputParsingError.EMPTY_INPUT)
  }

  @Test
  fun testSubmitTimeError_whitespaceOnly_returnsEmptyInput() {
    val error = stringToNumberParser.getSubmitTimeError("   ", allowExponentialNotation = false)

    assertThat(error).isEqualTo(NumericInputParsingError.EMPTY_INPUT)
  }

  @Test
  fun testSubmitTimeError_longerThan15Chars_returnsNumberTooLong() {
    val error = stringToNumberParser.getSubmitTimeError(
      "1234567890123456",
      allowExponentialNotation = false
    )

    assertThat(error).isEqualTo(NumericInputParsingError.NUMBER_TOO_LONG)
  }

  @Test
  fun testSubmitTimeError_withStandardNotation_validNumber_returnsValid() {
    val error = stringToNumberParser.getSubmitTimeError("123.45", allowExponentialNotation = false)

    assertThat(error).isEqualTo(NumericInputParsingError.VALID)
  }

  @Test
  fun testSubmitTimeError_withStandardNotation_justMinus_returnsInvalidFormat() {
    val error = stringToNumberParser.getSubmitTimeError("-", allowExponentialNotation = false)

    assertThat(error).isEqualTo(NumericInputParsingError.INVALID_FORMAT)
  }

  @Test
  fun testSubmitTimeError_withStandardNotation_expNotation_returnsInvalidFormat() {
    val error = stringToNumberParser.getSubmitTimeError("1.8e3", allowExponentialNotation = false)

    assertThat(error).isEqualTo(NumericInputParsingError.INVALID_FORMAT)
  }

  @Test
  fun testSubmitTimeError_withStandardNotation_upperExpNotation_returnsInvalidFormat() {
    val error = stringToNumberParser.getSubmitTimeError("1.8E3", allowExponentialNotation = false)

    assertThat(error).isEqualTo(NumericInputParsingError.INVALID_FORMAT)
  }

  @Test
  fun testSubmitTimeError_withExponentialNotation_validExp_returnsValid() {
    val error = stringToNumberParser.getSubmitTimeError("1.8e3", allowExponentialNotation = true)

    assertThat(error).isEqualTo(NumericInputParsingError.VALID)
  }

  @Test
  fun testSubmitTimeError_withExponentialNotation_validNegativeExp_returnsValid() {
    val error = stringToNumberParser.getSubmitTimeError("1.8e-3", allowExponentialNotation = true)

    assertThat(error).isEqualTo(NumericInputParsingError.VALID)
  }

  @Test
  fun testSubmitTimeError_withExponentialNotation_validNegativeBaseAndExp_returnsValid() {
    val error = stringToNumberParser.getSubmitTimeError("-2.5e-4", allowExponentialNotation = true)

    assertThat(error).isEqualTo(NumericInputParsingError.VALID)
  }

  @Test
  fun testSubmitTimeError_withExponentialNotation_incompleteExp_returnsInvalidFormat() {
    val error = stringToNumberParser.getSubmitTimeError("1.8e", allowExponentialNotation = true)

    assertThat(error).isEqualTo(NumericInputParsingError.INVALID_FORMAT)
  }

  @Test
  fun testSubmitTimeError_withExponentialNotation_incompleteNegativeExp_returnsInvalidFormat() {
    val error = stringToNumberParser.getSubmitTimeError("1.8e-", allowExponentialNotation = true)

    assertThat(error).isEqualTo(NumericInputParsingError.INVALID_FORMAT)
  }

  @Test
  fun testSubmitTimeError_withExponentialNotation_infiniteNumber_returnsInvalidFormat() {
    val error =
      stringToNumberParser.getSubmitTimeError("1e999999999", allowExponentialNotation = true)

    assertThat(error).isEqualTo(NumericInputParsingError.INVALID_FORMAT)
  }

  @Test
  fun testParsingError_getErrorMessageFromStringRes_returnsCorrectLocalizedMessage() {
    runWithLaunchedActivity {
      onActivity { activity ->
        val handler = activity.appLanguageResourceHandler
        assertThat(
          NumericInputParsingError.INVALID_FORMAT.getErrorMessageFromStringRes(handler)
        ).isEqualTo("Please enter a valid number.")
        assertThat(
          NumericInputParsingError.STARTING_WITH_FLOATING_POINT
            .getErrorMessageFromStringRes(handler)
        ).isEqualTo("Please begin your answer with a number (e.g.,”0” in 0.5).")
        assertThat(
          NumericInputParsingError.NUMBER_TOO_LONG.getErrorMessageFromStringRes(handler)
        ).isEqualTo("The answer can contain at most 15 digits (0–9) or symbols (. or -).")
        assertThat(
          NumericInputParsingError.EMPTY_INPUT.getErrorMessageFromStringRes(handler)
        ).isEqualTo("Enter a number to continue.")
        assertThat(
          NumericInputParsingError.VALID.getErrorMessageFromStringRes(handler)
        ).isNull()
      }
    }
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  private fun runWithLaunchedActivity(testBlock: ActivityScenario<TestActivity>.() -> Unit) {
    ActivityScenario.launch<TestActivity>(TestActivity.createIntent(context)).use { scenario ->
      testCoroutineDispatchers.runCurrent()
      scenario.testBlock()
    }
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @Singleton
  @Component(
    modules = [
      AccessibilityTestModule::class,
      ActivityRecreatorTestModule::class,
      ActivityRouterModule::class,
      AlgebraicExpressionInputModule::class,
      ApplicationLifecycleModule::class,
      ApplicationModule::class,
      ApplicationStartupListenerModule::class,
      AssetModule::class,
      ContinueModule::class,
      CpuPerformanceSnapshotterModule::class,
      DeveloperOptionsModule::class,
      DeveloperOptionsStarterModule::class,
      DragDropSortInputModule::class,
      ExpirationMetaDataRetrieverModule::class,
      ExplorationProgressModule::class,
      ExplorationStorageModule::class,
      FakeOppiaClockModule::class,
      FractionInputModule::class,
      GcsResourceModule::class,
      GlideImageLoaderModule::class,
      HintsAndSolutionConfigModule::class,
      HintsAndSolutionProdModule::class,
      HtmlParserEntityTypeModule::class,
      ImageClickInputModule::class,
      ImageParsingModule::class,
      InteractionsModule::class,
      ItemSelectionInputModule::class,
      LocaleProdModule::class,
      LogReportWorkerModule::class,
      LogStorageModule::class,
      LoggerModule::class,
      LoggingIdentifierModule::class,
      MathEquationInputModule::class,
      MetricLogSchedulerModule::class,
      MultipleChoiceInputModule::class,
      NetworkConfigProdModule::class,
      NetworkConnectionDebugUtilModule::class,
      NetworkConnectionUtilDebugModule::class,
      NumberWithUnitsRuleModule::class,
      NumericExpressionInputModule::class,
      NumericInputRuleModule::class,
      TestPlatformParameterModule::class,
      PlatformParameterSingletonModule::class,
      QuestionModule::class,
      RatioInputModule::class,
      RetrofitModule::class,
      RetrofitServiceModule::class,
      RobolectricModule::class,
      SplitScreenInteractionModule::class,
      SyncStatusModule::class,
      TestAuthenticationModule::class,
      TestDispatcherModule::class,
      TestLogReportingModule::class,
      TestingBuildFlavorModule::class,
      TextInputRuleModule::class,
      ViewBindingShimModule::class,
      WorkManagerConfigurationModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder {
      override fun build(): TestApplicationComponent
    }

    fun inject(stringToNumberParserTest: StringToNumberParserTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerStringToNumberParserTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(stringToNumberParserTest: StringToNumberParserTest) {
      component.inject(stringToNumberParserTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
