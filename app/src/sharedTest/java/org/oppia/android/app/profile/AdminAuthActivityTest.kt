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
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.google.common.truth.Truth.assertThat
import dagger.Component
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
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
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
  application = AdminAuthActivityTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class AdminAuthActivityTest {
  @get:Rule
  val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @get:Rule
  val oppiaTestRule = OppiaTestRule()

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var editTextInputAction: EditTextInputAction

  private val internalProfileId: Int = 0

  @get:Rule
  val activityTestRule: ActivityTestRule<AdminAuthActivity> = ActivityTestRule(
    AdminAuthActivity::class.java, /* initialTouchMode= */ true, /* launchActivity= */ false
  )

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
  fun testActivity_createIntent_verifyScreenNameInIntent() {
    val screenName = AdminAuthActivity.createAdminAuthActivityIntent(
      context = context,
      adminPin = "12345",
      profileId = internalProfileId,
      colorRgb = -10710042,
      adminPinEnum = AdminAuthEnum.PROFILE_ADD_PROFILE.value
    ).extractCurrentAppScreenName()

    assertThat(screenName).isEqualTo(ScreenName.ADMIN_AUTH_ACTIVITY)
  }

  @Test
  fun testAdminAuthActivity_closeButton_checkContentDescription() {
    launch<AdminAuthActivity>(
      AdminAuthActivity.createAdminAuthActivityIntent(
        context = context,
        adminPin = "12345",
        profileId = internalProfileId,
        colorRgb = -10710042,
        adminPinEnum = AdminAuthEnum.PROFILE_ADD_PROFILE.value
      )
    ).use {
      onView(withContentDescription(R.string.admin_auth_close)).check(matches(isDisplayed()))
    }
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
        .check(
          matches(
            hasErrorText(
              context.resources.getString(R.string.admin_auth_incorrect)
            )
          )
        )
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
        .check(
          matches(
            hasErrorText(
              context.resources.getString(R.string.admin_auth_incorrect)
            )
          )
        )
    }
  }

  @Test
  fun testAdminAuthActivity_defaultButtonState_isDisabled() {
    launch<AdminAuthActivity>(
      AdminAuthActivity.createAdminAuthActivityIntent(
        context = context,
        adminPin = "12345",
        profileId = internalProfileId,
        colorRgb = -10710042,
        adminPinEnum = AdminAuthEnum.PROFILE_ADMIN_CONTROLS.value
      )
    ).use {
      onView(withId(R.id.admin_auth_submit_button)).check(matches(not(isEnabled())))
    }
  }

  @Test
  fun testAdminAuthActivity_inputPin_buttonStateIsEnabled() {
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
      onView(withId(R.id.admin_auth_submit_button)).check(matches(isEnabled()))
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
        .check(
          matches(
            hasErrorText(
              context.resources.getString(R.string.admin_auth_incorrect)
            )
          )
        )
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.admin_auth_input_pin))
        .check(
          matches(
            hasErrorText(
              context.resources.getString(R.string.admin_auth_incorrect)
            )
          )
        )
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
        .check(
          matches(
            hasErrorText(
              context.resources.getString(R.string.admin_auth_incorrect)
            )
          )
        )
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.admin_auth_input_pin))
        .check(
          matches(
            hasErrorText(
              context.resources.getString(R.string.admin_auth_incorrect)
            )
          )
        )
    }
  }

  @Test
  fun testAdminAuthActivity_forProfileAdminControls_hasAuthorizeAccessControlsTitle() {
    activityTestRule.launchActivity(
      AdminAuthActivity.createAdminAuthActivityIntent(
        context = context,
        adminPin = "12345",
        profileId = internalProfileId,
        colorRgb = -10710042,
        adminPinEnum = AdminAuthEnum.PROFILE_ADMIN_CONTROLS.value
      )
    )
    val title = activityTestRule.activity.title

    // Verify that the activity label is correct as a proxy to verify TalkBack will announce the
    // correct string when it's read out.
    assertThat(title).isEqualTo(
      context.getString(R.string.admin_auth_activity_access_controls_title)
    )
  }

  @Test
  fun testAdminAuthActivity_forAddProfile_hasAuthorizeAddProfileTitle() {
    activityTestRule.launchActivity(
      AdminAuthActivity.createAdminAuthActivityIntent(
        context = context,
        adminPin = "12345",
        profileId = internalProfileId,
        colorRgb = -10710042,
        adminPinEnum = AdminAuthEnum.PROFILE_ADD_PROFILE.value
      )
    )
    val title = activityTestRule.activity.title

    // Verify that the activity label is correct as a proxy to verify TalkBack will announce the
    // correct string when it's read out.
    assertThat(title).isEqualTo(context.getString(R.string.admin_auth_activity_add_profiles_title))
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @Singleton
  @Component(
    modules = [
      RobolectricModule::class, PlatformParameterModule::class, TestDispatcherModule::class,
      ApplicationModule::class, LoggerModule::class, ContinueModule::class,
      FractionInputModule::class, ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
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
      PlatformParameterSingletonModule::class,
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
