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
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.ProfileId
import org.oppia.android.data.persistence.PersistentCacheStore
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.topic.TEST_TOPIC_ID_0
import org.oppia.android.domain.topic.TEST_TOPIC_ID_1
import org.oppia.android.testing.FakeExceptionLogger
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClock
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

private const val SESSION_LENGTH_1 = 300000L
private const val SESSION_LENGTH_2 = 600000L

/** Tests for [ExplorationActiveTimeController]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = ExplorationActiveTimeControllerTest.TestApplication::class)
class ExplorationActiveTimeControllerTest {
  @Inject
  lateinit var fakeExceptionLogger: FakeExceptionLogger

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var monitorFactory: DataProviderTestMonitor.Factory

  @Inject
  lateinit var cacheFactory: PersistentCacheStore.Factory

  @Inject
  lateinit var oppiaClock: FakeOppiaClock

  @Inject
  lateinit var explorationActiveTimeController: ExplorationActiveTimeController

  private val firstTestProfile = ProfileId.newBuilder().setInternalId(0).build()
  private val secondTestProfile = ProfileId.newBuilder().setInternalId(1).build()

  @Test
  fun testSetExplorationSessionPaused_previousAggregateTimeIsNull_setsCurrentSessionLength() {
    setUpTestApplicationComponent()
    oppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)

    explorationActiveTimeController.setExplorationSessionStarted()
    testCoroutineDispatchers.advanceTimeBy(SESSION_LENGTH_1)
    explorationActiveTimeController.setExplorationSessionStopped(firstTestProfile, TEST_TOPIC_ID_0)

    val retrieveAggregateTimeProvider =
      explorationActiveTimeController.retrieveAggregateTopicLearningTimeDataProvider(
        firstTestProfile, TEST_TOPIC_ID_0
      )

    val aggregateTime = monitorFactory.waitForNextSuccessfulResult(retrieveAggregateTimeProvider)

    assertThat(aggregateTime.lastUpdatedTimeMs).isEqualTo(oppiaClock.getCurrentTimeMs())
    assertThat(aggregateTime.topicLearningTimeMs).isEqualTo(SESSION_LENGTH_1)
  }

  @Test
  fun testSetSessionPaused_previousAggregateNotStale_incrementsOldAggregateByCurrentLength() {
    // Simulate previous app already has some topic learning time recorded
    executeInPreviousAppInstance { testComponent ->
      testComponent.getOppiaClock().setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
      testComponent.getExplorationActiveTimeController().setExplorationSessionStarted()
      testComponent.getTestCoroutineDispatchers().advanceTimeBy(SESSION_LENGTH_1)
      testComponent.getExplorationActiveTimeController()
        .setExplorationSessionStopped(firstTestProfile, TEST_TOPIC_ID_0)
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }

    // Create the application after previous arrangement to simulate a re-creation.
    setUpTestApplicationComponent()
    oppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.DAYS.toMillis(1))
    explorationActiveTimeController.setExplorationSessionStarted()
    testCoroutineDispatchers.advanceTimeBy(SESSION_LENGTH_2)
    explorationActiveTimeController.setExplorationSessionStopped(firstTestProfile, TEST_TOPIC_ID_0)

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
  fun testSetExplorationSessionPaused_previousAggregateIsStale_overwritesOldAggregateTime() {
    // Simulate previous app already has some topic learning time recorded
    executeInPreviousAppInstance { testComponent ->
      testComponent.getOppiaClock().setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
      testComponent.getExplorationActiveTimeController().setExplorationSessionStarted()
      testComponent.getTestCoroutineDispatchers().advanceTimeBy(SESSION_LENGTH_1)
      testComponent.getExplorationActiveTimeController()
        .setExplorationSessionStopped(firstTestProfile, TEST_TOPIC_ID_0)
      testComponent.getTestCoroutineDispatchers().runCurrent()
    }

    // Create the application after previous arrangement to simulate a re-creation.
    setUpTestApplicationComponent()
    oppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
    testCoroutineDispatchers.advanceTimeBy(TimeUnit.DAYS.toMillis(10))
    explorationActiveTimeController.setExplorationSessionStarted()
    testCoroutineDispatchers.advanceTimeBy(SESSION_LENGTH_2)
    explorationActiveTimeController.setExplorationSessionStopped(firstTestProfile, TEST_TOPIC_ID_0)

    val retrieveAggregateTimeProvider =
      explorationActiveTimeController.retrieveAggregateTopicLearningTimeDataProvider(
        firstTestProfile, TEST_TOPIC_ID_0
      )

    val aggregateTime = monitorFactory.waitForNextSuccessfulResult(retrieveAggregateTimeProvider)

    assertThat(aggregateTime.lastUpdatedTimeMs).isEqualTo(oppiaClock.getCurrentTimeMs())
    assertThat(aggregateTime.topicLearningTimeMs).isEqualTo(SESSION_LENGTH_2)
  }

  @Test
  fun testSetSessionPaused_multipleOngoingTopics_correctTopicLearningTimeUpdated() {
    setUpTestApplicationComponent()
    oppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)

    explorationActiveTimeController.setExplorationSessionStarted()
    testCoroutineDispatchers.advanceTimeBy(SESSION_LENGTH_1)
    explorationActiveTimeController.setExplorationSessionStopped(firstTestProfile, TEST_TOPIC_ID_0)

    explorationActiveTimeController.setExplorationSessionStarted()
    testCoroutineDispatchers.advanceTimeBy(SESSION_LENGTH_2)
    explorationActiveTimeController.setExplorationSessionStopped(firstTestProfile, TEST_TOPIC_ID_1)

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
  fun testSetSessionPaused_multipleProfiles_sameTopicId_learningTimeUpdatedInCorrectProfile() {
    setUpTestApplicationComponent()
    oppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)

    explorationActiveTimeController.setExplorationSessionStarted()
    testCoroutineDispatchers.advanceTimeBy(SESSION_LENGTH_1)
    explorationActiveTimeController.setExplorationSessionStopped(firstTestProfile, TEST_TOPIC_ID_0)

    explorationActiveTimeController.setExplorationSessionStarted()
    testCoroutineDispatchers.advanceTimeBy(SESSION_LENGTH_2)
    explorationActiveTimeController.setExplorationSessionStopped(secondTestProfile, TEST_TOPIC_ID_0)

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
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, RobolectricModule::class, FakeOppiaClockModule::class,
      ApplicationLifecycleModule::class, TestDispatcherModule::class, LocaleProdModule::class,
      ExplorationProgressModule::class, TestLogReportingModule::class
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
