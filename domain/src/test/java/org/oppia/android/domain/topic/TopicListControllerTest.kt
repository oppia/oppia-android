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
import org.oppia.android.app.model.LessonThumbnailGraphic
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.PromotedActivityList
import org.oppia.android.app.model.PromotedStory
import org.oppia.android.app.model.TopicList
import org.oppia.android.app.model.TopicSummary
import org.oppia.android.app.model.UpcomingTopic
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.testing.RobolectricModule
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
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

private const val NINE_DAYS_IN_MS = 9 * 24 * 60 * 60 * 1000

/** Tests for [TopicListController]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = TopicListControllerTest.TestApplication::class)
class TopicListControllerTest {

  @Rule
  @JvmField
  val mockitoRule: MockitoRule = MockitoJUnit.rule()

  @Inject
  lateinit var context: Context

  @Inject
  lateinit var topicListController: TopicListController

  @Inject
  lateinit var storyProgressController: StoryProgressController

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Mock
  lateinit var mockTopicListObserver: Observer<AsyncResult<TopicList>>

  @Mock
  lateinit var mockPromotedActivityListObserver: Observer<AsyncResult<PromotedActivityList>>

  @Captor
  lateinit var topicListResultCaptor: ArgumentCaptor<AsyncResult<TopicList>>

  @Captor
  lateinit var promotedActivityListResultCaptor:
    ArgumentCaptor<AsyncResult<PromotedActivityList>>

  private lateinit var profileId0: ProfileId

  @Before
  fun setUp() {
    profileId0 = ProfileId.newBuilder().setInternalId(0).build()
    setUpTestApplicationComponent()
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  // TODO(#15): Add tests for recommended lessons rather than promoted, and tests for the 'continue playing' LiveData
  //  not providing any data for cases when there are no ongoing lessons. Also, add tests for other uncovered cases
  //  (such as having and not having lessons in either of the PromotedActivityList section, or AsyncResult errors).

  @Test
  fun testRetrieveTopicList_isSuccessful() {
    val topicListLiveData = topicListController.getTopicList().toLiveData()

    topicListLiveData.observeForever(mockTopicListObserver)
    testCoroutineDispatchers.runCurrent()

    verify(mockTopicListObserver).onChanged(topicListResultCaptor.capture())
    val topicListResult = topicListResultCaptor.value
    assertThat(topicListResult!!.isSuccess()).isTrue()
  }

  @Test
  fun testRetrieveTopicList_providesListOfMultipleTopics() {
    val topicList = retrieveTopicList()
    assertThat(topicList.topicSummaryCount).isGreaterThan(1)
  }

  @Test
  fun testRetrieveTopicList_firstTopic_hasCorrectTopicInfo() {
    val topicList = retrieveTopicList()
    val firstTopic = topicList.getTopicSummary(0)
    assertThat(firstTopic.topicId).isEqualTo(TEST_TOPIC_ID_0)
    assertThat(firstTopic.name).isEqualTo("First Test Topic")
  }

  @Test
  fun testRetrieveTopicList_firstTopic_hasCorrectThumbnail() {
    val topicList = retrieveTopicList()
    val firstTopic = topicList.getTopicSummary(0)
    assertThat(firstTopic.topicThumbnail.thumbnailGraphic)
      .isEqualTo(LessonThumbnailGraphic.ADDING_AND_SUBTRACTING_FRACTIONS)
  }

  @Test
  fun testRetrieveTopicList_firstTopic_hasCorrectLessonCount() {
    val topicList = retrieveTopicList()
    val firstTopic = topicList.getTopicSummary(0)
    assertThat(firstTopic.totalChapterCount).isEqualTo(5)
  }

  @Test
  fun testRetrieveTopicList_secondTopic_hasCorrectTopicInfo() {
    val topicList = retrieveTopicList()
    val secondTopic = topicList.getTopicSummary(1)
    assertThat(secondTopic.topicId).isEqualTo(TEST_TOPIC_ID_1)
    assertThat(secondTopic.name).isEqualTo("Second Test Topic")
  }

  @Test
  fun testRetrieveTopicList_secondTopic_hasCorrectThumbnail() {
    val topicList = retrieveTopicList()
    val secondTopic = topicList.getTopicSummary(1)
    assertThat(secondTopic.topicThumbnail.thumbnailGraphic)
      .isEqualTo(LessonThumbnailGraphic.BAKER)
  }

  @Test
  fun testRetrieveTopicList_secondTopic_hasCorrectLessonCount() {
    val topicList = retrieveTopicList()
    val secondTopic = topicList.getTopicSummary(1)
    assertThat(secondTopic.totalChapterCount).isEqualTo(1)
  }

  @Test
  fun testRetrieveTopicList_fractionsTopic_hasCorrectTopicInfo() {
    val topicList = retrieveTopicList()
    val fractionsTopic = topicList.getTopicSummary(2)
    assertThat(fractionsTopic.topicId).isEqualTo(FRACTIONS_TOPIC_ID)
    assertThat(fractionsTopic.name).isEqualTo("Fractions")
  }

  @Test
  fun testRetrieveTopicList_fractionsTopic_hasCorrectThumbnail() {
    val topicList = retrieveTopicList()
    val fractionsTopic = topicList.getTopicSummary(2)
    assertThat(fractionsTopic.topicThumbnail.thumbnailGraphic)
      .isEqualTo(LessonThumbnailGraphic.CHILD_WITH_FRACTIONS_HOMEWORK)
  }

  @Test
  fun testRetrieveTopicList_fractionsTopic_hasCorrectLessonCount() {
    val topicList = retrieveTopicList()
    val fractionsTopic = topicList.getTopicSummary(2)
    assertThat(fractionsTopic.totalChapterCount).isEqualTo(2)
  }

  @Test
  fun testRetrieveTopicList_ratiosTopic_hasCorrectTopicInfo() {
    val topicList = retrieveTopicList()
    val ratiosTopic = topicList.getTopicSummary(3)
    assertThat(ratiosTopic.topicId).isEqualTo(RATIOS_TOPIC_ID)
    assertThat(ratiosTopic.name).isEqualTo("Ratios and Proportional Reasoning")
  }

  @Test
  fun testRetrieveTopicList_ratiosTopic_hasCorrectThumbnail() {
    val topicList = retrieveTopicList()
    val ratiosTopic = topicList.getTopicSummary(3)
    assertThat(ratiosTopic.topicThumbnail.thumbnailGraphic)
      .isEqualTo(LessonThumbnailGraphic.DUCK_AND_CHICKEN)
  }

  @Test
  fun testRetrieveTopicList_ratiosTopic_hasCorrectLessonCount() {
    val topicList = retrieveTopicList()
    val ratiosTopic = topicList.getTopicSummary(3)
    assertThat(ratiosTopic.totalChapterCount).isEqualTo(4)
  }

  @Test
  fun testRetrieveTopicList_doesNotContainUnavailableTopic() {
    val topicListLiveData = topicListController.getTopicList().toLiveData()

    topicListLiveData.observeForever(mockTopicListObserver)
    testCoroutineDispatchers.runCurrent()

    // Verify that the topic list does not contain a not-yet published topic (since it can't be
    // played by the user).
    verify(mockTopicListObserver).onChanged(topicListResultCaptor.capture())
    val topicList = topicListResultCaptor.value.getOrThrow()
    val topicIds = topicList.topicSummaryList.map(TopicSummary::getTopicId)
    assertThat(topicIds).doesNotContain(TEST_TOPIC_ID_2)
  }

  @Test
  fun testRetrievePromotedActivityList_defaultLesson_hasCorrectInfo() {
    topicListController.getPromotedActivityList(profileId0).toLiveData()
      .observeForever(mockPromotedActivityListObserver)
    testCoroutineDispatchers.runCurrent()

    verifyGetPromotedActivityListSucceeded()
  }

  @Test
  fun testGetPromotedActivityList_markRecentlyPlayedFracStory0Exp0_ongoingStoryListIsCorrect() {
    storyProgressController.recordRecentlyPlayedChapter(
      profileId0,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_0,
      getCurrentTimestamp()
    )
    testCoroutineDispatchers.runCurrent()

    val promotedActivityList = retrievePromotedActivityList()
    assertThat(promotedActivityList.promotedStoryList.recentlyPlayedStoryCount)
      .isEqualTo(1)
    verifyOngoingStoryAsFractionStory0Exploration0(
      promotedActivityList.promotedStoryList.recentlyPlayedStoryList[0]
    )
  }

  @Test
  fun testGetPromotedStoryList_markChapDoneFracStory0Exp0_ongoingStoryListIsCorrect() {
    storyProgressController.recordCompletedChapter(
      profileId0,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_0,
      getCurrentTimestamp()
    )
    testCoroutineDispatchers.runCurrent()

    val promotedActivityList = retrievePromotedActivityList()
    assertThat(promotedActivityList.promotedStoryList.recentlyPlayedStoryCount)
      .isEqualTo(1)
    verifyOngoingStoryAsFractionStory0Exploration1(
      promotedActivityList.promotedStoryList.recentlyPlayedStoryList[0]
    )
  }

  @Test
  fun testGetStoryList_markChapDoneFracStory0Exp0_playedFracStory0Exp1_ongoingStoryListCorrect() {
    storyProgressController.recordCompletedChapter(
      profileId0,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_0,
      getCurrentTimestamp()
    )
    testCoroutineDispatchers.runCurrent()

    storyProgressController.recordRecentlyPlayedChapter(
      profileId0,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_1,
      getCurrentTimestamp()
    )
    testCoroutineDispatchers.runCurrent()

    val promotedActivityList = retrievePromotedActivityList()
    assertThat(promotedActivityList.promotedStoryList.recentlyPlayedStoryCount)
      .isEqualTo(1)
    verifyOngoingStoryAsFractionStory0Exploration1(
      promotedActivityList.promotedStoryList.recentlyPlayedStoryList[0]
    )
  }

  @Test
  fun testGetPromotedStoryList_markAllChapsDoneInFractions_suggestedStoryListIsCorrect() {
    storyProgressController.recordCompletedChapter(
      profileId0,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_0,
      getCurrentTimestamp()
    )
    testCoroutineDispatchers.runCurrent()

    storyProgressController.recordCompletedChapter(
      profileId0,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_1,
      getCurrentTimestamp()
    )
    testCoroutineDispatchers.runCurrent()

    val promotedActivityList = retrievePromotedActivityList()
    assertThat(promotedActivityList.promotedStoryList.recentlyPlayedStoryCount)
      .isEqualTo(0)
    assertThat(promotedActivityList.promotedStoryList.suggestedStoryCount)
      .isEqualTo(2)
    verifyPromotedStoryAsFirstTestTopicStory0Exploration0(
      promotedActivityList.promotedStoryList.suggestedStoryList[0]
    )
    verifyPromotedStoryAsRatioStory0Exploration0(
      promotedActivityList.promotedStoryList.suggestedStoryList[1]
    )
  }

  @Test
  fun testGetStoryList_markRecentPlayedFirstChapInAllStoriesInRatios_ongoingStoryListIsCorrect() {
    storyProgressController.recordRecentlyPlayedChapter(
      profileId0,
      RATIOS_TOPIC_ID,
      RATIOS_STORY_ID_0,
      RATIOS_EXPLORATION_ID_0,
      getCurrentTimestamp()
    )
    testCoroutineDispatchers.runCurrent()

    storyProgressController.recordRecentlyPlayedChapter(
      profileId0,
      RATIOS_TOPIC_ID,
      RATIOS_STORY_ID_1,
      RATIOS_EXPLORATION_ID_2,
      getCurrentTimestamp()
    )
    testCoroutineDispatchers.runCurrent()

    val promotedActivityList = retrievePromotedActivityList()
    assertThat(promotedActivityList.promotedStoryList.recentlyPlayedStoryCount)
      .isEqualTo(2)
    verifyOngoingStoryAsRatioStory0Exploration0(
      promotedActivityList.promotedStoryList.recentlyPlayedStoryList[0]
    )
    verifyOngoingStoryAsRatioStory1Exploration2(
      promotedActivityList.promotedStoryList.recentlyPlayedStoryList[1]
    )
  }

  @Test
  fun testGetStoryList_markExp0DoneAndExp2InRatios_promotedStoryListIsCorrect() {
    storyProgressController.recordCompletedChapter(
      profileId0,
      RATIOS_TOPIC_ID,
      RATIOS_STORY_ID_0,
      RATIOS_EXPLORATION_ID_0,
      getCurrentTimestamp()
    )
    testCoroutineDispatchers.runCurrent()

    storyProgressController.recordCompletedChapter(
      profileId0,
      RATIOS_TOPIC_ID,
      RATIOS_STORY_ID_0,
      RATIOS_EXPLORATION_ID_1,
      getCurrentTimestamp()
    )
    testCoroutineDispatchers.runCurrent()

    val promotedActivityList = retrievePromotedActivityList()
    assertThat(promotedActivityList.promotedStoryList.suggestedStoryCount)
      .isEqualTo(1)
    verifyPromotedStoryAsFractionStory0Exploration0(
      promotedActivityList.promotedStoryList.suggestedStoryList[0]
    )
  }

  @Test
  fun testGetStoryList_markStoryDoneOfRatiosAndFirstTestTopic_suggestedStoryListIsCorrect() {
    storyProgressController.recordCompletedChapter(
      profileId0,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      getCurrentTimestamp()
    )
    testCoroutineDispatchers.runCurrent()

    storyProgressController.recordCompletedChapter(
      profileId0,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_5,
      getCurrentTimestamp()
    )
    testCoroutineDispatchers.runCurrent()

    storyProgressController.recordCompletedChapter(
      profileId0,
      RATIOS_TOPIC_ID,
      RATIOS_STORY_ID_0,
      RATIOS_EXPLORATION_ID_0,
      getCurrentTimestamp()
    )
    testCoroutineDispatchers.runCurrent()

    storyProgressController.recordCompletedChapter(
      profileId0,
      RATIOS_TOPIC_ID,
      RATIOS_STORY_ID_0,
      RATIOS_EXPLORATION_ID_1,
      getCurrentTimestamp()
    )
    testCoroutineDispatchers.runCurrent()

    val promotedActivityList = retrievePromotedActivityList()

    assertThat(promotedActivityList.promotedStoryList.suggestedStoryCount)
      .isEqualTo(1)
    verifyPromotedStoryAsSecondTestTopicStory0Exploration0(
      promotedActivityList.promotedStoryList.suggestedStoryList[0]
    )
  }

  @Test
  fun testGetStoryList_markRecentlyPlayedFirstTestTopic_defaultSuggestedStoryListIsCorrect() {
    storyProgressController.recordRecentlyPlayedChapter(
      profileId0,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      getCurrentTimestamp()
    )
    testCoroutineDispatchers.runCurrent()

    val promotedActivityList = retrievePromotedActivityList()

    assertThat(promotedActivityList.promotedStoryList.recentlyPlayedStoryCount)
      .isEqualTo(1)
    assertThat(promotedActivityList.promotedStoryList.suggestedStoryCount)
      .isEqualTo(2)
    verifyOngoingStoryAsFirstTopicStory0Exploration0(
      promotedActivityList.promotedStoryList.recentlyPlayedStoryList[0]
    )
    verifyPromotedStoryAsFractionStory0Exploration0(
      promotedActivityList.promotedStoryList.suggestedStoryList[0]
    )
    verifyPromotedStoryAsRatioStory0Exploration0(
      promotedActivityList.promotedStoryList.suggestedStoryList[1]
    )
  }

  @Test
  fun testRetrievePromotedActivityList_markAllChapDoneInAllTopics_comingSoonTopicListIsCorrect() {
    storyProgressController.recordCompletedChapter(
      profileId0,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_0,
      getCurrentTimestamp()
    )
    testCoroutineDispatchers.runCurrent()
    storyProgressController.recordCompletedChapter(
      profileId0,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_1,
      getCurrentTimestamp()
    )
    testCoroutineDispatchers.runCurrent()
    storyProgressController.recordCompletedChapter(
      profileId0,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      getCurrentTimestamp()
    )
    testCoroutineDispatchers.runCurrent()
    storyProgressController.recordCompletedChapter(
      profileId0,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_5,
      getCurrentTimestamp()
    )
    testCoroutineDispatchers.runCurrent()
    storyProgressController.recordCompletedChapter(
      profileId0,
      TEST_TOPIC_ID_1,
      TEST_STORY_ID_2,
      TEST_EXPLORATION_ID_4,
      getCurrentTimestamp()
    )
    testCoroutineDispatchers.runCurrent()
    storyProgressController.recordCompletedChapter(
      profileId0,
      RATIOS_TOPIC_ID,
      RATIOS_STORY_ID_0,
      RATIOS_EXPLORATION_ID_0,
      getCurrentTimestamp()
    )
    testCoroutineDispatchers.runCurrent()

    storyProgressController.recordCompletedChapter(
      profileId0,
      RATIOS_TOPIC_ID,
      RATIOS_STORY_ID_0,
      RATIOS_EXPLORATION_ID_1,
      getCurrentTimestamp()
    )
    testCoroutineDispatchers.runCurrent()

    val promotedActivityList = retrievePromotedActivityList()

    assertThat(promotedActivityList.comingSoonTopicList.upcomingTopicCount)
      .isEqualTo(1)
  }

  @Test
  fun testGetStoryList_markAllChapDoneInSecondTestTopic_doesNotPromoteAnyStories() {
    storyProgressController.recordCompletedChapter(
      profileId0,
      TEST_TOPIC_ID_1,
      TEST_STORY_ID_2,
      TEST_EXPLORATION_ID_4,
      getCurrentTimestamp()
    )
    testCoroutineDispatchers.runCurrent()

    val promotedActivityList = retrievePromotedActivityList()
    assertThat(promotedActivityList.promotedStoryList.recentlyPlayedStoryCount)
      .isEqualTo(0)
    assertThat(promotedActivityList.promotedStoryList.olderPlayedStoryCount)
      .isEqualTo(0)
    assertThat(promotedActivityList.promotedStoryList.suggestedStoryCount)
      .isEqualTo(0)
  }

  @Test
  fun testGetStoryList_markAllChapDoneInSecondTestTopic_comingSoonTopicListIsCorrect() {
    storyProgressController.recordCompletedChapter(
      profileId0,
      TEST_TOPIC_ID_1,
      TEST_STORY_ID_2,
      TEST_EXPLORATION_ID_4,
      getCurrentTimestamp()
    )
    testCoroutineDispatchers.runCurrent()

    val promotedActivityList = retrievePromotedActivityList()
    assertThat(promotedActivityList.promotedStoryList.recentlyPlayedStoryCount)
      .isEqualTo(0)
    assertThat(promotedActivityList.promotedStoryList.olderPlayedStoryCount)
      .isEqualTo(0)
    assertThat(promotedActivityList.promotedStoryList.suggestedStoryCount)
      .isEqualTo(0)
    assertThat(promotedActivityList.comingSoonTopicList.upcomingTopicCount)
      .isEqualTo(1)
    verifyUpcomingTopic1(promotedActivityList.comingSoonTopicList.upcomingTopicList[0])
  }

  @Test
  fun testGetStoryList_markFirstExpOfEveryStoryDoneWithinLastSevenDays_ongoingListIsCorrect() {
    storyProgressController.recordCompletedChapter(
      profileId0,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_0,
      getCurrentTimestamp()
    )
    testCoroutineDispatchers.runCurrent()

    storyProgressController.recordCompletedChapter(
      profileId0,
      RATIOS_TOPIC_ID,
      RATIOS_STORY_ID_0,
      RATIOS_EXPLORATION_ID_0,
      getCurrentTimestamp()
    )
    testCoroutineDispatchers.runCurrent()

    storyProgressController.recordCompletedChapter(
      profileId0,
      RATIOS_TOPIC_ID,
      RATIOS_STORY_ID_1,
      RATIOS_EXPLORATION_ID_2,
      getCurrentTimestamp()
    )
    testCoroutineDispatchers.runCurrent()

    val promotedActivityList = retrievePromotedActivityList()
    assertThat(promotedActivityList.promotedStoryList.recentlyPlayedStoryCount)
      .isEqualTo(3)
    verifyOngoingStoryAsRatioStory0Exploration1(
      promotedActivityList.promotedStoryList.recentlyPlayedStoryList[0]
    )
    verifyOngoingStoryAsRatioStory1Exploration3(
      promotedActivityList.promotedStoryList.recentlyPlayedStoryList[1]
    )
    verifyOngoingStoryAsFractionStory0Exploration1(
      promotedActivityList.promotedStoryList.recentlyPlayedStoryList[2]
    )
  }

  @Test
  fun testGetStoryList_markFirstExpOfEveryStoryDoneWithinLastMonth_ongoingOlderListIsCorrect() {
    storyProgressController.recordCompletedChapter(
      profileId0,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_0,
      getOldTimestamp()
    )
    testCoroutineDispatchers.runCurrent()

    storyProgressController.recordCompletedChapter(
      profileId0,
      RATIOS_TOPIC_ID,
      RATIOS_STORY_ID_0,
      RATIOS_EXPLORATION_ID_0,
      getOldTimestamp()
    )
    testCoroutineDispatchers.runCurrent()

    storyProgressController.recordCompletedChapter(
      profileId0,
      RATIOS_TOPIC_ID,
      RATIOS_STORY_ID_1,
      RATIOS_EXPLORATION_ID_2,
      getOldTimestamp()
    )
    testCoroutineDispatchers.runCurrent()

    val promotedActivityList = retrievePromotedActivityList()
    assertThat(promotedActivityList.promotedStoryList.olderPlayedStoryCount)
      .isEqualTo(3)
    verifyOngoingStoryAsRatioStory0Exploration1(
      promotedActivityList.promotedStoryList.olderPlayedStoryList[0]
    )
    verifyOngoingStoryAsRatioStory1Exploration3(
      promotedActivityList.promotedStoryList.olderPlayedStoryList[1]
    )
    verifyOngoingStoryAsFractionStory0Exploration1(
      promotedActivityList.promotedStoryList.olderPlayedStoryList[2]
    )
  }

  @Test
  fun testGetStoryList_markRecentlyPlayedForFirstTestTopic_ongoingStoryListIsCorrect() {
    storyProgressController.recordRecentlyPlayedChapter(
      profileId0,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      getCurrentTimestamp()
    )
    testCoroutineDispatchers.runCurrent()

    val promotedActivityList = retrievePromotedActivityList()
    assertThat(promotedActivityList.promotedStoryList.recentlyPlayedStoryCount)
      .isEqualTo(1)
    verifyOngoingStoryAsFirstTopicStory0Exploration0(
      promotedActivityList.promotedStoryList.recentlyPlayedStoryList[0]
    )
  }

  @Test
  fun testGetStoryList_markOneStoryDoneForFirstTestTopic_suggestedStoryListIsCorrect() {
    storyProgressController.recordCompletedChapter(
      profileId0,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      getCurrentTimestamp()
    )
    testCoroutineDispatchers.runCurrent()

    storyProgressController.recordCompletedChapter(
      profileId0,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_5,
      getCurrentTimestamp()
    )
    testCoroutineDispatchers.runCurrent()

    val promotedActivityList = retrievePromotedActivityList()

    assertThat(promotedActivityList.promotedStoryList.suggestedStoryCount)
      .isEqualTo(1)
    verifyPromotedStoryAsRatioStory0Exploration0(
      promotedActivityList.promotedStoryList.suggestedStoryList[0]
    )
  }

  @Test
  fun testGetStoryList_markOneStoryDoneAndPlayNextStoryOfFirstTestTopic_ongoingListIsCorrect() {
    storyProgressController.recordCompletedChapter(
      profileId0,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      getCurrentTimestamp()
    )
    testCoroutineDispatchers.runCurrent()

    storyProgressController.recordCompletedChapter(
      profileId0,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_5,
      getCurrentTimestamp()
    )
    testCoroutineDispatchers.runCurrent()

    storyProgressController.recordRecentlyPlayedChapter(
      profileId0,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_1,
      TEST_EXPLORATION_ID_1,
      getCurrentTimestamp()
    )
    testCoroutineDispatchers.runCurrent()

    val promotedActivityList = retrievePromotedActivityList()
    assertThat(promotedActivityList.promotedStoryList.recentlyPlayedStoryCount)
      .isEqualTo(1)
    assertThat(promotedActivityList.promotedStoryList.recentlyPlayedStoryList[0].isTopicLearned)
      .isTrue()
    verifyOngoingStoryAsFirstTopicStory1Exploration0(
      promotedActivityList.promotedStoryList.recentlyPlayedStoryList[0]
    )
  }

  @Test
  fun testGetStoryList_story0DonePlayStory1FirstTestTopic_playRatios_firstTestTopicisLearned() {
    storyProgressController.recordCompletedChapter(
      profileId0,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      getCurrentTimestamp()
    )
    testCoroutineDispatchers.runCurrent()

    storyProgressController.recordCompletedChapter(
      profileId0,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_5,
      getCurrentTimestamp()
    )
    testCoroutineDispatchers.runCurrent()

    storyProgressController.recordRecentlyPlayedChapter(
      profileId0,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_1,
      TEST_EXPLORATION_ID_1,
      getCurrentTimestamp()
    )
    testCoroutineDispatchers.runCurrent()

    storyProgressController.recordRecentlyPlayedChapter(
      profileId0,
      RATIOS_TOPIC_ID,
      RATIOS_STORY_ID_0,
      RATIOS_EXPLORATION_ID_0,
      getCurrentTimestamp()
    )
    testCoroutineDispatchers.runCurrent()

    val promotedActivityList = retrievePromotedActivityList()
    assertThat(promotedActivityList.promotedStoryList.recentlyPlayedStoryCount)
      .isEqualTo(2)
    assertThat(promotedActivityList.promotedStoryList.recentlyPlayedStoryList[0].isTopicLearned)
      .isFalse()
    verifyOngoingStoryAsRatioStory0Exploration0(
      promotedActivityList.promotedStoryList.recentlyPlayedStoryList[0]
    )
    assertThat(promotedActivityList.promotedStoryList.recentlyPlayedStoryList[1].isTopicLearned)
      .isTrue()
    verifyOngoingStoryAsFirstTopicStory1Exploration0(
      promotedActivityList.promotedStoryList.recentlyPlayedStoryList[1]
    )
  }

  @Test
  fun testRetrieveStoryList_markFirstExpOfEveryStoryDoneWithinLastMonth_ongoingListIsCorrect() {
    storyProgressController.recordCompletedChapter(
      profileId0,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_0,
      getOldTimestamp()
    )
    testCoroutineDispatchers.runCurrent()

    storyProgressController.recordCompletedChapter(
      profileId0,
      RATIOS_TOPIC_ID,
      RATIOS_STORY_ID_0,
      RATIOS_EXPLORATION_ID_0,
      getOldTimestamp()
    )
    testCoroutineDispatchers.runCurrent()

    storyProgressController.recordCompletedChapter(
      profileId0,
      RATIOS_TOPIC_ID,
      RATIOS_STORY_ID_1,
      RATIOS_EXPLORATION_ID_2,
      getCurrentTimestamp()
    )
    testCoroutineDispatchers.runCurrent()

    val promotedActivityList = retrievePromotedActivityList()
    assertThat(promotedActivityList.promotedStoryList.recentlyPlayedStoryCount)
      .isEqualTo(1)
    assertThat(promotedActivityList.promotedStoryList.olderPlayedStoryCount)
      .isEqualTo(2)
    verifyOngoingStoryAsRatioStory0Exploration1(
      promotedActivityList.promotedStoryList.olderPlayedStoryList[0]
    )
    verifyOngoingStoryAsFractionStory0Exploration1(
      promotedActivityList.promotedStoryList.olderPlayedStoryList[1]
    )
    verifyOngoingStoryAsRatioStory1Exploration3(
      promotedActivityList.promotedStoryList.recentlyPlayedStoryList[0]
    )
  }

  private fun verifyGetPromotedActivityListSucceeded() {
    verify(
      mockPromotedActivityListObserver,
      atLeastOnce()
    ).onChanged(promotedActivityListResultCaptor.capture())
    assertThat(promotedActivityListResultCaptor.value.isSuccess()).isTrue()
  }

  private fun verifyPromotedStoryAsFirstTestTopicStory0Exploration0(promotedStory: PromotedStory) {
    assertThat(promotedStory.explorationId).isEqualTo(TEST_EXPLORATION_ID_2)
    assertThat(promotedStory.storyId).isEqualTo(TEST_STORY_ID_0)
    assertThat(promotedStory.topicId).isEqualTo(TEST_TOPIC_ID_0)
    assertThat(promotedStory.topicName).isEqualTo("First Test Topic")
    assertThat(promotedStory.nextChapterName).isEqualTo("Prototype Exploration")
    assertThat(promotedStory.lessonThumbnail.thumbnailGraphic)
      .isEqualTo(LessonThumbnailGraphic.BAKER)
    assertThat(promotedStory.completedChapterCount).isEqualTo(0)
    assertThat(promotedStory.isTopicLearned).isFalse()
    assertThat(promotedStory.totalChapterCount).isEqualTo(2)
  }

  private fun verifyOngoingStoryAsFirstTopicStory1Exploration0(promotedStory: PromotedStory) {
    assertThat(promotedStory.explorationId).isEqualTo(TEST_EXPLORATION_ID_1)
    assertThat(promotedStory.storyId).isEqualTo(TEST_STORY_ID_1)
    assertThat(promotedStory.topicId).isEqualTo(TEST_TOPIC_ID_0)
    assertThat(promotedStory.topicName).isEqualTo("First Test Topic")
    assertThat(promotedStory.nextChapterName).isEqualTo("Second Exploration")
    assertThat(promotedStory.lessonThumbnail.thumbnailGraphic)
      .isEqualTo(LessonThumbnailGraphic.COMPARING_FRACTIONS)
    assertThat(promotedStory.completedChapterCount).isEqualTo(0)
    assertThat(promotedStory.isTopicLearned).isTrue()
    assertThat(promotedStory.totalChapterCount).isEqualTo(3)
  }

  private fun verifyOngoingStoryAsFirstTopicStory0Exploration0(promotedStory: PromotedStory) {
    assertThat(promotedStory.explorationId).isEqualTo(TEST_EXPLORATION_ID_2)
    assertThat(promotedStory.storyId).isEqualTo(TEST_STORY_ID_0)
    assertThat(promotedStory.topicId).isEqualTo(TEST_TOPIC_ID_0)
    assertThat(promotedStory.topicName).isEqualTo("First Test Topic")
    assertThat(promotedStory.nextChapterName).isEqualTo("Prototype Exploration")
    assertThat(promotedStory.lessonThumbnail.thumbnailGraphic)
      .isEqualTo(LessonThumbnailGraphic.BAKER)
    assertThat(promotedStory.completedChapterCount).isEqualTo(0)
    assertThat(promotedStory.isTopicLearned).isFalse()
    assertThat(promotedStory.totalChapterCount).isEqualTo(2)
  }

  private fun verifyPromotedStoryAsSecondTestTopicStory0Exploration0(promotedStory: PromotedStory) {
    assertThat(promotedStory.explorationId).isEqualTo(TEST_EXPLORATION_ID_4)
    assertThat(promotedStory.storyId).isEqualTo(TEST_STORY_ID_2)
    assertThat(promotedStory.topicId).isEqualTo(TEST_TOPIC_ID_1)
    assertThat(promotedStory.topicName).isEqualTo("Second Test Topic")
    assertThat(promotedStory.nextChapterName).isEqualTo("Fifth Exploration")
    assertThat(promotedStory.lessonThumbnail.thumbnailGraphic)
      .isEqualTo(LessonThumbnailGraphic.DERIVE_A_RATIO)
    assertThat(promotedStory.completedChapterCount).isEqualTo(0)
    assertThat(promotedStory.isTopicLearned).isFalse()
    assertThat(promotedStory.totalChapterCount).isEqualTo(1)
  }

  private fun verifyOngoingStoryAsFractionStory0Exploration0(promotedStory: PromotedStory) {
    assertThat(promotedStory.explorationId).isEqualTo(FRACTIONS_EXPLORATION_ID_0)
    assertThat(promotedStory.storyId).isEqualTo(FRACTIONS_STORY_ID_0)
    assertThat(promotedStory.topicId).isEqualTo(FRACTIONS_TOPIC_ID)
    assertThat(promotedStory.topicName).isEqualTo("Fractions")
    assertThat(promotedStory.nextChapterName).isEqualTo("What is a Fraction?")
    assertThat(promotedStory.lessonThumbnail.thumbnailGraphic)
      .isEqualTo(LessonThumbnailGraphic.DUCK_AND_CHICKEN)
    assertThat(promotedStory.completedChapterCount).isEqualTo(0)
    assertThat(promotedStory.isTopicLearned).isFalse()
    assertThat(promotedStory.totalChapterCount).isEqualTo(2)
  }

  private fun verifyPromotedStoryAsFractionStory0Exploration0(promotedStory: PromotedStory) {
    assertThat(promotedStory.explorationId).isEqualTo(FRACTIONS_EXPLORATION_ID_0)
    assertThat(promotedStory.storyId).isEqualTo(FRACTIONS_STORY_ID_0)
    assertThat(promotedStory.topicId).isEqualTo(FRACTIONS_TOPIC_ID)
    assertThat(promotedStory.topicName).isEqualTo("Fractions")
    assertThat(promotedStory.nextChapterName).isEqualTo("What is a Fraction?")
    assertThat(promotedStory.lessonThumbnail.thumbnailGraphic)
      .isEqualTo(LessonThumbnailGraphic.DUCK_AND_CHICKEN)
    assertThat(promotedStory.completedChapterCount).isEqualTo(0)
    assertThat(promotedStory.isTopicLearned).isFalse()
    assertThat(promotedStory.totalChapterCount).isEqualTo(2)
  }

  private fun verifyOngoingStoryAsFractionStory0Exploration1(promotedStory: PromotedStory) {
    assertThat(promotedStory.explorationId).isEqualTo(FRACTIONS_EXPLORATION_ID_1)
    assertThat(promotedStory.storyId).isEqualTo(FRACTIONS_STORY_ID_0)
    assertThat(promotedStory.topicId).isEqualTo(FRACTIONS_TOPIC_ID)
    assertThat(promotedStory.topicName).isEqualTo("Fractions")
    assertThat(promotedStory.nextChapterName).isEqualTo("The Meaning of Equal Parts")
    assertThat(promotedStory.lessonThumbnail.thumbnailGraphic)
      .isEqualTo(LessonThumbnailGraphic.DUCK_AND_CHICKEN)
    assertThat(promotedStory.completedChapterCount).isEqualTo(1)
    assertThat(promotedStory.totalChapterCount).isEqualTo(2)
  }

  private fun verifyOngoingStoryAsRatioStory0Exploration0(promotedStory: PromotedStory) {
    assertThat(promotedStory.explorationId).isEqualTo(RATIOS_EXPLORATION_ID_0)
    assertThat(promotedStory.storyId).isEqualTo(RATIOS_STORY_ID_0)
    assertThat(promotedStory.topicId).isEqualTo(RATIOS_TOPIC_ID)
    assertThat(promotedStory.nextChapterName).isEqualTo("What is a Ratio?")
    assertThat(promotedStory.topicName).isEqualTo("Ratios and Proportional Reasoning")
    assertThat(promotedStory.lessonThumbnail.thumbnailGraphic)
      .isEqualTo(LessonThumbnailGraphic.CHILD_WITH_FRACTIONS_HOMEWORK)
    assertThat(promotedStory.completedChapterCount).isEqualTo(0)
    assertThat(promotedStory.isTopicLearned).isFalse()
    assertThat(promotedStory.totalChapterCount).isEqualTo(2)
  }

  private fun verifyPromotedStoryAsRatioStory0Exploration0(promotedStory: PromotedStory) {
    assertThat(promotedStory.explorationId).isEqualTo(RATIOS_EXPLORATION_ID_0)
    assertThat(promotedStory.storyId).isEqualTo(RATIOS_STORY_ID_0)
    assertThat(promotedStory.topicId).isEqualTo(RATIOS_TOPIC_ID)
    assertThat(promotedStory.nextChapterName).isEqualTo("What is a Ratio?")
    assertThat(promotedStory.topicName).isEqualTo("Ratios and Proportional Reasoning")
    assertThat(promotedStory.lessonThumbnail.thumbnailGraphic)
      .isEqualTo(LessonThumbnailGraphic.CHILD_WITH_FRACTIONS_HOMEWORK)
    assertThat(promotedStory.completedChapterCount).isEqualTo(0)
    assertThat(promotedStory.isTopicLearned).isFalse()
    assertThat(promotedStory.totalChapterCount).isEqualTo(2)
  }

  private fun verifyOngoingStoryAsRatioStory0Exploration1(promotedStory: PromotedStory) {
    assertThat(promotedStory.explorationId).isEqualTo(RATIOS_EXPLORATION_ID_1)
    assertThat(promotedStory.storyId).isEqualTo(RATIOS_STORY_ID_0)
    assertThat(promotedStory.topicId).isEqualTo(RATIOS_TOPIC_ID)
    assertThat(promotedStory.nextChapterName).isEqualTo("Order is important")
    assertThat(promotedStory.topicName).isEqualTo("Ratios and Proportional Reasoning")
    assertThat(promotedStory.lessonThumbnail.thumbnailGraphic)
      .isEqualTo(LessonThumbnailGraphic.CHILD_WITH_FRACTIONS_HOMEWORK)
    assertThat(promotedStory.completedChapterCount).isEqualTo(1)
    assertThat(promotedStory.isTopicLearned).isFalse()
    assertThat(promotedStory.totalChapterCount).isEqualTo(2)
  }

  private fun verifyUpcomingTopic1(upcomingTopic: UpcomingTopic) {
    assertThat(upcomingTopic.topicId).isEqualTo(UPCOMING_TOPIC_ID_1)
    assertThat(upcomingTopic.name).isEqualTo("Third Test Topic")
    assertThat(upcomingTopic.lessonThumbnail.thumbnailGraphic)
      .isEqualTo(LessonThumbnailGraphic.CHILD_WITH_FRACTIONS_HOMEWORK)
  }

  private fun verifyOngoingStoryAsRatioStory1Exploration2(promotedStory: PromotedStory) {
    assertThat(promotedStory.explorationId).isEqualTo(RATIOS_EXPLORATION_ID_2)
    assertThat(promotedStory.storyId).isEqualTo(RATIOS_STORY_ID_1)
    assertThat(promotedStory.topicId).isEqualTo(RATIOS_TOPIC_ID)
    assertThat(promotedStory.nextChapterName).isEqualTo("Equivalent Ratios")
    assertThat(promotedStory.topicName).isEqualTo("Ratios and Proportional Reasoning")
    assertThat(promotedStory.lessonThumbnail.thumbnailGraphic)
      .isEqualTo(LessonThumbnailGraphic.CHILD_WITH_CUPCAKES)
    assertThat(promotedStory.completedChapterCount).isEqualTo(0)
    assertThat(promotedStory.isTopicLearned).isFalse()
    assertThat(promotedStory.totalChapterCount).isEqualTo(2)
  }

  private fun verifyOngoingStoryAsRatioStory1Exploration3(promotedStory: PromotedStory) {
    assertThat(promotedStory.explorationId).isEqualTo(RATIOS_EXPLORATION_ID_3)
    assertThat(promotedStory.storyId).isEqualTo(RATIOS_STORY_ID_1)
    assertThat(promotedStory.topicId).isEqualTo(RATIOS_TOPIC_ID)
    assertThat(promotedStory.nextChapterName).isEqualTo("Writing Ratios in Simplest Form")
    assertThat(promotedStory.topicName).isEqualTo("Ratios and Proportional Reasoning")
    assertThat(promotedStory.lessonThumbnail.thumbnailGraphic)
      .isEqualTo(LessonThumbnailGraphic.CHILD_WITH_CUPCAKES)
    assertThat(promotedStory.completedChapterCount).isEqualTo(1)
    assertThat(promotedStory.isTopicLearned).isFalse()
    assertThat(promotedStory.totalChapterCount).isEqualTo(2)
  }

  private fun getCurrentTimestamp(): Long {
    return Date().time
  }

  // Returns a timestamp which is atleast a week older than current timestamp.
  private fun getOldTimestamp(): Long {
    return Date().time - NINE_DAYS_IN_MS
  }

  private fun retrieveTopicList(): TopicList {
    val topicListLiveData = topicListController.getTopicList().toLiveData()
    topicListLiveData.observeForever(mockTopicListObserver)
    testCoroutineDispatchers.runCurrent()
    verify(mockTopicListObserver).onChanged(topicListResultCaptor.capture())
    return topicListResultCaptor.value.getOrThrow()
  }

  private fun retrievePromotedActivityList(): PromotedActivityList {
    testCoroutineDispatchers.runCurrent()
    topicListController.getPromotedActivityList(profileId0).toLiveData()
      .observeForever(mockPromotedActivityListObserver)
    testCoroutineDispatchers.runCurrent()
    verifyGetPromotedActivityListSucceeded()
    return promotedActivityListResultCaptor.value.getOrThrow()
  }

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }

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
      return "oppiaserver-resources/"
    }

    @Provides
    @ImageDownloadUrlTemplate
    @Singleton
    fun provideImageDownloadUrlTemplate(): String {
      return "%s/%s/assets/image/%s"
    }

    @EnableConsoleLog
    @Provides
    fun provideEnableConsoleLog(): Boolean = true

    @EnableFileLog
    @Provides
    fun provideEnableFileLog(): Boolean = false

    @GlobalLogLevel
    @Provides
    fun provideGlobalLogLevel(): LogLevel = LogLevel.VERBOSE
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, TestLogReportingModule::class, LogStorageModule::class,
      TestDispatcherModule::class, RobolectricModule::class
    ]
  )
  interface TestApplicationComponent : DataProvidersInjector {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(topicListControllerTest: TopicListControllerTest)
  }

  class TestApplication : Application(), DataProvidersInjectorProvider {
    private val component: TestApplicationComponent by lazy {
      DaggerTopicListControllerTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(topicListControllerTest: TopicListControllerTest) {
      component.inject(topicListControllerTest)
    }

    override fun getDataProvidersInjector(): DataProvidersInjector = component
  }
}
