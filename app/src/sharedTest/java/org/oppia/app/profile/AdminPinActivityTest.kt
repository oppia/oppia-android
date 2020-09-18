package org.oppia.app.profile

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.pressImeActionButton
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import dagger.Component
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.activity.ActivityComponent
import org.oppia.app.administratorcontrols.AdministratorControlsActivity
import org.oppia.app.application.ActivityComponentFactory
import org.oppia.app.application.ApplicationComponent
import org.oppia.app.application.ApplicationInjector
import org.oppia.app.application.ApplicationInjectorProvider
import org.oppia.app.application.ApplicationModule
import org.oppia.app.application.ApplicationStartupListenerModule
import org.oppia.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.app.shim.ViewBindingShimModule
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
import org.oppia.testing.TestingUtils
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

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = AdminPinActivityTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class AdminPinActivityTest {

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var profileTestHelper: ProfileTestHelper

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var testingUtils: TestingUtils

  @Before
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
    testCoroutineDispatchers.registerIdlingResource()
    profileTestHelper.initializeProfiles()
    FirebaseApp.initializeApp(context)
  }

  @After
  fun tearDown() {
    testCoroutineDispatchers.unregisterIdlingResource()
    Intents.release()
  }

  @Test
  fun testAdminPinActivity_inputPin_inputConfirmPin_clickSubmit_checkOpensAddProfileActivity() {
    launch<AdminPinActivity>(
      AdminPinActivity.createAdminPinActivityIntent(
        context,
        0,
        -10710042,
        1
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        testingUtils.appendText("12345"),
        closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        scrollTo(),
        testingUtils.appendText("12345"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.submit_button)).perform(scrollTo()).perform(click())
      testCoroutineDispatchers.runCurrent()
      intended(hasComponent(AddProfileActivity::class.java.name))
    }
  }

  // TODO(#962): Reenable once IME_ACTIONS work correctly on ProfileInputView.
  @Ignore("IME_ACTIONS doesn't work properly in ProfileInputView")
  @Test
  fun testAdminPinActivity_inputPin_inputConfirmPin_clickImeActionButton_checkOpensAddProfileActivity() { // ktlint-disable max-line-length
    launch<AdminPinActivity>(
      AdminPinActivity.createAdminPinActivityIntent(
        context,
        0,
        -10710042,
        1
      )
    ).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        typeText("12345"),
        closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        scrollTo(),
        typeText("12345"),
        pressImeActionButton()
      )
      intended(hasComponent(AddProfileActivity::class.java.name))
    }
  }

  @Test
  fun testAdminAuthActivity_inputPin_inputConfirmPin_clickSubmit_checkOpensAdministratorControlsActivity() { // ktlint-disable max-line-length
    launch<AdminAuthActivity>(
      AdminPinActivity.createAdminPinActivityIntent(
        context,
        0,
        -10710042,
        0
      )
    )
      .use {
        testCoroutineDispatchers.runCurrent()
        onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
          testingUtils.appendText("12345"),
          closeSoftKeyboard()
        )
        testCoroutineDispatchers.runCurrent()
        onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
          scrollTo(),
          testingUtils.appendText("12345"),
          closeSoftKeyboard()
        )
        testCoroutineDispatchers.runCurrent()
        onView(withId(R.id.submit_button)).perform(scrollTo()).perform(click())
        testCoroutineDispatchers.runCurrent()
        intended(hasComponent(AdministratorControlsActivity::class.java.name))
      }
  }

  // TODO(#962): Reenable once IME_ACTIONS work correctly on ProfileInputView.
  @Ignore("IME_ACTIONS doesn't work properly in ProfileInputView")
  @Test
  fun testAdminAuthActivity_inputPin_inputConfirmPin_clickImeActionButton_checkOpensAdministratorControlsActivity() { // ktlint-disable max-line-length
    launch<AdminAuthActivity>(
      AdminPinActivity.createAdminPinActivityIntent(
        context,
        0,
        -10710042,
        0
      )
    )
      .use {
        onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
          typeText("12345"),
          closeSoftKeyboard()
        )
        onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
          scrollTo(),
          typeText("12345"),
          pressImeActionButton()
        )
        intended(hasComponent(AdministratorControlsActivity::class.java.name))
      }
  }

  @Test
  fun testAdminPinActivity_inputShortPin_clickIsDisabled() {
    launch<AdminPinActivity>(
      AdminPinActivity.createAdminPinActivityIntent(
        context,
        0,
        -10710042,
        0
      )
    ).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        typeText("123"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.submit_button)).perform(scrollTo())
      onView(withId(R.id.submit_button)).check(matches(not(isClickable())))
    }
  }

  @Test
  fun testAdminPinActivity_inputShortPin_clickSubmit_inputPin_checkErrorIsCleared() {
    launch<AdminPinActivity>(
      AdminPinActivity.createAdminPinActivityIntent(
        context,
        0,
        -10710042,
        0
      )
    ).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        typeText("123"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.submit_button)).perform(scrollTo(), click())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        typeText("45"),
        closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.error_text), isDescendantOfA(withId(R.id.input_pin)))).check(
        matches(
          withText("")
        )
      )
    }
  }

  @Test
  fun testAdminPinActivity_inputPin_inputWrongConfirmPin_clickSubmit_checkConfirmWrongError() {
    launch<AdminPinActivity>(
      AdminPinActivity.createAdminPinActivityIntent(
        context,
        0,
        -10710042,
        0
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        testingUtils.appendText("12345"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        testingUtils.appendText("1234"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.submit_button)).perform(scrollTo()).perform(click())
      onView(allOf(withId(R.id.error_text), isDescendantOfA(withId(R.id.input_confirm_pin)))).check(
        matches(
          withText(
            context.getString(R.string.admin_pin_error_pin_confirm_wrong)
          )
        )
      )
    }
  }

  // TODO(#962): Reenable once IME_ACTIONS work correctly on ProfileInputView.
  @Ignore("IME_ACTIONS doesn't work properly in ProfileInputView")
  @Test
  fun testAdminPinActivity_inputPin_inputWrongConfirmPin_clickImeActionButton_checkConfirmWrongError() { // ktlint-disable max-line-length
    launch<AdminPinActivity>(
      AdminPinActivity.createAdminPinActivityIntent(
        context,
        0,
        -10710042,
        0
      )
    ).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        typeText("12345"),
        closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        typeText("1234"),
        pressImeActionButton()
      )
      onView(allOf(withId(R.id.error_text), isDescendantOfA(withId(R.id.input_confirm_pin)))).check(
        matches(
          withText(
            context.getString(R.string.admin_pin_error_pin_confirm_wrong)
          )
        )
      )
    }
  }

  @Test
  fun testAdminPinActivity_inputPin_inputWrongConfirmPin_clickSubmit_inputConfirmPin_checkErrorIsCleared() { // ktlint-disable max-line-length
    launch<AdminPinActivity>(
      AdminPinActivity.createAdminPinActivityIntent(
        context,
        0,
        -10710042,
        0
      )
    ).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        typeText("12345"),
        closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        typeText("1234"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.submit_button)).perform(scrollTo()).perform(click())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        typeText("5"),
        closeSoftKeyboard()
      )
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.input_confirm_pin))
        )
      ).check(matches(withText("")))
    }
  }

  // TODO(#962): Reenable once IME_ACTIONS work correctly on ProfileInputView.
  @Ignore("IME_ACTIONS doesn't work properly in ProfileInputView")
  @Test
  fun testAdminPinActivity_inputPin_inputWrongConfirmPin_clickImeActionButton_inputConfirmPin_checkErrorIsCleared() { // ktlint-disable max-line-length
    launch<AdminPinActivity>(
      AdminPinActivity.createAdminPinActivityIntent(
        context,
        0,
        -10710042,
        0
      )
    ).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        typeText("12345"),
        closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        typeText("1234"),
        pressImeActionButton()
      )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        typeText("5"),
        closeSoftKeyboard()
      )
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.input_confirm_pin))
        )
      ).check(matches(withText("")))
    }
  }

  @Test
  fun testAdminPinActivity_configurationChange_inputPin_inputConfirmPin_clickSubmit_checkOpensAddProfileActivity() { // ktlint-disable max-line-length
    launch<AdminPinActivity>(
      AdminPinActivity.createAdminPinActivityIntent(
        context,
        0,
        -10710042,
        1
      )
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      closeSoftKeyboard()
      testCoroutineDispatchers.runCurrent()
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        testingUtils.appendText("12345"),
        closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        scrollTo(),
        testingUtils.appendText("12345"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.submit_button)).perform(scrollTo(), click())
      testCoroutineDispatchers.runCurrent()
      intended(hasComponent(AddProfileActivity::class.java.name))
    }
  }

  // TODO(#962): Reenable once IME_ACTIONS work correctly on ProfileInputView.
  @Ignore("IME_ACTIONS doesn't work properly in ProfileInputView")
  @Test
  fun testAdminPinActivity_configurationChange_inputPin_inputConfirmPin_clickImeActionButton_checkOpensAddProfileActivity() { // ktlint-disable max-line-length
    launch<AdminPinActivity>(
      AdminPinActivity.createAdminPinActivityIntent(
        context,
        0,
        -10710042,
        1
      )
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      closeSoftKeyboard()
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        typeText("12345"),
        closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        scrollTo(),
        typeText("12345"),
        pressImeActionButton()
      )
      intended(hasComponent(AddProfileActivity::class.java.name))
    }
  }

  @Test
  fun testAdminPinActivity_configurationChange_inputPin_inputConfirmPin_clickSubmit_checkOpensAdministratorControlsActivity() { // ktlint-disable max-line-length
    launch<AdminPinActivity>(
      AdminPinActivity.createAdminPinActivityIntent(
        context,
        0,
        -10710042,
        0
      )
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      closeSoftKeyboard()
      testCoroutineDispatchers.runCurrent()
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        testingUtils.appendText("12345"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        scrollTo(),
        testingUtils.appendText("12345"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.submit_button)).perform(scrollTo(), click())
      testCoroutineDispatchers.runCurrent()
      intended(hasComponent(AdministratorControlsActivity::class.java.name))
    }
  }

  // TODO(#962): Reenable once IME_ACTIONS work correctly on ProfileInputView.
  @Ignore("IME_ACTIONS doesn't work properly in ProfileInputView")
  @Test
  fun testAdminPinActivity_configurationChange_inputPin_inputConfirmPin_clickImeActionButton_checkOpensAdministratorControlsActivity() { // ktlint-disable max-line-length
    launch<AdminPinActivity>(
      AdminPinActivity.createAdminPinActivityIntent(
        context,
        0,
        -10710042,
        0
      )
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      closeSoftKeyboard()
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        typeText("12345"),
        closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        scrollTo(),
        typeText("12345"),
        pressImeActionButton()
      )
      intended(hasComponent(AdministratorControlsActivity::class.java.name))
    }
  }

  @Test
  fun testAdminPinActivity_configurationChange_inputShortPin_clickSubmit_clickIsDisabled() {
    launch<AdminPinActivity>(
      AdminPinActivity.createAdminPinActivityIntent(
        context,
        0,
        -10710042,
        0
      )
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        typeText("123"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.submit_button)).perform(scrollTo())
      onView(withId(R.id.submit_button)).check(matches(not(isClickable())))
    }
  }

  @Test
  fun testAdminPinActivity_configurationChange_inputShortPin_clickSubmit_inputPin_checkErrorIsCleared() { // ktlint-disable max-line-length
    launch<AdminPinActivity>(
      AdminPinActivity.createAdminPinActivityIntent(
        context,
        0,
        -10710042,
        0
      )
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        typeText("123"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.submit_button)).perform(scrollTo(), click())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        typeText("45"),
        closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.error_text), isDescendantOfA(withId(R.id.input_pin)))).check(
        matches(
          withText("")
        )
      )
    }
  }

  @Test
  fun testAdminPinActivity_configurationChange_inputPin_inputWrongConfirmPin_clickSubmit_checkConfirmWrongError() { // ktlint-disable max-line-length
    launch<AdminPinActivity>(
      AdminPinActivity.createAdminPinActivityIntent(
        context,
        0,
        -10710042,
        0
      )
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      closeSoftKeyboard()
      testCoroutineDispatchers.runCurrent()
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        testingUtils.appendText("12345"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        scrollTo(),
        testingUtils.appendText("1234"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.submit_button)).perform(scrollTo(), click())
      testCoroutineDispatchers.runCurrent()
      onView(allOf(withId(R.id.error_text), isDescendantOfA(withId(R.id.input_confirm_pin)))).check(
        matches(
          withText(
            context.getString(R.string.admin_pin_error_pin_confirm_wrong)
          )
        )
      )
    }
  }

  // TODO(#962): Reenable once IME_ACTIONS work correctly on ProfileInputView.
  @Ignore("IME_ACTIONS doesn't work properly in ProfileInputView")
  @Test
  fun testAdminPinActivity_configurationChange_inputPin_inputWrongConfirmPin_clickImeActionButton_checkConfirmWrongError() { // ktlint-disable max-line-length
    launch<AdminPinActivity>(
      AdminPinActivity.createAdminPinActivityIntent(
        context,
        0,
        -10710042,
        0
      )
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      closeSoftKeyboard()
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        typeText("12345"),
        closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        scrollTo(),
        typeText("1234"),
        pressImeActionButton()
      )
      onView(allOf(withId(R.id.error_text), isDescendantOfA(withId(R.id.input_confirm_pin)))).check(
        matches(
          withText(
            context.getString(R.string.admin_pin_error_pin_confirm_wrong)
          )
        )
      )
    }
  }

  @Test
  fun testAdminPinActivity_configurationChange_inputPin_inputWrongConfirmPin_clickSubmit_inputConfirmPin_checkErrorIsCleared() { // ktlint-disable max-line-length
    launch<AdminPinActivity>(
      AdminPinActivity.createAdminPinActivityIntent(
        context,
        0,
        -10710042,
        0
      )
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      closeSoftKeyboard()
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        typeText("12345"),
        closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        scrollTo(),
        typeText("1234"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.submit_button)).perform(scrollTo(), click())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        scrollTo(),
        typeText("5"),
        closeSoftKeyboard()
      )
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.input_confirm_pin))
        )
      ).check(matches(withText("")))
    }
  }

  // TODO(#962): Reenable once IME_ACTIONS work correctly on ProfileInputView.
  @Ignore("IME_ACTIONS doesn't work properly in ProfileInputView")
  @Test
  fun testAdminPinActivity_configurationChange_inputPin_inputWrongConfirmPin_clickImeActionButton_inputConfirmPin_checkErrorIsCleared() { // ktlint-disable max-line-length
    launch<AdminPinActivity>(
      AdminPinActivity.createAdminPinActivityIntent(
        context,
        0,
        -10710042,
        0
      )
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      closeSoftKeyboard()
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        typeText("12345"),
        closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        scrollTo(),
        typeText("1234"),
        pressImeActionButton()
      )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        scrollTo(),
        typeText("5"),
        closeSoftKeyboard()
      )
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.input_confirm_pin))
        )
      ).check(matches(withText("")))
    }
  }

  @Test
  fun testAdminPinActivity_inputPin_inputWrongConfirmPin_clickSubmit_configurationChange_checkConfirmWrongError() { // ktlint-disable max-line-length
    launch<AdminPinActivity>(
      AdminPinActivity.createAdminPinActivityIntent(
        context,
        0,
        -10710042,
        0
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        testingUtils.appendText("12345"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        scrollTo(),
        testingUtils.appendText("54321"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.submit_button)).perform(scrollTo()).perform(click())
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.input_confirm_pin))
        )
      ).check(matches(withText(R.string.admin_pin_error_pin_confirm_wrong)))
    }
  }

  // TODO(#962): Reenable once IME_ACTIONS work correctly on ProfileInputView.
  @Ignore("IME_ACTIONS doesn't work properly in ProfileInputView")
  @Test
  fun testAdminPinActivity_inputPin_inputWrongConfirmPin_clickImeActionButton_configurationChange_checkConfirmWrongError() { // ktlint-disable max-line-length
    launch<AdminPinActivity>(
      AdminPinActivity.createAdminPinActivityIntent(
        context,
        0,
        -10710042,
        0
      )
    ).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        typeText("12345"),
        closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        scrollTo(),
        typeText("54321"),
        pressImeActionButton()
      )
      onView(isRoot()).perform(orientationLandscape())
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.input_confirm_pin))
        )
      ).check(matches(withText(R.string.admin_pin_error_pin_confirm_wrong)))
    }
  }

  @Test
  fun testAdminPinActivity_inputShortPin_configurationChange_clicIsDisabled() {
    launch<AdminPinActivity>(
      AdminPinActivity.createAdminPinActivityIntent(
        context,
        0,
        -10710042,
        0
      )
    ).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        typeText("123"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.submit_button)).perform(scrollTo())
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.submit_button)).check(matches(not(isClickable())))
    }
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
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

    fun inject(adminPinActivityTest: AdminPinActivityTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerAdminPinActivityTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(adminPinActivityTest: AdminPinActivityTest) {
      component.inject(adminPinActivityTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
