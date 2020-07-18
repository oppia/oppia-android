package org.oppia.domain.oppialogger.analytics

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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
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
import org.oppia.app.model.EventLog.Context.ActivityContextCase.ACTIVITYCONTEXT_NOT_SET
import org.oppia.app.model.EventLog.Context.ActivityContextCase.CONCEPT_CARD_CONTEXT
import org.oppia.app.model.EventLog.Context.ActivityContextCase.EXPLORATION_CONTEXT
import org.oppia.app.model.EventLog.Context.ActivityContextCase.QUESTION_CONTEXT
import org.oppia.app.model.EventLog.Context.ActivityContextCase.REVISION_CARD_CONTEXT
import org.oppia.app.model.EventLog.Context.ActivityContextCase.STORY_CONTEXT
import org.oppia.app.model.EventLog.Context.ActivityContextCase.TOPIC_CONTEXT
import org.oppia.app.model.EventLog.EventAction
import org.oppia.app.model.EventLog.Priority
import org.oppia.app.model.OppiaEventLogs
import org.oppia.testing.FakeEventLogger
import org.oppia.testing.TestCoroutineDispatchers
import org.oppia.testing.TestDispatcherModule
import org.oppia.testing.TestLogReportingModule
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.EnableConsoleLog
import org.oppia.util.logging.EnableFileLog
import org.oppia.util.logging.GlobalLogLevel
import org.oppia.util.logging.LogLevel
import org.oppia.util.networking.NetworkConnectionUtil
import org.oppia.util.networking.NetworkConnectionUtil.ConnectionStatus.NONE
import org.robolectric.annotation.Config
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton

const val TEST_TIMESTAMP = 1556094120000
const val TEST_TOPIC_ID = "test_topicId"
const val TEST_STORY_ID = "test_storyId"
const val TEST_EXPLORATION_ID = "test_explorationId"
const val TEST_QUESTION_ID = "test_questionId"
const val TEST_SKILL_ID = "test_skillId"
const val TEST_SKILL_LIST_ID = "test_skillListId"
const val TEST_SUB_TOPIC_ID = "test_subTopicId"

@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class AnalyticsControllerTest {

  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Inject
  lateinit var analyticsController: AnalyticsController

  @Inject
  lateinit var networkConnectionUtil: NetworkConnectionUtil

  @Inject
  lateinit var fakeEventLogger: FakeEventLogger

  @InternalCoroutinesApi
  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Mock
  lateinit var mockOppiaEventLogsObserver: Observer<AsyncResult<OppiaEventLogs>>

  @Captor
  lateinit var oppiaEventLogsResultCaptor: ArgumentCaptor<AsyncResult<OppiaEventLogs>>

  @Before
  fun setUp() {
    networkConnectionUtil = NetworkConnectionUtil(ApplicationProvider.getApplicationContext())
    setUpTestApplicationComponent()
  }

  @Test
  fun testController_logTransitionEvent_withQuestionContext_checkLogsEvent() {
    analyticsController.logTransitionEvent(
      TEST_TIMESTAMP,
      EventAction.EVENT_ACTION_UNSPECIFIED,
      analyticsController.createQuestionContext(
        TEST_QUESTION_ID,
        listOf(
          TEST_SKILL_LIST_ID, TEST_SKILL_LIST_ID
        )
      )
    )

    assertThat(fakeEventLogger.getMostRecentEvent().actionName)
      .isEqualTo(EventAction.EVENT_ACTION_UNSPECIFIED)
    assertThat(fakeEventLogger.getMostRecentEvent().timestamp).isEqualTo(TEST_TIMESTAMP)
    assertThat(fakeEventLogger.getMostRecentEvent().priority).isEqualTo(Priority.ESSENTIAL)
    assertThat(fakeEventLogger.getMostRecentEvent().context.activityContextCase)
      .isEqualTo(QUESTION_CONTEXT)
  }

  @Test
  fun testController_logTransitionEvent_withExplorationContext_checkLogsEvent() {
    analyticsController.logTransitionEvent(
      TEST_TIMESTAMP,
      EventAction.EVENT_ACTION_UNSPECIFIED,
      analyticsController.createExplorationContext(
        TEST_TOPIC_ID,
        TEST_STORY_ID,
        TEST_EXPLORATION_ID
      )
    )

    assertThat(fakeEventLogger.getMostRecentEvent().actionName)
      .isEqualTo(EventAction.EVENT_ACTION_UNSPECIFIED)
    assertThat(fakeEventLogger.getMostRecentEvent().timestamp).isEqualTo(TEST_TIMESTAMP)
    assertThat(fakeEventLogger.getMostRecentEvent().priority).isEqualTo(Priority.ESSENTIAL)
    assertThat(fakeEventLogger.getMostRecentEvent().context.activityContextCase)
      .isEqualTo(EXPLORATION_CONTEXT)
  }

  @Test
  fun testController_logTransitionEvent_withTopicContext_checkLogsEvent() {
    analyticsController.logTransitionEvent(
      TEST_TIMESTAMP,
      EventAction.EVENT_ACTION_UNSPECIFIED,
      analyticsController.createTopicContext(TEST_TOPIC_ID)
    )

    assertThat(fakeEventLogger.getMostRecentEvent().actionName)
      .isEqualTo(EventAction.EVENT_ACTION_UNSPECIFIED)
    assertThat(fakeEventLogger.getMostRecentEvent().timestamp).isEqualTo(TEST_TIMESTAMP)
    assertThat(fakeEventLogger.getMostRecentEvent().priority).isEqualTo(Priority.ESSENTIAL)
    assertThat(fakeEventLogger.getMostRecentEvent().context.activityContextCase)
      .isEqualTo(TOPIC_CONTEXT)
  }

  @Test
  fun testController_logTransitionEvent_withStoryContext_checkLogsEvent() {
    analyticsController.logTransitionEvent(
      TEST_TIMESTAMP,
      EventAction.EVENT_ACTION_UNSPECIFIED,
      analyticsController.createStoryContext(TEST_TOPIC_ID, TEST_STORY_ID)
    )

    assertThat(fakeEventLogger.getMostRecentEvent().actionName)
      .isEqualTo(EventAction.EVENT_ACTION_UNSPECIFIED)
    assertThat(fakeEventLogger.getMostRecentEvent().timestamp).isEqualTo(TEST_TIMESTAMP)
    assertThat(fakeEventLogger.getMostRecentEvent().priority).isEqualTo(Priority.ESSENTIAL)
    assertThat(fakeEventLogger.getMostRecentEvent().context.activityContextCase)
      .isEqualTo(STORY_CONTEXT)
  }

  @Test
  fun testController_logTransitionEvent_withRevisionContext_checkLogsEvent() {
    analyticsController.logTransitionEvent(
      TEST_TIMESTAMP,
      EventAction.EVENT_ACTION_UNSPECIFIED,
      analyticsController.createRevisionCardContext(TEST_TOPIC_ID, TEST_SUB_TOPIC_ID)
    )

    assertThat(fakeEventLogger.getMostRecentEvent().actionName)
      .isEqualTo(EventAction.EVENT_ACTION_UNSPECIFIED)
    assertThat(fakeEventLogger.getMostRecentEvent().timestamp).isEqualTo(TEST_TIMESTAMP)
    assertThat(fakeEventLogger.getMostRecentEvent().priority).isEqualTo(Priority.ESSENTIAL)
    assertThat(fakeEventLogger.getMostRecentEvent().context.activityContextCase)
      .isEqualTo(REVISION_CARD_CONTEXT)
  }

  @Test
  fun testController_logTransitionEvent_withConceptCardContext_checkLogsEvent() {
    analyticsController.logTransitionEvent(
      TEST_TIMESTAMP,
      EventAction.EVENT_ACTION_UNSPECIFIED,
      analyticsController.createConceptCardContext(TEST_SKILL_ID)
    )

    assertThat(fakeEventLogger.getMostRecentEvent().actionName)
      .isEqualTo(EventAction.EVENT_ACTION_UNSPECIFIED)
    assertThat(fakeEventLogger.getMostRecentEvent().timestamp).isEqualTo(TEST_TIMESTAMP)
    assertThat(fakeEventLogger.getMostRecentEvent().priority).isEqualTo(Priority.ESSENTIAL)
    assertThat(fakeEventLogger.getMostRecentEvent().context.activityContextCase)
      .isEqualTo(CONCEPT_CARD_CONTEXT)
  }

  @Test
  fun testController_logTransitionEvent_withNoContext_checkLogsEvent() {
    analyticsController.logTransitionEvent(
      TEST_TIMESTAMP,
      EventAction.EVENT_ACTION_UNSPECIFIED,
      null
    )

    assertThat(fakeEventLogger.getMostRecentEvent().actionName)
      .isEqualTo(EventAction.EVENT_ACTION_UNSPECIFIED)
    assertThat(fakeEventLogger.getMostRecentEvent().timestamp).isEqualTo(TEST_TIMESTAMP)
    assertThat(fakeEventLogger.getMostRecentEvent().priority).isEqualTo(Priority.ESSENTIAL)
    assertThat(fakeEventLogger.getMostRecentEvent().context.activityContextCase)
      .isEqualTo(ACTIVITYCONTEXT_NOT_SET)
  }

  @Test
  fun testController_logClickEvent_withQuestionContext_checkLogsEvent() {
    analyticsController.logClickEvent(
      TEST_TIMESTAMP,
      EventAction.EVENT_ACTION_UNSPECIFIED,
      analyticsController.createQuestionContext(
        TEST_QUESTION_ID,
        listOf(
          TEST_SKILL_LIST_ID, TEST_SKILL_LIST_ID
        )
      )
    )

    assertThat(fakeEventLogger.getMostRecentEvent().actionName)
      .isEqualTo(EventAction.EVENT_ACTION_UNSPECIFIED)
    assertThat(fakeEventLogger.getMostRecentEvent().timestamp).isEqualTo(TEST_TIMESTAMP)
    assertThat(fakeEventLogger.getMostRecentEvent().priority).isEqualTo(Priority.OPTIONAL)
    assertThat(fakeEventLogger.getMostRecentEvent().context.activityContextCase)
      .isEqualTo(QUESTION_CONTEXT)
  }

  @Test
  fun testController_logClickEvent_withExplorationContext_checkLogsEvent() {
    analyticsController.logClickEvent(
      TEST_TIMESTAMP,
      EventAction.EVENT_ACTION_UNSPECIFIED,
      analyticsController.createExplorationContext(
        TEST_TOPIC_ID,
        TEST_STORY_ID,
        TEST_EXPLORATION_ID
      )
    )

    assertThat(fakeEventLogger.getMostRecentEvent().actionName)
      .isEqualTo(EventAction.EVENT_ACTION_UNSPECIFIED)
    assertThat(fakeEventLogger.getMostRecentEvent().timestamp).isEqualTo(TEST_TIMESTAMP)
    assertThat(fakeEventLogger.getMostRecentEvent().priority).isEqualTo(Priority.OPTIONAL)
    assertThat(fakeEventLogger.getMostRecentEvent().context.activityContextCase)
      .isEqualTo(EXPLORATION_CONTEXT)
  }

  @Test
  fun testController_logClickEvent_withTopicContext_checkLogsEvent() {
    analyticsController.logClickEvent(
      TEST_TIMESTAMP,
      EventAction.EVENT_ACTION_UNSPECIFIED,
      analyticsController.createTopicContext(TEST_TOPIC_ID)
    )

    assertThat(fakeEventLogger.getMostRecentEvent().actionName)
      .isEqualTo(EventAction.EVENT_ACTION_UNSPECIFIED)
    assertThat(fakeEventLogger.getMostRecentEvent().timestamp).isEqualTo(TEST_TIMESTAMP)
    assertThat(fakeEventLogger.getMostRecentEvent().priority).isEqualTo(Priority.OPTIONAL)
    assertThat(fakeEventLogger.getMostRecentEvent().context.activityContextCase)
      .isEqualTo(TOPIC_CONTEXT)
  }

  @Test
  fun testController_logClickEvent_withStoryContext_checkLogsEvent() {
    analyticsController.logClickEvent(
      TEST_TIMESTAMP,
      EventAction.EVENT_ACTION_UNSPECIFIED,
      analyticsController.createStoryContext(TEST_TOPIC_ID, TEST_STORY_ID)
    )

    assertThat(fakeEventLogger.getMostRecentEvent().actionName)
      .isEqualTo(EventAction.EVENT_ACTION_UNSPECIFIED)
    assertThat(fakeEventLogger.getMostRecentEvent().timestamp).isEqualTo(TEST_TIMESTAMP)
    assertThat(fakeEventLogger.getMostRecentEvent().priority).isEqualTo(Priority.OPTIONAL)
    assertThat(fakeEventLogger.getMostRecentEvent().context.activityContextCase)
      .isEqualTo(STORY_CONTEXT)
  }

  @Test
  fun testController_logClickEvent_withRevisionContext_checkLogsEvent() {
    analyticsController.logClickEvent(
      TEST_TIMESTAMP,
      EventAction.EVENT_ACTION_UNSPECIFIED,
      analyticsController.createRevisionCardContext(TEST_TOPIC_ID, TEST_SUB_TOPIC_ID)
    )

    assertThat(fakeEventLogger.getMostRecentEvent().actionName)
      .isEqualTo(EventAction.EVENT_ACTION_UNSPECIFIED)
    assertThat(fakeEventLogger.getMostRecentEvent().timestamp).isEqualTo(TEST_TIMESTAMP)
    assertThat(fakeEventLogger.getMostRecentEvent().priority).isEqualTo(Priority.OPTIONAL)
    assertThat(fakeEventLogger.getMostRecentEvent().context.activityContextCase)
      .isEqualTo(REVISION_CARD_CONTEXT)
  }

  @Test
  fun testController_logClickEvent_withConceptCardContext_checkLogsEvent() {
    analyticsController.logClickEvent(
      TEST_TIMESTAMP,
      EventAction.EVENT_ACTION_UNSPECIFIED,
      analyticsController.createConceptCardContext(TEST_SKILL_ID)
    )

    assertThat(fakeEventLogger.getMostRecentEvent().actionName)
      .isEqualTo(EventAction.EVENT_ACTION_UNSPECIFIED)
    assertThat(fakeEventLogger.getMostRecentEvent().timestamp).isEqualTo(TEST_TIMESTAMP)
    assertThat(fakeEventLogger.getMostRecentEvent().priority).isEqualTo(Priority.OPTIONAL)
    assertThat(fakeEventLogger.getMostRecentEvent().context.activityContextCase)
      .isEqualTo(CONCEPT_CARD_CONTEXT)
  }

  @Test
  fun testController_logClickEvent_withNoContext_checkLogsEvent() {
    analyticsController.logClickEvent(
      TEST_TIMESTAMP,
      EventAction.EVENT_ACTION_UNSPECIFIED,
      null
    )

    assertThat(fakeEventLogger.getMostRecentEvent().actionName)
      .isEqualTo(EventAction.EVENT_ACTION_UNSPECIFIED)
    assertThat(fakeEventLogger.getMostRecentEvent().timestamp).isEqualTo(TEST_TIMESTAMP)
    assertThat(fakeEventLogger.getMostRecentEvent().priority).isEqualTo(Priority.OPTIONAL)
    assertThat(fakeEventLogger.getMostRecentEvent().context.activityContextCase)
      .isEqualTo(ACTIVITYCONTEXT_NOT_SET)
  }

  @Test
  fun testController_createExplorationContext_returnsCorrectExplorationContext() {
    val eventContext = analyticsController.createExplorationContext(
      TEST_TOPIC_ID,
      TEST_STORY_ID,
      TEST_EXPLORATION_ID
    )

    assertThat(eventContext.activityContextCase).isEqualTo(EXPLORATION_CONTEXT)
    assertThat(eventContext.explorationContext.topicId).matches(TEST_TOPIC_ID)
    assertThat(eventContext.explorationContext.storyId).matches(TEST_STORY_ID)
    assertThat(eventContext.explorationContext.explorationId).matches(TEST_EXPLORATION_ID)
  }

  @Test
  fun testController_createQuestionContext_returnsCorrectQuestionContext() {
    val eventContext = analyticsController.createQuestionContext(
      TEST_QUESTION_ID,
      listOf(TEST_SKILL_LIST_ID, TEST_SKILL_LIST_ID)
    )

    assertThat(eventContext.activityContextCase).isEqualTo(QUESTION_CONTEXT)
    assertThat(eventContext.questionContext.questionId).matches(TEST_QUESTION_ID)
    assertThat(eventContext.questionContext.skillIdList)
      .containsAllIn(arrayOf(TEST_SKILL_LIST_ID, TEST_SKILL_LIST_ID))
  }

  @Test
  fun testController_createStoryContext_returnsCorrectStoryContext() {
    val eventContext = analyticsController.createStoryContext(
      TEST_TOPIC_ID,
      TEST_STORY_ID
    )

    assertThat(eventContext.activityContextCase).isEqualTo(STORY_CONTEXT)
    assertThat(eventContext.storyContext.topicId).matches(TEST_TOPIC_ID)
    assertThat(eventContext.storyContext.storyId).matches(TEST_STORY_ID)
  }

  @Test
  fun testController_createTopicContext_returnsCorrectTopicContext() {
    val eventContext = analyticsController.createTopicContext(TEST_TOPIC_ID)

    assertThat(eventContext.activityContextCase).isEqualTo(TOPIC_CONTEXT)
    assertThat(eventContext.topicContext.topicId).matches(TEST_TOPIC_ID)
  }

  @Test
  fun testController_createConceptCardContext_returnsCorrectConceptCardContext() {
    val eventContext = analyticsController.createConceptCardContext(TEST_SKILL_ID)

    assertThat(eventContext.activityContextCase).isEqualTo(CONCEPT_CARD_CONTEXT)
    assertThat(eventContext.conceptCardContext.skillId).matches(TEST_SKILL_ID)
  }

  @Test
  fun testController_createRevisionCardContext_returnsCorrectRevisionCardContext() {
    val eventContext =
      analyticsController.createRevisionCardContext(TEST_TOPIC_ID, TEST_SUB_TOPIC_ID)

    assertThat(eventContext.activityContextCase).isEqualTo(REVISION_CARD_CONTEXT)
    assertThat(eventContext.revisionCardContext.topicId).matches(TEST_TOPIC_ID)
    assertThat(eventContext.revisionCardContext.subTopicId).matches(TEST_SUB_TOPIC_ID)
  }

  @ExperimentalCoroutinesApi
  @InternalCoroutinesApi
  @Test
  fun testController_logTransitionEvent_withNoNetwork_checkLogsEventToStore() {
    networkConnectionUtil.setCurrentConnectionStatus(NONE)
    analyticsController.logTransitionEvent(
      TEST_TIMESTAMP,
      EventAction.EVENT_ACTION_UNSPECIFIED,
      analyticsController.createQuestionContext(
        TEST_QUESTION_ID,
        listOf(
          TEST_SKILL_LIST_ID, TEST_SKILL_LIST_ID
        )
      )
    )

    val eventLogs = analyticsController.getEventLogs()
    eventLogs.observeForever(this.mockOppiaEventLogsObserver)
    testCoroutineDispatchers.advanceUntilIdle()
    verify(
      this.mockOppiaEventLogsObserver,
      atLeastOnce()
    ).onChanged(oppiaEventLogsResultCaptor.capture())

    val eventLog = oppiaEventLogsResultCaptor.value.getOrThrow().getEventLog(0)
    assertThat(eventLog.priority).isEqualTo(Priority.ESSENTIAL)
    assertThat(eventLog.context.activityContextCase).isEqualTo(QUESTION_CONTEXT)
    assertThat(eventLog.timestamp).isEqualTo(TEST_TIMESTAMP)
    assertThat(eventLog.actionName).isEqualTo(EventAction.EVENT_ACTION_UNSPECIFIED)
  }

  @ExperimentalCoroutinesApi
  @InternalCoroutinesApi
  @Test
  fun testController_logClickEvent_withNoNetwork_checkLogsEventToStore() {
    networkConnectionUtil.setCurrentConnectionStatus(NONE)
    analyticsController.logClickEvent(
      TEST_TIMESTAMP,
      EventAction.EVENT_ACTION_UNSPECIFIED,
      analyticsController.createQuestionContext(
        TEST_QUESTION_ID,
        listOf(
          TEST_SKILL_LIST_ID, TEST_SKILL_LIST_ID
        )
      )
    )

    val eventLogs = analyticsController.getEventLogs()
    eventLogs.observeForever(this.mockOppiaEventLogsObserver)
    testCoroutineDispatchers.advanceUntilIdle()
    verify(
      this.mockOppiaEventLogsObserver,
      atLeastOnce()
    ).onChanged(oppiaEventLogsResultCaptor.capture())

    val eventLog = oppiaEventLogsResultCaptor.value.getOrThrow().getEventLog(0)
    assertThat(eventLog.priority).isEqualTo(Priority.OPTIONAL)
    assertThat(eventLog.context.activityContextCase).isEqualTo(QUESTION_CONTEXT)
    assertThat(eventLog.timestamp).isEqualTo(TEST_TIMESTAMP)
    assertThat(eventLog.actionName).isEqualTo(EventAction.EVENT_ACTION_UNSPECIFIED)
  }

  @ExperimentalCoroutinesApi
  @InternalCoroutinesApi
  @Test
  fun testController_logTransitionEvent_withNoNetwork_exceedLimit_checkEventLogStoreSize() {
    networkConnectionUtil.setCurrentConnectionStatus(NONE)
    logMultipleEvents()

    val eventLogs = analyticsController.getEventLogs()
    eventLogs.observeForever(this.mockOppiaEventLogsObserver)
    testCoroutineDispatchers.advanceUntilIdle()
    verify(
      this.mockOppiaEventLogsObserver,
      atLeastOnce()
    ).onChanged(oppiaEventLogsResultCaptor.capture())

    val eventLogStoreSize = oppiaEventLogsResultCaptor.value.getOrThrow().eventLogList.size
    assertThat(eventLogStoreSize).isEqualTo(2)
  }

  @ExperimentalCoroutinesApi
  @InternalCoroutinesApi
  @Test
  fun testController_logTransitionEvent_logClickEvent_withNoNetwork_checkOrderinCache() {
    networkConnectionUtil.setCurrentConnectionStatus(NONE)
    analyticsController.logClickEvent(
      TEST_TIMESTAMP,
      EventAction.EVENT_ACTION_UNSPECIFIED,
      analyticsController.createQuestionContext(
        TEST_QUESTION_ID,
        listOf(
          TEST_SKILL_LIST_ID, TEST_SKILL_LIST_ID
        )
      )
    )
    analyticsController.logTransitionEvent(
      TEST_TIMESTAMP,
      EventAction.EVENT_ACTION_UNSPECIFIED,
      analyticsController.createQuestionContext(
        TEST_QUESTION_ID,
        listOf(
          TEST_SKILL_LIST_ID, TEST_SKILL_LIST_ID
        )
      )
    )

    val eventLogs = analyticsController.getEventLogs()
    eventLogs.observeForever(this.mockOppiaEventLogsObserver)
    testCoroutineDispatchers.advanceUntilIdle()
    verify(
      this.mockOppiaEventLogsObserver,
      atLeastOnce()
    ).onChanged(oppiaEventLogsResultCaptor.capture())

    val firstEventLog = oppiaEventLogsResultCaptor.value.getOrThrow().getEventLog(0)
    val secondEventLog = oppiaEventLogsResultCaptor.value.getOrThrow().getEventLog(1)

    assertThat(firstEventLog.priority).isEqualTo(Priority.OPTIONAL)
    assertThat(secondEventLog.priority).isEqualTo(Priority.ESSENTIAL)
  }

  @ExperimentalCoroutinesApi
  @InternalCoroutinesApi
  @Test
  fun testController_logTransitionEvent_switchToNoNetwork_logClickEvent_checkManagement() {
    analyticsController.logTransitionEvent(
      TEST_TIMESTAMP,
      EventAction.EVENT_ACTION_UNSPECIFIED,
      analyticsController.createQuestionContext(
        TEST_QUESTION_ID,
        listOf(
          TEST_SKILL_LIST_ID, TEST_SKILL_LIST_ID
        )
      )
    )
    networkConnectionUtil.setCurrentConnectionStatus(NONE)
    analyticsController.logClickEvent(
      TEST_TIMESTAMP,
      EventAction.EVENT_ACTION_UNSPECIFIED,
      analyticsController.createQuestionContext(
        TEST_QUESTION_ID,
        listOf(
          TEST_SKILL_LIST_ID, TEST_SKILL_LIST_ID
        )
      )
    )

    val cachedEventLogs = analyticsController.getEventLogs()
    cachedEventLogs.observeForever(this.mockOppiaEventLogsObserver)
    testCoroutineDispatchers.advanceUntilIdle()
    verify(
      this.mockOppiaEventLogsObserver,
      atLeastOnce()
    ).onChanged(oppiaEventLogsResultCaptor.capture())

    val uploadedEventLog = fakeEventLogger.getMostRecentEvent()
    val cachedEventLog = oppiaEventLogsResultCaptor.value.getOrThrow().getEventLog(0)

    assertThat(uploadedEventLog.priority).isEqualTo(Priority.ESSENTIAL)
    assertThat(uploadedEventLog.context.activityContextCase).isEqualTo(QUESTION_CONTEXT)
    assertThat(uploadedEventLog.timestamp).isEqualTo(TEST_TIMESTAMP)
    assertThat(uploadedEventLog.actionName).isEqualTo(EventAction.EVENT_ACTION_UNSPECIFIED)

    assertThat(cachedEventLog.priority).isEqualTo(Priority.OPTIONAL)
    assertThat(cachedEventLog.context.activityContextCase).isEqualTo(QUESTION_CONTEXT)
    assertThat(cachedEventLog.timestamp).isEqualTo(TEST_TIMESTAMP)
    assertThat(cachedEventLog.actionName).isEqualTo(EventAction.EVENT_ACTION_UNSPECIFIED)
  }

  @ExperimentalCoroutinesApi
  @InternalCoroutinesApi
  @Test
  fun testController_logEvents_exceedLimit_withNoNetwork_checkCorrectEventIsEvicted() {
    networkConnectionUtil.setCurrentConnectionStatus(NONE)
    logMultipleEvents()

    val cachedEventLogs = analyticsController.getEventLogs()
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
    assertThat(firstEventLog.priority).isNotEqualTo(Priority.OPTIONAL)
    assertThat(secondEventLog.priority).isNotEqualTo(Priority.OPTIONAL)
    assertThat(firstEventLog.timestamp).isEqualTo(1556094120000)
    assertThat(secondEventLog.timestamp).isEqualTo(1556094100000)
  }

  private fun setUpTestApplicationComponent() {
    DaggerAnalyticsControllerTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  private fun logMultipleEvents() {
    analyticsController.logTransitionEvent(
      1556094120000,
      EventAction.EVENT_ACTION_UNSPECIFIED,
      analyticsController.createQuestionContext(
        TEST_QUESTION_ID,
        listOf(
          TEST_SKILL_LIST_ID, TEST_SKILL_LIST_ID
        )
      )
    )

    analyticsController.logClickEvent(
      1556094110000,
      EventAction.EVENT_ACTION_UNSPECIFIED,
      analyticsController.createQuestionContext(
        TEST_QUESTION_ID,
        listOf(
          TEST_SKILL_LIST_ID, TEST_SKILL_LIST_ID
        )
      )
    )

    analyticsController.logTransitionEvent(
      1556093100000,
      EventAction.EVENT_ACTION_UNSPECIFIED,
      analyticsController.createQuestionContext(
        TEST_QUESTION_ID,
        listOf(
          TEST_SKILL_LIST_ID, TEST_SKILL_LIST_ID
        )
      )
    )

    analyticsController.logTransitionEvent(
      1556094100000,
      EventAction.EVENT_ACTION_UNSPECIFIED,
      analyticsController.createQuestionContext(
        TEST_QUESTION_ID,
        listOf(
          TEST_SKILL_LIST_ID, TEST_SKILL_LIST_ID
        )
      )
    )
  }

  @Qualifier
  annotation class TestDispatcher

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
  }

  @Qualifier
  annotation class EventLogStorageCacheSize

  @Module
  class TestLogStorageModule {
    @Provides
    @org.oppia.domain.oppialogger.EventLogStorageCacheSize
    fun provideEventLogStorageCacheSize(): Int = 2
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, TestLogReportingModule::class,
      TestDispatcherModule::class, TestLogStorageModule::class

    ]
  )
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(analyticsControllerTest: AnalyticsControllerTest)
  }
}
