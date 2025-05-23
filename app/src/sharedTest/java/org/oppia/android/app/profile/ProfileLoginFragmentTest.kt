package org.oppia.android.app.profile

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
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
import org.oppia.android.app.classroom.ClassroomListActivity
import org.oppia.android.app.devoptions.DeveloperOptionsModule
import org.oppia.android.app.devoptions.DeveloperOptionsStarterModule
import org.oppia.android.app.home.HomeActivity
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.test.R
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
import org.oppia.android.app.utility.OrientationChangeAction.Companion.orientationLandscape
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
import org.oppia.android.testing.espresso.EditTextInputAction
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

  @get:Rule val oppiaTestRule = OppiaTestRule()
  @Inject lateinit var context: Context
  @Inject lateinit var profileTestHelper: ProfileTestHelper
  @Inject lateinit var profileManagementController: ProfileManagementController
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @get:Rule val composeRule = createEmptyComposeRule()
  @Inject lateinit var editTextInputAction: EditTextInputAction

  private lateinit var scenario: ActivityScenario<ProfileLoginActivity>
  private lateinit var appName: String

  @Before
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
    appName = context.getString(R.string.app_name)
  }

  @After
  fun tearDown() {
    TestPlatformParameterModule.reset()
    Intents.release()
  }

  @Test
  fun testFragment_onLaunch_allTextViewsHaveCorrectContent() {
    profileTestHelper.addOnlyAdminProfile()
    scenario = launch(ProfileLoginActivity::class.java)
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
  fun testFragment_onConfigChange_profileNameIsRetained() {
    profileTestHelper.addOnlyAdminProfile()
    scenario = launch(ProfileLoginActivity::class.java)
    onView(isRoot()).perform(orientationLandscape())
    testCoroutineDispatchers.runCurrent()

    composeRule.onNodeWithTag(GREETING_TEST_TAG)
      .assertTextContains(context.getString(R.string.profile_login_activity_greeting_text, "Admin"))
      .assertIsDisplayed()
  }

  @Test
  fun testFragment_onLaunch_adminProfile_fivePinInputBoxesAreDisplayed() {
    profileTestHelper.addOnlyAdminProfile()
    scenario = launch(ProfileLoginActivity::class.java)
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
    scenario = launch(ProfileLoginActivity::class.java)
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
    scenario = launch(ProfileLoginActivity::class.java)
    testCoroutineDispatchers.runCurrent()

    composeRule
      .onNodeWithTag(PIN_ERROR_TEST_TAG)
      .assertDoesNotExist()
  }

  @Test
  fun testFragment_nonAdmin_enterTwoDigits_doesNotTriggerLogin() {
    profileTestHelper.addMoreProfiles(1)
    scenario = launch(ProfileLoginActivity::class.java)
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
    TestPlatformParameterModule.forceEnableMultipleClassrooms(false)
    profileTestHelper.addMoreProfiles(1)
    scenario = launch(ProfileLoginActivity::class.java)
    testCoroutineDispatchers.runCurrent()

    composeRule
      .onNodeWithTag(PIN_INPUT_TEST_TAG, useUnmergedTree = true)
      .performClick()
      .performTextInput("123")

    testCoroutineDispatchers.runCurrent()

    intended(hasComponent(HomeActivity::class.java.name))
  }

  @Test
  fun testFragment_nonAdmin_classroomsEnabled_enterCorrectThreeDigits_opensClassroomsScreen() {
    TestPlatformParameterModule.forceEnableMultipleClassrooms(true)
    profileTestHelper.addMoreProfiles(1)
    scenario = launch(ProfileLoginActivity::class.java)
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
    scenario = launch(ProfileLoginActivity::class.java)
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
    scenario = launch(ProfileLoginActivity::class.java)
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
  fun testFragment_adminProfile_enterCorrectFiveDigits_triggersLoginAndOpensHomeScreen() {
    TestPlatformParameterModule.forceEnableMultipleClassrooms(false)
    profileTestHelper.addOnlyAdminProfile()
    scenario = launch(ProfileLoginActivity::class.java)
    testCoroutineDispatchers.runCurrent()

    composeRule
      .onNodeWithTag(PIN_INPUT_TEST_TAG, useUnmergedTree = true)
      .performClick()
      .performTextInput("12345")

    testCoroutineDispatchers.runCurrent()

    intended(hasComponent(HomeActivity::class.java.name))
  }

  @Test
  fun testFragment_adminProfile_classroomsEnabled_enterFiveDigits_opensClassroomScreen() {
    TestPlatformParameterModule.forceEnableMultipleClassrooms(true)
    profileTestHelper.addOnlyAdminProfile()
    scenario = launch(ProfileLoginActivity::class.java)
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
    scenario = launch(ProfileLoginActivity::class.java)
    testCoroutineDispatchers.runCurrent()

    composeRule
      .onNodeWithTag(PIN_INPUT_TEST_TAG, useUnmergedTree = true)
      .performClick()
      .performTextInput("22222")

    composeRule
      .onNodeWithTag(PIN_ERROR_TEST_TAG)
      .assertTextContains(context.getString(R.string.profile_login_activity_pin_error))
      .assertIsDisplayed()
  }

  @Test
  fun testFragment_adminUser_clickForgotPin_opensAdminForgotPinDialogFlow() {
    profileTestHelper.addOnlyAdminProfile()
    scenario = launch(ProfileLoginActivity::class.java)
    testCoroutineDispatchers.runCurrent()

    composeRule.onNodeWithTag(FORGOT_PIN_TEST_TAG).performClick()

    composeRule.onNodeWithTag(ADMIN_FORGOT_PIN_DIALOG_TEST_TAG)
      .assertExists()
      .assertIsDisplayed()

    composeRule
      .onNodeWithText(context.getString(R.string.profile_login_forgot_pin_dialog_title))
      .assertIsDisplayed()

    composeRule
      .onNodeWithText(
        context.getString(R.string.profile_login_forgot_pin_dialog_message, appName)
      )
      .assertIsDisplayed()

    composeRule
      .onNodeWithText(
        context.getString(R.string.profile_login_forgot_pin_dialog_cancel_button)
      )
      .assertIsDisplayed()

    composeRule
      .onNodeWithText(
        context.getString(R.string.profile_login_forgot_pin_dialog_reset_button, appName)
      )
      .assertIsDisplayed()
  }

  @Test
  fun testFragment_adminUser_openForgotPin_clickCancel_dismissesTheDialog() {
    profileTestHelper.addOnlyAdminProfile()
    scenario = launch(ProfileLoginActivity::class.java)
    testCoroutineDispatchers.runCurrent()

    composeRule.onNodeWithTag(FORGOT_PIN_TEST_TAG).performClick()

    composeRule.onNodeWithTag(ADMIN_FORGOT_PIN_DIALOG_TEST_TAG)
      .assertExists()
      .assertIsDisplayed()

    composeRule
      .onNodeWithText(
        context.getString(R.string.profile_login_forgot_pin_dialog_cancel_button)
      )
      .performClick()

    composeRule.onNodeWithTag(ADMIN_FORGOT_PIN_DIALOG_TEST_TAG)
      .assertDoesNotExist()
  }

  @Test
  fun testFragment_adminUser_openForgotPin_clickResetData_opensAdminResetPinDialogFlow() {
    profileTestHelper.addOnlyAdminProfile()
    scenario = launch(ProfileLoginActivity::class.java)
    testCoroutineDispatchers.runCurrent()

    composeRule.onNodeWithTag(FORGOT_PIN_TEST_TAG).performClick()

    composeRule
      .onNodeWithText(
        context.getString(R.string.profile_login_forgot_pin_dialog_reset_button, appName)
      )
      .assertIsDisplayed()
      .performClick()

    composeRule.onNodeWithTag(ADMIN_RESET_PIN_DIALOG_TEST_TAG)
      .assertExists()
      .assertIsDisplayed()

    composeRule
      .onNodeWithText(context.getString(R.string.admin_confirm_app_wipe_title, appName))
      .assertIsDisplayed()

    composeRule
      .onNodeWithText(
        context.getString(R.string.admin_confirm_app_wipe_message, appName)
      )
      .assertIsDisplayed()

    composeRule
      .onNodeWithText(
        context.getString(R.string.admin_confirm_app_wipe_negative_button_text)
      )
      .assertIsDisplayed()

    composeRule
      .onNodeWithText(
        context.getString(R.string.admin_confirm_app_wipe_positive_button_text)
      )
      .assertIsDisplayed()
  }

  @Test
  fun testFragment_adminUser_declineDataReset_dismissesTheDialog() {
    profileTestHelper.addOnlyAdminProfile()
    scenario = launch(ProfileLoginActivity::class.java)
    testCoroutineDispatchers.runCurrent()

    composeRule.onNodeWithTag(FORGOT_PIN_TEST_TAG).performClick()

    composeRule.onNodeWithTag(ADMIN_FORGOT_PIN_DIALOG_TEST_TAG)
      .assertExists()
      .assertIsDisplayed()

    composeRule
      .onNodeWithText(
        context.getString(R.string.profile_login_forgot_pin_dialog_reset_button, appName)
      )
      .performClick()

    composeRule.onNodeWithTag(ADMIN_RESET_PIN_DIALOG_TEST_TAG)
      .assertExists()
      .assertIsDisplayed()

    composeRule
      .onNodeWithText(
        context.getString(R.string.admin_confirm_app_wipe_negative_button_text)
      )
      .performClick()

    composeRule.onNodeWithTag(ADMIN_RESET_PIN_DIALOG_TEST_TAG)
      .assertDoesNotExist()
  }

  @Test
  fun testFragment_adminUser_confirmDataReset_closesTheApp() {
    profileTestHelper.addOnlyAdminProfile()
    scenario = launch(ProfileLoginActivity::class.java)
    testCoroutineDispatchers.runCurrent()

    composeRule.onNodeWithTag(FORGOT_PIN_TEST_TAG).performClick()

    composeRule.onNodeWithTag(ADMIN_FORGOT_PIN_DIALOG_TEST_TAG)
      .assertExists()
      .assertIsDisplayed()

    composeRule
      .onNodeWithText(
        context.getString(R.string.profile_login_forgot_pin_dialog_reset_button, appName)
      )
      .performClick()

    composeRule.onNodeWithTag(ADMIN_RESET_PIN_DIALOG_TEST_TAG)
      .assertExists()
      .assertIsDisplayed()

    composeRule
      .onNodeWithText(
        context.getString(R.string.admin_confirm_app_wipe_positive_button_text)
      )
      .performClick()

    testCoroutineDispatchers.runCurrent()

    assertThat(scenario.result.resultCode).isEqualTo(Activity.RESULT_CANCELED)
  }

  @Test
  fun testFragment_nonAdminUser_clickForgotPin_opensNonAdminForgotPinDialogFlow() {
    profileTestHelper.addMoreProfiles(1)
    scenario = launch(ProfileLoginActivity::class.java)
    testCoroutineDispatchers.runCurrent()

    composeRule.onNodeWithTag(FORGOT_PIN_TEST_TAG).performClick()

    composeRule.waitForIdle()

    onView(withText(context.getString(R.string.admin_settings_heading)))
      .inRoot(isDialog())
      .check(matches(isDisplayed()))
  }

  @Test
  fun testFragment_nonAdminUser_forgotPinDialog_clickCancel_dismissesTheDialog() {
    profileTestHelper.addMoreProfiles(1)
    scenario = launch(ProfileLoginActivity::class.java)
    testCoroutineDispatchers.runCurrent()

    composeRule.onNodeWithTag(FORGOT_PIN_TEST_TAG).performClick()

    composeRule.waitForIdle()

    onView(withText(context.getString(R.string.admin_settings_cancel)))
      .inRoot(isDialog())
      .check(matches(isDisplayed()))
      .perform(click())

    onView(withText(context.getString(R.string.admin_settings_heading)))
      .check(doesNotExist())
  }

  @Test
  fun testFragment_nonAdminUser_enterWrongAdminPin_showsWrongAdminPinError() {
    profileTestHelper.addMoreProfiles(1)
    scenario = launch(ProfileLoginActivity::class.java)
    testCoroutineDispatchers.runCurrent()

    composeRule.onNodeWithTag(FORGOT_PIN_TEST_TAG).performClick()

    composeRule.waitForIdle()

    onView(withId(R.id.admin_settings_input_pin_edit_text))
      .inRoot(isDialog())
      .check(matches(isDisplayed()))
      .perform(editTextInputAction.appendText("1111"), closeSoftKeyboard())

    onView(withText(context.getString(R.string.admin_settings_submit)))
      .inRoot(isDialog())
      .perform(click())

    onView(withText(context.getString(R.string.admin_settings_incorrect)))
      .inRoot(isDialog())
      .check(matches(isDisplayed()))
  }

  @Test
  fun testFragment_nonAdminUser_enterCorrectAdminPin_opensPinResetDialog() {
    profileTestHelper.initializeProfiles()
    val currentUserProfileId = ProfileId.newBuilder().setInternalId(1).build()
    scenario = launch(
      ProfileLoginActivity.createProfileLoginActivityIntent(context, currentUserProfileId)
    )
    testCoroutineDispatchers.runCurrent()

    composeRule.onNodeWithTag(FORGOT_PIN_TEST_TAG).performClick()

    composeRule.waitForIdle()

    onView(withId(R.id.admin_settings_input_pin_edit_text))
      .inRoot(isDialog())
      .check(matches(isDisplayed()))
      .perform(editTextInputAction.appendText("12345"), closeSoftKeyboard())

    onView(withText(context.getString(R.string.admin_settings_submit)))
      .inRoot(isDialog())
      .perform(click())

    onView(withId(R.id.reset_pin_input_pin))
      .inRoot(isDialog())
      .check(matches(isDisplayed()))
  }

  @Test
  fun testFragment_nonAdminUser_enterAndSubmitNewPin_opensSuccessDialog() {
    profileTestHelper.initializeProfiles()
    val currentUserProfileId = ProfileId.newBuilder().setInternalId(1).build()
    scenario = launch(
      ProfileLoginActivity.createProfileLoginActivityIntent(context, currentUserProfileId)
    )
    testCoroutineDispatchers.runCurrent()

    composeRule.onNodeWithTag(FORGOT_PIN_TEST_TAG).performClick()

    composeRule.waitForIdle()

    onView(withId(R.id.admin_settings_input_pin_edit_text))
      .inRoot(isDialog())
      .check(matches(isDisplayed()))
      .perform(editTextInputAction.appendText("12345"), closeSoftKeyboard())

    onView(withText(context.getString(R.string.admin_settings_submit)))
      .inRoot(isDialog())
      .perform(click())

    onView(withId(R.id.reset_pin_input_pin_edit_text))
      .inRoot(isDialog())
      .check(matches(isDisplayed()))
      .perform(editTextInputAction.appendText("111"), closeSoftKeyboard())

    onView(withText(context.getString(R.string.admin_settings_submit)))
      .inRoot(isDialog())
      .perform(click())

    testCoroutineDispatchers.runCurrent()

    onView(withText(context.getString(R.string.profile_login_reset_pin_success_dialog_message)))
      .inRoot(isDialog())
      .check(matches(isDisplayed()))
  }

  private fun setUpTestApplicationComponent() {
    TestPlatformParameterModule.forceEnableOnboardingFlowV2(true)
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
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
