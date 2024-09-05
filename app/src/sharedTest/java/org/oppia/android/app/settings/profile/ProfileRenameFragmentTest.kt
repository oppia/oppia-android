package org.oppia.android.app.settings.profile

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.pressImeActionButton
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.google.common.truth.Truth.assertThat
import dagger.Component
import org.hamcrest.CoreMatchers.not
import org.hamcrest.core.AllOf.allOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponent
import org.oppia.android.app.activity.ActivityComponentFactory
import org.oppia.android.app.activity.route.ActivityRouterModule
import org.oppia.android.app.application.ApplicationComponent
import org.oppia.android.app.application.ApplicationInjector
import org.oppia.android.app.application.ApplicationInjectorProvider
import org.oppia.android.app.application.ApplicationModule
import org.oppia.android.app.application.ApplicationStartupListenerModule
import org.oppia.android.app.application.testing.TestingBuildFlavorModule
import org.oppia.android.app.devoptions.DeveloperOptionsModule
import org.oppia.android.app.devoptions.DeveloperOptionsStarterModule
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
import org.oppia.android.util.logging.EventLoggingConfigurationModule
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
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId

/** Tests for [ProfileRenameFragment]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = ProfileRenameFragmentTest.TestApplication::class, qualifiers = "port-xxhdpi")
class ProfileRenameFragmentTest {
  @get:Rule
  val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @get:Rule
  val oppiaTestRule = OppiaTestRule()

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var profileTestHelper: ProfileTestHelper

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var editTextInputAction: EditTextInputAction

  @get:Rule
  val activityTestRule: ActivityTestRule<ProfileRenameActivity> =
    ActivityTestRule(
      ProfileRenameActivity::class.java,
      /* initialTouchMode= */ true,
      /* launchActivity= */ false
    )

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
  fun testProfileRenameFragment_inputNewName_clickSave_checkProfileEditActivityIsOpen() {
    ActivityScenario.launch<ProfileRenameActivity>(
      ProfileRenameActivity.createProfileRenameActivity(
        context = context,
        internalProfileId = 1
      )
    ).use {
      onView(
        allOf(
          withId(R.id.profile_rename_input_edit_text),
          isDescendantOfA(withId(R.id.profile_rename_input))
        )
      ).perform(editTextInputAction.appendText("James"))
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.profile_rename_save_button)).perform(click())
      testCoroutineDispatchers.runCurrent()
      intended(hasComponent(ProfileEditActivity::class.java.name))
    }
  }

  @Test
  fun testProfileRenameFragment_inputNewName_clickImeActionButton_checkProfileEditActivityIsOpen() {
    ActivityScenario.launch<ProfileRenameActivity>(
      ProfileRenameActivity.createProfileRenameActivity(
        context = context,
        internalProfileId = 1
      )
    ).use {
      onView(
        allOf(
          withId(R.id.profile_rename_input_edit_text),
          isDescendantOfA(withId(R.id.profile_rename_input))
        )
      ).perform(
        editTextInputAction.appendText("James"),
        pressImeActionButton()
      )
      testCoroutineDispatchers.runCurrent()
    }
    intended(hasComponent(ProfileEditActivity::class.java.name))
  }

  @Test
  fun testProfileRenameFragment_inputNewName_configurationChange_checkSaveIsEnabled() {
    ActivityScenario.launch<ProfileRenameActivity>(
      ProfileRenameActivity.createProfileRenameActivity(
        context = context,
        internalProfileId = 1
      )
    ).use {
      onView(
        allOf(
          withId(R.id.profile_rename_input_edit_text),
          isDescendantOfA(withId(R.id.profile_rename_input))
        )
      ).perform(editTextInputAction.appendText("James"))
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.profile_rename_save_button)).check(matches(isEnabled()))
    }
  }

  @Test
  fun testProfileRenameFragment_inputNewName_configurationChange_inputTextExists() {
    ActivityScenario.launch<ProfileRenameActivity>(
      ProfileRenameActivity.createProfileRenameActivity(
        context = context,
        internalProfileId = 1
      )
    ).use {
      onView(
        allOf(
          withId(R.id.profile_rename_input_edit_text),
          isDescendantOfA(withId(R.id.profile_rename_input))
        )
      ).perform(editTextInputAction.appendText("James"))
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.profile_rename_input_edit_text),
          isDescendantOfA(withId(R.id.profile_rename_input))
        )
      ).check(
        matches(
          withText("James")
        )
      )
    }
  }

  @Test
  fun testProfileRenameFragment_inputOldName_clickSave_checkNameNotUniqueError() {
    ActivityScenario.launch<ProfileRenameActivity>(
      ProfileRenameActivity.createProfileRenameActivity(
        context = context,
        internalProfileId = 1
      )
    ).use {
      onView(
        allOf(
          withId(R.id.profile_rename_input_edit_text),
          isDescendantOfA(withId(R.id.profile_rename_input))
        )
      ).perform(editTextInputAction.appendText("Admin"))
      onView(withId(R.id.profile_rename_save_button)).perform(click())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.profile_rename_input)).check(
        matches(
          hasErrorText(
            context.resources.getString(
              R.string.add_profile_error_name_not_unique
            )
          )
        )
      )
    }
  }

  @Test
  fun testProfileRenameFragment_inputOldName_clickSave_inputName_checkErrorIsCleared() {
    ActivityScenario.launch<ProfileRenameActivity>(
      ProfileRenameActivity.createProfileRenameActivity(
        context = context,
        internalProfileId = 1
      )
    ).use {
      onView(
        allOf(
          withId(R.id.profile_rename_input_edit_text),
          isDescendantOfA(withId(R.id.profile_rename_input))
        )
      ).perform(editTextInputAction.appendText("Admin"))
      onView(withId(R.id.profile_rename_save_button)).perform(click())
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.profile_rename_input_edit_text),
          isDescendantOfA(withId(R.id.profile_rename_input))
        )
      ).perform(editTextInputAction.appendText(" "))
      onView(withId(R.id.profile_rename_input)).check(matches(hasNoErrorText()))
    }
  }

  @Test
  fun testProfileRenameFragment_inputNameWithNumbers_clickCreate_checkNameOnlyLettersError() {
    ActivityScenario.launch<ProfileRenameActivity>(
      ProfileRenameActivity.createProfileRenameActivity(
        context = context,
        internalProfileId = 1
      )
    ).use {
      onView(
        allOf(
          withId(R.id.profile_rename_input_edit_text),
          isDescendantOfA(withId(R.id.profile_rename_input))
        )
      ).perform(editTextInputAction.appendText("123"))
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.profile_rename_save_button)).perform(click())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.profile_rename_input))
        .check(
          matches(
            hasErrorText(
              context.resources.getString(R.string.add_profile_error_name_only_letters)
            )
          )
        )
    }
  }

  @Test
  fun testProfileRenameFragment_inputNameWithNumbers_clickCreate_inputName_checkErrorIsCleared() {
    ActivityScenario.launch<ProfileRenameActivity>(
      ProfileRenameActivity.createProfileRenameActivity(
        context = context,
        internalProfileId = 1
      )
    ).use {
      onView(
        allOf(
          withId(R.id.profile_rename_input_edit_text),
          isDescendantOfA(withId(R.id.profile_rename_input))
        )
      ).perform(editTextInputAction.appendText("123"))
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.profile_rename_save_button)).perform(click())
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.profile_rename_input_edit_text),
          isDescendantOfA(withId(R.id.profile_rename_input))
        )
      ).perform(editTextInputAction.appendText(" "))
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.profile_rename_input)).check(matches(hasNoErrorText()))
    }
  }

  @Test
  fun testProfileRenameFragment_inputName_changeConfiguration_checkNameIsDisplayed() {
    ActivityScenario.launch<ProfileRenameActivity>(
      ProfileRenameActivity.createProfileRenameActivity(
        context = context,
        internalProfileId = 1
      )
    ).use {
      onView(
        allOf(
          withId(R.id.profile_rename_input_edit_text),
          isDescendantOfA(withId(R.id.profile_rename_input))
        )
      ).perform(
        editTextInputAction.appendText("test"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      onView(
        allOf(
          withId(R.id.profile_rename_input_edit_text),
          isDescendantOfA(withId(R.id.profile_rename_input))
        )
      ).check(
        matches(
          withText("test")
        )
      )
    }
  }

  @Test
  fun testProfileRenameFragment_inputOldName_clickSave_changeConfiguration_errorIsVisible() {
    ActivityScenario.launch<ProfileRenameActivity>(
      ProfileRenameActivity.createProfileRenameActivity(
        context = context,
        internalProfileId = 1
      )
    ).use {
      onView(
        allOf(
          withId(R.id.profile_rename_input_edit_text),
          isDescendantOfA(withId(R.id.profile_rename_input))
        )
      ).perform(
        editTextInputAction.appendText("Admin"),
        closeSoftKeyboard()
      )
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.profile_rename_save_button)).perform(click())
      testCoroutineDispatchers.runCurrent()
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.profile_rename_input))
        .check(
          matches(
            hasErrorText(
              context.resources.getString(R.string.add_profile_error_name_not_unique)
            )
          )
        )
    }
  }

  @Test
  fun testProfileRenameFragment_clickSave_changeConfiguration_saveButtonIsNotClickable() {
    ActivityScenario.launch<ProfileRenameActivity>(
      ProfileRenameActivity.createProfileRenameActivity(
        context = context,
        internalProfileId = 1
      )
    ).use {
      onView(withId(R.id.profile_rename_save_button)).check(matches(not(isClickable())))
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.profile_rename_save_button)).check(matches(not(isClickable())))
    }
  }

  @Test
  fun testFragment_fragmentLoaded_verifyCorrectArgumentsPassed() {
    ActivityScenario.launch<ProfileRenameActivity>(
      ProfileRenameActivity.createProfileRenameActivity(
        context = context,
        internalProfileId = 1
      )
    ).use { scenario ->
      testCoroutineDispatchers.runCurrent()
      scenario.onActivity { activity ->

        val profileRenameFragment = activity.supportFragmentManager
          .findFragmentById(R.id.profile_rename_fragment_placeholder) as ProfileRenameFragment
        val args =
          checkNotNull(profileRenameFragment.arguments) {
            "Expected arguments to be passed to ProfileRenameFragment"
          }
        val receivedProfileId = args.extractCurrentUserProfileId().internalId

        assertThat(receivedProfileId).isEqualTo(1)
      }
    }
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
      EventLoggingConfigurationModule::class, ActivityRouterModule::class,
      CpuPerformanceSnapshotterModule::class, ExplorationProgressModule::class,
      TestAuthenticationModule::class
    ]
  )
  interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder {
      override fun build(): TestApplicationComponent
    }

    fun inject(profileRenameFragmentTest: ProfileRenameFragmentTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerProfileRenameFragmentTest_TestApplicationComponent.builder().setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(profileRenameFragmentTest: ProfileRenameFragmentTest) {
      component.inject(profileRenameFragmentTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
