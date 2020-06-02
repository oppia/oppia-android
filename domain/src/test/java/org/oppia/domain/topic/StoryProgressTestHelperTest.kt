package org.oppia.domain.topic

import android.app.Application
import android.content.Context
import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.google.firebase.crashlytics.FirebaseCrashlytics
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
import org.mockito.Mockito
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.oppia.app.model.ChapterPlayState
import org.oppia.app.model.CompletedStoryList
import org.oppia.app.model.OngoingStoryList
import org.oppia.app.model.OngoingTopicList
import org.oppia.app.model.ProfileId
import org.oppia.app.model.StorySummary
import org.oppia.app.model.Topic
import org.oppia.util.caching.CacheAssetsLocally
import org.oppia.util.data.AsyncResult
import org.oppia.util.firebase.CrashlyticsWrapper
import org.oppia.util.logging.EnableConsoleLog
import org.oppia.util.logging.EnableFileLog
import org.oppia.util.logging.GlobalLogLevel
import org.oppia.util.logging.LogLevel
import org.oppia.util.parser.DefaultGcsPrefix
import org.oppia.util.parser.DefaultGcsResource
import org.oppia.util.parser.ImageDownloadUrlTemplate
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import org.robolectric.annotation.Config
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlin.coroutines.EmptyCoroutineContext

/** Tests for [StoryProgressTestHelper]. */
@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class StoryProgressTestHelperTest {
  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Inject lateinit var context: Context

  @Inject lateinit var storyProgressTestHelper: StoryProgressTestHelper

  @Inject lateinit var topicController: TopicController

  @Inject lateinit var topicListController: TopicListController

  @Mock lateinit var mockCompletedStoryListObserver: Observer<AsyncResult<CompletedStoryList>>
  @Captor lateinit var completedStoryListResultCaptor: ArgumentCaptor<AsyncResult<CompletedStoryList>>

  @Mock lateinit var mockOngoingStoryListObserver: Observer<AsyncResult<OngoingStoryList>>
  @Captor lateinit var ongoingStoryListResultCaptor: ArgumentCaptor<AsyncResult<OngoingStoryList>>

  @Mock lateinit var mockOngoingTopicListObserver: Observer<AsyncResult<OngoingTopicList>>
  @Captor lateinit var ongoingTopicListResultCaptor: ArgumentCaptor<AsyncResult<OngoingTopicList>>

  @Mock lateinit var mockStorySummaryObserver: Observer<AsyncResult<StorySummary>>
  @Captor lateinit var storySummaryResultCaptor: ArgumentCaptor<AsyncResult<StorySummary>>

  @Mock lateinit var mockTopicObserver: Observer<AsyncResult<Topic>>
  @Captor lateinit var topicResultCaptor: ArgumentCaptor<AsyncResult<Topic>>

  @Inject
  @field:TestDispatcher
  lateinit var testDispatcher: CoroutineDispatcher

  private val coroutineContext by lazy {
    EmptyCoroutineContext + testDispatcher
  }

  // https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-test/
  @ObsoleteCoroutinesApi
  private val testThread = newSingleThreadContext("TestMain")

  private lateinit var profileId: ProfileId

  @Before
  @ExperimentalCoroutinesApi
  @ObsoleteCoroutinesApi
  fun setUp() {
    profileId = ProfileId.newBuilder().setInternalId(0).build()
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

  private fun setUpTestApplicationComponent() {
    DaggerStoryProgressTestHelperTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  @Test
  @ExperimentalCoroutinesApi
  fun testStoryProgressTestHelper_markPartialStoryProgressForFractions_getTopicIsCorrect() =
    runBlockingTest(coroutineContext) {

      storyProgressTestHelper.markPartialStoryProgressForFractions(profileId, /* timestampOlderThanAWeek= */ false)
      advanceUntilIdle()

      topicController.getTopic(profileId, FRACTIONS_TOPIC_ID).observeForever(mockTopicObserver)
      advanceUntilIdle()

      verifyGetTopicSucceeded()

      val topic = topicResultCaptor.value.getOrThrow()
      assertThat(topic.topicId).isEqualTo(FRACTIONS_TOPIC_ID)
      assertThat(topic.storyList[0].chapterList[0].chapterPlayState).isEqualTo(ChapterPlayState.COMPLETED)
      assertThat(topic.storyList[0].chapterList[1].chapterPlayState).isEqualTo(ChapterPlayState.NOT_STARTED)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testStoryProgressTestHelper_markPartialStoryProgressForFractions_getStoryIsCorrect() =
    runBlockingTest(coroutineContext) {

      storyProgressTestHelper.markPartialStoryProgressForFractions(profileId, /* timestampOlderThanAWeek= */ false)
      advanceUntilIdle()

      topicController.getStory(profileId, FRACTIONS_TOPIC_ID, FRACTIONS_STORY_ID_0)
        .observeForever(mockStorySummaryObserver)
      advanceUntilIdle()

      verifyGetStorySucceeded()

      val storySummary = storySummaryResultCaptor.value.getOrThrow()
      assertThat(storySummary.storyId).isEqualTo(FRACTIONS_STORY_ID_0)
      assertThat(storySummary.chapterList[0].chapterPlayState).isEqualTo(ChapterPlayState.COMPLETED)
      assertThat(storySummary.chapterList[1].chapterPlayState).isEqualTo(ChapterPlayState.NOT_STARTED)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testStoryProgressTestHelper_markPartialStoryProgressForFractions_getOngoingTopicListIsCorrect() =
    runBlockingTest(coroutineContext) {

      storyProgressTestHelper.markPartialStoryProgressForFractions(profileId, /* timestampOlderThanAWeek= */ false)
      advanceUntilIdle()

      topicController.getOngoingTopicList(profileId).observeForever(mockOngoingTopicListObserver)
      advanceUntilIdle()

      verifyGetOngoingTopicListSucceeded()

      val ongoingTopicList = ongoingTopicListResultCaptor.value.getOrThrow()
      assertThat(ongoingTopicList.topicList.size).isEqualTo(1)
      assertThat(ongoingTopicList.topicList[0].topicId).isEqualTo(FRACTIONS_TOPIC_ID)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testStoryProgressTestHelper_markPartialStoryProgressForFractions_getCompletedStoryListIsCorrect() =
    runBlockingTest(coroutineContext) {

      storyProgressTestHelper.markPartialStoryProgressForFractions(profileId, /* timestampOlderThanAWeek= */ false)
      advanceUntilIdle()

      topicController.getCompletedStoryList(profileId).observeForever(mockCompletedStoryListObserver)
      advanceUntilIdle()

      verifyGetCompletedStoryListSucceeded()

      val completedStoryList = completedStoryListResultCaptor.value.getOrThrow()
      assertThat(completedStoryList.completedStoryList.size).isEqualTo(0)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testStoryProgressTestHelper_markPartialTopicProgressForFractions_getTopicIsCorrect() =
    runBlockingTest(coroutineContext) {

      storyProgressTestHelper.markPartialTopicProgressForFractions(profileId, /* timestampOlderThanAWeek= */ false)
      advanceUntilIdle()

      topicController.getTopic(profileId, FRACTIONS_TOPIC_ID).observeForever(mockTopicObserver)
      advanceUntilIdle()

      verifyGetTopicSucceeded()

      val topic = topicResultCaptor.value.getOrThrow()
      assertThat(topic.topicId).isEqualTo(FRACTIONS_TOPIC_ID)
      assertThat(topic.storyList[0].chapterList[0].chapterPlayState).isEqualTo(ChapterPlayState.COMPLETED)
      assertThat(topic.storyList[0].chapterList[1].chapterPlayState).isEqualTo(ChapterPlayState.NOT_STARTED)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testStoryProgressTestHelper_markPartialTopicProgressForFractions_getStoryIsCorrect() =
    runBlockingTest(coroutineContext) {

      storyProgressTestHelper.markPartialTopicProgressForFractions(profileId, /* timestampOlderThanAWeek= */ false)
      advanceUntilIdle()

      topicController.getStory(profileId, FRACTIONS_TOPIC_ID, FRACTIONS_STORY_ID_0)
        .observeForever(mockStorySummaryObserver)
      advanceUntilIdle()

      verifyGetStorySucceeded()

      val storySummary = storySummaryResultCaptor.value.getOrThrow()
      assertThat(storySummary.storyId).isEqualTo(FRACTIONS_STORY_ID_0)
      assertThat(storySummary.chapterList[0].chapterPlayState).isEqualTo(ChapterPlayState.COMPLETED)
      assertThat(storySummary.chapterList[1].chapterPlayState).isEqualTo(ChapterPlayState.NOT_STARTED)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testStoryProgressTestHelper_markPartialTopicProgressForFractions_getOngoingTopicListIsCorrect() =
    runBlockingTest(coroutineContext) {

      storyProgressTestHelper.markPartialTopicProgressForFractions(profileId, /* timestampOlderThanAWeek= */ false)
      advanceUntilIdle()

      topicController.getOngoingTopicList(profileId).observeForever(mockOngoingTopicListObserver)
      advanceUntilIdle()

      verifyGetOngoingTopicListSucceeded()

      val ongoingTopicList = ongoingTopicListResultCaptor.value.getOrThrow()
      assertThat(ongoingTopicList.topicList.size).isEqualTo(1)
      assertThat(ongoingTopicList.topicList[0].topicId).isEqualTo(FRACTIONS_TOPIC_ID)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testStoryProgressTestHelper_markPartialTopicProgressForFractions_getCompletedStoryListIsCorrect() =
    runBlockingTest(coroutineContext) {

      storyProgressTestHelper.markPartialTopicProgressForFractions(profileId, /* timestampOlderThanAWeek= */ false)
      advanceUntilIdle()

      topicController.getCompletedStoryList(profileId).observeForever(mockCompletedStoryListObserver)
      advanceUntilIdle()

      verifyGetCompletedStoryListSucceeded()

      val completedStoryList = completedStoryListResultCaptor.value.getOrThrow()
      assertThat(completedStoryList.completedStoryList.size).isEqualTo(0)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testStoryProgressTestHelper_markFullStoryProgressForFractions_getTopicIsCorrect() =
    runBlockingTest(coroutineContext) {

      storyProgressTestHelper.markFullStoryProgressForFractions(profileId, /* timestampOlderThanAWeek= */ false)
      advanceUntilIdle()

      topicController.getTopic(profileId, FRACTIONS_TOPIC_ID).observeForever(mockTopicObserver)
      advanceUntilIdle()

      verifyGetTopicSucceeded()

      val topic = topicResultCaptor.value.getOrThrow()
      assertThat(topic.topicId).isEqualTo(FRACTIONS_TOPIC_ID)
      assertThat(topic.storyList[0].chapterList[0].chapterPlayState).isEqualTo(ChapterPlayState.COMPLETED)
      assertThat(topic.storyList[0].chapterList[1].chapterPlayState).isEqualTo(ChapterPlayState.COMPLETED)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testStoryProgressTestHelper_markFullStoryProgressForFractions_getStoryIsCorrect() =
    runBlockingTest(coroutineContext) {

      storyProgressTestHelper.markFullStoryProgressForFractions(profileId, /* timestampOlderThanAWeek= */ false)
      advanceUntilIdle()

      topicController.getStory(profileId, FRACTIONS_TOPIC_ID, FRACTIONS_STORY_ID_0)
        .observeForever(mockStorySummaryObserver)
      advanceUntilIdle()

      verifyGetStorySucceeded()

      val storySummary = storySummaryResultCaptor.value.getOrThrow()
      assertThat(storySummary.storyId).isEqualTo(FRACTIONS_STORY_ID_0)
      assertThat(storySummary.chapterList[0].chapterPlayState).isEqualTo(ChapterPlayState.COMPLETED)
      assertThat(storySummary.chapterList[1].chapterPlayState).isEqualTo(ChapterPlayState.COMPLETED)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testStoryProgressTestHelper_markFullStoryProgressForFractions_getOngoingTopicListIsCorrect() =
    runBlockingTest(coroutineContext) {

      storyProgressTestHelper.markFullStoryProgressForFractions(profileId, /* timestampOlderThanAWeek= */ false)
      advanceUntilIdle()

      topicController.getOngoingTopicList(profileId).observeForever(mockOngoingTopicListObserver)
      advanceUntilIdle()

      verifyGetOngoingTopicListSucceeded()

      val ongoingTopicList = ongoingTopicListResultCaptor.value.getOrThrow()
      assertThat(ongoingTopicList.topicList.size).isEqualTo(0)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testStoryProgressTestHelper_markFullStoryProgressForFractions_getCompletedStoryListIsCorrect() =
    runBlockingTest(coroutineContext) {

      storyProgressTestHelper.markFullStoryProgressForFractions(profileId, /* timestampOlderThanAWeek= */ false)
      advanceUntilIdle()

      topicController.getCompletedStoryList(profileId).observeForever(mockCompletedStoryListObserver)
      advanceUntilIdle()

      verifyGetCompletedStoryListSucceeded()

      val completedStoryList = completedStoryListResultCaptor.value.getOrThrow()
      assertThat(completedStoryList.completedStoryList.size).isEqualTo(1)
      assertThat(completedStoryList.completedStoryList[0].storyId).isEqualTo(FRACTIONS_STORY_ID_0)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testStoryProgressTestHelper_markFullTopicProgressForFractions_getTopicIsCorrect() =
    runBlockingTest(coroutineContext) {

      storyProgressTestHelper.markFullTopicProgressForFractions(profileId, /* timestampOlderThanAWeek= */ false)
      advanceUntilIdle()

      topicController.getTopic(profileId, FRACTIONS_TOPIC_ID).observeForever(mockTopicObserver)
      advanceUntilIdle()

      verifyGetTopicSucceeded()

      val topic = topicResultCaptor.value.getOrThrow()
      assertThat(topic.topicId).isEqualTo(FRACTIONS_TOPIC_ID)
      assertThat(topic.storyList[0].chapterList[0].chapterPlayState).isEqualTo(ChapterPlayState.COMPLETED)
      assertThat(topic.storyList[0].chapterList[1].chapterPlayState).isEqualTo(ChapterPlayState.COMPLETED)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testStoryProgressTestHelper_markFullTopicProgressForFractions_getStoryIsCorrect() =
    runBlockingTest(coroutineContext) {

      storyProgressTestHelper.markFullTopicProgressForFractions(profileId, /* timestampOlderThanAWeek= */ false)
      advanceUntilIdle()

      topicController.getStory(profileId, FRACTIONS_TOPIC_ID, FRACTIONS_STORY_ID_0)
        .observeForever(mockStorySummaryObserver)
      advanceUntilIdle()

      verifyGetStorySucceeded()

      val storySummary = storySummaryResultCaptor.value.getOrThrow()
      assertThat(storySummary.storyId).isEqualTo(FRACTIONS_STORY_ID_0)
      assertThat(storySummary.chapterList[0].chapterPlayState).isEqualTo(ChapterPlayState.COMPLETED)
      assertThat(storySummary.chapterList[1].chapterPlayState).isEqualTo(ChapterPlayState.COMPLETED)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testStoryProgressTestHelper_markFullTopicProgressForFractions_getOngoingTopicListIsCorrect() =
    runBlockingTest(coroutineContext) {

      storyProgressTestHelper.markFullTopicProgressForFractions(profileId, /* timestampOlderThanAWeek= */ false)
      advanceUntilIdle()

      topicController.getOngoingTopicList(profileId).observeForever(mockOngoingTopicListObserver)
      advanceUntilIdle()

      verifyGetOngoingTopicListSucceeded()

      val ongoingTopicList = ongoingTopicListResultCaptor.value.getOrThrow()
      assertThat(ongoingTopicList.topicList.size).isEqualTo(0)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testStoryProgressTestHelper_markFullTopicProgressForFractions_getCompletedStoryListIsCorrect() =
    runBlockingTest(coroutineContext) {

      storyProgressTestHelper.markFullTopicProgressForFractions(profileId, /* timestampOlderThanAWeek= */ false)
      advanceUntilIdle()

      topicController.getCompletedStoryList(profileId).observeForever(mockCompletedStoryListObserver)
      advanceUntilIdle()

      verifyGetCompletedStoryListSucceeded()

      val completedStoryList = completedStoryListResultCaptor.value.getOrThrow()
      assertThat(completedStoryList.completedStoryList.size).isEqualTo(1)
      assertThat(completedStoryList.completedStoryList[0].storyId).isEqualTo(FRACTIONS_STORY_ID_0)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testStoryProgressTestHelper_markFullStoryPartialTopicProgressForRatios_getTopicIsCorrect() =
    runBlockingTest(coroutineContext) {

      storyProgressTestHelper.markFullStoryPartialTopicProgressForRatios(
        profileId, /* timestampOlderThanAWeek= */
        false
      )
      advanceUntilIdle()

      topicController.getTopic(profileId, RATIOS_TOPIC_ID).observeForever(mockTopicObserver)

      advanceUntilIdle()

      verifyGetTopicSucceeded()

      val topic = topicResultCaptor.value.getOrThrow()
      assertThat(topic.topicId).isEqualTo(RATIOS_TOPIC_ID)
      assertThat(topic.storyList[0].chapterList[0].chapterPlayState).isEqualTo(ChapterPlayState.COMPLETED)
      assertThat(topic.storyList[0].chapterList[1].chapterPlayState).isEqualTo(ChapterPlayState.COMPLETED)
      assertThat(topic.storyList[1].chapterList[0].chapterPlayState).isEqualTo(ChapterPlayState.NOT_STARTED)
      assertThat(topic.storyList[1].chapterList[1].chapterPlayState).isEqualTo(ChapterPlayState.NOT_PLAYABLE_MISSING_PREREQUISITES)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testStoryProgressTestHelper_markFullStoryPartialTopicProgressForRatios_getStoryIsCorrect() =
    runBlockingTest(coroutineContext) {

      storyProgressTestHelper.markFullStoryPartialTopicProgressForRatios(
        profileId, /* timestampOlderThanAWeek= */
        false
      )
      advanceUntilIdle()

      topicController.getStory(profileId, RATIOS_TOPIC_ID, RATIOS_STORY_ID_0)
        .observeForever(mockStorySummaryObserver)
      advanceUntilIdle()

      verifyGetStorySucceeded()

      val storySummary = storySummaryResultCaptor.value.getOrThrow()
      assertThat(storySummary.storyId).isEqualTo(RATIOS_STORY_ID_0)
      assertThat(storySummary.chapterList[0].chapterPlayState).isEqualTo(ChapterPlayState.COMPLETED)
      assertThat(storySummary.chapterList[1].chapterPlayState).isEqualTo(ChapterPlayState.COMPLETED)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testStoryProgressTestHelper_markFullStoryPartialTopicProgressForRatios_getOngoingTopicListIsCorrect() =
    runBlockingTest(coroutineContext) {

      storyProgressTestHelper.markFullStoryPartialTopicProgressForRatios(
        profileId, /* timestampOlderThanAWeek= */
        false
      )
      advanceUntilIdle()

      topicController.getOngoingTopicList(profileId).observeForever(mockOngoingTopicListObserver)
      advanceUntilIdle()

      verifyGetOngoingTopicListSucceeded()

      val ongoingTopicList = ongoingTopicListResultCaptor.value.getOrThrow()
      assertThat(ongoingTopicList.topicList.size).isEqualTo(1)
      assertThat(ongoingTopicList.topicList[0].topicId).isEqualTo(RATIOS_TOPIC_ID)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testStoryProgressTestHelper_markFullStoryPartialTopicProgressForRatios_getCompletedStoryListIsCorrect() =
    runBlockingTest(coroutineContext) {

      storyProgressTestHelper.markFullStoryPartialTopicProgressForRatios(
        profileId, /* timestampOlderThanAWeek= */
        false
      )
      advanceUntilIdle()

      topicController.getCompletedStoryList(profileId).observeForever(mockCompletedStoryListObserver)
      advanceUntilIdle()

      verifyGetCompletedStoryListSucceeded()

      val completedStoryList = completedStoryListResultCaptor.value.getOrThrow()
      assertThat(completedStoryList.completedStoryList.size).isEqualTo(1)
      assertThat(completedStoryList.completedStoryList[0].storyId).isEqualTo(RATIOS_STORY_ID_0)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testStoryProgressTestHelper_markTwoPartialStoryProgressForRatios_getTopicIsCorrect() =
    runBlockingTest(coroutineContext) {

      storyProgressTestHelper.markTwoPartialStoryProgressForRatios(profileId, /* timestampOlderThanAWeek= */ false)
      advanceUntilIdle()

      topicController.getTopic(profileId, RATIOS_TOPIC_ID).observeForever(mockTopicObserver)
      advanceUntilIdle()

      verifyGetTopicSucceeded()

      val topic = topicResultCaptor.value.getOrThrow()
      assertThat(topic.topicId).isEqualTo(RATIOS_TOPIC_ID)
      assertThat(topic.storyList[0].chapterList[0].chapterPlayState).isEqualTo(ChapterPlayState.COMPLETED)
      assertThat(topic.storyList[0].chapterList[1].chapterPlayState).isEqualTo(ChapterPlayState.NOT_STARTED)
      assertThat(topic.storyList[1].chapterList[0].chapterPlayState).isEqualTo(ChapterPlayState.COMPLETED)
      assertThat(topic.storyList[1].chapterList[1].chapterPlayState).isEqualTo(ChapterPlayState.NOT_STARTED)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testStoryProgressTestHelper_markTwoPartialStoryProgressForRatios_getStoryIsCorrect() =
    runBlockingTest(coroutineContext) {

      storyProgressTestHelper.markTwoPartialStoryProgressForRatios(profileId, /* timestampOlderThanAWeek= */ false)
      advanceUntilIdle()

      topicController.getStory(profileId, RATIOS_TOPIC_ID, RATIOS_STORY_ID_0)
        .observeForever(mockStorySummaryObserver)
      advanceUntilIdle()

      verifyGetStorySucceeded()

      val storySummary = storySummaryResultCaptor.value.getOrThrow()
      assertThat(storySummary.storyId).isEqualTo(RATIOS_STORY_ID_0)
      assertThat(storySummary.chapterList[0].chapterPlayState).isEqualTo(ChapterPlayState.COMPLETED)
      assertThat(storySummary.chapterList[1].chapterPlayState).isEqualTo(ChapterPlayState.NOT_STARTED)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testStoryProgressTestHelper_markTwoPartialStoryProgressForRatios_getOngoingTopicListIsCorrect() =
    runBlockingTest(coroutineContext) {

      storyProgressTestHelper.markTwoPartialStoryProgressForRatios(profileId, /* timestampOlderThanAWeek= */ false)
      advanceUntilIdle()

      topicController.getOngoingTopicList(profileId).observeForever(mockOngoingTopicListObserver)
      advanceUntilIdle()

      verifyGetOngoingTopicListSucceeded()

      val ongoingTopicList = ongoingTopicListResultCaptor.value.getOrThrow()
      assertThat(ongoingTopicList.topicList.size).isEqualTo(1)
      assertThat(ongoingTopicList.topicList[0].topicId).isEqualTo(RATIOS_TOPIC_ID)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testStoryProgressTestHelper_markTwoPartialStoryProgressForRatios_getCompletedStoryListIsCorrect() =
    runBlockingTest(coroutineContext) {

      storyProgressTestHelper.markTwoPartialStoryProgressForRatios(profileId, /* timestampOlderThanAWeek= */ false)
      advanceUntilIdle()

      topicController.getCompletedStoryList(profileId).observeForever(mockCompletedStoryListObserver)
      advanceUntilIdle()

      verifyGetCompletedStoryListSucceeded()

      val completedStoryList = completedStoryListResultCaptor.value.getOrThrow()
      assertThat(completedStoryList.completedStoryList.size).isEqualTo(0)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testStoryProgressTestHelper_markRecentlyPlayedForFractionsStory0Exploration0_getOngoingStoryListIsCorrect() =
    runBlockingTest(coroutineContext) {

      storyProgressTestHelper.markRecentlyPlayedForFractionsStory0Exploration0(
        profileId, /* timestampOlderThanAWeek= */false
      )
      advanceUntilIdle()

      topicListController.getOngoingStoryList(profileId).observeForever(mockOngoingStoryListObserver)
      advanceUntilIdle()

      verifyGetOngoingStoryListSucceeded()

      val ongoingStoryList = ongoingStoryListResultCaptor.value.getOrThrow()
      assertThat(ongoingStoryList.recentStoryCount).isEqualTo(1)
      assertThat(ongoingStoryList.olderStoryCount).isEqualTo(0)
      assertThat(ongoingStoryList.recentStoryList[0].explorationId).isEqualTo(FRACTIONS_EXPLORATION_ID_0)
      assertThat(ongoingStoryList.recentStoryList[0].completedChapterCount).isEqualTo(0)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testStoryProgressTestHelper_markRecentlyPlayedForRatiosStory0Exploration0_getOngoingStoryListIsCorrect() =
    runBlockingTest(coroutineContext) {

      storyProgressTestHelper.markRecentlyPlayedForRatiosStory0Exploration0(
        profileId, /* timestampOlderThanAWeek= */false
      )
      advanceUntilIdle()

      topicListController.getOngoingStoryList(profileId).observeForever(mockOngoingStoryListObserver)
      advanceUntilIdle()

      verifyGetOngoingStoryListSucceeded()

      val ongoingStoryList = ongoingStoryListResultCaptor.value.getOrThrow()
      assertThat(ongoingStoryList.recentStoryCount).isEqualTo(1)
      assertThat(ongoingStoryList.olderStoryCount).isEqualTo(0)
      assertThat(ongoingStoryList.recentStoryList[0].explorationId).isEqualTo(RATIOS_EXPLORATION_ID_0)
      assertThat(ongoingStoryList.recentStoryList[0].completedChapterCount).isEqualTo(0)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testStoryProgressTestHelper_markRecentlyPlayedForRatiosStory0Exploration0AndStory1Exploration2_getOngoingStoryListIsCorrect() =
    runBlockingTest(coroutineContext) {

      storyProgressTestHelper.markRecentlyPlayedForRatiosStory0Exploration0AndStory1Exploration2(
        profileId, /* timestampOlderThanAWeek= */false
      )
      advanceUntilIdle()

      topicListController.getOngoingStoryList(profileId).observeForever(mockOngoingStoryListObserver)
      advanceUntilIdle()

      verifyGetOngoingStoryListSucceeded()

      val ongoingStoryList = ongoingStoryListResultCaptor.value.getOrThrow()
      assertThat(ongoingStoryList.recentStoryCount).isEqualTo(2)
      assertThat(ongoingStoryList.olderStoryCount).isEqualTo(0)
      assertThat(ongoingStoryList.recentStoryList[0].explorationId).isEqualTo(RATIOS_EXPLORATION_ID_0)
      assertThat(ongoingStoryList.recentStoryList[0].completedChapterCount).isEqualTo(0)

      assertThat(ongoingStoryList.recentStoryList[1].explorationId).isEqualTo(RATIOS_EXPLORATION_ID_2)
      assertThat(ongoingStoryList.recentStoryList[1].completedChapterCount).isEqualTo(0)
    }

  @Test
  @ExperimentalCoroutinesApi
  fun testStoryProgressTestHelper_markRecentlyPlayedForFirstExplorationInAllStoriesInFractionsAndRatio_asOldStories_getOngoingStoryListIsCorrect() =
    runBlockingTest(coroutineContext) {

      storyProgressTestHelper.markRecentlyPlayedForFirstExplorationInAllStoriesInFractionsAndRatios(
        profileId, /* timestampOlderThanAWeek= */true
      )
      advanceUntilIdle()

      topicListController.getOngoingStoryList(profileId).observeForever(mockOngoingStoryListObserver)
      advanceUntilIdle()

      verifyGetOngoingStoryListSucceeded()

      val ongoingStoryList = ongoingStoryListResultCaptor.value.getOrThrow()
      assertThat(ongoingStoryList.recentStoryCount).isEqualTo(0)
      assertThat(ongoingStoryList.olderStoryCount).isEqualTo(3)

      assertThat(ongoingStoryList.olderStoryList[0].explorationId).isEqualTo(FRACTIONS_EXPLORATION_ID_0)
      assertThat(ongoingStoryList.olderStoryList[0].completedChapterCount).isEqualTo(0)

      assertThat(ongoingStoryList.olderStoryList[1].explorationId).isEqualTo(RATIOS_EXPLORATION_ID_0)
      assertThat(ongoingStoryList.olderStoryList[1].completedChapterCount).isEqualTo(0)

      assertThat(ongoingStoryList.olderStoryList[2].explorationId).isEqualTo(RATIOS_EXPLORATION_ID_2)
      assertThat(ongoingStoryList.olderStoryList[2].completedChapterCount).isEqualTo(0)
    }

  private fun verifyGetTopicSucceeded() {
    verify(mockTopicObserver, atLeastOnce()).onChanged(topicResultCaptor.capture())
    assertThat(topicResultCaptor.value.isSuccess()).isTrue()
  }

  private fun verifyGetStorySucceeded() {
    verify(mockStorySummaryObserver, atLeastOnce()).onChanged(storySummaryResultCaptor.capture())
    assertThat(storySummaryResultCaptor.value.isSuccess()).isTrue()
  }

  private fun verifyGetOngoingTopicListSucceeded() {
    verify(mockOngoingTopicListObserver, atLeastOnce()).onChanged(ongoingTopicListResultCaptor.capture())
    assertThat(ongoingTopicListResultCaptor.value.isSuccess()).isTrue()
  }

  private fun verifyGetCompletedStoryListSucceeded() {
    verify(mockCompletedStoryListObserver, atLeastOnce()).onChanged(completedStoryListResultCaptor.capture())
    assertThat(completedStoryListResultCaptor.value.isSuccess()).isTrue()
  }

  private fun verifyGetOngoingStoryListSucceeded() {
    verify(mockOngoingStoryListObserver, atLeastOnce()).onChanged(ongoingStoryListResultCaptor.capture())
    assertThat(ongoingStoryListResultCaptor.value.isSuccess()).isTrue()
  }

  @Module
  class TestFirebaseModule {
    companion object {
      var mockCrashlyticsWrapper = Mockito.mock(CrashlyticsWrapper::class.java)
    }
    @Provides
    @Singleton
    fun provideFirebaseCrashlytics(): FirebaseCrashlytics {
      return Mockito.mock(FirebaseCrashlytics::class.java)
    }

    @Provides
    @Singleton
    fun provideCrashlyticsWrapper(): CrashlyticsWrapper {
      return mockCrashlyticsWrapper
    }
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

    @Provides
    @DefaultGcsPrefix
    @Singleton
    fun provideDefaultGcsPrefix(): String {
      return "https://storage.googleapis.com/"
    }

    @Provides
    @DefaultGcsResource
    @Singleton
    fun provideDefaultGcsResource(): String {
      return "oppiaserver-resources/"
    }

    @Provides
    @ImageDownloadUrlTemplate
    @Singleton
    fun provideImageDownloadUrlTemplate(): String {
      return "%s/%s/assets/image/%s"
    }
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(modules = [TestModule::class, TestFirebaseModule::class])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(storyProgressTestHelperTest: StoryProgressTestHelperTest)
  }
}
