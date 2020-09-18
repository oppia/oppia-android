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
import androidx.test.espresso.action.ViewActions.typeText
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
import dagger.Component
import org.hamcrest.Matchers.allOf
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
  application = AdminAuthActivityTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class AdminAuthActivityTest {

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var testingUtils: TestingUtils

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
        context,
        "12345",
        internalProfileId,
        -10710042,
        AdminAuthEnum.PROFILE_ADD_PROFILE.value
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.admin_auth_input_pin)))).perform(
        testingUtils.appendText("12345"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.admin_auth_submit_button)).perform(click())
      testCoroutineDispatchers.runCurrent()
      intended(hasComponent(AddProfileActivity::class.java.name))
    }
  }

  // TODO(#962): Reenable once IME_ACTIONS work correctly on ProfileInputView.
  @Ignore("IME_ACTIONS doesn't work properly in ProfileInputView")
  @Test
  fun testAdminAuthActivity_inputCorrectPassword_clickImeActionButton_opensAddProfileActivity() {
    launch<AdminAuthActivity>(
      AdminAuthActivity.createAdminAuthActivityIntent(
        context,
        "12345",
        internalProfileId,
        -10710042,
        AdminAuthEnum.PROFILE_ADD_PROFILE.value
      )
    ).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.admin_auth_input_pin)))).perform(
        typeText("12345"),
        pressImeActionButton()
      )
      intended(hasComponent(AddProfileActivity::class.java.name))
    }
  }

  @Test
  fun testAdminAuthActivity_inputCorrectPassword_opensAddAdministratorControlsActivity() {
    launch<AdminAuthActivity>(
      AdminAuthActivity.createAdminAuthActivityIntent(
        context,
        "12345",
        internalProfileId,
        -10710042,
        AdminAuthEnum.PROFILE_ADMIN_CONTROLS.value
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.admin_auth_input_pin)))).perform(
        testingUtils.appendText("12345"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.admin_auth_submit_button)).perform(click())
      testCoroutineDispatchers.runCurrent()
      intended(hasComponent(AdministratorControlsActivity::class.java.name))
    }
  }

  // TODO(#962): Reenable once IME_ACTIONS work correctly on ProfileInputView.
  @Ignore("IME_ACTIONS doesn't work properly in ProfileInputView")
  @Test
  fun testAdminAuthActivity_inputCorrectPassword_clickImeActionButton_opensAddAdministratorControlsActivity() { // ktlint-disable max-line-length
    launch<AdminAuthActivity>(
      AdminAuthActivity.createAdminAuthActivityIntent(
        context,
        "12345",
        internalProfileId,
        -10710042,
        AdminAuthEnum.PROFILE_ADMIN_CONTROLS.value
      )
    ).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.admin_auth_input_pin)))).perform(
        typeText("12345"),
        pressImeActionButton()
      )
      intended(hasComponent(AdministratorControlsActivity::class.java.name))
    }
  }

  @Test
  fun testAdminAuthActivity_inputIncorrectPassword_checkError() {
    launch<AdminAuthActivity>(
      AdminAuthActivity.createAdminAuthActivityIntent(
        context,
        "12345",
        internalProfileId,
        -10710042,
        AdminAuthEnum.PROFILE_ADMIN_CONTROLS.value
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.admin_auth_input_pin)))).perform(
        testingUtils.appendText("12354"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.admin_auth_submit_button)).perform(click())
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.admin_auth_input_pin))
        )
      ).check(matches(withText(context.resources.getString(R.string.admin_auth_incorrect))))
    }
  }

  // TODO(#962): Reenable once IME_ACTIONS work correctly on ProfileInputView.
  @Ignore("IME_ACTIONS doesn't work properly in ProfileInputView")
  @Test
  fun testAdminAuthActivity_inputIncorrectPassword_clickImeActionButton_checkError() {
    launch<AdminAuthActivity>(
      AdminAuthActivity.createAdminAuthActivityIntent(
        context,
        "12345",
        internalProfileId,
        -10710042,
        AdminAuthEnum.PROFILE_ADMIN_CONTROLS.value
      )
    ).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.admin_auth_input_pin)))).perform(
        typeText("12354"),
        pressImeActionButton()
      )
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.admin_auth_input_pin))
        )
      ).check(matches(withText(context.resources.getString(R.string.admin_auth_incorrect))))
    }
  }

  @Test
  fun testAdminAuthActivity_inputIncorrectPassword_inputAgain_checkErrorIsGone() {
    launch<AdminAuthActivity>(
      AdminAuthActivity.createAdminAuthActivityIntent(
        context,
        "12345",
        internalProfileId,
        -10710042,
        AdminAuthEnum.PROFILE_ADMIN_CONTROLS.value
      )
    ).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.admin_auth_input_pin)))).perform(
        typeText("123"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.admin_auth_submit_button)).perform(click())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.admin_auth_input_pin)))).perform(
        typeText("4"),
        closeSoftKeyboard()
      )
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.admin_auth_input_pin))
        )
      ).check(matches(withText("")))
    }
  }

  // TODO(#962): Reenable once IME_ACTIONS work correctly on ProfileInputView.
  @Ignore("IME_ACTIONS doesn't work properly in ProfileInputView")
  @Test
  fun testAdminAuthActivity_inputIncorrectPassword_inputAgain_clickImeActionButton_checkErrorIsGone() { // ktlint-disable max-line-length
    launch<AdminAuthActivity>(
      AdminAuthActivity.createAdminAuthActivityIntent(
        context,
        "12345",
        internalProfileId,
        -10710042,
        AdminAuthEnum.PROFILE_ADMIN_CONTROLS.value
      )
    ).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.admin_auth_input_pin)))).perform(
        typeText("123"),
        pressImeActionButton()
      )
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.admin_auth_input_pin)))).perform(
        typeText("4"),
        closeSoftKeyboard()
      )
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.admin_auth_input_pin))
        )
      ).check(matches(withText("")))
    }
  }

  @Test
  fun testAdminAuthActivity_buttonState_configurationChanged_buttonStateIsPreserved() {
    launch<AdminAuthActivity>(
      AdminAuthActivity.createAdminAuthActivityIntent(
        context,
        "12345",
        internalProfileId,
        -10710042,
        AdminAuthEnum.PROFILE_ADMIN_CONTROLS.value
      )
    ).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.admin_auth_input_pin)))).perform(
        typeText("12345"),
        closeSoftKeyboard()
      )
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.admin_auth_submit_button)).check(matches(isEnabled()))
    }
  }

  @Test
  fun testAdminAuthActivity_openedFromAdminControls_configurationChanged_checkHeadingSubHeadingIsPreserved() { // ktlint-disable max-line-length
    launch<AdminAuthActivity>(
      AdminAuthActivity.createAdminAuthActivityIntent(
        context,
        "12345",
        internalProfileId,
        -10710042,
        AdminAuthEnum.PROFILE_ADMIN_CONTROLS.value
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
  fun testAdminAuthActivity_openedFromProfile_configurationChanged_checkHeadingSubHeadingIsPreserved() { // ktlint-disable max-line-length
    launch<AdminAuthActivity>(
      AdminAuthActivity.createAdminAuthActivityIntent(
        context,
        "12345",
        internalProfileId,
        -10710042,
        AdminAuthEnum.PROFILE_ADD_PROFILE.value
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
  fun testAdminAuthActivity_inputText_configurationChanged_inputTextIsPreserved() {
    launch<AdminAuthActivity>(
      AdminAuthActivity.createAdminAuthActivityIntent(
        context,
        "12345",
        internalProfileId,
        -10710042,
        AdminAuthEnum.PROFILE_ADMIN_CONTROLS.value
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.admin_auth_input_pin)))).perform(
        testingUtils.appendText("12345"),
        closeSoftKeyboard()
      )
      onView(isRoot()).perform(orientationLandscape())
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.admin_auth_input_pin)))).check(
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
        context,
        "12345",
        internalProfileId,
        -10710042,
        AdminAuthEnum.PROFILE_ADMIN_CONTROLS.value
      )
    ).use {
      testCoroutineDispatchers.runCurrent()
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.admin_auth_input_pin)))).perform(
        testingUtils.appendText("12354"),
        closeSoftKeyboard()
      )
      onView(withId(R.id.admin_auth_submit_button)).perform(click())
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.admin_auth_input_pin))
        )
      ).check(matches(withText(context.resources.getString(R.string.admin_auth_incorrect))))
      onView(isRoot()).perform(orientationLandscape())
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.admin_auth_input_pin))
        )
      ).check(matches(withText(context.resources.getString(R.string.admin_auth_incorrect))))
    }
  }

  // TODO(#962): Reenable once IME_ACTIONS work correctly on ProfileInputView.
  @Ignore("IME_ACTIONS doesn't work properly in ProfileInputView")
  @Test
  fun testAdminAuthActivity_inputIncorrectPasswordLandscape_clickImeActionButton_checkError() {
    launch<AdminAuthActivity>(
      AdminAuthActivity.createAdminAuthActivityIntent(
        context,
        "12345",
        internalProfileId,
        -10710042,
        AdminAuthEnum.PROFILE_ADMIN_CONTROLS.value
      )
    ).use {
      onView(allOf(withId(R.id.input), isDescendantOfA(withId(R.id.admin_auth_input_pin)))).perform(
        typeText("12354"),
        pressImeActionButton()
      )
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.admin_auth_input_pin))
        )
      ).check(matches(withText(context.resources.getString(R.string.admin_auth_incorrect))))
      onView(isRoot()).perform(orientationLandscape())
      onView(
        allOf(
          withId(R.id.error_text),
          isDescendantOfA(withId(R.id.admin_auth_input_pin))
        )
      ).check(matches(withText(context.resources.getString(R.string.admin_auth_incorrect))))
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
