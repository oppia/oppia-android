package org.oppia.domain.analytics

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
import org.oppia.app.model.EventLog.Context.ActivityContextCase.ACTIVITYCONTEXT_NOT_SET
import org.oppia.app.model.EventLog.Context.ActivityContextCase.EXPLORATION_CONTEXT
import org.oppia.app.model.EventLog.Context.ActivityContextCase.QUESTION_CONTEXT
import org.oppia.app.model.EventLog.Context.ActivityContextCase.STORY_CONTEXT
import org.oppia.app.model.EventLog.Context.ActivityContextCase.TOPIC_CONTEXT
import org.oppia.app.model.EventLog.EventAction
import org.oppia.app.model.EventLog.Priority
import org.oppia.testing.FakeEventLogger
import org.oppia.testing.TestLogReportingModule
import org.robolectric.annotation.Config
import javax.inject.Inject
import javax.inject.Singleton

@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class AnalyticsControllerTest {

  @Inject
  lateinit var analyticsController: AnalyticsController

  @Inject
  lateinit var fakeEventLogger: FakeEventLogger

  @Test
  fun testController_logTransitionEvent_withQuestionContext_checkLogsEvent() {
    analyticsController.logTransitionEvent(
      ApplicationProvider.getApplicationContext(),
      TEST_TIMESTAMP,
      EventAction.EVENT_ACTION_UNSPECIFIED,
      analyticsController.createQuestionContext(TEST_TOPIC_ID, TEST_QUESTION_ID)
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
      ApplicationProvider.getApplicationContext(),
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
      ApplicationProvider.getApplicationContext(),
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
      ApplicationProvider.getApplicationContext(),
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
  fun testController_logTransitionEvent_withNoContext_checkLogsEvent() {
    analyticsController.logTransitionEvent(
      ApplicationProvider.getApplicationContext(),
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
      ApplicationProvider.getApplicationContext(),
      TEST_TIMESTAMP,
      EventAction.EVENT_ACTION_UNSPECIFIED,
      analyticsController.createQuestionContext(TEST_TOPIC_ID, TEST_QUESTION_ID)
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
      ApplicationProvider.getApplicationContext(),
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
      ApplicationProvider.getApplicationContext(),
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
      ApplicationProvider.getApplicationContext(),
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
  fun testController_logClickEvent_withNoContext_checkLogsEvent() {
    analyticsController.logClickEvent(
      ApplicationProvider.getApplicationContext(),
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
  fun testController_createExplorationContext_isSuccessful() {
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
  fun testController_createQuestionContext_isSuccessful() {
    val eventContext = analyticsController.createQuestionContext(TEST_TOPIC_ID, TEST_QUESTION_ID)

    assertThat(eventContext.activityContextCase).isEqualTo(QUESTION_CONTEXT)
    assertThat(eventContext.questionContext.topicId).matches(TEST_TOPIC_ID)
    assertThat(eventContext.questionContext.questionId).matches(TEST_QUESTION_ID)
  }

  @Test
  fun testController_createStoryContext_isSuccessful() {
    val eventContext = analyticsController.createStoryContext(TEST_TOPIC_ID, TEST_STORY_ID)

    assertThat(eventContext.activityContextCase).isEqualTo(STORY_CONTEXT)
    assertThat(eventContext.storyContext.topicId).matches(TEST_TOPIC_ID)
    assertThat(eventContext.storyContext.storyId).matches(TEST_STORY_ID)
  }

  @Test
  fun testController_createTopicContext_isSuccessful() {
    val eventContext = analyticsController.createTopicContext(TEST_TOPIC_ID)

    assertThat(eventContext.activityContextCase).isEqualTo(TOPIC_CONTEXT)
    assertThat(eventContext.topicContext.topicId).matches(TEST_TOPIC_ID)
  }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  private fun setUpTestApplicationComponent() {
    DaggerAnalyticsControllerTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
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
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(modules = [TestModule::class, TestLogReportingModule::class])
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
