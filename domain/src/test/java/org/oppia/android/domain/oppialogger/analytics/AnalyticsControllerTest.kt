package org.oppia.android.domain.oppialogger.analytics

import android.app.Application
import android.content.Context
import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.OPEN_CONCEPT_CARD
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.OPEN_EXPLORATION_ACTIVITY
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.OPEN_INFO_TAB
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.OPEN_LESSONS_TAB
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.OPEN_PRACTICE_TAB
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.OPEN_QUESTION_PLAYER
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.OPEN_REVISION_CARD
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.OPEN_REVISION_TAB
import org.oppia.android.app.model.EventLog.Context.ActivityContextCase.OPEN_STORY_ACTIVITY
import org.oppia.android.app.model.EventLog.Priority
import org.oppia.android.app.model.OppiaEventLogs
import org.oppia.android.domain.oppialogger.EventLogStorageCacheSize
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.testing.FakeEventLogger
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.oppia.android.util.networking.NetworkConnectionDebugUtil
import org.oppia.android.util.networking.NetworkConnectionUtil.ProdConnectionStatus.NONE
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.testing.FakeSyncStatusManager
import org.oppia.android.util.logging.SyncStatusManager

private const val TEST_TIMESTAMP = 1556094120000
private const val TEST_TOPIC_ID = "test_topicId"
private const val TEST_STORY_ID = "test_storyId"
private const val TEST_EXPLORATION_ID = "test_explorationId"
private const val TEST_QUESTION_ID = "test_questionId"
private const val TEST_SKILL_ID = "test_skillId"
private const val TEST_SKILL_LIST_ID = "test_skillListId"
private const val TEST_SUB_TOPIC_ID = 1

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = AnalyticsControllerTest.TestApplication::class)
class AnalyticsControllerTest {

  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Inject
  lateinit var analyticsController: AnalyticsController

  @Inject
  lateinit var oppiaLogger: OppiaLogger

  @Inject
  lateinit var networkConnectionUtil: NetworkConnectionDebugUtil

  @Inject
  lateinit var fakeEventLogger: FakeEventLogger

  @Inject
  lateinit var fakeSyncStatusManager: FakeSyncStatusManager

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var dataProviders: DataProviders

  @Mock
  lateinit var mockOppiaEventLogsObserver: Observer<AsyncResult<OppiaEventLogs>>

  @Captor
  lateinit var oppiaEventLogsResultCaptor: ArgumentCaptor<AsyncResult<OppiaEventLogs>>

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testController_logTransitionEvent_withQuestionContext_checkLogsEvent() {
    analyticsController.logTransitionEvent(
      TEST_TIMESTAMP,
      oppiaLogger.createOpenQuestionPlayerContext(
        TEST_QUESTION_ID,
        listOf(
          TEST_SKILL_LIST_ID, TEST_SKILL_LIST_ID
        )
      )
    )

    assertThat(fakeEventLogger.getMostRecentEvent().timestamp).isEqualTo(TEST_TIMESTAMP)
    // ESSENTIAL priority confirms that the event logged is a transition event.
    assertThat(fakeEventLogger.getMostRecentEvent().priority).isEqualTo(Priority.ESSENTIAL)
    assertThat(fakeEventLogger.getMostRecentEvent().context.activityContextCase)
      .isEqualTo(OPEN_QUESTION_PLAYER)
  }

  @Test
  fun testController_logTransitionEvent_withExplorationContext_checkLogsEvent() {
    analyticsController.logTransitionEvent(
      TEST_TIMESTAMP,
      oppiaLogger.createOpenExplorationActivityContext(
        TEST_TOPIC_ID,
        TEST_STORY_ID,
        TEST_EXPLORATION_ID
      )
    )

    assertThat(fakeEventLogger.getMostRecentEvent().timestamp).isEqualTo(TEST_TIMESTAMP)
    // ESSENTIAL priority confirms that the event logged is a transition event.
    assertThat(fakeEventLogger.getMostRecentEvent().priority).isEqualTo(Priority.ESSENTIAL)
    assertThat(fakeEventLogger.getMostRecentEvent().context.activityContextCase)
      .isEqualTo(OPEN_EXPLORATION_ACTIVITY)
  }

  @Test
  fun testController_logTransitionEvent_withOpenInfoTabContext_checkLogsEvent() {
    analyticsController.logTransitionEvent(
      TEST_TIMESTAMP,
      oppiaLogger.createOpenInfoTabContext(TEST_TOPIC_ID)
    )

    assertThat(fakeEventLogger.getMostRecentEvent().timestamp).isEqualTo(TEST_TIMESTAMP)
    // OPTIONAL priority confirms that the event logged is a click event.
    assertThat(fakeEventLogger.getMostRecentEvent().priority).isEqualTo(Priority.ESSENTIAL)
    assertThat(fakeEventLogger.getMostRecentEvent().context.activityContextCase)
      .isEqualTo(OPEN_INFO_TAB)
  }

  @Test
  fun testController_logTransitionEvent_withOpenPracticeTabContext_checkLogsEvent() {
    analyticsController.logTransitionEvent(
      TEST_TIMESTAMP,
      oppiaLogger.createOpenPracticeTabContext(TEST_TOPIC_ID)
    )

    assertThat(fakeEventLogger.getMostRecentEvent().timestamp).isEqualTo(TEST_TIMESTAMP)
    // OPTIONAL priority confirms that the event logged is a click event.
    assertThat(fakeEventLogger.getMostRecentEvent().priority).isEqualTo(Priority.ESSENTIAL)
    assertThat(fakeEventLogger.getMostRecentEvent().context.activityContextCase)
      .isEqualTo(OPEN_PRACTICE_TAB)
  }

  @Test
  fun testController_logTransitionEvent_withOpenLessonsTabContext_checkLogsEvent() {
    analyticsController.logTransitionEvent(
      TEST_TIMESTAMP,
      oppiaLogger.createOpenLessonsTabContext(TEST_TOPIC_ID)
    )

    assertThat(fakeEventLogger.getMostRecentEvent().timestamp).isEqualTo(TEST_TIMESTAMP)
    // OPTIONAL priority confirms that the event logged is a click event.
    assertThat(fakeEventLogger.getMostRecentEvent().priority).isEqualTo(Priority.ESSENTIAL)
    assertThat(fakeEventLogger.getMostRecentEvent().context.activityContextCase)
      .isEqualTo(OPEN_LESSONS_TAB)
  }

  @Test
  fun testController_logTransitionEvent_withOpenRevisionTabContext_checkLogsEvent() {
    analyticsController.logTransitionEvent(
      TEST_TIMESTAMP,
      oppiaLogger.createOpenRevisionTabContext(TEST_TOPIC_ID)
    )

    assertThat(fakeEventLogger.getMostRecentEvent().timestamp).isEqualTo(TEST_TIMESTAMP)
    // OPTIONAL priority confirms that the event logged is a click event.
    assertThat(fakeEventLogger.getMostRecentEvent().priority).isEqualTo(Priority.ESSENTIAL)
    assertThat(fakeEventLogger.getMostRecentEvent().context.activityContextCase)
      .isEqualTo(OPEN_REVISION_TAB)
  }

  @Test
  fun testController_logTransitionEvent_withStoryContext_checkLogsEvent() {
    analyticsController.logTransitionEvent(
      TEST_TIMESTAMP,
      oppiaLogger.createOpenStoryActivityContext(TEST_TOPIC_ID, TEST_STORY_ID)
    )

    assertThat(fakeEventLogger.getMostRecentEvent().timestamp).isEqualTo(TEST_TIMESTAMP)
    // ESSENTIAL priority confirms that the event logged is a transition event.
    assertThat(fakeEventLogger.getMostRecentEvent().priority).isEqualTo(Priority.ESSENTIAL)
    assertThat(fakeEventLogger.getMostRecentEvent().context.activityContextCase)
      .isEqualTo(OPEN_STORY_ACTIVITY)
  }

  @Test
  fun testController_logTransitionEvent_withRevisionContext_checkLogsEvent() {
    analyticsController.logTransitionEvent(
      TEST_TIMESTAMP,
      oppiaLogger.createOpenRevisionCardContext(TEST_TOPIC_ID, TEST_SUB_TOPIC_ID)
    )

    assertThat(fakeEventLogger.getMostRecentEvent().timestamp).isEqualTo(TEST_TIMESTAMP)
    // ESSENTIAL priority confirms that the event logged is a transition event.
    assertThat(fakeEventLogger.getMostRecentEvent().priority).isEqualTo(Priority.ESSENTIAL)
    assertThat(fakeEventLogger.getMostRecentEvent().context.activityContextCase)
      .isEqualTo(OPEN_REVISION_CARD)
  }

  @Test
  fun testController_logTransitionEvent_withConceptCardContext_checkLogsEvent() {
    analyticsController.logTransitionEvent(
      TEST_TIMESTAMP,
      oppiaLogger.createOpenConceptCardContext(TEST_SKILL_ID)
    )

    assertThat(fakeEventLogger.getMostRecentEvent().timestamp).isEqualTo(TEST_TIMESTAMP)
    // ESSENTIAL priority confirms that the event logged is a transition event.
    assertThat(fakeEventLogger.getMostRecentEvent().priority).isEqualTo(Priority.ESSENTIAL)
    assertThat(fakeEventLogger.getMostRecentEvent().context.activityContextCase)
      .isEqualTo(OPEN_CONCEPT_CARD)
  }

  @Test
  fun testController_logClickEvent_withQuestionContext_checkLogsEvent() {
    analyticsController.logClickEvent(
      TEST_TIMESTAMP,
      oppiaLogger.createOpenQuestionPlayerContext(
        TEST_QUESTION_ID,
        listOf(
          TEST_SKILL_LIST_ID, TEST_SKILL_LIST_ID
        )
      )
    )

    assertThat(fakeEventLogger.getMostRecentEvent().timestamp).isEqualTo(TEST_TIMESTAMP)
    // OPTIONAL priority confirms that the event logged is a click event.
    assertThat(fakeEventLogger.getMostRecentEvent().priority).isEqualTo(Priority.OPTIONAL)
    assertThat(fakeEventLogger.getMostRecentEvent().context.activityContextCase)
      .isEqualTo(OPEN_QUESTION_PLAYER)
  }

  @Test
  fun testController_logClickEvent_withExplorationContext_checkLogsEvent() {
    analyticsController.logClickEvent(
      TEST_TIMESTAMP,
      oppiaLogger.createOpenExplorationActivityContext(
        TEST_TOPIC_ID,
        TEST_STORY_ID,
        TEST_EXPLORATION_ID
      )
    )

    assertThat(fakeEventLogger.getMostRecentEvent().timestamp).isEqualTo(TEST_TIMESTAMP)
    // OPTIONAL priority confirms that the event logged is a click event.
    assertThat(fakeEventLogger.getMostRecentEvent().priority).isEqualTo(Priority.OPTIONAL)
    assertThat(fakeEventLogger.getMostRecentEvent().context.activityContextCase)
      .isEqualTo(OPEN_EXPLORATION_ACTIVITY)
  }

  @Test
  fun testController_logClickEvent_withOpenInfoTabContext_checkLogsEvent() {
    analyticsController.logClickEvent(
      TEST_TIMESTAMP,
      oppiaLogger.createOpenInfoTabContext(TEST_TOPIC_ID)
    )

    assertThat(fakeEventLogger.getMostRecentEvent().timestamp).isEqualTo(TEST_TIMESTAMP)
    // OPTIONAL priority confirms that the event logged is a click event.
    assertThat(fakeEventLogger.getMostRecentEvent().priority).isEqualTo(Priority.OPTIONAL)
    assertThat(fakeEventLogger.getMostRecentEvent().context.activityContextCase)
      .isEqualTo(OPEN_INFO_TAB)
  }

  @Test
  fun testController_logClickEvent_withOpenPracticeTabContext_checkLogsEvent() {
    analyticsController.logClickEvent(
      TEST_TIMESTAMP,
      oppiaLogger.createOpenPracticeTabContext(TEST_TOPIC_ID)
    )

    assertThat(fakeEventLogger.getMostRecentEvent().timestamp).isEqualTo(TEST_TIMESTAMP)
    // OPTIONAL priority confirms that the event logged is a click event.
    assertThat(fakeEventLogger.getMostRecentEvent().priority).isEqualTo(Priority.OPTIONAL)
    assertThat(fakeEventLogger.getMostRecentEvent().context.activityContextCase)
      .isEqualTo(OPEN_PRACTICE_TAB)
  }

  @Test
  fun testController_logClickEvent_withOpenLessonsTabContext_checkLogsEvent() {
    analyticsController.logClickEvent(
      TEST_TIMESTAMP,
      oppiaLogger.createOpenLessonsTabContext(TEST_TOPIC_ID)
    )

    assertThat(fakeEventLogger.getMostRecentEvent().timestamp).isEqualTo(TEST_TIMESTAMP)
    // OPTIONAL priority confirms that the event logged is a click event.
    assertThat(fakeEventLogger.getMostRecentEvent().priority).isEqualTo(Priority.OPTIONAL)
    assertThat(fakeEventLogger.getMostRecentEvent().context.activityContextCase)
      .isEqualTo(OPEN_LESSONS_TAB)
  }

  @Test
  fun testController_logClickEvent_withOpenRevisionTabContext_checkLogsEvent() {
    analyticsController.logClickEvent(
      TEST_TIMESTAMP,
      oppiaLogger.createOpenRevisionTabContext(TEST_TOPIC_ID)
    )

    assertThat(fakeEventLogger.getMostRecentEvent().timestamp).isEqualTo(TEST_TIMESTAMP)
    // OPTIONAL priority confirms that the event logged is a click event.
    assertThat(fakeEventLogger.getMostRecentEvent().priority).isEqualTo(Priority.OPTIONAL)
    assertThat(fakeEventLogger.getMostRecentEvent().context.activityContextCase)
      .isEqualTo(OPEN_REVISION_TAB)
  }

  @Test
  fun testController_logClickEvent_withStoryContext_checkLogsEvent() {
    analyticsController.logClickEvent(
      TEST_TIMESTAMP,
      oppiaLogger.createOpenStoryActivityContext(TEST_TOPIC_ID, TEST_STORY_ID)
    )

    assertThat(fakeEventLogger.getMostRecentEvent().timestamp).isEqualTo(TEST_TIMESTAMP)
    // OPTIONAL priority confirms that the event logged is a click event.
    assertThat(fakeEventLogger.getMostRecentEvent().priority).isEqualTo(Priority.OPTIONAL)
    assertThat(fakeEventLogger.getMostRecentEvent().context.activityContextCase)
      .isEqualTo(OPEN_STORY_ACTIVITY)
  }

  @Test
  fun testController_logClickEvent_withRevisionContext_checkLogsEvent() {
    analyticsController.logClickEvent(
      TEST_TIMESTAMP,
      oppiaLogger.createOpenRevisionCardContext(TEST_TOPIC_ID, TEST_SUB_TOPIC_ID)
    )

    assertThat(fakeEventLogger.getMostRecentEvent().timestamp).isEqualTo(TEST_TIMESTAMP)
    // OPTIONAL priority confirms that the event logged is a click event.
    assertThat(fakeEventLogger.getMostRecentEvent().priority).isEqualTo(Priority.OPTIONAL)
    assertThat(fakeEventLogger.getMostRecentEvent().context.activityContextCase)
      .isEqualTo(OPEN_REVISION_CARD)
  }

  @Test
  fun testController_logClickEvent_withConceptCardContext_checkLogsEvent() {
    analyticsController.logClickEvent(
      TEST_TIMESTAMP,
      oppiaLogger.createOpenConceptCardContext(TEST_SKILL_ID)
    )

    assertThat(fakeEventLogger.getMostRecentEvent().timestamp).isEqualTo(TEST_TIMESTAMP)
    // OPTIONAL priority confirms that the event logged is a click event.
    assertThat(fakeEventLogger.getMostRecentEvent().priority).isEqualTo(Priority.OPTIONAL)
    assertThat(fakeEventLogger.getMostRecentEvent().context.activityContextCase)
      .isEqualTo(OPEN_CONCEPT_CARD)
  }

  // TODO(#3621): Addition of tests tracking behaviour of the controller after uploading of logs to the remote service.

  @Test
  fun testController_logTransitionEvent_withNoNetwork_checkLogsEventToStore() {
    networkConnectionUtil.setCurrentConnectionStatus(NONE)
    analyticsController.logTransitionEvent(
      TEST_TIMESTAMP,
      oppiaLogger.createOpenQuestionPlayerContext(
        TEST_QUESTION_ID,
        listOf(
          TEST_SKILL_LIST_ID, TEST_SKILL_LIST_ID
        )
      )
    )

    val eventLogs = analyticsController.getEventLogStore().toLiveData()
    eventLogs.observeForever(this.mockOppiaEventLogsObserver)
    testCoroutineDispatchers.advanceUntilIdle()
    verify(
      this.mockOppiaEventLogsObserver,
      atLeastOnce()
    ).onChanged(oppiaEventLogsResultCaptor.capture())

    val eventLog = oppiaEventLogsResultCaptor.value.getOrThrow().getEventLog(0)
    // ESSENTIAL priority confirms that the event logged is a transition event.
    assertThat(eventLog.priority).isEqualTo(Priority.ESSENTIAL)
    assertThat(eventLog.context.activityContextCase).isEqualTo(OPEN_QUESTION_PLAYER)
    assertThat(eventLog.timestamp).isEqualTo(TEST_TIMESTAMP)
  }

  @Test
  fun testController_logClickEvent_withNoNetwork_checkLogsEventToStore() {
    networkConnectionUtil.setCurrentConnectionStatus(NONE)
    analyticsController.logClickEvent(
      TEST_TIMESTAMP,
      oppiaLogger.createOpenQuestionPlayerContext(
        TEST_QUESTION_ID,
        listOf(
          TEST_SKILL_LIST_ID, TEST_SKILL_LIST_ID
        )
      )
    )

    val eventLogs = analyticsController.getEventLogStore().toLiveData()
    eventLogs.observeForever(this.mockOppiaEventLogsObserver)
    testCoroutineDispatchers.advanceUntilIdle()
    verify(
      this.mockOppiaEventLogsObserver,
      atLeastOnce()
    ).onChanged(oppiaEventLogsResultCaptor.capture())

    val eventLog = oppiaEventLogsResultCaptor.value.getOrThrow().getEventLog(0)
    // OPTIONAL priority confirms that the event logged is a click event.
    assertThat(eventLog.priority).isEqualTo(Priority.OPTIONAL)
    assertThat(eventLog.context.activityContextCase).isEqualTo(OPEN_QUESTION_PLAYER)
    assertThat(eventLog.timestamp).isEqualTo(TEST_TIMESTAMP)
  }

  @Test
  fun testController_logTransitionEvent_withNoNetwork_exceedLimit_checkEventLogStoreSize() {
    networkConnectionUtil.setCurrentConnectionStatus(NONE)
    logMultipleEvents()

    val eventLogs = analyticsController.getEventLogStore().toLiveData()
    eventLogs.observeForever(this.mockOppiaEventLogsObserver)
    testCoroutineDispatchers.advanceUntilIdle()
    verify(
      this.mockOppiaEventLogsObserver,
      atLeastOnce()
    ).onChanged(oppiaEventLogsResultCaptor.capture())

    val eventLogStoreSize = oppiaEventLogsResultCaptor.value.getOrThrow().eventLogList.size
    assertThat(eventLogStoreSize).isEqualTo(2)
  }

  @Test
  fun testController_logTransitionEvent_logClickEvent_withNoNetwork_checkOrderinCache() {
    networkConnectionUtil.setCurrentConnectionStatus(NONE)
    analyticsController.logClickEvent(
      TEST_TIMESTAMP,
      oppiaLogger.createOpenQuestionPlayerContext(
        TEST_QUESTION_ID,
        listOf(
          TEST_SKILL_LIST_ID, TEST_SKILL_LIST_ID
        )
      )
    )
    analyticsController.logTransitionEvent(
      TEST_TIMESTAMP,
      oppiaLogger.createOpenQuestionPlayerContext(
        TEST_QUESTION_ID,
        listOf(
          TEST_SKILL_LIST_ID, TEST_SKILL_LIST_ID
        )
      )
    )

    val eventLogs = analyticsController.getEventLogStore().toLiveData()
    eventLogs.observeForever(this.mockOppiaEventLogsObserver)
    testCoroutineDispatchers.advanceUntilIdle()
    verify(
      this.mockOppiaEventLogsObserver,
      atLeastOnce()
    ).onChanged(oppiaEventLogsResultCaptor.capture())

    val firstEventLog = oppiaEventLogsResultCaptor.value.getOrThrow().getEventLog(0)
    val secondEventLog = oppiaEventLogsResultCaptor.value.getOrThrow().getEventLog(1)

    // OPTIONAL priority confirms that the event logged is a click event.
    assertThat(firstEventLog.priority).isEqualTo(Priority.OPTIONAL)
    // ESSENTIAL priority confirms that the event logged is a transition event.
    assertThat(secondEventLog.priority).isEqualTo(Priority.ESSENTIAL)
  }

  @Test
  fun testController_logTransitionEvent_switchToNoNetwork_logClickEvent_checkManagement() {
    analyticsController.logTransitionEvent(
      TEST_TIMESTAMP,
      oppiaLogger.createOpenQuestionPlayerContext(
        TEST_QUESTION_ID,
        listOf(
          TEST_SKILL_LIST_ID, TEST_SKILL_LIST_ID
        )
      )
    )
    networkConnectionUtil.setCurrentConnectionStatus(NONE)
    analyticsController.logClickEvent(
      TEST_TIMESTAMP,
      oppiaLogger.createOpenQuestionPlayerContext(
        TEST_QUESTION_ID,
        listOf(
          TEST_SKILL_LIST_ID, TEST_SKILL_LIST_ID
        )
      )
    )

    val cachedEventLogs = analyticsController.getEventLogStore().toLiveData()
    cachedEventLogs.observeForever(this.mockOppiaEventLogsObserver)
    testCoroutineDispatchers.advanceUntilIdle()
    verify(
      this.mockOppiaEventLogsObserver,
      atLeastOnce()
    ).onChanged(oppiaEventLogsResultCaptor.capture())

    val uploadedEventLog = fakeEventLogger.getMostRecentEvent()
    val cachedEventLog = oppiaEventLogsResultCaptor.value.getOrThrow().getEventLog(0)

    // ESSENTIAL priority confirms that the event logged is a transition event.
    assertThat(uploadedEventLog.priority).isEqualTo(Priority.ESSENTIAL)
    assertThat(uploadedEventLog.context.activityContextCase).isEqualTo(OPEN_QUESTION_PLAYER)
    assertThat(uploadedEventLog.timestamp).isEqualTo(TEST_TIMESTAMP)

    // OPTIONAL priority confirms that the event logged is a click event.
    assertThat(cachedEventLog.priority).isEqualTo(Priority.OPTIONAL)
    assertThat(cachedEventLog.context.activityContextCase).isEqualTo(OPEN_QUESTION_PLAYER)
    assertThat(cachedEventLog.timestamp).isEqualTo(TEST_TIMESTAMP)
  }

  @Test
  fun testController_logEvents_exceedLimit_withNoNetwork_checkCorrectEventIsEvicted() {
    networkConnectionUtil.setCurrentConnectionStatus(NONE)
    logMultipleEvents()

    val cachedEventLogs = analyticsController.getEventLogStore().toLiveData()
    cachedEventLogs.observeForever(this.mockOppiaEventLogsObserver)
    testCoroutineDispatchers.advanceUntilIdle()
    verify(
      this.mockOppiaEventLogsObserver,
      atLeastOnce()
    ).onChanged(oppiaEventLogsResultCaptor.capture())

    val firstEventLog = oppiaEventLogsResultCaptor.value.getOrThrow().getEventLog(0)
    val secondEventLog = oppiaEventLogsResultCaptor.value.getOrThrow().getEventLog(1)
    val eventLogStoreSize = oppiaEventLogsResultCaptor.value.getOrThrow().eventLogList.size
    assertThat(eventLogStoreSize).isEqualTo(2)
    // In this case, 3 ESSENTIAL and 1 OPTIONAL event was logged. So while pruning, none of the retained logs should have OPTIONAL priority.
    assertThat(firstEventLog.priority).isNotEqualTo(Priority.OPTIONAL)
    assertThat(secondEventLog.priority).isNotEqualTo(Priority.OPTIONAL)
    // If we analyse the implementation of logMultipleEvents(), we can see that record pruning will begin from the logging of the third record.
    // At first, the second event log will be removed as it has OPTIONAL priority and the event logged at the third place will become the event record at the second place in the store.
    // When the forth event gets logged then the pruning will be purely based on timestamp of the event as both event logs have ESSENTIAL priority.
    // As the third event's timestamp was lesser than that of the first event, it will be pruned from the store and the forth event will become the second event in the store.
    assertThat(firstEventLog.timestamp).isEqualTo(1556094120000)
    assertThat(secondEventLog.timestamp).isEqualTo(1556094100000)
  }

  @Test
  fun testController_logEvent_withoutNetwork_verifySyncStatusEqualsNetworkError() {
    networkConnectionUtil.setCurrentConnectionStatus(NONE)
    analyticsController.logTransitionEvent(
      1556094120000,
      oppiaLogger.createOpenQuestionPlayerContext(
        TEST_QUESTION_ID,
        listOf(
          TEST_SKILL_LIST_ID, TEST_SKILL_LIST_ID
        )
      )
    )

    assertThat(fakeSyncStatusManager.getSyncStatusList().last()).isEqualTo(
      SyncStatusManager.SyncStatus.NETWORK_ERROR
    )
  }

  @Test
  fun testController_logEvent_afterCompletion_verifySyncStatusEqualsDataUploaded() {
    analyticsController.logTransitionEvent(
      1556094120000,
      oppiaLogger.createOpenQuestionPlayerContext(
        TEST_QUESTION_ID,
        listOf(
          TEST_SKILL_LIST_ID, TEST_SKILL_LIST_ID
        )
      )
    )

    assertThat(fakeSyncStatusManager.getSyncStatusList().last()).isEqualTo(
      SyncStatusManager.SyncStatus.DATA_UPLOADED
    )
  }

  @Test
  fun testController_logEvent_beforeCompletion_verifySyncStatusEqualsDataUploading() {
    analyticsController.logTransitionEvent(
      1556094120000,
      oppiaLogger.createOpenQuestionPlayerContext(
        TEST_QUESTION_ID,
        listOf(
          TEST_SKILL_LIST_ID, TEST_SKILL_LIST_ID
        )
      )
    )
    val syncStatusList = fakeSyncStatusManager.getSyncStatusList()
    assertThat(syncStatusList.size).isEqualTo(2)
    assertThat(syncStatusList[0]).isEqualTo(SyncStatusManager.SyncStatus.DATA_UPLOADING)
    assertThat(syncStatusList[1]).isEqualTo(SyncStatusManager.SyncStatus.DATA_UPLOADED)
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  private fun logMultipleEvents() {
    analyticsController.logTransitionEvent(
      1556094120000,
      oppiaLogger.createOpenQuestionPlayerContext(
        TEST_QUESTION_ID,
        listOf(
          TEST_SKILL_LIST_ID, TEST_SKILL_LIST_ID
        )
      )
    )

    analyticsController.logClickEvent(
      1556094110000,
      oppiaLogger.createOpenQuestionPlayerContext(
        TEST_QUESTION_ID,
        listOf(
          TEST_SKILL_LIST_ID, TEST_SKILL_LIST_ID
        )
      )
    )

    analyticsController.logTransitionEvent(
      1556093100000,
      oppiaLogger.createOpenQuestionPlayerContext(
        TEST_QUESTION_ID,
        listOf(
          TEST_SKILL_LIST_ID, TEST_SKILL_LIST_ID
        )
      )
    )

    analyticsController.logTransitionEvent(
      1556094100000,
      oppiaLogger.createOpenQuestionPlayerContext(
        TEST_QUESTION_ID,
        listOf(
          TEST_SKILL_LIST_ID, TEST_SKILL_LIST_ID
        )
      )
    )
  }

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }

    // TODO(#59): Either isolate these to their own shared test module, or use the real logging
    // module in tests to avoid needing to specify these settings for tests.
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
    fun provideSyncStatusManager(fakeSyncStatusManager: FakeSyncStatusManager): SyncStatusManager =
      fakeSyncStatusManager
  }

  @Module
  class TestLogStorageModule {

    @Provides
    @EventLogStorageCacheSize
    fun provideEventLogStorageCacheSize(): Int = 2
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, TestLogReportingModule::class, RobolectricModule::class,
      TestDispatcherModule::class, TestLogStorageModule::class,
      NetworkConnectionUtilDebugModule::class, LocaleProdModule::class, FakeOppiaClockModule::class,
      PlatformParameterModule::class, LoggingIdentifierModule::class,
      PlatformParameterSingletonModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(analyticsControllerTest: AnalyticsControllerTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerAnalyticsControllerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(analyticsControllerTest: AnalyticsControllerTest) {
      component.inject(analyticsControllerTest)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
