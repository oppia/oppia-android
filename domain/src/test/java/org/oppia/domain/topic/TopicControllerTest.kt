package org.oppia.domain.topic

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
import org.oppia.app.model.ChapterSummary
import org.oppia.app.model.SkillSummary
import org.oppia.app.model.StorySummary
import org.oppia.app.model.Topic
import org.robolectric.annotation.Config
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [TopicController]. */
@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class TopicControllerTest {
  @Inject
  lateinit var topicController: TopicController

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testRetrieveTopic_validTopic_isSuccessful() {
    val topicLiveData = topicController.getTopic(TEST_TOPIC_ID_0)

    val topicResult = topicLiveData.value
    assertThat(topicResult).isNotNull()
    assertThat(topicResult!!.isSuccess()).isTrue()
  }

  @Test
  fun testRetrieveTopic_validTopic_returnsCorrectTopic() {
    val topicLiveData = topicController.getTopic(TEST_TOPIC_ID_0)

    val topic = topicLiveData.value!!.getOrThrow()
    assertThat(topic.topicId).isEqualTo(TEST_TOPIC_ID_0)
  }

  @Test
  fun testRetrieveTopic_validTopic_returnsTopicWithDescription() {
    val topicLiveData = topicController.getTopic(TEST_TOPIC_ID_0)

    val topic = topicLiveData.value!!.getOrThrow()
    assertThat(topic.description).isEqualTo("A topic investigating the interesting aspects of the Oppia Android app.")
  }

  @Test
  fun testRetrieveTopic_validTopic_returnsTopicWithStories() {
    val topicLiveData = topicController.getTopic(TEST_TOPIC_ID_0)

    val topic = topicLiveData.value!!.getOrThrow()
    assertThat(getStoryIds(topic)).containsExactly(TEST_STORY_ID_0, TEST_STORY_ID_1).inOrder()
  }

  @Test
  fun testRetrieveTopic_validTopic_returnsTopicWithSkills() {
    val topicLiveData = topicController.getTopic(TEST_TOPIC_ID_0)

    val topic = topicLiveData.value!!.getOrThrow()
    assertThat(getSkillIds(topic)).containsExactly("test_skill_id_0", "test_skill_id_1").inOrder()
  }

  @Test
  fun testRetrieveTopic_validTopic_returnsTopicWithProgress() {
    val topicLiveData = topicController.getTopic(TEST_TOPIC_ID_0)

    val topic = topicLiveData.value!!.getOrThrow()
    assertThat(topic.getStory(0).chapterCount).isEqualTo(1)
    assertThat(topic.getStory(0).getChapter(0).explorationId).isEqualTo(TEST_EXPLORATION_ID_0)
    assertThat(topic.getStory(0).getChapter(0).completed).isTrue()
  }

  @Test
  fun testRetrieveTopic_validSecondTopic_isSuccessful() {
    val topicLiveData = topicController.getTopic(TEST_TOPIC_ID_1)

    val topicResult = topicLiveData.value
    assertThat(topicResult).isNotNull()
    assertThat(topicResult!!.isSuccess()).isTrue()
  }

  @Test
  fun testRetrieveTopic_validSecondTopic_returnsCorrectTopic() {
    val topicLiveData = topicController.getTopic(TEST_TOPIC_ID_1)

    val topic = topicLiveData.value!!.getOrThrow()
    assertThat(topic.topicId).isEqualTo(TEST_TOPIC_ID_1)
  }

  @Test
  fun testRetrieveTopic_invalidTopic_returnsFailure() {
    val topicLiveData = topicController.getTopic("invalid_topic_id")

    assertThat(topicLiveData.value!!.isFailure()).isTrue()
  }

  @Test
  fun testRetrieveStory_validStory_isSuccessful() {
    val storyLiveData = topicController.getStory(TEST_STORY_ID_2)

    val storyResult = storyLiveData.value
    assertThat(storyResult).isNotNull()
    assertThat(storyResult!!.isSuccess()).isTrue()
  }

  @Test
  fun testRetrieveStory_validStory_returnsCorrectStory() {
    val storyLiveData = topicController.getStory(TEST_STORY_ID_2)

    val story = storyLiveData.value!!.getOrThrow()
    assertThat(story.storyId).isEqualTo(TEST_STORY_ID_2)
  }

  @Test
  fun testRetrieveStory_validStory_returnsStoryWithName() {
    val storyLiveData = topicController.getStory(TEST_STORY_ID_2)

    val story = storyLiveData.value!!.getOrThrow()
    assertThat(story.storyName).isEqualTo("Other Interesting Story")
  }

  @Test
  fun testRetrieveStory_validStory_returnsStoryWithChapter() {
    val storyLiveData = topicController.getStory(TEST_STORY_ID_2)

    val story = storyLiveData.value!!.getOrThrow()
    assertThat(getExplorationIds(story)).containsExactly(TEST_EXPLORATION_ID_4)
  }

  @Test
  fun testRetrieveStory_validStory_returnsStoryWithChapterName() {
    val storyLiveData = topicController.getStory(TEST_STORY_ID_2)

    val story = storyLiveData.value!!.getOrThrow()
    assertThat(story.getChapter(0).name).isEqualTo("Fifth Exploration")
  }

  @Test
  fun testRetrieveStory_validSecondStory_isSuccessful() {
    val storyLiveData = topicController.getStory(TEST_STORY_ID_1)

    val storyResult = storyLiveData.value
    assertThat(storyResult).isNotNull()
    assertThat(storyResult!!.isSuccess()).isTrue()
  }

  @Test
  fun testRetrieveStory_validSecondStory_returnsCorrectStory() {
    val storyLiveData = topicController.getStory(TEST_STORY_ID_1)

    val story = storyLiveData.value!!.getOrThrow()
    assertThat(story.storyId).isEqualTo(TEST_STORY_ID_1)
  }

  @Test
  fun testRetrieveStory_validSecondStory_returnsStoryWithMultipleChapters() {
    val storyLiveData = topicController.getStory(TEST_STORY_ID_1)

    val story = storyLiveData.value!!.getOrThrow()
    assertThat(getExplorationIds(story)).containsExactly(
      TEST_EXPLORATION_ID_1,
      TEST_EXPLORATION_ID_2,
      TEST_EXPLORATION_ID_3
    ).inOrder()
  }

  @Test
  fun testRetrieveStory_validSecondStory_returnsStoryWithProgress() {
    val storyLiveData = topicController.getStory(TEST_STORY_ID_1)

    val story = storyLiveData.value!!.getOrThrow()
    assertThat(story.getChapter(0).completed).isTrue()
    assertThat(story.getChapter(1).completed).isFalse()
    assertThat(story.getChapter(2).completed).isFalse()
  }

  @Test
  fun testRetrieveStory_invalidStory_returnsFailure() {
    val storyLiveData = topicController.getStory("invalid_story_id")

    assertThat(storyLiveData.value!!.isFailure()).isTrue()
  }

  private fun setUpTestApplicationComponent() {
    DaggerTopicControllerTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  private fun getStoryIds(topic: Topic): List<String> {
    return topic.storyList.map(StorySummary::getStoryId)
  }

  private fun getSkillIds(topic: Topic): List<String> {
    return topic.skillList.map(SkillSummary::getSkillId)
  }

  private fun getExplorationIds(story: StorySummary): List<String> {
    return story.chapterList.map(ChapterSummary::getExplorationId)
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

    fun inject(topicControllerTest: TopicControllerTest)
  }
}
