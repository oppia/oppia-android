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
import org.oppia.android.domain.classroom.TEST_CLASSROOM_ID_0
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
import org.oppia.android.domain.topic.TEST_TOPIC_ID_1
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.assertThrows
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
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

private const val SESSION_LENGTH_1 = 300000L
private const val SESSION_LENGTH_2 = 600000L
private const val SESSION_LENGTH_3 = 100000L

/** Tests for [ExplorationActiveTimeController]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = ExplorationActiveTimeControllerTest.TestApplication::class)
class ExplorationActiveTimeControllerTest {
  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var monitorFactory: DataProviderTestMonitor.Factory

  @Inject
  lateinit var oppiaClock: FakeOppiaClock

  @Inject
  lateinit var explorationActiveTimeController: ExplorationActiveTimeController

  @Inject
  lateinit var applicationLifecycleObserver: ApplicationLifecycleObserver

  @Inject
  lateinit var explorationDataController: ExplorationDataController

  private val firstTestProfile = ProfileId.newBuilder().setLoggedInInternalProfileId(0).build()
  private val secondTestProfile = ProfileId.newBuilder().setLoggedInInternalProfileId(1).build()

  @Before
  fun setUp() {
    TestPlatformParameterModule.forceEnableNpsSurvey(true)
  }

  @Test
  fun testSessionTimer_explorationStartedCallbackReceived_startsSessionTimer() {
    setUpTestApplicationComponent()
    oppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)

    applicationLifecycleObserver.onAppInForeground()
    startPlayingNewExploration(
      TEST_CLASSROOM_ID_0,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      firstTestProfile
    )

    testCoroutineDispatchers.advanceTimeBy(SESSION_LENGTH_1)

    stopExploration()

    val retrieveAggregateTimeProvider =
      explorationActiveTimeController.retrieveAggregateTopicLearningTimeDataProvider(
        firstTestProfile, TEST_TOPIC_ID_0
      )
    val aggregateTime = monitorFactory.waitForNextSuccessfulResult(retrieveAggregateTimeProvider)
    assertThat(aggregateTime.topicLearningTimeMs).isEqualTo(SESSION_LENGTH_1)
  }

  @Test
  fun testSessionTimer_explorationStarted_appInBackground_pausesSessionTimer() {
    setUpTestApplicationComponent()
    oppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)

    applicationLifecycleObserver.onAppInForeground()
    startPlayingNewExploration(
      TEST_CLASSROOM_ID_0,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      firstTestProfile
    )

    testCoroutineDispatchers.advanceTimeBy(SESSION_LENGTH_1)

    applicationLifecycleObserver.onAppInBackground()

    val retrieveAggregateTimeProvider =
      explorationActiveTimeController.retrieveAggregateTopicLearningTimeDataProvider(
        firstTestProfile, TEST_TOPIC_ID_0
      )
    val aggregateTime = monitorFactory.waitForNextSuccessfulResult(retrieveAggregateTimeProvider)
    assertThat(aggregateTime.topicLearningTimeMs).isEqualTo(SESSION_LENGTH_1)
  }

  @Test
  fun testSessionTimer_explorationStarted_appInBackground_thenInForeground_resumesSessionTimer() {
    setUpTestApplicationComponent()
    oppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)

    explorationActiveTimeController.onAppInForeground()
    startPlayingNewExploration(
      TEST_CLASSROOM_ID_0,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      firstTestProfile
    )

    testCoroutineDispatchers.advanceTimeBy(SESSION_LENGTH_1)

    explorationActiveTimeController.onAppInBackground()
    testCoroutineDispatchers.runCurrent()

    // App spends time in the background.
    testCoroutineDispatchers.advanceTimeBy(SESSION_LENGTH_3)

    explorationActiveTimeController.onAppInForeground()
    testCoroutineDispatchers.runCurrent()

    // Additional active time in an exploration.
    testCoroutineDispatchers.advanceTimeBy(SESSION_LENGTH_2)

    stopExploration()

    val retrieveAggregateTimeProvider =
      explorationActiveTimeController.retrieveAggregateTopicLearningTimeDataProvider(
        firstTestProfile, TEST_TOPIC_ID_0
      )
    val aggregateTime = monitorFactory.waitForNextSuccessfulResult(retrieveAggregateTimeProvider)
    assertThat(aggregateTime.topicLearningTimeMs).isEqualTo(
      SESSION_LENGTH_1 + SESSION_LENGTH_2
    )
  }

  @Test
  fun testSessionTimer_explorationNotStarted_appInForeground_doesNotStartSessionTimer() {
    setUpTestApplicationComponent()
    oppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)

    applicationLifecycleObserver.onAppInForeground()

    testCoroutineDispatchers.advanceTimeBy(SESSION_LENGTH_1)

    val retrieveAggregateTimeProvider =
      explorationActiveTimeController.retrieveAggregateTopicLearningTimeDataProvider(
        firstTestProfile, TEST_TOPIC_ID_0
      )
    val aggregateTime = monitorFactory.waitForNextSuccessfulResult(retrieveAggregateTimeProvider)
    assertThat(aggregateTime.lastUpdatedTimeMs).isEqualTo(0L)
  }

  @Test
  fun testSessionTimer_explorationStopped_appInForeground_doesNotStartSessionTimer() {
    setUpTestApplicationComponent()
    oppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)

    applicationLifecycleObserver.onAppInForeground()
    startPlayingNewExploration(
      TEST_CLASSROOM_ID_0,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      firstTestProfile
    )

    testCoroutineDispatchers.advanceTimeBy(SESSION_LENGTH_1)

    stopExploration()

    // Some more time has passed after the exploration was ended.
    testCoroutineDispatchers.advanceTimeBy(SESSION_LENGTH_2)

    val retrieveAggregateTimeProvider =
      explorationActiveTimeController.retrieveAggregateTopicLearningTimeDataProvider(
        firstTestProfile, TEST_TOPIC_ID_0
      )
    val aggregateTime = monitorFactory.waitForNextSuccessfulResult(retrieveAggregateTimeProvider)

    assertThat(aggregateTime.topicLearningTimeMs).isEqualTo(SESSION_LENGTH_1)
  }

  @Test
  fun testStopTimer_beforeStarting_isFailure() {
    setUpTestApplicationComponent()

    val exception = assertThrows<IllegalStateException>() {
      stopExploration()
    }
    assertThat(exception)
      .hasMessageThat()
      .contains("Session isn't initialized yet.")
  }

  @Test
  fun testSessionTimer_explorationNotStarted_onAppInBackground_doesNotStartSessionTimer() {
    setUpTestApplicationComponent()
    oppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)

    applicationLifecycleObserver.onAppInBackground()

    testCoroutineDispatchers.advanceTimeBy(SESSION_LENGTH_1)

    val retrieveAggregateTimeProvider =
      explorationActiveTimeController.retrieveAggregateTopicLearningTimeDataProvider(
        firstTestProfile, TEST_TOPIC_ID_0
      )
    val aggregateTime = monitorFactory.waitForNextSuccessfulResult(retrieveAggregateTimeProvider)

    assertThat(aggregateTime.topicLearningTimeMs).isEqualTo(0L)
  }

  @Test
  fun testTimerStopped_previousAggregateTimeIsNull_setsCurrentSessionLength() {
    setUpTestApplicationComponent()
    oppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)

    explorationActiveTimeController.onAppInForeground()
    startPlayingNewExploration(
      TEST_CLASSROOM_ID_0,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      firstTestProfile
    )

    testCoroutineDispatchers.advanceTimeBy(SESSION_LENGTH_1)

    stopExploration()

    val retrieveAggregateTimeProvider =
      explorationActiveTimeController.retrieveAggregateTopicLearningTimeDataProvider(
        firstTestProfile, TEST_TOPIC_ID_0
      )

    val aggregateTime = monitorFactory.waitForNextSuccessfulResult(retrieveAggregateTimeProvider)

    assertThat(aggregateTime.lastUpdatedTimeMs).isEqualTo(oppiaClock.getCurrentTimeMs())
    assertThat(aggregateTime.topicLearningTimeMs).isEqualTo(SESSION_LENGTH_1)
  }

  @Test
  fun testTimerStopped_previousAggregateNotStale_incrementsOldAggregateByCurrentLength() {
    // Simulate previous app already has some topic learning time recorded
    executeInPreviousAppInstance { testComponent ->
      testComponent.getOppiaClock().setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
      testComponent.getExplorationActiveTimeController().onAppInForeground()
      testComponent.getTestCoroutineDispatchers().runCurrent()

      testComponent.getExplorationActiveTimeController()
        .onExplorationStarted(firstTestProfile, TEST_TOPIC_ID_0)
      testComponent.getTestCoroutineDispatchers().runCurrent()

      testComponent.getTestCoroutineDispatchers().advanceTimeBy(SESSION_LENGTH_1)

      testComponent.getExplorationActiveTimeController().onExplorationEnded()
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }

    // Create the application after previous arrangement to simulate a re-creation.
    setUpTestApplicationComponent()
    oppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    applicationLifecycleObserver.onAppInForeground()
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.DAYS.toMillis(1))

    startPlayingNewExploration(
      TEST_CLASSROOM_ID_0,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      firstTestProfile
    )
    testCoroutineDispatchers.advanceTimeBy(SESSION_LENGTH_2)

    stopExploration()

    val retrieveAggregateTimeProvider =
      explorationActiveTimeController.retrieveAggregateTopicLearningTimeDataProvider(
        firstTestProfile, TEST_TOPIC_ID_0
      )

    val aggregateTime = monitorFactory.waitForNextSuccessfulResult(retrieveAggregateTimeProvider)

    val expectedAggregate = SESSION_LENGTH_1 + SESSION_LENGTH_2
    assertThat(aggregateTime.lastUpdatedTimeMs).isEqualTo(oppiaClock.getCurrentTimeMs())
    assertThat(aggregateTime.topicLearningTimeMs).isEqualTo(expectedAggregate)
  }

  @Test
  fun testTimerStopped_previousAggregateIsStale_overwritesOldAggregateTime() {
    // Simulate previous app already has some topic learning time recorded
    executeInPreviousAppInstance { testComponent ->
      testComponent.getOppiaClock().setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
      testComponent.getExplorationActiveTimeController().onAppInForeground()
      testComponent.getTestCoroutineDispatchers().runCurrent()

      testComponent.getExplorationActiveTimeController()
        .onExplorationStarted(firstTestProfile, TEST_TOPIC_ID_0)
      testComponent.getTestCoroutineDispatchers().runCurrent()

      testComponent.getTestCoroutineDispatchers().advanceTimeBy(SESSION_LENGTH_1)
      testComponent.getExplorationActiveTimeController().onExplorationEnded()
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }

    // Create the application after previous arrangement to simulate a re-creation.
    setUpTestApplicationComponent()
    oppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.DAYS.toMillis(10))
    applicationLifecycleObserver.onAppInForeground()

    startPlayingNewExploration(
      TEST_CLASSROOM_ID_0,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      firstTestProfile
    )
    testCoroutineDispatchers.advanceTimeBy(SESSION_LENGTH_2)

    stopExploration()

    val retrieveAggregateTimeProvider =
      explorationActiveTimeController.retrieveAggregateTopicLearningTimeDataProvider(
        firstTestProfile, TEST_TOPIC_ID_0
      )

    val aggregateTime = monitorFactory.waitForNextSuccessfulResult(retrieveAggregateTimeProvider)

    assertThat(aggregateTime.lastUpdatedTimeMs).isEqualTo(oppiaClock.getCurrentTimeMs())
    assertThat(aggregateTime.topicLearningTimeMs).isEqualTo(SESSION_LENGTH_2)
  }

  @Test
  fun testTimerStopped_multipleOngoingTopics_correctTopicLearningTimeUpdated() {
    setUpTestApplicationComponent()
    oppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    applicationLifecycleObserver.onAppInForeground()

    startPlayingNewExploration(
      TEST_CLASSROOM_ID_0,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      firstTestProfile
    )
    testCoroutineDispatchers.advanceTimeBy(SESSION_LENGTH_1)
    stopExploration()

    startPlayingNewExploration(
      TEST_CLASSROOM_ID_0,
      TEST_TOPIC_ID_1,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      firstTestProfile
    )
    testCoroutineDispatchers.advanceTimeBy(SESSION_LENGTH_2)
    stopExploration()

    val retrieveTopic0AggregateTimeProvider =
      explorationActiveTimeController.retrieveAggregateTopicLearningTimeDataProvider(
        firstTestProfile, TEST_TOPIC_ID_0
      )

    val retrieveTopic1AggregateTimeProvider =
      explorationActiveTimeController.retrieveAggregateTopicLearningTimeDataProvider(
        firstTestProfile, TEST_TOPIC_ID_1
      )

    val topic0AggregateTime =
      monitorFactory.waitForNextSuccessfulResult(retrieveTopic0AggregateTimeProvider)
    val topic1AggregateTime =
      monitorFactory.waitForNextSuccessfulResult(retrieveTopic1AggregateTimeProvider)

    assertThat(topic0AggregateTime.topicLearningTimeMs).isEqualTo(SESSION_LENGTH_1)
    assertThat(topic1AggregateTime.topicLearningTimeMs).isEqualTo(SESSION_LENGTH_2)
  }

  @Test
  fun testSessionStopped_multipleProfiles_sameTopicId_learningTimeUpdatedInCorrectProfile() {
    setUpTestApplicationComponent()
    oppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    applicationLifecycleObserver.onAppInForeground()

    startPlayingNewExploration(
      TEST_CLASSROOM_ID_0,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      firstTestProfile
    )
    testCoroutineDispatchers.advanceTimeBy(SESSION_LENGTH_1)
    stopExploration()

    startPlayingNewExploration(
      TEST_CLASSROOM_ID_0,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      secondTestProfile
    )
    testCoroutineDispatchers.advanceTimeBy(SESSION_LENGTH_2)
    stopExploration()

    val retrieveFirstTestProfileAggregateTimeProvider =
      explorationActiveTimeController.retrieveAggregateTopicLearningTimeDataProvider(
        firstTestProfile, TEST_TOPIC_ID_0
      )

    val retrieveSecondTestProfileAggregateTimeProvider =
      explorationActiveTimeController.retrieveAggregateTopicLearningTimeDataProvider(
        secondTestProfile, TEST_TOPIC_ID_0
      )

    val firstTestProfileAggregateTime =
      monitorFactory.waitForNextSuccessfulResult(retrieveFirstTestProfileAggregateTimeProvider)
    val secondTestProfileAggregateTime =
      monitorFactory.waitForNextSuccessfulResult(retrieveSecondTestProfileAggregateTimeProvider)

    assertThat(firstTestProfileAggregateTime.topicLearningTimeMs).isEqualTo(SESSION_LENGTH_1)
    assertThat(secondTestProfileAggregateTime.topicLearningTimeMs).isEqualTo(SESSION_LENGTH_2)
  }

  private fun startPlayingNewExploration(
    classroomId: String,
    topicId: String,
    storyId: String,
    explorationId: String,
    profileId: ProfileId
  ) {
    val startPlayingProvider =
      explorationDataController.startPlayingNewExploration(
        profileId.loggedInInternalProfileId, classroomId, topicId, storyId, explorationId
      )
    monitorFactory.waitForNextSuccessfulResult(startPlayingProvider)
  }

  private fun stopExploration(isCompletion: Boolean = true) {
    val stopProvider = explorationDataController.stopPlayingExploration(isCompletion)
    monitorFactory.waitForNextSuccessfulResult(stopProvider)
  }

  private fun executeInPreviousAppInstance(block: (TestApplicationComponent) -> Unit) {
    val testApplication = TestApplication()
    // The true application is hooked as a base context. This is to make sure the new application
    // can behave like a real Android application class (per Robolectric) without having a shared
    // Dagger dependency graph with the application under test.
    testApplication.attachBaseContext(ApplicationProvider.getApplicationContext())
    block(
      DaggerExplorationActiveTimeControllerTest_TestApplicationComponent.builder()
        .setApplication(testApplication)
        .build()
    )
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
      TestModule::class, RobolectricModule::class, FakeOppiaClockModule::class,
      ApplicationLifecycleModule::class, TestDispatcherModule::class, LocaleProdModule::class,
      ExplorationProgressModule::class, TestLogReportingModule::class, ContinueModule::class,
      ItemSelectionInputModule::class, MultipleChoiceInputModule::class, FractionInputModule::class,
      NumberWithUnitsRuleModule::class, NumericInputRuleModule::class, TextInputRuleModule::class,
      DragDropSortInputModule::class, InteractionsModule::class, ImageClickInputModule::class,
      HintsAndSolutionConfigModule::class, HintsAndSolutionProdModule::class, AssetModule::class,
      NetworkConnectionUtilDebugModule::class, NumericExpressionInputModule::class,
      AlgebraicExpressionInputModule::class, MathEquationInputModule::class,
      RatioInputModule::class, SyncStatusModule::class, LoggingIdentifierModule::class,
      CpuPerformanceSnapshotterModule::class, PlatformParameterSingletonModule::class,
      TestPlatformParameterModule::class, ExplorationStorageTestModule::class,
      LogStorageModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(explorationActiveTimeControllerTest: ExplorationActiveTimeControllerTest)

    fun getExplorationActiveTimeController(): ExplorationActiveTimeController

    fun getTestCoroutineDispatchers(): TestCoroutineDispatchers

    fun getOppiaClock(): FakeOppiaClock
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerExplorationActiveTimeControllerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(explorationActiveTimeControllerTest: ExplorationActiveTimeControllerTest) {
      component.inject(explorationActiveTimeControllerTest)
    }

    public override fun attachBaseContext(base: Context?) {
      super.attachBaseContext(base)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
