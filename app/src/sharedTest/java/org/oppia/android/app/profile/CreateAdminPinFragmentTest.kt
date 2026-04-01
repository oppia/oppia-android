package org.oppia.android.app.profile

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertValueEquals
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.Component
import org.junit.After
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
import org.oppia.android.app.test.R
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
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.platformparameter.TestPlatformParameterModule
import org.oppia.android.testing.profile.ProfileTestHelper
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

/** Tests for [CreateAdminPinFragment]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = CreateAdminPinFragmentTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class CreateAdminPinFragmentTest {
  @get:Rule val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()
  @get:Rule val oppiaTestRule = OppiaTestRule()
  @get:Rule val composeRule = createEmptyComposeRule()
  @Inject lateinit var context: Context
  @Inject lateinit var profileTestHelper: ProfileTestHelper
  @Inject lateinit var profileManagementController: ProfileManagementController
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Before
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
  }

  @After
  fun tearDown() {
    Intents.release()
    TestPlatformParameterModule.reset()
  }

  @Test
  fun testFragment_onLaunch_allTextViewsHaveCorrectContent() {
    launch(CreateAdminPinActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      composeRule
        .onNodeWithText(context.getString(R.string.create_admin_pin_activity_header))
        .assertIsDisplayed()
      composeRule
        .onNodeWithText(context.getString(R.string.create_admin_pin_activity_message))
        .assertIsDisplayed()
      composeRule
        .onNodeWithText(context.getString(R.string.create_admin_pin_activity_enter_pin_label))
        .assertIsDisplayed()
      composeRule
        .onNodeWithText(context.getString(R.string.create_admin_pin_activity_confirm_pin_label))
        .assertIsDisplayed()
      composeRule
        .onNodeWithText(context.getString(R.string.onboarding_step_count_five))
        .assertIsDisplayed()
      composeRule
        .onNodeWithText(context.getString(R.string.onboarding_navigation_back))
        .assertIsDisplayed()
      composeRule
        .onNodeWithText(context.getString(R.string.onboarding_navigation_continue))
        .assertIsDisplayed()
    }
  }

  @Test
  @Config(qualifiers = "+land")
  fun testFragment_landscapeMode_onLaunch_allTextViewsHaveCorrectContent() {
    launch(CreateAdminPinActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      composeRule
        .onNodeWithText(context.getString(R.string.create_admin_pin_activity_header))
        .assertIsDisplayed()
      composeRule
        .onNodeWithText(context.getString(R.string.create_admin_pin_activity_message))
        .assertIsDisplayed()
      composeRule
        .onNodeWithText(context.getString(R.string.create_admin_pin_activity_enter_pin_label))
        .assertIsDisplayed()
      composeRule
        .onNodeWithText(context.getString(R.string.create_admin_pin_activity_confirm_pin_label))
        .assertIsDisplayed()
      composeRule
        .onNodeWithText(context.getString(R.string.onboarding_step_count_five))
        .assertDoesNotExist()
      composeRule
        .onNodeWithText(context.getString(R.string.onboarding_navigation_back))
        .assertIsDisplayed()
      composeRule
        .onNodeWithText(context.getString(R.string.onboarding_navigation_continue))
        .assertIsDisplayed()
    }
  }

  @Test
  fun testFragment_clickContinue_withEmptyPin_showsBlankPinError() {
    launch(CreateAdminPinActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      composeRule
        .onNodeWithText(context.getString(R.string.onboarding_navigation_continue))
        .performClick()

      testCoroutineDispatchers.runCurrent()

      composeRule
        .onNodeWithText(context.getString(R.string.create_admin_pin_activity_blank_error))
        .assertIsDisplayed()
    }
  }

  @Test
  fun testFragment_clickContinue_withFilledPinAndEmptyConfirmPin_showsMismatchError() {
    launch(CreateAdminPinActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      composeRule
        .onNodeWithText(context.getString(R.string.create_admin_pin_activity_enter_pin_label))
        .performTextInput("12345")

      composeRule
        .onNodeWithText(context.getString(R.string.onboarding_navigation_continue))
        .performClick()

      testCoroutineDispatchers.runCurrent()

      composeRule
        .onNodeWithText(context.getString(R.string.create_admin_pin_activity_mismatch_error))
        .assertIsDisplayed()
    }
  }

  @Test
  fun testFragment_clickContinue_withMismatchedPins_showsMismatchError_continueButtonIsDisabled() {
    launch(CreateAdminPinActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      composeRule
        .onNodeWithText(context.getString(R.string.create_admin_pin_activity_enter_pin_label))
        .performTextInput("12345")

      composeRule
        .onNodeWithText(context.getString(R.string.create_admin_pin_activity_confirm_pin_label))
        .performTextInput("54321")

      composeRule
        .onNodeWithText(context.getString(R.string.create_admin_pin_activity_mismatch_error))
        .assertIsDisplayed()

      composeRule
        .onNodeWithText(context.getString(R.string.onboarding_navigation_continue))
        .assertIsNotEnabled()
    }
  }

  @Test
  fun testFragment_inputThreeDigitPin_showsPinLengthError_continueButtonIsDisabled() {
    launch(CreateAdminPinActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      composeRule
        .onNodeWithText(context.getString(R.string.create_admin_pin_activity_enter_pin_label))
        .performTextInput("123")

      composeRule
        .onNodeWithText(context.getString(R.string.create_admin_pin_activity_length_error))
        .assertIsDisplayed()

      composeRule
        .onNodeWithText(context.getString(R.string.onboarding_navigation_continue))
        .assertIsNotEnabled()
    }
  }

  @Test
  fun testFragment_inputShortPin_inputSameShortConfirmPin_showsErrorForPin_continueIsDisabled() {
    launch(CreateAdminPinActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      composeRule
        .onNodeWithText(context.getString(R.string.create_admin_pin_activity_enter_pin_label))
        .performClick()
        .performTextInput("123")

      testCoroutineDispatchers.runCurrent()

      // Verify that the length error is shown for the PIN field.
      composeRule
        .onNodeWithText(context.getString(R.string.create_admin_pin_activity_length_error))
        .assertIsDisplayed()

      composeRule
        .onNodeWithText(context.getString(R.string.create_admin_pin_activity_confirm_pin_label))
        .performClick()
        .performTextInput("123")

      // Verify that the length error is shown for the confirm PIN field.
      composeRule
        .onNodeWithText(context.getString(R.string.create_admin_pin_activity_mismatch_error))
        .assertIsDisplayed()

      composeRule
        .onNodeWithText(context.getString(R.string.onboarding_navigation_continue))
        .assertIsNotEnabled()
    }
  }

  @Test
  fun testFragment_inputShortPin_inputDiffShortConfirmPin_showsErrorForBoth_continueIsDisabled() {
    launch(CreateAdminPinActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      composeRule
        .onNodeWithText(context.getString(R.string.create_admin_pin_activity_enter_pin_label))
        .performClick()
        .performTextInput("123")

      composeRule
        .onNodeWithText(context.getString(R.string.create_admin_pin_activity_length_error))
        .assertIsDisplayed()

      composeRule
        .onNodeWithText(context.getString(R.string.create_admin_pin_activity_confirm_pin_label))
        .performClick()
        .performTextInput("456")

      composeRule
        .onNodeWithText(context.getString(R.string.create_admin_pin_activity_mismatch_error))
        .assertIsDisplayed()

      composeRule
        .onNodeWithText(context.getString(R.string.onboarding_navigation_continue))
        .assertIsNotEnabled()
    }
  }

  @Test
  fun testFragment_validPin_shortConfirm_showsMismatchError_continueDisabled() {
    launch(CreateAdminPinActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      composeRule
        .onNodeWithText(context.getString(R.string.create_admin_pin_activity_enter_pin_label))
        .performClick()
        .performTextInput("12345")

      // Enter a too-short confirm PIN.
      composeRule
        .onNodeWithText(context.getString(R.string.create_admin_pin_activity_confirm_pin_label))
        .performClick()
        .performTextInput("12")

      composeRule
        .onNodeWithText(context.getString(R.string.create_admin_pin_activity_mismatch_error))
        .assertIsDisplayed()

      composeRule
        .onNodeWithText(context.getString(R.string.onboarding_navigation_continue))
        .assertIsNotEnabled()
    }
  }

  @Test
  fun testFragment_enterMatchingConfirmPin_afterPinLengthError_enablesContinue() {
    launch(CreateAdminPinActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      // Start with a short PIN to trigger length error.
      val enterPinNode = context.getString(R.string.create_admin_pin_activity_enter_pin_label)
      composeRule.onNodeWithText(enterPinNode).performTextInput("12")

      composeRule
        .onNodeWithText(context.getString(R.string.create_admin_pin_activity_length_error))
        .assertIsDisplayed()

      composeRule
        .onNodeWithText(context.getString(R.string.onboarding_navigation_continue))
        .assertIsNotEnabled()

      composeRule.onNodeWithText(enterPinNode)
        .performClick()
        .performTextInput("345")

      composeRule
        .onNodeWithText(context.getString(R.string.create_admin_pin_activity_length_error))
        .assertDoesNotExist()

      composeRule
        .onNodeWithText(context.getString(R.string.create_admin_pin_activity_confirm_pin_label))
        .performClick()
        .performTextInput("12345")

      // Enter matching confirm PIN; continue should be enabled now.
      composeRule
        .onNodeWithText(context.getString(R.string.onboarding_navigation_continue))
        .assertIsEnabled()
    }
  }

  @Test
  fun testFragment_imeActionDone_withMatchingValidPins_navigatesToProfileChooser() {
    launch(CreateAdminPinActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      composeRule
        .onNodeWithText(context.getString(R.string.create_admin_pin_activity_enter_pin_label))
        .performClick()
        .performTextInput("12345")

      composeRule
        .onNodeWithText(context.getString(R.string.create_admin_pin_activity_confirm_pin_label))
        .performClick()
        .performTextInput("12345")

      composeRule
        .onNodeWithText(context.getString(R.string.create_admin_pin_activity_confirm_pin_label))
        .performClick()
        .performImeAction()

      intended(hasComponent(ProfileChooserActivity::class.java.name))
    }
  }

  @Test
  fun testFragment_clickContinue_withMatchingValidPins_navigatesToProfileChooser() {
    launch(CreateAdminPinActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      composeRule
        .onNodeWithText(context.getString(R.string.create_admin_pin_activity_enter_pin_label))
        .performClick()
        .performTextInput("12345")

      composeRule
        .onNodeWithText(context.getString(R.string.create_admin_pin_activity_confirm_pin_label))
        .performClick()
        .performTextInput("12345")

      composeRule
        .onNodeWithText(context.getString(R.string.onboarding_navigation_continue))
        .performClick()

      intended(hasComponent(ProfileChooserActivity::class.java.name))
    }
  }

  @Test
  fun testFragment_onBackButtonClicked_currentScreenIsDestroyed() {
    launch(CreateAdminPinActivity::class.java).use { scenario ->
      testCoroutineDispatchers.runCurrent()

      scenario.onActivity { activity ->
        composeRule.onNodeWithText(context.getString(R.string.onboarding_navigation_back))
          .performClick()

        assertThat(activity.isFinishing).isTrue()
      }
    }
  }

  @Test
  fun testFragment_inputPin_onlyAcceptsDigits() {
    launch(CreateAdminPinActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      // Try to input non-digit characters.
      composeRule
        .onNodeWithText(context.getString(R.string.create_admin_pin_activity_enter_pin_label))
        .performClick()
        .performTextInput("abc12def")

      // Should only accept the digits.
      composeRule
        .onNodeWithText(context.getString(R.string.create_admin_pin_activity_enter_pin_label))
        .assertValueEquals("12")
    }
  }

  @Test
  fun testFragment_inputConfirmPin_onlyAcceptsDigits() {
    launch(CreateAdminPinActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      // Try to input non-digit characters.
      composeRule
        .onNodeWithText(context.getString(R.string.create_admin_pin_activity_confirm_pin_label))
        .performClick()
        .performTextInput("abc12def")

      // Should only accept the digits.
      composeRule
        .onNodeWithText(context.getString(R.string.create_admin_pin_activity_confirm_pin_label))
        .assertValueEquals("12")
    }
  }

  @Test
  fun testFragment_inputPin_limitsToFiveDigits() {
    launch(CreateAdminPinActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      // Try to input more than 5 digits.
      composeRule
        .onNodeWithText(context.getString(R.string.create_admin_pin_activity_enter_pin_label))
        .performClick()
        .performTextInput("123456789")

      testCoroutineDispatchers.runCurrent()

      // Should only accept the first 5 digits.
      composeRule
        .onNodeWithText(context.getString(R.string.create_admin_pin_activity_enter_pin_label))
        .assertValueEquals("12345")
    }
  }

  @Test
  fun testFragment_inputConfirmPin_limitsToFiveDigits() {
    launch(CreateAdminPinActivity::class.java).use {
      testCoroutineDispatchers.runCurrent()

      // Try to input more than 5 digits.
      composeRule
        .onNodeWithText(context.getString(R.string.create_admin_pin_activity_confirm_pin_label))
        .performClick()
        .performTextInput("123456789")

      // Should only accept the first 5 digits.
      composeRule
        .onNodeWithText(context.getString(R.string.create_admin_pin_activity_confirm_pin_label))
        .assertValueEquals("12345")
    }
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  // TODO(#89): Move this to a common test application component.
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
      TestPlatformParameterModule::class,
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

    fun inject(createAdminPinFragmentTest: CreateAdminPinFragmentTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerCreateAdminPinFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(createAdminPinFragmentTest: CreateAdminPinFragmentTest) {
      component.inject(createAdminPinFragmentTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
