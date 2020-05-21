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
import org.oppia.app.model.ChapterSummaryDomain
import org.oppia.app.model.LessonThumbnail
import org.oppia.app.model.LessonThumbnailGraphic
import org.oppia.app.model.RevisionCardDomain
import org.oppia.app.model.RevisionCardView
import org.oppia.app.model.SkillSummaryDomain
import org.oppia.app.model.SkillSummaryView
import org.oppia.app.model.StorySummaryDomain
import org.oppia.app.model.StorySummaryView
import org.oppia.app.model.SubtitledHtml
import org.oppia.app.model.SubtopicDomain
import org.oppia.app.model.SubtopicView
import org.oppia.app.model.TopicDomain
import org.oppia.app.model.TopicSummaryListView
import org.oppia.app.model.TopicView
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

private const val DUMMY_DESCRIPTION_TEXT = "dummy_description_text"
private const val DUMMY_TITLE_TEXT = "dummy_title_text"
private const val DUMMY_THUMBNAIL_URL = "dummy_thumbnail_url"
private const val TEST_SUBTOPIC_ID_0 = "test_subtopic_id_0"
private const val TEST_SUBTOPIC_ID_1 = "test_subtopic_id_1"
private const val TEST_CONTENT_ID = "test_content_id"
private const val TEST_HTML_TEXT = "test_html_text"

/** Tests for [TopicRepository]. */
@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class TopicRepositoryTest {

  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Rule
  @JvmField
  val executorRule = InstantTaskExecutorRule()

  @Inject
  lateinit var topicRepository: TopicRepository

  @Mock
  lateinit var mockRevisionCardViewObserver: Observer<AsyncResult<RevisionCardView>>
  @Captor
  lateinit var revisionCardViewResultCaptor: ArgumentCaptor<AsyncResult<RevisionCardView>>

  @Mock
  lateinit var mockSkillSummaryViewObserver: Observer<AsyncResult<SkillSummaryView>>
  @Captor
  lateinit var skillSummaryViewResultCaptor: ArgumentCaptor<AsyncResult<SkillSummaryView>>

  @Mock
  lateinit var mockStorySummaryViewListObserver: Observer<AsyncResult<List<StorySummaryView>>>
  @Captor
  lateinit var storySummaryViewListResultCaptor: ArgumentCaptor<AsyncResult<List<StorySummaryView>>>

  @Mock
  lateinit var mockStorySummaryViewObserver: Observer<AsyncResult<StorySummaryView>>
  @Captor
  lateinit var storySummaryViewResultCaptor: ArgumentCaptor<AsyncResult<StorySummaryView>>

  @Mock
  lateinit var mockSubtopicViewListObserver: Observer<AsyncResult<List<SubtopicView>>>
  @Captor
  lateinit var subtopicViewListResultCaptor: ArgumentCaptor<AsyncResult<List<SubtopicView>>>

  @Mock
  lateinit var mockSubtopicViewObserver: Observer<AsyncResult<SubtopicView>>
  @Captor
  lateinit var subtopicViewResultCaptor: ArgumentCaptor<AsyncResult<SubtopicView>>

  @Mock
  lateinit var mockTopicSummaryListViewObserver: Observer<AsyncResult<TopicSummaryListView>>
  @Captor
  lateinit var topicSummaryListViewResultCaptor: ArgumentCaptor<AsyncResult<TopicSummaryListView>>

  @Mock
  lateinit var mockTopicViewObserver: Observer<AsyncResult<TopicView>>
  @Captor
  lateinit var topicViewResultCaptor: ArgumentCaptor<AsyncResult<TopicView>>

  @Inject
  lateinit var dataProviders: DataProviders

  @Inject
  @field:TestDispatcher
  lateinit var testDispatcher: CoroutineDispatcher

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
  @ExperimentalCoroutinesApi
  fun testRevisionCard_addRevisionCardList_getRevisionCard0_correctRevisionCardData() =
    runBlockingTest(coroutineContext) {
      topicRepository.addRevisionCardList(createRevisionCardList())
      advanceUntilIdle()

      val revisionCardDataProvider = topicRepository.getRevisionCardDataProvider(DUMMY_TITLE_TEXT)
      dataProviders.convertToLiveData(revisionCardDataProvider)
        .observeForever(mockRevisionCardViewObserver)
      advanceUntilIdle()

      verifyGetRevisionCardViewSucceeded()
      val revisionCardView = revisionCardViewResultCaptor.value.getOrThrow()
      assertThat(revisionCardView.subtopicTitle).isEqualTo(DUMMY_TITLE_TEXT)
      assertThat(revisionCardView.pageContents.contentId).isEqualTo(TEST_CONTENT_ID)
      assertThat(revisionCardView.pageContents.html).isEqualTo(TEST_HTML_TEXT)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun tesSkillSummary_addSkillSummaryList_getSkillSummary0_correctSkillSummaryData() =
    runBlockingTest(coroutineContext) {
      topicRepository.addSkillSummaryList(createSkillSummaryList())
      advanceUntilIdle()

      val skillSummaryDataProvider = topicRepository.getSkillSummaryDataProvider(TEST_SKILL_ID_0)
      dataProviders.convertToLiveData(skillSummaryDataProvider)
        .observeForever(mockSkillSummaryViewObserver)
      advanceUntilIdle()

      verifyGetSkillSummaryViewSucceeded()
      val skillSummaryView = skillSummaryViewResultCaptor.value.getOrThrow()
      assertThat(skillSummaryView.skillId).isEqualTo(TEST_SKILL_ID_0)
      assertThat(skillSummaryView.description).isEqualTo(DUMMY_DESCRIPTION_TEXT)
      assertThat(skillSummaryView.thumbnailUrl).isEqualTo(DUMMY_THUMBNAIL_URL)
      assertThat(skillSummaryView.skillThumbnail.thumbnailGraphic).isEqualTo(LessonThumbnailGraphic.ADDING_AND_SUBTRACTING_FRACTIONS)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun tesSkillSummary_addSkillSummaryList_getSkillSummary1_correctSkillSummaryData() =
    runBlockingTest(coroutineContext) {
      topicRepository.addSkillSummaryList(createSkillSummaryList())
      advanceUntilIdle()

      val skillSummaryDataProvider = topicRepository.getSkillSummaryDataProvider(TEST_SKILL_ID_1)
      dataProviders.convertToLiveData(skillSummaryDataProvider)
        .observeForever(mockSkillSummaryViewObserver)
      advanceUntilIdle()

      verifyGetSkillSummaryViewSucceeded()
      val skillSummaryView = skillSummaryViewResultCaptor.value.getOrThrow()
      assertThat(skillSummaryView.skillId).isEqualTo(TEST_SKILL_ID_1)
      assertThat(skillSummaryView.description).isEqualTo(DUMMY_DESCRIPTION_TEXT)
      assertThat(skillSummaryView.thumbnailUrl).isEqualTo(DUMMY_THUMBNAIL_URL)
      assertThat(skillSummaryView.skillThumbnail.thumbnailGraphic).isEqualTo(LessonThumbnailGraphic.IDENTIFYING_THE_PARTS_OF_A_FRACTION)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun tesStorySummary_addStorySummaryList_getStorySummary0_correctStorySummaryData() =
    runBlockingTest(coroutineContext) {
      topicRepository.addStorySummaryList(createStorySummaryList())
      advanceUntilIdle()

      val storySummaryDataProvider = topicRepository.getStorySummaryDataProvider(TEST_STORY_ID_0)
      dataProviders.convertToLiveData(storySummaryDataProvider)
        .observeForever(mockStorySummaryViewObserver)
      advanceUntilIdle()

      verifyGetStorySummaryViewSucceeded()
      val storySummaryView = storySummaryViewResultCaptor.value.getOrThrow()
      assertThat(storySummaryView.storyId).isEqualTo(TEST_STORY_ID_0)
      assertThat(storySummaryView.storyName).isEqualTo("First story")
      assertThat(storySummaryView.storyThumbnail.thumbnailGraphic).isEqualTo(LessonThumbnailGraphic.ADDING_AND_SUBTRACTING_FRACTIONS)
      assertThat(storySummaryView.chapterCount).isEqualTo(0)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun tesStorySummaryAndChapterSummary_addStorySummaryList_addChapterSummaryList_getStorySummary0_correctStorySummaryAndChapterSummaryData() =
    runBlockingTest(coroutineContext) {
      topicRepository.addStorySummaryList(createStorySummaryList())
      advanceUntilIdle()

      topicRepository.addChapterSummaryList(createChapterSummaryList())
      advanceUntilIdle()

      val storySummaryDataProvider = topicRepository.getStorySummaryDataProvider(TEST_STORY_ID_0)
      dataProviders.convertToLiveData(storySummaryDataProvider)
        .observeForever(mockStorySummaryViewObserver)
      advanceUntilIdle()

      verifyGetStorySummaryViewSucceeded()
      val storySummaryView = storySummaryViewResultCaptor.value.getOrThrow()
      assertThat(storySummaryView.storyId).isEqualTo(TEST_STORY_ID_0)
      assertThat(storySummaryView.storyName).isEqualTo("First story")
      assertThat(storySummaryView.storyThumbnail.thumbnailGraphic).isEqualTo(LessonThumbnailGraphic.ADDING_AND_SUBTRACTING_FRACTIONS)
      assertThat(storySummaryView.chapterCount).isEqualTo(1)

      val chapterSummaryView = storySummaryView.chapterList[0]
      assertThat(chapterSummaryView.explorationId).isEqualTo(TEST_EXPLORATION_ID_0)
      assertThat(chapterSummaryView.name).isEqualTo(DUMMY_TITLE_TEXT)
      assertThat(chapterSummaryView.summary).isEqualTo(DUMMY_DESCRIPTION_TEXT)
      assertThat(chapterSummaryView.chapterThumbnail.thumbnailGraphic).isEqualTo(
        LessonThumbnailGraphic.ADDING_AND_SUBTRACTING_FRACTIONS
      )
      assertThat(chapterSummaryView.chapterPlayState).isEqualTo(ChapterPlayState.NOT_PLAYABLE_MISSING_PREREQUISITES)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun tesStorySummary_addStorySummaryList_getStorySummary1_correctStorySummaryData() =
    runBlockingTest(coroutineContext) {
      topicRepository.addStorySummaryList(createStorySummaryList())
      advanceUntilIdle()

      val storySummaryDataProvider = topicRepository.getStorySummaryDataProvider(TEST_STORY_ID_1)
      dataProviders.convertToLiveData(storySummaryDataProvider)
        .observeForever(mockStorySummaryViewObserver)
      advanceUntilIdle()

      verifyGetStorySummaryViewSucceeded()
      val storySummaryView = storySummaryViewResultCaptor.value.getOrThrow()
      assertThat(storySummaryView.storyId).isEqualTo(TEST_STORY_ID_1)
      assertThat(storySummaryView.storyName).isEqualTo("Second story")
      assertThat(storySummaryView.storyThumbnail.thumbnailGraphic).isEqualTo(LessonThumbnailGraphic.IDENTIFYING_THE_PARTS_OF_A_FRACTION)
      assertThat(storySummaryView.chapterCount).isEqualTo(0)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun tesStorySummaryAndChapterSummary_addStorySummaryList_addChapterSummaryList_getStorySummary1_correctStorySummaryAndChapterSummaryData() =
    runBlockingTest(coroutineContext) {
      topicRepository.addStorySummaryList(createStorySummaryList())
      advanceUntilIdle()

      topicRepository.addChapterSummaryList(createChapterSummaryList())
      advanceUntilIdle()

      val storySummaryDataProvider = topicRepository.getStorySummaryDataProvider(TEST_STORY_ID_1)
      dataProviders.convertToLiveData(storySummaryDataProvider)
        .observeForever(mockStorySummaryViewObserver)
      advanceUntilIdle()

      verifyGetStorySummaryViewSucceeded()
      val storySummaryView = storySummaryViewResultCaptor.value.getOrThrow()
      assertThat(storySummaryView.storyId).isEqualTo(TEST_STORY_ID_1)
      assertThat(storySummaryView.storyName).isEqualTo("Second story")
      assertThat(storySummaryView.storyThumbnail.thumbnailGraphic).isEqualTo(LessonThumbnailGraphic.IDENTIFYING_THE_PARTS_OF_A_FRACTION)
      assertThat(storySummaryView.chapterCount).isEqualTo(1)

      val chapterSummaryView = storySummaryView.chapterList[0]
      assertThat(chapterSummaryView.explorationId).isEqualTo(TEST_EXPLORATION_ID_1)
      assertThat(chapterSummaryView.name).isEqualTo(DUMMY_TITLE_TEXT)
      assertThat(chapterSummaryView.summary).isEqualTo(DUMMY_DESCRIPTION_TEXT)
      assertThat(chapterSummaryView.chapterThumbnail.thumbnailGraphic).isEqualTo(
        LessonThumbnailGraphic.IDENTIFYING_THE_PARTS_OF_A_FRACTION
      )
      assertThat(chapterSummaryView.chapterPlayState).isEqualTo(ChapterPlayState.NOT_PLAYABLE_MISSING_PREREQUISITES)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun tesStorySummary_addStorySummaryList_getStorySummaryList_correctStorySummaryData() =
    runBlockingTest(coroutineContext) {
      topicRepository.addStorySummaryList(createStorySummaryList())
      advanceUntilIdle()

      val storySummaryListDataProvider =
        topicRepository.getStorySummaryListDataProvider(TEST_TOPIC_ID_0)
      dataProviders.convertToLiveData(storySummaryListDataProvider)
        .observeForever(mockStorySummaryViewListObserver)
      advanceUntilIdle()

      verifyGetStorySummaryViewListSucceeded()
      val storySummaryViewList = storySummaryViewListResultCaptor.value.getOrThrow()
      assertThat(storySummaryViewList.size).isEqualTo(2)

      val storySummaryView0 = storySummaryViewList[0]
      assertThat(storySummaryView0.storyId).isEqualTo(TEST_STORY_ID_0)
      assertThat(storySummaryView0.storyName).isEqualTo("First story")
      assertThat(storySummaryView0.storyThumbnail.thumbnailGraphic).isEqualTo(LessonThumbnailGraphic.ADDING_AND_SUBTRACTING_FRACTIONS)
      assertThat(storySummaryView0.chapterCount).isEqualTo(0)

      val storySummaryView1 = storySummaryViewList[1]
      assertThat(storySummaryView1.storyId).isEqualTo(TEST_STORY_ID_1)
      assertThat(storySummaryView1.storyName).isEqualTo("Second story")
      assertThat(storySummaryView1.storyThumbnail.thumbnailGraphic).isEqualTo(LessonThumbnailGraphic.IDENTIFYING_THE_PARTS_OF_A_FRACTION)
      assertThat(storySummaryView1.chapterCount).isEqualTo(0)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun tesStorySummaryAndChapterSummary_addStorySummaryList_getStorySummaryList_correctStorySummaryAndChapterSummaryData() =
    runBlockingTest(coroutineContext) {
      topicRepository.addStorySummaryList(createStorySummaryList())
      advanceUntilIdle()

      topicRepository.addChapterSummaryList(createChapterSummaryList())
      advanceUntilIdle()

      val storySummaryListDataProvider =
        topicRepository.getStorySummaryListDataProvider(TEST_TOPIC_ID_0)
      dataProviders.convertToLiveData(storySummaryListDataProvider)
        .observeForever(mockStorySummaryViewListObserver)
      advanceUntilIdle()

      verifyGetStorySummaryViewListSucceeded()
      val storySummaryViewList = storySummaryViewListResultCaptor.value.getOrThrow()
      assertThat(storySummaryViewList.size).isEqualTo(2)

      val storySummaryView0 = storySummaryViewList[0]
      assertThat(storySummaryView0.storyId).isEqualTo(TEST_STORY_ID_0)
      assertThat(storySummaryView0.storyName).isEqualTo("First story")
      assertThat(storySummaryView0.storyThumbnail.thumbnailGraphic).isEqualTo(LessonThumbnailGraphic.ADDING_AND_SUBTRACTING_FRACTIONS)
      assertThat(storySummaryView0.chapterCount).isEqualTo(1)

      val chapterSummaryView0 = storySummaryView0.chapterList[0]
      assertThat(chapterSummaryView0.explorationId).isEqualTo(TEST_EXPLORATION_ID_0)
      assertThat(chapterSummaryView0.name).isEqualTo(DUMMY_TITLE_TEXT)
      assertThat(chapterSummaryView0.summary).isEqualTo(DUMMY_DESCRIPTION_TEXT)
      assertThat(chapterSummaryView0.chapterThumbnail.thumbnailGraphic).isEqualTo(
        LessonThumbnailGraphic.ADDING_AND_SUBTRACTING_FRACTIONS
      )
      assertThat(chapterSummaryView0.chapterPlayState).isEqualTo(ChapterPlayState.NOT_PLAYABLE_MISSING_PREREQUISITES)

      val storySummaryView1 = storySummaryViewList[1]
      assertThat(storySummaryView1.storyId).isEqualTo(TEST_STORY_ID_1)
      assertThat(storySummaryView1.storyName).isEqualTo("Second story")
      assertThat(storySummaryView1.storyThumbnail.thumbnailGraphic).isEqualTo(LessonThumbnailGraphic.IDENTIFYING_THE_PARTS_OF_A_FRACTION)
      assertThat(storySummaryView1.chapterCount).isEqualTo(1)

      val chapterSummaryView1 = storySummaryView1.chapterList[0]
      assertThat(chapterSummaryView1.explorationId).isEqualTo(TEST_EXPLORATION_ID_1)
      assertThat(chapterSummaryView1.name).isEqualTo(DUMMY_TITLE_TEXT)
      assertThat(chapterSummaryView1.summary).isEqualTo(DUMMY_DESCRIPTION_TEXT)
      assertThat(chapterSummaryView1.chapterThumbnail.thumbnailGraphic).isEqualTo(
        LessonThumbnailGraphic.IDENTIFYING_THE_PARTS_OF_A_FRACTION
      )
      assertThat(chapterSummaryView1.chapterPlayState).isEqualTo(ChapterPlayState.NOT_PLAYABLE_MISSING_PREREQUISITES)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testSubtopic_addSubtopicList_getSubtopic0_correctSubtopicData() =
    runBlockingTest(coroutineContext) {
      topicRepository.addSubTopicList(createSubtopicList())
      advanceUntilIdle()

      val subtopicDataProvider = topicRepository.getSubtopicDataProvider(TEST_SUBTOPIC_ID_0)
      dataProviders.convertToLiveData(subtopicDataProvider)
        .observeForever(mockSubtopicViewObserver)
      advanceUntilIdle()

      verifyGetSubtopicViewSucceeded()
      val subtopicView = subtopicViewResultCaptor.value.getOrThrow()
      assertThat(subtopicView.subtopicId).isEqualTo(TEST_SUBTOPIC_ID_0)
      assertThat(subtopicView.title).isEqualTo(DUMMY_TITLE_TEXT)
      assertThat(subtopicView.skillIdsList.size).isEqualTo(createSkillIdsList().size)
      assertThat(subtopicView.thumbnailUrl).isEqualTo(DUMMY_THUMBNAIL_URL)
      assertThat(subtopicView.subtopicThumbnail.thumbnailGraphic).isEqualTo(LessonThumbnailGraphic.ADDING_AND_SUBTRACTING_FRACTIONS)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testSubtopic_addSubtopicList_getSubtopic1_correctSubtopicData() =
    runBlockingTest(coroutineContext) {
      topicRepository.addSubTopicList(createSubtopicList())
      advanceUntilIdle()

      val subtopicDataProvider = topicRepository.getSubtopicDataProvider(TEST_SUBTOPIC_ID_1)
      dataProviders.convertToLiveData(subtopicDataProvider)
        .observeForever(mockSubtopicViewObserver)
      advanceUntilIdle()

      verifyGetSubtopicViewSucceeded()
      val subtopicView = subtopicViewResultCaptor.value.getOrThrow()
      assertThat(subtopicView.subtopicId).isEqualTo(TEST_SUBTOPIC_ID_1)
      assertThat(subtopicView.title).isEqualTo(DUMMY_TITLE_TEXT)
      assertThat(subtopicView.skillIdsList.size).isEqualTo(createSkillIdsList().size)
      assertThat(subtopicView.thumbnailUrl).isEqualTo(DUMMY_THUMBNAIL_URL)
      assertThat(subtopicView.subtopicThumbnail.thumbnailGraphic).isEqualTo(LessonThumbnailGraphic.IDENTIFYING_THE_PARTS_OF_A_FRACTION)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testSubtopic_addSubtopicList_getSubtopicList_correctSubtopicData() =
    runBlockingTest(coroutineContext) {
      topicRepository.addSubTopicList(createSubtopicList())
      advanceUntilIdle()

      val subtopicListDataProvider = topicRepository.getSubtopicListDataProvider(TEST_TOPIC_ID_0)
      dataProviders.convertToLiveData(subtopicListDataProvider)
        .observeForever(mockSubtopicViewListObserver)
      advanceUntilIdle()

      verifyGetSubtopicViewListSucceeded()
      val subtopicViewList = subtopicViewListResultCaptor.value.getOrThrow()
      assertThat(subtopicViewList.size).isEqualTo(2)

      val subtopicView0 = subtopicViewList[0]
      assertThat(subtopicView0.subtopicId).isEqualTo(TEST_SUBTOPIC_ID_0)
      assertThat(subtopicView0.title).isEqualTo(DUMMY_TITLE_TEXT)
      assertThat(subtopicView0.skillIdsList.size).isEqualTo(createSkillIdsList().size)
      assertThat(subtopicView0.thumbnailUrl).isEqualTo(DUMMY_THUMBNAIL_URL)
      assertThat(subtopicView0.subtopicThumbnail.thumbnailGraphic).isEqualTo(LessonThumbnailGraphic.ADDING_AND_SUBTRACTING_FRACTIONS)

      val subtopicView1 = subtopicViewList[1]
      assertThat(subtopicView1.subtopicId).isEqualTo(TEST_SUBTOPIC_ID_1)
      assertThat(subtopicView1.title).isEqualTo(DUMMY_TITLE_TEXT)
      assertThat(subtopicView1.skillIdsList.size).isEqualTo(createSkillIdsList().size)
      assertThat(subtopicView1.thumbnailUrl).isEqualTo(DUMMY_THUMBNAIL_URL)
      assertThat(subtopicView1.subtopicThumbnail.thumbnailGraphic).isEqualTo(LessonThumbnailGraphic.IDENTIFYING_THE_PARTS_OF_A_FRACTION)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testTopic_addTopic0_getTopic0_correctTopicData() =
    runBlockingTest(coroutineContext) {
      topicRepository.addTopic(createTopic0())
      advanceUntilIdle()

      val topicDataProvider = topicRepository.getTopicDataProvider(TEST_TOPIC_ID_0)
      dataProviders.convertToLiveData(topicDataProvider)
        .observeForever(mockTopicViewObserver)
      advanceUntilIdle()

      verifyGetTopicViewSucceeded()
      val topicView = topicViewResultCaptor.value.getOrThrow()
      assertThat(topicView.topicId).isEqualTo(TEST_TOPIC_ID_0)
      assertThat(topicView.name).isEqualTo(DUMMY_TITLE_TEXT)
      assertThat(topicView.description).isEqualTo(DUMMY_DESCRIPTION_TEXT)
      assertThat(topicView.diskSizeBytes).isEqualTo(1024)
      assertThat(topicView.topicThumbnail.thumbnailGraphic).isEqualTo(LessonThumbnailGraphic.ADDING_AND_SUBTRACTING_FRACTIONS)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testTopic_addTopic1_getTopic1_correctTopicData() =
    runBlockingTest(coroutineContext) {
      topicRepository.addTopic(createTopic1())
      advanceUntilIdle()

      val topicDataProvider = topicRepository.getTopicDataProvider(TEST_TOPIC_ID_1)
      dataProviders.convertToLiveData(topicDataProvider)
        .observeForever(mockTopicViewObserver)
      advanceUntilIdle()

      verifyGetTopicViewSucceeded()
      val topicView = topicViewResultCaptor.value.getOrThrow()
      assertThat(topicView.topicId).isEqualTo(TEST_TOPIC_ID_1)
      assertThat(topicView.name).isEqualTo(DUMMY_TITLE_TEXT)
      assertThat(topicView.description).isEqualTo(DUMMY_DESCRIPTION_TEXT)
      assertThat(topicView.diskSizeBytes).isEqualTo(2048)
      assertThat(topicView.topicThumbnail.thumbnailGraphic).isEqualTo(LessonThumbnailGraphic.IDENTIFYING_THE_PARTS_OF_A_FRACTION)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testTopic_addTopics_getTopicSummaryList_correctTopicSummaryListData() =
    runBlockingTest(coroutineContext) {
      topicRepository.initialiseAllTopics()
      advanceUntilIdle()

      val topicSummaryListViewDataProvider = topicRepository.getTopicSummaryListDataProvider()
      dataProviders.convertToLiveData(topicSummaryListViewDataProvider)
        .observeForever(mockTopicSummaryListViewObserver)
      advanceUntilIdle()

      verifyGetTopicSummaryListViewSucceeded()
      val topicSummaryListView = topicSummaryListViewResultCaptor.value.getOrThrow()
      assertThat(topicSummaryListView.topicSummaryCount).isEqualTo(2)

      val topicSummaryView0 = topicSummaryListView.getTopicSummary(0)
      assertThat(topicSummaryView0.topicId).isEqualTo(FRACTIONS_TOPIC_ID)
      assertThat(topicSummaryView0.name).isEqualTo("Fractions")
      assertThat(topicSummaryView0.totalChapterCount).isEqualTo(1)
      assertThat(topicSummaryView0.topicThumbnail.thumbnailGraphic).isEqualTo(LessonThumbnailGraphic.CHILD_WITH_FRACTIONS_HOMEWORK)

      val topicSummaryView1 = topicSummaryListView.getTopicSummary(1)
      assertThat(topicSummaryView1.topicId).isEqualTo(RATIOS_TOPIC_ID)
      assertThat(topicSummaryView1.name).isEqualTo("Ratios and Proportional Reasoning")
      assertThat(topicSummaryView1.totalChapterCount).isEqualTo(2)
      assertThat(topicSummaryView1.topicThumbnail.thumbnailGraphic).isEqualTo(LessonThumbnailGraphic.DUCK_AND_CHICKEN)
    }

  private fun setUpTestApplicationComponent() {
    DaggerTopicRepositoryTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  private fun verifyGetRevisionCardViewSucceeded() {
    verify(
      mockRevisionCardViewObserver,
      atLeastOnce()
    ).onChanged(revisionCardViewResultCaptor.capture())
    assertThat(revisionCardViewResultCaptor.value.isSuccess()).isTrue()
  }

  private fun verifyGetSkillSummaryViewSucceeded() {
    verify(
      mockSkillSummaryViewObserver,
      atLeastOnce()
    ).onChanged(skillSummaryViewResultCaptor.capture())
    assertThat(skillSummaryViewResultCaptor.value.isSuccess()).isTrue()
  }

  private fun verifyGetStorySummaryViewListSucceeded() {
    verify(
      mockStorySummaryViewListObserver,
      atLeastOnce()
    ).onChanged(storySummaryViewListResultCaptor.capture())
    assertThat(storySummaryViewListResultCaptor.value.isSuccess()).isTrue()
  }

  private fun verifyGetStorySummaryViewSucceeded() {
    verify(
      mockStorySummaryViewObserver,
      atLeastOnce()
    ).onChanged(storySummaryViewResultCaptor.capture())
    assertThat(storySummaryViewResultCaptor.value.isSuccess()).isTrue()
  }

  private fun verifyGetSubtopicViewListSucceeded() {
    verify(
      mockSubtopicViewListObserver,
      atLeastOnce()
    ).onChanged(subtopicViewListResultCaptor.capture())
    assertThat(subtopicViewListResultCaptor.value.isSuccess()).isTrue()
  }

  private fun verifyGetSubtopicViewSucceeded() {
    verify(mockSubtopicViewObserver, atLeastOnce()).onChanged(subtopicViewResultCaptor.capture())
    assertThat(subtopicViewResultCaptor.value.isSuccess()).isTrue()
  }

  private fun verifyGetTopicSummaryListViewSucceeded() {
    verify(mockTopicSummaryListViewObserver, atLeastOnce()).onChanged(
      topicSummaryListViewResultCaptor.capture()
    )
    assertThat(topicSummaryListViewResultCaptor.value.isSuccess()).isTrue()
  }

  private fun verifyGetTopicViewSucceeded() {
    verify(mockTopicViewObserver, atLeastOnce()).onChanged(topicViewResultCaptor.capture())
    assertThat(topicViewResultCaptor.value.isSuccess()).isTrue()
  }

  private fun createChapterSummaryList(): List<ChapterSummaryDomain> {
    val chapterSummaryDomainList = mutableListOf<ChapterSummaryDomain>()
    chapterSummaryDomainList.add(createStory0ChapterSummary0())
    chapterSummaryDomainList.add(createStory1ChapterSummary0())
    return chapterSummaryDomainList
  }

  private fun createStory0ChapterSummary0(): ChapterSummaryDomain {
    return ChapterSummaryDomain.newBuilder()
      .setStoryId(TEST_STORY_ID_0)
      .setTopicId(TEST_TOPIC_ID_0)
      .setExplorationId(TEST_EXPLORATION_ID_0)
      .setName(DUMMY_TITLE_TEXT)
      .setSummary(DUMMY_DESCRIPTION_TEXT)
      .setChapterPlayState(ChapterPlayState.NOT_PLAYABLE_MISSING_PREREQUISITES)
      .setChapterThumbnail(createThumbnail0())
      .build()
  }

  private fun createStory1ChapterSummary0(): ChapterSummaryDomain {
    return ChapterSummaryDomain.newBuilder()
      .setStoryId(TEST_STORY_ID_1)
      .setTopicId(TEST_TOPIC_ID_0)
      .setExplorationId(TEST_EXPLORATION_ID_1)
      .setName(DUMMY_TITLE_TEXT)
      .setSummary(DUMMY_DESCRIPTION_TEXT)
      .setChapterPlayState(ChapterPlayState.NOT_PLAYABLE_MISSING_PREREQUISITES)
      .setChapterThumbnail(createThumbnail1())
      .build()
  }

  private fun createRevisionCardList(): List<RevisionCardDomain> {
    val revisionCardDomainList = mutableListOf<RevisionCardDomain>()
    revisionCardDomainList.add(createRevisionCard0())
    return revisionCardDomainList
  }

  private fun createRevisionCard0(): RevisionCardDomain {
    return RevisionCardDomain.newBuilder()
      .setSubtopicTitle(DUMMY_TITLE_TEXT)
      .setPageContents(createRevisionCardPageContent0())
      .build()
  }

  private fun createRevisionCardPageContent0(): SubtitledHtml {
    return SubtitledHtml.newBuilder()
      .setContentId(TEST_CONTENT_ID)
      .setHtml(TEST_HTML_TEXT)
      .build()
  }

  private fun createSkillSummaryList(): List<SkillSummaryDomain> {
    val skillSummaryDomainList = mutableListOf<SkillSummaryDomain>()
    skillSummaryDomainList.add(createSkillSummary0())
    skillSummaryDomainList.add(createSkillSummary1())
    return skillSummaryDomainList
  }

  private fun createSkillSummary0(): SkillSummaryDomain {
    return SkillSummaryDomain.newBuilder()
      .setSkillId(TEST_SKILL_ID_0)
      .setDescription(DUMMY_DESCRIPTION_TEXT)
      .setSkillThumbnail(createThumbnail0())
      .setThumbnailUrl(DUMMY_THUMBNAIL_URL)
      .build()
  }

  private fun createSkillSummary1(): SkillSummaryDomain {
    return SkillSummaryDomain.newBuilder()
      .setSkillId(TEST_SKILL_ID_1)
      .setDescription(DUMMY_DESCRIPTION_TEXT)
      .setSkillThumbnail(createThumbnail1())
      .setThumbnailUrl(DUMMY_THUMBNAIL_URL)
      .build()
  }

  private fun createStorySummaryList(): List<StorySummaryDomain> {
    val storySummaryDomainList = mutableListOf<StorySummaryDomain>()
    storySummaryDomainList.add(createStorySummary0())
    storySummaryDomainList.add(createStorySummary1())
    return storySummaryDomainList
  }

  private fun createStorySummary0(): StorySummaryDomain {
    return StorySummaryDomain.newBuilder()
      .setStoryId(TEST_STORY_ID_0)
      .setStoryName("First story")
      .setTopicId(TEST_TOPIC_ID_0)
      .setStoryThumbnail(createThumbnail0())
      .build()
  }

  private fun createStorySummary1(): StorySummaryDomain {
    return StorySummaryDomain.newBuilder()
      .setStoryId(TEST_STORY_ID_1)
      .setStoryName("Second story")
      .setTopicId(TEST_TOPIC_ID_0)
      .setStoryThumbnail(createThumbnail1())
      .build()
  }

  private fun createSubtopicList(): List<SubtopicDomain> {
    val subtopicDomainList = mutableListOf<SubtopicDomain>()
    subtopicDomainList.add(createSubtopic0())
    subtopicDomainList.add(createSubtopic1())
    return subtopicDomainList
  }

  private fun createSubtopic0(): SubtopicDomain {
    return SubtopicDomain.newBuilder()
      .setSubtopicId(TEST_SUBTOPIC_ID_0)
      .setTopicId(TEST_TOPIC_ID_0)
      .setTitle(DUMMY_TITLE_TEXT)
      .setSubtopicThumbnail(createThumbnail0())
      .setThumbnailUrl(DUMMY_THUMBNAIL_URL)
      .addAllSkillIds(createSkillIdsList())
      .build()
  }

  private fun createSubtopic1(): SubtopicDomain {
    return SubtopicDomain.newBuilder()
      .setSubtopicId(TEST_SUBTOPIC_ID_1)
      .setTopicId(TEST_TOPIC_ID_0)
      .setTitle(DUMMY_TITLE_TEXT)
      .setSubtopicThumbnail(createThumbnail1())
      .setThumbnailUrl(DUMMY_THUMBNAIL_URL)
      .addAllSkillIds(createSkillIdsList())
      .build()
  }

  private fun createTopic0(): TopicDomain {
    return TopicDomain.newBuilder()
      .setTopicId(TEST_TOPIC_ID_0)
      .setName(DUMMY_TITLE_TEXT)
      .setDescription(DUMMY_DESCRIPTION_TEXT)
      .setDiskSizeBytes(1024)
      .setTopicThumbnail(createThumbnail0())
      .build()
  }

  private fun createTopic1(): TopicDomain {
    return TopicDomain.newBuilder()
      .setTopicId(TEST_TOPIC_ID_1)
      .setName(DUMMY_TITLE_TEXT)
      .setDescription(DUMMY_DESCRIPTION_TEXT)
      .setDiskSizeBytes(2048)
      .setTopicThumbnail(createThumbnail1())
      .build()
  }

  private fun createThumbnail0(): LessonThumbnail {
    return LessonThumbnail.newBuilder()
      .setThumbnailGraphic(LessonThumbnailGraphic.ADDING_AND_SUBTRACTING_FRACTIONS)
      .build()
  }

  private fun createThumbnail1(): LessonThumbnail {
    return LessonThumbnail.newBuilder()
      .setThumbnailGraphic(LessonThumbnailGraphic.IDENTIFYING_THE_PARTS_OF_A_FRACTION)
      .build()
  }

  private fun createSkillIdsList(): List<String> {
    val skillIdsList = mutableListOf<String>()
    skillIdsList.add(TEST_SKILL_ID_0)
    skillIdsList.add(TEST_SKILL_ID_1)
    skillIdsList.add(TEST_SKILL_ID_2)
    return skillIdsList
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

    fun inject(topicRepositoryTest: TopicRepositoryTest)
  }
}
