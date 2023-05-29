package org.oppia.android.domain.exploration

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.ProfileId
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
import org.oppia.android.domain.exploration.testing.ExplorationStorageTestModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionConfigModule
import org.oppia.android.domain.hintsandsolution.HintsAndSolutionProdModule
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleObserver
import org.oppia.android.domain.oppialogger.analytics.CpuPerformanceSnapshotterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.topic.TEST_EXPLORATION_ID_2
import org.oppia.android.domain.topic.TEST_STORY_ID_0
import org.oppia.android.domain.topic.TEST_TOPIC_ID_0
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.environment.TestEnvironmentConfig
import org.oppia.android.testing.platformparameter.TestPlatformParameterModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClock
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.LoadLessonProtosFromAssets
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

private const val SESSION_LENGTH = 300000L
private const val SESSION_LENGTH_2 = 600000L

/** Tests for [ExplorationSessionTimerController] .*/
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = ExplorationSessionTimerControllerTest.TestApplication::class)
class ExplorationSessionTimerControllerTest {
  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var monitorFactory: DataProviderTestMonitor.Factory

  @Inject
  lateinit var oppiaClock: FakeOppiaClock

  @Inject
  lateinit var explorationSessionTimerController: ExplorationSessionTimerController

  @Inject
  lateinit var explorationActiveTimeController: ExplorationActiveTimeController

  @Inject
  lateinit var applicationLifecycleObserver: ApplicationLifecycleObserver

  @Inject
  lateinit var explorationDataController: ExplorationDataController

  private val profileId = ProfileId.newBuilder().setInternalId(0).build()

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testSessionTimer_explorationStarted_startsTimer() {
    oppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)

    explorationDataController.startPlayingNewExploration(
      profileId.internalId, TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2
    )
    testCoroutineDispatchers.advanceTimeBy(SESSION_LENGTH)
    explorationActiveTimeController.setExplorationSessionStopped(profileId, TEST_TOPIC_ID_0)

    val retrieveAggregateTimeProvider =
      explorationActiveTimeController.retrieveAggregateTopicLearningTimeDataProvider(
        profileId, TEST_TOPIC_ID_0
      )
    val aggregateTime = monitorFactory.waitForNextSuccessfulResult(retrieveAggregateTimeProvider)

    assertThat(aggregateTime.topicLearningTimeMs).isEqualTo(SESSION_LENGTH)
  }

  @Test
  fun testSessionTimer_explorationStopped_stopsTimer() {
    oppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    explorationDataController.startPlayingNewExploration(
      profileId.internalId, TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2
    )
    testCoroutineDispatchers.advanceTimeBy(SESSION_LENGTH)
    explorationDataController.stopPlayingExploration(isCompletion = false)

    val retrieveAggregateTimeProvider =
      explorationActiveTimeController.retrieveAggregateTopicLearningTimeDataProvider(
        profileId, TEST_TOPIC_ID_0
      )
    val aggregateTime = monitorFactory.waitForNextSuccessfulResult(retrieveAggregateTimeProvider)

    assertThat(aggregateTime.topicLearningTimeMs).isEqualTo(SESSION_LENGTH)
  }

  @Test
  fun testSessionTimer_explorationStarted_onAppInBackground_stopsTimer() {
    oppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    explorationDataController.startPlayingNewExploration(
      profileId.internalId, TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2
    )
    testCoroutineDispatchers.advanceTimeBy(SESSION_LENGTH)

    applicationLifecycleObserver.onAppInBackground()

    // Additional time has passed
    testCoroutineDispatchers.advanceTimeBy(SESSION_LENGTH_2)

    val retrieveAggregateTimeProvider =
      explorationActiveTimeController.retrieveAggregateTopicLearningTimeDataProvider(
        profileId, TEST_TOPIC_ID_0
      )
    val aggregateTime = monitorFactory.waitForNextSuccessfulResult(retrieveAggregateTimeProvider)

    assertThat(aggregateTime.topicLearningTimeMs).isEqualTo(SESSION_LENGTH)
  }

  @Test
  fun testSessionTimer_explorationStarted_onAppInBackground_thenInForeground_resumesTimer() {
    oppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    explorationDataController.startPlayingNewExploration(
      profileId.internalId, TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2
    )
    testCoroutineDispatchers.advanceTimeBy(SESSION_LENGTH)

    applicationLifecycleObserver.onAppInBackground()
    applicationLifecycleObserver.onAppInForeground()

    // Additional time has passed
    testCoroutineDispatchers.advanceTimeBy(SESSION_LENGTH_2)

    explorationActiveTimeController.setExplorationSessionStopped(profileId, TEST_TOPIC_ID_0)

    val retrieveAggregateTimeProvider =
      explorationActiveTimeController.retrieveAggregateTopicLearningTimeDataProvider(
        profileId, TEST_TOPIC_ID_0
      )
    val aggregateTime = monitorFactory.waitForNextSuccessfulResult(retrieveAggregateTimeProvider)

    assertThat(aggregateTime.topicLearningTimeMs).isEqualTo(
      SESSION_LENGTH + SESSION_LENGTH_2
    )
  }

  @Test
  fun testSessionTimer_onAppInForeground_explorationNotStarted_doesNotStartTimer() {
    oppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    explorationDataController.startPlayingNewExploration(
      profileId.internalId, TEST_TOPIC_ID_0, TEST_STORY_ID_0, TEST_EXPLORATION_ID_2
    )
    applicationLifecycleObserver.onAppInForeground()

    testCoroutineDispatchers.advanceTimeBy(SESSION_LENGTH)

    applicationLifecycleObserver.onAppInBackground()
    val retrieveAggregateTimeProvider =
      explorationActiveTimeController.retrieveAggregateTopicLearningTimeDataProvider(
        profileId, TEST_TOPIC_ID_0
      )
    val aggregateTime = monitorFactory.waitForNextSuccessfulResult(retrieveAggregateTimeProvider)

    assertThat(aggregateTime.topicLearningTimeMs).isEqualTo(SESSION_LENGTH)
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>()
      .inject(this)
  }

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }

    @EnableConsoleLog
    @Provides
    fun provideEnableConsoleLog(): Boolean = true

    @EnableFileLog
    @Provides
    fun provideEnableFileLog(): Boolean = false

    @GlobalLogLevel
    @Provides
    fun provideGlobalLogLevel(): LogLevel = LogLevel.VERBOSE

    @Provides
    @LoadLessonProtosFromAssets
    fun provideLoadLessonProtosFromAssets(testEnvironmentConfig: TestEnvironmentConfig): Boolean =
      testEnvironmentConfig.isUsingBazel()
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, ContinueModule::class, FractionInputModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, InteractionsModule::class, TestLogReportingModule::class,
      ImageClickInputModule::class, LogStorageModule::class, TestDispatcherModule::class,
      RatioInputModule::class, RobolectricModule::class, FakeOppiaClockModule::class,
      ExplorationStorageTestModule::class, HintsAndSolutionConfigModule::class,
      HintsAndSolutionProdModule::class, NetworkConnectionUtilDebugModule::class,
      AssetModule::class, LocaleProdModule::class, NumericExpressionInputModule::class,
      AlgebraicExpressionInputModule::class, MathEquationInputModule::class,
      LoggingIdentifierModule::class, SyncStatusModule::class, LocaleProdModule::class,
      PlatformParameterSingletonModule::class, TestPlatformParameterModule::class,
      PlatformParameterSingletonModule::class, ApplicationLifecycleModule::class,
      CpuPerformanceSnapshotterModule::class, ExplorationProgressModule::class

    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(explorationSessionTimerControllerTest: ExplorationSessionTimerControllerTest)

    fun getTestCoroutineDispatchers(): TestCoroutineDispatchers
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerExplorationSessionTimerControllerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(explorationSessionTimerControllerTest: ExplorationSessionTimerControllerTest) {
      component.inject(explorationSessionTimerControllerTest)
    }

    public override fun attachBaseContext(base: Context?) {
      super.attachBaseContext(base)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
