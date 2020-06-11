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
import org.oppia.app.model.Priority
import org.oppia.testing.FakeEventLogger
import org.oppia.testing.TestLogReportingModule
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class AnalyticsControllerTest {

  @Inject lateinit var analyticsController: AnalyticsController
  @Inject lateinit var fakeEventLogger: FakeEventLogger

  @Test
  fun testController_LogTransitionEvent_checkLogsEvent() {
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

    assertThat(fakeEventLogger.getMostRecentEvent().title).matches(EventAction.EVENT_ACTION_UNSPECIFIED.toString())
    assertThat(fakeEventLogger.getMostRecentEvent().bundle.get(TIMESTAMP_KEY)).isEqualTo(TEST_TIMESTAMP)
    assertThat(fakeEventLogger.getMostRecentEvent().bundle.get(TOPIC_ID_KEY)).isEqualTo(TEST_TOPIC_ID)
    assertThat(fakeEventLogger.getMostRecentEvent().bundle.get(STORY_ID_KEY)).isEqualTo(TEST_STORY_ID)
    assertThat(fakeEventLogger.getMostRecentEvent().bundle.get(EXPLORATION_ID_KEY)).isEqualTo(TEST_EXPLORATION_ID)
    assertThat(fakeEventLogger.getMostRecentEvent().bundle.get(QUESTION_ID_KEY)).isEqualTo(TEST_QUESTION_ID)
    assertThat(fakeEventLogger.getMostRecentEvent().bundle.get(PRIORITY_KEY)).isEqualTo(Priority.ESSENTIAL.toString())
  }

  @Test
  fun testController_LogClickEvent_checkLogsEvent() {
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

    assertThat(fakeEventLogger.getMostRecentEvent().title).matches(EventAction.EVENT_ACTION_UNSPECIFIED.toString())
    assertThat(fakeEventLogger.getMostRecentEvent().bundle.get(TIMESTAMP_KEY)).isEqualTo(TEST_TIMESTAMP)
    assertThat(fakeEventLogger.getMostRecentEvent().bundle.get(TOPIC_ID_KEY)).isEqualTo(TEST_TOPIC_ID)
    assertThat(fakeEventLogger.getMostRecentEvent().bundle.get(STORY_ID_KEY)).isEqualTo(TEST_STORY_ID)
    assertThat(fakeEventLogger.getMostRecentEvent().bundle.get(EXPLORATION_ID_KEY)).isEqualTo(TEST_EXPLORATION_ID)
    assertThat(fakeEventLogger.getMostRecentEvent().bundle.get(QUESTION_ID_KEY)).isEqualTo(TEST_QUESTION_ID)
    assertThat(fakeEventLogger.getMostRecentEvent().bundle.get(PRIORITY_KEY)).isEqualTo(Priority.OPTIONAL.toString())
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
