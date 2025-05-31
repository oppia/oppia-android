package org.oppia.android.app.onboarding

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtraWithKey
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.Component
import org.hamcrest.CoreMatchers.allOf
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
import org.oppia.android.app.devoptions.DeveloperOptionsModule
import org.oppia.android.app.devoptions.DeveloperOptionsStarterModule
import org.oppia.android.app.model.CreateProfileActivityParams
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ProfileType
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.profile.ProfileChooserActivity
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.test.R
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
import org.oppia.android.app.utility.EspressoTestsMatchers.hasProtoExtra
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
import org.oppia.android.domain.platformparameter.testing.PlatformParameterInitializationInjector
import org.oppia.android.domain.platformparameter.testing.PlatformParameterInitializationInjectorProvider
import org.oppia.android.domain.platformparameter.testing.PlatformParameterTestModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.FakeAnalyticsEventLogger
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.logging.EventLogSubject
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
import org.oppia.android.util.locale.OppiaLocale
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.networking.NetworkConnectionDebugUtilModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.parser.html.HtmlParserEntityTypeModule
import org.oppia.android.util.parser.image.GlideImageLoaderModule
import org.oppia.android.util.parser.image.ImageParsingModule
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.PROFILE_ID_INTENT_DECORATOR
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [OnboardingProfileTypeFragment]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = OnboardingProfileTypeFragmentTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class OnboardingProfileTypeFragmentTest {
  @get:Rule
  val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @get:Rule
  val oppiaTestRule = OppiaTestRule()

  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject lateinit var context: Context
  @Inject lateinit var machineLocale: OppiaLocale.MachineLocale
  @Inject lateinit var profileTestHelper: ProfileTestHelper
  @Inject lateinit var fakeAnalyticsEventLogger: FakeAnalyticsEventLogger

  private val testProfileId = ProfileId.newBuilder().setInternalId(0).build()

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

  @Test
  fun testFragment_portraitMode_headerTextIsDisplayed() {
    launchOnboardingProfileTypeActivity().use {
      onView(withId(R.id.profile_type_title))
        .check(
          matches(
            allOf(
              isDisplayed(),
              withText(
                R.string.onboarding_profile_type_activity_header
              )
            )
          )
        )
    }
  }

  @Test
  fun testFragment_landscapeMode_headerTextIsDisplayed() {
    launchOnboardingProfileTypeActivity().use {
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.profile_type_title))
        .check(
          matches(
            allOf(
              isDisplayed(),
              withText(
                R.string.onboarding_profile_type_activity_header
              )
            )
          )
        )
    }
  }

  @Test
  fun testFragment_portraitMode_navigationCardsAreDisplayed() {
    launchOnboardingProfileTypeActivity().use {
      onView(withId(R.id.profile_type_learner_navigation_card))
        .check(
          matches(
            allOf(
              isDisplayed(),
              hasDescendant(
                withText(R.string.onboarding_profile_type_activity_student_text)
              )
            )
          )
        )

      onView(withId(R.id.profile_type_supervisor_navigation_card))
        .check(
          matches(
            allOf(
              isDisplayed(),
              hasDescendant(
                withText(R.string.onboarding_profile_type_activity_parent_text)
              )
            )
          )
        )
    }
  }

  @Test
  fun testFragment_landscapeMode_navigationCardsAreDisplayed() {
    launchOnboardingProfileTypeActivity().use {
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.profile_type_learner_navigation_card))
        .check(
          matches(
            allOf(
              isDisplayed(),
              hasDescendant(
                withText(R.string.onboarding_profile_type_activity_student_text)
              )
            )
          )
        )

      onView(withId(R.id.profile_type_supervisor_navigation_card))
        .check(
          matches(
            allOf(
              isDisplayed(),
              hasDescendant(
                withText(R.string.onboarding_profile_type_activity_parent_text)
              )
            )
          )
        )
    }
  }

  @Test
  fun testFragment_portrait_stepCountTextIsDisplayed() {
    launchOnboardingProfileTypeActivity().use {
      onView(withId(R.id.onboarding_steps_count))
        .check(
          matches(
            allOf(
              isDisplayed(),
              withText(
                R.string.onboarding_step_count_two
              )
            )
          )
        )
    }
  }

  @Test
  fun testFragment_studentNavigationCardClicked_launchesCreateProfileScreen() {
    launchOnboardingProfileTypeActivity().use {
      onView(withId(R.id.profile_type_learner_navigation_card)).perform(click())
      testCoroutineDispatchers.runCurrent()

      val params = CreateProfileActivityParams.newBuilder()
        .setProfileType(ProfileType.SOLE_LEARNER)
        .build()

      intended(hasComponent(CreateProfileActivity::class.java.name))
      intended(hasExtraWithKey(PROFILE_ID_INTENT_DECORATOR))
      intended(hasProtoExtra(CREATE_PROFILE_PARAMS_KEY, params))
    }
  }

  @Test
  fun testFragment_orientationChange_studentNavigationCardClicked_launchesCreateProfileScreen() {
    launchOnboardingProfileTypeActivity().use {
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()

      onView(withId(R.id.profile_type_learner_navigation_card)).perform(click())
      testCoroutineDispatchers.runCurrent()

      val params = CreateProfileActivityParams.newBuilder()
        .setProfileType(ProfileType.SOLE_LEARNER)
        .build()

      intended(hasComponent(CreateProfileActivity::class.java.name))
      intended(hasExtraWithKey(PROFILE_ID_INTENT_DECORATOR))
      intended(hasProtoExtra(CREATE_PROFILE_PARAMS_KEY, params))
    }
  }

  @Test
  fun testFragment_supervisorNavigationCardClicked_launchesProfileChooserScreen() {
    launchOnboardingProfileTypeActivity().use {
      onView(withId(R.id.profile_type_supervisor_navigation_card)).perform(click())
      testCoroutineDispatchers.runCurrent()
      intended(hasComponent(ProfileChooserActivity::class.java.name))
    }
  }

  @Test
  fun testFragment_orientationChange_supervisorCardClicked_launchesProfileChooserScreen() {
    launchOnboardingProfileTypeActivity().use {
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.profile_type_supervisor_navigation_card)).perform(click())
      testCoroutineDispatchers.runCurrent()
      intended(hasComponent(ProfileChooserActivity::class.java.name))
    }
  }

  @Test
  fun testFragment_backButtonPressed_currentScreenIsDestroyed() {
    launchOnboardingProfileTypeActivity().use { scenario ->
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.onboarding_navigation_back)).perform(click())
      scenario?.onActivity { activity ->
        assertThat(activity.isFinishing).isTrue()
      }
    }
  }

  @Test
  fun testFragment_landscapeMode_backButtonPressed_currentScreenIsDestroyed() {
    launchOnboardingProfileTypeActivity().use { scenario ->
      onView(isRoot()).perform(orientationLandscape())
      testCoroutineDispatchers.runCurrent()
      onView(withId(R.id.onboarding_navigation_back)).perform(click())
      testCoroutineDispatchers.runCurrent()
      scenario?.onActivity { activity ->
        assertThat(activity.isFinishing).isTrue()
      }
    }
  }

  @Test
  fun testFragment_launchFragment_logsProfileOnboardingStartedEvent() {
    launchOnboardingProfileTypeActivity().use {
      onView(withId(R.id.profile_type_supervisor_navigation_card)).perform(click())
      testCoroutineDispatchers.runCurrent()
      val event = fakeAnalyticsEventLogger.getMostRecentEvent()
      EventLogSubject.assertThat(event).hasStartProfileOnboardingContextThat {
        hasProfileIdThat().isEqualTo(testProfileId)
      }
    }
  }

  private fun launchOnboardingProfileTypeActivity():
    ActivityScenario<OnboardingProfileTypeActivity>? {
      val scenario = ActivityScenario.launch<OnboardingProfileTypeActivity>(
        OnboardingProfileTypeActivity.createOnboardingProfileTypeActivityIntent(context).apply {
          decorateWithUserProfileId(testProfileId)
        }
      )
      testCoroutineDispatchers.runCurrent()
      return scenario
    }

  private fun setUpTestApplicationComponent() {
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
      PlatformParameterTestModule::class,
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
      TestingBuildFlavorModule::class,
      TextInputRuleModule::class,
      ViewBindingShimModule::class,
      WorkManagerConfigurationModule::class
    ]
  )
  interface TestApplicationComponent :
    ApplicationComponent,
    PlatformParameterInitializationInjector {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder

    fun inject(onboardingProfileTypeFragmentTest: OnboardingProfileTypeFragmentTest)
  }

  class TestApplication :
    Application(),
    ActivityComponentFactory,
    ApplicationInjectorProvider,
    PlatformParameterInitializationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerOnboardingProfileTypeFragmentTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(onboardingProfileTypeFragmentTest: OnboardingProfileTypeFragmentTest) {
      component.inject(onboardingProfileTypeFragmentTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component

    override fun getPlatformParameterInitializationInjector() = component
  }
}
