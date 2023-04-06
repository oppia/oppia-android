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
import org.oppia.android.app.model.TopicLearningTime
import org.oppia.android.app.model.TopicLearningTimeDatabase
import org.oppia.android.data.persistence.PersistentCacheStore
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.topic.TEST_TOPIC_ID_0
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
import org.oppia.android.util.locale.testing.LocaleTestModule
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

private const val CACHE_NAME = "topic_learning_time_database"
private const val SESSION_LENGTH = 5000L

/** Tests for [ExplorationActiveTimeController]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = ExplorationActiveTimeControllerTest.TestApplication::class)
class ExplorationActiveTimeControllerTest {
  @Inject lateinit var fakeExceptionLogger: FakeExceptionLogger
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject lateinit var monitorFactory: DataProviderTestMonitor.Factory
  @Inject lateinit var cacheFactory: PersistentCacheStore.Factory
  @Inject lateinit var oppiaClock: FakeOppiaClock
  @Inject lateinit var explorationActiveTimeController: ExplorationActiveTimeController

  private val profileId = ProfileId.newBuilder().setInternalId(0).build()

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testExplorationSessionStarted_setsStartExplorationTimestampToCurrentTime() {
    oppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
    explorationActiveTimeController.setExplorationSessionStarted()
    val currentTime = oppiaClock.getCurrentTimeMs()
    assertThat(explorationActiveTimeController.startExplorationTimestampMs).isEqualTo(currentTime)
  }

  @Test
  fun testRecordAggregateTopicLearningTime_returnsSuccess() {
    val recordAggregateTimeProvider =
      explorationActiveTimeController.recordAggregateTopicLearningTime(
        profileId, TEST_TOPIC_ID_0, SESSION_LENGTH
      )
    monitorFactory.waitForNextSuccessfulResult(recordAggregateTimeProvider)
  }

  @Test
  fun testSaveSessionLength_previousAggregateIsStale_overwritesOldAggregateWithCurrentLength() {
    oppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
    val currentTime = oppiaClock.getCurrentTimeMs()

    // set aggregate learning time last updated timestamp to
    // [LEARNING_TIME_STALENESS_THRESHOLD_MILLIS]+1 days ago.
    val lastUpdatedTimeMs = currentTime.minus(LEARNING_TIME_STALENESS_THRESHOLD_MILLIS).minus(
      TimeUnit.DAYS.toMillis(1)
    )

    createCacheStore(lastUpdatedTimeMs)

    val recordAggregateTimeProvider =
      explorationActiveTimeController.recordAggregateTopicLearningTime(
        profileId, TEST_TOPIC_ID_0, SESSION_LENGTH
      )

    monitorFactory.waitForNextSuccessfulResult(recordAggregateTimeProvider)

    val retrieveAggregateTimeProvider =
      explorationActiveTimeController.retrieveAggregateTopicLearningTimeDataProvider(
        profileId, TEST_TOPIC_ID_0
      )

    val aggregateTime = monitorFactory.waitForNextSuccessfulResult(retrieveAggregateTimeProvider)

    assertThat(aggregateTime.lastUpdatedTimeMs).isEqualTo(currentTime)
    assertThat(aggregateTime.topicLearningTimeMs).isEqualTo(SESSION_LENGTH)
  }

  @Test
  fun testSaveSessionLength_previousAggregateIsNotStale_incrementsOldAggregateByCurrentLength() {
    oppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_FIXED_FAKE_TIME)
    val currentTime = oppiaClock.getCurrentTimeMs()

    // set aggregate learning time last updated timestamp to yesterday.
    val lastUpdatedTimeMs = currentTime.minus(TimeUnit.DAYS.toMillis(1))

    createCacheStore(lastUpdatedTimeMs)

    val recordAggregateTimeProvider =
      explorationActiveTimeController.recordAggregateTopicLearningTime(
        profileId, TEST_TOPIC_ID_0, SESSION_LENGTH
      )

    monitorFactory.waitForNextSuccessfulResult(recordAggregateTimeProvider)

    val retrieveAggregateTimeProvider =
      explorationActiveTimeController.retrieveAggregateTopicLearningTimeDataProvider(
        profileId, TEST_TOPIC_ID_0
      )

    val aggregateTime = monitorFactory.waitForNextSuccessfulResult(retrieveAggregateTimeProvider)

    val expectedAggregate = SESSION_LENGTH + SESSION_LENGTH
    assertThat(aggregateTime.lastUpdatedTimeMs).isEqualTo(currentTime)
    assertThat(aggregateTime.topicLearningTimeMs).isEqualTo(expectedAggregate)
  }

  private fun createCacheStore(lastUpdatedTimeMs: Long) {
    cacheFactory.createPerProfile(
      CACHE_NAME,
      createTopicLearningTimeDatabase(lastUpdatedTimeMs),
      profileId
    )
  }

  private fun createTopicLearningTimeDatabase(lastUpdatedTimeMs: Long): TopicLearningTimeDatabase {
    val topicLearningTime = TopicLearningTime.newBuilder()
      .setTopicId(TEST_TOPIC_ID_0)
      .setTopicLearningTimeMs(SESSION_LENGTH)
      .setLastUpdatedTimeMs(lastUpdatedTimeMs)
      .build()
    return TopicLearningTimeDatabase.newBuilder()
      .putAggregateTopicLearningTime(TEST_TOPIC_ID_0, topicLearningTime)
      .build()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>()
      .inject(this)
  }

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

  @Singleton
  @Component(
    modules = [
      TestModule::class, RobolectricModule::class, TestLogReportingModule::class,
      FakeOppiaClockModule::class, ApplicationLifecycleModule::class, TestDispatcherModule::class,
      LocaleTestModule::class, ExplorationProgressModule::class
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
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerTopicLearningTimeControllerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(explorationActiveTimeControllerTest: ExplorationActiveTimeControllerTest) {
      component.inject(explorationActiveTimeControllerTest)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
