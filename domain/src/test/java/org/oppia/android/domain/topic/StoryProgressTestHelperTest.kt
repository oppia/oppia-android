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
import org.oppia.android.app.model.CompletedStoryList
import org.oppia.android.app.model.OngoingStoryList
import org.oppia.android.app.model.OngoingTopicList
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.StorySummary
import org.oppia.android.app.model.Topic
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.testing.TestCoroutineDispatchers
import org.oppia.android.testing.TestDispatcherModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.util.caching.CacheAssetsLocally
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.gcsresource.DefaultResourceBucketName
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.oppia.android.util.parser.DefaultGcsPrefix
import org.oppia.android.util.parser.ImageDownloadUrlTemplate
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [StoryProgressTestHelper]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = StoryProgressTestHelperTest.TestApplication::class)
class StoryProgressTestHelperTest {
  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var storyProgressTestHelper: StoryProgressTestHelper

  @Inject
  lateinit var topicController: TopicController

  @Inject
  lateinit var topicListController: TopicListController

  @Mock
  lateinit var mockCompletedStoryListObserver: Observer<AsyncResult<CompletedStoryList>>

  @Captor
  lateinit var completedStoryListResultCaptor: ArgumentCaptor<AsyncResult<CompletedStoryList>>

  @Mock
  lateinit var mockOngoingStoryListObserver: Observer<AsyncResult<OngoingStoryList>>

  @Captor
  lateinit var ongoingStoryListResultCaptor: ArgumentCaptor<AsyncResult<OngoingStoryList>>

  @Mock
  lateinit var mockOngoingTopicListObserver: Observer<AsyncResult<OngoingTopicList>>

  @Captor
  lateinit var ongoingTopicListResultCaptor: ArgumentCaptor<AsyncResult<OngoingTopicList>>

  @Mock
  lateinit var mockStorySummaryObserver: Observer<AsyncResult<StorySummary>>

  @Captor
  lateinit var storySummaryResultCaptor: ArgumentCaptor<AsyncResult<StorySummary>>

  @Mock
  lateinit var mockTopicObserver: Observer<AsyncResult<Topic>>

  @Captor
  lateinit var topicResultCaptor: ArgumentCaptor<AsyncResult<Topic>>

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  private lateinit var profileId: ProfileId

  @Before
  fun setUp() {
    profileId = ProfileId.newBuilder().setInternalId(0).build()
    setUpTestApplicationComponent()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  @Test
  fun testProgressTestHelper_markPartialStoryProgressForFractions_getTopicIsCorrect() {
    storyProgressTestHelper.markPartialStoryProgressForFractions(
      profileId,
      /* timestampOlderThanAWeek= */ false
    )
    testCoroutineDispatchers.runCurrent()

    topicController.getTopic(
      profileId, FRACTIONS_TOPIC_ID
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
  fun testProgressTestHelper_markPartialStoryProgressForFractions_getStoryIsCorrect() {
    storyProgressTestHelper.markPartialStoryProgressForFractions(
      profileId,
      /* timestampOlderThanAWeek= */ false
    )
    testCoroutineDispatchers.runCurrent()

    topicController.getStory(profileId, FRACTIONS_TOPIC_ID, FRACTIONS_STORY_ID_0).toLiveData()
      .observeForever(mockStorySummaryObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetStorySucceeded()

    val storySummary = storySummaryResultCaptor.value.getOrThrow()
    assertThat(storySummary.storyId).isEqualTo(FRACTIONS_STORY_ID_0)
    assertThat(storySummary.chapterList[0].chapterPlayState)
      .isEqualTo(ChapterPlayState.COMPLETED)
    assertThat(storySummary.chapterList[1].chapterPlayState)
      .isEqualTo(ChapterPlayState.NOT_STARTED)
  }

  @Test
  fun testProgressTestHelper_markPartialStoryProgressForFractions_getOngoingTopicListIsCorrect() {
    storyProgressTestHelper.markPartialStoryProgressForFractions(
      profileId,
      /* timestampOlderThanAWeek= */ false
    )
    testCoroutineDispatchers.runCurrent()

    topicController.getOngoingTopicList(
      profileId
    ).toLiveData().observeForever(mockOngoingTopicListObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetOngoingTopicListSucceeded()

    val ongoingTopicList = ongoingTopicListResultCaptor.value.getOrThrow()
    assertThat(ongoingTopicList.topicList.size).isEqualTo(1)
    assertThat(ongoingTopicList.topicList[0].topicId).isEqualTo(FRACTIONS_TOPIC_ID)
  }

  @Test
  fun testProgressTestHelper_markPartialStoryProgressForFractions_getCompletedStoryListIsCorrect() {
    storyProgressTestHelper.markPartialStoryProgressForFractions(
      profileId,
      /* timestampOlderThanAWeek= */ false
    )
    testCoroutineDispatchers.runCurrent()

    topicController.getCompletedStoryList(profileId).toLiveData()
      .observeForever(mockCompletedStoryListObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetCompletedStoryListSucceeded()

    val completedStoryList = completedStoryListResultCaptor.value.getOrThrow()
    assertThat(completedStoryList.completedStoryList.size).isEqualTo(0)
  }

  @Test
  fun testProgressTestHelper_markPartialTopicProgressForFractions_getTopicIsCorrect() {
    storyProgressTestHelper.markPartialTopicProgressForFractions(
      profileId,
      /* timestampOlderThanAWeek= */ false
    )
    testCoroutineDispatchers.runCurrent()

    topicController.getTopic(
      profileId, FRACTIONS_TOPIC_ID
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
  fun testProgressTestHelper_markPartialTopicProgressForFractions_getStoryIsCorrect() {
    storyProgressTestHelper.markPartialTopicProgressForFractions(
      profileId,
      /* timestampOlderThanAWeek= */ false
    )
    testCoroutineDispatchers.runCurrent()

    topicController.getStory(profileId, FRACTIONS_TOPIC_ID, FRACTIONS_STORY_ID_0).toLiveData()
      .observeForever(mockStorySummaryObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetStorySucceeded()

    val storySummary = storySummaryResultCaptor.value.getOrThrow()
    assertThat(storySummary.storyId).isEqualTo(FRACTIONS_STORY_ID_0)
    assertThat(storySummary.chapterList[0].chapterPlayState)
      .isEqualTo(ChapterPlayState.COMPLETED)
    assertThat(storySummary.chapterList[1].chapterPlayState)
      .isEqualTo(ChapterPlayState.NOT_STARTED)
  }

  @Test
  fun testProgressTestHelper_markPartialTopicProgressForFractions_getOngoingTopicListIsCorrect() {
    storyProgressTestHelper.markPartialTopicProgressForFractions(
      profileId,
      /* timestampOlderThanAWeek= */ false
    )
    testCoroutineDispatchers.runCurrent()

    topicController.getOngoingTopicList(
      profileId
    ).toLiveData().observeForever(mockOngoingTopicListObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetOngoingTopicListSucceeded()

    val ongoingTopicList = ongoingTopicListResultCaptor.value.getOrThrow()
    assertThat(ongoingTopicList.topicList.size).isEqualTo(1)
    assertThat(ongoingTopicList.topicList[0].topicId).isEqualTo(FRACTIONS_TOPIC_ID)
  }

  @Test
  fun testProgressTestHelper_markPartialTopicProgressForFractions_getCompletedStoryListIsCorrect() {
    storyProgressTestHelper.markPartialTopicProgressForFractions(
      profileId,
      /* timestampOlderThanAWeek= */ false
    )
    testCoroutineDispatchers.runCurrent()

    topicController.getCompletedStoryList(profileId).toLiveData()
      .observeForever(mockCompletedStoryListObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetCompletedStoryListSucceeded()

    val completedStoryList = completedStoryListResultCaptor.value.getOrThrow()
    assertThat(completedStoryList.completedStoryList.size).isEqualTo(0)
  }

  @Test
  fun testProgressTestHelper_markFullStoryProgressForFractions_getTopicIsCorrect() {
    storyProgressTestHelper.markFullStoryProgressForFractions(
      profileId,
      /* timestampOlderThanAWeek= */ false
    )
    testCoroutineDispatchers.runCurrent()

    topicController.getTopic(
      profileId, FRACTIONS_TOPIC_ID
    ).toLiveData().observeForever(mockTopicObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetTopicSucceeded()

    val topic = topicResultCaptor.value.getOrThrow()
    assertThat(topic.topicId).isEqualTo(FRACTIONS_TOPIC_ID)
    assertThat(topic.storyList[0].chapterList[0].chapterPlayState)
      .isEqualTo(ChapterPlayState.COMPLETED)
    assertThat(topic.storyList[0].chapterList[1].chapterPlayState)
      .isEqualTo(ChapterPlayState.COMPLETED)
  }

  @Test
  fun testProgressTestHelper_markFullStoryProgressForFractions_getStoryIsCorrect() {
    storyProgressTestHelper.markFullStoryProgressForFractions(
      profileId,
      /* timestampOlderThanAWeek= */ false
    )
    testCoroutineDispatchers.runCurrent()

    topicController.getStory(profileId, FRACTIONS_TOPIC_ID, FRACTIONS_STORY_ID_0).toLiveData()
      .observeForever(mockStorySummaryObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetStorySucceeded()

    val storySummary = storySummaryResultCaptor.value.getOrThrow()
    assertThat(storySummary.storyId).isEqualTo(FRACTIONS_STORY_ID_0)
    assertThat(storySummary.chapterList[0].chapterPlayState).isEqualTo(ChapterPlayState.COMPLETED)
    assertThat(storySummary.chapterList[1].chapterPlayState).isEqualTo(ChapterPlayState.COMPLETED)
  }

  @Test
  fun testProgressTestHelper_markFullStoryProgressForFractions_getOngoingTopicListIsCorrect() {
    storyProgressTestHelper.markFullStoryProgressForFractions(
      profileId,
      /* timestampOlderThanAWeek= */ false
    )
    testCoroutineDispatchers.runCurrent()

    topicController.getOngoingTopicList(
      profileId
    ).toLiveData().observeForever(mockOngoingTopicListObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetOngoingTopicListSucceeded()

    val ongoingTopicList = ongoingTopicListResultCaptor.value.getOrThrow()
    assertThat(ongoingTopicList.topicList.size).isEqualTo(0)
  }

  @Test
  fun testProgressTestHelper_markFullStoryProgressForFractions_getCompletedStoryListIsCorrect() {
    storyProgressTestHelper.markFullStoryProgressForFractions(
      profileId,
      /* timestampOlderThanAWeek= */ false
    )
    testCoroutineDispatchers.runCurrent()

    topicController.getCompletedStoryList(profileId).toLiveData()
      .observeForever(mockCompletedStoryListObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetCompletedStoryListSucceeded()

    val completedStoryList = completedStoryListResultCaptor.value.getOrThrow()
    assertThat(completedStoryList.completedStoryList.size).isEqualTo(1)
    assertThat(completedStoryList.completedStoryList[0].storyId).isEqualTo(FRACTIONS_STORY_ID_0)
  }

  @Test
  fun testProgressTestHelper_markFullTopicProgressForFractions_getTopicIsCorrect() {
    storyProgressTestHelper.markFullTopicProgressForFractions(
      profileId,
      /* timestampOlderThanAWeek= */ false
    )
    testCoroutineDispatchers.runCurrent()

    topicController.getTopic(
      profileId, FRACTIONS_TOPIC_ID
    ).toLiveData().observeForever(mockTopicObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetTopicSucceeded()

    val topic = topicResultCaptor.value.getOrThrow()
    assertThat(topic.topicId).isEqualTo(FRACTIONS_TOPIC_ID)
    assertThat(topic.storyList[0].chapterList[0].chapterPlayState)
      .isEqualTo(ChapterPlayState.COMPLETED)
    assertThat(topic.storyList[0].chapterList[1].chapterPlayState)
      .isEqualTo(ChapterPlayState.COMPLETED)
  }

  @Test
  fun testProgressTestHelper_markFullTopicProgressForFractions_getStoryIsCorrect() {
    storyProgressTestHelper.markFullTopicProgressForFractions(
      profileId,
      /* timestampOlderThanAWeek= */ false
    )
    testCoroutineDispatchers.runCurrent()

    topicController.getStory(profileId, FRACTIONS_TOPIC_ID, FRACTIONS_STORY_ID_0).toLiveData()
      .observeForever(mockStorySummaryObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetStorySucceeded()

    val storySummary = storySummaryResultCaptor.value.getOrThrow()
    assertThat(storySummary.storyId).isEqualTo(FRACTIONS_STORY_ID_0)
    assertThat(storySummary.chapterList[0].chapterPlayState).isEqualTo(ChapterPlayState.COMPLETED)
    assertThat(storySummary.chapterList[1].chapterPlayState).isEqualTo(ChapterPlayState.COMPLETED)
  }

  @Test
  fun testProgressTestHelper_markFullTopicProgressForFractions_getOngoingTopicListIsCorrect() {
    storyProgressTestHelper.markFullTopicProgressForFractions(
      profileId,
      /* timestampOlderThanAWeek= */ false
    )
    testCoroutineDispatchers.runCurrent()

    topicController.getOngoingTopicList(
      profileId
    ).toLiveData().observeForever(mockOngoingTopicListObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetOngoingTopicListSucceeded()

    val ongoingTopicList = ongoingTopicListResultCaptor.value.getOrThrow()
    assertThat(ongoingTopicList.topicList.size).isEqualTo(0)
  }

  @Test
  fun testProgressTestHelper_markFullTopicProgressForFractions_getCompletedStoryListIsCorrect() {
    storyProgressTestHelper.markFullTopicProgressForFractions(
      profileId,
      /* timestampOlderThanAWeek= */ false
    )
    testCoroutineDispatchers.runCurrent()

    topicController.getCompletedStoryList(profileId).toLiveData()
      .observeForever(mockCompletedStoryListObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetCompletedStoryListSucceeded()

    val completedStoryList = completedStoryListResultCaptor.value.getOrThrow()
    assertThat(completedStoryList.completedStoryList.size).isEqualTo(1)
    assertThat(completedStoryList.completedStoryList[0].storyId).isEqualTo(FRACTIONS_STORY_ID_0)
  }

  @Test
  fun testProgressTestHelper_markFullStoryPartialTopicProgressForRatios_getTopicIsCorrect() {
    storyProgressTestHelper.markFullStoryPartialTopicProgressForRatios(
      profileId,
      /* timestampOlderThanAWeek= */ false
    )
    testCoroutineDispatchers.runCurrent()

    topicController.getTopic(
      profileId, RATIOS_TOPIC_ID
    ).toLiveData().observeForever(mockTopicObserver)

    testCoroutineDispatchers.runCurrent()

    verifyGetTopicSucceeded()

    val topic = topicResultCaptor.value.getOrThrow()
    assertThat(topic.topicId).isEqualTo(RATIOS_TOPIC_ID)
    assertThat(topic.storyList[0].chapterList[0].chapterPlayState)
      .isEqualTo(ChapterPlayState.COMPLETED)
    assertThat(topic.storyList[0].chapterList[1].chapterPlayState)
      .isEqualTo(ChapterPlayState.COMPLETED)
    assertThat(topic.storyList[1].chapterList[0].chapterPlayState)
      .isEqualTo(ChapterPlayState.NOT_STARTED)
    assertThat(topic.storyList[1].chapterList[1].chapterPlayState)
      .isEqualTo(ChapterPlayState.NOT_PLAYABLE_MISSING_PREREQUISITES)
  }

  @Test
  fun testProgressTestHelper_markFullStoryPartialTopicProgressForRatios_getStoryIsCorrect() {
    storyProgressTestHelper.markFullStoryPartialTopicProgressForRatios(
      profileId,
      /* timestampOlderThanAWeek= */ false
    )
    testCoroutineDispatchers.runCurrent()

    topicController.getStory(profileId, RATIOS_TOPIC_ID, RATIOS_STORY_ID_0).toLiveData()
      .observeForever(mockStorySummaryObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetStorySucceeded()

    val storySummary = storySummaryResultCaptor.value.getOrThrow()
    assertThat(storySummary.storyId).isEqualTo(RATIOS_STORY_ID_0)
    assertThat(storySummary.chapterList[0].chapterPlayState).isEqualTo(ChapterPlayState.COMPLETED)
    assertThat(storySummary.chapterList[1].chapterPlayState).isEqualTo(ChapterPlayState.COMPLETED)
  }

  @Test
  fun testProgressTestHelper_markPartialTopicProgressForRatios_getOngoingTopicListIsCorrect() {
    storyProgressTestHelper.markFullStoryPartialTopicProgressForRatios(
      profileId,
      /* timestampOlderThanAWeek= */ false
    )
    testCoroutineDispatchers.runCurrent()

    topicController.getOngoingTopicList(
      profileId
    ).toLiveData().observeForever(mockOngoingTopicListObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetOngoingTopicListSucceeded()

    val ongoingTopicList = ongoingTopicListResultCaptor.value.getOrThrow()
    assertThat(ongoingTopicList.topicList.size).isEqualTo(1)
    assertThat(ongoingTopicList.topicList[0].topicId).isEqualTo(RATIOS_TOPIC_ID)
  }

  @Test
  fun testProgressTestHelper_markPartialTopicProgressForRatios_getCompletedStoryListIsCorrect() {
    storyProgressTestHelper.markFullStoryPartialTopicProgressForRatios(
      profileId,
      /* timestampOlderThanAWeek= */ false
    )
    testCoroutineDispatchers.runCurrent()

    topicController.getCompletedStoryList(profileId).toLiveData()
      .observeForever(mockCompletedStoryListObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetCompletedStoryListSucceeded()

    val completedStoryList = completedStoryListResultCaptor.value.getOrThrow()
    assertThat(completedStoryList.completedStoryList.size).isEqualTo(1)
    assertThat(completedStoryList.completedStoryList[0].storyId).isEqualTo(RATIOS_STORY_ID_0)
  }

  @Test
  fun testProgressTestHelper_markTwoPartialStoryProgressForRatios_getTopicIsCorrect() {
    storyProgressTestHelper.markTwoPartialStoryProgressForRatios(
      profileId,
      /* timestampOlderThanAWeek= */ false
    )
    testCoroutineDispatchers.runCurrent()

    topicController.getTopic(
      profileId, RATIOS_TOPIC_ID
    ).toLiveData().observeForever(mockTopicObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetTopicSucceeded()

    val topic = topicResultCaptor.value.getOrThrow()
    assertThat(topic.topicId).isEqualTo(RATIOS_TOPIC_ID)
    assertThat(topic.storyList[0].chapterList[0].chapterPlayState)
      .isEqualTo(ChapterPlayState.COMPLETED)
    assertThat(topic.storyList[0].chapterList[1].chapterPlayState)
      .isEqualTo(ChapterPlayState.NOT_STARTED)
    assertThat(topic.storyList[1].chapterList[0].chapterPlayState)
      .isEqualTo(ChapterPlayState.COMPLETED)
    assertThat(topic.storyList[1].chapterList[1].chapterPlayState)
      .isEqualTo(ChapterPlayState.NOT_STARTED)
  }

  @Test
  fun testProgressTestHelper_markTwoPartialStoryProgressForRatios_getStoryIsCorrect() {
    storyProgressTestHelper.markTwoPartialStoryProgressForRatios(
      profileId,
      /* timestampOlderThanAWeek= */ false
    )
    testCoroutineDispatchers.runCurrent()

    topicController.getStory(profileId, RATIOS_TOPIC_ID, RATIOS_STORY_ID_0).toLiveData()
      .observeForever(mockStorySummaryObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetStorySucceeded()

    val storySummary = storySummaryResultCaptor.value.getOrThrow()
    assertThat(storySummary.storyId)
      .isEqualTo(RATIOS_STORY_ID_0)
    assertThat(storySummary.chapterList[0].chapterPlayState)
      .isEqualTo(ChapterPlayState.COMPLETED)
    assertThat(storySummary.chapterList[1].chapterPlayState)
      .isEqualTo(ChapterPlayState.NOT_STARTED)
  }

  @Test
  fun testProgressTestHelper_markTwoPartialStoryProgressForRatios_getOngoingTopicListIsCorrect() {
    storyProgressTestHelper.markTwoPartialStoryProgressForRatios(
      profileId,
      /* timestampOlderThanAWeek= */ false
    )
    testCoroutineDispatchers.runCurrent()

    topicController.getOngoingTopicList(
      profileId
    ).toLiveData().observeForever(mockOngoingTopicListObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetOngoingTopicListSucceeded()

    val ongoingTopicList = ongoingTopicListResultCaptor.value.getOrThrow()
    assertThat(ongoingTopicList.topicList.size).isEqualTo(1)
    assertThat(ongoingTopicList.topicList[0].topicId).isEqualTo(RATIOS_TOPIC_ID)
  }

  @Test
  fun testProgressTestHelper_markTwoPartialStoryProgressForRatios_getCompletedStoryListIsCorrect() {
    storyProgressTestHelper.markTwoPartialStoryProgressForRatios(
      profileId,
      /* timestampOlderThanAWeek= */ false
    )
    testCoroutineDispatchers.runCurrent()

    topicController.getCompletedStoryList(profileId).toLiveData()
      .observeForever(mockCompletedStoryListObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetCompletedStoryListSucceeded()

    val completedStoryList = completedStoryListResultCaptor.value.getOrThrow()
    assertThat(completedStoryList.completedStoryList.size).isEqualTo(0)
  }

  @Test
  fun testProgressTestHelper_markRecentlyPlayed_fractionsStory0Exp0_getOngoingStoryListIsCorrect() {
    storyProgressTestHelper.markRecentlyPlayedForFractionsStory0Exploration0(
      profileId,
      /* timestampOlderThanAWeek= */ false
    )
    testCoroutineDispatchers.runCurrent()

    topicListController.getOngoingStoryList(profileId).toLiveData()
      .observeForever(mockOngoingStoryListObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetOngoingStoryListSucceeded()

    val ongoingStoryList = ongoingStoryListResultCaptor.value.getOrThrow()
    assertThat(ongoingStoryList.recentStoryCount).isEqualTo(1)
    assertThat(ongoingStoryList.olderStoryCount).isEqualTo(0)
    assertThat(ongoingStoryList.recentStoryList[0].explorationId).isEqualTo(
      FRACTIONS_EXPLORATION_ID_0
    )
    assertThat(ongoingStoryList.recentStoryList[0].completedChapterCount).isEqualTo(0)
  }

  @Test
  fun testProgressTestHelper_markRecentlyPlayed_ratiosStory0Exp0_getOngoingStoryListIsCorrect() {
    storyProgressTestHelper.markRecentlyPlayedForRatiosStory0Exploration0(
      profileId,
      /* timestampOlderThanAWeek= */ false
    )
    testCoroutineDispatchers.runCurrent()

    topicListController.getOngoingStoryList(profileId).toLiveData()
      .observeForever(mockOngoingStoryListObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetOngoingStoryListSucceeded()

    val ongoingStoryList = ongoingStoryListResultCaptor.value.getOrThrow()
    assertThat(ongoingStoryList.recentStoryCount).isEqualTo(1)
    assertThat(ongoingStoryList.olderStoryCount).isEqualTo(0)
    assertThat(ongoingStoryList.recentStoryList[0].explorationId).isEqualTo(
      RATIOS_EXPLORATION_ID_0
    )
    assertThat(ongoingStoryList.recentStoryList[0].completedChapterCount).isEqualTo(0)
  }

  @Test
  fun testProgressTestHelper_markRecentlyPlayed_ratiosStory0Exp0AndStory1Exp2_storyListIsCorrect() {
    storyProgressTestHelper.markRecentlyPlayedForRatiosStory0Exploration0AndStory1Exploration2(
      profileId,
      /* timestampOlderThanAWeek= */ false
    )
    testCoroutineDispatchers.runCurrent()

    topicListController.getOngoingStoryList(profileId).toLiveData()
      .observeForever(mockOngoingStoryListObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetOngoingStoryListSucceeded()

    val ongoingStoryList = ongoingStoryListResultCaptor.value.getOrThrow()
    assertThat(ongoingStoryList.recentStoryCount).isEqualTo(2)
    assertThat(ongoingStoryList.olderStoryCount).isEqualTo(0)
    assertThat(ongoingStoryList.recentStoryList[0].explorationId).isEqualTo(
      RATIOS_EXPLORATION_ID_0
    )
    assertThat(ongoingStoryList.recentStoryList[0].completedChapterCount).isEqualTo(0)

    assertThat(ongoingStoryList.recentStoryList[1].explorationId).isEqualTo(
      RATIOS_EXPLORATION_ID_2
    )
    assertThat(ongoingStoryList.recentStoryList[1].completedChapterCount).isEqualTo(0)
  }

  @Test
  fun testHelper_recentlyPlayed_firstExpInAllFracRatio_asOldStories_ongoingStoryListCorrect() {
    storyProgressTestHelper.markRecentlyPlayedForFirstExplorationInAllStoriesInFractionsAndRatios(
      profileId,
      /* timestampOlderThanAWeek= */ true
    )
    testCoroutineDispatchers.runCurrent()

    topicListController.getOngoingStoryList(profileId).toLiveData()
      .observeForever(mockOngoingStoryListObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetOngoingStoryListSucceeded()

    val ongoingStoryList = ongoingStoryListResultCaptor.value.getOrThrow()
    assertThat(ongoingStoryList.recentStoryCount).isEqualTo(0)
    assertThat(ongoingStoryList.olderStoryCount).isEqualTo(3)

    assertThat(ongoingStoryList.olderStoryList[0].explorationId).isEqualTo(
      FRACTIONS_EXPLORATION_ID_0
    )
    assertThat(ongoingStoryList.olderStoryList[0].completedChapterCount)
      .isEqualTo(0)

    assertThat(ongoingStoryList.olderStoryList[1].explorationId)
      .isEqualTo(RATIOS_EXPLORATION_ID_0)
    assertThat(ongoingStoryList.olderStoryList[1].completedChapterCount)
      .isEqualTo(0)

    assertThat(ongoingStoryList.olderStoryList[2].explorationId)
      .isEqualTo(RATIOS_EXPLORATION_ID_2)
    assertThat(ongoingStoryList.olderStoryList[2].completedChapterCount)
      .isEqualTo(0)
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

  private fun verifyGetOngoingStoryListSucceeded() {
    verify(
      mockOngoingStoryListObserver,
      atLeastOnce()
    ).onChanged(ongoingStoryListResultCaptor.capture())
    assertThat(ongoingStoryListResultCaptor.value.isSuccess()).isTrue()
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

    @Provides
    @DefaultGcsPrefix
    @Singleton
    fun provideDefaultGcsPrefix(): String {
      return "https://storage.googleapis.com/"
    }

    @Provides
    @DefaultResourceBucketName
    @Singleton
    fun provideDefaultGcsResource(): String {
      return "oppiaserver-resources"
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

    fun inject(storyProgressTestHelperTest: StoryProgressTestHelperTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerStoryProgressTestHelperTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(storyProgressTestHelperTest: StoryProgressTestHelperTest) {
      component.inject(storyProgressTestHelperTest)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
