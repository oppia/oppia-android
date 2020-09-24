package org.oppia.android.domain.topic

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
import org.json.JSONException
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
import org.oppia.android.app.model.ChapterPlayState
import org.oppia.android.app.model.ChapterSummary
import org.oppia.android.app.model.CompletedStoryList
import org.oppia.android.app.model.LessonThumbnailGraphic
import org.oppia.android.app.model.OngoingTopicList
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.Question
import org.oppia.android.app.model.StorySummary
import org.oppia.android.app.model.Topic
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.testing.FakeExceptionLogger
import org.oppia.android.testing.TestCoroutineDispatchers
import org.oppia.android.testing.TestDispatcherModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.util.caching.CacheAssetsLocally
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.oppia.android.app.model.ChapterPlayState
import org.oppia.android.app.model.ChapterSummary
import org.oppia.android.app.model.CompletedStoryList
import org.oppia.android.app.model.LessonThumbnailGraphic
import org.oppia.android.app.model.OngoingTopicList
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.Question
import org.oppia.android.app.model.StorySummary
import org.oppia.android.app.model.Topic
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.testing.FakeExceptionLogger
import org.oppia.testing.TestCoroutineDispatchers
import org.oppia.testing.TestDispatcherModule
import org.oppia.testing.TestLogReportingModule
import org.oppia.android.util.caching.CacheAssetsLocally
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.io.FileNotFoundException
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

private const val INVALID_STORY_ID_1 = "INVALID_STORY_ID_1"
private const val INVALID_TOPIC_ID_1 = "INVALID_TOPIC_ID_1"

/** Tests for [TopicController]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = TopicControllerTest.TestApplication::class)
class TopicControllerTest {

  @Inject
  lateinit var storyProgressController: StoryProgressController

  @Inject
  lateinit var topicController: TopicController

  @Inject
  lateinit var fakeExceptionLogger: FakeExceptionLogger

  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Mock
  lateinit var mockCompletedStoryListObserver: Observer<AsyncResult<CompletedStoryList>>

  @Captor
  lateinit var completedStoryListResultCaptor: ArgumentCaptor<AsyncResult<CompletedStoryList>>

  @Mock
  lateinit var mockOngoingTopicListObserver: Observer<AsyncResult<OngoingTopicList>>

  @Captor
  lateinit var ongoingTopicListResultCaptor: ArgumentCaptor<AsyncResult<OngoingTopicList>>

  @Mock
  lateinit var mockQuestionListObserver: Observer<AsyncResult<List<Question>>>

  @Captor
  lateinit var questionListResultCaptor: ArgumentCaptor<AsyncResult<List<Question>>>

  @Mock
  lateinit var mockRecordProgressObserver: Observer<AsyncResult<Any?>>

  @Captor
  lateinit var recordProgressResultCaptor: ArgumentCaptor<AsyncResult<Any?>>

  @Mock
  lateinit var mockStorySummaryObserver: Observer<AsyncResult<StorySummary>>

  @Captor
  lateinit var storySummaryResultCaptor: ArgumentCaptor<AsyncResult<StorySummary>>

  @Mock
  lateinit var mockTopicObserver: Observer<AsyncResult<Topic>>

  @Captor
  lateinit var topicResultCaptor: ArgumentCaptor<AsyncResult<Topic>>

  @Inject
  lateinit var dataProviders: DataProviders

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  private lateinit var profileId1: ProfileId
  private lateinit var profileId2: ProfileId

  private val currentTimestamp = Date().time

  @Before
  fun setUp() {
    profileId1 = ProfileId.newBuilder().setInternalId(1).build()
    profileId2 = ProfileId.newBuilder().setInternalId(2).build()
    setUpTestApplicationComponent()
  }

  @Test
  fun testRetrieveTopic_validSecondTopic_returnsCorrectTopic() {
    topicController.getTopic(
      profileId1, TEST_TOPIC_ID_1
    ).toLiveData().observeForever(mockTopicObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetTopicSucceeded()
    val topic = topicResultCaptor.value.getOrThrow()
    assertThat(topic.topicId).isEqualTo(TEST_TOPIC_ID_1)
  }

  @Test
  fun testRetrieveTopic_validSecondTopic_returnsTopicWithThumbnail() {
    topicController.getTopic(
      profileId1, TEST_TOPIC_ID_1
    ).toLiveData().observeForever(mockTopicObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetTopicSucceeded()
    val topic = topicResultCaptor.value!!.getOrThrow()
    assertThat(topic.topicThumbnail.thumbnailGraphic)
      .isEqualTo(LessonThumbnailGraphic.BAKER)
  }

  @Test
  fun testRetrieveTopic_fractionsTopic_returnsCorrectTopic() {
    topicController.getTopic(
      profileId1, FRACTIONS_TOPIC_ID
    ).toLiveData().observeForever(mockTopicObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetTopicSucceeded()
    val topic = topicResultCaptor.value!!.getOrThrow()
    assertThat(topic.topicId).isEqualTo(FRACTIONS_TOPIC_ID)
    assertThat(topic.storyCount).isEqualTo(1)
  }

  @Test
  fun testRetrieveTopic_fractionsTopic_hasCorrectDescription() {
    topicController.getTopic(
      profileId1, FRACTIONS_TOPIC_ID
    ).toLiveData().observeForever(mockTopicObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetTopicSucceeded()
    val topic = topicResultCaptor.value!!.getOrThrow()
    assertThat(topic.topicId).isEqualTo(FRACTIONS_TOPIC_ID)
    assertThat(topic.description).contains("You'll often need to talk about")
  }

  @Test
  fun testRetrieveTopic_ratiosTopic_returnsCorrectTopic() {
    topicController.getTopic(
      profileId1, RATIOS_TOPIC_ID
    ).toLiveData().observeForever(mockTopicObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetTopicSucceeded()
    val topic = topicResultCaptor.value!!.getOrThrow()
    assertThat(topic.topicId).isEqualTo(RATIOS_TOPIC_ID)
    assertThat(topic.storyCount).isEqualTo(2)
  }

  @Test
  fun testRetrieveTopic_ratiosTopic_hasCorrectDescription() {
    topicController.getTopic(
      profileId1, RATIOS_TOPIC_ID
    ).toLiveData().observeForever(mockTopicObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetTopicSucceeded()
    val topic = topicResultCaptor.value!!.getOrThrow()
    assertThat(topic.topicId).isEqualTo(RATIOS_TOPIC_ID)
    assertThat(topic.description).contains(
      "Many everyday problems involve thinking about proportions"
    )
  }

  @Test
  fun testRetrieveTopic_invalidTopic_returnsFailure() {
    topicController.getTopic(
      profileId1, INVALID_TOPIC_ID_1
    ).toLiveData().observeForever(mockTopicObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetTopicFailed()
    assertThat(topicResultCaptor.value!!.isFailure()).isTrue()
  }

  @Test
  fun testRetrieveStory_validStory_isSuccessful() {
    topicController.getStory(profileId1, TEST_TOPIC_ID_1, TEST_STORY_ID_2).toLiveData()
      .observeForever(mockStorySummaryObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetStorySucceeded()
    val storyResult = storySummaryResultCaptor.value
    assertThat(storyResult).isNotNull()
    assertThat(storyResult!!.isSuccess()).isTrue()
  }

  @Test
  fun testRetrieveStory_validStory_returnsCorrectStory() {
    topicController.getStory(profileId1, TEST_TOPIC_ID_1, TEST_STORY_ID_2).toLiveData()
      .observeForever(mockStorySummaryObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetStorySucceeded()
    val story = storySummaryResultCaptor.value!!.getOrThrow()
    assertThat(story.storyId).isEqualTo(TEST_STORY_ID_2)
  }

  @Test
  fun testRetrieveStory_validStory_returnsStoryWithName() {
    topicController.getStory(profileId1, TEST_TOPIC_ID_1, TEST_STORY_ID_2).toLiveData()
      .observeForever(mockStorySummaryObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetStorySucceeded()
    val story = storySummaryResultCaptor.value!!.getOrThrow()
    assertThat(story.storyName).isEqualTo("Other Interesting Story")
  }

  @Test
  fun testRetrieveStory_fractionsStory_returnsCorrectStory() {
    topicController.getStory(profileId1, FRACTIONS_TOPIC_ID, FRACTIONS_STORY_ID_0).toLiveData()
      .observeForever(mockStorySummaryObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetStorySucceeded()
    val story = storySummaryResultCaptor.value!!.getOrThrow()
    assertThat(story.storyId).isEqualTo(FRACTIONS_STORY_ID_0)
  }

  @Test
  fun testRetrieveStory_fractionsStory_returnsStoryWithName() {
    topicController.getStory(profileId1, FRACTIONS_TOPIC_ID, FRACTIONS_STORY_ID_0).toLiveData()
      .observeForever(mockStorySummaryObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetStorySucceeded()
    val story = storySummaryResultCaptor.value!!.getOrThrow()
    assertThat(story.storyName).isEqualTo("Matthew Goes to the Bakery")
  }

  @Test
  fun testRetrieveStory_ratiosFirstStory_returnsCorrectStory() {
    topicController.getStory(profileId1, RATIOS_TOPIC_ID, RATIOS_STORY_ID_0).toLiveData()
      .observeForever(mockStorySummaryObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetStorySucceeded()
    val story = storySummaryResultCaptor.value!!.getOrThrow()
    assertThat(story.storyId).isEqualTo(RATIOS_STORY_ID_0)
    assertThat(story.storyName).isEqualTo("Ratios: Part 1")
  }

  @Test
  fun testRetrieveStory_ratiosFirstStory_returnsStoryWithMultipleChapters() {
    topicController.getStory(profileId1, RATIOS_TOPIC_ID, RATIOS_STORY_ID_0).toLiveData()
      .observeForever(mockStorySummaryObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetStorySucceeded()
    val story = storySummaryResultCaptor.value!!.getOrThrow()
    assertThat(getExplorationIds(story)).containsExactly(
      RATIOS_EXPLORATION_ID_0,
      RATIOS_EXPLORATION_ID_1
    ).inOrder()
  }

  @Test
  fun testRetrieveStory_ratiosSecondStory_returnsCorrectStory() {
    topicController.getStory(profileId1, RATIOS_TOPIC_ID, RATIOS_STORY_ID_1).toLiveData()
      .observeForever(mockStorySummaryObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetStorySucceeded()
    val story = storySummaryResultCaptor.value!!.getOrThrow()
    assertThat(story.storyId).isEqualTo(RATIOS_STORY_ID_1)
    assertThat(story.storyName).isEqualTo("Ratios: Part 2")
  }

  @Test
  fun testRetrieveStory_ratiosSecondStory_returnsStoryWithMultipleChapters() {
    topicController.getStory(profileId1, RATIOS_TOPIC_ID, RATIOS_STORY_ID_1).toLiveData()
      .observeForever(mockStorySummaryObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetStorySucceeded()
    val story = storySummaryResultCaptor.value!!.getOrThrow()
    assertThat(getExplorationIds(story)).containsExactly(
      RATIOS_EXPLORATION_ID_2,
      RATIOS_EXPLORATION_ID_3
    ).inOrder()
  }

  @Test
  fun testRetrieveStory_validStory_returnsStoryWithChapter() {
    topicController.getStory(profileId1, TEST_TOPIC_ID_1, TEST_STORY_ID_2).toLiveData()
      .observeForever(mockStorySummaryObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetStorySucceeded()
    val story = storySummaryResultCaptor.value!!.getOrThrow()
    assertThat(getExplorationIds(story)).containsExactly(TEST_EXPLORATION_ID_4)
  }

  @Test
  fun testRetrieveStory_validStory_returnsStoryWithChapterName() {
    topicController.getStory(profileId1, TEST_TOPIC_ID_1, TEST_STORY_ID_2).toLiveData()
      .observeForever(mockStorySummaryObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetStorySucceeded()
    val story = storySummaryResultCaptor.value!!.getOrThrow()
    assertThat(story.getChapter(0).name).isEqualTo("Fifth Exploration")
  }

  @Test
  fun testRetrieveStory_validStory_returnsStoryWithChapterSummary() {
    topicController.getStory(profileId1, FRACTIONS_TOPIC_ID, FRACTIONS_STORY_ID_0).toLiveData()
      .observeForever(mockStorySummaryObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetStorySucceeded()
    val story = storySummaryResultCaptor.value!!.getOrThrow()
    assertThat(story.getChapter(0).summary)
      .isEqualTo("This is outline/summary for <b>What is a Fraction?</b>")
  }

  @Test
  fun testRetrieveStory_validStory_returnsStoryWithChapterThumbnail() {
    topicController.getStory(profileId1, TEST_TOPIC_ID_1, TEST_STORY_ID_2).toLiveData()
      .observeForever(mockStorySummaryObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetStorySucceeded()
    val story = storySummaryResultCaptor.value!!.getOrThrow()
    val chapter = story.getChapter(0)
    assertThat(chapter.chapterThumbnail.thumbnailGraphic)
      .isEqualTo(LessonThumbnailGraphic.CHILD_WITH_FRACTIONS_HOMEWORK)
  }

  @Test
  fun testRetrieveStory_validSecondStory_isSuccessful() {
    topicController.getStory(profileId1, TEST_TOPIC_ID_0, TEST_STORY_ID_1).toLiveData()
      .observeForever(mockStorySummaryObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetStorySucceeded()
    val storyResult = storySummaryResultCaptor.value
    assertThat(storyResult).isNotNull()
    assertThat(storyResult!!.isSuccess()).isTrue()
  }

  @Test
  fun testRetrieveStory_validSecondStory_returnsCorrectStory() {
    topicController.getStory(profileId1, TEST_TOPIC_ID_0, TEST_STORY_ID_1).toLiveData()
      .observeForever(mockStorySummaryObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetStorySucceeded()
    val story = storySummaryResultCaptor.value!!.getOrThrow()
    assertThat(story.storyId).isEqualTo(TEST_STORY_ID_1)
  }

  @Test
  fun testRetrieveStory_validSecondStory_returnsStoryWithMultipleChapters() {
    topicController.getStory(profileId1, TEST_TOPIC_ID_0, TEST_STORY_ID_1).toLiveData()
      .observeForever(mockStorySummaryObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetStorySucceeded()
    val story = storySummaryResultCaptor.value!!.getOrThrow()
    assertThat(getExplorationIds(story)).containsExactly(
      TEST_EXPLORATION_ID_1,
      TEST_EXPLORATION_ID_0,
      TEST_EXPLORATION_ID_3
    ).inOrder()
  }

  @Test
  fun testRetrieveStory_validSecondStory_returnsStoryWithProgress() {
    topicController.getStory(profileId1, TEST_TOPIC_ID_0, TEST_STORY_ID_1).toLiveData()
      .observeForever(mockStorySummaryObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetStorySucceeded()
    val story = storySummaryResultCaptor.value!!.getOrThrow()
    assertThat(story.getChapter(0).chapterPlayState)
      .isEqualTo(ChapterPlayState.NOT_STARTED)
    assertThat(story.getChapter(1).chapterPlayState)
      .isEqualTo(ChapterPlayState.NOT_PLAYABLE_MISSING_PREREQUISITES)
    assertThat(story.getChapter(2).chapterPlayState)
      .isEqualTo(ChapterPlayState.NOT_PLAYABLE_MISSING_PREREQUISITES)
  }

  @Test
  fun testRetrieveStory_invalidStory_returnsFailure() {
    topicController.getStory(profileId1, INVALID_TOPIC_ID_1, INVALID_STORY_ID_1).toLiveData()
      .observeForever(mockStorySummaryObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetStoryFailed()
    assertThat(storySummaryResultCaptor.value!!.isFailure()).isTrue()
  }

  @Test
  fun testGetConceptCard_validSkill_isSuccessful() {
    val conceptCardLiveData = topicController
      .getConceptCard(TEST_SKILL_ID_0)

    val conceptCardResult = conceptCardLiveData.value
    assertThat(conceptCardResult).isNotNull()
    assertThat(conceptCardResult!!.isSuccess()).isTrue()
  }

  @Test
  fun testGetConceptCard_validSkill_returnsCorrectConceptCard() {
    val conceptCardLiveData = topicController
      .getConceptCard(TEST_SKILL_ID_0)

    val conceptCard = conceptCardLiveData.value!!.getOrThrow()
    assertThat(conceptCard.skillId).isEqualTo(TEST_SKILL_ID_0)
  }

  @Test
  fun testGetConceptCard_validSkill_returnsCardWithCorrectDescription() {
    val conceptCardLiveData = topicController
      .getConceptCard(TEST_SKILL_ID_0)

    val conceptCard = conceptCardLiveData.value!!.getOrThrow()
    assertThat(conceptCard.skillDescription).isEqualTo("An important skill")
  }

  @Test
  fun testGetConceptCard_validSkill_returnsCardWithCorrectExplanation() {
    val conceptCardLiveData = topicController
      .getConceptCard(TEST_SKILL_ID_0)

    val conceptCard = conceptCardLiveData.value!!.getOrThrow()
    assertThat(conceptCard.explanation.html).isEqualTo("Hello. Welcome to Oppia.")
  }

  @Test
  fun testGetConceptCard_validSkill_returnsCardWithCorrectWorkedExample() {
    val conceptCardLiveData = topicController
      .getConceptCard(TEST_SKILL_ID_0)

    val conceptCard = conceptCardLiveData.value!!.getOrThrow()
    assertThat(conceptCard.workedExampleCount).isEqualTo(1)
    assertThat(conceptCard.getWorkedExample(0).html)
      .isEqualTo("This is the first example.")
  }

  @Test
  fun getConceptCard_validSkill_returnsCardWithSpanishTranslationForExplanation() {
    val conceptCardLiveData = topicController
      .getConceptCard(TEST_SKILL_ID_0)

    val conceptCard = conceptCardLiveData.value!!.getOrThrow()
    val contentId = conceptCard.explanation.contentId
    assertThat(conceptCard.writtenTranslationMap).containsKey(contentId)
    val translations = conceptCard.writtenTranslationMap
      .getValue(contentId).translationMappingMap
    assertThat(translations).containsKey("es")
    assertThat(translations.getValue("es").html).isEqualTo(
      "Hola. Bienvenidos a Oppia."
    )
  }

  @Test
  fun getConceptCard_validSkill_returnsCardWithSpanishTranslationForWorkedExample() {
    val conceptCardLiveData = topicController
      .getConceptCard(TEST_SKILL_ID_0)

    val conceptCard = conceptCardLiveData.value!!.getOrThrow()
    val contentId = conceptCard.getWorkedExample(0).contentId
    assertThat(conceptCard.writtenTranslationMap).containsKey(contentId)
    val translations = conceptCard.writtenTranslationMap
      .getValue(contentId).translationMappingMap
    assertThat(translations).containsKey("es")
    assertThat(translations.getValue("es").html)
      .isEqualTo("Este es el primer ejemplo trabajado.")
  }

  @Test
  fun getConceptCard_validSkill_returnsCardWithSpanishVoiceoverForExplanation() {
    val conceptCardLiveData = topicController
      .getConceptCard(TEST_SKILL_ID_0)

    val conceptCard = conceptCardLiveData.value!!.getOrThrow()
    val contentId = conceptCard.explanation.contentId
    assertThat(conceptCard.recordedVoiceoverMap).containsKey(contentId)
    val voiceovers = conceptCard.recordedVoiceoverMap
      .getValue(contentId).voiceoverMappingMap
    assertThat(voiceovers).containsKey("es")
    assertThat(voiceovers.getValue("es").fileName)
      .isEqualTo("fake_spanish_xlated_explanation.mp3")
  }

  @Test
  fun getConceptCard_validSkill_returnsCardWithSpanishVoiceoverForWorkedExample() {
    val conceptCardLiveData = topicController
      .getConceptCard(TEST_SKILL_ID_0)

    val conceptCard = conceptCardLiveData.value!!.getOrThrow()
    val contentId = conceptCard.getWorkedExample(0).contentId
    assertThat(conceptCard.recordedVoiceoverMap).containsKey(contentId)
    val voiceovers = conceptCard.recordedVoiceoverMap
      .getValue(contentId).voiceoverMappingMap
    assertThat(voiceovers).containsKey("es")
    assertThat(voiceovers.getValue("es").fileName)
      .isEqualTo("fake_spanish_xlated_example.mp3")
  }

  @Test
  fun testGetConceptCard_validSecondSkill_isSuccessful() {
    val conceptCardLiveData = topicController
      .getConceptCard(TEST_SKILL_ID_1)

    val conceptCardResult = conceptCardLiveData.value
    assertThat(conceptCardResult).isNotNull()
    assertThat(conceptCardResult!!.isSuccess()).isTrue()
  }

  @Test
  fun testGetConceptCard_validSecondSkill_returnsCorrectConceptCard() {
    val conceptCardLiveData = topicController
      .getConceptCard(TEST_SKILL_ID_1)

    val conceptCard = conceptCardLiveData.value!!.getOrThrow()
    assertThat(conceptCard.skillId).isEqualTo(TEST_SKILL_ID_1)
  }

  @Test
  fun testGetConceptCard_validSecondSkill_returnsCardWithCorrectDescription() {
    val conceptCardLiveData = topicController
      .getConceptCard(TEST_SKILL_ID_1)

    val conceptCard = conceptCardLiveData.value!!.getOrThrow()
    assertThat(conceptCard.skillDescription).isEqualTo("Another important skill")
  }

  @Test
  fun testGetConceptCard_validSecondSkill_returnsCardWithRichTextExplanation() {
    val conceptCardLiveData = topicController
      .getConceptCard(TEST_SKILL_ID_1)

    val conceptCard = conceptCardLiveData.value!!.getOrThrow()
    assertThat(conceptCard.explanation.html).isEqualTo(
      "Explanation with <b>rich text</b>."
    )
  }

  @Test
  fun testGetConceptCard_validSecondSkill_returnsCardWithRichTextWorkedExample() {
    val conceptCardLiveData = topicController
      .getConceptCard(TEST_SKILL_ID_1)

    val conceptCard = conceptCardLiveData.value!!.getOrThrow()
    assertThat(conceptCard.workedExampleCount).isEqualTo(1)
    assertThat(conceptCard.getWorkedExample(0).html)
      .isEqualTo("Worked example with <i>rich text</i>.")
  }

  @Test
  fun testGetConceptCard_validThirdSkillDifferentTopic_isSuccessful() {
    val conceptCardLiveData = topicController
      .getConceptCard(TEST_SKILL_ID_2)

    val conceptCardResult = conceptCardLiveData.value
    assertThat(conceptCardResult).isNotNull()
    assertThat(conceptCardResult!!.isSuccess()).isTrue()
  }

  @Test
  fun testGetConceptCard_validThirdSkillDifferentTopic_returnsCorrectConceptCard() {
    val conceptCardLiveData = topicController
      .getConceptCard(TEST_SKILL_ID_2)

    val conceptCard = conceptCardLiveData.value!!.getOrThrow()
    assertThat(conceptCard.skillId).isEqualTo(TEST_SKILL_ID_2)
  }

  @Test
  fun testGetConceptCard_validThirdSkillDifferentTopic_returnsCardWithCorrectDescription() {
    val conceptCardLiveData = topicController
      .getConceptCard(TEST_SKILL_ID_2)

    val conceptCard = conceptCardLiveData.value!!.getOrThrow()
    assertThat(conceptCard.skillDescription)
      .isEqualTo("A different skill in a different topic")
  }

  @Test
  fun testGetConceptCard_validThirdSkillDifferentTopic_returnsCardWithCorrectExplanation() {
    val conceptCardLiveData = topicController
      .getConceptCard(TEST_SKILL_ID_2)

    val conceptCard = conceptCardLiveData.value!!.getOrThrow()
    assertThat(conceptCard.explanation.html).isEqualTo("Explanation without rich text.")
  }

  @Test
  fun testGetConceptCard_validThirdSkillDifferentTopic_returnsCardWithMultipleWorkedExamples() {
    val conceptCardLiveData = topicController
      .getConceptCard(TEST_SKILL_ID_2)

    val conceptCard = conceptCardLiveData.value!!.getOrThrow()
    assertThat(conceptCard.workedExampleCount).isEqualTo(2)
    assertThat(conceptCard.getWorkedExample(0).html)
      .isEqualTo("Worked example without rich text.")
    assertThat(conceptCard.getWorkedExample(1).html)
      .isEqualTo("Second worked example.")
  }

  @Test
  fun testGetConceptCard_fractionsSkill0_isSuccessful() {
    val conceptCardLiveData = topicController
      .getConceptCard(FRACTIONS_SKILL_ID_0)

    val conceptCardResult = conceptCardLiveData.value
    assertThat(conceptCardResult).isNotNull()
    assertThat(conceptCardResult!!.isSuccess()).isTrue()
  }

  @Test
  fun testGetConceptCard_fractionsSkill0_returnsCorrectConceptCard() {
    val conceptCardLiveData = topicController
      .getConceptCard(FRACTIONS_SKILL_ID_0)

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
    val conceptCardLiveData = topicController
      .getConceptCard(RATIOS_SKILL_ID_0)

    val conceptCardResult = conceptCardLiveData.value
    assertThat(conceptCardResult).isNotNull()
    assertThat(conceptCardResult!!.isSuccess()).isTrue()
  }

  @Test
  fun testGetConceptCard_ratiosSkill0_returnsCorrectConceptCard() {
    val conceptCardLiveData = topicController
      .getConceptCard(RATIOS_SKILL_ID_0)

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
    topicController.getConceptCard("invalid_skill_id")

    val exception = fakeExceptionLogger.getMostRecentException()

    assertThat(exception).isInstanceOf(JSONException::class.java)
  }

  @Test
  fun testGetReviewCard_fractionSubtopicId1_isSuccessful() {
    val reviewCardLiveData = topicController
      .getRevisionCard(FRACTIONS_TOPIC_ID, SUBTOPIC_TOPIC_ID_2)
    val reviewCardResult = reviewCardLiveData.value
    assertThat(reviewCardResult).isNotNull()
    assertThat(reviewCardResult!!.isSuccess()).isTrue()
    assertThat(reviewCardResult.getOrThrow().pageContents.html)
      .isEqualTo("<p>Description of subtopic is here.</p>")
  }

  @Test
  fun testRetrieveSubtopicTopic_validSubtopic_returnsSubtopicWithThumbnail() {
    topicController.getTopic(
      profileId1, FRACTIONS_TOPIC_ID
    ).toLiveData().observeForever(mockTopicObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetTopicSucceeded()
    val topic = topicResultCaptor.value!!.getOrThrow()
    assertThat(topic.subtopicList[0].subtopicThumbnail.thumbnailGraphic).isEqualTo(
      LessonThumbnailGraphic.WHAT_IS_A_FRACTION
    )
  }

  @Test
  fun testRetrieveQuestionsForSkillIds_returnsAllQuestions() {
    val questionsListProvider = topicController
      .retrieveQuestionsForSkillIds(
        listOf(TEST_SKILL_ID_0, TEST_SKILL_ID_1)
      )
    questionsListProvider.toLiveData().observeForever(mockQuestionListObserver)
    testCoroutineDispatchers.runCurrent()
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
  fun testRetrieveQuestionsForFractionsSkillId0_returnsAllQuestions() {
    val questionsListProvider = topicController
      .retrieveQuestionsForSkillIds(
        listOf(FRACTIONS_SKILL_ID_0)
      )
    questionsListProvider.toLiveData()
      .observeForever(mockQuestionListObserver)
    testCoroutineDispatchers.runCurrent()
    verify(mockQuestionListObserver).onChanged(questionListResultCaptor.capture())

    assertThat(questionListResultCaptor.value.isSuccess()).isTrue()
    val questionsList = questionListResultCaptor.value.getOrThrow()
    assertThat(questionsList.size).isEqualTo(4)
    val questionIds = questionsList.map { it.questionId }
    assertThat(questionIds).containsExactlyElementsIn(
      mutableListOf(
        FRACTIONS_QUESTION_ID_0, FRACTIONS_QUESTION_ID_1,
        FRACTIONS_QUESTION_ID_2, FRACTIONS_QUESTION_ID_3
      )
    )
  }

  @Test
  fun testRetrieveQuestionsForFractionsSkillId1_returnsAllQuestions() {
    val questionsListProvider = topicController
      .retrieveQuestionsForSkillIds(
        listOf(FRACTIONS_SKILL_ID_1)
      )
    questionsListProvider.toLiveData()
      .observeForever(mockQuestionListObserver)
    testCoroutineDispatchers.runCurrent()
    verify(mockQuestionListObserver).onChanged(questionListResultCaptor.capture())

    assertThat(questionListResultCaptor.value.isSuccess()).isTrue()
    val questionsList = questionListResultCaptor.value.getOrThrow()
    assertThat(questionsList.size).isEqualTo(3)
    val questionIds = questionsList.map { it.questionId }
    assertThat(questionIds).containsExactlyElementsIn(
      mutableListOf(
        FRACTIONS_QUESTION_ID_8, FRACTIONS_QUESTION_ID_9, FRACTIONS_QUESTION_ID_10
      )
    )
  }

  @Test
  fun testRetrieveQuestionsForFractionsSkillId2_returnsAllQuestions() {
    val questionsListProvider = topicController
      .retrieveQuestionsForSkillIds(
        listOf(FRACTIONS_SKILL_ID_2)
      )
    questionsListProvider.toLiveData()
      .observeForever(mockQuestionListObserver)
    testCoroutineDispatchers.runCurrent()
    verify(mockQuestionListObserver).onChanged(questionListResultCaptor.capture())

    assertThat(questionListResultCaptor.value.isSuccess()).isTrue()
    val questionsList = questionListResultCaptor.value.getOrThrow()
    assertThat(questionsList.size).isEqualTo(4)
    val questionIds = questionsList.map { it.questionId }
    assertThat(questionIds).containsExactlyElementsIn(
      mutableListOf(
        FRACTIONS_QUESTION_ID_4, FRACTIONS_QUESTION_ID_5,
        FRACTIONS_QUESTION_ID_6, FRACTIONS_QUESTION_ID_7
      )
    )
  }

  @Test
  fun testRetrieveQuestionsForRatiosSkillId0_returnsAllQuestions() {
    val questionsListProvider = topicController
      .retrieveQuestionsForSkillIds(
        listOf(RATIOS_SKILL_ID_0)
      )
    questionsListProvider.toLiveData()
      .observeForever(mockQuestionListObserver)
    testCoroutineDispatchers.runCurrent()
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
  fun testRetrieveQuestionsForInvalidSkillIds_returnsResultForValidSkillsOnly() {
    val questionsListProvider = topicController
      .retrieveQuestionsForSkillIds(
        listOf(TEST_SKILL_ID_0, TEST_SKILL_ID_1, "NON_EXISTENT_SKILL_ID")
      )
    questionsListProvider.toLiveData()
      .observeForever(mockQuestionListObserver)
    testCoroutineDispatchers.runCurrent()
    verify(mockQuestionListObserver).onChanged(questionListResultCaptor.capture())

    assertThat(questionListResultCaptor.value.isSuccess()).isTrue()
    val questionsList = questionListResultCaptor.value.getOrThrow()
    assertThat(questionsList.size).isEqualTo(5)
  }

  @Test
  fun testGetTopic_invalidTopicId_getTopic_noResultFound() {
    topicController.getTopic(
      profileId1, INVALID_TOPIC_ID_1
    ).toLiveData().observeForever(mockTopicObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetTopicFailed()
  }

  @Test
  fun testGetTopic_validTopicId_withoutAnyProgress_getTopicSucceedsWithCorrectProgress() {
    topicController.getTopic(
      profileId1, FRACTIONS_TOPIC_ID
    ).toLiveData().observeForever(mockTopicObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetTopicSucceeded()
    val topic = topicResultCaptor.value.getOrThrow()
    assertThat(topic.topicId).isEqualTo(FRACTIONS_TOPIC_ID)
    assertThat(topic.storyList[0].chapterList[0].chapterPlayState)
      .isEqualTo(ChapterPlayState.NOT_STARTED)
    assertThat(topic.storyList[0].chapterList[1].chapterPlayState)
      .isEqualTo(ChapterPlayState.NOT_PLAYABLE_MISSING_PREREQUISITES)
  }

  @Test
  fun testGetTopic_recordProgress_getTopic_correctProgressFound() {
    markFractionsStory0Chapter0AsCompleted()
    testCoroutineDispatchers.runCurrent()

    topicController.getTopic(
      profileId1, FRACTIONS_TOPIC_ID
    ).toLiveData().observeForever(mockTopicObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetTopicSucceeded()
    val topic = topicResultCaptor.value.getOrThrow()
    assertThat(topic.topicId).isEqualTo(FRACTIONS_TOPIC_ID)
    assertThat(topic.storyList[0].chapterList[0].chapterPlayState)
      .isEqualTo(ChapterPlayState.COMPLETED)
    assertThat(topic.storyList[0].chapterList[1].chapterPlayState)
      .isEqualTo(ChapterPlayState.NOT_STARTED)
  }

  @Test
  fun testGetStory_invalidData_getStory_noResultFound() {
    topicController.getStory(profileId1, INVALID_TOPIC_ID_1, INVALID_STORY_ID_1).toLiveData()
      .observeForever(mockStorySummaryObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetStoryFailed()
  }

  @Test
  fun testGetStory_validData_withoutAnyProgress_getStorySucceedsWithCorrectProgress() {
    topicController.getStory(profileId1, FRACTIONS_TOPIC_ID, FRACTIONS_STORY_ID_0).toLiveData()
      .observeForever(mockStorySummaryObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetStorySucceeded()
    val storySummary = storySummaryResultCaptor.value.getOrThrow()
    assertThat(storySummary.storyId).isEqualTo(FRACTIONS_STORY_ID_0)
    assertThat(storySummary.chapterList[0].chapterPlayState)
      .isEqualTo(ChapterPlayState.NOT_STARTED)
    assertThat(storySummary.chapterList[1].chapterPlayState)
      .isEqualTo(ChapterPlayState.NOT_PLAYABLE_MISSING_PREREQUISITES)
  }

  @Test
  fun testGetStory_recordProgress_getTopic_correctProgressFound() {
    markFractionsStory0Chapter0AsCompleted()
    testCoroutineDispatchers.runCurrent()

    topicController.getTopic(
      profileId1, FRACTIONS_TOPIC_ID
    ).toLiveData().observeForever(mockTopicObserver)
    testCoroutineDispatchers.runCurrent()

    verifyRecordProgressSucceeded()
    verifyGetTopicSucceeded()
    val topic = topicResultCaptor.value.getOrThrow()
    assertThat(topic.topicId).isEqualTo(FRACTIONS_TOPIC_ID)
    assertThat(topic.storyList[0].chapterList[0].chapterPlayState)
      .isEqualTo(ChapterPlayState.COMPLETED)
    assertThat(topic.storyList[0].chapterList[1].chapterPlayState)
      .isEqualTo(ChapterPlayState.NOT_STARTED)
  }

  @Test
  fun testOngoingTopicList_validData_withoutAnyProgress_ongoingTopicListIsEmpty() {
    topicController.getOngoingTopicList(
      profileId1
    ).toLiveData().observeForever(mockOngoingTopicListObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetOngoingTopicListSucceeded()
    val ongoingTopicList = ongoingTopicListResultCaptor.value.getOrThrow()
    assertThat(ongoingTopicList.topicCount).isEqualTo(0)
  }

  @Test
  fun testOngoingTopicList_recordOneChapterCompleted_correctOngoingList() {
    markFractionsStory0Chapter0AsCompleted()
    testCoroutineDispatchers.runCurrent()

    topicController.getOngoingTopicList(
      profileId1
    ).toLiveData().observeForever(mockOngoingTopicListObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetOngoingTopicListSucceeded()
    val ongoingTopicList = ongoingTopicListResultCaptor.value.getOrThrow()
    assertThat(ongoingTopicList.topicCount).isEqualTo(1)
    assertThat(ongoingTopicList.topicList[0].topicId).isEqualTo(FRACTIONS_TOPIC_ID)
  }

  @Test
  fun testOngoingTopicList_finishEntireTopic_ongoingTopicListIsEmpty() {
    markFractionsStory0Chapter0AsCompleted()
    testCoroutineDispatchers.runCurrent()

    markFractionsStory0Chapter1AsCompleted()
    testCoroutineDispatchers.runCurrent()

    topicController.getOngoingTopicList(
      profileId1
    ).toLiveData().observeForever(mockOngoingTopicListObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetOngoingTopicListSucceeded()
    val ongoingTopicList = ongoingTopicListResultCaptor.value.getOrThrow()
    assertThat(ongoingTopicList.topicCount).isEqualTo(0)
  }

  @Test
  fun testOngoingTopicList_finishOneEntireTopicAndOneChapterInOtherTopic_ongoingListIsCorrect() {
    // Mark entire FRACTIONS topic as finished.
    markFractionsStory0Chapter0AsCompleted()
    testCoroutineDispatchers.runCurrent()

    markFractionsStory0Chapter1AsCompleted()
    testCoroutineDispatchers.runCurrent()

    // Mark only one chapter in RATIOS topic as finished.
    markRatiosStory0Chapter0AsCompleted()
    testCoroutineDispatchers.runCurrent()

    topicController.getOngoingTopicList(
      profileId1
    ).toLiveData().observeForever(mockOngoingTopicListObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetOngoingTopicListSucceeded()
    val ongoingTopicList = ongoingTopicListResultCaptor.value.getOrThrow()
    assertThat(ongoingTopicList.topicCount).isEqualTo(1)
    assertThat(ongoingTopicList.topicList[0].topicId).isEqualTo(RATIOS_TOPIC_ID)
  }

  @Test
  fun testCompletedStoryList_validData_withoutAnyProgress_completedStoryListIsEmpty() {
    topicController.getCompletedStoryList(profileId1).toLiveData()
      .observeForever(mockCompletedStoryListObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetCompletedStoryListSucceeded()
    val completedStoryList = completedStoryListResultCaptor.value.getOrThrow()
    assertThat(completedStoryList.completedStoryCount).isEqualTo(0)
  }

  @Test
  fun testCompletedStoryList_recordOneChapterProgress_completedStoryListIsEmpty() {
    markFractionsStory0Chapter0AsCompleted()
    testCoroutineDispatchers.runCurrent()

    topicController.getCompletedStoryList(profileId1).toLiveData()
      .observeForever(mockCompletedStoryListObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetCompletedStoryListSucceeded()
    val completedStoryList = completedStoryListResultCaptor.value.getOrThrow()
    assertThat(completedStoryList.completedStoryCount).isEqualTo(0)
  }

  @Test
  fun testCompletedStoryList_finishEntireStory_completedStoryListIsCorrect() {
    markFractionsStory0Chapter0AsCompleted()
    testCoroutineDispatchers.runCurrent()

    markFractionsStory0Chapter1AsCompleted()
    testCoroutineDispatchers.runCurrent()

    topicController.getCompletedStoryList(profileId1).toLiveData()
      .observeForever(mockCompletedStoryListObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetCompletedStoryListSucceeded()
    val completedStoryList = completedStoryListResultCaptor.value.getOrThrow()
    assertThat(completedStoryList.completedStoryCount).isEqualTo(1)
    assertThat(completedStoryList.completedStoryList[0].storyId).isEqualTo(FRACTIONS_STORY_ID_0)
    assertThat(completedStoryList.completedStoryList[0].topicId).isEqualTo(FRACTIONS_TOPIC_ID)
  }

  @Test
  fun testCompletedStoryList_finishEntireStory_checkChapters_allAreCompleted() {
    markFractionsStory0Chapter0AsCompleted()
    testCoroutineDispatchers.runCurrent()

    markFractionsStory0Chapter1AsCompleted()
    testCoroutineDispatchers.runCurrent()

    topicController.getStory(profileId1, FRACTIONS_TOPIC_ID, FRACTIONS_STORY_ID_0).toLiveData()
      .observeForever(mockStorySummaryObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetStorySucceeded()
    val storySummary = storySummaryResultCaptor.value.getOrThrow()
    assertThat(storySummary.chapterCount).isEqualTo(2)
    assertThat(storySummary.chapterList[0].chapterPlayState).isEqualTo(ChapterPlayState.COMPLETED)
    assertThat(storySummary.chapterList[1].chapterPlayState).isEqualTo(ChapterPlayState.COMPLETED)
  }

  @Test
  fun testCompletedStoryList_finishOneStoryAndOneChapterInOtherStory_completedStoryListIsCorrect() {
    markFractionsStory0Chapter0AsCompleted()
    testCoroutineDispatchers.runCurrent()

    markRatiosStory0Chapter0AsCompleted()
    testCoroutineDispatchers.runCurrent()

    markRatiosStory0Chapter1AsCompleted()
    testCoroutineDispatchers.runCurrent()

    topicController.getCompletedStoryList(profileId1).toLiveData()
      .observeForever(mockCompletedStoryListObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetCompletedStoryListSucceeded()
    val completedStoryList = completedStoryListResultCaptor.value.getOrThrow()
    assertThat(completedStoryList.completedStoryCount).isEqualTo(1)
    assertThat(completedStoryList.completedStoryList[0].storyId).isEqualTo(RATIOS_STORY_ID_0)
    assertThat(completedStoryList.completedStoryList[0].topicId).isEqualTo(RATIOS_TOPIC_ID)
  }

  @Test
  fun testCompletedStoryList_finishTwoStories_completedStoryListIsCorrect() {
    markFractionsStory0Chapter0AsCompleted()
    testCoroutineDispatchers.runCurrent()

    markFractionsStory0Chapter1AsCompleted()
    testCoroutineDispatchers.runCurrent()

    markRatiosStory0Chapter0AsCompleted()
    testCoroutineDispatchers.runCurrent()

    markRatiosStory0Chapter1AsCompleted()
    testCoroutineDispatchers.runCurrent()

    topicController.getCompletedStoryList(profileId1).toLiveData()
      .observeForever(mockCompletedStoryListObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetCompletedStoryListSucceeded()
    val completedStoryList = completedStoryListResultCaptor.value.getOrThrow()
    assertThat(completedStoryList.completedStoryCount).isEqualTo(2)
    assertThat(completedStoryList.completedStoryList[0].storyId).isEqualTo(FRACTIONS_STORY_ID_0)
    assertThat(completedStoryList.completedStoryList[1].storyId).isEqualTo(RATIOS_STORY_ID_0)
  }

  @Test
  fun testGetRevisionCard_noTopicAndSubtopicId_returnsFailure_logsException() {
    topicController.getRevisionCard("", 0)

    val exception = fakeExceptionLogger.getMostRecentException()

    assertThat(exception).isInstanceOf(FileNotFoundException::class.java)
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  private fun markFractionsStory0Chapter0AsCompleted() {
    storyProgressController.recordCompletedChapter(
      profileId1,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_0,
      currentTimestamp
    ).toLiveData().observeForever(mockRecordProgressObserver)
  }

  private fun markFractionsStory0Chapter1AsCompleted() {
    storyProgressController.recordCompletedChapter(
      profileId1,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_1,
      currentTimestamp
    ).toLiveData().observeForever(mockRecordProgressObserver)
  }

  private fun markRatiosStory0Chapter0AsCompleted() {
    storyProgressController.recordCompletedChapter(
      profileId1,
      RATIOS_TOPIC_ID,
      RATIOS_STORY_ID_0,
      RATIOS_EXPLORATION_ID_0,
      currentTimestamp
    ).toLiveData().observeForever(mockRecordProgressObserver)
  }

  private fun markRatiosStory0Chapter1AsCompleted() {
    storyProgressController.recordCompletedChapter(
      profileId1,
      RATIOS_TOPIC_ID,
      RATIOS_STORY_ID_0,
      RATIOS_EXPLORATION_ID_1,
      currentTimestamp
    ).toLiveData().observeForever(mockRecordProgressObserver)
  }

  private fun verifyRecordProgressSucceeded() {
    verify(
      mockRecordProgressObserver,
      atLeastOnce()
    ).onChanged(recordProgressResultCaptor.capture())
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
    verify(
      mockOngoingTopicListObserver,
      atLeastOnce()
    ).onChanged(ongoingTopicListResultCaptor.capture())
    assertThat(ongoingTopicListResultCaptor.value.isSuccess()).isTrue()
  }

  private fun verifyGetCompletedStoryListSucceeded() {
    verify(
      mockCompletedStoryListObserver,
      atLeastOnce()
    ).onChanged(completedStoryListResultCaptor.capture())
    assertThat(completedStoryListResultCaptor.value.isSuccess()).isTrue()
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
  @Component(
    modules = [
      TestModule::class, TestLogReportingModule::class, LogStorageModule::class,
      TestDispatcherModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(topicControllerTest: TopicControllerTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerTopicControllerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(topicControllerTest: TopicControllerTest) {
      component.inject(topicControllerTest)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
