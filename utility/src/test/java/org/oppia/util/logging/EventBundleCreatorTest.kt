package org.oppia.util.logging

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
import org.oppia.app.model.EventLog
import org.robolectric.annotation.Config
import javax.inject.Singleton

const val TEST_TIMESTAMP = 1556094120000
const val TEST_TOPIC_ID = "test_topicId"
const val TEST_STORY_ID = "test_storyId"
const val TEST_EXPLORATION_ID = "test_explorationId"
const val TEST_QUESTION_ID = "test_questionId"
const val TEST_SKILL_ID_ONE = "test_skillId_one"
const val TEST_SKILL_ID_TWO = "test_skillId_two"
const val TEST_SUB_TOPIC_ID = "test_subTopicId"

@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class EventBundleCreatorTest {

  private val eventBundleCreator = EventBundleCreator()
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
            .setQuestionId(TEST_QUESTION_ID)
            .addAllSkillId(listOf(TEST_SKILL_ID_ONE, TEST_SKILL_ID_TWO))
            .build()
        )
        .build()
    )
    .setTimestamp(TEST_TIMESTAMP)
    .setPriority(EventLog.Priority.ESSENTIAL)
    .build()

  private val eventLogTopicContext = EventLog.newBuilder()
    .setActionName(EventLog.EventAction.EVENT_ACTION_UNSPECIFIED)
    .setContext(
      EventLog.Context.newBuilder()
        .setTopicContext(
          EventLog.TopicContext.newBuilder()
            .setTopicId(TEST_TOPIC_ID)
            .build()
        )
        .build()
    )
    .setTimestamp(TEST_TIMESTAMP)
    .setPriority(EventLog.Priority.ESSENTIAL)
    .build()

  private val eventLogStoryContext = EventLog.newBuilder()
    .setActionName(EventLog.EventAction.EVENT_ACTION_UNSPECIFIED)
    .setContext(
      EventLog.Context.newBuilder()
        .setStoryContext(
          EventLog.StoryContext.newBuilder()
            .setTopicId(TEST_TOPIC_ID)
            .setStoryId(TEST_STORY_ID)
            .build()
        )
        .build()
    )
    .setTimestamp(TEST_TIMESTAMP)
    .setPriority(EventLog.Priority.ESSENTIAL)
    .build()

  private val eventLogConceptCardContext = EventLog.newBuilder()
    .setActionName(EventLog.EventAction.EVENT_ACTION_UNSPECIFIED)
    .setContext(
      EventLog.Context.newBuilder()
        .setConceptCardContext(
          EventLog.ConceptCardContext.newBuilder()
            .setSkillId(TEST_SKILL_ID_ONE)
            .build()
        )
        .build()
    )
    .setTimestamp(TEST_TIMESTAMP)
    .setPriority(EventLog.Priority.ESSENTIAL)
    .build()

  private val eventLogRevisionCardContext = EventLog.newBuilder()
    .setActionName(EventLog.EventAction.EVENT_ACTION_UNSPECIFIED)
    .setContext(
      EventLog.Context.newBuilder()
        .setRevisionCardContext(
          EventLog.RevisionCardContext.newBuilder()
            .setTopicId(TEST_TOPIC_ID)
            .setSubTopicId(TEST_SUB_TOPIC_ID)
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
  }

  @Test
  fun testBundleCreation_logEvent_withExplorationContext_isSuccessful() {
    val eventBundle = EventBundleCreator().createEventBundle(eventLogExplorationContext)

    assertThat(eventBundle.get(TIMESTAMP_KEY)).isEqualTo(TEST_TIMESTAMP)
    assertThat(eventBundle.get(PRIORITY_KEY)).isEqualTo(EventLog.Priority.ESSENTIAL.toString())
    assertThat(eventBundle.get(TOPIC_ID_KEY)).isEqualTo(TEST_TOPIC_ID)
    assertThat(eventBundle.get(STORY_ID_KEY)).isEqualTo(TEST_STORY_ID)
    assertThat(eventBundle.get(EXPLORATION_ID_KEY)).isEqualTo(TEST_EXPLORATION_ID)
  }

  @Test
  fun testBundleCreation_logEvent_withQuestionContext_isSuccessful() {
    val eventBundle = eventBundleCreator.createEventBundle(eventLogQuestionContext)

    assertThat(eventBundle.get(TIMESTAMP_KEY)).isEqualTo(TEST_TIMESTAMP)
    assertThat(eventBundle.get(PRIORITY_KEY)).isEqualTo(EventLog.Priority.ESSENTIAL.toString())
    assertThat(eventBundle.get(QUESTION_ID_KEY)).isEqualTo(TEST_QUESTION_ID)
    assertThat(eventBundle.get(SKILL_ID_KEY))
      .isEqualTo(listOf(TEST_SKILL_ID_ONE, TEST_SKILL_ID_TWO).joinToString())
  }

  @Test
  fun testBundleCreation_logEvent_withTopicContext_isSuccessful() {
    val eventBundle = eventBundleCreator.createEventBundle(eventLogTopicContext)

    assertThat(eventBundle.get(TIMESTAMP_KEY)).isEqualTo(TEST_TIMESTAMP)
    assertThat(eventBundle.get(PRIORITY_KEY)).isEqualTo(EventLog.Priority.ESSENTIAL.toString())
    assertThat(eventBundle.get(TOPIC_ID_KEY)).isEqualTo(TEST_TOPIC_ID)
  }

  @Test
  fun testBundleCreation_logEvent_withStoryContext_isSuccessful() {
    val eventBundle = eventBundleCreator.createEventBundle(eventLogStoryContext)

    assertThat(eventBundle.get(TIMESTAMP_KEY)).isEqualTo(TEST_TIMESTAMP)
    assertThat(eventBundle.get(PRIORITY_KEY)).isEqualTo(EventLog.Priority.ESSENTIAL.toString())
    assertThat(eventBundle.get(TOPIC_ID_KEY)).isEqualTo(TEST_TOPIC_ID)
    assertThat(eventBundle.get(STORY_ID_KEY)).isEqualTo(TEST_STORY_ID)
  }

  @Test
  fun testBundleCreation_logEvent_withConceptCardContext_isSuccessful() {
    val eventBundle = eventBundleCreator.createEventBundle(eventLogConceptCardContext)

    assertThat(eventBundle.get(TIMESTAMP_KEY)).isEqualTo(TEST_TIMESTAMP)
    assertThat(eventBundle.get(PRIORITY_KEY)).isEqualTo(EventLog.Priority.ESSENTIAL.toString())
    assertThat(eventBundle.get(SKILL_ID_KEY)).isEqualTo(TEST_SKILL_ID_ONE)
  }

  @Test
  fun testBundleCreation_logEvent_withRevisionCardContext_isSuccessful() {
    val eventBundle = eventBundleCreator.createEventBundle(eventLogRevisionCardContext)

    assertThat(eventBundle.get(TIMESTAMP_KEY)).isEqualTo(TEST_TIMESTAMP)
    assertThat(eventBundle.get(PRIORITY_KEY)).isEqualTo(EventLog.Priority.ESSENTIAL.toString())
    assertThat(eventBundle.get(TOPIC_ID_KEY)).isEqualTo(TEST_TOPIC_ID)
    assertThat(eventBundle.get(SUB_TOPIC_ID_KEY)).isEqualTo(TEST_SUB_TOPIC_ID)
  }

  @Test
  fun testBundleCreation_logEvent_withNoContext_isSuccessful() {
    val eventBundle = eventBundleCreator.createEventBundle(eventLogNoContext)

    assertThat(eventBundle.get(TIMESTAMP_KEY)).isEqualTo(TEST_TIMESTAMP)
    assertThat(eventBundle.get(PRIORITY_KEY)).isEqualTo(EventLog.Priority.ESSENTIAL.toString())
  }

  private fun setUpTestApplicationComponent() {
    DaggerEventBundleCreatorTest_TestApplicationComponent.builder()
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
  @Component(modules = [TestModule::class])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(eventBundleCreatorTest: EventBundleCreatorTest)
  }
}
