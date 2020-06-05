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
import javax.inject.Inject
import javax.inject.Singleton
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.model.EventAction
import org.oppia.testing.FakeEventLogger
import org.oppia.testing.TestLogReportingModule
import org.robolectric.annotation.Config

private const val TIMESTAMP = 1556094120000
private const val TOPIC_ID = "topicId"
private const val STORY_ID = "storyId"
private const val EXPLORATION_ID = "explorationId"
private const val QUESTION_ID = "questionId"

@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class AnalyticsControllerTest {

  @Inject lateinit var analyticsController: AnalyticsController
  private val fakeEventLogger = FakeEventLogger()

  @Test
  fun testController_clearList_LogTransitionEvent_checkLogsEvent() {
    fakeEventLogger.clearAllTitles()

    analyticsController.logTransitionEvent(
      ApplicationProvider.getApplicationContext(),
      TIMESTAMP,
      EventAction.UNKNOWN_EVENT_ACTION,
      TOPIC_ID,
      STORY_ID,
      EXPLORATION_ID,
      QUESTION_ID
    )

    assertThat(fakeEventLogger.getMostRecentTitle()).matches(EventAction.UNKNOWN_EVENT_ACTION.toString())
  }

  @Test
  fun testController_clearList_LogClickEvent_checkLogsEvent() {
    fakeEventLogger.clearAllTitles()

    analyticsController.logClickEvent(
      ApplicationProvider.getApplicationContext(),
      TIMESTAMP,
      EventAction.UNKNOWN_EVENT_ACTION,
      TOPIC_ID,
      STORY_ID,
      EXPLORATION_ID,
      QUESTION_ID
    )

    assertThat(fakeEventLogger.getMostRecentTitle()).matches(EventAction.UNKNOWN_EVENT_ACTION.toString())
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
