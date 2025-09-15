package org.oppia.android.app.profile

import android.app.Application
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.Component
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
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.END_PROFILE_ONBOARDING_EVENT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.OPEN_PROFILE_CHOOSER
import org.oppia.android.app.model.EventLog.Priority
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
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
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.question.QuestionModule
import org.oppia.android.domain.workmanager.WorkManagerConfigurationModule
import org.oppia.android.testing.FakeAnalyticsEventLogger
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.platformparameter.TestPlatformParameterModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.accessibility.AccessibilityTestModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.gcsresource.GcsResourceModule
import org.oppia.android.util.locale.LocaleProdModule
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
import org.junit.After
import org.oppia.android.app.model.ProfileChooserActivityParams
import org.oppia.android.app.model.ProfileChooserActivityParams.ParentScreen
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.onboarding.PROFILE_CHOOSER_PARAMS_KEY
import org.oppia.android.testing.profile.ProfileTestHelper
import org.oppia.android.util.extensions.putProtoExtra

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = ProfileChooserFragmentLocalTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class ProfileChooserFragmentLocalTest {
  @get:Rule val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @Inject lateinit var fakeAnalyticsEventLogger: FakeAnalyticsEventLogger
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject lateinit var profileTestHelper: ProfileTestHelper

  @After
  fun tearDown() {
    TestPlatformParameterModule.reset()
  }

  @Test
  fun testProfileChooser_onboardingV1_onEveryLaunch_logsProfileChooserEvent() {
    TestPlatformParameterModule.forceEnableOnboardingFlowV2(false)
    setUpTestApplicationComponent()
    launch<ProfileChooserActivity>(createProfileChooserActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      val event = fakeAnalyticsEventLogger.getMostRecentEvent()

      assertThat(event.priority).isEqualTo(Priority.ESSENTIAL)
      assertThat(event.context.activityContextCase).isEqualTo(OPEN_PROFILE_CHOOSER)
    }
  }

  @Test
  fun testProfileChooser_onboardingV2_onInitialLaunch_logsProfileChooserEvent() {
    TestPlatformParameterModule.forceEnableOnboardingFlowV2(true)
    setUpTestApplicationComponent()
    launch<ProfileChooserActivity>(
      createProfileChooserActivityIntent(ParentScreen.ADMIN_INTRO_SCREEN)
    ).use {
      testCoroutineDispatchers.runCurrent()
      val event = fakeAnalyticsEventLogger.getMostRecentEvent()

      assertThat(event.priority).isEqualTo(Priority.ESSENTIAL)
      assertThat(event.context.activityContextCase).isEqualTo(OPEN_PROFILE_CHOOSER)
    }
  }

  @Test
  fun testProfileChooser_onboardingV2_onEveryLaunch_logsProfileChooserEvent() {
    TestPlatformParameterModule.forceEnableOnboardingFlowV2(true)
    setUpTestApplicationComponent()
    launch<ProfileChooserActivity>(createProfileChooserActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()
      val event = fakeAnalyticsEventLogger.getMostRecentEvent()

      assertThat(event.priority).isEqualTo(Priority.ESSENTIAL)
      assertThat(event.context.activityContextCase).isEqualTo(OPEN_PROFILE_CHOOSER)
    }
  }

  @Test
  fun testProfileChooser_onboardingV1_onInitialLaunch_doesNotLogCompleteProfileOboardingEvent() {
    TestPlatformParameterModule.forceEnableOnboardingFlowV2(false)
    setUpTestApplicationComponent()
    profileTestHelper.initializeProfiles(autoLogIn = true)
    launch<ProfileChooserActivity>(createProfileChooserActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()

      val hasProfileOnboardingEndedEvent = fakeAnalyticsEventLogger.hasEventLogged {
        it.context.activityContextCase == END_PROFILE_ONBOARDING_EVENT
      }

      assertThat(hasProfileOnboardingEndedEvent).isFalse()
    }
  }

  @Test
  fun testProfileChooser_onboardingV2_onInitialLaunch_logsCompleteProfileOboardingEvent() {
    TestPlatformParameterModule.forceEnableOnboardingFlowV2(true)
    setUpTestApplicationComponent()
    profileTestHelper.addOnlyAdminProfileWithoutPin()

    launch<ProfileChooserActivity>(
      createProfileChooserActivityIntent(ParentScreen.ADMIN_INTRO_SCREEN)
    ).use {
      testCoroutineDispatchers.runCurrent()

      val hasProfileOnboardingEndedEvent = fakeAnalyticsEventLogger.hasEventLogged {
        it.context.activityContextCase == END_PROFILE_ONBOARDING_EVENT
      }

      assertThat(hasProfileOnboardingEndedEvent).isTrue()
    }
  }

  @Test
  fun testProfileChooser_onboardingV2_onSubsequentLaunch_doesNotLogCompleteProfileOboardingEvent() {
    TestPlatformParameterModule.forceEnableOnboardingFlowV2(true)
    setUpTestApplicationComponent()

    // Logs in to the admin account for the first time.
    profileTestHelper.initializeProfiles(autoLogIn = true)

    // Logs in to the admin account for the second time.
    profileTestHelper.logIntoAdmin()

    launch<ProfileChooserActivity>(createProfileChooserActivityIntent()).use {
      testCoroutineDispatchers.runCurrent()

      val hasProfileOnboardingEndedEvent = fakeAnalyticsEventLogger.hasEventLogged {
        it.context.activityContextCase == END_PROFILE_ONBOARDING_EVENT
      }

      assertThat(hasProfileOnboardingEndedEvent).isFalse()
    }
  }

  private fun createProfileChooserActivityIntent(
    parentScreen: ParentScreen = ParentScreen.SPLASH_SCREEN
  ): Intent {
    val params = ProfileChooserActivityParams.newBuilder()
      .setParentScreen(parentScreen)
      .build()

    return ProfileChooserActivity
      .createProfileChooserActivity(ApplicationProvider.getApplicationContext()).apply {
        putProtoExtra(PROFILE_CHOOSER_PARAMS_KEY, params)
      }
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
      PlatformParameterSingletonModule::class,
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
      TestPlatformParameterModule::class,
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

    fun inject(profileChooserFragmentLocalTest: ProfileChooserFragmentLocalTest)
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerProfileChooserFragmentLocalTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(profileChooserFragmentLocalTest: ProfileChooserFragmentLocalTest) {
      component.inject(profileChooserFragmentLocalTest)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
