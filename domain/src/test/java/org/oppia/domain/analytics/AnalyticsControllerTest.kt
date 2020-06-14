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
import org.oppia.app.model.EventLog.Context.ActivityContextCase.EXPLORATION_CONTEXT
import org.oppia.app.model.EventLog.Context.ActivityContextCase.QUESTION_CONTEXT
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
  fun testController_LogTransitionEvent_withQuestionContext_checkLogsEvent() {
    fakeEventLogger.clearAllEvents()

    analyticsController.logTransitionEvent(
      ApplicationProvider.getApplicationContext(),
      TEST_TIMESTAMP,
      EventAction.EVENT_ACTION_UNSPECIFIED,
      TEST_TOPIC_ID,
      TEST_STORY_ID,
      TEST_EXPLORATION_ID,
      TEST_QUESTION_ID
    )

    assertThat(fakeEventLogger.getMostRecentEvent().actionName)
      .isEqualTo(EventAction.EVENT_ACTION_UNSPECIFIED)
    assertThat(fakeEventLogger.getMostRecentEvent().timestamp).isEqualTo(TEST_TIMESTAMP)
    assertThat(fakeEventLogger.getMostRecentEvent().priority).isEqualTo(Priority.ESSENTIAL)
    assertThat(fakeEventLogger.getMostRecentEvent().context.activityContextCase)
      .isEqualTo(QUESTION_CONTEXT)
  }

  @Test
  fun testController_LogTransitionEvent_withExplorationContext_checkLogsEvent() {
    fakeEventLogger.clearAllEvents()

    analyticsController.logTransitionEvent(
      ApplicationProvider.getApplicationContext(),
      TEST_TIMESTAMP,
      EventAction.EVENT_ACTION_UNSPECIFIED,
      TEST_TOPIC_ID,
      TEST_STORY_ID,
      TEST_EXPLORATION_ID,
      null
    )

    assertThat(fakeEventLogger.getMostRecentEvent().actionName)
      .isEqualTo(EventAction.EVENT_ACTION_UNSPECIFIED)
    assertThat(fakeEventLogger.getMostRecentEvent().timestamp).isEqualTo(TEST_TIMESTAMP)
    assertThat(fakeEventLogger.getMostRecentEvent().priority).isEqualTo(Priority.ESSENTIAL)
    assertThat(fakeEventLogger.getMostRecentEvent().context.activityContextCase)
      .isEqualTo(EXPLORATION_CONTEXT)
  }

  @Test
  fun testController_LogClickEvent_withQuestionContext_checkLogsEvent() {
    fakeEventLogger.clearAllEvents()

    analyticsController.logClickEvent(
      ApplicationProvider.getApplicationContext(),
      TEST_TIMESTAMP,
      EventAction.EVENT_ACTION_UNSPECIFIED,
      TEST_TOPIC_ID,
      TEST_STORY_ID,
      TEST_EXPLORATION_ID,
      TEST_QUESTION_ID
    )

    assertThat(fakeEventLogger.getMostRecentEvent().actionName)
      .isEqualTo(EventAction.EVENT_ACTION_UNSPECIFIED)
    assertThat(fakeEventLogger.getMostRecentEvent().timestamp).isEqualTo(TEST_TIMESTAMP)
    assertThat(fakeEventLogger.getMostRecentEvent().priority).isEqualTo(Priority.OPTIONAL)
    assertThat(fakeEventLogger.getMostRecentEvent().context.activityContextCase)
      .isEqualTo(QUESTION_CONTEXT)
  }

  @Test
  fun testController_LogClickEvent_withExplorationContext_checkLogsEvent() {
    fakeEventLogger.clearAllEvents()

    analyticsController.logClickEvent(
      ApplicationProvider.getApplicationContext(),
      TEST_TIMESTAMP,
      EventAction.EVENT_ACTION_UNSPECIFIED,
      TEST_TOPIC_ID,
      TEST_STORY_ID,
      TEST_EXPLORATION_ID,
      null
    )

    assertThat(fakeEventLogger.getMostRecentEvent().actionName)
      .isEqualTo(EventAction.EVENT_ACTION_UNSPECIFIED)
    assertThat(fakeEventLogger.getMostRecentEvent().timestamp).isEqualTo(TEST_TIMESTAMP)
    assertThat(fakeEventLogger.getMostRecentEvent().priority).isEqualTo(Priority.OPTIONAL)
    assertThat(fakeEventLogger.getMostRecentEvent().context.activityContextCase)
      .isEqualTo(EXPLORATION_CONTEXT)
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
