package org.oppia.app.profile

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.os.Build
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.hasFocus
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import com.google.firebase.FirebaseApp
import dagger.Component
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.activity.ActivityComponent
import org.oppia.app.application.ActivityComponentFactory
import org.oppia.app.application.ApplicationComponent
import org.oppia.app.application.ApplicationInjector
import org.oppia.app.application.ApplicationInjectorProvider
import org.oppia.app.application.ApplicationModule
import org.oppia.app.application.ApplicationStartupListenerModule
import org.oppia.app.home.HomeActivity
import org.oppia.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.app.shim.ViewBindingShimModule
import org.oppia.app.utility.EspressoTestsMatchers.withDrawable
import org.oppia.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.oppia.domain.classify.InteractionsModule
import org.oppia.domain.classify.rules.continueinteraction.ContinueModule
import org.oppia.domain.classify.rules.dragAndDropSortInput.DragDropSortInputModule
import org.oppia.domain.classify.rules.fractioninput.FractionInputModule
import org.oppia.domain.classify.rules.imageClickInput.ImageClickInputModule
import org.oppia.domain.classify.rules.itemselectioninput.ItemSelectionInputModule
import org.oppia.domain.classify.rules.multiplechoiceinput.MultipleChoiceInputModule
import org.oppia.domain.classify.rules.numberwithunits.NumberWithUnitsRuleModule
import org.oppia.domain.classify.rules.numericinput.NumericInputRuleModule
import org.oppia.domain.classify.rules.ratioinput.RatioInputModule
import org.oppia.domain.classify.rules.textinput.TextInputRuleModule
import org.oppia.domain.onboarding.ExpirationMetaDataRetrieverModule
import org.oppia.domain.oppialogger.LogStorageModule
import org.oppia.domain.oppialogger.loguploader.LogUploadWorkerModule
import org.oppia.domain.oppialogger.loguploader.WorkManagerConfigurationModule
import org.oppia.domain.question.QuestionModule
import org.oppia.domain.topic.PrimeTopicAssetsControllerModule
import org.oppia.testing.TestAccessibilityModule
import org.oppia.testing.TestCoroutineDispatchers
import org.oppia.testing.TestDispatcherModule
import org.oppia.testing.TestLogReportingModule
import org.oppia.testing.profile.ProfileTestHelper
import org.oppia.util.caching.testing.CachingTestModule
import org.oppia.util.gcsresource.GcsResourceModule
import org.oppia.util.logging.LoggerModule
import org.oppia.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.util.parser.GlideImageLoaderModule
import org.oppia.util.parser.HtmlParserEntityTypeModule
import org.oppia.util.parser.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

private const val TIMEOUT = 1000L
private const val CONDITION_CHECK_INTERVAL = 100L

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = PinPasswordActivityTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class PinPasswordActivityTest {

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var profileTestHelper: ProfileTestHelper

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  private val adminPin = "12345"
  private val adminId = 0
  private val userId = 1

  @Before
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
    profileTestHelper.initializeProfiles()
    testCoroutineDispatchers.registerIdlingResource()
    FirebaseApp.initializeApp(context)
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
  fun testPinPasswordActivityWithAdmin_checkKeyboardIsVisibleByDefault() {
    ActivityScenario.launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context,
        adminPin,
        adminId
      )
    ).use {
      onView(withId(R.id.input_pin)).check(matches(hasFocus()))
    }
  }

  @Test
  fun testPinPasswordActivityWithAdmin_inputCorrectPin_checkOpensHomeActivity() {
    ActivityScenario.launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context,
        adminPin,
        adminId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.input_pin)).perform(appendText("12345"))
      testCoroutineDispatchers.runCurrent()
      intended(hasComponent(HomeActivity::class.java.name))
    }
  }

  @Test
  fun testPinPasswordActivityWithUser_inputCorrectPin_checkOpensHomeActivity() {
    ActivityScenario.launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context,
        adminPin,
        userId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.input_pin)).perform(appendText("123"))
      testCoroutineDispatchers.runCurrent()
      intended(hasComponent(HomeActivity::class.java.name))
    }
  }

  @Test
  fun testPinPasswordActivityWithAdmin_inputWrongPin_checkIncorrectPinShows() {
    ActivityScenario.launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context,
        adminPin,
        adminId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      closeSoftKeyboard()
      onView(withId(R.id.input_pin)).perform(closeSoftKeyboard())
        .perform(appendText("54321"), closeSoftKeyboard())
      onView(withText(context.getString(R.string.pin_password_incorrect_pin))).check(
        matches(
          isDisplayed()
        )
      )
    }
  }

  @Test
  fun testPinPasswordActivityWithUser_inputWrongPin_checkIncorrectPinShows() {
    ActivityScenario.launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context,
        adminPin,
        userId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.input_pin)).perform(appendText("321"), closeSoftKeyboard())
      onView(withText(context.getString(R.string.pin_password_incorrect_pin))).check(
        matches(
          isDisplayed()
        )
      )
    }
  }

  @Test
  fun testPinPasswordActivityWithAdmin_clickForgot_checkOpensAdminForgotDialog() {
    ActivityScenario.launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context,
        adminPin,
        adminId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.input_pin)).perform(appendText(""), closeSoftKeyboard())
      onView(withId(R.id.forgot_pin)).perform(click())
      onView(withText(context.getString(R.string.pin_password_forgot_message)))
        .inRoot(isDialog())
        .check(matches(isDisplayed()))
    }
  }

  @Test
  fun testPinPasswordActivityWithUser_clickForgot_inputWrongAdminPin_checkWrongAdminPinError() {
    ActivityScenario.launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context,
        adminPin,
        userId
      )
    ).use {
      onView(withId(R.id.forgot_pin)).perform(click())
      testCoroutineDispatchers.runCurrent()
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin))))
        .inRoot(isDialog())
        .perform(appendText("1234"), closeSoftKeyboard())

      onView(withText(context.getString(R.string.admin_settings_submit)))
        .inRoot(isDialog())
        .perform(click())
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.input_pin))
        )
      ).inRoot(isDialog())
        .check(matches(withText(context.getString(R.string.admin_settings_incorrect))))

      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        appendText("5"),
        closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.error_text), isDescendantOfA(withId(R.id.input_pin))))
        .inRoot(isDialog())
        .check(matches(withText("")))
    }
  }

  @Test
  fun testPinPasswordActivityWithUser_clickForgot_inputAdminPin_inputShortPin_checkPinLengthError() { // ktlint-disable max-line-length
    ActivityScenario.launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context,
        adminPin,
        userId
      )
    ).use {
      onView(withId(R.id.input_pin)).perform(typeText(""), closeSoftKeyboard())
      onView(withId(R.id.forgot_pin)).perform(click())
      testCoroutineDispatchers.runCurrent()
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin))))
        .inRoot(isDialog())
        .perform(appendText("12345"), closeSoftKeyboard())

      onView(withText(context.getString(R.string.admin_settings_submit)))
        .inRoot(isDialog())
        .perform(click())

      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin))))
        .inRoot(isDialog())
        .perform(appendText("32"), closeSoftKeyboard())

      onView(withText(context.getString(R.string.admin_settings_submit)))
        .inRoot(isDialog())
        .perform(click())
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.input_pin))
        )
      ).inRoot(isDialog())
        .check(matches(withText(context.getString(R.string.add_profile_error_pin_length))))

      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        appendText("1"),
        closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.error_text), isDescendantOfA(withId(R.id.input_pin))))
        .inRoot(isDialog())
        .check(matches(withText("")))
    }
  }

  @Test
  fun testPinPasswordActivityWithUser_clickForgot_inputAdminPin_inputNewPin_inputOldPin_checkWrongPinError() { // ktlint-disable max-line-length
    ActivityScenario.launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context,
        adminPin,
        userId
      )
    ).use {
      onView(withId(R.id.input_pin)).perform(typeText(""), closeSoftKeyboard())
      onView(withId(R.id.forgot_pin)).perform(click())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin))))
        .inRoot(isDialog())
        .perform(appendText("12345"), closeSoftKeyboard())

      onView(withText(context.getString(R.string.admin_settings_submit)))
        .inRoot(isDialog())
        .perform(click())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin))))
        .inRoot(isDialog())
        .perform(appendText("321"), closeSoftKeyboard())

      onView(withText(context.getString(R.string.admin_settings_submit)))
        .inRoot(isDialog())
        .perform(click())

      testCoroutineDispatchers.runCurrent()
      onView(withText(context.getString(R.string.pin_password_close)))
        .inRoot(isDialog())
        .perform(click())
      onView(withId(R.id.input_pin)).perform(appendText("123"), closeSoftKeyboard())
      onView(withText(context.getString(R.string.pin_password_incorrect_pin)))
        .check(matches(isDisplayed()))
    }
  }

  @Test
  fun testPinPasswordActivityWithUser_clickForgot_inputAdminPin_inputNewPin_inputNewPin_checkOpensHomeActivity() { // ktlint-disable max-line-length
    ActivityScenario.launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context,
        adminPin,
        userId
      )
    ).use {
      onView(withId(R.id.input_pin)).perform(typeText(""), closeSoftKeyboard())
      onView(withId(R.id.forgot_pin)).perform(click())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin))))
        .inRoot(isDialog())
        .perform(appendText("12345"), closeSoftKeyboard())

      onView(withText(context.getString(R.string.admin_settings_submit)))
        .inRoot(isDialog())
        .perform(click())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin))))
        .inRoot(isDialog())
        .perform(appendText("321"), closeSoftKeyboard())
      onView(withText(context.getString(R.string.admin_settings_submit)))
        .inRoot(isDialog())
        .perform(click())

      testCoroutineDispatchers.runCurrent()
      onView(withText(context.getString(R.string.pin_password_close)))
        .inRoot(isDialog())
        .perform(click())
      onView(withId(R.id.input_pin)).perform(appendText("321"))
      testCoroutineDispatchers.runCurrent()
      intended(hasComponent(HomeActivity::class.java.name))
    }
  }

  @Test
  fun testPinPasswordActivityWithUser_clickForgot_inputAdminPin_changeConfiguration_checkInputPinIsPresent() { // ktlint-disable max-line-length
    ActivityScenario.launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context,
        adminPin,
        userId
      )
    ).use {
      onView(withId(R.id.input_pin)).perform(typeText(""), closeSoftKeyboard())
      onView(withId(R.id.forgot_pin)).perform(click())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin))))
        .inRoot(isDialog())
        .perform(appendText("1234"), closeSoftKeyboard())
      onView(isRoot()).perform(orientationLandscape())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin))))
        .inRoot(isDialog())
        .check(matches(withText("1234")))
    }
  }

  @Test
  fun testPinPasswordActivityWithUser_clickForgot_inputAdminPin_clickSubmit_changeConfiguration_restPinDialogIsDisplayed() { // ktlint-disable max-line-length
    ActivityScenario.launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context,
        adminPin,
        userId
      )
    ).use {
      onView(withId(R.id.input_pin)).perform(typeText(""), closeSoftKeyboard())
      onView(withId(R.id.forgot_pin)).perform(click())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin))))
        .inRoot(isDialog())
        .perform(appendText("12345"), closeSoftKeyboard())
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
  fun testPinPasswordActivityWithUser_clickForgot_inputAdminPin_clickSubmit_inputNewPin_changeConfiguration_clickSubmit_pinChangeIsSuccessful() { // ktlint-disable max-line-length
    ActivityScenario.launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context,
        adminPin,
        userId
      )
    ).use {
      onView(withId(R.id.input_pin)).perform(typeText(""), closeSoftKeyboard())
      onView(withId(R.id.forgot_pin)).perform(click())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin))))
        .inRoot(isDialog())
        .perform(appendText("12345"), closeSoftKeyboard())

      onView(withText(context.getString(R.string.admin_settings_submit)))
        .inRoot(isDialog())
        .perform(click())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin))))
        .inRoot(isDialog())
        .perform(appendText("123"), closeSoftKeyboard())
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
  fun testPinPasswordActivityWithAdmin_clickForgot_changeConfiguration_checkOpensAdminForgotDialog() { // ktlint-disable max-line-length
    ActivityScenario.launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context,
        adminPin,
        adminId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      closeSoftKeyboard()
      onView(withId(R.id.forgot_pin)).perform(click())
      onView(isRoot()).perform(orientationLandscape())
      onView(withText(context.getString(R.string.pin_password_forgot_message)))
        .inRoot(isDialog())
        .check(matches(isDisplayed()))
    }
  }

  @Test
  fun testPinPasswordActivityWithUser_clickForgot_inputWrongAdminPin_changeConfiguration_checkWrongAdminPinError() { // ktlint-disable max-line-length
    ActivityScenario.launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context,
        adminPin,
        userId
      )
    ).use {
      onView(withId(R.id.input_pin)).perform(typeText(""), closeSoftKeyboard())
      onView(withId(R.id.forgot_pin)).perform(click())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin))))
        .inRoot(isDialog())
        .perform(appendText("1234"), closeSoftKeyboard())
      onView(withText(context.getString(R.string.admin_settings_submit)))
        .inRoot(isDialog())
        .perform(click())
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.input_pin))
        )
      ).inRoot(isDialog())
        .check(matches(withText(context.getString(R.string.admin_settings_incorrect))))

      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin))))
        .inRoot(isDialog())
        .perform(appendText("5"), closeSoftKeyboard())
      onView(isRoot()).perform(orientationLandscape())
      onView(allOf(withId(R.id.error_text), isDescendantOfA(withId(R.id.input_pin))))
        .inRoot(isDialog())
        .check(matches(withText("")))
    }
  }

  @Test
  fun testPinPasswordActivityWithUser_clickForgot_inputAdminPin_inputIncorrectPin_clickSubmit_changeConfiguration_errorIsDisplayed() { // ktlint-disable max-line-length
    ActivityScenario.launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context,
        adminPin,
        userId
      )
    ).use {
      onView(withId(R.id.input_pin)).perform(typeText(""), closeSoftKeyboard())
      onView(withId(R.id.forgot_pin)).perform(click())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin))))
        .inRoot(isDialog())
        .perform(appendText("12345"), closeSoftKeyboard())

      onView(withText(context.getString(R.string.admin_settings_submit)))
        .inRoot(isDialog())
        .perform(click())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin))))
        .inRoot(isDialog())
        .perform(appendText("11"), closeSoftKeyboard())
      onView(withText(context.getString(R.string.admin_settings_submit)))
        .inRoot(isDialog())
        .perform(click())
      onView(isRoot()).perform(orientationLandscape())
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.input_pin))
        )
      ).inRoot(isDialog())
        .check(matches(withText(R.string.add_profile_error_pin_length)))
    }
  }

  @Test
  fun testPinPasswordActivityWithAdmin_inputWrongPin_changeConfiguration_checkIncorrectPinShows() {
    ActivityScenario.launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context,
        adminPin,
        adminId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      closeSoftKeyboard()
      onView(withId(R.id.input_pin)).perform(appendText("54321"), closeSoftKeyboard())
      onView(isRoot()).perform(orientationLandscape())
      onView(withText(context.getString(R.string.pin_password_incorrect_pin))).check(
        matches(
          isDisplayed()
        )
      )
    }
  }

  @Test
  fun testPinPasswordActivityWithAdmin_checkShowHidePassword_defaultText() {
    ActivityScenario.launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context,
        adminPin,
        adminId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      closeSoftKeyboard()
      onView(withText(context.getString(R.string.pin_password_show))).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testPinPasswordActivityWithAdmin_checkShowHidePassword_defaultImage() {
    ActivityScenario.launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context,
        adminPin,
        adminId
      )
    ).use {
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
  fun testPinPasswordActivityWithAdmin_checkShowHidePassword_clickShowHidePassword_textChangesToHide() { // ktlint-disable max-line-length
    ActivityScenario.launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context,
        adminPin,
        adminId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      closeSoftKeyboard()
      onView(withId(R.id.show_pin)).perform(click())
      onView(withText(context.getString(R.string.pin_password_hide))).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testPinPasswordActivityWithAdmin_checkShowHidePassword_clickShowHidePassword_imageChangesToHide() { // ktlint-disable max-line-length
    ActivityScenario.launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context,
        adminPin,
        adminId
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      closeSoftKeyboard()
      onView(withId(R.id.show_pin)).perform(click())
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
  fun testPinPasswordActivityWithAdmin_checkShowHidePassword_clickShowHidePassword_changeConfiguration_hideViewIsShown() { // ktlint-disable max-line-length
    ActivityScenario.launch<PinPasswordActivity>(
      PinPasswordActivity.createPinPasswordActivityIntent(
        context,
        adminPin,
        adminId
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
              R.drawable.ic_hide_eye_icon
            )
          )
        )
    }
  }

  private fun getCurrentActivity(): Activity? {
    var currentActivity: Activity? = null
    getInstrumentation().runOnMainSync {
      run {
        currentActivity = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(
          Stage.RESUMED
        ).elementAtOrNull(0)
      }
    }
    return currentActivity
  }

  private inline fun <reified T : Activity> isVisible(): Boolean {
    val am =
      InstrumentationRegistry.getInstrumentation().targetContext.getSystemService(
        ACTIVITY_SERVICE
      ) as ActivityManager
    val visibleActivityName = this.getCurrentActivity()!!::class.java.name
    return visibleActivityName == T::class.java.name
  }

  private inline fun <reified T : Activity> waitUntilActivityVisible() {
    val startTime = System.currentTimeMillis()
    while (!isVisible<T>()) {
      Thread.sleep(CONDITION_CHECK_INTERVAL)
      if (System.currentTimeMillis() - startTime >= TIMEOUT) {
        throw AssertionError(
          "Activity ${T::class.java.simpleName} not visible after $TIMEOUT milliseconds"
        )
      }
    }
  }

  // TODO(#1840)
  /**
   * Appends the specified text to a view. This is needed because Robolectric doesn't seem to
   * properly input digits for text views using 'android:digits'. See
   * https://github.com/robolectric/robolectric/issues/5110 for specifics.
   */
  private fun appendText(text: String): ViewAction {
    val typeTextViewAction = typeText(text)
    return object : ViewAction {
      override fun getDescription(): String = typeTextViewAction.description

      override fun getConstraints(): Matcher<View> = typeTextViewAction.constraints

      override fun perform(uiController: UiController?, view: View?) {
        // Appending text only works on Robolectric, whereas Espresso needs to use typeText().
        if (Build.FINGERPRINT.contains("robolectric", ignoreCase = true)) {
          (view as? EditText)?.append(text)
          testCoroutineDispatchers.runCurrent()
        } else {
          typeTextViewAction.perform(uiController, view)
        }
      }
    }
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  // TODO(#1675): Add NetworkModule once data module is migrated off of Moshi.
  @Singleton
  @Component(
    modules = [
      TestDispatcherModule::class, ApplicationModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, ImageClickInputModule::class, InteractionsModule::class,
      GcsResourceModule::class, GlideImageLoaderModule::class, ImageParsingModule::class,
      HtmlParserEntityTypeModule::class, QuestionModule::class, TestLogReportingModule::class,
      TestAccessibilityModule::class, LogStorageModule::class, CachingTestModule::class,
      PrimeTopicAssetsControllerModule::class, ExpirationMetaDataRetrieverModule::class,
      ViewBindingShimModule::class, RatioInputModule::class,
      ApplicationStartupListenerModule::class, LogUploadWorkerModule::class,
      WorkManagerConfigurationModule::class, HintsAndSolutionConfigModule::class,
      FirebaseLogUploaderModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent, ApplicationInjector {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

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
