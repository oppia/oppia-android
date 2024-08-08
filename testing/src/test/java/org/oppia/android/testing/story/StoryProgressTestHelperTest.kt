package org.oppia.android.testing.story

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
import org.oppia.android.app.model.ChapterPlayState
import org.oppia.android.app.model.ChapterProgress
import org.oppia.android.app.model.ChapterSummary
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.StoryProgress
import org.oppia.android.app.model.StorySummary
import org.oppia.android.app.model.Topic
import org.oppia.android.app.model.TopicProgress
import org.oppia.android.app.model.TopicProgressDatabase
import org.oppia.android.data.persistence.PersistentCacheStore
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.platformparameter.PlatformParameterModule
import org.oppia.android.domain.platformparameter.PlatformParameterSingletonModule
import org.oppia.android.domain.topic.FRACTIONS_EXPLORATION_ID_0
import org.oppia.android.domain.topic.FRACTIONS_EXPLORATION_ID_1
import org.oppia.android.domain.topic.FRACTIONS_STORY_ID_0
import org.oppia.android.domain.topic.FRACTIONS_TOPIC_ID
import org.oppia.android.domain.topic.RATIOS_EXPLORATION_ID_0
import org.oppia.android.domain.topic.RATIOS_EXPLORATION_ID_1
import org.oppia.android.domain.topic.RATIOS_EXPLORATION_ID_2
import org.oppia.android.domain.topic.RATIOS_EXPLORATION_ID_3
import org.oppia.android.domain.topic.RATIOS_STORY_ID_0
import org.oppia.android.domain.topic.RATIOS_STORY_ID_1
import org.oppia.android.domain.topic.RATIOS_TOPIC_ID
import org.oppia.android.domain.topic.TEST_EXPLORATION_ID_13
import org.oppia.android.domain.topic.TEST_EXPLORATION_ID_2
import org.oppia.android.domain.topic.TEST_EXPLORATION_ID_4
import org.oppia.android.domain.topic.TEST_EXPLORATION_ID_5
import org.oppia.android.domain.topic.TEST_STORY_ID_0
import org.oppia.android.domain.topic.TEST_STORY_ID_2
import org.oppia.android.domain.topic.TEST_TOPIC_ID_0
import org.oppia.android.domain.topic.TEST_TOPIC_ID_1
import org.oppia.android.domain.topic.TopicController
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.environment.TestEnvironmentConfig
import org.oppia.android.testing.robolectric.RobolectricModule
import org.oppia.android.testing.threading.TestCoroutineDispatchers
import org.oppia.android.testing.threading.TestDispatcherModule
import org.oppia.android.testing.time.FakeOppiaClock
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.AssetModule
import org.oppia.android.util.caching.LoadLessonProtosFromAssets
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.locale.LocaleProdModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.SyncStatusModule
import org.oppia.android.util.networking.NetworkConnectionUtilDebugModule
import org.oppia.android.util.parser.image.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [StoryProgressTestHelper]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = StoryProgressTestHelperTest.TestApplication::class)
class StoryProgressTestHelperTest {
  @Inject lateinit var context: Context
  @Inject lateinit var storyProgressTestHelper: StoryProgressTestHelper
  @Inject lateinit var topicController: TopicController
  @Inject lateinit var persistentCacheStoreFactory: PersistentCacheStore.Factory
  @Inject lateinit var testCoroutineDispatchers: TestCoroutineDispatchers
  @Inject lateinit var fakeOppiaClock: FakeOppiaClock
  @Inject lateinit var monitorFactory: DataProviderTestMonitor.Factory

  private val profileId0: ProfileId by lazy { ProfileId.newBuilder().setLoggedInInternalProfileId(0).build() }
  private val profileId1: ProfileId by lazy { ProfileId.newBuilder().setLoggedInInternalProfileId(1).build() }

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
    fakeOppiaClock.setFakeTimeMode(FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS)
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  /* Test topic chapter completion tests. */

  @Test
  fun testMarkChapterDone_testTopic0_story0_exp2_chapterIsDone() {
    storyProgressTestHelper.markCompletedTestTopic0Story0Exp0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    val story0 = testTopic0.getStory(TEST_STORY_ID_0)
    val exp2 = story0.getChapter(TEST_EXPLORATION_ID_2)
    assertThat(exp2.isCompleted()).isTrue()
  }

  @Test
  fun testMarkChapterDone_testTopic0_story0_exp2_story0IsNotDone() {
    storyProgressTestHelper.markCompletedTestTopic0Story0Exp0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    val story0 = testTopic0.getStory(TEST_STORY_ID_0)
    assertThat(story0.isCompleted()).isFalse()
  }

  @Test
  fun testMarkChapterDone_testTopic0_story0_exp13_chapterIsDone() {
    storyProgressTestHelper.markCompletedTestTopic0Story0Exp1(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    val story0 = testTopic0.getStory(TEST_STORY_ID_0)
    val exp13 = story0.getChapter(TEST_EXPLORATION_ID_13)
    assertThat(exp13.isCompleted()).isTrue()
  }

  @Test
  fun testMarkChapterDone_testTopic0_story0_exp13_story0IsNotDone() {
    storyProgressTestHelper.markCompletedTestTopic0Story0Exp1(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    val story0 = testTopic0.getStory(TEST_STORY_ID_0)
    assertThat(story0.isCompleted()).isFalse()
  }

  @Test
  fun testMarkChapterDone_testTopic0_story0_exp5_chapterIsDone() {
    storyProgressTestHelper.markCompletedTestTopic0Story0Exp2(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    val story0 = testTopic0.getStory(TEST_STORY_ID_0)
    val exp13 = story0.getChapter(TEST_EXPLORATION_ID_5)
    assertThat(exp13.isCompleted()).isTrue()
  }

  @Test
  fun testMarkChapterDone_testTopic0_story0_exp5_story0IsDone() {
    storyProgressTestHelper.markCompletedTestTopic0Story0Exp2(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    val story0 = testTopic0.getStory(TEST_STORY_ID_0)
    // The story is completed since exp 5 requires exp 13 to be finished first.
    assertThat(story0.isCompleted()).isTrue()
  }

  @Test
  fun testMarkChapterDone_testTopic1_story2_exp4_chapterIsDone() {
    storyProgressTestHelper.markCompletedTestTopic1Story0Exp0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic1 = getTopic(profileId0, TEST_TOPIC_ID_1)
    val story2 = testTopic1.getStory(TEST_STORY_ID_2)
    val exp4 = story2.getChapter(TEST_EXPLORATION_ID_4)
    assertThat(exp4.isCompleted()).isTrue()
  }

  @Test
  fun testMarkChapterDone_testTopic1_story2_exp4_story2IsDone() {
    storyProgressTestHelper.markCompletedTestTopic1Story0Exp0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic1 = getTopic(profileId0, TEST_TOPIC_ID_1)
    val story2 = testTopic1.getStory(TEST_STORY_ID_2)
    assertThat(story2.isCompleted()).isTrue()
  }

  /* Test topic story completion tests. */

  @Test
  fun testMarkStoryDone_testTopic0_story0_storyIsDone() {
    storyProgressTestHelper.markCompletedTestTopic0Story0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    val story0 = testTopic0.getStory(TEST_STORY_ID_0)
    assertThat(story0.isCompleted()).isTrue()
  }

  @Test
  fun testMarkStoryDone_testTopic0_story0_topicIsDone() {
    storyProgressTestHelper.markCompletedTestTopic0Story0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    assertThat(testTopic0.isCompleted()).isTrue()
  }

  @Test
  fun testMarkStoryDone_testTopic1_story2_storyIsDone() {
    storyProgressTestHelper.markCompletedTestTopic1Story0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic1 = getTopic(profileId0, TEST_TOPIC_ID_1)
    val story2 = testTopic1.getStory(TEST_STORY_ID_2)
    assertThat(story2.isCompleted()).isTrue()
  }

  @Test
  fun testMarkStoryDone_testTopic1_story2_topicIsDone() {
    storyProgressTestHelper.markCompletedTestTopic1Story0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic1 = getTopic(profileId0, TEST_TOPIC_ID_1)
    assertThat(testTopic1.isCompleted()).isTrue()
  }

  @Test
  fun testMarkStoryDone_testTopic0_story0_topicIsPartiallyDone() {
    storyProgressTestHelper.markCompletedTestTopic0Story0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    assertThat(testTopic0.isPartiallyCompleted()).isTrue()
  }

  /* Test topic completion tests. */

  @Test
  fun testMarkTopicDone_testTopic0_topicIsDone() {
    storyProgressTestHelper.markCompletedTestTopic0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    val testTopic1 = getTopic(profileId0, TEST_TOPIC_ID_1)
    assertThat(testTopic0.isCompleted()).isTrue()
    assertThat(testTopic1.isCompleted()).isFalse()
  }

  @Test
  fun testMarkTopicDone_testTopic1_topicIsDone() {
    storyProgressTestHelper.markCompletedTestTopic1(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    val testTopic1 = getTopic(profileId0, TEST_TOPIC_ID_1)
    assertThat(testTopic0.isCompleted()).isFalse()
    assertThat(testTopic1.isCompleted()).isTrue()
  }

  @Test
  fun testMarkTopicsDone_testTopics_bothTestTopicsAreDone() {
    storyProgressTestHelper.markCompletedTestTopics(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    val testTopic1 = getTopic(profileId0, TEST_TOPIC_ID_1)
    assertThat(testTopic0.isCompleted()).isTrue()
    assertThat(testTopic1.isCompleted()).isTrue()
  }

  /* Ratios chapter completion tests. */

  @Test
  fun testMarkChapterDone_ratiosTopic_story0_exp0_chapterIsDone() {
    storyProgressTestHelper.markCompletedRatiosStory0Exp0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val ratiosTopic = getTopic(profileId0, RATIOS_TOPIC_ID)
    val story0 = ratiosTopic.getStory(RATIOS_STORY_ID_0)
    val exp0 = story0.getChapter(RATIOS_EXPLORATION_ID_0)
    assertThat(exp0.isCompleted()).isTrue()
  }

  @Test
  fun testMarkChapterDone_ratiosTopic_story0_exp0_story0IsNotDone() {
    storyProgressTestHelper.markCompletedRatiosStory0Exp0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val ratiosTopic = getTopic(profileId0, RATIOS_TOPIC_ID)
    val story0 = ratiosTopic.getStory(RATIOS_STORY_ID_0)
    assertThat(story0.isCompleted()).isFalse()
  }

  @Test
  fun testMarkChapterDone_ratiosTopic_story0_exp1_chapterIsDone() {
    storyProgressTestHelper.markCompletedRatiosStory0Exp1(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val ratiosTopic = getTopic(profileId0, RATIOS_TOPIC_ID)
    val story0 = ratiosTopic.getStory(RATIOS_STORY_ID_0)
    val exp1 = story0.getChapter(RATIOS_EXPLORATION_ID_1)
    assertThat(exp1.isCompleted()).isTrue()
  }

  @Test
  fun testMarkChapterDone_ratiosTopic_story0_exp1_story0IsDone() {
    storyProgressTestHelper.markCompletedRatiosStory0Exp1(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val ratiosTopic = getTopic(profileId0, RATIOS_TOPIC_ID)
    val story0 = ratiosTopic.getStory(RATIOS_STORY_ID_0)
    // The story is completed since exp 1 requires exp 0 to be finished first.
    assertThat(story0.isCompleted()).isTrue()
  }

  @Test
  fun testMarkChapterDone_ratiosTopic_story1_exp2_chapterIsDone() {
    storyProgressTestHelper.markCompletedRatiosStory1Exp0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val ratiosTopic = getTopic(profileId0, RATIOS_TOPIC_ID)
    val story1 = ratiosTopic.getStory(RATIOS_STORY_ID_1)
    val exp2 = story1.getChapter(RATIOS_EXPLORATION_ID_2)
    assertThat(exp2.isCompleted()).isTrue()
  }

  @Test
  fun testMarkChapterDone_ratiosTopic_story1_exp2_story1IsNotDone() {
    storyProgressTestHelper.markCompletedRatiosStory1Exp0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val ratiosTopic = getTopic(profileId0, RATIOS_TOPIC_ID)
    val story1 = ratiosTopic.getStory(RATIOS_STORY_ID_1)
    assertThat(story1.isCompleted()).isFalse()
  }

  @Test
  fun testMarkChapterDone_ratiosTopic_story1_exp3_chapterIsDone() {
    storyProgressTestHelper.markCompletedRatiosStory1Exp1(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val ratiosTopic = getTopic(profileId0, RATIOS_TOPIC_ID)
    val story1 = ratiosTopic.getStory(RATIOS_STORY_ID_1)
    val exp3 = story1.getChapter(RATIOS_EXPLORATION_ID_3)
    assertThat(exp3.isCompleted()).isTrue()
  }

  @Test
  fun testMarkChapterDone_ratiosTopic_story1_exp3_story1IsDone() {
    storyProgressTestHelper.markCompletedRatiosStory1Exp1(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val ratiosTopic = getTopic(profileId0, RATIOS_TOPIC_ID)
    val story1 = ratiosTopic.getStory(RATIOS_STORY_ID_1)
    // The story is completed since exp 3 requires exp 2 to be finished first.
    assertThat(story1.isCompleted()).isTrue()
  }

  /* Ratios story/topic completion tests. */

  @Test
  fun testMarkStoryDone_ratiosTopic_story0_storyIsDone() {
    storyProgressTestHelper.markCompletedRatiosStory0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val ratiosTopic = getTopic(profileId0, RATIOS_TOPIC_ID)
    val story0 = ratiosTopic.getStory(RATIOS_STORY_ID_0)
    val story1 = ratiosTopic.getStory(RATIOS_STORY_ID_1)
    assertThat(story0.isCompleted()).isTrue()
    assertThat(story1.isCompleted()).isFalse()
  }

  @Test
  fun testMarkStoryDone_ratiosTopic_story0_topicIsNotDone() {
    storyProgressTestHelper.markCompletedRatiosStory0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val ratiosTopic = getTopic(profileId0, RATIOS_TOPIC_ID)
    assertThat(ratiosTopic.isCompleted()).isFalse()
  }

  @Test
  fun testMarkStoryDone_ratiosTopic_story1_storyIsDone() {
    storyProgressTestHelper.markCompletedRatiosStory1(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val ratiosTopic = getTopic(profileId0, RATIOS_TOPIC_ID)
    val story0 = ratiosTopic.getStory(RATIOS_STORY_ID_0)
    val story1 = ratiosTopic.getStory(RATIOS_STORY_ID_1)
    assertThat(story0.isCompleted()).isFalse()
    assertThat(story1.isCompleted()).isTrue()
  }

  @Test
  fun testMarkStoryDone_ratiosTopic_story1_topicIsNotDone() {
    storyProgressTestHelper.markCompletedRatiosStory0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val ratiosTopic = getTopic(profileId0, RATIOS_TOPIC_ID)
    assertThat(ratiosTopic.isCompleted()).isFalse()
  }

  @Test
  fun testMarkTopicDone_ratiosTopic_topicIsDone() {
    storyProgressTestHelper.markCompletedRatiosTopic(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val ratiosTopic = getTopic(profileId0, RATIOS_TOPIC_ID)
    assertThat(ratiosTopic.isCompleted()).isTrue()
  }

  /* Fractions chapter/story/topic completion tests. */

  @Test
  fun testMarkChapterDone_fractionsTopic_story0_exp0_chapterIsDone() {
    storyProgressTestHelper.markCompletedFractionsStory0Exp0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val fractionsTopic = getTopic(profileId0, FRACTIONS_TOPIC_ID)
    val story0 = fractionsTopic.getStory(FRACTIONS_STORY_ID_0)
    val exp0 = story0.getChapter(FRACTIONS_EXPLORATION_ID_0)
    assertThat(exp0.isCompleted()).isTrue()
  }

  @Test
  fun testMarkChapterDone_fractionsTopic_story0_exp0_story0IsNotDone() {
    storyProgressTestHelper.markCompletedFractionsStory0Exp0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val fractionsTopic = getTopic(profileId0, FRACTIONS_TOPIC_ID)
    val story0 = fractionsTopic.getStory(FRACTIONS_STORY_ID_0)
    assertThat(story0.isCompleted()).isFalse()
  }

  @Test
  fun testMarkChapterDone_fractionsTopic_story0_exp1_chapterIsDone() {
    storyProgressTestHelper.markCompletedFractionsStory0Exp1(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val fractionsTopic = getTopic(profileId0, FRACTIONS_TOPIC_ID)
    val story0 = fractionsTopic.getStory(FRACTIONS_STORY_ID_0)
    val exp1 = story0.getChapter(FRACTIONS_EXPLORATION_ID_1)
    assertThat(exp1.isCompleted()).isTrue()
  }

  @Test
  fun testMarkChapterDone_fractionsTopic_story0_exp1_story0IsDone() {
    storyProgressTestHelper.markCompletedFractionsStory0Exp1(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val fractionsTopic = getTopic(profileId0, FRACTIONS_TOPIC_ID)
    val story0 = fractionsTopic.getStory(FRACTIONS_STORY_ID_0)
    // The story is completed since exp 1 requires exp 0 to be finished first.
    assertThat(story0.isCompleted()).isTrue()
  }

  @Test
  fun testMarkStoryDone_fractionsTopic_story0_storyIsDone() {
    storyProgressTestHelper.markCompletedFractionsStory0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val fractionsTopic = getTopic(profileId0, FRACTIONS_TOPIC_ID)
    val story0 = fractionsTopic.getStory(FRACTIONS_STORY_ID_0)
    assertThat(story0.isCompleted()).isTrue()
  }

  @Test
  fun testMarkTopicDone_fractionsTopic_topicIsDone() {
    storyProgressTestHelper.markCompletedFractionsTopic(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val fractionsTopic = getTopic(profileId0, FRACTIONS_TOPIC_ID)
    assertThat(fractionsTopic.isCompleted()).isTrue()
  }

  /* Test topic chapter started tests. */

  @Test
  fun testMarkAsInProgressSaved_testTopic0_story0_exp2_chapterIsInProgressSaved() {
    storyProgressTestHelper.markInProgressSavedTestTopic0Story0Exp0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    val story0 = testTopic0.getStory(TEST_STORY_ID_0)
    val exp2 = story0.getChapter(TEST_EXPLORATION_ID_2)
    assertThat(exp2.isInProgressSaved()).isTrue()
  }

  @Test
  fun testMarkAsInProgressNotSaved_testTopic0_story0_exp2_chapterIsInProgressSaved() {
    storyProgressTestHelper.markInProgressNotSavedTestTopic0Story0Exp0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    val story0 = testTopic0.getStory(TEST_STORY_ID_0)
    val exp2 = story0.getChapter(TEST_EXPLORATION_ID_2)
    assertThat(exp2.isInProgressNotSaved()).isTrue()
  }

  @Test
  fun testMarkChapterAsInProgressSaved_testTopic0_story0_exp2_story0IsNotDone() {
    storyProgressTestHelper.markInProgressSavedTestTopic0Story0Exp0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    val story0 = testTopic0.getStory(TEST_STORY_ID_0)
    assertThat(story0.isCompleted()).isFalse()
  }

  @Test
  fun testMarkChapterAsInProgressNotSaved_testTopic0_story0_exp2_story0IsNotDone() {
    storyProgressTestHelper.markInProgressNotSavedTestTopic0Story0Exp0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    val story0 = testTopic0.getStory(TEST_STORY_ID_0)
    assertThat(story0.isCompleted()).isFalse()
  }

  @Test
  fun testMarkInProgressSaved_testTopic0_story0_exp13_chapterIsInProgressSaved() {
    storyProgressTestHelper.markInProgressSavedTestTopic0Story0Exp1(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    val story0 = testTopic0.getStory(TEST_STORY_ID_0)
    val exp13 = story0.getChapter(TEST_EXPLORATION_ID_13)
    assertThat(exp13.isInProgressSaved()).isTrue()
  }

  @Test
  fun testMarkChapterAsInProgressNotSaved_testTopic0_story0_exp13_chapterIsInProgressNotSaved() {
    storyProgressTestHelper.markInProgressNotSavedTestTopic0Story0Exp1(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    val story0 = testTopic0.getStory(TEST_STORY_ID_0)
    val exp13 = story0.getChapter(TEST_EXPLORATION_ID_13)
    assertThat(exp13.isInProgressNotSaved()).isTrue()
  }

  @Test
  fun testMarkInProgressSaved_testTopic0_story0_exp5_chapterIsInProgressSaved() {
    storyProgressTestHelper.markInProgressSavedTestTopic0Story0Exp2(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    val story0 = testTopic0.getStory(TEST_STORY_ID_0)
    val exp13 = story0.getChapter(TEST_EXPLORATION_ID_5)
    assertThat(exp13.isInProgressSaved()).isTrue()
  }

  @Test
  fun testMarkChapterAsInProgressNotSaved_testTopic0_story0_exp5_chapterIsInProgressNotSaved() {
    storyProgressTestHelper.markInProgressNotSavedTestTopic0Story0Exp2(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    val story0 = testTopic0.getStory(TEST_STORY_ID_0)
    val exp13 = story0.getChapter(TEST_EXPLORATION_ID_5)
    assertThat(exp13.isInProgressNotSaved()).isTrue()
  }

  @Test
  fun markInProgressSavedForTestTopic0Story0Exp13() {
    storyProgressTestHelper.markInProgressSavedTestTopic0Story0Exp0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    val story0 = testTopic0.getStory(TEST_STORY_ID_0)
    assertThat(story0.isCompleted()).isFalse()
  }

  @Test
  fun markInProgressNotSavedForTestTopic0Story0Exp13() {
    storyProgressTestHelper.markInProgressNotSavedTestTopic0Story0Exp0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    val story0 = testTopic0.getStory(TEST_STORY_ID_0)
    assertThat(story0.isCompleted()).isFalse()
  }

  @Test
  fun testMarkChapterAsInProgressSaved_testTopic1_story2_exp4_chapterIsInProgressSaved() {
    storyProgressTestHelper.markInProgressSavedTestTopic1Story2Exp0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic1 = getTopic(profileId0, TEST_TOPIC_ID_1)
    val story2 = testTopic1.getStory(TEST_STORY_ID_2)
    val exp4 = story2.getChapter(TEST_EXPLORATION_ID_4)
    assertThat(exp4.isInProgressSaved()).isTrue()
  }

  @Test
  fun testMarkChapterAsInProgressNotSaved_testTopic1_story2_exp4_chapterIsInProgressNotSaved() {
    storyProgressTestHelper.markInProgressNotSavedTestTopic1Story2Exp0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic1 = getTopic(profileId0, TEST_TOPIC_ID_1)
    val story2 = testTopic1.getStory(TEST_STORY_ID_2)
    val exp4 = story2.getChapter(TEST_EXPLORATION_ID_4)
    assertThat(exp4.isInProgressNotSaved()).isTrue()
  }

  @Test
  fun testMarkChapterAsInProgressSaved_testTopic1_story2_exp4_story2IsNotDone() {
    storyProgressTestHelper.markInProgressSavedTestTopic1Story2Exp0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic1 = getTopic(profileId0, TEST_TOPIC_ID_1)
    val story2 = testTopic1.getStory(TEST_STORY_ID_2)
    assertThat(story2.isCompleted()).isFalse()
  }

  @Test
  fun testMarkChapterAsInProgressNotSaved_testTopic1_story2_exp4_story2IsNotDone() {
    storyProgressTestHelper.markInProgressNotSavedTestTopic1Story2Exp0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic1 = getTopic(profileId0, TEST_TOPIC_ID_1)
    val story2 = testTopic1.getStory(TEST_STORY_ID_2)
    assertThat(story2.isCompleted()).isFalse()
  }

  /* Test topic/story started tests. */

  @Test
  fun testMarkStoryAsInProgressSaved_testTopic0_story0_storyIsInProgressSaved() {
    storyProgressTestHelper.markInProgressSavedTestTopic0Story0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    val story0 = testTopic0.getStory(TEST_STORY_ID_0)
    assertThat(story0.isInProgressSaved()).isTrue()
  }

  @Test
  fun testMarkStoryAsInProgressNotSaved_testTopic0_story0_storyIsInProgressNotSaved() {
    storyProgressTestHelper.markInProgressNotSavedTestTopic0Story0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    val story0 = testTopic0.getStory(TEST_STORY_ID_0)
    assertThat(story0.isInProgressNotSaved()).isTrue()
  }

  @Test
  fun testMarkStoryAsInProgressSaved_testTopic0_story0_topicIsNotDone() {
    storyProgressTestHelper.markInProgressSavedTestTopic0Story0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    assertThat(testTopic0.isCompleted()).isFalse()
  }

  @Test
  fun testMarkStoryAsInProgressNotSaved_testTopic0_story0_topicIsNotDone() {
    storyProgressTestHelper.markInProgressNotSavedTestTopic0Story0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    assertThat(testTopic0.isCompleted()).isFalse()
  }

  @Test
  fun testMarkStoryAsInProgressSaved_testTopic1_story2_storyIsInProgressSaved() {
    storyProgressTestHelper.markInProgressSavedTestTopic1Story0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic1 = getTopic(profileId0, TEST_TOPIC_ID_1)
    val story2 = testTopic1.getStory(TEST_STORY_ID_2)
    assertThat(story2.isInProgressSaved()).isTrue()
  }

  @Test
  fun testMarkStoryAsInProgressNotSaved_testTopic1_story2_storyIsInProgressNotSaved() {
    storyProgressTestHelper.markInProgressNotSavedTestTopic1Story0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic1 = getTopic(profileId0, TEST_TOPIC_ID_1)
    val story2 = testTopic1.getStory(TEST_STORY_ID_2)
    assertThat(story2.isInProgressNotSaved()).isTrue()
  }

  @Test
  fun testMarkStoryAsInProgressSaved_testTopic1_story2_topicIsNotDone() {
    storyProgressTestHelper.markInProgressSavedTestTopic1Story0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic1 = getTopic(profileId0, TEST_TOPIC_ID_1)
    assertThat(testTopic1.isCompleted()).isFalse()
  }

  @Test
  fun testMarkStoryAsInProgressNotSaved_testTopic1_story2_topicIsNotDone() {
    storyProgressTestHelper.markInProgressNotSavedTestTopic1Story0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic1 = getTopic(profileId0, TEST_TOPIC_ID_1)
    assertThat(testTopic1.isCompleted()).isFalse()
  }

  @Test
  fun testMarkTopicAsInProgressSaved_testTopic0_topicIsInProgressSaved() {
    storyProgressTestHelper.markInProgressSavedTestTopic0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    val testTopic1 = getTopic(profileId0, TEST_TOPIC_ID_1)
    assertThat(testTopic0.isInProgressSaved()).isTrue()
    assertThat(testTopic1.isInProgressSaved()).isFalse()
  }

  @Test
  fun testMarkTopicAsInProgressNotSaved_testTopic0_topicIsInProgressNotSaved() {
    storyProgressTestHelper.markInProgressNotSavedTestTopic0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    val testTopic1 = getTopic(profileId0, TEST_TOPIC_ID_1)
    assertThat(testTopic0.isInProgressNotSaved()).isTrue()
    assertThat(testTopic1.isInProgressNotSaved()).isFalse()
  }

  @Test
  fun testMarkTopicAsStartedNotCompleted_testTopic1_topicIsInProgressSaved() {
    storyProgressTestHelper.markInProgressSavedTestTopic1(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    val testTopic1 = getTopic(profileId0, TEST_TOPIC_ID_1)
    assertThat(testTopic0.isInProgressSaved()).isFalse()
    assertThat(testTopic1.isInProgressSaved()).isTrue()
  }

  @Test
  fun testMarkTopicAsInProgressNotSaved_testTopic1_topicIsInProgressNotSaved() {
    storyProgressTestHelper.markInProgressNotSavedTestTopic1(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    val testTopic1 = getTopic(profileId0, TEST_TOPIC_ID_1)
    assertThat(testTopic0.isInProgressNotSaved()).isFalse()
    assertThat(testTopic1.isInProgressNotSaved()).isTrue()
  }

  @Test
  fun testMarkTopicsAsInProgressSaved_testTopics_oneTopicIsInProgressSaved() {
    storyProgressTestHelper.markInProgressSavedTestTopics(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    val testTopic1 = getTopic(profileId0, TEST_TOPIC_ID_1)
    // At least one of the topics is started.
    assertThat(listOf(testTopic0, testTopic1).any { it.isInProgressSaved() }).isTrue()
    // But neither is completed.
    assertThat(testTopic0.isCompleted()).isFalse()
    assertThat(testTopic1.isCompleted()).isFalse()
  }

  @Test
  fun testMarkTopicsAsInProgressNotSaved_testTopics_oneTopicIsInProgressNotSaved() {
    storyProgressTestHelper.markInProgressNotSavedTestTopics(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    val testTopic1 = getTopic(profileId0, TEST_TOPIC_ID_1)
    // At least one of the topics is started.
    assertThat(listOf(testTopic0, testTopic1).any { it.isInProgressNotSaved() }).isTrue()
    // But neither is completed.
    assertThat(testTopic0.isCompleted()).isFalse()
    assertThat(testTopic1.isCompleted()).isFalse()
  }

  /* Ratios chapter started tests. */
  @Test
  fun testMarkChapterAsInProgressSaved_ratiosTopic_story0_exp0_chapterIsInProgressSaved() {
    storyProgressTestHelper.markInProgressSavedRatiosStory0Exp0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val ratiosTopic = getTopic(profileId0, RATIOS_TOPIC_ID)
    val story0 = ratiosTopic.getStory(RATIOS_STORY_ID_0)
    val exp0 = story0.getChapter(RATIOS_EXPLORATION_ID_0)
    assertThat(exp0.isInProgressSaved()).isTrue()
  }

  @Test
  fun testMarkChapterAsInProgressNotSaved_ratiosTopic_story0_exp0_chapterIsInProgressNotSaved() {
    storyProgressTestHelper.markInProgressNotSavedRatiosStory0Exp0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val ratiosTopic = getTopic(profileId0, RATIOS_TOPIC_ID)
    val story0 = ratiosTopic.getStory(RATIOS_STORY_ID_0)
    val exp0 = story0.getChapter(RATIOS_EXPLORATION_ID_0)
    assertThat(exp0.isInProgressNotSaved()).isTrue()
  }

  @Test
  fun testMarkChapterAsInProgressSaved_ratiosTopic_story0_exp0_story0IsNotDone() {
    storyProgressTestHelper.markInProgressSavedRatiosStory0Exp0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val ratiosTopic = getTopic(profileId0, RATIOS_TOPIC_ID)
    val story0 = ratiosTopic.getStory(RATIOS_STORY_ID_0)
    assertThat(story0.isCompleted()).isFalse()
  }

  @Test
  fun testMarkChapterAsInProgressNotSaved_ratiosTopic_story0_exp0_story0IsNotDone() {
    storyProgressTestHelper.markInProgressNotSavedRatiosStory0Exp0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val ratiosTopic = getTopic(profileId0, RATIOS_TOPIC_ID)
    val story0 = ratiosTopic.getStory(RATIOS_STORY_ID_0)
    assertThat(story0.isCompleted()).isFalse()
  }

  @Test
  fun testMarkChapterAsInProgressSaved_ratiosTopic_story0_exp1_chapterIsInProgressSaved() {
    storyProgressTestHelper.markInProgressSavedRatiosStory0Exp1(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val ratiosTopic = getTopic(profileId0, RATIOS_TOPIC_ID)
    val story0 = ratiosTopic.getStory(RATIOS_STORY_ID_0)
    val exp1 = story0.getChapter(RATIOS_EXPLORATION_ID_1)
    assertThat(exp1.isInProgressSaved()).isTrue()
  }

  @Test
  fun testMarkChapterAsInProgressNotSaved_ratiosTopic_story0_exp1_chapterIsInProgressNotSaved() {
    storyProgressTestHelper.markInProgressNotSavedRatiosStory0Exp1(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val ratiosTopic = getTopic(profileId0, RATIOS_TOPIC_ID)
    val story0 = ratiosTopic.getStory(RATIOS_STORY_ID_0)
    val exp1 = story0.getChapter(RATIOS_EXPLORATION_ID_1)
    assertThat(exp1.isInProgressNotSaved()).isTrue()
  }

  @Test
  fun testMarkChapterAsInProgressSaved_ratiosTopic_story0_exp1_story0IsNotDone() {
    storyProgressTestHelper.markInProgressSavedRatiosStory0Exp1(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val ratiosTopic = getTopic(profileId0, RATIOS_TOPIC_ID)
    val story0 = ratiosTopic.getStory(RATIOS_STORY_ID_0)
    assertThat(story0.isCompleted()).isFalse()
  }

  @Test
  fun testMarkChapterAsInProgressNotSaved_ratiosTopic_story0_exp1_story0IsNotDone() {
    storyProgressTestHelper.markInProgressNotSavedRatiosStory0Exp1(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val ratiosTopic = getTopic(profileId0, RATIOS_TOPIC_ID)
    val story0 = ratiosTopic.getStory(RATIOS_STORY_ID_0)
    assertThat(story0.isCompleted()).isFalse()
  }

  @Test
  fun testMarkChapterAsInProgressSaved_ratiosTopic_story1_exp2_chapterIsInProgressSaved() {
    storyProgressTestHelper.markInProgressSavedRatiosStory1Exp0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val ratiosTopic = getTopic(profileId0, RATIOS_TOPIC_ID)
    val story1 = ratiosTopic.getStory(RATIOS_STORY_ID_1)
    val exp2 = story1.getChapter(RATIOS_EXPLORATION_ID_2)
    assertThat(exp2.isInProgressSaved()).isTrue()
  }

  @Test
  fun testMarkChapterAsInProgressNotSaved_ratiosTopic_story1_exp2_chapterIsInProgressNotSaved() {
    storyProgressTestHelper.markInProgressNotSavedRatiosStory1Exp0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val ratiosTopic = getTopic(profileId0, RATIOS_TOPIC_ID)
    val story1 = ratiosTopic.getStory(RATIOS_STORY_ID_1)
    val exp2 = story1.getChapter(RATIOS_EXPLORATION_ID_2)
    assertThat(exp2.isInProgressNotSaved()).isTrue()
  }

  @Test
  fun testMarkChapterAsInProgressSaved_ratiosTopic_story1_exp2_story1IsNotDone() {
    storyProgressTestHelper.markInProgressSavedRatiosStory1Exp0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val ratiosTopic = getTopic(profileId0, RATIOS_TOPIC_ID)
    val story1 = ratiosTopic.getStory(RATIOS_STORY_ID_1)
    assertThat(story1.isCompleted()).isFalse()
  }

  @Test
  fun testMarkChapterAsInProgressNotSaved_ratiosTopic_story1_exp2_story1IsNotDone() {
    storyProgressTestHelper.markInProgressNotSavedRatiosStory1Exp0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val ratiosTopic = getTopic(profileId0, RATIOS_TOPIC_ID)
    val story1 = ratiosTopic.getStory(RATIOS_STORY_ID_1)
    assertThat(story1.isCompleted()).isFalse()
  }

  @Test
  fun testMarkChapterAsInProgressSaved_ratiosTopic_story1_exp3_chapterIsInProgressSaved() {
    storyProgressTestHelper.markInProgressSavedRatiosStory1Exp1(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val ratiosTopic = getTopic(profileId0, RATIOS_TOPIC_ID)
    val story1 = ratiosTopic.getStory(RATIOS_STORY_ID_1)
    val exp3 = story1.getChapter(RATIOS_EXPLORATION_ID_3)
    assertThat(exp3.isInProgressSaved()).isTrue()
  }

  @Test
  fun testMarkChapterAsInProgressNotSaved_ratiosTopic_story1_exp3_chapterIsInProgressNotSaved() {
    storyProgressTestHelper.markInProgressNotSavedRatiosStory1Exp1(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val ratiosTopic = getTopic(profileId0, RATIOS_TOPIC_ID)
    val story1 = ratiosTopic.getStory(RATIOS_STORY_ID_1)
    val exp3 = story1.getChapter(RATIOS_EXPLORATION_ID_3)
    assertThat(exp3.isInProgressNotSaved()).isTrue()
  }

  @Test
  fun testMarkChapterAsInProgressSaved_ratiosTopic_story1_exp3_story1IsNotDone() {
    storyProgressTestHelper.markInProgressSavedRatiosStory1Exp1(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val ratiosTopic = getTopic(profileId0, RATIOS_TOPIC_ID)
    val story1 = ratiosTopic.getStory(RATIOS_STORY_ID_1)
    assertThat(story1.isCompleted()).isFalse()
  }

  @Test
  fun testMarkChapterAsInProgressNotSaved_ratiosTopic_story1_exp3_story1IsNotDone() {
    storyProgressTestHelper.markInProgressNotSavedRatiosStory1Exp1(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val ratiosTopic = getTopic(profileId0, RATIOS_TOPIC_ID)
    val story1 = ratiosTopic.getStory(RATIOS_STORY_ID_1)
    assertThat(story1.isCompleted()).isFalse()
  }

  /* Ratios topic/story started tests. */
  @Test
  fun testMarkStoryAsInProgressSaved_ratiosTopic_story0_storyIsInProgressSaved() {
    storyProgressTestHelper.markInProgressSavedRatiosStory0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val ratiosTopic = getTopic(profileId0, RATIOS_TOPIC_ID)
    val story0 = ratiosTopic.getStory(RATIOS_STORY_ID_0)
    val story1 = ratiosTopic.getStory(RATIOS_STORY_ID_1)
    assertThat(story0.isInProgressSaved()).isTrue()
    assertThat(story1.isInProgressSaved()).isFalse()
  }

  @Test
  fun testMarkStoryAsInProgressNotSaved_ratiosTopic_story0_storyIsInProgressNotSaved() {
    storyProgressTestHelper.markInProgressNotSavedRatiosStory0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val ratiosTopic = getTopic(profileId0, RATIOS_TOPIC_ID)
    val story0 = ratiosTopic.getStory(RATIOS_STORY_ID_0)
    val story1 = ratiosTopic.getStory(RATIOS_STORY_ID_1)
    assertThat(story0.isInProgressNotSaved()).isTrue()
    assertThat(story1.isInProgressNotSaved()).isFalse()
  }

  @Test
  fun testMarkStoryAsInProgressSaved_ratiosTopic_story0_topicIsNotDone() {
    storyProgressTestHelper.markInProgressSavedRatiosStory0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val ratiosTopic = getTopic(profileId0, RATIOS_TOPIC_ID)
    assertThat(ratiosTopic.isCompleted()).isFalse()
  }

  @Test
  fun testMarkStoryAsInProgressNotSaved_ratiosTopic_story0_topicIsNotDone() {
    storyProgressTestHelper.markInProgressNotSavedRatiosStory0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val ratiosTopic = getTopic(profileId0, RATIOS_TOPIC_ID)
    assertThat(ratiosTopic.isCompleted()).isFalse()
  }

  @Test
  fun testMarkStoryAsInProgressSaved_ratiosTopic_story1_storyIsInProgressSaved() {
    storyProgressTestHelper.markInProgressSavedRatiosStory1(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val ratiosTopic = getTopic(profileId0, RATIOS_TOPIC_ID)
    val story0 = ratiosTopic.getStory(RATIOS_STORY_ID_0)
    val story1 = ratiosTopic.getStory(RATIOS_STORY_ID_1)
    assertThat(story0.isInProgressSaved()).isFalse()
    assertThat(story1.isInProgressSaved()).isTrue()
  }

  @Test
  fun testMarkStoryAsInProgressNotSaved_ratiosTopic_story1_storyIsInProgressNotSaved() {
    storyProgressTestHelper.markInProgressNotSavedRatiosStory1(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val ratiosTopic = getTopic(profileId0, RATIOS_TOPIC_ID)
    val story0 = ratiosTopic.getStory(RATIOS_STORY_ID_0)
    val story1 = ratiosTopic.getStory(RATIOS_STORY_ID_1)
    assertThat(story0.isInProgressNotSaved()).isFalse()
    assertThat(story1.isInProgressNotSaved()).isTrue()
  }

  @Test
  fun testMarkStoryAsInProgressSaved_ratiosTopic_story1_topicIsNotDone() {
    storyProgressTestHelper.markInProgressSavedRatiosStory1(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val ratiosTopic = getTopic(profileId0, RATIOS_TOPIC_ID)
    assertThat(ratiosTopic.isCompleted()).isFalse()
  }

  @Test
  fun testMarkStoryAsInProgressNotSaved_ratiosTopic_story1_topicIsNotDone() {
    storyProgressTestHelper.markInProgressNotSavedRatiosStory1(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val ratiosTopic = getTopic(profileId0, RATIOS_TOPIC_ID)
    assertThat(ratiosTopic.isCompleted()).isFalse()
  }

  @Test
  fun testMarkTopicAsInProgressSaved_ratiosTopic_topicIsInProgressSaved() {
    storyProgressTestHelper.markInProgressSavedRatios(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val ratiosTopic = getTopic(profileId0, RATIOS_TOPIC_ID)
    assertThat(ratiosTopic.isInProgressSaved()).isTrue()
  }

  @Test
  fun testMarkTopicAsInProgressNotSaved_ratiosTopic_topicIsInProgressNotSaved() {
    storyProgressTestHelper.markInProgressNotSavedRatios(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val ratiosTopic = getTopic(profileId0, RATIOS_TOPIC_ID)
    assertThat(ratiosTopic.isInProgressNotSaved()).isTrue()
  }

  /* Fractions topic/story/chapter started tests. */

  @Test
  fun testMarkChapterAsInProgressSaved_fractionsTopic_story0_exp0_chapterIsInProgressSaved() {
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val fractionsTopic = getTopic(profileId0, FRACTIONS_TOPIC_ID)
    val story0 = fractionsTopic.getStory(FRACTIONS_STORY_ID_0)
    val exp0 = story0.getChapter(FRACTIONS_EXPLORATION_ID_0)
    assertThat(exp0.isInProgressSaved()).isTrue()
  }

  @Test
  fun testMarkChapterAsInProgressNotSaved_fractionsTopic_story0_exp0_chapterIsInProgressNotSaved() {
    storyProgressTestHelper.markInProgressNotSavedFractionsStory0Exp0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val fractionsTopic = getTopic(profileId0, FRACTIONS_TOPIC_ID)
    val story0 = fractionsTopic.getStory(FRACTIONS_STORY_ID_0)
    val exp0 = story0.getChapter(FRACTIONS_EXPLORATION_ID_0)
    assertThat(exp0.isInProgressNotSaved()).isTrue()
  }

  @Test
  fun testMarkChapterAsInProgressSaved_fractionsTopic_story0_exp0_story0IsNotDone() {
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val fractionsTopic = getTopic(profileId0, FRACTIONS_TOPIC_ID)
    val story0 = fractionsTopic.getStory(FRACTIONS_STORY_ID_0)
    assertThat(story0.isCompleted()).isFalse()
  }

  @Test
  fun testMarkChapterAsInProgressNotSaved_fractionsTopic_story0_exp0_story0IsNotDone() {
    storyProgressTestHelper.markInProgressNotSavedFractionsStory0Exp0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val fractionsTopic = getTopic(profileId0, FRACTIONS_TOPIC_ID)
    val story0 = fractionsTopic.getStory(FRACTIONS_STORY_ID_0)
    assertThat(story0.isCompleted()).isFalse()
  }

  @Test
  fun testMarkChapterAsInProgressSaved_fractionsTopic_story0_exp1_chapterIsInProgressSaved() {
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp1(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val fractionsTopic = getTopic(profileId0, FRACTIONS_TOPIC_ID)
    val story0 = fractionsTopic.getStory(FRACTIONS_STORY_ID_0)
    val exp1 = story0.getChapter(FRACTIONS_EXPLORATION_ID_1)
    assertThat(exp1.isInProgressSaved()).isTrue()
  }

  @Test
  fun testMarkChapterAsInProgressNotSaved_fractionsTopic_story0_exp1_chapterIsInProgressNotSaved() {
    storyProgressTestHelper.markInProgressNotSavedFractionsStory0Exp1(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val fractionsTopic = getTopic(profileId0, FRACTIONS_TOPIC_ID)
    val story0 = fractionsTopic.getStory(FRACTIONS_STORY_ID_0)
    val exp1 = story0.getChapter(FRACTIONS_EXPLORATION_ID_1)
    assertThat(exp1.isInProgressNotSaved()).isTrue()
  }

  @Test
  fun testMarkChapterAsInProgressSaved_fractionsTopic_story0_exp1_story0IsNotDone() {
    storyProgressTestHelper.markInProgressSavedFractionsStory0Exp1(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val fractionsTopic = getTopic(profileId0, FRACTIONS_TOPIC_ID)
    val story0 = fractionsTopic.getStory(FRACTIONS_STORY_ID_0)
    assertThat(story0.isCompleted()).isFalse()
  }

  @Test
  fun testMarkChapterAsInProgressNotSaved_fractionsTopic_story0_exp1_story0IsNotDone() {
    storyProgressTestHelper.markInProgressNotSavedFractionsStory0Exp1(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val fractionsTopic = getTopic(profileId0, FRACTIONS_TOPIC_ID)
    val story0 = fractionsTopic.getStory(FRACTIONS_STORY_ID_0)
    assertThat(story0.isCompleted()).isFalse()
  }

  @Test
  fun testMarkStoryAsInProgressSaved_fractionsTopic_story0_storyIsInProgressSaved() {
    storyProgressTestHelper.markInProgressSavedFractionsStory0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val fractionsTopic = getTopic(profileId0, FRACTIONS_TOPIC_ID)
    val story0 = fractionsTopic.getStory(FRACTIONS_STORY_ID_0)
    assertThat(story0.isInProgressSaved()).isTrue()
  }

  @Test
  fun testMarkStoryAsInProgressNotSaved_fractionsTopic_story0_storyIsInProgressNotSaved() {
    storyProgressTestHelper.markInProgressNotSavedFractionsStory0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val fractionsTopic = getTopic(profileId0, FRACTIONS_TOPIC_ID)
    val story0 = fractionsTopic.getStory(FRACTIONS_STORY_ID_0)
    assertThat(story0.isInProgressNotSaved()).isTrue()
  }

  @Test
  fun testMarkStoryAsInProgressSaved_fractionsTopic_story0_topicIsInProgressSaved() {
    storyProgressTestHelper.markInProgressSavedFractionsStory0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val fractionsTopic = getTopic(profileId0, FRACTIONS_TOPIC_ID)
    assertThat(fractionsTopic.isInProgressSaved()).isTrue()
  }

  @Test
  fun testMarkStoryAsInProgressNotSaved_fractionsTopic_story0_topicIsInProgressNotSaved() {
    storyProgressTestHelper.markInProgressNotSavedFractionsStory0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val fractionsTopic = getTopic(profileId0, FRACTIONS_TOPIC_ID)
    assertThat(fractionsTopic.isInProgressNotSaved()).isTrue()
  }

  @Test
  fun testMarkTopicAsInProgressSaved_fractionsTopic_topicIsInProgressSaved() {
    storyProgressTestHelper.markInProgressSavedFractions(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val fractionsTopic = getTopic(profileId0, FRACTIONS_TOPIC_ID)
    assertThat(fractionsTopic.isInProgressSaved()).isTrue()
  }

  @Test
  fun testMarkTopicAsInProgressNotSaved_fractionsTopic_topicIsInProgressNotSaved() {
    storyProgressTestHelper.markInProgressNotSavedFractions(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val fractionsTopic = getTopic(profileId0, FRACTIONS_TOPIC_ID)
    assertThat(fractionsTopic.isInProgressNotSaved()).isTrue()
  }

  /* Specific state & cross-topic tests. */

  @Test
  fun testInitialState_allTopicsAreNotStarted() {
    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    val testTopic1 = getTopic(profileId0, TEST_TOPIC_ID_1)
    val ratiosTopic = getTopic(profileId0, RATIOS_TOPIC_ID)
    val fractionsTopic = getTopic(profileId0, FRACTIONS_TOPIC_ID)

    assertThat(testTopic0.isNotStarted()).isTrue()
    assertThat(testTopic1.isNotStarted()).isTrue()
    assertThat(ratiosTopic.isNotStarted()).isTrue()
    assertThat(fractionsTopic.isNotStarted()).isTrue()
  }

  @Test
  fun testMarkAllTopicsAsInProgressSaved_allTopicsAreInProgressSaved() {
    storyProgressTestHelper.markAllTopicsAsInProgressSaved(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    val testTopic1 = getTopic(profileId0, TEST_TOPIC_ID_1)
    val ratiosTopic = getTopic(profileId0, RATIOS_TOPIC_ID)
    val fractionsTopic = getTopic(profileId0, FRACTIONS_TOPIC_ID)

    assertThat(testTopic0.isInProgressSaved()).isTrue()
    assertThat(testTopic1.isInProgressSaved()).isTrue()
    assertThat(ratiosTopic.isInProgressSaved()).isTrue()
    assertThat(fractionsTopic.isInProgressSaved()).isTrue()
  }

  @Test
  fun testMarkAllTopicsAsInProgressNotSaved_allTopicsAreInProgressNotSaved() {
    storyProgressTestHelper.markAllTopicsAsInProgressNotSaved(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    val testTopic1 = getTopic(profileId0, TEST_TOPIC_ID_1)
    val ratiosTopic = getTopic(profileId0, RATIOS_TOPIC_ID)
    val fractionsTopic = getTopic(profileId0, FRACTIONS_TOPIC_ID)

    assertThat(testTopic0.isInProgressNotSaved()).isTrue()
    assertThat(testTopic1.isInProgressNotSaved()).isTrue()
    assertThat(ratiosTopic.isInProgressNotSaved()).isTrue()
    assertThat(fractionsTopic.isInProgressNotSaved()).isTrue()
  }

  @Test
  fun testMarkAllTopicsDone_allTopicsAreDone() {
    storyProgressTestHelper.markAllTopicsAsCompleted(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    val testTopic1 = getTopic(profileId0, TEST_TOPIC_ID_1)
    val ratiosTopic = getTopic(profileId0, RATIOS_TOPIC_ID)
    val fractionsTopic = getTopic(profileId0, FRACTIONS_TOPIC_ID)

    assertThat(testTopic0.isCompleted()).isTrue()
    assertThat(testTopic1.isCompleted()).isTrue()
    assertThat(ratiosTopic.isCompleted()).isTrue()
    assertThat(fractionsTopic.isCompleted()).isTrue()
  }

  @Test
  fun testInProgressSavedChapter_thenMarkedDone_chapterIsCompleted() {
    storyProgressTestHelper.markInProgressSavedTestTopic0Story0Exp0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markCompletedTestTopic0Story0Exp0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    val story0 = testTopic0.getStory(TEST_STORY_ID_0)
    val exp2 = story0.getChapter(TEST_EXPLORATION_ID_2)
    assertThat(exp2.isCompleted()).isTrue()
  }

  @Test
  fun testInProgressNotSavedChapter_thenMarkedDone_chapterIsCompleted() {
    storyProgressTestHelper.markInProgressNotSavedTestTopic0Story0Exp0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markCompletedTestTopic0Story0Exp0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    val story0 = testTopic0.getStory(TEST_STORY_ID_0)
    val exp2 = story0.getChapter(TEST_EXPLORATION_ID_2)
    assertThat(exp2.isCompleted()).isTrue()
  }

  @Test
  fun testMarkChapterDone_thenInProgressSaved_chapterIsCompleted() {
    storyProgressTestHelper.markCompletedTestTopic0Story0Exp0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markInProgressSavedTestTopic0Story0Exp0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    val story0 = testTopic0.getStory(TEST_STORY_ID_0)
    val exp2 = story0.getChapter(TEST_EXPLORATION_ID_2)
    assertThat(exp2.isCompleted()).isTrue()
  }

  @Test
  fun testMarkChapterDone_thenInProgressNotSaved_chapterIsCompleted() {
    storyProgressTestHelper.markCompletedTestTopic0Story0Exp0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markInProgressNotSavedTestTopic0Story0Exp0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    val story0 = testTopic0.getStory(TEST_STORY_ID_0)
    val exp2 = story0.getChapter(TEST_EXPLORATION_ID_2)
    assertThat(exp2.isCompleted()).isTrue()
  }

  @Test
  fun testMarkChapterDone_newerThanWeek_timestampNewerThanWeek() {
    storyProgressTestHelper.markCompletedTestTopic0Story0Exp0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val progressDatabase = getTopicProgressDatabase(profileId0)
    val testTopic0Progress = progressDatabase.getTopicProgress(TEST_TOPIC_ID_0)
    val story0Progress = testTopic0Progress.getStoryProgress(TEST_STORY_ID_0)
    val exp2Progress = story0Progress.getChapterProgress(TEST_EXPLORATION_ID_2)

    val currentTime = fakeOppiaClock.getCurrentTimeMs()
    val timeSinceFinished = currentTime - exp2Progress.lastPlayedTimestamp
    assertThat(timeSinceFinished).isAtMost(TimeUnit.DAYS.toMillis(7))
  }

  @Test
  fun testMarkChapterDone_olderThanWeek_timestampOlderThanWeek() {
    storyProgressTestHelper.markCompletedTestTopic0Story0Exp0(
      profileId = profileId0,
      timestampOlderThanOneWeek = true
    )

    val progressDatabase = getTopicProgressDatabase(profileId0)
    val testTopic0Progress = progressDatabase.getTopicProgress(TEST_TOPIC_ID_0)
    val story0Progress = testTopic0Progress.getStoryProgress(TEST_STORY_ID_0)
    val exp2Progress = story0Progress.getChapterProgress(TEST_EXPLORATION_ID_2)

    val currentTime = fakeOppiaClock.getCurrentTimeMs()
    val timeSinceFinished = currentTime - exp2Progress.lastPlayedTimestamp
    assertThat(timeSinceFinished).isAtLeast(TimeUnit.DAYS.toMillis(7))
  }

  @Test
  fun testMarkChapterAsInProgressSaved_newerThanWeek_timestampNewerThanWeek() {
    storyProgressTestHelper.markInProgressSavedTestTopic0Story0Exp0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val progressDatabase = getTopicProgressDatabase(profileId0)
    val testTopic0Progress = progressDatabase.getTopicProgress(TEST_TOPIC_ID_0)
    val story0Progress = testTopic0Progress.getStoryProgress(TEST_STORY_ID_0)
    val exp2Progress = story0Progress.getChapterProgress(TEST_EXPLORATION_ID_2)

    val currentTime = fakeOppiaClock.getCurrentTimeMs()
    val timeSincePlayed = currentTime - exp2Progress.lastPlayedTimestamp
    assertThat(timeSincePlayed).isAtMost(TimeUnit.DAYS.toMillis(7))
  }

  @Test
  fun testMarkChapterAsInProgressNotSaved_newerThanWeek_timestampNewerThanWeek() {
    storyProgressTestHelper.markInProgressNotSavedTestTopic0Story0Exp0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val progressDatabase = getTopicProgressDatabase(profileId0)
    val testTopic0Progress = progressDatabase.getTopicProgress(TEST_TOPIC_ID_0)
    val story0Progress = testTopic0Progress.getStoryProgress(TEST_STORY_ID_0)
    val exp2Progress = story0Progress.getChapterProgress(TEST_EXPLORATION_ID_2)

    val currentTime = fakeOppiaClock.getCurrentTimeMs()
    val timeSincePlayed = currentTime - exp2Progress.lastPlayedTimestamp
    assertThat(timeSincePlayed).isAtMost(TimeUnit.DAYS.toMillis(7))
  }

  @Test
  fun testMarkChapterInProgressSaved_olderThanWeek_timestampOlderThanWeek() {
    storyProgressTestHelper.markInProgressSavedTestTopic0Story0Exp0(
      profileId = profileId0,
      timestampOlderThanOneWeek = true
    )

    val progressDatabase = getTopicProgressDatabase(profileId0)
    val testTopic0Progress = progressDatabase.getTopicProgress(TEST_TOPIC_ID_0)
    val story0Progress = testTopic0Progress.getStoryProgress(TEST_STORY_ID_0)
    val exp2Progress = story0Progress.getChapterProgress(TEST_EXPLORATION_ID_2)

    val currentTime = fakeOppiaClock.getCurrentTimeMs()
    val timeSincePlayed = currentTime - exp2Progress.lastPlayedTimestamp
    assertThat(timeSincePlayed).isAtLeast(TimeUnit.DAYS.toMillis(7))
  }

  @Test
  fun testMarkChapterInProgressNotSaved_olderThanWeek_timestampOlderThanWeek() {
    storyProgressTestHelper.markInProgressNotSavedTestTopic0Story0Exp0(
      profileId = profileId0,
      timestampOlderThanOneWeek = true
    )

    val progressDatabase = getTopicProgressDatabase(profileId0)
    val testTopic0Progress = progressDatabase.getTopicProgress(TEST_TOPIC_ID_0)
    val story0Progress = testTopic0Progress.getStoryProgress(TEST_STORY_ID_0)
    val exp2Progress = story0Progress.getChapterProgress(TEST_EXPLORATION_ID_2)

    val currentTime = fakeOppiaClock.getCurrentTimeMs()
    val timeSincePlayed = currentTime - exp2Progress.lastPlayedTimestamp
    assertThat(timeSincePlayed).isAtLeast(TimeUnit.DAYS.toMillis(7))
  }

  @Test
  fun testMarkChapterComplete_onOneProfile_notCompletedOnOtherProfile() {
    storyProgressTestHelper.markCompletedTestTopic0Story0Exp0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId1, TEST_TOPIC_ID_0)
    val story0 = testTopic0.getStory(TEST_STORY_ID_0)
    val exp2 = story0.getChapter(TEST_EXPLORATION_ID_2)
    assertThat(exp2.isCompleted()).isFalse()
  }

  @Test
  fun testMarkChapterAsInProgressSaved_onOneProfile_notStartedOnOtherProfile() {
    storyProgressTestHelper.markInProgressSavedTestTopic0Story0Exp0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId1, TEST_TOPIC_ID_0)
    val story0 = testTopic0.getStory(TEST_STORY_ID_0)
    val exp2 = story0.getChapter(TEST_EXPLORATION_ID_2)
    assertThat(exp2.isStartedNotCompleted()).isFalse()
  }

  @Test
  fun testMarkChapterAsInProgressNotSaved_onOneProfile_notStartedOnOtherProfile() {
    storyProgressTestHelper.markInProgressNotSavedTestTopic0Story0Exp0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId1, TEST_TOPIC_ID_0)
    val story0 = testTopic0.getStory(TEST_STORY_ID_0)
    val exp2 = story0.getChapter(TEST_EXPLORATION_ID_2)
    assertThat(exp2.isStartedNotCompleted()).isFalse()
  }

  private fun getTopic(profileId: ProfileId, topicId: String): Topic =
    monitorFactory.waitForNextSuccessfulResult(topicController.getTopic(profileId, topicId)).topic

  private fun Topic.getStory(storyId: String): StorySummary {
    return storyList.find { it.storyId == storyId } ?: error("Failed to find story: $storyId")
  }

  private fun Topic.isNotStarted(): Boolean = storyList.all { it.isNotStarted() }

  private fun Topic.isInProgressSaved(): Boolean = storyList.any { it.isInProgressSaved() }

  private fun Topic.isInProgressNotSaved(): Boolean = storyList.any { it.isInProgressNotSaved() }

  private fun Topic.isPartiallyCompleted(): Boolean = storyList.any { it.isCompleted() }

  private fun Topic.isCompleted(): Boolean = storyList.all { it.isCompleted() }

  private fun StorySummary.getChapter(expId: String): ChapterSummary {
    return chapterList.find { it.explorationId == expId } ?: error("Failed to find chapter: $expId")
  }

  private fun StorySummary.isNotStarted(): Boolean = chapterList.all { it.isNotStarted() }

  private fun StorySummary.isInProgressSaved(): Boolean = chapterList.any { it.isInProgressSaved() }

  private fun StorySummary.isInProgressNotSaved(): Boolean =
    chapterList.any { it.isInProgressNotSaved() }

  private fun StorySummary.isCompleted(): Boolean = chapterList.all { it.isCompleted() }

  private fun ChapterSummary.isNotStarted(): Boolean =
    chapterPlayState in listOf(
      ChapterPlayState.NOT_STARTED, ChapterPlayState.NOT_PLAYABLE_MISSING_PREREQUISITES
    )

  private fun ChapterSummary.isStartedNotCompleted(): Boolean =
    chapterPlayState == ChapterPlayState.STARTED_NOT_COMPLETED

  private fun ChapterSummary.isInProgressSaved(): Boolean =
    chapterPlayState == ChapterPlayState.IN_PROGRESS_SAVED

  private fun ChapterSummary.isInProgressNotSaved(): Boolean =
    chapterPlayState == ChapterPlayState.IN_PROGRESS_NOT_SAVED

  private fun ChapterSummary.isCompleted(): Boolean = chapterPlayState == ChapterPlayState.COMPLETED

  private fun getTopicProgressDatabase(profileId: ProfileId): TopicProgressDatabase {
    // Hacky way to retrieve the current progress database.
    val persistentCacheStore =
      persistentCacheStoreFactory.createPerProfile(
        "topic_progress_database",
        TopicProgressDatabase.getDefaultInstance(),
        profileId
      )
    return monitorFactory.waitForNextSuccessfulResult(persistentCacheStore)
  }

  private fun TopicProgressDatabase.getTopicProgress(topicId: String): TopicProgress {
    return topicProgressMap[topicId] ?: error("Failed to get progress for topic: $topicId")
  }

  private fun TopicProgress.getStoryProgress(storyId: String): StoryProgress {
    return storyProgressMap[storyId] ?: error("Failed to get progress for story: $storyId")
  }

  private fun StoryProgress.getChapterProgress(expId: String): ChapterProgress {
    return chapterProgressMap[expId] ?: error("Failed to get progress for chapter: $expId")
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
      ImageParsingModule::class, LoggerModule::class, NetworkConnectionUtilDebugModule::class,
      AssetModule::class, LocaleProdModule::class, LoggingIdentifierModule::class,
      ApplicationLifecycleModule::class, SyncStatusModule::class,
      PlatformParameterModule::class, PlatformParameterSingletonModule::class
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
