package org.oppia.domain.topic

import android.app.Application
import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.After
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
import org.oppia.app.model.ChapterPlayState
import org.oppia.app.model.ChapterSummary
import org.oppia.app.model.CompletedStoryList
import org.oppia.app.model.LessonThumbnailGraphic
import org.oppia.app.model.OngoingTopicList
import org.oppia.app.model.ProfileId
import org.oppia.app.model.Question
import org.oppia.app.model.SkillSummary
import org.oppia.app.model.StorySummary
import org.oppia.app.model.SubtopicThumbnailGraphic
import org.oppia.app.model.Topic
import org.oppia.domain.exploration.TEST_EXPLORATION_ID_30
import org.oppia.util.caching.CacheAssetsLocally
import org.oppia.util.data.AsyncResult
import org.oppia.util.data.DataProviders
import org.oppia.util.logging.EnableConsoleLog
import org.oppia.util.logging.EnableFileLog
import org.oppia.util.logging.GlobalLogLevel
import org.oppia.util.logging.LogLevel
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import org.robolectric.annotation.Config
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlin.coroutines.EmptyCoroutineContext

private const val INVALID_STORY_ID_1 = "INVALID_STORY_ID_1"
private const val INVALID_TOPIC_ID_1 = "INVALID_TOPIC_ID_1"

/** Tests for [TopicController]. */
@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class TopicControllerTest {
  @Inject lateinit var storyProgressController: StoryProgressController
  @Inject lateinit var topicController: TopicController

  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Rule
  @JvmField
  val executorRule = InstantTaskExecutorRule()

  @Mock lateinit var mockCompletedStoryListObserver: Observer<AsyncResult<CompletedStoryList>>
  @Captor lateinit var completedStoryListResultCaptor: ArgumentCaptor<AsyncResult<CompletedStoryList>>

  @Mock lateinit var mockOngoingTopicListObserver: Observer<AsyncResult<OngoingTopicList>>
  @Captor lateinit var ongoingTopicListResultCaptor: ArgumentCaptor<AsyncResult<OngoingTopicList>>

  @Mock lateinit var mockQuestionListObserver: Observer<AsyncResult<List<Question>>>
  @Captor lateinit var questionListResultCaptor: ArgumentCaptor<AsyncResult<List<Question>>>

  @Mock lateinit var mockRecordProgressObserver: Observer<AsyncResult<Any?>>
  @Captor lateinit var recordProgressResultCaptor: ArgumentCaptor<AsyncResult<Any?>>

  @Mock lateinit var mockStorySummaryObserver: Observer<AsyncResult<StorySummary>>
  @Captor lateinit var storySummaryResultCaptor: ArgumentCaptor<AsyncResult<StorySummary>>

  @Mock lateinit var mockTopicObserver: Observer<AsyncResult<Topic>>
  @Captor lateinit var topicResultCaptor: ArgumentCaptor<AsyncResult<Topic>>

  @Inject lateinit var dataProviders: DataProviders

  @Inject
  @field:TestDispatcher
  lateinit var testDispatcher: CoroutineDispatcher

  private lateinit var profileId1: ProfileId
  private lateinit var profileId2: ProfileId

  private val coroutineContext by lazy {
    EmptyCoroutineContext + testDispatcher
  }

  // https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-test/
  @ObsoleteCoroutinesApi
  private val testThread = newSingleThreadContext("TestMain")

  @Before
  @ExperimentalCoroutinesApi
  @ObsoleteCoroutinesApi
  fun setUp() {
    profileId1 = ProfileId.newBuilder().setInternalId(1).build()
    profileId2 = ProfileId.newBuilder().setInternalId(2).build()
    Dispatchers.setMain(testThread)
    setUpTestApplicationComponent()
  }

  @After
  @ExperimentalCoroutinesApi
  @ObsoleteCoroutinesApi
  fun tearDown() {
    Dispatchers.resetMain()
    testThread.close()
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
    assertThat(getSkillIds(topic)).containsExactly(
      TEST_SKILL_ID_0, TEST_SKILL_ID_1,
      TEST_SKILL_ID_1, TEST_SKILL_ID_1
    ).inOrder()
  }

  @Test
  fun testRetrieveTopic_validTopicWithSkills_skillsHaveNoThumbnailUrls() {
    val topicLiveData = topicController.getTopic(TEST_TOPIC_ID_0)

    // This test is intentionally verifying that there are no thumbnails for skills, since there are not yet any to
    // populate.
    val topic = topicLiveData.value!!.getOrThrow()
    assertThat(topic.getSkill(0).thumbnailUrl).isEmpty()
    assertThat(topic.getSkill(1).thumbnailUrl).isEmpty()
  }

  @Test
  fun testRetrieveTopic_validTopic_returnsTopicWithProgress() {
    val topicLiveData = topicController.getTopic(TEST_TOPIC_ID_0)

    val topic = topicLiveData.value!!.getOrThrow()
    assertThat(topic.getStory(0).chapterCount).isEqualTo(1)
    assertThat(topic.getStory(0).getChapter(0).explorationId).isEqualTo(TEST_EXPLORATION_ID_30)
    assertThat(topic.getStory(0).getChapter(0).chapterPlayState).isEqualTo(ChapterPlayState.COMPLETION_STATUS_UNSPECIFIED)
  }

  @Test
  fun testRetrieveTopic_validTopic_returnsTopicWithThumbnail() {
    val topicLiveData = topicController.getTopic(TEST_TOPIC_ID_0)

    val topic = topicLiveData.value!!.getOrThrow()
    assertThat(topic.topicThumbnail.thumbnailGraphic).isEqualTo(LessonThumbnailGraphic.CHILD_WITH_FRACTIONS_HOMEWORK)
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
  fun testRetrieveTopic_validSecondTopic_returnsTopicWithThumbnail() {
    val topicLiveData = topicController.getTopic(TEST_TOPIC_ID_1)

    val topic = topicLiveData.value!!.getOrThrow()
    assertThat(topic.topicThumbnail.thumbnailGraphic).isEqualTo(LessonThumbnailGraphic.DUCK_AND_CHICKEN)
  }

  @Test
  fun testRetrieveTopic_fractionsTopic_returnsCorrectTopic() {
    val topicLiveData = topicController.getTopic(FRACTIONS_TOPIC_ID)

    val topic = topicLiveData.value!!.getOrThrow()
    assertThat(topic.topicId).isEqualTo(FRACTIONS_TOPIC_ID)
    assertThat(topic.storyCount).isEqualTo(1)
    assertThat(topic.skillCount).isEqualTo(3)
  }

  @Test
  fun testRetrieveTopic_fractionsTopic_hasCorrectDescription() {
    val topicLiveData = topicController.getTopic(FRACTIONS_TOPIC_ID)

    val topic = topicLiveData.value!!.getOrThrow()
    assertThat(topic.topicId).isEqualTo(FRACTIONS_TOPIC_ID)
    assertThat(topic.description).contains("You'll often need to talk about")
  }

  @Test
  fun testRetrieveTopic_ratiosTopic_returnsCorrectTopic() {
    val topicLiveData = topicController.getTopic(RATIOS_TOPIC_ID)

    val topic = topicLiveData.value!!.getOrThrow()
    assertThat(topic.topicId).isEqualTo(RATIOS_TOPIC_ID)
    assertThat(topic.storyCount).isEqualTo(2)
    assertThat(topic.skillCount).isEqualTo(1)
  }

  @Test
  fun testRetrieveTopic_ratiosTopic_hasCorrectDescription() {
    val topicLiveData = topicController.getTopic(RATIOS_TOPIC_ID)

    val topic = topicLiveData.value!!.getOrThrow()
    assertThat(topic.topicId).isEqualTo(RATIOS_TOPIC_ID)
    assertThat(topic.description).contains("Many everyday problems involve thinking about proportions")
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
  fun testRetrieveStory_fractionsStory_returnsCorrectStory() {
    val storyLiveData = topicController.getStory(FRACTIONS_STORY_ID_0)

    val story = storyLiveData.value!!.getOrThrow()
    assertThat(story.storyId).isEqualTo(FRACTIONS_STORY_ID_0)
  }

  @Test
  fun testRetrieveStory_fractionsStory_returnsStoryWithName() {
    val storyLiveData = topicController.getStory(FRACTIONS_STORY_ID_0)

    val story = storyLiveData.value!!.getOrThrow()
    assertThat(story.storyName).isEqualTo("Matthew Goes to the Bakery")
  }

  @Test
  fun testRetrieveStory_ratiosFirstStory_returnsCorrectStory() {
    val storyLiveData = topicController.getStory(RATIOS_STORY_ID_0)

    val story = storyLiveData.value!!.getOrThrow()
    assertThat(story.storyId).isEqualTo(RATIOS_STORY_ID_0)
    assertThat(story.storyName).isEqualTo("Ratios: Part 1")
  }

  @Test
  fun testRetrieveStory_ratiosFirstStory_returnsStoryWithMultipleChapters() {
    val storyLiveData = topicController.getStory(RATIOS_STORY_ID_0)

    val story = storyLiveData.value!!.getOrThrow()
    assertThat(getExplorationIds(story)).containsExactly(
      RATIOS_EXPLORATION_ID_0,
      RATIOS_EXPLORATION_ID_1
    ).inOrder()
  }

  @Test
  fun testRetrieveStory_ratiosSecondStory_returnsCorrectStory() {
    val storyLiveData = topicController.getStory(RATIOS_STORY_ID_1)

    val story = storyLiveData.value!!.getOrThrow()
    assertThat(story.storyId).isEqualTo(RATIOS_STORY_ID_1)
    assertThat(story.storyName).isEqualTo("Ratios: Part 2")
  }

  @Test
  fun testRetrieveStory_ratiosSecondStory_returnsStoryWithMultipleChapters() {
    val storyLiveData = topicController.getStory(RATIOS_STORY_ID_1)

    val story = storyLiveData.value!!.getOrThrow()
    assertThat(getExplorationIds(story)).containsExactly(
      RATIOS_EXPLORATION_ID_2,
      RATIOS_EXPLORATION_ID_3
    ).inOrder()
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
  fun testRetrieveStory_validStory_returnsStoryWithChapterThumbnail() {
    val storyLiveData = topicController.getStory(TEST_STORY_ID_2)

    val story = storyLiveData.value!!.getOrThrow()
    val chapter = story.getChapter(0)
    assertThat(chapter.chapterThumbnail.thumbnailGraphic).isEqualTo(LessonThumbnailGraphic.BAKER)
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
    assertThat(story.getChapter(0).chapterPlayState).isEqualTo(ChapterPlayState.COMPLETION_STATUS_UNSPECIFIED)
    assertThat(story.getChapter(1).chapterPlayState).isEqualTo(ChapterPlayState.COMPLETION_STATUS_UNSPECIFIED)
    assertThat(story.getChapter(2).chapterPlayState).isEqualTo(ChapterPlayState.COMPLETION_STATUS_UNSPECIFIED)
  }

  @Test
  fun testRetrieveStory_invalidStory_returnsFailure() {
    val storyLiveData = topicController.getStory("invalid_story_id")
    assertThat(storyLiveData.value!!.isFailure()).isTrue()
  }

  @Test
  fun testGetConceptCard_validSkill_isSuccessful() {
    val conceptCardLiveData = topicController.getConceptCard(TEST_SKILL_ID_0)

    val conceptCardResult = conceptCardLiveData.value
    assertThat(conceptCardResult).isNotNull()
    assertThat(conceptCardResult!!.isSuccess()).isTrue()
  }

  @Test
  fun testGetConceptCard_validSkill_returnsCorrectConceptCard() {
    val conceptCardLiveData = topicController.getConceptCard(TEST_SKILL_ID_0)

    val conceptCard = conceptCardLiveData.value!!.getOrThrow()
    assertThat(conceptCard.skillId).isEqualTo(TEST_SKILL_ID_0)
  }

  @Test
  fun testGetConceptCard_validSkill_returnsCardWithCorrectDescription() {
    val conceptCardLiveData = topicController.getConceptCard(TEST_SKILL_ID_0)

    val conceptCard = conceptCardLiveData.value!!.getOrThrow()
    assertThat(conceptCard.skillDescription).isEqualTo("An important skill")
  }

  @Test
  fun testGetConceptCard_validSkill_returnsCardWithCorrectExplanation() {
    val conceptCardLiveData = topicController.getConceptCard(TEST_SKILL_ID_0)

    val conceptCard = conceptCardLiveData.value!!.getOrThrow()
    assertThat(conceptCard.explanation.html).isEqualTo("Hello. Welcome to Oppia.")
  }

  @Test
  fun testGetConceptCard_validSkill_returnsCardWithCorrectWorkedExample() {
    val conceptCardLiveData = topicController.getConceptCard(TEST_SKILL_ID_0)

    val conceptCard = conceptCardLiveData.value!!.getOrThrow()
    assertThat(conceptCard.workedExampleCount).isEqualTo(1)
    assertThat(conceptCard.getWorkedExample(0).html).isEqualTo("This is the first example.")
  }

  @Test
  fun getConceptCard_validSkill_returnsCardWithSpanishTranslationForExplanation() {
    val conceptCardLiveData = topicController.getConceptCard(TEST_SKILL_ID_0)

    val conceptCard = conceptCardLiveData.value!!.getOrThrow()
    val contentId = conceptCard.explanation.contentId
    assertThat(conceptCard.writtenTranslationMap).containsKey(contentId)
    val translations = conceptCard.writtenTranslationMap.getValue(contentId).translationMappingMap
    assertThat(translations).containsKey("es")
    assertThat(translations.getValue("es").html).isEqualTo("Hola. Bienvenidos a Oppia.")
  }

  @Test
  fun getConceptCard_validSkill_returnsCardWithSpanishTranslationForWorkedExample() {
    val conceptCardLiveData = topicController.getConceptCard(TEST_SKILL_ID_0)

    val conceptCard = conceptCardLiveData.value!!.getOrThrow()
    val contentId = conceptCard.getWorkedExample(0).contentId
    assertThat(conceptCard.writtenTranslationMap).containsKey(contentId)
    val translations = conceptCard.writtenTranslationMap.getValue(contentId).translationMappingMap
    assertThat(translations).containsKey("es")
    assertThat(translations.getValue("es").html).isEqualTo("Este es el primer ejemplo trabajado.")
  }

  @Test
  fun getConceptCard_validSkill_returnsCardWithSpanishVoiceoverForExplanation() {
    val conceptCardLiveData = topicController.getConceptCard(TEST_SKILL_ID_0)

    val conceptCard = conceptCardLiveData.value!!.getOrThrow()
    val contentId = conceptCard.explanation.contentId
    assertThat(conceptCard.recordedVoiceoverMap).containsKey(contentId)
    val voiceovers = conceptCard.recordedVoiceoverMap.getValue(contentId).voiceoverMappingMap
    assertThat(voiceovers).containsKey("es")
    assertThat(voiceovers.getValue("es").fileName).isEqualTo("fake_spanish_xlated_explanation.mp3")
  }

  @Test
  fun getConceptCard_validSkill_returnsCardWithSpanishVoiceoverForWorkedExample() {
    val conceptCardLiveData = topicController.getConceptCard(TEST_SKILL_ID_0)

    val conceptCard = conceptCardLiveData.value!!.getOrThrow()
    val contentId = conceptCard.getWorkedExample(0).contentId
    assertThat(conceptCard.recordedVoiceoverMap).containsKey(contentId)
    val voiceovers = conceptCard.recordedVoiceoverMap.getValue(contentId).voiceoverMappingMap
    assertThat(voiceovers).containsKey("es")
    assertThat(voiceovers.getValue("es").fileName).isEqualTo("fake_spanish_xlated_example.mp3")
  }

  @Test
  fun testGetConceptCard_validSecondSkill_isSuccessful() {
    val conceptCardLiveData = topicController.getConceptCard(TEST_SKILL_ID_1)

    val conceptCardResult = conceptCardLiveData.value
    assertThat(conceptCardResult).isNotNull()
    assertThat(conceptCardResult!!.isSuccess()).isTrue()
  }

  @Test
  fun testGetConceptCard_validSecondSkill_returnsCorrectConceptCard() {
    val conceptCardLiveData = topicController.getConceptCard(TEST_SKILL_ID_1)

    val conceptCard = conceptCardLiveData.value!!.getOrThrow()
    assertThat(conceptCard.skillId).isEqualTo(TEST_SKILL_ID_1)
  }

  @Test
  fun testGetConceptCard_validSecondSkill_returnsCardWithCorrectDescription() {
    val conceptCardLiveData = topicController.getConceptCard(TEST_SKILL_ID_1)

    val conceptCard = conceptCardLiveData.value!!.getOrThrow()
    assertThat(conceptCard.skillDescription).isEqualTo("Another important skill")
  }

  @Test
  fun testGetConceptCard_validSecondSkill_returnsCardWithRichTextExplanation() {
    val conceptCardLiveData = topicController.getConceptCard(TEST_SKILL_ID_1)

    val conceptCard = conceptCardLiveData.value!!.getOrThrow()
    assertThat(conceptCard.explanation.html).isEqualTo("Explanation with <b>rich text</b>.")
  }

  @Test
  fun testGetConceptCard_validSecondSkill_returnsCardWithRichTextWorkedExample() {
    val conceptCardLiveData = topicController.getConceptCard(TEST_SKILL_ID_1)

    val conceptCard = conceptCardLiveData.value!!.getOrThrow()
    assertThat(conceptCard.workedExampleCount).isEqualTo(1)
    assertThat(conceptCard.getWorkedExample(0).html).isEqualTo("Worked example with <i>rich text</i>.")
  }

  @Test
  fun testGetConceptCard_validThirdSkillDifferentTopic_isSuccessful() {
    val conceptCardLiveData = topicController.getConceptCard(TEST_SKILL_ID_2)

    val conceptCardResult = conceptCardLiveData.value
    assertThat(conceptCardResult).isNotNull()
    assertThat(conceptCardResult!!.isSuccess()).isTrue()
  }

  @Test
  fun testGetConceptCard_validThirdSkillDifferentTopic_returnsCorrectConceptCard() {
    val conceptCardLiveData = topicController.getConceptCard(TEST_SKILL_ID_2)

    val conceptCard = conceptCardLiveData.value!!.getOrThrow()
    assertThat(conceptCard.skillId).isEqualTo(TEST_SKILL_ID_2)
  }

  @Test
  fun testGetConceptCard_validThirdSkillDifferentTopic_returnsCardWithCorrectDescription() {
    val conceptCardLiveData = topicController.getConceptCard(TEST_SKILL_ID_2)

    val conceptCard = conceptCardLiveData.value!!.getOrThrow()
    assertThat(conceptCard.skillDescription).isEqualTo("A different skill in a different topic")
  }

  @Test
  fun testGetConceptCard_validThirdSkillDifferentTopic_returnsCardWithCorrectExplanation() {
    val conceptCardLiveData = topicController.getConceptCard(TEST_SKILL_ID_2)

    val conceptCard = conceptCardLiveData.value!!.getOrThrow()
    assertThat(conceptCard.explanation.html).isEqualTo("Explanation without rich text.")
  }

  @Test
  fun testGetConceptCard_validThirdSkillDifferentTopic_returnsCardWithMultipleWorkedExamples() {
    val conceptCardLiveData = topicController.getConceptCard(TEST_SKILL_ID_2)

    val conceptCard = conceptCardLiveData.value!!.getOrThrow()
    assertThat(conceptCard.workedExampleCount).isEqualTo(2)
    assertThat(conceptCard.getWorkedExample(0).html).isEqualTo("Worked example without rich text.")
    assertThat(conceptCard.getWorkedExample(1).html).isEqualTo("Second worked example.")
  }

  @Test
  fun testGetConceptCard_fractionsSkill0_isSuccessful() {
    val conceptCardLiveData = topicController.getConceptCard(FRACTIONS_SKILL_ID_0)

    val conceptCardResult = conceptCardLiveData.value
    assertThat(conceptCardResult).isNotNull()
    assertThat(conceptCardResult!!.isSuccess()).isTrue()
  }

  @Test
  fun testGetConceptCard_fractionsSkill0_returnsCorrectConceptCard() {
    val conceptCardLiveData = topicController.getConceptCard(FRACTIONS_SKILL_ID_0)

    val conceptCard = conceptCardLiveData.value!!.getOrThrow()
    assertThat(conceptCard.skillId).isEqualTo(FRACTIONS_SKILL_ID_0)
    assertThat(conceptCard.skillDescription).isEqualTo(
      "Given a picture divided into unequal parts, write the fraction."
    )
    assertThat(conceptCard.explanation.html).contains(
      "<p>First, divide the picture into equal parts"
    )
  }

  @Test
  fun testGetConceptCard_ratiosSkill0_isSuccessful() {
    val conceptCardLiveData = topicController.getConceptCard(RATIOS_SKILL_ID_0)

    val conceptCardResult = conceptCardLiveData.value
    assertThat(conceptCardResult).isNotNull()
    assertThat(conceptCardResult!!.isSuccess()).isTrue()
  }

  @Test
  fun testGetConceptCard_ratiosSkill0_returnsCorrectConceptCard() {
    val conceptCardLiveData = topicController.getConceptCard(RATIOS_SKILL_ID_0)

    val conceptCard = conceptCardLiveData.value!!.getOrThrow()
    assertThat(conceptCard.skillId).isEqualTo(RATIOS_SKILL_ID_0)
    assertThat(conceptCard.skillDescription).isEqualTo(
      "Derive a ratio from a description or a picture"
    )
    assertThat(conceptCard.explanation.html).contains(
      "<p>A ratio represents a relative relationship between two or more amounts."
    )
  }

  @Test
  fun testGetConceptCard_invalidSkillId_returnsFailure() {
    val conceptCardLiveData = topicController.getConceptCard("invalid_skill_id")

    assertThat(conceptCardLiveData.value!!.isFailure()).isTrue()
  }

  @Test
  fun testGetReviewCard_fractionSubtopicId1_isSuccessful() {
    val reviewCardLiveData = topicController.getReviewCard(FRACTIONS_TOPIC_ID, SUBTOPIC_TOPIC_ID)
    val reviewCardResult = reviewCardLiveData.value
    assertThat(reviewCardResult).isNotNull()
    assertThat(reviewCardResult!!.isSuccess()).isTrue()
    assertThat(reviewCardResult.getOrThrow().pageContents.html).isEqualTo("<p>Description of subtopic is here.</p>")
  }

  @Test
  fun testRetrieveSubtopicTopic_validSubtopic_returnsSubtopicWithThumbnail() {
    val topicLiveData = topicController.getTopic(FRACTIONS_TOPIC_ID)

    val topic = topicLiveData.value!!.getOrThrow()
    assertThat(topic.subtopicList.get(0).subtopicThumbnail.thumbnailGraphic).isEqualTo(SubtopicThumbnailGraphic.WHAT_IS_A_FRACTION)
  }

  @Test
  fun testRetrieveSubtopicTopic_validSubtopic_subtopicsHaveNoThumbnailUrls() {
    val topicLiveData = topicController.getTopic(FRACTIONS_TOPIC_ID)
    val topic = topicLiveData.value!!.getOrThrow()
    assertThat(topic.subtopicList.get(0).thumbnailUrl).isEmpty()
    assertThat(topic.subtopicList.get(1).thumbnailUrl).isEmpty()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testRetrieveQuestionsForSkillIds_returnsAllQuestions() = runBlockingTest(coroutineContext) {
    val questionsListProvider = topicController.retrieveQuestionsForSkillIds(
      listOf(TEST_SKILL_ID_0, TEST_SKILL_ID_1)
    )
    dataProviders.convertToLiveData(questionsListProvider).observeForever(mockQuestionListObserver)
    verify(mockQuestionListObserver).onChanged(questionListResultCaptor.capture())

    assertThat(questionListResultCaptor.value.isSuccess()).isTrue()
    val questionsList = questionListResultCaptor.value.getOrThrow()
    assertThat(questionsList.size).isEqualTo(5)
    val questionIds = questionsList.map { it.questionId }
    assertThat(questionIds).containsExactlyElementsIn(
      mutableListOf(
        TEST_QUESTION_ID_0, TEST_QUESTION_ID_1,
        TEST_QUESTION_ID_2, TEST_QUESTION_ID_0, TEST_QUESTION_ID_3
      )
    )
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testRetrieveQuestionsForFractionsSkillId0_returnsAllQuestions() = runBlockingTest(coroutineContext) {
    val questionsListProvider = topicController.retrieveQuestionsForSkillIds(
      listOf(FRACTIONS_SKILL_ID_0)
    )
    dataProviders.convertToLiveData(questionsListProvider).observeForever(mockQuestionListObserver)
    verify(mockQuestionListObserver).onChanged(questionListResultCaptor.capture())

    assertThat(questionListResultCaptor.value.isSuccess()).isTrue()
    val questionsList = questionListResultCaptor.value.getOrThrow()
    assertThat(questionsList.size).isEqualTo(5)
    val questionIds = questionsList.map { it.questionId }
    assertThat(questionIds).containsExactlyElementsIn(
      mutableListOf(
        FRACTIONS_QUESTION_ID_0, FRACTIONS_QUESTION_ID_1,
        FRACTIONS_QUESTION_ID_2, FRACTIONS_QUESTION_ID_3, FRACTIONS_QUESTION_ID_4
      )
    )
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testRetrieveQuestionsForFractionsSkillId1_returnsAllQuestions() = runBlockingTest(coroutineContext) {
    val questionsListProvider = topicController.retrieveQuestionsForSkillIds(
      listOf(FRACTIONS_SKILL_ID_1)
    )
    dataProviders.convertToLiveData(questionsListProvider).observeForever(mockQuestionListObserver)
    verify(mockQuestionListObserver).onChanged(questionListResultCaptor.capture())

    assertThat(questionListResultCaptor.value.isSuccess()).isTrue()
    val questionsList = questionListResultCaptor.value.getOrThrow()
    assertThat(questionsList.size).isEqualTo(4)
    val questionIds = questionsList.map { it.questionId }
    assertThat(questionIds).containsExactlyElementsIn(
      mutableListOf(
        FRACTIONS_QUESTION_ID_5, FRACTIONS_QUESTION_ID_6,
        FRACTIONS_QUESTION_ID_7, FRACTIONS_QUESTION_ID_10
      )
    )
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testRetrieveQuestionsForFractionsSkillId2_returnsAllQuestions() = runBlockingTest(coroutineContext) {
    val questionsListProvider = topicController.retrieveQuestionsForSkillIds(
      listOf(FRACTIONS_SKILL_ID_2)
    )
    dataProviders.convertToLiveData(questionsListProvider).observeForever(mockQuestionListObserver)
    verify(mockQuestionListObserver).onChanged(questionListResultCaptor.capture())

    assertThat(questionListResultCaptor.value.isSuccess()).isTrue()
    val questionsList = questionListResultCaptor.value.getOrThrow()
    assertThat(questionsList.size).isEqualTo(3)
    val questionIds = questionsList.map { it.questionId }
    assertThat(questionIds).containsExactlyElementsIn(
      mutableListOf(
        FRACTIONS_QUESTION_ID_8, FRACTIONS_QUESTION_ID_9,
        FRACTIONS_QUESTION_ID_10
      )
    )
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testRetrieveQuestionsForRatiosSkillId0_returnsAllQuestions() = runBlockingTest(coroutineContext) {
    val questionsListProvider = topicController.retrieveQuestionsForSkillIds(
      listOf(RATIOS_SKILL_ID_0)
    )
    dataProviders.convertToLiveData(questionsListProvider).observeForever(mockQuestionListObserver)
    verify(mockQuestionListObserver).onChanged(questionListResultCaptor.capture())

    assertThat(questionListResultCaptor.value.isSuccess()).isTrue()
    val questionsList = questionListResultCaptor.value.getOrThrow()
    assertThat(questionsList.size).isEqualTo(1)
    val questionIds = questionsList.map { it.questionId }
    assertThat(questionIds).containsExactlyElementsIn(
      mutableListOf(
        RATIOS_QUESTION_ID_0
      )
    )
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testRetrieveQuestionsForInvalidSkillIds_returnsFailure() = runBlockingTest(coroutineContext) {
    val questionsListProvider = topicController.retrieveQuestionsForSkillIds(
      listOf(TEST_SKILL_ID_0, TEST_SKILL_ID_1, "NON_EXISTENT_SKILL_ID")
    )
    dataProviders.convertToLiveData(questionsListProvider).observeForever(mockQuestionListObserver)
    verify(mockQuestionListObserver).onChanged(questionListResultCaptor.capture())

    assertThat(questionListResultCaptor.value.isFailure()).isTrue()
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testGetTopic_invalidTopicId_getTopic_noResultFound() =
    runBlockingTest(coroutineContext) {
      topicController.getTopic(profileId1, INVALID_TOPIC_ID_1).observeForever(mockTopicObserver)
      advanceUntilIdle()

      verifyGetTopicFailed()
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testGetTopic_validTopicId_withoutAnyProgress_getTopicSucceedsWithCorrectProgress() =
    runBlockingTest(coroutineContext) {
      topicController.getTopic(profileId1, FRACTIONS_TOPIC_ID).observeForever(mockTopicObserver)
      advanceUntilIdle()

      verifyGetTopicSucceeded()

      val topic = topicResultCaptor.value.getOrThrow()
      assertThat(topic.topicId).isEqualTo(FRACTIONS_TOPIC_ID)
      assertThat(topic.storyList[0].chapterList[0].chapterPlayState).isEqualTo(ChapterPlayState.NOT_STARTED)
      assertThat(topic.storyList[0].chapterList[1].chapterPlayState).isEqualTo(ChapterPlayState.NOT_PLAYABLE_MISSING_PREREQUISITES)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testGetTopic_recordProgress_getTopic_correctProgressFound() =
    runBlockingTest(coroutineContext) {
      markFractionsStory0Chapter0AsCompleted()
      advanceUntilIdle()

      topicController.getTopic(profileId1, FRACTIONS_TOPIC_ID).observeForever(mockTopicObserver)
      advanceUntilIdle()

      verifyGetTopicSucceeded()

      val topic = topicResultCaptor.value.getOrThrow()
      assertThat(topic.topicId).isEqualTo(FRACTIONS_TOPIC_ID)
      assertThat(topic.storyList[0].chapterList[0].chapterPlayState).isEqualTo(ChapterPlayState.COMPLETED)
      assertThat(topic.storyList[0].chapterList[1].chapterPlayState).isEqualTo(ChapterPlayState.NOT_STARTED)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testGetStory_invalidData_getStory_noResultFound() =
    runBlockingTest(coroutineContext) {
      topicController.getStory(profileId1, INVALID_TOPIC_ID_1, INVALID_STORY_ID_1)
        .observeForever(mockStorySummaryObserver)
      advanceUntilIdle()

      verifyGetStoryFailed()
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testGetStory_validData_withoutAnyProgress_getStorySucceedsWithCorrectProgress() =
    runBlockingTest(coroutineContext) {
      topicController.getStory(profileId1, FRACTIONS_TOPIC_ID, FRACTIONS_STORY_ID_0)
        .observeForever(mockStorySummaryObserver)
      advanceUntilIdle()

      verifyGetStorySucceeded()

      val storySummary = storySummaryResultCaptor.value.getOrThrow()
      assertThat(storySummary.storyId).isEqualTo(FRACTIONS_STORY_ID_0)
      assertThat(storySummary.chapterList[0].chapterPlayState).isEqualTo(ChapterPlayState.NOT_STARTED)
      assertThat(storySummary.chapterList[1].chapterPlayState).isEqualTo(ChapterPlayState.NOT_PLAYABLE_MISSING_PREREQUISITES)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testGetStory_recordProgress_getTopic_correctProgressFound() =
    runBlockingTest(coroutineContext) {
      markFractionsStory0Chapter0AsCompleted()
      advanceUntilIdle()

      topicController.getTopic(profileId1, FRACTIONS_TOPIC_ID).observeForever(mockTopicObserver)
      advanceUntilIdle()

      verifyRecordProgressSucceeded()
      verifyGetTopicSucceeded()

      val topic = topicResultCaptor.value.getOrThrow()
      assertThat(topic.topicId).isEqualTo(FRACTIONS_TOPIC_ID)
      assertThat(topic.storyList[0].chapterList[0].chapterPlayState).isEqualTo(ChapterPlayState.COMPLETED)
      assertThat(topic.storyList[0].chapterList[1].chapterPlayState).isEqualTo(ChapterPlayState.NOT_STARTED)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testOngoingTopicList_validData_withoutAnyProgress_ongoingTopicListIsEmpty() =
    runBlockingTest(coroutineContext) {
      topicController.getOngoingTopicList(profileId1).observeForever(mockOngoingTopicListObserver)
      advanceUntilIdle()

      verifyGetOngoingTopicListSucceeded()

      val ongoingTopicList = ongoingTopicListResultCaptor.value.getOrThrow()
      assertThat(ongoingTopicList.topicCount).isEqualTo(0)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testOngoingTopicList_recordOneChapterCompleted_correctOngoingList() =
    runBlockingTest(coroutineContext) {
      markFractionsStory0Chapter0AsCompleted()
      advanceUntilIdle()

      topicController.getOngoingTopicList(profileId1).observeForever(mockOngoingTopicListObserver)
      advanceUntilIdle()

      verifyGetOngoingTopicListSucceeded()

      val ongoingTopicList = ongoingTopicListResultCaptor.value.getOrThrow()
      assertThat(ongoingTopicList.topicCount).isEqualTo(1)
      assertThat(ongoingTopicList.topicList[0].topicId).isEqualTo(FRACTIONS_TOPIC_ID)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testOngoingTopicList_finishEntireTopic_ongoingTopicListIsEmpty() =
    runBlockingTest(coroutineContext) {
      markFractionsStory0Chapter0AsCompleted()
      advanceUntilIdle()

      markFractionsStory0Chapter1AsCompleted()
      advanceUntilIdle()

      topicController.getOngoingTopicList(profileId1).observeForever(mockOngoingTopicListObserver)
      advanceUntilIdle()

      verifyGetOngoingTopicListSucceeded()

      val ongoingTopicList = ongoingTopicListResultCaptor.value.getOrThrow()
      assertThat(ongoingTopicList.topicCount).isEqualTo(0)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testOngoingTopicList_finishOneEntireTopicAndOneChapterInAnotherTopic_ongoingTopicListIsCorrect() =
    runBlockingTest(coroutineContext) {
      // Mark entire FRACTIONS topic as finished.
      markFractionsStory0Chapter0AsCompleted()
      advanceUntilIdle()

      markFractionsStory0Chapter1AsCompleted()
      advanceUntilIdle()

      // Mark only one chapter in RATIOS topic as finished.
      markRatiosStory0Chapter0AsCompleted()
      advanceUntilIdle()

      topicController.getOngoingTopicList(profileId1).observeForever(mockOngoingTopicListObserver)
      advanceUntilIdle()

      verifyGetOngoingTopicListSucceeded()

      val ongoingTopicList = ongoingTopicListResultCaptor.value.getOrThrow()
      assertThat(ongoingTopicList.topicCount).isEqualTo(1)
      assertThat(ongoingTopicList.topicList[0].topicId).isEqualTo(RATIOS_TOPIC_ID)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testCompletedStoryList_validData_withoutAnyProgress_completedStoryListIsEmpty() =
    runBlockingTest(coroutineContext) {
      topicController.getCompletedStoryList(profileId1).observeForever(mockCompletedStoryListObserver)
      advanceUntilIdle()

      verifyGetCompletedStoryListSucceeded()
      val completedStoryList = completedStoryListResultCaptor.value.getOrThrow()
      assertThat(completedStoryList.storySummaryCount).isEqualTo(0)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testCompletedStoryList_recordOneChapterProgress_completedStoryListIsEmpty() =
    runBlockingTest(coroutineContext) {
      markFractionsStory0Chapter0AsCompleted()
      advanceUntilIdle()

      topicController.getCompletedStoryList(profileId1).observeForever(mockCompletedStoryListObserver)
      advanceUntilIdle()

      verifyGetCompletedStoryListSucceeded()
      val completedStoryList = completedStoryListResultCaptor.value.getOrThrow()
      assertThat(completedStoryList.storySummaryCount).isEqualTo(0)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testCompletedStoryList_finishEntireStory_completedStoryListIsCorrect() =
    runBlockingTest(coroutineContext) {
      markFractionsStory0Chapter0AsCompleted()
      advanceUntilIdle()

      markFractionsStory0Chapter1AsCompleted()
      advanceUntilIdle()

      topicController.getCompletedStoryList(profileId1).observeForever(mockCompletedStoryListObserver)
      advanceUntilIdle()

      verifyGetCompletedStoryListSucceeded()

      val completedStoryList = completedStoryListResultCaptor.value.getOrThrow()
      assertThat(completedStoryList.storySummaryCount).isEqualTo(1)
      assertThat(completedStoryList.storySummaryList[0].storyId).isEqualTo(FRACTIONS_STORY_ID_0)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testCompletedStoryList_finishEntireStory_checkChapters_allAreCompleted() =
    runBlockingTest(coroutineContext) {
      markFractionsStory0Chapter0AsCompleted()
      advanceUntilIdle()

      markFractionsStory0Chapter1AsCompleted()
      advanceUntilIdle()

      topicController.getStory(profileId1, FRACTIONS_TOPIC_ID, FRACTIONS_STORY_ID_0)
        .observeForever(mockStorySummaryObserver)
      advanceUntilIdle()

      verifyGetStorySucceeded()

      val storySummary = storySummaryResultCaptor.value.getOrThrow()
      assertThat(storySummary.chapterCount).isEqualTo(2)
      assertThat(storySummary.chapterList[0].chapterPlayState).isEqualTo(ChapterPlayState.COMPLETED)
      assertThat(storySummary.chapterList[1].chapterPlayState).isEqualTo(ChapterPlayState.COMPLETED)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testCompletedStoryList_finishOneEntireStoryAndOneChapterInAnotherStory_completedStoryListIsCorrect() =
    runBlockingTest(coroutineContext) {
      markFractionsStory0Chapter0AsCompleted()
      advanceUntilIdle()

      markRatiosStory0Chapter0AsCompleted()
      advanceUntilIdle()

      markRatiosStory0Chapter1AsCompleted()
      advanceUntilIdle()

      topicController.getCompletedStoryList(profileId1).observeForever(mockCompletedStoryListObserver)
      advanceUntilIdle()

      verifyGetCompletedStoryListSucceeded()

      val completedStoryList = completedStoryListResultCaptor.value.getOrThrow()
      assertThat(completedStoryList.storySummaryCount).isEqualTo(1)
      assertThat(completedStoryList.storySummaryList[0].storyId).isEqualTo(RATIOS_STORY_ID_0)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testCompletedStoryList_finishTwoStories_completedStoryListIsCorrect() =
    runBlockingTest(coroutineContext) {
      markFractionsStory0Chapter0AsCompleted()
      advanceUntilIdle()

      markFractionsStory0Chapter1AsCompleted()
      advanceUntilIdle()

      markRatiosStory0Chapter0AsCompleted()
      advanceUntilIdle()

      markRatiosStory0Chapter1AsCompleted()
      advanceUntilIdle()

      topicController.getCompletedStoryList(profileId1).observeForever(mockCompletedStoryListObserver)
      advanceUntilIdle()

      verifyGetCompletedStoryListSucceeded()

      val completedStoryList = completedStoryListResultCaptor.value.getOrThrow()
      assertThat(completedStoryList.storySummaryCount).isEqualTo(2)
      assertThat(completedStoryList.storySummaryList[0].storyId).isEqualTo(FRACTIONS_STORY_ID_0)
      assertThat(completedStoryList.storySummaryList[1].storyId).isEqualTo(RATIOS_STORY_ID_0)
    }

  private fun setUpTestApplicationComponent() {
    DaggerTopicControllerTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  private fun markFractionsStory0Chapter0AsCompleted() {
    storyProgressController.recordCompletedChapter(
      profileId1,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_0
    ).observeForever(mockRecordProgressObserver)
  }

  private fun markFractionsStory0Chapter1AsCompleted() {
    storyProgressController.recordCompletedChapter(
      profileId1,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_1
    ).observeForever(mockRecordProgressObserver)
  }

  private fun markRatiosStory0Chapter0AsCompleted() {
    storyProgressController.recordCompletedChapter(
      profileId1,
      RATIOS_TOPIC_ID,
      RATIOS_STORY_ID_0,
      RATIOS_EXPLORATION_ID_0
    ).observeForever(mockRecordProgressObserver)
  }

  private fun markRatiosStory0Chapter1AsCompleted() {
    storyProgressController.recordCompletedChapter(
      profileId1,
      RATIOS_TOPIC_ID,
      RATIOS_STORY_ID_0,
      RATIOS_EXPLORATION_ID_1
    ).observeForever(mockRecordProgressObserver)
  }

  private fun verifyRecordProgressSucceeded() {
    verify(mockRecordProgressObserver, atLeastOnce()).onChanged(recordProgressResultCaptor.capture())
    assertThat(recordProgressResultCaptor.value.isSuccess()).isTrue()
  }

  private fun verifyGetTopicSucceeded() {
    verify(mockTopicObserver, atLeastOnce()).onChanged(topicResultCaptor.capture())
    assertThat(topicResultCaptor.value.isSuccess()).isTrue()
  }

  private fun verifyGetTopicFailed() {
    verify(mockTopicObserver, atLeastOnce()).onChanged(topicResultCaptor.capture())
    assertThat(topicResultCaptor.value.isFailure()).isTrue()
  }

  private fun verifyGetStorySucceeded() {
    verify(mockStorySummaryObserver, atLeastOnce()).onChanged(storySummaryResultCaptor.capture())
    assertThat(storySummaryResultCaptor.value.isSuccess()).isTrue()
  }

  private fun verifyGetStoryFailed() {
    verify(mockStorySummaryObserver, atLeastOnce()).onChanged(storySummaryResultCaptor.capture())
    assertThat(storySummaryResultCaptor.value.isFailure()).isTrue()
  }

  private fun verifyGetOngoingTopicListSucceeded() {
    verify(mockOngoingTopicListObserver, atLeastOnce()).onChanged(ongoingTopicListResultCaptor.capture())
    assertThat(ongoingTopicListResultCaptor.value.isSuccess()).isTrue()
  }

  private fun verifyGetCompletedStoryListSucceeded() {
    verify(mockCompletedStoryListObserver, atLeastOnce()).onChanged(completedStoryListResultCaptor.capture())
    assertThat(completedStoryListResultCaptor.value.isSuccess()).isTrue()
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

  @Qualifier annotation class TestDispatcher

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }

    @ExperimentalCoroutinesApi
    @Singleton
    @Provides
    @TestDispatcher
    fun provideTestDispatcher(): CoroutineDispatcher {
      return TestCoroutineDispatcher()
    }

    @Singleton
    @Provides
    @BackgroundDispatcher
    fun provideBackgroundDispatcher(@TestDispatcher testDispatcher: CoroutineDispatcher): CoroutineDispatcher {
      return testDispatcher
    }

    @Singleton
    @Provides
    @BlockingDispatcher
    fun provideBlockingDispatcher(@TestDispatcher testDispatcher: CoroutineDispatcher): CoroutineDispatcher {
      return testDispatcher
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

    @CacheAssetsLocally
    @Provides
    fun provideCacheAssetsLocally(): Boolean = false
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
