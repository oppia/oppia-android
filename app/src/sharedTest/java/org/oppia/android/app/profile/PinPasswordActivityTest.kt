package org.oppia.android.app.profile

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.pressImeActionButton
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtraWithKey
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.hasFocus
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withInputType
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.textfield.TextInputEditText
import com.google.common.truth.Truth.assertThat
import dagger.Component
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Matchers.allOf
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
import org.oppia.android.app.model.ScreenName
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.test.R
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
import org.oppia.android.app.utility.EspressoTestsMatchers.withDrawable
import org.oppia.android.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.oppia.android.data.backends.gae.RetrofitModule
import org.oppia.android.data.backends.gae.RetrofitServiceModule
import org.oppia.android.data.backends.gae.testing.NetworkConfigTestModule
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
import org.oppia.android.domain.exploration.testing.ExplorationStorageTestModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionProdModule
import org.oppia.android.domain.onboarding.testing.ExpirationMetaDataRetrieverTestModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.oppialogger.analytics.CpuPerformanceSnapshotterModule
import org.oppia.android.domain.oppialogger.logscheduler.MetricLogSchedulerModule
import org.oppia.android.domain.oppialogger.loguploader.LogReportWorkerModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.ImageLoaderTestModule
import org.oppia.android.testing.LogReportingTestModule
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.espresso.EditTextInputAction
import org.oppia.android.testing.espresso.TextInputAction.Companion.hasErrorText
import org.oppia.android.testing.espresso.TextInputAction.Companion.hasNoErrorText
import org.oppia.android.testing.firebase.AuthenticationTestModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.logging.SyncStatusTestModule
import org.oppia.android.testing.platformparameter.PlatformParameterTestModule
import org.oppia.android.testing.profile.ProfileTestHelper
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.DispatcherTestModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.accessibility.FakeAccessibilityService
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.locale.testing.LocaleTestModule
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.extractCurrentAppScreenName
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.logging.performancemetrics.testing.PerformanceMetricsAssessorTestModule
import org.oppia.android.util.networking.NetworkConnectionDebugUtilModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.parser.html.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.image.ImageParsingModule
import org.oppia.android.util.profile.PROFILE_ID_INTENT_DECORATOR
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = PinPasswordActivityTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class PinPasswordActivityTest {
  @get:Rule val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()
  @get:Rule val oppiaTestRule = OppiaTestRule()

  @Inject lateinit var context: Context
  @Inject lateinit var profileTestHelper: ProfileTestHelper
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject lateinit var editTextInputAction: EditTextInputAction
  @Inject lateinit var fakeAccessibilityService: FakeAccessibilityService

  private val adminPin = "12345"
  private val adminId = 0
  private val userId = 1

  @Before
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
    profileTestHelper.initializeProfiles()
    testCoroutineDispatchers.registerIdlingResource()
  }

  @After
  fun tearDown() {
    testCoroutineDispatchers.unregisterIdlingResource()
    Intents.release()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  @Test
  fun testActivity_createIntent_verifyScreenNameInIntent() {
    val currentScreenName = PinPasswordActivity.createPinPasswordActivityIntent(
      context = context,
      adminPin = adminPin,
      profileId = adminId
    ).extractCurrentAppScreenName()

    assertThat(currentScreenName).isEqualTo(ScreenName.PIN_PASSWORD_ACTIVITY)
  }

  @Test
  fun testPinPassword_withAdmin_screenReaderOff_keyboardIsVisible() {
    fakeAccessibilityService.setScreenReaderEnabled(false)
    launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = adminId
      )
    ).use {
      onView(withId(R.id.pin_password_input_pin_edit_text)).check(matches(hasFocus()))
    }
  }

  @Test
  fun testPinPassword_withAdmin_screenReaderOn_keyboardIsNotVisible() {
    fakeAccessibilityService.setScreenReaderEnabled(true)
    launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = adminId
      )
    ).use {
      onView(withId(R.id.pin_password_input_pin_edit_text)).check(matches(not(hasFocus())))
    }
  }

  @Test
  fun testPinPassword_withAdmin_inputCorrectPin_opensHomeActivity() {
    PlatformParameterTestModule.forceEnableMultipleClassrooms(false)
    launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = adminId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.pin_password_input_pin_edit_text))
        .perform(editTextInputAction.appendText("12345"))
      testCoroutineDispatchers.runCurrent()
      intended(hasComponent(HomeActivity::class.java.name))
    }
  }

  @Test
  fun testPinPassword_enableClassrooms_withAdmin_inputCorrectPin_opensClassroomListActivity() {
    PlatformParameterTestModule.forceEnableMultipleClassrooms(true)
    launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = adminId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.pin_password_input_pin_edit_text))
        .perform(editTextInputAction.appendText("12345"))
      testCoroutineDispatchers.runCurrent()
      intended(hasComponent(ClassroomListActivity::class.java.name))
      hasExtraWithKey(PROFILE_ID_INTENT_DECORATOR)
    }
  }

  @Test
  fun testPinPassword_withUser_inputCorrectPin_opensHomeActivity() {
    PlatformParameterTestModule.forceEnableMultipleClassrooms(false)
    launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = userId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.pin_password_input_pin_edit_text))
        .perform(editTextInputAction.appendText("123"))
      testCoroutineDispatchers.runCurrent()
      intended(hasComponent(HomeActivity::class.java.name))
    }
  }

  @Test
  fun testPinPassword_enableClassrooms_withUser_inputCorrectPin_opensClassroomListActivity() {
    PlatformParameterTestModule.forceEnableMultipleClassrooms(true)
    launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = userId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.pin_password_input_pin_edit_text))
        .perform(editTextInputAction.appendText("123"))
      testCoroutineDispatchers.runCurrent()
      intended(hasComponent(ClassroomListActivity::class.java.name))
      hasExtraWithKey(PROFILE_ID_INTENT_DECORATOR)
    }
  }

  @Test
  fun testPinPassword_withAdmin_inputWrongPin_incorrectPinShows() {
    launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = adminId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      closeSoftKeyboard()
      onView(withId(R.id.pin_password_input_pin_edit_text)).perform(closeSoftKeyboard())
        .perform(editTextInputAction.appendText("54321"), closeSoftKeyboard())
      onView(withId(R.id.pin_password_input_pin)).check(
        matches(
          hasErrorText(context.resources.getString(R.string.pin_password_incorrect_pin))
        )
      )
    }
  }

  @Test
  fun testPinPasswordActivity_hasCorrectActivityLabel() {
    launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = adminId,
      )
    ).use { scenario ->
      scenario.onActivity { activity ->
        val title = activity.title

        // Verify that the activity label is correct as a proxy to verify TalkBack will announce the
        // correct string when it's read out.
        assertThat(title).isEqualTo(context.getString(R.string.pin_password_activity_title))
      }
    }
  }

  @Test
  fun testPinPassword_withUser_inputWrongPin_incorrectPinShows() {
    launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = userId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.pin_password_input_pin_edit_text)).perform(
        editTextInputAction.appendText("321"), closeSoftKeyboard()
      )
      onView(withId(R.id.pin_password_input_pin)).check(
        matches(
          hasErrorText(context.resources.getString(R.string.pin_password_incorrect_pin))
        )
      )
    }
  }

  @Test
  fun testPinPassword_withUser_inputCorrectPin_doesNotShowIncorrectPin() {
    launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = userId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.pin_password_input_pin_edit_text)).perform(
        editTextInputAction.appendText("123"), closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.pin_password_input_pin)).check(
        matches(
          not(hasErrorText(context.resources.getString(R.string.pin_password_incorrect_pin)))
        )
      )
    }
  }

  @Test
  fun testPinPassword_withAdmin_forgot_opensAdminForgotDialog() {
    launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = adminId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.pin_password_input_pin_edit_text)).perform(
        editTextInputAction.appendText(""),
        closeSoftKeyboard()
      )
      onView(withId(R.id.forgot_pin)).perform(click())
      onView(withText(getPinPasswordForgotMessage()))
        .inRoot(isDialog())
        .check(matches(isDisplayed()))
    }
  }

  @Test
  fun testPinPassword_withUser_forgot_inputWrongAdminPin_wrongAdminPinError() {
    launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = userId
      )
    ).use {
      onView(withId(R.id.forgot_pin)).perform(click())
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.admin_settings_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_settings_input_pin))
        )
      ).inRoot(isDialog())
        .perform(editTextInputAction.appendText("1234"), closeSoftKeyboard())

      onView(withText(context.getString(R.string.admin_settings_submit)))
        .inRoot(isDialog())
        .perform(click())
      onView(withId(R.id.admin_settings_input_pin))
        .check(
          matches(
            hasErrorText(
              context.resources.getString(R.string.admin_settings_incorrect)
            )
          )
        )
      onView(
        allOf(
          withId(R.id.admin_settings_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_settings_input_pin))
        )
      ).perform(
        editTextInputAction.appendText("5"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.admin_settings_input_pin))
        .check(matches(hasNoErrorText()))
    }
  }

  @Test
  fun testPinPassword_withUser_forgot_inputAdminPinAndShortPin_pinLengthError() {
    launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = userId
      )
    ).use {
      onView(withId(R.id.pin_password_input_pin_edit_text))
        .perform(editTextInputAction.appendText(""), closeSoftKeyboard())
      onView(withId(R.id.forgot_pin)).perform(click())
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.admin_settings_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_settings_input_pin))
        )
      ).inRoot(isDialog())
        .perform(editTextInputAction.appendText("12345"), closeSoftKeyboard())

      onView(withText(context.getString(R.string.admin_settings_submit)))
        .inRoot(isDialog())
        .perform(click())

      onView(
        allOf(
          withId(R.id.reset_pin_input_pin_edit_text),
          isDescendantOfA(withId(R.id.reset_pin_input_pin))
        )
      ).inRoot(isDialog())
        .perform(editTextInputAction.appendText("32"), closeSoftKeyboard())

      onView(withText(context.getString(R.string.admin_settings_submit)))
        .inRoot(isDialog())
        .perform(click())
      onView(withId(R.id.reset_pin_input_pin))
        .check(
          matches(
            hasErrorText(
              context.resources.getString(R.string.add_profile_error_pin_length)
            )
          )
        )
      onView(
        allOf(
          withId(R.id.reset_pin_input_pin_edit_text),
          isDescendantOfA(withId(R.id.reset_pin_input_pin))
        )
      ).perform(
        editTextInputAction.appendText("1"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.reset_pin_input_pin))
        .check(matches(hasNoErrorText()))
    }
  }

  @Test
  fun testPinPassword_withUser_forgot_inputAdminPinAndNewPinAndOldPin_wrongPinError() {
    launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = userId
      )
    ).use {
      onView(withId(R.id.pin_password_input_pin_edit_text))
        .perform(editTextInputAction.appendText(""), closeSoftKeyboard())
      onView(withId(R.id.forgot_pin)).perform(click())
      onView(
        allOf(
          withId(R.id.admin_settings_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_settings_input_pin))
        )
      ).inRoot(isDialog())
        .perform(editTextInputAction.appendText("12345"), closeSoftKeyboard())

      onView(withText(context.getString(R.string.admin_settings_submit)))
        .inRoot(isDialog())
        .perform(click())
      onView(
        allOf(
          withId(R.id.reset_pin_input_pin_edit_text),
          isDescendantOfA(withId(R.id.reset_pin_input_pin))
        )
      ).inRoot(isDialog())
        .perform(editTextInputAction.appendText("321"), closeSoftKeyboard())

      onView(withText(context.getString(R.string.admin_settings_submit)))
        .inRoot(isDialog())
        .perform(click())

      testCoroutineDispatchers.runCurrent()
      onView(withText(context.getString(R.string.pin_password_close)))
        .inRoot(isDialog())
        .perform(click())
      onView(withId(R.id.pin_password_input_pin_edit_text)).perform(
        editTextInputAction.appendText("123"),
        closeSoftKeyboard()
      )
      onView(withText(context.getString(R.string.pin_password_incorrect_pin)))
        .check(matches(isDisplayed()))
    }
  }

  @Test
  fun testPinPassword_withUser_forgot_inputAdminPinAndNewPin_opensHomeActivity() {
    PlatformParameterTestModule.forceEnableMultipleClassrooms(false)
    launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = userId
      )
    ).use {
      onView(withId(R.id.pin_password_input_pin_edit_text))
        .perform(editTextInputAction.appendText(""), closeSoftKeyboard())
      onView(withId(R.id.forgot_pin)).perform(click())
      onView(
        allOf(
          withId(R.id.admin_settings_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_settings_input_pin))
        )
      ).inRoot(isDialog())
        .perform(editTextInputAction.appendText("12345"), closeSoftKeyboard())

      onView(withText(context.getString(R.string.admin_settings_submit)))
        .inRoot(isDialog())
        .perform(click())
      onView(
        allOf(
          withId(R.id.reset_pin_input_pin_edit_text),
          isDescendantOfA(withId(R.id.reset_pin_input_pin))
        )
      )
        .inRoot(isDialog())
        .perform(editTextInputAction.appendText("321"), closeSoftKeyboard())
      onView(withText(context.getString(R.string.admin_settings_submit)))
        .inRoot(isDialog())
        .perform(click())

      testCoroutineDispatchers.runCurrent()
      onView(withText(context.getString(R.string.pin_password_close)))
        .inRoot(isDialog())
        .perform(click())
      onView(withId(R.id.pin_password_input_pin_edit_text))
        .perform(editTextInputAction.appendText("321"))
      testCoroutineDispatchers.runCurrent()
      intended(hasComponent(HomeActivity::class.java.name))
    }
  }

  @Test
  fun testPinPassword_withUser_forgot_inputAdminPin_configChange_inputPinIsPresent() {
    launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = userId
      )
    ).use {
      onView(withId(R.id.pin_password_input_pin_edit_text))
        .perform(editTextInputAction.appendText(""), closeSoftKeyboard())
      onView(withId(R.id.forgot_pin)).perform(click())
      onView(
        allOf(
          withId(R.id.admin_settings_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_settings_input_pin))
        )
      ).inRoot(isDialog())
        .perform(editTextInputAction.appendText("1234"), closeSoftKeyboard())
      onView(isRoot()).perform(orientationLandscape())
      onView(
        allOf(
          withId(R.id.admin_settings_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_settings_input_pin))
        )
      ).inRoot(isDialog())
        .check(matches(withText("1234")))
    }
  }

  @Test
  fun testPinPassword_withUser_forgot_inputAdminPin_submit_configChange_resetPinDisplayed() {
    launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = userId
      )
    ).use {
      onView(withId(R.id.pin_password_input_pin_edit_text))
        .perform(editTextInputAction.appendText(""), closeSoftKeyboard())
      onView(withId(R.id.forgot_pin)).perform(click())
      onView(
        allOf(
          withId(R.id.admin_settings_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_settings_input_pin))
        )
      ).inRoot(isDialog())
        .perform(editTextInputAction.appendText("12345"), closeSoftKeyboard())
      onView(withText(context.getString(R.string.admin_settings_submit)))
        .inRoot(isDialog())
        .perform(click())
      onView(isRoot()).perform(orientationLandscape())
      onView(withText(context.getString(R.string.reset_pin_enter)))
        .inRoot(isDialog())
        .check(matches(isDisplayed()))
    }
  }

  @Test
  fun testPinPassword_withUser_forgot_inputAdminPin_submit_inputNewPin_pinChanged() {
    launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = userId
      )
    ).use {
      onView(withId(R.id.pin_password_input_pin_edit_text))
        .perform(editTextInputAction.appendText(""), closeSoftKeyboard())
      onView(withId(R.id.forgot_pin)).perform(click())
      onView(
        allOf(
          withId(R.id.admin_settings_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_settings_input_pin))
        )
      ).inRoot(isDialog())
        .perform(editTextInputAction.appendText("12345"), closeSoftKeyboard())

      onView(withText(context.getString(R.string.admin_settings_submit)))
        .inRoot(isDialog())
        .perform(click())
      onView(
        allOf(
          withId(R.id.reset_pin_input_pin_edit_text),
          isDescendantOfA(withId(R.id.reset_pin_input_pin))
        )
      )
        .inRoot(isDialog())
        .perform(editTextInputAction.appendText("123"), closeSoftKeyboard())
      onView(isRoot()).perform(orientationLandscape())
      onView(withText(context.getString(R.string.admin_settings_submit)))
        .inRoot(isDialog())
        .perform(click())
      testCoroutineDispatchers.runCurrent()
      onView(
        withText(context.getString(R.string.pin_password_success))
      ).inRoot(isDialog()).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testPinPassword_withAdmin_forgot_configChange_opensAdminForgotDialog() {
    launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = adminId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      closeSoftKeyboard()
      onView(withId(R.id.forgot_pin)).perform(click())
      onView(isRoot()).perform(orientationLandscape())
      onView(withText(getPinPasswordForgotMessage()))
        .inRoot(isDialog())
        .check(matches(isDisplayed()))
    }
  }

  @Test
  fun testPinPassword_withUser_forgot_inputWrongAdminPin_configChange_wrongAdminPinError() {
    launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = userId
      )
    ).use {
      onView(withId(R.id.pin_password_input_pin_edit_text))
        .perform(editTextInputAction.appendText(""), closeSoftKeyboard())
      onView(withId(R.id.forgot_pin)).perform(click())
      onView(
        allOf(
          withId(R.id.admin_settings_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_settings_input_pin))
        )
      ).inRoot(isDialog())
        .perform(editTextInputAction.appendText("1234"), closeSoftKeyboard())
      onView(withText(context.getString(R.string.admin_settings_submit)))
        .inRoot(isDialog())
        .perform(click())
      onView(withId(R.id.admin_settings_input_pin))
        .check(
          matches(
            hasErrorText(
              context.resources.getString(R.string.admin_settings_incorrect)
            )
          )
        )
      onView(
        allOf(
          withId(R.id.admin_settings_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_settings_input_pin))
        )
      ).inRoot(isDialog())
        .perform(editTextInputAction.appendText("5"), closeSoftKeyboard())
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.admin_settings_input_pin))
        .check(matches(hasNoErrorText()))
    }
  }

  @Test
  fun testPinPassword_withUser_forgot_inputAdminPinAndIncorrectPin_errorIsDisplayed() {
    launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = userId
      )
    ).use {
      onView(withId(R.id.pin_password_input_pin_edit_text))
        .perform(editTextInputAction.appendText(""), closeSoftKeyboard())
      onView(withId(R.id.forgot_pin)).perform(click())
      onView(
        allOf(
          withId(R.id.admin_settings_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_settings_input_pin))
        )
      ).inRoot(isDialog())
        .perform(editTextInputAction.appendText("1234"), closeSoftKeyboard())
      onView(withText(context.getString(R.string.admin_settings_submit)))
        .inRoot(isDialog())
        .perform(click())
      onView(withId(R.id.admin_settings_input_pin))
        .check(
          matches(
            hasErrorText(
              context.resources.getString(R.string.admin_settings_incorrect)
            )
          )
        )
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.admin_settings_input_pin))
        .check(
          matches(
            hasErrorText(
              context.resources.getString(R.string.admin_settings_incorrect)
            )
          )
        )
    }
  }

  @Test
  fun testPinPassword_withUser_forgot_inputAdminPinAndNullPin_errorIsDisplayed() {
    launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = userId
      )
    ).use {
      onView(withId(R.id.pin_password_input_pin_edit_text))
        .perform(editTextInputAction.appendText(""), closeSoftKeyboard())
      onView(withId(R.id.forgot_pin)).perform(click())
      onView(
        allOf(
          withId(R.id.admin_settings_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_settings_input_pin))
        )
      ).inRoot(isDialog())
        .perform(editTextInputAction.appendText(""), closeSoftKeyboard())
      onView(withText(context.getString(R.string.admin_settings_submit)))
        .inRoot(isDialog())
        .perform(click())
      onView(withId(R.id.admin_settings_input_pin))
        .check(
          matches(
            hasErrorText(
              context.resources.getString(R.string.admin_auth_null)
            )
          )
        )
    }
  }

  @Test
  fun testPinPassword_withUser_forgot_inputAdminPinAndNullPin_configChange_errorIsDisplayed() {
    launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = userId
      )
    ).use {
      onView(withId(R.id.pin_password_input_pin_edit_text))
        .perform(editTextInputAction.appendText(""), closeSoftKeyboard())
      onView(withId(R.id.forgot_pin)).perform(click())
      onView(
        allOf(
          withId(R.id.admin_settings_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_settings_input_pin))
        )
      ).inRoot(isDialog())
        .perform(editTextInputAction.appendText(""), closeSoftKeyboard())
      onView(withText(context.getString(R.string.admin_settings_submit)))
        .inRoot(isDialog())
        .perform(click())
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.admin_settings_input_pin))
        .check(
          matches(
            hasErrorText(
              context.resources.getString(R.string.admin_auth_null)
            )
          )
        )
    }
  }

  // TODO(#4209): Error -> Expected error text doesn't match the selected view
  @Test
  fun testPinPassword_withUser_forgot_inputAdminPinAndNullPin_imeAction_errorIsDisplayed() {
    launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = userId
      )
    ).use {
      onView(withId(R.id.pin_password_input_pin_edit_text))
        .perform(editTextInputAction.appendText(""), closeSoftKeyboard())
      onView(withId(R.id.forgot_pin)).perform(click())
      onView(
        allOf(
          withId(R.id.admin_settings_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_settings_input_pin))
        )
      ).inRoot(isDialog())
        .perform(editTextInputAction.appendText(""), pressImeActionButton())
      onView(withId(R.id.admin_settings_input_pin))
        .check(
          matches(
            hasErrorText(
              context.resources.getString(R.string.admin_auth_null)
            )
          )
        )
    }
  }

  @Test
  fun testPinPassword_user_forgot_adminPinAndNullPin_configChange_imeAction_errorIsDisplayed() {
    launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = userId
      )
    ).use {
      onView(withId(R.id.pin_password_input_pin_edit_text))
        .perform(editTextInputAction.appendText(""), closeSoftKeyboard())
      onView(withId(R.id.forgot_pin)).perform(click())
      onView(
        allOf(
          withId(R.id.admin_settings_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_settings_input_pin))
        )
      ).inRoot(isDialog())
        .perform(editTextInputAction.appendText(""), pressImeActionButton())
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.admin_settings_input_pin))
        .check(
          matches(
            hasErrorText(
              context.resources.getString(R.string.admin_auth_null)
            )
          )
        )
    }
  }

  @Test
  fun testPinPassword_withUser_forgot_inputNullAdminPin_configChange_wrongAdminPinError() {
    launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = userId
      )
    ).use {
      onView(withId(R.id.pin_password_input_pin_edit_text))
        .perform(editTextInputAction.appendText(""), closeSoftKeyboard())
      onView(withId(R.id.forgot_pin)).perform(click())
      onView(
        allOf(
          withId(R.id.admin_settings_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_settings_input_pin))
        )
      ).inRoot(isDialog())
        .perform(editTextInputAction.appendText(""), closeSoftKeyboard())
      onView(withText(context.getString(R.string.admin_settings_submit)))
        .inRoot(isDialog())
        .perform(click())
      onView(withId(R.id.admin_settings_input_pin))
        .check(
          matches(
            hasErrorText(
              context.resources.getString(R.string.admin_auth_null)
            )
          )
        )
      onView(
        allOf(
          withId(R.id.admin_settings_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_settings_input_pin))
        )
      ).inRoot(isDialog())
        .perform(editTextInputAction.appendText("1"), closeSoftKeyboard())
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.admin_settings_input_pin))
        .check(matches(hasNoErrorText()))
    }
  }

  @Test
  fun testPinPassword_withUser_forgot_inputAdminPinAndInvalidPin_errorIsDisplayed() {
    launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = userId
      )
    ).use {
      onView(withId(R.id.pin_password_input_pin_edit_text))
        .perform(editTextInputAction.appendText(""), closeSoftKeyboard())
      onView(withId(R.id.forgot_pin)).perform(click())
      onView(
        allOf(
          withId(R.id.admin_settings_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_settings_input_pin))
        )
      ).inRoot(isDialog())
        .perform(editTextInputAction.appendText("12345"), closeSoftKeyboard())

      onView(withText(context.getString(R.string.admin_settings_submit)))
        .inRoot(isDialog())
        .perform(click())
      onView(
        allOf(
          withId(R.id.reset_pin_input_pin_edit_text),
          isDescendantOfA(withId(R.id.reset_pin_input_pin))
        )
      ).inRoot(isDialog())
        .perform(editTextInputAction.appendText("11"), closeSoftKeyboard())
      onView(withText(context.getString(R.string.admin_settings_submit)))
        .inRoot(isDialog())
        .perform(click())
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.reset_pin_input_pin))
        .check(
          matches(
            hasErrorText(
              context.resources.getString(R.string.add_profile_error_pin_length)
            )
          )
        )
    }
  }

  @Test
  fun testPinPassword_withAdmin_inputWrongPin_configChange_incorrectPinIsDisplayed() {
    launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = adminId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      closeSoftKeyboard()
      onView(withId(R.id.pin_password_input_pin_edit_text)).perform(
        editTextInputAction.appendText("54321"),
        closeSoftKeyboard()
      )
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.pin_password_input_pin)).check(
        matches(
          hasErrorText(context.resources.getString(R.string.pin_password_incorrect_pin))
        )
      )
    }
  }

  @Test
  fun testPinPassword_withAdmin_checkShowHidePassword_defaultText() {
    launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = adminId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      closeSoftKeyboard()
      onView(withText(context.getString(R.string.pin_password_show))).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testPinPassword_withAdmin_checkShowHidePassword_defaultImage() {
    launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = adminId
      )
    ).use {
      onView(withId(R.id.show_hide_password_image_view))
        .check(
          matches(
            withDrawable(
              R.drawable.ic_hide_eye_icon
            )
          )
        )
    }
  }

  @Test
  fun testPinPassword_withAdmin_showHideIcon_hasPasswordHiddenContentDescription() {
    launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = adminId
      )
    ).use {
      onView(withId(R.id.show_hide_password_image_view))
        .check(
          matches(
            withContentDescription(
              R.string.password_hidden_icon
            )
          )
        )
    }
  }

  @Test
  fun testPinPassword_withAdmin_showHidePassword_textChangesToHide() {
    launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = adminId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.show_pin)).perform(click())
      onView(withText(context.getString(R.string.pin_password_hide))).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testPinPassword_withAdmin_clickShowHideIcon_hasPasswordShownContentDescription() {
    launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = adminId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      closeSoftKeyboard()
      onView(withId(R.id.show_pin)).perform(click())
      onView(withId(R.id.show_hide_password_image_view))
        .check(
          matches(
            withContentDescription(
              R.string.password_shown_icon
            )
          )
        )
    }
  }

  @Test
  fun testPinPassword_withAdmin_showHidePassword_imageChangesToShow() {
    launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = adminId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      closeSoftKeyboard()
      onView(withId(R.id.show_pin)).perform(click())
      onView(withId(R.id.show_hide_password_image_view))
        .check(
          matches(
            withDrawable(
              R.drawable.ic_show_eye_icon
            )
          )
        )
    }
  }

  @Test
  fun testPinPassword_withAdmin_showHidePassword_configChange_showViewIsShown() {
    launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = adminId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      closeSoftKeyboard()
      onView(withId(R.id.show_pin)).perform(click())
      onView(isRoot()).perform(orientationLandscape())
      onView(withText(context.getString(R.string.pin_password_hide))).check(matches(isDisplayed()))
      onView(withId(R.id.show_hide_password_image_view))
        .check(
          matches(
            withDrawable(
              R.drawable.ic_show_eye_icon
            )
          )
        )
    }
  }

  @Test
  fun testPinPassword_checkInputType_showHidePassword_inputTypeIsSame() {
    launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = adminId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()

      var inputType: Int = 0
      it.onActivity {
        inputType =
          it.findViewById<TextInputEditText>(R.id.pin_password_input_pin_edit_text).inputType
      }
      onView(withId(R.id.pin_password_input_pin_edit_text))
        .check(matches(withInputType(inputType)))
      onView(withId(R.id.show_pin)).perform(click())
      onView(withId(R.id.pin_password_input_pin_edit_text))
        .check(matches(withInputType(inputType)))
    }
  }

  @Test
  fun testPinPassword_clickForgotPin_enterAdminPin_clickSubmit_dialogMessageIsCorrect() {
    launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = userId
      )
    ).use {
      onView(withId(R.id.forgot_pin)).perform(click())
      onView(
        allOf(
          withId(R.id.admin_settings_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_settings_input_pin))
        )
      ).inRoot(isDialog())
        .perform(editTextInputAction.appendText("12345"), closeSoftKeyboard())
      onView(withText(context.getString(R.string.admin_settings_submit)))
        .inRoot(isDialog())
        .perform(click())
      testCoroutineDispatchers.runCurrent()
      onView(
        withText(
          containsString(
            context.resources.getString(R.string.reset_pin_enter_dialog_message, "Ben")
          )
        )
      ).inRoot(isDialog()).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testPinPassword_adminUser_inputFiveDigitPin_configChange_inputIsPersisted() {
    launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = adminId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.pin_password_input_pin_edit_text))
        .perform(editTextInputAction.appendText("12345"), closeSoftKeyboard())
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.pin_password_input_pin_edit_text))
        .check(matches(withText("12345")))
    }
  }

  @Test
  fun testPinPassword_nonAdminUser_inputThreeDigitPin_configChange_inputIsPersisted() {
    launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = userId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.pin_password_input_pin_edit_text))
        .perform(editTextInputAction.appendText("123"), closeSoftKeyboard())
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.pin_password_input_pin_edit_text))
        .check(matches(withText("123")))
    }
  }

  @Test
  fun testPinPassword_adminUser_inputPinExceedsFive_textIsTrimmedToFive() {
    launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = adminId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.pin_password_input_pin_edit_text))
        .perform(editTextInputAction.appendText("1234567"), closeSoftKeyboard())
      onView(withId(R.id.pin_password_input_pin_edit_text))
        .check(matches(withText("12345")))
    }
  }

  @Test
  fun testPinPassword_nonAdminUser_inputPinExceedsThree_textIsTrimmedToThree() {
    launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = userId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.pin_password_input_pin_edit_text))
        .perform(editTextInputAction.appendText("1234567"), closeSoftKeyboard())
      onView(withId(R.id.pin_password_input_pin_edit_text))
        .check(matches(withText("123")))
    }
  }

  @Test
  fun testActivity_multipleClassroomsDisabled_adminUser_inputPin_changeConfig_opensHomeActivity() {
    PlatformParameterTestModule.forceEnableMultipleClassrooms(false)
    launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = adminId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.pin_password_input_pin_edit_text))
        .perform(editTextInputAction.appendText("123"), closeSoftKeyboard())
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.pin_password_input_pin_edit_text))
        .perform(editTextInputAction.appendText("45"), closeSoftKeyboard())
      testCoroutineDispatchers.runCurrent()
      intended(hasComponent(HomeActivity::class.java.name))
    }
  }

  @Test
  fun testActivity_enablesClassroom_adminUser_inputPin_changeConfig_opensClassroomListActivity() {
    PlatformParameterTestModule.forceEnableMultipleClassrooms(true)
    launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = adminId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.pin_password_input_pin_edit_text))
        .perform(editTextInputAction.appendText("123"), closeSoftKeyboard())
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.pin_password_input_pin_edit_text))
        .perform(editTextInputAction.appendText("45"), closeSoftKeyboard())
      testCoroutineDispatchers.runCurrent()
      intended(hasComponent(ClassroomListActivity::class.java.name))
    }
  }

  @Test
  fun testActivity_disableMultipleClassroom_nonAdminUser_inputPin_changeConfig_opensHomeActivity() {
    PlatformParameterTestModule.forceEnableMultipleClassrooms(false)
    launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = userId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.pin_password_input_pin_edit_text))
        .perform(editTextInputAction.appendText("12"), closeSoftKeyboard())
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.pin_password_input_pin_edit_text))
        .perform(editTextInputAction.appendText("3"), closeSoftKeyboard())
      testCoroutineDispatchers.runCurrent()
      intended(hasComponent(HomeActivity::class.java.name))
    }
  }

  @Test
  fun testActivity_enableClassroom_nonAdminUser_inputPin_changeConfig_opensClassroomListActivity() {
    PlatformParameterTestModule.forceEnableMultipleClassrooms(true)
    launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context = context,
        adminPin = adminPin,
        profileId = userId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.pin_password_input_pin_edit_text))
        .perform(editTextInputAction.appendText("12"), closeSoftKeyboard())
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.pin_password_input_pin_edit_text))
        .perform(editTextInputAction.appendText("3"), closeSoftKeyboard())
      testCoroutineDispatchers.runCurrent()
      intended(hasComponent(ClassroomListActivity::class.java.name))
    }
  }

  private fun getAppName(): String = context.resources.getString(R.string.app_name)

  private fun getPinPasswordForgotMessage(): String =
    context.resources.getString(R.string.admin_forgot_pin_message, getAppName())

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
      AuthenticationTestModule::class,
      CachingTestModule::class,
      ContinueModule::class,
      CpuPerformanceSnapshotterModule::class,
      DeveloperOptionsModule::class,
      DeveloperOptionsStarterModule::class,
      DispatcherTestModule::class,
      DragDropSortInputModule::class,
      ExpirationMetaDataRetrieverTestModule::class,
      ExplorationProgressModule::class,
      ExplorationStorageTestModule::class,
      FakeOppiaClockModule::class,
      FirebaseLogUploaderModule::class,
      FractionInputModule::class,
      GcsResourceModule::class,
      HintsAndSolutionConfigModule::class,
      HintsAndSolutionProdModule::class,
      HtmlParserEntityTypeModule::class,
      ImageClickInputModule::class,
      ImageLoaderTestModule::class,
      ImageParsingModule::class,
      InteractionsModule::class,
      ItemSelectionInputModule::class,
      LocaleTestModule::class,
      LogReportWorkerModule::class,
      LogReportingTestModule::class,
      LogStorageModule::class,
      LoggerModule::class,
      LoggingIdentifierModule::class,
      MathEquationInputModule::class,
      MetricLogSchedulerModule::class,
      MultipleChoiceInputModule::class,
      NetworkConfigTestModule::class,
      NetworkConnectionDebugUtilModule::class,
      NetworkConnectionUtilDebugModule::class,
      NumberWithUnitsRuleModule::class,
      NumericExpressionInputModule::class,
      NumericInputRuleModule::class,
      PerformanceMetricsAssessorTestModule::class,
      PlatformParameterSingletonModule::class,
      PlatformParameterTestModule::class,
      QuestionModule::class,
      RatioInputModule::class,
      RetrofitModule::class,
      RetrofitServiceModule::class,
      RobolectricModule::class,
      SplitScreenInteractionModule::class,
      SyncStatusTestModule::class,
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

    fun inject(pinPasswordActivityTest: PinPasswordActivityTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerPinPasswordActivityTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(pinPasswordActivityTest: PinPasswordActivityTest) {
      component.inject(pinPasswordActivityTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
