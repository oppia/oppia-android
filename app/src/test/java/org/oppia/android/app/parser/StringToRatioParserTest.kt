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
import org.oppia.android.app.model.RatioExpression
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
import org.oppia.android.domain.platformparameter.testing.PlatformParameterInitializationInjector
import org.oppia.android.domain.platformparameter.testing.PlatformParameterInitializationInjectorProvider
import org.oppia.android.domain.platformparameter.testing.PlatformParameterTestModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.assertThrows
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
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
import org.oppia.android.util.networking.NetworkConnectionDebugUtilModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.parser.html.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.image.GlideImageLoaderModule
import org.oppia.android.util.parser.image.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [StringToRatioParser]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = StringToRatioParserTest.TestApplication::class, qualifiers = "port-xxhdpi")
class StringToRatioParserTest {
  @get:Rule val oppiaTestRule = OppiaTestRule()
  @get:Rule val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @Inject lateinit var context: Context
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  private lateinit var stringToRatioParser: StringToRatioParser

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    stringToRatioParser = StringToRatioParser()
  }

  @Test
  fun testParser_realtimeError_answerWithAlphabets_returnsInvalidCharsError() {
    runWithLaunchedActivity {
      onActivity { activity ->
        val error =
          stringToRatioParser.getRealTimeAnswerError("abc")
            .getErrorMessageFromStringRes(activity.appLanguageResourceHandler)
        assertThat(error).isEqualTo(
          "Please write a ratio that consists of digits separated by colons (e.g. 1:2 or 1:2:3)."
        )
      }
    }
  }

  @Test
  fun testParser_realtimeError_answerWithTwoAdjacentColons_returnsInvalidColonsError() {
    runWithLaunchedActivity {
      onActivity { activity ->
        val error = stringToRatioParser.getRealTimeAnswerError("1::2")
          .getErrorMessageFromStringRes(activity.appLanguageResourceHandler)
        assertThat(error).isEqualTo("Your answer has two colons (:) next to each other.")
      }
    }
  }

  @Test
  fun testParser_realtimeError_answerWithCorrectRatio_returnsValid() {
    runWithLaunchedActivity {
      onActivity { activity ->
        val error = stringToRatioParser.getRealTimeAnswerError("1:2:3")
          .getErrorMessageFromStringRes(activity.appLanguageResourceHandler)
        assertThat(error).isEqualTo(null)
      }
    }
  }

  @Test
  fun testParser_submitTimeError_numberOfTermsZero_returnsValid() {
    runWithLaunchedActivity {
      onActivity { activity ->
        val error = stringToRatioParser.getSubmitTimeError("1:2:3:4", numberOfTerms = 0)
          .getErrorMessageFromStringRes(activity.appLanguageResourceHandler)
        assertThat(error).isEqualTo(null)
      }
    }
  }

  @Test
  fun testParser_submitTimeError_numberOfTermsThree_returnsInvalidSizeError() {
    runWithLaunchedActivity {
      onActivity { activity ->
        val error = stringToRatioParser.getSubmitTimeError("1:2:3:4", numberOfTerms = 3)
          .getErrorMessageFromStringRes(activity.appLanguageResourceHandler)
        assertThat(error).isEqualTo("Number of terms is not equal to the required terms.")
      }
    }
  }

  @Test
  fun testParser_submitTimeError_numberOfTermsFour_returnsValid() {
    runWithLaunchedActivity {
      onActivity { activity ->
        val error = stringToRatioParser.getSubmitTimeError("1:2:3:4", numberOfTerms = 4)
          .getErrorMessageFromStringRes(activity.appLanguageResourceHandler)
        assertThat(error).isEqualTo(null)
      }
    }
  }

  @Test
  fun testParser_submitTimeError_numberOfTermsFive_returnsInvalidSizeError() {
    runWithLaunchedActivity {
      onActivity { activity ->
        val error = stringToRatioParser.getSubmitTimeError("1:2:3:4", numberOfTerms = 5)
          .getErrorMessageFromStringRes(activity.appLanguageResourceHandler)
        assertThat(error).isEqualTo("Number of terms is not equal to the required terms.")
      }
    }
  }

  @Test
  fun testParser_submitTimeError_answerWithOneExtraColon_returnInvalidFormatError() {
    runWithLaunchedActivity {
      onActivity { activity ->
        val error =
          stringToRatioParser.getSubmitTimeError("1:2:3:", numberOfTerms = 3)
            .getErrorMessageFromStringRes(activity.appLanguageResourceHandler)
        assertThat(error).isEqualTo("Please enter a valid ratio (e.g. 1:2 or 1:2:3).")
      }
    }
  }

  @Test
  fun testParser_realtimeError_answerWithMixedFrationRatio_returnInvalidFormatError() {
    runWithLaunchedActivity {
      onActivity { activity ->
        val error = stringToRatioParser.getSubmitTimeError("1/2:3:4", 0)
          .getErrorMessageFromStringRes(activity.appLanguageResourceHandler)
        assertThat(error).isEqualTo("Please enter a valid ratio (e.g. 1:2 or 1:2:3).")
      }
    }
  }

  @Test
  fun testParser_submitTimeError_answerWithZeroComponent_returnsIncludesZero() {
    runWithLaunchedActivity {
      onActivity { activity ->
        val error =
          stringToRatioParser.getSubmitTimeError("1:2:0", numberOfTerms = 3)
            .getErrorMessageFromStringRes(activity.appLanguageResourceHandler)
        assertThat(error).isEqualTo("Ratios cannot have 0 as an element.")
      }
    }
  }

  @Test
  fun testParser_submitTimeError_returnsValid() {
    runWithLaunchedActivity {
      onActivity { activity ->
        val error = stringToRatioParser.getSubmitTimeError("1:2:3:4", numberOfTerms = 4)
          .getErrorMessageFromStringRes(activity.appLanguageResourceHandler)
        assertThat(error).isEqualTo(null)
      }
    }
  }

  @Test
  fun testParser_parseRatioOrNull_returnsRatioExpression() {
    val parsedRatio = stringToRatioParser.parseRatioOrNull("1:2:3:4")
    val constructedRatio = createRatio(listOf(1, 2, 3, 4))
    assertThat(parsedRatio).isEqualTo(constructedRatio)
  }

  @Test
  fun testParser_parseRatioOrNull_returnNull() {
    val parsedRatio = stringToRatioParser.parseRatioOrNull("1:2:3:4:")
    assertThat(parsedRatio).isEqualTo(null)
  }

  @Test
  fun testParser_parseRatioOrThrow_ratioWithWhiteSpaces_returnRatioExpression() {
    val parsedRatio = stringToRatioParser.parseRatioOrThrow("1   :   2   : 3: 4")
    val constructedRatio = createRatio(listOf(1, 2, 3, 4))
    assertThat(parsedRatio).isEqualTo(constructedRatio)
  }

  @Test
  fun testParser_parseRatioOrThrow_ratioWithInvalidRatio_throwsException() {
    val exception = assertThrows<IllegalArgumentException>() {
      stringToRatioParser.parseRatioOrThrow("a:b:c")
    }
    assertThat(exception)
      .hasMessageThat()
      .contains("Incorrectly formatted ratio: a:b:c")
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

  private fun createRatio(element: List<Int>): RatioExpression {
    return RatioExpression.newBuilder().addAllRatioComponent(element).build()
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
      CachingTestModule::class,
      ContinueModule::class,
      CpuPerformanceSnapshotterModule::class,
      DeveloperOptionsModule::class,
      DeveloperOptionsStarterModule::class,
      DragDropSortInputModule::class,
      ExpirationMetaDataRetrieverModule::class,
      ExplorationProgressModule::class,
      ExplorationStorageModule::class,
      FakeOppiaClockModule::class,
      FirebaseLogUploaderModule::class,
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
      PlatformParameterTestModule::class,
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
  interface TestApplicationComponent :
    ApplicationComponent,
    PlatformParameterInitializationInjector {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder {
      override fun build(): TestApplicationComponent
    }

    fun inject(stringToRatioParserTest: StringToRatioParserTest)
  }

  class TestApplication :
    Application(),
    ActivityComponentFactory,
    ApplicationInjectorProvider,
    PlatformParameterInitializationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerStringToRatioParserTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(stringToRatioParserTest: StringToRatioParserTest) {
      component.inject(stringToRatioParserTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component

    override fun getPlatformParameterInitializationInjector() = component
  }
}
