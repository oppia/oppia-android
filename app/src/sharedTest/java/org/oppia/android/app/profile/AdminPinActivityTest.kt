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
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.util.HumanReadables
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.google.common.truth.Truth.assertThat
import dagger.Component
import org.hamcrest.Matchers
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.activity.ActivityComponentFactory
import org.oppia.android.app.activity.route.ActivityRouterModule
import org.oppia.android.app.administratorcontrols.AdministratorControlsActivity
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.application.testing.TestingBuildFlavorModule
import org.oppia.android.app.devoptions.DeveloperOptionsModule
import org.oppia.android.app.devoptions.DeveloperOptionsStarterModule
import org.oppia.android.app.model.ScreenName
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
import org.oppia.android.app.utility.OrientationChangeAction.Companion.orientationLandscape
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
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.espresso.EditTextInputAction
import org.oppia.android.testing.espresso.TextInputAction.Companion.hasErrorText
import org.oppia.android.testing.espresso.TextInputAction.Companion.hasNoErrorText
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
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
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.extractCurrentAppScreenName
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

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = AdminPinActivityTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class AdminPinActivityTest {
  @get:Rule
  val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @get:Rule
  val oppiaTestRule = OppiaTestRule()

  @get: Rule
  val activityTestRule: ActivityTestRule<AdminPinActivity> = ActivityTestRule(
    AdminPinActivity::class.java, /* initialTouchMode= */ true, /* launchActivity= */ false
  )

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
  }

  @After
  fun tearDown() {
    testCoroutineDispatchers.unregisterIdlingResource()
    Intents.release()
  }

  @Test
  fun testActivity_createIntent_verifyScreenNameInIntent() {
    val currentScreenName = AdminPinActivity.createAdminPinActivityIntent(
      context = context,
      profileId = 0,
      colorRgb = -10710042,
      adminPinEnum = 0
    ).extractCurrentAppScreenName()

    assertThat(currentScreenName).isEqualTo(ScreenName.ADMIN_PIN_ACTIVITY)
  }

  @Test
  fun testAdminPinActivity_hasCorrectActivityLabel() {
    activityTestRule.launchActivity(
      AdminPinActivity.createAdminPinActivityIntent(
        context = context,
        profileId = 0,
        colorRgb = -10710042,
        adminPinEnum = 0
      )
    )
    val title = activityTestRule.activity.title

    // Verify that the activity label is correct as a proxy to verify TalkBack will announce the
    // correct string when it's read out.
    assertThat(title).isEqualTo(context.getString(R.string.admin_pin_activity_title))
  }

  @Test
  fun testAdminPinActivity_inputPin_submit_opensAddProfileActivity() {
    launch<AdminPinActivity>(
      AdminPinActivity.createAdminPinActivityIntent(
        context = context,
        profileId = 0,
        colorRgb = -10710042,
        adminPinEnum = 1
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.admin_pin_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_pin_input_pin))
        )
      ).perform(
        editTextInputAction.appendText("12345"),
        closeSoftKeyboard()
      )
      onView(
        allOf(
          withId(R.id.admin_pin_input_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_pin_input_confirm_pin))
        )
      ).perform(
        nestedScrollTo(),
        editTextInputAction.appendText("12345"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.submit_button)).perform(nestedScrollTo()).perform(click())
      testCoroutineDispatchers.runCurrent()
      intended(hasComponent(AddProfileActivity::class.java.name))
    }
  }

  @Test
  fun testAdminPinActivity_inputPin_imeAction_opensAddProfileActivity() {
    launch<AdminPinActivity>(
      AdminPinActivity.createAdminPinActivityIntent(
        context = context,
        profileId = 0,
        colorRgb = -10710042,
        adminPinEnum = 1
      )
    ).use {
      onView(
        allOf(
          withId(R.id.admin_pin_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_pin_input_pin))
        )
      ).perform(
        editTextInputAction.appendText("12345"),
        closeSoftKeyboard()
      )
      onView(
        allOf(
          withId(R.id.admin_pin_input_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_pin_input_confirm_pin))
        )
      ).perform(
        nestedScrollTo(),
        editTextInputAction.appendText("12345"),
        pressImeActionButton()
      )
      testCoroutineDispatchers.runCurrent()
      intended(hasComponent(AddProfileActivity::class.java.name))
    }
  }

  @Test
  fun testAdminAuthActivity_inputPin_submit_opensAdminControlsActivity() {
    launch<AdminAuthActivity>(
      AdminPinActivity.createAdminPinActivityIntent(
        context = context,
        profileId = 0,
        colorRgb = -10710042,
        adminPinEnum = 0
      )
    )
      .use {
        testCoroutineDispatchers.runCurrent()
        onView(
          allOf(
            withId(R.id.admin_pin_input_pin_edit_text),
            isDescendantOfA(withId(R.id.admin_pin_input_pin))
          )
        ).perform(
          editTextInputAction.appendText("12345"),
          closeSoftKeyboard()
        )
        testCoroutineDispatchers.runCurrent()
        onView(
          allOf(
            withId(R.id.admin_pin_input_confirm_pin_edit_text),
            isDescendantOfA(withId(R.id.admin_pin_input_confirm_pin))
          )
        ).perform(
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

  @Test
  fun testAdminAuthActivity_inputPin_imeAction_opensAdminControlsActivity() {
    launch<AdminAuthActivity>(
      AdminPinActivity.createAdminPinActivityIntent(
        context = context,
        profileId = 0,
        colorRgb = -10710042,
        adminPinEnum = 0
      )
    )
      .use {
        onView(
          allOf(
            withId(R.id.admin_pin_input_pin_edit_text),
            isDescendantOfA(withId(R.id.admin_pin_input_pin))
          )
        ).perform(
          editTextInputAction.appendText("12345"),
          closeSoftKeyboard()
        )
        onView(
          allOf(
            withId(R.id.admin_pin_input_confirm_pin_edit_text),
            isDescendantOfA(withId(R.id.admin_pin_input_confirm_pin))
          )
        ).perform(
          nestedScrollTo(),
          editTextInputAction.appendText("12345"),
          pressImeActionButton()
        )
        testCoroutineDispatchers.runCurrent()
        intended(hasComponent(AdministratorControlsActivity::class.java.name))
      }
  }

  @Test
  fun testAdminPinActivity_inputShortPin_clickIsDisabled() {
    launch<AdminPinActivity>(
      AdminPinActivity.createAdminPinActivityIntent(
        context = context,
        profileId = 0,
        colorRgb = -10710042,
        adminPinEnum = 0
      )
    ).use {
      onView(
        allOf(
          withId(R.id.admin_pin_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_pin_input_pin))
        )
      ).perform(
        editTextInputAction.appendText("123"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.submit_button)).perform(nestedScrollTo())
      onView(withId(R.id.submit_button)).check(matches(not(isClickable())))
    }
  }

  @Test
  fun testAdminPinActivity_inputShortPin_submit_inputPin_errorIsCleared() {
    launch<AdminPinActivity>(
      AdminPinActivity.createAdminPinActivityIntent(
        context = context,
        profileId = 0,
        colorRgb = -10710042,
        adminPinEnum = 0
      )
    ).use {
      onView(
        allOf(
          withId(R.id.admin_pin_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_pin_input_pin))
        )
      ).perform(
        editTextInputAction.appendText("123"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.submit_button)).perform(nestedScrollTo(), click())
      onView(
        allOf(
          withId(R.id.admin_pin_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_pin_input_pin))
        )
      ).perform(
        editTextInputAction.appendText("45"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.admin_pin_input_confirm_pin))
        .check(matches(hasNoErrorText()))
    }
  }

  @Test
  fun testAdminPinActivity_inputPin_inputWrongConfirmPin_submit_confirmWrongError() {
    launch<AdminPinActivity>(
      AdminPinActivity.createAdminPinActivityIntent(
        context = context,
        profileId = 0,
        colorRgb = -10710042,
        adminPinEnum = 0
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.admin_pin_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_pin_input_pin))
        )
      ).perform(
        editTextInputAction.appendText("12345"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.admin_pin_input_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_pin_input_confirm_pin))
        )
      ).perform(
        editTextInputAction.appendText("1234"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.submit_button)).perform(nestedScrollTo()).perform(click())
      onView(withId(R.id.admin_pin_input_confirm_pin))
        .check(
          matches(
            hasErrorText(
              context.resources.getString(
                R.string.admin_pin_error_pin_confirm_wrong
              )
            )
          )
        )
    }
  }

  @Test
  fun testAdminPinActivity_inputPinAndWrongConfirmPin_imeAction_confirmWrongError() {
    launch<AdminPinActivity>(
      AdminPinActivity.createAdminPinActivityIntent(
        context = context,
        profileId = 0,
        colorRgb = -10710042,
        adminPinEnum = 0
      )
    ).use {
      onView(
        allOf(
          withId(R.id.admin_pin_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_pin_input_pin))
        )
      ).perform(
        editTextInputAction.appendText("12345"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.admin_pin_input_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_pin_input_confirm_pin))
        )
      ).perform(
        editTextInputAction.appendText("1234"),
        pressImeActionButton()
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.admin_pin_input_confirm_pin)).check(
        matches(
          hasErrorText(
            context.resources.getString(R.string.admin_pin_error_pin_confirm_wrong)
          )
        )
      )
    }
  }

  @Test
  fun testAdminPinActivity_inputPinAndWrongConfirmPin_submit_inputConfirmPin_errorIsCleared() {
    launch<AdminPinActivity>(
      AdminPinActivity.createAdminPinActivityIntent(
        context = context,
        profileId = 0,
        colorRgb = -10710042,
        adminPinEnum = 0
      )
    ).use {
      onView(
        allOf(
          withId(R.id.admin_pin_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_pin_input_pin))
        )
      ).perform(
        editTextInputAction.appendText("12345"),
        closeSoftKeyboard()
      )
      onView(
        allOf(
          withId(R.id.admin_pin_input_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_pin_input_confirm_pin))
        )
      ).perform(
        editTextInputAction.appendText("1234"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.submit_button)).perform(nestedScrollTo()).perform(click())
      onView(
        allOf(
          withId(R.id.admin_pin_input_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_pin_input_confirm_pin))
        )
      ).perform(
        editTextInputAction.appendText("5"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.admin_pin_input_confirm_pin))
        .check(matches(hasNoErrorText()))
    }
  }

  @Test
  fun testAdminPinActivity_inputPinAndWrongConfirmPin_inputConfirmPin_errorIsCleared() {
    launch<AdminPinActivity>(
      AdminPinActivity.createAdminPinActivityIntent(
        context = context,
        profileId = 0,
        colorRgb = -10710042,
        adminPinEnum = 0
      )
    ).use {
      onView(
        allOf(
          withId(R.id.admin_pin_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_pin_input_pin))
        )
      ).perform(
        editTextInputAction.appendText("12345"),
        closeSoftKeyboard()
      )
      onView(
        allOf(
          withId(R.id.admin_pin_input_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_pin_input_confirm_pin))
        )
      ).perform(
        editTextInputAction.appendText("1234"),
        pressImeActionButton()
      )
      onView(
        allOf(
          withId(R.id.admin_pin_input_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_pin_input_confirm_pin))
        )
      ).perform(
        editTextInputAction.appendText("5"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.admin_pin_input_confirm_pin))
        .check(matches(hasNoErrorText()))
    }
  }

  @Test
  fun testAdminPinActivity_closeButton_checkContentDescription() {
    launch<AdminPinActivity>(
      AdminPinActivity.createAdminPinActivityIntent(
        context = context,
        profileId = 0,
        colorRgb = -10710042,
        adminPinEnum = 1
      )
    ).use {
      onView(withContentDescription(R.string.admin_auth_close)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testAdminPinActivity_configChange_inputPin_submit_opensAddProfileActivity() {
    launch<AdminPinActivity>(
      AdminPinActivity.createAdminPinActivityIntent(
        context = context,
        profileId = 0,
        colorRgb = -10710042,
        adminPinEnum = 1
      )
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      closeSoftKeyboard()
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.admin_pin_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_pin_input_pin))
        )
      ).perform(
        editTextInputAction.appendText("12345"),
        closeSoftKeyboard()
      )
      onView(
        allOf(
          withId(R.id.admin_pin_input_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_pin_input_confirm_pin))
        )
      ).perform(
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

  @Test
  fun testAdminPinActivity_configChange_inputPin_imeAction_opensAddProfileActivity() {
    launch<AdminPinActivity>(
      AdminPinActivity.createAdminPinActivityIntent(
        context = context,
        profileId = 0,
        colorRgb = -10710042,
        adminPinEnum = 1
      )
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      closeSoftKeyboard()
      onView(
        allOf(
          withId(R.id.admin_pin_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_pin_input_pin))
        )
      ).perform(
        editTextInputAction.appendText("12345"),
        closeSoftKeyboard()
      )
      onView(
        allOf(
          withId(R.id.admin_pin_input_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_pin_input_confirm_pin))
        )
      ).perform(
        nestedScrollTo(),
        editTextInputAction.appendText("12345"),
        pressImeActionButton()
      )
      testCoroutineDispatchers.runCurrent()
      intended(hasComponent(AddProfileActivity::class.java.name))
    }
  }

  @Test
  fun testAdminPinActivity_configChange_inputPin_submit_opensAdminControlsActivity() {
    launch<AdminPinActivity>(
      AdminPinActivity.createAdminPinActivityIntent(
        context = context,
        profileId = 0,
        colorRgb = -10710042,
        adminPinEnum = 0
      )
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      closeSoftKeyboard()
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.admin_pin_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_pin_input_pin))
        )
      ).perform(
        editTextInputAction.appendText("12345"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.admin_pin_input_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_pin_input_confirm_pin))
        )
      ).perform(
        nestedScrollTo(),
        editTextInputAction.appendText("12345"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.submit_button)).perform(nestedScrollTo(), click())
      testCoroutineDispatchers.runCurrent()
      intended(hasComponent(AdministratorControlsActivity::class.java.name))
    }
  }

  @Test
  fun testAdminPinActivity_configChange_inputPin_imeAction_opensAdminControlsActivity() {
    launch<AdminPinActivity>(
      AdminPinActivity.createAdminPinActivityIntent(
        context = context,
        profileId = 0,
        colorRgb = -10710042,
        adminPinEnum = 0
      )
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      closeSoftKeyboard()
      onView(
        allOf(
          withId(R.id.admin_pin_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_pin_input_pin))
        )
      ).perform(
        editTextInputAction.appendText("12345"),
        closeSoftKeyboard()
      )
      onView(
        allOf(
          withId(R.id.admin_pin_input_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_pin_input_confirm_pin))
        )
      ).perform(
        nestedScrollTo(),
        editTextInputAction.appendText("12345"),
        pressImeActionButton()
      )
      testCoroutineDispatchers.runCurrent()
      intended(hasComponent(AdministratorControlsActivity::class.java.name))
    }
  }

  @Test
  fun testAdminPinActivity_configChange_inputShortPin_submit_clickIsDisabled() {
    launch<AdminPinActivity>(
      AdminPinActivity.createAdminPinActivityIntent(
        context = context,
        profileId = 0,
        colorRgb = -10710042,
        adminPinEnum = 0
      )
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(
        allOf(
          withId(R.id.admin_pin_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_pin_input_pin))
        )
      ).perform(
        editTextInputAction.appendText("123"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.submit_button)).perform(nestedScrollTo())
      onView(withId(R.id.submit_button)).check(matches(not(isClickable())))
    }
  }

  @Test
  fun testAdminPinActivity_configChange_inputShortPin_submit_inputPin_errorIsCleared() {
    launch<AdminPinActivity>(
      AdminPinActivity.createAdminPinActivityIntent(
        context = context,
        profileId = 0,
        colorRgb = -10710042,
        adminPinEnum = 0
      )
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(
        allOf(
          withId(R.id.admin_pin_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_pin_input_pin))
        )
      ).perform(
        editTextInputAction.appendText("123"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.submit_button)).perform(nestedScrollTo(), click())
      onView(
        allOf(
          withId(R.id.admin_pin_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_pin_input_pin))
        )
      ).perform(
        editTextInputAction.appendText("45"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.admin_pin_input_confirm_pin))
        .check(matches(hasNoErrorText()))
    }
  }

  @Test
  fun testAdminPinActivity_configChange_inputPinAndWrongConfirmPin_submit_confirmWrongError() {
    launch<AdminPinActivity>(
      AdminPinActivity.createAdminPinActivityIntent(
        context = context,
        profileId = 0,
        colorRgb = -10710042,
        adminPinEnum = 0
      )
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      closeSoftKeyboard()
      onView(
        allOf(
          withId(R.id.admin_pin_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_pin_input_pin))
        )
      ).perform(
        editTextInputAction.appendText("12345"),
        closeSoftKeyboard()
      )
      onView(
        allOf(
          withId(R.id.admin_pin_input_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_pin_input_confirm_pin))
        )
      ).perform(
        nestedScrollTo(),
        editTextInputAction.appendText("1234"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.submit_button)).perform(nestedScrollTo(), click())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.admin_pin_input_confirm_pin)).check(
        matches(
          hasErrorText(
            context.resources.getString(R.string.admin_pin_error_pin_confirm_wrong)
          )
        )
      )
    }
  }

  @Test
  fun testAdminPinActivity_configChange_inputPinAndWrongConfirmPin_confirmWrongError() {
    launch<AdminPinActivity>(
      AdminPinActivity.createAdminPinActivityIntent(
        context = context,
        profileId = 0,
        colorRgb = -10710042,
        adminPinEnum = 0
      )
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      closeSoftKeyboard()
      onView(
        allOf(
          withId(R.id.admin_pin_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_pin_input_pin))
        )
      ).perform(
        editTextInputAction.appendText("12345"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.advanceUntilIdle()
      onView(
        allOf(
          withId(R.id.admin_pin_input_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_pin_input_confirm_pin))
        )
      ).perform(
        nestedScrollTo(),
        editTextInputAction.appendText("1234"),
        pressImeActionButton()
      )
      testCoroutineDispatchers.advanceUntilIdle()
      onView(withId(R.id.admin_pin_input_confirm_pin)).check(
        matches(
          hasErrorText(
            context.resources.getString(R.string.admin_pin_error_pin_confirm_wrong)
          )
        )
      )
    }
  }

  @Test
  fun testAdminPinActivity_configChange_inputPinAndWrongConfirmPin_inputConfirmPinErrorIsCleared() {
    launch<AdminPinActivity>(
      AdminPinActivity.createAdminPinActivityIntent(
        context = context,
        profileId = 0,
        colorRgb = -10710042,
        adminPinEnum = 0
      )
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      closeSoftKeyboard()
      onView(
        allOf(
          withId(R.id.admin_pin_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_pin_input_pin))
        )
      ).perform(
        editTextInputAction.appendText("12345"),
        closeSoftKeyboard()
      )
      onView(
        allOf(
          withId(R.id.admin_pin_input_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_pin_input_confirm_pin))
        )
      ).perform(
        nestedScrollTo(),
        editTextInputAction.appendText("1234"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.submit_button)).perform(nestedScrollTo(), click())
      onView(
        allOf(
          withId(R.id.admin_pin_input_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_pin_input_confirm_pin))
        )
      ).perform(
        nestedScrollTo(),
        editTextInputAction.appendText("5"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.admin_pin_input_confirm_pin))
        .check(matches(hasNoErrorText()))
    }
  }

  @Test
  fun testAdminPinActivity_configChange_inputWrongConfirmPin_imeAction_correct_errorIsCleared() {
    launch<AdminPinActivity>(
      AdminPinActivity.createAdminPinActivityIntent(
        context = context,
        profileId = 0,
        colorRgb = -10710042,
        adminPinEnum = 0
      )
    ).use {
      onView(isRoot()).perform(orientationLandscape())
      closeSoftKeyboard()
      onView(
        allOf(
          withId(R.id.admin_pin_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_pin_input_pin))
        )
      ).perform(
        editTextInputAction.appendText("12345"),
        closeSoftKeyboard()
      )
      onView(
        allOf(
          withId(R.id.admin_pin_input_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_pin_input_confirm_pin))
        )
      ).perform(
        nestedScrollTo(),
        editTextInputAction.appendText("1234"),
        pressImeActionButton()
      )
      onView(
        allOf(
          withId(R.id.admin_pin_input_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_pin_input_confirm_pin))
        )
      ).perform(
        nestedScrollTo(),
        editTextInputAction.appendText("5"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.admin_pin_input_confirm_pin))
        .check(matches(hasNoErrorText()))
    }
  }

  @Test
  fun testAdminPinActivity_inputPinAndWrongConfirmPin_submit_configChange_confirmWrongError() {
    launch<AdminPinActivity>(
      AdminPinActivity.createAdminPinActivityIntent(
        context = context,
        profileId = 0,
        colorRgb = -10710042,
        adminPinEnum = 0
      )
    ).use {
      onView(
        allOf(
          withId(R.id.admin_pin_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_pin_input_pin))
        )
      ).perform(
        editTextInputAction.appendText("12345"),
        closeSoftKeyboard()
      )
      onView(
        allOf(
          withId(R.id.admin_pin_input_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_pin_input_confirm_pin))
        )
      ).perform(
        nestedScrollTo(),
        editTextInputAction.appendText("54321"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.submit_button)).perform(nestedScrollTo()).perform(click())
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.admin_pin_input_confirm_pin)).check(
        matches(
          hasErrorText(
            context.resources.getString(R.string.admin_pin_error_pin_confirm_wrong)
          )
        )
      )
    }
  }

  @Test
  fun testAdminPinActivity_inputPinAndWrongConfirmPin_imeAction_configChange_confirmWrongError() {
    launch<AdminPinActivity>(
      AdminPinActivity.createAdminPinActivityIntent(
        context = context,
        profileId = 0,
        colorRgb = -10710042,
        adminPinEnum = 0
      )
    ).use {
      onView(
        allOf(
          withId(R.id.admin_pin_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_pin_input_pin))
        )
      ).perform(
        editTextInputAction.appendText("12345"),
        closeSoftKeyboard()
      )
      onView(
        allOf(
          withId(R.id.admin_pin_input_confirm_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_pin_input_confirm_pin))
        )
      ).perform(
        nestedScrollTo(),
        editTextInputAction.appendText("54321"),
        pressImeActionButton()
      )
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.admin_pin_input_confirm_pin)).check(
        matches(
          hasErrorText(
            context.resources.getString(R.string.admin_pin_error_pin_confirm_wrong)
          )
        )
      )
    }
  }

  @Test
  fun testAdminPinActivity_inputShortPin_configChange_clickIsDisabled() {
    launch<AdminPinActivity>(
      AdminPinActivity.createAdminPinActivityIntent(
        context = context,
        profileId = 0,
        colorRgb = -10710042,
        adminPinEnum = 0
      )
    ).use {
      onView(
        allOf(
          withId(R.id.admin_pin_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_pin_input_pin))
        )
      ).perform(
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
  @Singleton
  @Component(
    modules = [
      RobolectricModule::class,
      PlatformParameterModule::class, PlatformParameterSingletonModule::class,
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
