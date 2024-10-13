package org.oppia.android.domain.topic

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
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.PromotedActivityList
import org.oppia.android.app.model.PromotedStory
import org.oppia.android.app.model.TopicRecord
import org.oppia.android.app.model.UpcomingTopic
import org.oppia.android.domain.classroom.TEST_CLASSROOM_ID_0
import org.oppia.android.domain.classroom.TEST_CLASSROOM_ID_1
import org.oppia.android.domain.classroom.TEST_CLASSROOM_ID_2
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.testing.BuildEnvironment
import org.oppia.android.testing.OppiaTestRule
import org.oppia.android.testing.RunOn
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.environment.TestEnvironmentConfig
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.story.StoryProgressTestHelper
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClock
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.AssetRepository
import org.oppia.android.util.caching.LoadLessonProtosFromAssets
import org.oppia.android.util.caching.testing.FakeAssetRepository
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.gcsresource.DefaultResourceBucketName
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.EnableConsoleLog
import org.oppia.android.util.logging.EnableFileLog
import org.oppia.android.util.logging.GlobalLogLevel
import org.oppia.android.util.logging.LogLevel
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.parser.image.DefaultGcsPrefix
import org.oppia.android.util.parser.image.ImageDownloadUrlTemplate
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [TopicListController]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = TopicListControllerTest.TestApplication::class)
class TopicListControllerTest {
  @get:Rule val oppiaTestRule = OppiaTestRule()

  @Inject lateinit var context: Context
  @Inject lateinit var topicListController: TopicListController
  @Inject lateinit var storyProgressTestHelper: StoryProgressTestHelper
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject lateinit var fakeOppiaClock: FakeOppiaClock
  @Inject lateinit var monitorFactory: DataProviderTestMonitor.Factory
  @Inject lateinit var storyProgressController: StoryProgressController
  @Inject lateinit var fakeAssetRepository: FakeAssetRepository

  private lateinit var profileId0: ProfileId

  @Before
  fun setUp() {
    profileId0 = ProfileId.newBuilder().setLoggedInInternalProfileId(0).build()
    setUpTestApplicationComponent()

    // Use uptime millis for time tracking since that allows proper time management for recorded
    // activities that need to be measurably apart from one another (such as completed chapters).
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
  }

  @Test
  fun testRetrieveTopicList_isSuccessful() {
    val topicListProvider = topicListController.getTopicList(profileId0)

    monitorFactory.waitForNextSuccessfulResult(topicListProvider)
  }

  @Test
  fun testRetrieveTopicList_providesListOfMultipleTopics() {
    val topicList = retrieveTopicList()

    assertThat(topicList.topicSummaryCount).isGreaterThan(1)
  }

  @Test
  fun testRetrieveTopicList_firstTopic_hasCorrectTopicInfo() {
    val topicList = retrieveTopicList()

    val firstTopic = topicList.getTopicSummary(0).topicSummary
    assertThat(firstTopic.topicId).isEqualTo(TEST_TOPIC_ID_0)
    assertThat(firstTopic.title.html).isEqualTo("First Test Topic")
  }

  @Test
  fun testRetrieveTopicList_firstTopic_hasCorrectClassroomInfo() {
    val topicList = retrieveTopicList()

    val firstTopic = topicList.getTopicSummary(0)
    assertThat(firstTopic.topicSummary.classroomId).isEqualTo(TEST_CLASSROOM_ID_0)
    assertThat(firstTopic.classroomTitle.html).isEqualTo("Science")
  }

  @Test
  fun testRetrieveTopicList_firstTopic_hasCorrectLessonCount() {
    val topicList = retrieveTopicList()

    val firstTopic = topicList.getTopicSummary(0).topicSummary
    assertThat(firstTopic.totalChapterCount).isEqualTo(3)
  }

  @Test
  fun testRetrieveTopicList_secondTopic_hasCorrectTopicInfo() {
    val topicList = retrieveTopicList()

    val secondTopic = topicList.getTopicSummary(1).topicSummary
    assertThat(secondTopic.topicId).isEqualTo(TEST_TOPIC_ID_1)
    assertThat(secondTopic.title.html).isEqualTo("Second Test Topic")
  }

  @Test
  fun testRetrieveTopicList_secondTopic_hasCorrectClassroomInfo() {
    val topicList = retrieveTopicList()

    val firstTopic = topicList.getTopicSummary(1)
    assertThat(firstTopic.topicSummary.classroomId).isEqualTo(TEST_CLASSROOM_ID_0)
    assertThat(firstTopic.classroomTitle.html).isEqualTo("Science")
  }

  @Test
  fun testRetrieveTopicList_secondTopic_hasCorrectLessonCount() {
    val topicList = retrieveTopicList()

    val secondTopic = topicList.getTopicSummary(1).topicSummary
    assertThat(secondTopic.totalChapterCount).isEqualTo(1)
  }

  @Test
  fun testRetrieveTopicList_fractionsTopic_hasCorrectTopicInfo() {
    val topicList = retrieveTopicList()

    val fractionsTopic = topicList.getTopicSummary(2).topicSummary
    assertThat(fractionsTopic.topicId).isEqualTo(FRACTIONS_TOPIC_ID)
    assertThat(fractionsTopic.title.html).isEqualTo("Fractions")
  }

  @Test
  fun testRetrieveTopicList_fractionsTopic_hasCorrectClassroomInfo() {
    val topicList = retrieveTopicList()

    val firstTopic = topicList.getTopicSummary(2)
    assertThat(firstTopic.topicSummary.classroomId).isEqualTo(TEST_CLASSROOM_ID_1)
    assertThat(firstTopic.classroomTitle.html).isEqualTo("Maths")
  }

  @Test
  fun testRetrieveTopicList_fractionsTopic_hasCorrectLessonCount() {
    val topicList = retrieveTopicList()

    val fractionsTopic = topicList.getTopicSummary(2).topicSummary
    assertThat(fractionsTopic.totalChapterCount).isEqualTo(2)
  }

  @Test
  fun testRetrieveTopicList_ratiosTopic_hasCorrectTopicInfo() {
    val topicList = retrieveTopicList()

    val ratiosTopic = topicList.getTopicSummary(3).topicSummary
    assertThat(ratiosTopic.topicId).isEqualTo(RATIOS_TOPIC_ID)
    assertThat(ratiosTopic.title.html).isEqualTo("Ratios and Proportional Reasoning")
  }

  @Test
  fun testRetrieveTopicList_ratiosTopic_hasCorrectClassroomInfo() {
    val topicList = retrieveTopicList()

    val firstTopic = topicList.getTopicSummary(3)
    assertThat(firstTopic.topicSummary.classroomId).isEqualTo(TEST_CLASSROOM_ID_1)
    assertThat(firstTopic.classroomTitle.html).isEqualTo("Maths")
  }

  @Test
  fun testRetrieveTopicList_ratiosTopic_hasCorrectLessonCount() {
    val topicList = retrieveTopicList()

    val ratiosTopic = topicList.getTopicSummary(3).topicSummary
    assertThat(ratiosTopic.totalChapterCount).isEqualTo(4)
  }

  @Test
  fun testRetrieveTopicList_doesNotContainUnavailableTopic() {
    val topicList = retrieveTopicList()

    // Verify that the topic list does not contain a not-yet published topic (since it can't be
    // played by the user).
    val topicIds = topicList.topicSummaryList.map { it.topicSummary }.map { it.getTopicId() }
    assertThat(topicIds).doesNotContain(TEST_TOPIC_ID_2)
  }

  @Test
  fun testRetrievePromotedActivityList_defaultLesson_hasCorrectInfo() {
    val promotedActivityProvider = topicListController.getPromotedActivityList(profileId0)

    monitorFactory.waitForNextSuccessfulResult(promotedActivityProvider)
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
  @RunOn(buildEnvironments = [BuildEnvironment.BAZEL]) // Only uses protos, so restrict to Bazel.
  fun testGetPromotedActivityList_startFractions_thenUnpublish_doesNotIncludeFractionsInList() {
    storyProgressTestHelper.markInProgressNotSavedFractionsStory0Exp0(
      profileId0,
      timestampOlderThanOneWeek = false
    )

    // Force the fractions topic to become unpublished (to simulate the failure case).
    fakeAssetRepository.setProtoAssetOverride(
      assetName = FRACTIONS_TOPIC_ID, proto = TopicRecord.getDefaultInstance()
    )

    val promotedActivityList = retrievePromotedActivityList()
    assertThat(promotedActivityList.promotedStoryList.recentlyPlayedStoryList).isEmpty()
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

  @Test
  @RunOn(buildEnvironments = [BuildEnvironment.BAZEL]) // The failure is specific to loading protos.
  fun testGetPromotedActivityList_missingTopicsWithProgress_doesNotIncludeThoseTopics() {
    // This is a slightly hacky way to simulate a previous topic's progress that works because
    // StoryProgressController doesn't verify whether the IDs passed to it correspond to locally
    // available topics.
    val previousTopicId = "previous_topic_id"
    val recordProgressDataProvider =
      storyProgressController.recordChapterAsInProgressSaved(
        profileId0,
        topicId = previousTopicId,
        storyId = "previous_story_id",
        explorationId = "previous_exploration_id",
        lastPlayedTimestamp = 123456789L
      )
    monitorFactory.ensureDataProviderExecutes(recordProgressDataProvider)

    val promotionList = retrievePromotedActivityList()

    val promotedStoryList = promotionList.promotedStoryList
    val olderTopicIds = promotedStoryList.olderPlayedStoryList.map { it.topicId }
    val recentTopicIds = promotedStoryList.recentlyPlayedStoryList.map { it.topicId }
    val upcomingTopicIds = promotionList.comingSoonTopicList.upcomingTopicList.map { it.topicId }
    assertThat(olderTopicIds).doesNotContain(previousTopicId)
    assertThat(recentTopicIds).doesNotContain(previousTopicId)
    assertThat(upcomingTopicIds).doesNotContain(previousTopicId)
  }

  private fun verifyPromotedStoryAsFirstTestTopicStory0Exploration0(promotedStory: PromotedStory) {
    assertThat(promotedStory.explorationId).isEqualTo(TEST_EXPLORATION_ID_2)
    assertThat(promotedStory.storyId).isEqualTo(TEST_STORY_ID_0)
    assertThat(promotedStory.topicId).isEqualTo(TEST_TOPIC_ID_0)
    assertThat(promotedStory.topicTitle.html).isEqualTo("First Test Topic")
    assertThat(promotedStory.classroomId).isEqualTo(TEST_CLASSROOM_ID_0)
    assertThat(promotedStory.classroomTitle.html).isEqualTo("Science")
    assertThat(promotedStory.nextChapterTitle.html).isEqualTo("Prototype Exploration")
    assertThat(promotedStory.completedChapterCount).isEqualTo(0)
    assertThat(promotedStory.isTopicLearned).isFalse()
    assertThat(promotedStory.totalChapterCount).isEqualTo(3)
  }

  private fun verifyOngoingStoryAsFirstTopicStory0Exploration0(promotedStory: PromotedStory) {
    assertThat(promotedStory.explorationId).isEqualTo(TEST_EXPLORATION_ID_2)
    assertThat(promotedStory.storyId).isEqualTo(TEST_STORY_ID_0)
    assertThat(promotedStory.topicId).isEqualTo(TEST_TOPIC_ID_0)
    assertThat(promotedStory.topicTitle.html).isEqualTo("First Test Topic")
    assertThat(promotedStory.classroomId).isEqualTo(TEST_CLASSROOM_ID_0)
    assertThat(promotedStory.classroomTitle.html).isEqualTo("Science")
    assertThat(promotedStory.nextChapterTitle.html).isEqualTo("Prototype Exploration")
    assertThat(promotedStory.completedChapterCount).isEqualTo(0)
    assertThat(promotedStory.isTopicLearned).isFalse()
    assertThat(promotedStory.totalChapterCount).isEqualTo(3)
  }

  private fun verifyPromotedStoryAsSecondTestTopicStory0Exploration0(promotedStory: PromotedStory) {
    assertThat(promotedStory.explorationId).isEqualTo(TEST_EXPLORATION_ID_4)
    assertThat(promotedStory.storyId).isEqualTo(TEST_STORY_ID_2)
    assertThat(promotedStory.topicId).isEqualTo(TEST_TOPIC_ID_1)
    assertThat(promotedStory.topicTitle.html).isEqualTo("Second Test Topic")
    assertThat(promotedStory.classroomId).isEqualTo(TEST_CLASSROOM_ID_0)
    assertThat(promotedStory.classroomTitle.html).isEqualTo("Science")
    assertThat(promotedStory.nextChapterTitle.html).isEqualTo("Fifth Exploration")
    assertThat(promotedStory.completedChapterCount).isEqualTo(0)
    assertThat(promotedStory.isTopicLearned).isFalse()
    assertThat(promotedStory.totalChapterCount).isEqualTo(1)
  }

  private fun verifyOngoingStoryAsFractionStory0Exploration0(promotedStory: PromotedStory) {
    assertThat(promotedStory.explorationId).isEqualTo(FRACTIONS_EXPLORATION_ID_0)
    assertThat(promotedStory.storyId).isEqualTo(FRACTIONS_STORY_ID_0)
    assertThat(promotedStory.topicId).isEqualTo(FRACTIONS_TOPIC_ID)
    assertThat(promotedStory.topicTitle.html).isEqualTo("Fractions")
    assertThat(promotedStory.classroomId).isEqualTo(TEST_CLASSROOM_ID_1)
    assertThat(promotedStory.classroomTitle.html).isEqualTo("Maths")
    assertThat(promotedStory.nextChapterTitle.html).isEqualTo("What is a Fraction?")
    assertThat(promotedStory.completedChapterCount).isEqualTo(0)
    assertThat(promotedStory.isTopicLearned).isFalse()
    assertThat(promotedStory.totalChapterCount).isEqualTo(2)
  }

  private fun verifyPromotedStoryAsFractionStory0Exploration0(promotedStory: PromotedStory) {
    assertThat(promotedStory.explorationId).isEqualTo(FRACTIONS_EXPLORATION_ID_0)
    assertThat(promotedStory.storyId).isEqualTo(FRACTIONS_STORY_ID_0)
    assertThat(promotedStory.topicId).isEqualTo(FRACTIONS_TOPIC_ID)
    assertThat(promotedStory.topicTitle.html).isEqualTo("Fractions")
    assertThat(promotedStory.classroomId).isEqualTo(TEST_CLASSROOM_ID_1)
    assertThat(promotedStory.classroomTitle.html).isEqualTo("Maths")
    assertThat(promotedStory.nextChapterTitle.html).isEqualTo("What is a Fraction?")
    assertThat(promotedStory.completedChapterCount).isEqualTo(0)
    assertThat(promotedStory.isTopicLearned).isFalse()
    assertThat(promotedStory.totalChapterCount).isEqualTo(2)
  }

  private fun verifyOngoingStoryAsFractionStory0Exploration1(promotedStory: PromotedStory) {
    assertThat(promotedStory.explorationId).isEqualTo(FRACTIONS_EXPLORATION_ID_1)
    assertThat(promotedStory.storyId).isEqualTo(FRACTIONS_STORY_ID_0)
    assertThat(promotedStory.topicId).isEqualTo(FRACTIONS_TOPIC_ID)
    assertThat(promotedStory.topicTitle.html).isEqualTo("Fractions")
    assertThat(promotedStory.classroomId).isEqualTo(TEST_CLASSROOM_ID_1)
    assertThat(promotedStory.classroomTitle.html).isEqualTo("Maths")
    assertThat(promotedStory.nextChapterTitle.html).isEqualTo("The Meaning of Equal Parts")
    assertThat(promotedStory.completedChapterCount).isEqualTo(1)
    assertThat(promotedStory.totalChapterCount).isEqualTo(2)
  }

  private fun verifyOngoingStoryAsRatioStory0Exploration0(promotedStory: PromotedStory) {
    assertThat(promotedStory.explorationId).isEqualTo(RATIOS_EXPLORATION_ID_0)
    assertThat(promotedStory.storyId).isEqualTo(RATIOS_STORY_ID_0)
    assertThat(promotedStory.topicId).isEqualTo(RATIOS_TOPIC_ID)
    assertThat(promotedStory.nextChapterTitle.html).isEqualTo("What is a Ratio?")
    assertThat(promotedStory.classroomId).isEqualTo(TEST_CLASSROOM_ID_1)
    assertThat(promotedStory.classroomTitle.html).isEqualTo("Maths")
    assertThat(promotedStory.topicTitle.html).isEqualTo("Ratios and Proportional Reasoning")
    assertThat(promotedStory.completedChapterCount).isEqualTo(0)
    assertThat(promotedStory.isTopicLearned).isFalse()
    assertThat(promotedStory.totalChapterCount).isEqualTo(2)
  }

  private fun verifyPromotedStoryAsRatioStory0Exploration0(promotedStory: PromotedStory) {
    assertThat(promotedStory.explorationId).isEqualTo(RATIOS_EXPLORATION_ID_0)
    assertThat(promotedStory.storyId).isEqualTo(RATIOS_STORY_ID_0)
    assertThat(promotedStory.topicId).isEqualTo(RATIOS_TOPIC_ID)
    assertThat(promotedStory.nextChapterTitle.html).isEqualTo("What is a Ratio?")
    assertThat(promotedStory.classroomId).isEqualTo(TEST_CLASSROOM_ID_1)
    assertThat(promotedStory.classroomTitle.html).isEqualTo("Maths")
    assertThat(promotedStory.topicTitle.html).isEqualTo("Ratios and Proportional Reasoning")
    assertThat(promotedStory.completedChapterCount).isEqualTo(0)
    assertThat(promotedStory.isTopicLearned).isFalse()
    assertThat(promotedStory.totalChapterCount).isEqualTo(2)
  }

  private fun verifyOngoingStoryAsRatioStory0Exploration1(promotedStory: PromotedStory) {
    assertThat(promotedStory.explorationId).isEqualTo(RATIOS_EXPLORATION_ID_1)
    assertThat(promotedStory.storyId).isEqualTo(RATIOS_STORY_ID_0)
    assertThat(promotedStory.topicId).isEqualTo(RATIOS_TOPIC_ID)
    assertThat(promotedStory.nextChapterTitle.html).isEqualTo("Order is important")
    assertThat(promotedStory.topicTitle.html).isEqualTo("Ratios and Proportional Reasoning")
    assertThat(promotedStory.classroomId).isEqualTo(TEST_CLASSROOM_ID_1)
    assertThat(promotedStory.classroomTitle.html).isEqualTo("Maths")
    assertThat(promotedStory.completedChapterCount).isEqualTo(1)
    assertThat(promotedStory.isTopicLearned).isFalse()
    assertThat(promotedStory.totalChapterCount).isEqualTo(2)
  }

  private fun verifyUpcomingTopic1(upcomingTopic: UpcomingTopic) {
    assertThat(upcomingTopic.topicId).isEqualTo(UPCOMING_TOPIC_ID_1)
    assertThat(upcomingTopic.title.html).isEqualTo("Third Test Topic")
    assertThat(upcomingTopic.classroomId).isEqualTo(TEST_CLASSROOM_ID_2)
    assertThat(upcomingTopic.classroomTitle.html).isEqualTo("English")
  }

  private fun verifyOngoingStoryAsRatioStory1Exploration2(
    promotedStory: PromotedStory,
    expectedToBeLearned: Boolean = false
  ) {
    assertThat(promotedStory.explorationId).isEqualTo(RATIOS_EXPLORATION_ID_2)
    assertThat(promotedStory.storyId).isEqualTo(RATIOS_STORY_ID_1)
    assertThat(promotedStory.topicId).isEqualTo(RATIOS_TOPIC_ID)
    assertThat(promotedStory.nextChapterTitle.html).isEqualTo("Equivalent Ratios")
    assertThat(promotedStory.topicTitle.html).isEqualTo("Ratios and Proportional Reasoning")
    assertThat(promotedStory.classroomId).isEqualTo(TEST_CLASSROOM_ID_1)
    assertThat(promotedStory.classroomTitle.html).isEqualTo("Maths")
    assertThat(promotedStory.completedChapterCount).isEqualTo(0)
    assertThat(promotedStory.isTopicLearned).isEqualTo(expectedToBeLearned)
    assertThat(promotedStory.totalChapterCount).isEqualTo(2)
  }

  private fun verifyOngoingStoryAsRatioStory1Exploration3(promotedStory: PromotedStory) {
    assertThat(promotedStory.explorationId).isEqualTo(RATIOS_EXPLORATION_ID_3)
    assertThat(promotedStory.storyId).isEqualTo(RATIOS_STORY_ID_1)
    assertThat(promotedStory.topicId).isEqualTo(RATIOS_TOPIC_ID)
    assertThat(promotedStory.nextChapterTitle.html).isEqualTo("Writing Ratios in Simplest Form")
    assertThat(promotedStory.topicTitle.html).isEqualTo("Ratios and Proportional Reasoning")
    assertThat(promotedStory.classroomId).isEqualTo(TEST_CLASSROOM_ID_1)
    assertThat(promotedStory.classroomTitle.html).isEqualTo("Maths")
    assertThat(promotedStory.completedChapterCount).isEqualTo(1)
    assertThat(promotedStory.isTopicLearned).isFalse()
    assertThat(promotedStory.totalChapterCount).isEqualTo(2)
  }

  private fun retrieveTopicList() =
    monitorFactory.waitForNextSuccessfulResult(topicListController.getTopicList(profileId0))

  private fun retrievePromotedActivityList(): PromotedActivityList {
    return monitorFactory.waitForNextSuccessfulResult(
      topicListController.getPromotedActivityList(profileId0)
    )
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
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
    @LoadLessonProtosFromAssets
    fun provideLoadLessonProtosFromAssets(testEnvironmentConfig: TestEnvironmentConfig): Boolean =
      testEnvironmentConfig.isUsingBazel()

    @Provides
    fun provideFakeAssetRepository(fakeImpl: FakeAssetRepository): AssetRepository = fakeImpl
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(
    modules = [
      TestModule::class, TestLogReportingModule::class, LogStorageModule::class,
      TestDispatcherModule::class, RobolectricModule::class, FakeOppiaClockModule::class,
      NetworkConnectionUtilDebugModule::class, LocaleProdModule::class,
      LoggingIdentifierModule::class, ApplicationLifecycleModule::class,
      SyncStatusModule::class, PlatformParameterModule::class,
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
