package org.oppia.util.logging.firebase

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
import org.mockito.MockitoAnnotations
import org.oppia.app.model.EventLog
import org.oppia.testing.FakeEventLogger
import org.oppia.testing.TestLogReportingModule
import org.robolectric.annotation.Config
import javax.inject.Inject
import javax.inject.Singleton

const val TEST_TIMESTAMP = 1556094120000
const val TEST_TOPIC_ID = "test_topicId"
const val TEST_STORY_ID = "test_storyId"
const val TEST_EXPLORATION_ID = "test_explorationId"
const val TEST_QUESTION_ID = "test_questionId"

@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class FirebaseEventLoggerTest {

  @Inject
  lateinit var fakeEventLogger: FakeEventLogger

  private val eventLogExplorationContext = EventLog.newBuilder()
    .setActionName(EventLog.EventAction.EVENT_ACTION_UNSPECIFIED)
    .setContext(
      EventLog.Context.newBuilder()
        .setExplorationContext(
          EventLog.ExplorationContext.newBuilder()
            .setTopicId(TEST_TOPIC_ID)
            .setExplorationId(TEST_EXPLORATION_ID)
            .setStoryId(TEST_STORY_ID)
            .build()
        )
        .build()
    )
    .setTimestamp(TEST_TIMESTAMP)
    .setPriority(EventLog.Priority.ESSENTIAL)
    .build()

  private val eventLogQuestionContext = EventLog.newBuilder()
    .setActionName(EventLog.EventAction.EVENT_ACTION_UNSPECIFIED)
    .setContext(
      EventLog.Context.newBuilder()
        .setQuestionContext(
          EventLog.QuestionContext.newBuilder()
            .setTopicId(TEST_TOPIC_ID)
            .setQuestionId(TEST_QUESTION_ID)
            .build()
        )
        .build()
    )
    .setTimestamp(TEST_TIMESTAMP)
    .setPriority(EventLog.Priority.ESSENTIAL)
    .build()

  private val eventLogNoContext = EventLog.newBuilder()
    .setTimestamp(TEST_TIMESTAMP)
    .setPriority(EventLog.Priority.ESSENTIAL)
    .build()

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    MockitoAnnotations.initMocks(this);
  }

  @Test
  fun testBundleCreation_logEvent_withExceptionContext_isSuccessful() {
    fakeEventLogger
      .logEvent(ApplicationProvider.getApplicationContext(), eventLogExplorationContext)
    val eventBundle = fakeEventLogger.getMostRecentEventBundle()

    assertThat(eventBundle.get(TIMESTAMP_KEY)).isEqualTo(TEST_TIMESTAMP)
    assertThat(eventBundle.get(PRIORITY_KEY)).isEqualTo(EventLog.Priority.ESSENTIAL.toString())
    assertThat(eventBundle.get(TOPIC_ID_KEY)).isEqualTo(TEST_TOPIC_ID)
    assertThat(eventBundle.get(STORY_ID_KEY)).isEqualTo(TEST_STORY_ID)
    assertThat(eventBundle.get(EXPLORATION_ID_KEY)).isEqualTo(TEST_EXPLORATION_ID)
  }

  @Test
  fun testBundleCreation_logEvent_withQuestionContext_isSuccessful() {
    fakeEventLogger
      .logEvent(ApplicationProvider.getApplicationContext(), eventLogQuestionContext)
    val eventBundle = fakeEventLogger.getMostRecentEventBundle()

    assertThat(eventBundle.get(TIMESTAMP_KEY)).isEqualTo(TEST_TIMESTAMP)
    assertThat(eventBundle.get(PRIORITY_KEY)).isEqualTo(EventLog.Priority.ESSENTIAL.toString())
    assertThat(eventBundle.get(TOPIC_ID_KEY)).isEqualTo(TEST_TOPIC_ID)
    assertThat(eventBundle.get(QUESTION_ID_KEY)).isEqualTo(TEST_QUESTION_ID)
  }

  @Test
  fun testBundleCreation_logEvent_withNoContext_isSuccessful() {
    fakeEventLogger
      .logEvent(ApplicationProvider.getApplicationContext(), eventLogNoContext)
    val eventBundle = fakeEventLogger.getMostRecentEventBundle()

    assertThat(eventBundle.get(TIMESTAMP_KEY)).isEqualTo(TEST_TIMESTAMP)
    assertThat(eventBundle.get(PRIORITY_KEY)).isEqualTo(EventLog.Priority.ESSENTIAL.toString())
  }

  private fun setUpTestApplicationComponent() {
    DaggerFirebaseEventLoggerTest_TestApplicationComponent.builder()
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

    fun inject(firebaseEventLoggerTest: FirebaseEventLoggerTest)
  }
}