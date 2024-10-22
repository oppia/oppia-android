package org.oppia.android.app.home

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.Component
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
import org.oppia.android.app.model.EventLog
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.END_PROFILE_ONBOARDING_EVENT
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.OPEN_HOME
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionModule
import org.oppia.android.app.shim.IntentFactoryShimModule
import org.oppia.android.app.shim.ViewBindingShimModule
import org.oppia.android.app.translation.testing.ActivityRecreatorTestModule
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
import org.oppia.android.domain.onboarding.AppStartupStateController
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
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.firebase.TestAuthenticationModule
import org.oppia.android.testing.junit.InitializeDefaultLocaleRule
import org.oppia.android.testing.platformparameter.TestPlatformParameterModule
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

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(
  application = HomeActivityLocalTest.TestApplication::class,
  qualifiers = "port-xxhdpi"
)
class HomeActivityLocalTest {
  @get:Rule
  val initializeDefaultLocaleRule = InitializeDefaultLocaleRule()

  @Inject
  lateinit var fakeAnalyticsEventLogger: FakeAnalyticsEventLogger

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var appStartupStateController: AppStartupStateController

  @Inject
  lateinit var monitorFactory: DataProviderTestMonitor.Factory

  @Inject
  lateinit var profileTestHelper: ProfileTestHelper

  private val internalProfileId: Int = 0

  private val profileId: ProfileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()

  @Before
  fun setUp() {
    Intents.init()
  }

  @After
  fun tearDown() {
    TestPlatformParameterModule.reset()
    Intents.release()
  }

  @Test
  fun testHomeActivity_onLaunch_logsOpenHomeEvent() {
    setUpTestWithOnboardingV2Enabled(false)

    launch<HomeActivity>(createHomeActivityIntent(profileId)).use {
      testCoroutineDispatchers.runCurrent()
      val event = fakeAnalyticsEventLogger.getOldestEvent()

      assertThat(event.priority).isEqualTo(EventLog.Priority.ESSENTIAL)
      assertThat(event.context.activityContextCase).isEqualTo(OPEN_HOME)
    }
  }

  @Test
  fun testHomeActivity_onSubsequentLaunch_doesNotLogCompletedAppOnboardingEvent() {
    executeInPreviousAppInstance { testComponent ->
      testComponent.getAppStartupStateController().markOnboardingFlowCompleted()
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }

    setUpTestWithOnboardingV2Enabled(false)
    launch<HomeActivity>(createHomeActivityIntent(profileId)).use {
      testCoroutineDispatchers.runCurrent()
      val eventCount = fakeAnalyticsEventLogger.getEventListCount()
      val event = fakeAnalyticsEventLogger.getMostRecentEvent()

      assertThat(eventCount).isEqualTo(1)
      assertThat(event.priority).isEqualTo(EventLog.Priority.ESSENTIAL)
      assertThat(event.context.activityContextCase).isEqualTo(OPEN_HOME)
    }
  }

  @Test
  fun testHomeActivity_onboardingV2Enabled_onInitialLaunch_logsEndProfileOnboardingEvent() {
    setUpTestWithOnboardingV2Enabled(true)
    profileTestHelper.addOnlyAdminProfileWithoutPin()
    launch<HomeActivity>(createHomeActivityIntent(profileId)).use {
      testCoroutineDispatchers.runCurrent()

      val hasProfileOnboardingEndedEvent = fakeAnalyticsEventLogger.hasEventLogged {
        it.context.activityContextCase == END_PROFILE_ONBOARDING_EVENT
      }
      assertThat(hasProfileOnboardingEndedEvent).isTrue()
    }
  }

  @Test
  fun testHomeActivity_onboardingV2_revisitApp_doesNotLogEndProfileOnboardingEvent() {
    executeInPreviousAppInstance { testComponent ->
      testComponent.getAppStartupStateController().markOnboardingFlowCompleted()
      testComponent.getProfileTestHelper().markProfileOnboardingEnded(profileId)
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }

    setUpTestWithOnboardingV2Enabled(true)
    launch<HomeActivity>(createHomeActivityIntent(profileId)).use {
      testCoroutineDispatchers.runCurrent()

      val event = fakeAnalyticsEventLogger.getMostRecentEvent()
      assertThat(event.context.activityContextCase).isEqualTo(OPEN_HOME)
    }
  }

  private fun setUpTestWithOnboardingV2Enabled(enableOnboardingFlowV2: Boolean) {
    TestPlatformParameterModule.forceEnableOnboardingFlowV2(enableOnboardingFlowV2)
    setUpTestApplicationComponent()
  }

  /**
   * Creates a separate test application component and executes the specified block. This should be
   * called before [setUpTestApplicationComponent] to avoid undefined behavior in production code.
   * This can be used to simulate arranging state in a "prior" run of the app.
   *
   * Note that only dependencies fetched from the specified [TestApplicationComponent] should be
   * used, not any class-level injected dependencies.
   */
  private fun executeInPreviousAppInstance(block: (TestApplicationComponent) -> Unit) {
    val testApplication = TestApplication()
    // The true application is hooked as a base context. This is to make sure the new application
    // can behave like a real Android application class (per Robolectric) without having a shared
    // Dagger dependency graph with the application under test.
    testApplication.attachBaseContext(ApplicationProvider.getApplicationContext())
    block(
      DaggerHomeActivityLocalTest_TestApplicationComponent.builder()
        .setApplication(testApplication)
        .build() as TestApplicationComponent
    )
  }

  private fun createHomeActivityIntent(profileId: ProfileId): Intent {
    return HomeActivity.createHomeActivity(ApplicationProvider.getApplicationContext(), profileId)
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  // TODO(#59): Figure out a way to reuse modules instead of needing to re-declare them.
  @Singleton
  @Component(
    modules = [
      TestDispatcherModule::class, ApplicationModule::class, RobolectricModule::class,
      TestPlatformParameterModule::class, PlatformParameterSingletonModule::class,
      LoggerModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, InteractionsModule::class, GcsResourceModule::class,
      GlideImageLoaderModule::class, ImageParsingModule::class, HtmlParserEntityTypeModule::class,
      QuestionModule::class, TestLogReportingModule::class, AccessibilityTestModule::class,
      ImageClickInputModule::class, LogStorageModule::class, IntentFactoryShimModule::class,
      ViewBindingShimModule::class, CachingTestModule::class, RatioInputModule::class,
      ExpirationMetaDataRetrieverModule::class,
      ApplicationStartupListenerModule::class, LogReportWorkerModule::class,
      WorkManagerConfigurationModule::class, HintsAndSolutionConfigModule::class,
      FirebaseLogUploaderModule::class, FakeOppiaClockModule::class,
      DeveloperOptionsStarterModule::class, DeveloperOptionsModule::class,
      ExplorationStorageModule::class, NetworkModule::class, HintsAndSolutionProdModule::class,
      NetworkConnectionUtilDebugModule::class, NetworkConnectionDebugUtilModule::class,
      AssetModule::class, LocaleProdModule::class, ActivityRecreatorTestModule::class,
      NetworkConfigProdModule::class, NumericExpressionInputModule::class,
      AlgebraicExpressionInputModule::class, MathEquationInputModule::class,
      SplitScreenInteractionModule::class,
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

    fun inject(homeActivityLocalTest: HomeActivityLocalTest)

    fun getAppStartupStateController(): AppStartupStateController

    fun getTestCoroutineDispatchers(): TestCoroutineDispatchers

    fun getProfileTestHelper(): ProfileTestHelper
  }

  class TestApplication : Application(), ActivityComponentFactory, ApplicationInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerHomeActivityLocalTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build() as TestApplicationComponent
    }

    fun inject(homeActivityLocalTest: HomeActivityLocalTest) {
      component.inject(homeActivityLocalTest)
    }

    public override fun attachBaseContext(base: Context?) {
      super.attachBaseContext(base)
    }

    override fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
      return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
    }

    override fun getApplicationInjector(): ApplicationInjector = component
  }
}
