package org.oppia.android.app.profile

import android.app.Application
import android.content.Context
import android.view.View
import androidx.annotation.StringRes
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
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.textfield.TextInputLayout
import dagger.Component
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.junit.After
import org.junit.Before
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
import org.oppia.android.testing.RobolectricModule
import org.oppia.android.testing.TestAccessibilityModule
import org.oppia.android.testing.TestCoroutineDispatchers
import org.oppia.android.testing.TestDispatcherModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.time.FakeOppiaClockModule
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
  application = AdminAuthActivityTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class AdminAuthActivityTest {

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var editTextInputAction: EditTextInputAction

  private val internalProfileId: Int = 0

  @Before
  fun setUp() {
    Intents.init()
    setUpTestApplicationComponent()
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
  fun testAdminAuthActivity_inputCorrectPassword_opensAddProfileActivity() {
    launch<AdminAuthActivity>(
      AdminAuthActivity.createAdminAuthActivityIntent(
        context = context,
        adminPin = "12345",
        profileId = internalProfileId,
        colorRgb = -10710042,
        adminPinEnum = AdminAuthEnum.PROFILE_ADD_PROFILE.value
      )
    ).use {
      onView(
        allOf(
          withId(R.id.admin_auth_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_auth_input_pin))
        )
      ).perform(
        editTextInputAction.appendText("12345"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.admin_auth_submit_button)).perform(click())
      intended(hasComponent(AddProfileActivity::class.java.name))
    }
  }

  @Test
  fun testAdminAuthActivity_inputCorrectPassword_imeAction_opensAddProfileActivity() {
    launch<AdminAuthActivity>(
      AdminAuthActivity.createAdminAuthActivityIntent(
        context = context,
        adminPin = "12345",
        profileId = internalProfileId,
        colorRgb = -10710042,
        adminPinEnum = AdminAuthEnum.PROFILE_ADD_PROFILE.value
      )
    ).use {
      onView(
        allOf(
          withId(R.id.admin_auth_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_auth_input_pin))
        )
      ).perform(
        editTextInputAction.appendText("12345"),
        pressImeActionButton()
      )
      intended(hasComponent(AddProfileActivity::class.java.name))
    }
  }

  @Test
  fun testAdminAuthActivity_inputCorrectPassword_opensAddAdminControlsActivity() {
    launch<AdminAuthActivity>(
      AdminAuthActivity.createAdminAuthActivityIntent(
        context = context,
        adminPin = "12345",
        profileId = internalProfileId,
        colorRgb = -10710042,
        adminPinEnum = AdminAuthEnum.PROFILE_ADMIN_CONTROLS.value
      )
    ).use {
      onView(
        allOf(
          withId(R.id.admin_auth_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_auth_input_pin))
        )
      ).perform(
        editTextInputAction.appendText("12345"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.admin_auth_submit_button)).perform(click())
      intended(hasComponent(AdministratorControlsActivity::class.java.name))
    }
  }

  @Test
  fun testAdminAuthActivity_inputCorrectPassword_imeAction_opensAdminControlsActivity() {
    launch<AdminAuthActivity>(
      AdminAuthActivity.createAdminAuthActivityIntent(
        context = context,
        adminPin = "12345",
        profileId = internalProfileId,
        colorRgb = -10710042,
        adminPinEnum = AdminAuthEnum.PROFILE_ADMIN_CONTROLS.value
      )
    ).use {
      onView(
        allOf(
          withId(R.id.admin_auth_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_auth_input_pin))
        )
      ).perform(
        editTextInputAction.appendText("12345"),
        pressImeActionButton()
      )
      intended(hasComponent(AdministratorControlsActivity::class.java.name))
    }
  }

  @Test
  fun testAdminAuthActivity_inputIncorrectPassword_checkError() {
    launch<AdminAuthActivity>(
      AdminAuthActivity.createAdminAuthActivityIntent(
        context = context,
        adminPin = "12345",
        profileId = internalProfileId,
        colorRgb = -10710042,
        adminPinEnum = AdminAuthEnum.PROFILE_ADMIN_CONTROLS.value
      )
    ).use {
      onView(
        allOf(
          withId(R.id.admin_auth_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_auth_input_pin))
        )
      ).perform(
        editTextInputAction.appendText("12354"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.admin_auth_submit_button)).perform(click())
      onView(withId(R.id.admin_auth_input_pin))
        .check(matches(hasErrorText(R.string.admin_auth_incorrect)))
    }
  }

  @Test
  fun testAdminAuthActivity_inputIncorrectPassword_imeAction_errorIsDisplayed() {
    launch<AdminAuthActivity>(
      AdminAuthActivity.createAdminAuthActivityIntent(
        context = context,
        adminPin = "12345",
        profileId = internalProfileId,
        colorRgb = -10710042,
        adminPinEnum = AdminAuthEnum.PROFILE_ADMIN_CONTROLS.value
      )
    ).use {
      onView(
        allOf(
          withId(R.id.admin_auth_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_auth_input_pin))
        )
      ).perform(
        editTextInputAction.appendText("12354"),
        pressImeActionButton()
      )
      onView(withId(R.id.admin_auth_input_pin))
        .check(matches(hasErrorText(R.string.admin_auth_incorrect)))
    }
  }

  @Test
  fun testAdminAuthActivity_inputIncorrectPassword_inputAgain_errorIsGone() {
    launch<AdminAuthActivity>(
      AdminAuthActivity.createAdminAuthActivityIntent(
        context = context,
        adminPin = "12345",
        profileId = internalProfileId,
        colorRgb = -10710042,
        adminPinEnum = AdminAuthEnum.PROFILE_ADMIN_CONTROLS.value
      )
    ).use {
      onView(
        allOf(
          withId(R.id.admin_auth_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_auth_input_pin))
        )
      ).perform(
        editTextInputAction.appendText("123"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.admin_auth_submit_button)).perform(click())
      onView(
        allOf(
          withId(R.id.admin_auth_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_auth_input_pin))
        )
      ).perform(
        editTextInputAction.appendText("4"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.admin_auth_input_pin))
        .check(matches(hasNoErrorText()))
    }
  }

  @Test
  fun testAdminAuthActivity_inputIncorrectPassword_correct_imeAction_errorIsGone() {
    launch<AdminAuthActivity>(
      AdminAuthActivity.createAdminAuthActivityIntent(
        context = context,
        adminPin = "12345",
        profileId = internalProfileId,
        colorRgb = -10710042,
        adminPinEnum = AdminAuthEnum.PROFILE_ADMIN_CONTROLS.value
      )
    ).use {
      onView(
        allOf(
          withId(R.id.admin_auth_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_auth_input_pin))
        )
      ).perform(
        editTextInputAction.appendText("123"),
        pressImeActionButton()
      )
      onView(
        allOf(
          withId(R.id.admin_auth_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_auth_input_pin))
        )
      ).perform(
        editTextInputAction.appendText("4"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.admin_auth_input_pin))
        .check(matches(hasNoErrorText()))
    }
  }

  @Test
  fun testAdminAuthActivity_buttonState_configChange_buttonStateIsPreserved() {
    launch<AdminAuthActivity>(
      AdminAuthActivity.createAdminAuthActivityIntent(
        context = context,
        adminPin = "12345",
        profileId = internalProfileId,
        colorRgb = -10710042,
        adminPinEnum = AdminAuthEnum.PROFILE_ADMIN_CONTROLS.value
      )
    ).use {
      onView(
        allOf(
          withId(R.id.admin_auth_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_auth_input_pin))
        )
      ).perform(
        editTextInputAction.appendText("12345"),
        closeSoftKeyboard()
      )
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.admin_auth_submit_button)).check(matches(isEnabled()))
    }
  }

  @Test
  fun testAdminAuthActivity_fromAdminControls_configChange_headingSubHeadingIsPreserved() {
    launch<AdminAuthActivity>(
      AdminAuthActivity.createAdminAuthActivityIntent(
        context = context,
        adminPin = "12345",
        profileId = internalProfileId,
        colorRgb = -10710042,
        adminPinEnum = AdminAuthEnum.PROFILE_ADMIN_CONTROLS.value
      )
    ).use {
      onView(withId(R.id.admin_auth_heading_textview)).check(
        matches(
          withText(
            context.resources.getString(
              R.string.admin_auth_heading
            )
          )
        )
      )
      onView(withId(R.id.admin_auth_sub_text))
        .check(
          matches(
            withText(
              context.resources.getString(
                R.string.admin_auth_admin_controls_sub
              )
            )
          )
        )
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.admin_auth_sub_text))
        .check(
          matches(
            withText(
              context.resources.getString(R.string.admin_auth_admin_controls_sub)
            )
          )
        )
      onView(withId(R.id.admin_auth_heading_textview)).check(
        matches(
          withText(
            context.resources.getString(
              R.string.admin_auth_heading
            )
          )
        )
      )
    }
  }

  @Test
  fun testAdminAuthActivity_fromProfile_configChange_headingSubHeadingIsPreserved() {
    launch<AdminAuthActivity>(
      AdminAuthActivity.createAdminAuthActivityIntent(
        context = context,
        adminPin = "12345",
        profileId = internalProfileId,
        colorRgb = -10710042,
        adminPinEnum = AdminAuthEnum.PROFILE_ADD_PROFILE.value
      )
    ).use {
      onView(withId(R.id.admin_auth_heading_textview)).check(
        matches(
          withText(
            context.resources.getString(
              R.string.admin_auth_heading
            )
          )
        )
      )
      onView(withId(R.id.admin_auth_sub_text))
        .check(
          matches(
            withText(
              context.resources.getString(
                R.string.admin_auth_sub
              )
            )
          )
        )
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.admin_auth_sub_text))
        .check(
          matches(
            withText(
              context.resources.getString(
                R.string.admin_auth_sub
              )
            )
          )
        )
      onView(withId(R.id.admin_auth_heading_textview)).check(
        matches(
          withText(
            context.resources.getString(
              R.string.admin_auth_heading
            )
          )
        )
      )
    }
  }

  @Test
  fun testAdminAuthActivity_inputText_configChange_inputTextIsPreserved() {
    launch<AdminAuthActivity>(
      AdminAuthActivity.createAdminAuthActivityIntent(
        context = context,
        adminPin = "12345",
        profileId = internalProfileId,
        colorRgb = -10710042,
        adminPinEnum = AdminAuthEnum.PROFILE_ADMIN_CONTROLS.value
      )
    ).use {
      onView(
        allOf(
          withId(R.id.admin_auth_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_auth_input_pin))
        )
      ).perform(
        editTextInputAction.appendText("12345"),
        closeSoftKeyboard()
      )
      onView(isRoot()).perform(orientationLandscape())
      onView(
        allOf(
          withId(R.id.admin_auth_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_auth_input_pin))
        )
      ).check(
        matches(
          withText("12345")
        )
      )
    }
  }

  @Test
  fun testAdminAuthActivity_inputIncorrectPasswordLandscape_checkError() {
    launch<AdminAuthActivity>(
      AdminAuthActivity.createAdminAuthActivityIntent(
        context = context,
        adminPin = "12345",
        profileId = internalProfileId,
        colorRgb = -10710042,
        adminPinEnum = AdminAuthEnum.PROFILE_ADMIN_CONTROLS.value
      )
    ).use {
      onView(
        allOf(
          withId(R.id.admin_auth_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_auth_input_pin))
        )
      ).perform(
        editTextInputAction.appendText("12354"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.admin_auth_submit_button)).perform(click())
      onView(withId(R.id.admin_auth_input_pin))
        .check(matches(hasErrorText(R.string.admin_auth_incorrect)))
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.admin_auth_input_pin))
        .check(matches(hasErrorText(R.string.admin_auth_incorrect)))
    }
  }

  @Test
  fun testAdminAuthActivity_inputIncorrectPassword_imeAction_configChange_errorIsDisplayed() {
    launch<AdminAuthActivity>(
      AdminAuthActivity.createAdminAuthActivityIntent(
        context = context,
        adminPin = "12345",
        profileId = internalProfileId,
        colorRgb = -10710042,
        adminPinEnum = AdminAuthEnum.PROFILE_ADMIN_CONTROLS.value
      )
    ).use {
      onView(
        allOf(
          withId(R.id.admin_auth_input_pin_edit_text),
          isDescendantOfA(withId(R.id.admin_auth_input_pin))
        )
      ).perform(
        editTextInputAction.appendText("12354"),
        pressImeActionButton()
      )
      onView(withId(R.id.admin_auth_input_pin))
        .check(matches(hasErrorText(R.string.admin_auth_incorrect)))
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.admin_auth_input_pin))
        .check(matches(hasErrorText(R.string.admin_auth_incorrect)))
    }
  }

  private fun hasErrorText(@StringRes expectedErrorTextId: Int): Matcher<View> {
    return object : TypeSafeMatcher<View>() {
      override fun matchesSafely(view: View): Boolean {
        val expectedErrorText = context.resources.getString(expectedErrorTextId)
        return (view as TextInputLayout).error == expectedErrorText
      }

      override fun describeTo(description: Description) {
        description.appendText("TextInputLayout's error")
      }
    }
  }

  private fun hasNoErrorText(): Matcher<View> {
    return object : TypeSafeMatcher<View>() {
      override fun matchesSafely(view: View): Boolean {
        return (view as TextInputLayout).error.isNullOrEmpty()
      }

      override fun describeTo(description: Description) {
        description.appendText("")
      }
    }
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  // TODO(#1675): Add NetworkModule once data module is migrated off of Moshi.
  @Singleton
  @Component(
    modules = [
      RobolectricModule::class, TestDispatcherModule::class, ApplicationModule::class,
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
      FirebaseLogUploaderModule::class, FakeOppiaClockModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

    fun inject(adminAuthActivityTest: AdminAuthActivityTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerAdminAuthActivityTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(adminAuthActivityTest: AdminAuthActivityTest) {
      component.inject(adminAuthActivityTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
