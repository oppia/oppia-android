package org.oppia.android.app.parser

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
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
import org.oppia.android.domain.onboarding.ExpirationMetaDataRetrieverModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.oppialogger.analytics.CpuPerformanceSnapshotterModule
import org.oppia.android.domain.oppialogger.logscheduler.MetricLogSchedulerModule
import org.oppia.android.domain.oppialogger.loguploader.LogReportWorkerModule
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.math.FractionParser
import org.oppia.android.util.math.FractionParser.FractionParsingError
import org.oppia.android.util.networking.NetworkConnectionDebugUtilModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.parser.html.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.image.GlideImageLoaderModule
import org.oppia.android.util.parser.image.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Singleton

/** Tests for [FractionParsingUiError]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = FractionParsingUiErrorTest.TestApplication::class, qualifiers = "port-xxhdpi")
class FractionParsingUiErrorTest {
  @get:Rule
  val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @get:Rule
  var activityRule =
    ActivityScenarioRule<TestActivity>(
      TestActivity.createIntent(ApplicationProvider.getApplicationContext())
    )

  private lateinit var fractionParser: FractionParser

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    fractionParser = FractionParser()
  }

  @Test
  fun testSubmitTimeError_validMixedNumber_noErrorMessage() {
    activityRule.scenario.onActivity { activity ->
      val errorMessage = fractionParser.getSubmitTimeError("11 22/33")
        .toUiError()
        .getErrorMessageFromStringRes(activity.appLanguageResourceHandler)
      assertThat(errorMessage).isNull()
    }
  }

  @Test
  fun testSubmitTimeError_tenDigitNumber_numberTooLong_hasRelevantErrorMessage() {
    activityRule.scenario.onActivity { activity ->
      val errorMessage = fractionParser.getSubmitTimeError("0123456789")
        .toUiError()
        .getErrorMessageFromStringRes(activity.appLanguageResourceHandler)
      assertThat(errorMessage)
        .isEqualTo("None of the numbers in the fraction should have more than 7 digits.")
    }
  }

  @Test
  fun testSubmitTimeError_nonDigits_invalidFormat_hasRelevantErrorMessage() {
    activityRule.scenario.onActivity { activity ->
      val errorMessage = fractionParser.getSubmitTimeError("jdhfc")
        .toUiError()
        .getErrorMessageFromStringRes(activity.appLanguageResourceHandler)
      assertThat(errorMessage)
        .isEqualTo("Please enter a valid fraction (e.g., 5/3 or 1 2/3)")
    }
  }

  @Test
  fun testSubmitTimeError_divisionByZero_hasRelevantErrorMessage() {
    activityRule.scenario.onActivity { activity ->
      val errorMessage = fractionParser.getSubmitTimeError("123/0")
        .toUiError()
        .getErrorMessageFromStringRes(activity.appLanguageResourceHandler)
      assertThat(errorMessage).isEqualTo("Please do not put 0 in the denominator")
    }
  }

  @Test
  fun testSubmitTimeError_ambiguousSpacing_invalidFormat_hasRelevantErrorMessage() {
    activityRule.scenario.onActivity { activity ->
      val errorMessage = fractionParser.getSubmitTimeError("1 2 3/4")
        .toUiError()
        .getErrorMessageFromStringRes(activity.appLanguageResourceHandler)
      assertThat(errorMessage)
        .isEqualTo("Please enter a valid fraction (e.g., 5/3 or 1 2/3)")
    }
  }

  @Test
  fun testSubmitTimeError_emptyString_invalidFormat_hasRelevantErrorMessage() {
    activityRule.scenario.onActivity { activity ->
      val errorMessage = fractionParser.getSubmitTimeError("")
        .toUiError()
        .getErrorMessageFromStringRes(activity.appLanguageResourceHandler)
      assertThat(errorMessage)
        .isEqualTo("Enter a fraction to continue.")
    }
  }

  @Test
  fun testSubmitTimeError_noDenominator_invalidFormat_hasRelevantErrorMessage() {
    activityRule.scenario.onActivity { activity ->
      val errorMessage = fractionParser.getSubmitTimeError("3/")
        .toUiError()
        .getErrorMessageFromStringRes(activity.appLanguageResourceHandler)
      assertThat(errorMessage)
        .isEqualTo("Please enter a valid fraction (e.g., 5/3 or 1 2/3)")
    }
  }

  @Test
  fun testRealTimeError_validRegularFraction_noErrorMessage() {
    activityRule.scenario.onActivity { activity ->
      val errorMessage = fractionParser.getRealTimeAnswerError("2/3")
        .toUiError()
        .getErrorMessageFromStringRes(activity.appLanguageResourceHandler)
      assertThat(errorMessage).isNull()
    }
  }

  @Test
  fun testRealTimeError_nonDigits_invalidChars_hasRelevantErrorMessage() {
    activityRule.scenario.onActivity { activity ->
      val errorMessage = fractionParser.getRealTimeAnswerError("abc")
        .toUiError()
        .getErrorMessageFromStringRes(activity.appLanguageResourceHandler)
      assertThat(errorMessage)
        .isEqualTo("Please only use numerical digits, spaces or forward slashes (/)")
    }
  }

  @Test
  fun testRealTimeError_noNumerator_invalidFormat_hasRelevantErrorMessage() {
    activityRule.scenario.onActivity { activity ->
      val errorMessage = fractionParser.getRealTimeAnswerError("/3")
        .toUiError()
        .getErrorMessageFromStringRes(activity.appLanguageResourceHandler)
      assertThat(errorMessage)
        .isEqualTo("Please enter a valid fraction (e.g., 5/3 or 1 2/3)")
    }
  }

  @Test
  fun testRealTimeError_severalSlashes_invalidFormat_hasRelevantErrorMessage() {
    activityRule.scenario.onActivity { activity ->
      val errorMessage = fractionParser.getRealTimeAnswerError("1/3/8")
        .toUiError()
        .getErrorMessageFromStringRes(activity.appLanguageResourceHandler)
      assertThat(errorMessage)
        .isEqualTo("Please enter a valid fraction (e.g., 5/3 or 1 2/3)")
    }
  }

  @Test
  fun testRealTimeError_severalDashes_invalidFormat_hasRelevantErrorMessage() {
    activityRule.scenario.onActivity { activity ->
      val errorMessage = fractionParser.getRealTimeAnswerError("-1/-3")
        .toUiError()
        .getErrorMessageFromStringRes(activity.appLanguageResourceHandler)
      assertThat(errorMessage)
        .isEqualTo("Please enter a valid fraction (e.g., 5/3 or 1 2/3)")
    }
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  private companion object {
    private fun FractionParsingError.toUiError(): FractionParsingUiError =
      FractionParsingUiError.createFromParsingError(this)
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @Singleton
  @Component(
    modules = [
      TestDispatcherModule::class, ApplicationModule::class, RobolectricModule::class,
      PlatformParameterModule::class, PlatformParameterSingletonModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      GcsResourceModule::class, GlideImageLoaderModule::class, ImageParsingModule::class,
      HtmlParserEntityTypeModule::class, QuestionModule::class, TestLogReportingModule::class,
      AccessibilityTestModule::class, LogStorageModule::class, CachingTestModule::class,
      ExpirationMetaDataRetrieverModule::class,
      ViewBindingShimModule::class, RatioInputModule::class, WorkManagerConfigurationModule::class,
      ApplicationStartupListenerModule::class, LogReportWorkerModule::class,
      HintsAndSolutionConfigModule::class, HintsAndSolutionProdModule::class,
      FirebaseLogUploaderModule::class, FakeOppiaClockModule::class,
      DeveloperOptionsStarterModule::class, DeveloperOptionsModule::class,
      ExplorationStorageModule::class, NetworkModule::class, NetworkConfigProdModule::class,
      NetworkConnectionUtilDebugModule::class, NetworkConnectionDebugUtilModule::class,
      AssetModule::class, LocaleProdModule::class, ActivityRecreatorTestModule::class,
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
    interface Builder : ApplicationComponent.Builder {
      override fun build(): TestApplicationComponent
    }

    fun inject(fractionParsingUiErrorTest: FractionParsingUiErrorTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerFractionParsingUiErrorTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(fractionParsingUiErrorTest: FractionParsingUiErrorTest) {
      component.inject(fractionParsingUiErrorTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
