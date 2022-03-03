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
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.PromotedActivityList
import org.oppia.android.app.model.PromotedStory
import org.oppia.android.app.model.TopicList
import org.oppia.android.app.model.TopicSummary
import org.oppia.android.app.model.UpcomingTopic
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.environment.TestEnvironmentConfig
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.story.StoryProgressTestHelper
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClock
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.CacheAssetsLocally
import org.oppia.android.util.caching.LoadLessonProtosFromAssets
import org.oppia.android.util.caching.TopicListToCache
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.gcsresource.DefaultResourceBucketName
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.parser.image.DefaultGcsPrefix
import org.oppia.android.util.parser.image.ImageDownloadUrlTemplate
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.util.logging.SyncStatusModule

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
  lateinit var storyProgressTestHelper: StoryProgressTestHelper

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var fakeOppiaClock: FakeOppiaClock

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

    // Use uptime millis for time tracking since that allows proper time management for recorded
    // activities that need to be measurably apart from one another (such as completed chapters).
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

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
  fun testRetrieveTopicList_firstTopic_hasCorrectLessonCount() {
    val topicList = retrieveTopicList()
    val firstTopic = topicList.getTopicSummary(0)
    assertThat(firstTopic.totalChapterCount).isEqualTo(2)
  }

  @Test
  fun testRetrieveTopicList_secondTopic_hasCorrectTopicInfo() {
    val topicList = retrieveTopicList()
    val secondTopic = topicList.getTopicSummary(1)
    assertThat(secondTopic.topicId).isEqualTo(TEST_TOPIC_ID_1)
    assertThat(secondTopic.name).isEqualTo("Second Test Topic")
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
  fun testGetPromotedActivityList_markFracStory0Exp0InProgressSaved_ongoingStoryListIsCorrect() {
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId0,
      timestampOlderThanOneWeek = false
    )

    val promotedActivityList = retrievePromotedActivityList()
    assertThat(promotedActivityList.promotedStoryList.recentlyPlayedStoryCount)
      .isEqualTo(1)
    verifyOngoingStoryAsFractionStory0Exploration0(
      promotedActivityList.promotedStoryList.recentlyPlayedStoryList[0]
    )
  }

  @Test
  fun testGetPromotedActivityList_markRecentlyPlayedFracStory0Exp0_ongoingStoryListIsCorrect() {
    storyProgressTestHelper.markInProgressNotSavedFractionsStory0Exp0(
      profileId0,
      timestampOlderThanOneWeek = false
    )

    val promotedActivityList = retrievePromotedActivityList()
    assertThat(promotedActivityList.promotedStoryList.recentlyPlayedStoryCount)
      .isEqualTo(1)
    verifyOngoingStoryAsFractionStory0Exploration0(
      promotedActivityList.promotedStoryList.recentlyPlayedStoryList[0]
    )
  }

  @Test
  fun testGetPromotedStoryList_markChapDoneFracStory0Exp0_ongoingStoryListIsCorrect() {
    storyProgressTestHelper.markCompletedFractionsStory0Exp0(
      profileId0,
      timestampOlderThanOneWeek = false
    )

    val promotedActivityList = retrievePromotedActivityList()
    assertThat(promotedActivityList.promotedStoryList.recentlyPlayedStoryCount)
      .isEqualTo(1)
    verifyOngoingStoryAsFractionStory0Exploration1(
      promotedActivityList.promotedStoryList.recentlyPlayedStoryList[0]
    )
  }

  @Test
  fun testStoryList_markChapDoneFracStory0Exp0_fracStory0Exp1ProgSaved_ongoingStoryListCorrect() {
    storyProgressTestHelper.markCompletedFractionsStory0Exp0(
      profileId0,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp1(
      profileId0,
      timestampOlderThanOneWeek = false
    )

    val promotedActivityList = retrievePromotedActivityList()
    assertThat(promotedActivityList.promotedStoryList.recentlyPlayedStoryCount)
      .isEqualTo(1)
    verifyOngoingStoryAsFractionStory0Exploration1(
      promotedActivityList.promotedStoryList.recentlyPlayedStoryList[0]
    )
  }

  @Test
  fun testGetStoryList_markChapDoneFracStory0Exp0_FracStory0Exp1ProgNotSaved_ongoingStoryListCrt() {
    storyProgressTestHelper.markCompletedFractionsStory0Exp0(
      profileId0,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markInProgressNotSavedFractionsStory0Exp1(
      profileId0,
      timestampOlderThanOneWeek = false
    )

    val promotedActivityList = retrievePromotedActivityList()
    assertThat(promotedActivityList.promotedStoryList.recentlyPlayedStoryCount)
      .isEqualTo(1)
    verifyOngoingStoryAsFractionStory0Exploration1(
      promotedActivityList.promotedStoryList.recentlyPlayedStoryList[0]
    )
  }

  @Test
  fun testGetPromotedStoryList_markAllChapsDoneInFractions_suggestedStoryListIsCorrect() {
    storyProgressTestHelper.markCompletedFractionsStory0(
      profileId0,
      timestampOlderThanOneWeek = false
    )

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
    storyProgressTestHelper.markInProgressSavedRatiosStory0Exp0(
      profileId0,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markInProgressNotSavedRatiosStory1Exp0(
      profileId0,
      timestampOlderThanOneWeek = false
    )

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
    storyProgressTestHelper.markCompletedRatiosStory0(
      profileId0,
      timestampOlderThanOneWeek = false
    )

    val promotedActivityList = retrievePromotedActivityList()
    assertThat(promotedActivityList.promotedStoryList.suggestedStoryCount)
      .isEqualTo(1)
    verifyPromotedStoryAsFractionStory0Exploration0(
      promotedActivityList.promotedStoryList.suggestedStoryList[0]
    )
  }

  @Test
  fun testGetStoryList_markStoryDoneOfRatiosAndFirstTestTopic_suggestedStoryListIsCorrect() {
    storyProgressTestHelper.markCompletedTestTopic0Story0(
      profileId0,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markCompletedRatiosStory0(
      profileId0,
      timestampOlderThanOneWeek = false
    )

    val promotedActivityList = retrievePromotedActivityList()

    assertThat(promotedActivityList.promotedStoryList.suggestedStoryCount)
      .isEqualTo(1)
    verifyPromotedStoryAsSecondTestTopicStory0Exploration0(
      promotedActivityList.promotedStoryList.suggestedStoryList[0]
    )
  }

  @Test
  fun testGetStoryList_noTopicProgress_defaultSuggestedStoryListIsCorrect() {
    val promotedActivityList = retrievePromotedActivityList()

    assertThat(promotedActivityList.promotedStoryList.recentlyPlayedStoryCount)
      .isEqualTo(0)
    assertThat(promotedActivityList.promotedStoryList.olderPlayedStoryCount)
      .isEqualTo(0)
    assertThat(promotedActivityList.promotedStoryList.suggestedStoryCount)
      .isEqualTo(2)
    verifyPromotedStoryAsFractionStory0Exploration0(
      promotedActivityList.promotedStoryList.suggestedStoryList[0]
    )
    verifyPromotedStoryAsRatioStory0Exploration0(
      promotedActivityList.promotedStoryList.suggestedStoryList[1]
    )
  }

  @Test
  fun testGetStoryList_markRecentlyPlayedFirstTestTopic_suggestedStoryListIsCorrect() {
    storyProgressTestHelper.markInProgressSavedTestTopic0Story0Exp0(
      profileId0,
      timestampOlderThanOneWeek = false
    )

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
    storyProgressTestHelper.markAllTopicsAsCompleted(
      profileId0,
      timestampOlderThanOneWeek = false
    )

    val promotedActivityList = retrievePromotedActivityList()

    assertThat(promotedActivityList.comingSoonTopicList.upcomingTopicCount)
      .isEqualTo(1)
  }

  @Test
  fun testGetStoryList_markAllChapDoneInSecondTestTopic_doesNotPromoteAnyStories() {
    storyProgressTestHelper.markCompletedTestTopic1Story0(
      profileId0,
      timestampOlderThanOneWeek = false
    )

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
    storyProgressTestHelper.markCompletedTestTopic1Story0(
      profileId0,
      timestampOlderThanOneWeek = false
    )

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
    storyProgressTestHelper.markCompletedFractionsStory0Exp0(
      profileId0,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markCompletedRatiosStory0Exp0(
      profileId0,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markCompletedRatiosStory1Exp0(
      profileId0,
      timestampOlderThanOneWeek = false
    )

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
    storyProgressTestHelper.markCompletedFractionsStory0Exp0(
      profileId0,
      timestampOlderThanOneWeek = true
    )
    storyProgressTestHelper.markCompletedRatiosStory0Exp0(
      profileId0,
      timestampOlderThanOneWeek = true
    )
    storyProgressTestHelper.markCompletedRatiosStory1Exp0(
      profileId0,
      timestampOlderThanOneWeek = true
    )

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
    storyProgressTestHelper.markInProgressSavedTestTopic0Story0Exp0(
      profileId0,
      timestampOlderThanOneWeek = false
    )

    val promotedActivityList = retrievePromotedActivityList()
    assertThat(promotedActivityList.promotedStoryList.recentlyPlayedStoryCount)
      .isEqualTo(1)
    verifyOngoingStoryAsFirstTopicStory0Exploration0(
      promotedActivityList.promotedStoryList.recentlyPlayedStoryList[0]
    )
  }

  @Test
  fun testGetStoryList_markOneStoryDoneForFirstTestTopic_suggestedStoryListIsCorrect() {
    storyProgressTestHelper.markCompletedTestTopic0Story0(
      profileId0,
      timestampOlderThanOneWeek = false
    )

    val promotedActivityList = retrievePromotedActivityList()

    assertThat(promotedActivityList.promotedStoryList.suggestedStoryCount)
      .isEqualTo(1)
    verifyPromotedStoryAsRatioStory0Exploration0(
      promotedActivityList.promotedStoryList.suggestedStoryList[0]
    )
  }

  @Test
  fun testGetStoryList_markOneStoryDoneAndPlayNextStoryOfRatiosTopic_ongoingListIsCorrect() {
    storyProgressTestHelper.markCompletedRatiosStory0(
      profileId0,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markInProgressSavedRatiosStory1Exp0(
      profileId0,
      timestampOlderThanOneWeek = false
    )

    val promotedActivityList = retrievePromotedActivityList()
    assertThat(promotedActivityList.promotedStoryList.recentlyPlayedStoryCount)
      .isEqualTo(1)
    assertThat(promotedActivityList.promotedStoryList.recentlyPlayedStoryList[0].isTopicLearned)
      .isTrue()
    verifyOngoingStoryAsRatioStory1Exploration2(
      promotedActivityList.promotedStoryList.recentlyPlayedStoryList[0],
      expectedToBeLearned = true // Since the first story was completed.
    )
  }

  @Test
  fun testGetStoryList_story0DonePlayStory1RatiosTopic_playRatios_firstTestTopicIsLearned() {
    storyProgressTestHelper.markCompletedRatiosStory0(
      profileId0,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markInProgressSavedRatiosStory1Exp0(
      profileId0,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId0,
      timestampOlderThanOneWeek = false
    )

    val promotedActivityList = retrievePromotedActivityList()
    assertThat(promotedActivityList.promotedStoryList.recentlyPlayedStoryCount)
      .isEqualTo(2)
    assertThat(promotedActivityList.promotedStoryList.recentlyPlayedStoryList[0].isTopicLearned)
      .isFalse()
    verifyOngoingStoryAsFractionStory0Exploration0(
      promotedActivityList.promotedStoryList.recentlyPlayedStoryList[0]
    )
    assertThat(promotedActivityList.promotedStoryList.recentlyPlayedStoryList[1].isTopicLearned)
      .isTrue()
    verifyOngoingStoryAsRatioStory1Exploration2(
      promotedActivityList.promotedStoryList.recentlyPlayedStoryList[1],
      expectedToBeLearned = true // Since the first story was completed.
    )
  }

  @Test
  fun testRetrieveStoryList_markFirstExpOfEveryStoryDoneWithinLastMonth_ongoingListIsCorrect() {
    storyProgressTestHelper.markCompletedFractionsStory0Exp0(
      profileId0,
      timestampOlderThanOneWeek = true
    )
    storyProgressTestHelper.markCompletedRatiosStory0Exp0(
      profileId0,
      timestampOlderThanOneWeek = true
    )
    storyProgressTestHelper.markCompletedRatiosStory1Exp0(
      profileId0,
      timestampOlderThanOneWeek = false
    )

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

  @Test
  fun testStoryList_markLessonInProgressSaved_anotherLessonInProgressNotSaved_ongoingListCorrect() {
    storyProgressTestHelper.markInProgressSavedRatiosStory0Exp0(
      profileId0,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markInProgressNotSavedRatiosStory1Exp0(
      profileId0,
      timestampOlderThanOneWeek = false
    )

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
    assertThat(promotedStory.completedChapterCount).isEqualTo(0)
    assertThat(promotedStory.isTopicLearned).isFalse()
    assertThat(promotedStory.totalChapterCount).isEqualTo(2)
  }

  private fun verifyOngoingStoryAsFirstTopicStory0Exploration0(promotedStory: PromotedStory) {
    assertThat(promotedStory.explorationId).isEqualTo(TEST_EXPLORATION_ID_2)
    assertThat(promotedStory.storyId).isEqualTo(TEST_STORY_ID_0)
    assertThat(promotedStory.topicId).isEqualTo(TEST_TOPIC_ID_0)
    assertThat(promotedStory.topicName).isEqualTo("First Test Topic")
    assertThat(promotedStory.nextChapterName).isEqualTo("Prototype Exploration")
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
    assertThat(promotedStory.completedChapterCount).isEqualTo(1)
    assertThat(promotedStory.totalChapterCount).isEqualTo(2)
  }

  private fun verifyOngoingStoryAsRatioStory0Exploration0(promotedStory: PromotedStory) {
    assertThat(promotedStory.explorationId).isEqualTo(RATIOS_EXPLORATION_ID_0)
    assertThat(promotedStory.storyId).isEqualTo(RATIOS_STORY_ID_0)
    assertThat(promotedStory.topicId).isEqualTo(RATIOS_TOPIC_ID)
    assertThat(promotedStory.nextChapterName).isEqualTo("What is a Ratio?")
    assertThat(promotedStory.topicName).isEqualTo("Ratios and Proportional Reasoning")
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
    assertThat(promotedStory.completedChapterCount).isEqualTo(1)
    assertThat(promotedStory.isTopicLearned).isFalse()
    assertThat(promotedStory.totalChapterCount).isEqualTo(2)
  }

  private fun verifyUpcomingTopic1(upcomingTopic: UpcomingTopic) {
    assertThat(upcomingTopic.topicId).isEqualTo(UPCOMING_TOPIC_ID_1)
    assertThat(upcomingTopic.name).isEqualTo("Third Test Topic")
  }

  private fun verifyOngoingStoryAsRatioStory1Exploration2(
    promotedStory: PromotedStory,
    expectedToBeLearned: Boolean = false
  ) {
    assertThat(promotedStory.explorationId).isEqualTo(RATIOS_EXPLORATION_ID_2)
    assertThat(promotedStory.storyId).isEqualTo(RATIOS_STORY_ID_1)
    assertThat(promotedStory.topicId).isEqualTo(RATIOS_TOPIC_ID)
    assertThat(promotedStory.nextChapterName).isEqualTo("Equivalent Ratios")
    assertThat(promotedStory.topicName).isEqualTo("Ratios and Proportional Reasoning")
    assertThat(promotedStory.completedChapterCount).isEqualTo(0)
    assertThat(promotedStory.isTopicLearned).isEqualTo(expectedToBeLearned)
    assertThat(promotedStory.totalChapterCount).isEqualTo(2)
  }

  private fun verifyOngoingStoryAsRatioStory1Exploration3(promotedStory: PromotedStory) {
    assertThat(promotedStory.explorationId).isEqualTo(RATIOS_EXPLORATION_ID_3)
    assertThat(promotedStory.storyId).isEqualTo(RATIOS_STORY_ID_1)
    assertThat(promotedStory.topicId).isEqualTo(RATIOS_TOPIC_ID)
    assertThat(promotedStory.nextChapterName).isEqualTo("Writing Ratios in Simplest Form")
    assertThat(promotedStory.topicName).isEqualTo("Ratios and Proportional Reasoning")
    assertThat(promotedStory.completedChapterCount).isEqualTo(1)
    assertThat(promotedStory.isTopicLearned).isFalse()
    assertThat(promotedStory.totalChapterCount).isEqualTo(2)
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

    @Provides
    @CacheAssetsLocally
    fun provideCacheAssetsLocally(): Boolean = false

    @Provides
    @TopicListToCache
    fun provideTopicListToCache(): List<String> = listOf()

    @Provides
    @LoadLessonProtosFromAssets
    fun provideLoadLessonProtosFromAssets(testEnvironmentConfig: TestEnvironmentConfig): Boolean =
      testEnvironmentConfig.isUsingBazel()
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, TestLogReportingModule::class, LogStorageModule::class,
      TestDispatcherModule::class, RobolectricModule::class, FakeOppiaClockModule::class,
      NetworkConnectionUtilDebugModule::class, AssetModule::class, LocaleProdModule::class,
      SyncStatusModule::class, PlatformParameterModule::class, LoggingIdentifierModule::class,
      PlatformParameterSingletonModule::class
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
