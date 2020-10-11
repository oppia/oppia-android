package org.oppia.android.app.profile

import android.app.Application
import android.content.Context
import android.view.View
import android.view.ViewParent
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.PerformException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.pressImeActionButton
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.util.HumanReadables
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import dagger.Component
import org.hamcrest.Matchers
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.administratorcontrols.AdministratorControlsActivity
import org.oppia.android.app.application.ActivityComponentFactory
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.player.state.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.utility.OrientationChangeAction.Companion.orientationLandscape
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
import org.oppia.android.testing.EditTextInputAction
import org.oppia.android.testing.TestAccessibilityModule
import org.oppia.android.testing.TestCoroutineDispatchers
import org.oppia.android.testing.TestDispatcherModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.profile.ProfileTestHelper
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
  lateinit var editTextInputAction: EditTextInputAction

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
        editTextInputAction.appendText("12345"),
        closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        nestedScrollTo(),
        editTextInputAction.appendText("12345"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.submit_button)).perform(nestedScrollTo()).perform(click())
      testCoroutineDispatchers.runCurrent()
      intended(hasComponent(AddProfileActivity::class.java.name))
    }
  }

  // TODO(#962): Reenable once IME_ACTIONS work correctly on ProfileInputView.
  @Ignore("IME_ACTIONS doesn't work properly in ProfileInputView")
  @Test
  fun testAdminPin_inputPin_inputConfirmPin_clickImeActionButton_checkOpensAddProfile() {
    launch<AdminPinActivity>(
      AdminPinActivity.createAdminPinActivityIntent(
        context,
        0,
        -10710042,
        1
      )
    ).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        editTextInputAction.appendText("12345"),
        closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        nestedScrollTo(),
        editTextInputAction.appendText("12345"),
        pressImeActionButton()
      )
      intended(hasComponent(AddProfileActivity::class.java.name))
    }
  }

  @Test
  fun testAdminAuth_inputPin_inputConfirmPin_clickSubmit_checkOpensAdminCtrlsActivity() {
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
          editTextInputAction.appendText("12345"),
          closeSoftKeyboard()
        )
        testCoroutineDispatchers.runCurrent()
        onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
          nestedScrollTo(),
          editTextInputAction.appendText("12345"),
          closeSoftKeyboard()
        )
        testCoroutineDispatchers.runCurrent()
        onView(withId(R.id.submit_button)).perform(nestedScrollTo()).perform(click())
        testCoroutineDispatchers.runCurrent()
        intended(hasComponent(AdministratorControlsActivity::class.java.name))
      }
  }

  // TODO(#962): Reenable once IME_ACTIONS work correctly on ProfileInputView.
  @Ignore("IME_ACTIONS doesn't work properly in ProfileInputView")
  @Test
  fun testAdminAuth_inputPin_inputConfirmPin_clickImeActionButton_checkOpensAdminCtrlsActivity() {
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
          editTextInputAction.appendText("12345"),
          closeSoftKeyboard()
        )
        onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
          nestedScrollTo(),
          editTextInputAction.appendText("12345"),
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
        editTextInputAction.appendText("123"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.submit_button)).perform(nestedScrollTo())
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
        editTextInputAction.appendText("123"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.submit_button)).perform(nestedScrollTo(), click())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        editTextInputAction.appendText("45"),
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
        editTextInputAction.appendText("12345"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        editTextInputAction.appendText("1234"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.submit_button)).perform(nestedScrollTo()).perform(click())
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
  fun testAdminPin_inputPin_inputWrongConfirmPin_clickImeActionButton_checkConfirmWrongError() {
    launch<AdminPinActivity>(
      AdminPinActivity.createAdminPinActivityIntent(
        context,
        0,
        -10710042,
        0
      )
    ).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        editTextInputAction.appendText("12345"),
        closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        editTextInputAction.appendText("1234"),
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
  fun testAdminPin_inputPin_inputWrongConfirmPin_clickSubmit_inputConfirmPin_checkErrorCleared() {
    launch<AdminPinActivity>(
      AdminPinActivity.createAdminPinActivityIntent(
        context,
        0,
        -10710042,
        0
      )
    ).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        editTextInputAction.appendText("12345"),
        closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        editTextInputAction.appendText("1234"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.submit_button)).perform(nestedScrollTo()).perform(click())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        editTextInputAction.appendText("5"),
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
  fun testAdminPin_inputPin_inputWrongConfirmPin_clickImeAction_inputConfirmPin_errorCleared() {
    launch<AdminPinActivity>(
      AdminPinActivity.createAdminPinActivityIntent(
        context,
        0,
        -10710042,
        0
      )
    ).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        editTextInputAction.appendText("12345"),
        closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        editTextInputAction.appendText("1234"),
        pressImeActionButton()
      )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        editTextInputAction.appendText("5"),
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
  fun testAdminPin_configChange_inputPin_inputConfirmPin_clickSubmit_checkOpensAddProfile() {
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
        editTextInputAction.appendText("12345"),
        closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        nestedScrollTo(),
        editTextInputAction.appendText("12345"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.submit_button)).perform(nestedScrollTo(), click())
      testCoroutineDispatchers.runCurrent()
      intended(hasComponent(AddProfileActivity::class.java.name))
    }
  }

  // TODO(#962): Reenable once IME_ACTIONS work correctly on ProfileInputView.
  @Ignore("IME_ACTIONS doesn't work properly in ProfileInputView")
  @Test
  fun testAdminPin_configChange_inputPin_inputConfirmPin_clickImeActionBtn_checkOpensAddProfile() {
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
        editTextInputAction.appendText("12345"),
        closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        nestedScrollTo(),
        editTextInputAction.appendText("12345"),
        pressImeActionButton()
      )
      intended(hasComponent(AddProfileActivity::class.java.name))
    }
  }

  @Test
  fun testAdminPin_configChange_inputPin_inputConfirmPin_clickSubmit_checkOpensAdminCtrls() {
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
        editTextInputAction.appendText("12345"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        nestedScrollTo(),
        editTextInputAction.appendText("12345"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.submit_button)).perform(nestedScrollTo(), click())
      testCoroutineDispatchers.runCurrent()
      intended(hasComponent(AdministratorControlsActivity::class.java.name))
    }
  }

  // TODO(#962): Reenable once IME_ACTIONS work correctly on ProfileInputView.
  @Ignore("IME_ACTIONS doesn't work properly in ProfileInputView")
  @Test
  fun testAdminPin_configChange_inputPin_inputConfirmPin_clickImeActionBtn_checkOpensAdminCtrls() {
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
        editTextInputAction.appendText("12345"),
        closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        nestedScrollTo(),
        editTextInputAction.appendText("12345"),
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
        editTextInputAction.appendText("123"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.submit_button)).perform(nestedScrollTo())
      onView(withId(R.id.submit_button)).check(matches(not(isClickable())))
    }
  }

  @Test
  fun testAdminPin_configChange_inputShortPin_clickSubmit_inputPin_checkErrorIsCleared() {
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
        editTextInputAction.appendText("123"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.submit_button)).perform(nestedScrollTo(), click())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        editTextInputAction.appendText("45"),
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
  fun testAdminPin_configChange_inputPin_inputWrongConfirmPin_clickSubmit_checkConfirmWrongError() {
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
        editTextInputAction.appendText("12345"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        nestedScrollTo(),
        editTextInputAction.appendText("1234"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.submit_button)).perform(nestedScrollTo(), click())
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
  fun testAdminPin_configChange_inputPin_inputWrongConfirmPin_clickImeAction_confirmWrongError() {
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
        editTextInputAction.appendText("12345"),
        closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        nestedScrollTo(),
        editTextInputAction.appendText("1234"),
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
  fun testAdminPin_configChange_inputPin_inputWrongConfirmPin_submit_inputConfirmPin_errorClrd() {
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
        editTextInputAction.appendText("12345"),
        closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        nestedScrollTo(),
        editTextInputAction.appendText("1234"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.submit_button)).perform(nestedScrollTo(), click())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        nestedScrollTo(),
        editTextInputAction.appendText("5"),
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
  fun testAdminPin_configChange_inputPin_WrongConfirmPin_clickImeAction_ConfirmPin_ErrorClrd() {
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
        editTextInputAction.appendText("12345"),
        closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        nestedScrollTo(),
        editTextInputAction.appendText("1234"),
        pressImeActionButton()
      )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        nestedScrollTo(),
        editTextInputAction.appendText("5"),
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
  fun testAdminPin_inputPin_inputWrongConfirmPin_submit_configChange_checkConfirmWrongError() {
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
        editTextInputAction.appendText("12345"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        nestedScrollTo(),
        editTextInputAction.appendText("54321"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.submit_button)).perform(nestedScrollTo()).perform(click())
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
  fun testAdminPin_inputPin_inputWrongConfirmPin_clickImeAction_configChange_confirmWrongError() {
    launch<AdminPinActivity>(
      AdminPinActivity.createAdminPinActivityIntent(
        context,
        0,
        -10710042,
        0
      )
    ).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_pin)))).perform(
        editTextInputAction.appendText("12345"),
        closeSoftKeyboard()
      )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.input_confirm_pin)))).perform(
        nestedScrollTo(),
        editTextInputAction.appendText("54321"),
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
        editTextInputAction.appendText("123"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.submit_button)).perform(nestedScrollTo())
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
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

    fun inject(adminPinActivityTest: AdminPinActivityTest)
  }

  /** Functions nestedScrollTo() and findFirstParentLayoutOfClass() taken from: https://stackoverflow.com/a/46037284/8860848 */
  private fun nestedScrollTo(): ViewAction {
    return object : ViewAction {
      override fun getDescription(): String {
        return "View is not NestedScrollView"
      }

      override fun getConstraints(): org.hamcrest.Matcher<View> {
        return Matchers.allOf(
          ViewMatchers.isDescendantOfA(ViewMatchers.isAssignableFrom(NestedScrollView::class.java))
        )
      }

      override fun perform(uiController: UiController, view: View) {
        try {
          val nestedScrollView =
            findFirstParentLayoutOfClass(view, NestedScrollView::class.java) as NestedScrollView
          nestedScrollView.scrollTo(0, view.top)
        } catch (e: Exception) {
          throw PerformException.Builder()
            .withActionDescription(this.description)
            .withViewDescription(HumanReadables.describe(view))
            .withCause(e)
            .build()
        }
        uiController.loopMainThreadUntilIdle()
      }
    }
  }

  private fun findFirstParentLayoutOfClass(view: View, parentClass: Class<out View>): View {
    var parent: ViewParent = FrameLayout(view.context)
    lateinit var incrementView: ViewParent
    var i = 0
    while (!(parent.javaClass === parentClass)) {
      if (i == 0) {
        parent = findParent(view)
      } else {
        parent = findParent(incrementView)
      }
      incrementView = parent
      i++
    }
    return parent as View
  }

  private fun findParent(view: View): ViewParent {
    return view.parent
  }

  private fun findParent(view: ViewParent): ViewParent {
    return view.parent
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
