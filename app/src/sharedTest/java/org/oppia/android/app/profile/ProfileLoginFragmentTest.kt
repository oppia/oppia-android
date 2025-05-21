package org.oppia.android.app.profile

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Component
import javax.inject.Inject
import javax.inject.Singleton
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
import org.oppia.android.app.classroom.ClassroomListActivity
import org.oppia.android.app.devoptions.DeveloperOptionsModule
import org.oppia.android.app.devoptions.DeveloperOptionsStarterModule
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.test.R
import org.oppia.android.app.translation.AppLanguageLocaleHandler
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

/** Tests for [ProfileLoginFragment]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = ProfileLoginFragmentTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class ProfileLoginFragmentTest {
  @get:Rule
  val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()
  @get:Rule
  val oppiaTestRule = OppiaTestRule()

  @Inject
  lateinit var profileTestHelper: ProfileTestHelper
  @Inject
  lateinit var profileManagementController: ProfileManagementController
  @Inject
  lateinit var context: Context
  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @get:Rule
  val composeRule = createEmptyComposeRule()
  @Inject
  lateinit var appLanguageLocaleHandler: AppLanguageLocaleHandler

  private lateinit var scenario: ActivityScenario<ProfileLoginActivity>

  private val testProfileId = ProfileId.newBuilder().setInternalId(0).build()

  @Before
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
    testCoroutineDispatchers.registerIdlingResource()
  }

  @After
  fun tearDown() {
    TestPlatformParameterModule.reset()
    testCoroutineDispatchers.unregisterIdlingResource()
    Intents.release()
  }

  @Test
  fun testFragment_onLaunch_allTextViewsHaveCorrectContent() {
    profileTestHelper.addOnlyAdminProfile()
    setUpTestApplicationComponent()
    scenario = ActivityScenario.launch(ProfileLoginActivity::class.java)
    testCoroutineDispatchers.runCurrent()

    composeRule.onNodeWithTag(GREETING_TEST_TAG)
      .assertTextContains(context.getString(R.string.profile_login_activity_greeting_text, "Admin"))
      .assertIsDisplayed()
    composeRule.onNodeWithTag(PROMPT_TEST_TAG).assertIsDisplayed()
      .assertTextContains(context.getString(R.string.profile_login_activity_enter_pin_prompt))
      .assertIsDisplayed()
    composeRule.onNodeWithTag(FORGOT_PIN_TEST_TAG)
      .assertTextContains(context.getString(R.string.profile_login_activity_forgot_pin_text))
      .assertIsDisplayed()
  }

  @Test
  fun testFragment_onLaunch_adminProfile_fivePinInputBoxesAreDisplayed() {
    profileTestHelper.addOnlyAdminProfile()
    setUpTestApplicationComponent()
    scenario = ActivityScenario.launch(ProfileLoginActivity::class.java)
    testCoroutineDispatchers.runCurrent()
    repeat(5) { index ->
      composeRule
        .onNodeWithTag(PIN_BOX_TEST_TAG + index, useUnmergedTree = true)
        .assertIsDisplayed()
    }

    // Ensure there's no 6th box
    composeRule
      .onNodeWithTag("pin_box_5", useUnmergedTree = true)
      .assertDoesNotExist()
  }

  @Test
  fun testFragment_onLaunch_learnerProfile_threePinInputBoxesAreDisplayed() {
    profileTestHelper.addMoreProfiles(1)
    setUpTestApplicationComponent()
    scenario = ActivityScenario.launch(ProfileLoginActivity::class.java)
    testCoroutineDispatchers.runCurrent()
    repeat(3) { index ->
      composeRule
        .onNodeWithTag(PIN_BOX_TEST_TAG + index, useUnmergedTree = true)
        .assertIsDisplayed()
    }

    // Ensure there's no 4th box
    composeRule
      .onNodeWithTag("pin_box_3", useUnmergedTree = true)
      .assertDoesNotExist()
  }

  @Test
  fun testFragment_onLaunch_errorMessageDoesNotShow() {
    profileTestHelper.addMoreProfiles(1)
    setUpTestApplicationComponent()
    scenario = ActivityScenario.launch(ProfileLoginActivity::class.java)
    testCoroutineDispatchers.runCurrent()

    composeRule
      .onNodeWithTag(PIN_ERROR_TEST_TAG)
      .assertDoesNotExist()
  }

  @Test
  fun testFragment_nonAdmin_enterTwoDigits_doesNotTriggerLogin() {
    profileTestHelper.addMoreProfiles(1)
    setUpTestApplicationComponent()
    scenario = ActivityScenario.launch(ProfileLoginActivity::class.java)
    testCoroutineDispatchers.runCurrent()

    composeRule
      .onNodeWithTag(PIN_INPUT_TEST_TAG, useUnmergedTree = true)
      .performClick()
      .performTextInput("12")

    composeRule
      .onNodeWithTag(PIN_BOX_TEST_TAG + 0, useUnmergedTree = true)
      .assertContentDescriptionEquals("1")
      .assertIsDisplayed()

    composeRule
      .onNodeWithTag(PIN_BOX_TEST_TAG + 1, useUnmergedTree = true)
      .assertContentDescriptionEquals("2")
      .assertIsDisplayed()

    composeRule
      .onNodeWithTag(PIN_BOX_TEST_TAG + 2, useUnmergedTree = true)
      .assertContentDescriptionEquals("")
      .assertIsDisplayed()
  }

  @Test
  fun testFragment_nonAdmin_enterThreeDigits_triggersLoginAndOpensHomeScreen() {
    profileTestHelper.addMoreProfiles(1)
    setUpTestApplicationComponent()
    scenario = ActivityScenario.launch(ProfileLoginActivity::class.java)
    testCoroutineDispatchers.runCurrent()

    composeRule
      .onNodeWithTag(PIN_INPUT_TEST_TAG, useUnmergedTree = true)
      .performClick()
      .performTextInput("123")

    testCoroutineDispatchers.runCurrent()

    intended(hasComponent(ClassroomListActivity::class.java.name))
  }

  @Test
  fun testFragment_nonAdmin_enterWrongThreeDigits_showsErrorMessage() {
    profileTestHelper.addMoreProfiles(1)
    setUpTestApplicationComponent()
    scenario = ActivityScenario.launch(ProfileLoginActivity::class.java)
    testCoroutineDispatchers.runCurrent()

    composeRule
      .onNodeWithTag(PIN_INPUT_TEST_TAG, useUnmergedTree = true)
      .performClick()
      .performTextInput("111")

    composeRule
      .onNodeWithTag(PIN_ERROR_TEST_TAG)
      .assertTextContains(context.getString(R.string.profile_login_activity_pin_error))
      .assertIsDisplayed()
  }

  fun testFragment_adminProfile_enterFourDigits_doesNotTriggerLogin() {
    profileTestHelper.addOnlyAdminProfile()
    setUpTestApplicationComponent()
    scenario = ActivityScenario.launch(ProfileLoginActivity::class.java)
    testCoroutineDispatchers.runCurrent()

    composeRule
      .onNodeWithTag(PIN_INPUT_TEST_TAG, useUnmergedTree = true)
      .performClick()
      .performTextInput("1234")

    composeRule
      .onNodeWithTag(PIN_BOX_TEST_TAG + 0, useUnmergedTree = true)
      .assertContentDescriptionEquals("1")
      .assertIsDisplayed()

    composeRule
      .onNodeWithTag(PIN_BOX_TEST_TAG + 1, useUnmergedTree = true)
      .assertContentDescriptionEquals("2")
      .assertIsDisplayed()

    composeRule
      .onNodeWithTag(PIN_BOX_TEST_TAG + 2, useUnmergedTree = true)
      .assertContentDescriptionEquals("3")
      .assertIsDisplayed()

    composeRule
      .onNodeWithTag(PIN_BOX_TEST_TAG + 3, useUnmergedTree = true)
      .assertContentDescriptionEquals("4")
      .assertIsDisplayed()

    composeRule
      .onNodeWithTag(PIN_BOX_TEST_TAG + 4, useUnmergedTree = true)
      .assertContentDescriptionEquals("")
      .assertIsDisplayed()
  }

  @Test
  fun testFragment_adminProfile_enterFiveDigits_triggersLoginAndOpensHomeScreen() {
    profileTestHelper.addOnlyAdminProfile()
    setUpTestApplicationComponent()
    scenario = ActivityScenario.launch(ProfileLoginActivity::class.java)
    testCoroutineDispatchers.runCurrent()

    composeRule
      .onNodeWithTag(PIN_INPUT_TEST_TAG, useUnmergedTree = true)
      .performClick()
      .performTextInput("12345")

    testCoroutineDispatchers.runCurrent()

    intended(hasComponent(ClassroomListActivity::class.java.name))
  }

  @Test
  fun testFragment_adminProfile_enterWrongFiveDigits_showsErrorMessage() {
    profileTestHelper.addOnlyAdminProfile()
    setUpTestApplicationComponent()
    scenario = ActivityScenario.launch(ProfileLoginActivity::class.java)
    testCoroutineDispatchers.runCurrent()

    composeRule
      .onNodeWithTag(PIN_INPUT_TEST_TAG, useUnmergedTree = true)
      .performClick()
      .performTextInput("11111")

    composeRule
      .onNodeWithTag(PIN_ERROR_TEST_TAG)
      .assertTextContains(context.getString(R.string.profile_login_activity_pin_error))
      .assertIsDisplayed()
  }

  @Test
  fun testFragment_enterWrongPin_showsErrorMessage_thenClearsPinAndError() {
    profileTestHelper.addOnlyAdminProfile()
    setUpTestApplicationComponent()
    scenario = ActivityScenario.launch(ProfileLoginActivity::class.java)
    testCoroutineDispatchers.runCurrent()

    composeRule
      .onNodeWithTag(PIN_INPUT_TEST_TAG, useUnmergedTree = true)
      .performClick()
      .performTextInput("11111")

    composeRule
      .onNodeWithTag(PIN_ERROR_TEST_TAG)
      .assertTextContains(context.getString(R.string.profile_login_activity_pin_error))
      .assertIsDisplayed()

    // Advance time for the error and PIN to be cleared.
    testCoroutineDispatchers.advanceTimeBy(3500)

    composeRule
      .onNodeWithTag(PIN_ERROR_TEST_TAG)
      .assertDoesNotExist()

    repeat(5) { index ->
      composeRule
        .onNodeWithTag(PIN_BOX_TEST_TAG + index, useUnmergedTree = true)
        .assertTextContains("")
    }
  }

//  1. Initial UI Rendering
//  PIN boxes are styled correctly (focused/unfocused state).
//@Test
//fun testFragment_onLaunch_fistPinInputBoxIsFocused() {
//  profileTestHelper.addMoreProfiles(1)
//  setUpTestApplicationComponent()
//  scenario = ActivityScenario.launch(ProfileLoginActivity::class.java)
//  testCoroutineDispatchers.runCurrent()
//
//  composeRule
//    .onNodeWithTag("pin_box_0", useUnmergedTree = true)
//    .assertIsFocused()
//
//  If enableMultipleClassrooms is true:
//
//  Navigates to ClassroomListActivity instead of HomeActivity.
//
//
//  🎭 5. State Restoration & Transitions
//  Rotation does not reset the profile name or input state (if applicable).
//
//  Rotation during error animation should not crash the UI.
//
//  LiveData is observed correctly and updates UI if the profile changes.
//
//  💥 6. Error Handling & Logging
//  If profile loading fails, app doesn't crash and default profile is used.
//
//  Ensure oppiaLogger logs the error on failure to fetch profile (mock verification).

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @Singleton
  @Component(
    modules = [
      RobolectricModule::class,
      TestPlatformParameterModule::class, PlatformParameterSingletonModule::class,
      TestDispatcherModule::class, ApplicationModule::class,
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
      ExplorationStorageModule::class, RetrofitModule::class, RetrofitServiceModule::class,
      NetworkConfigProdModule::class,
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

    fun inject(profileLoginFragmentTest: ProfileLoginFragmentTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerProfileLoginFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(profileLoginFragmentTest: ProfileLoginFragmentTest) {
      component.inject(profileLoginFragmentTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
