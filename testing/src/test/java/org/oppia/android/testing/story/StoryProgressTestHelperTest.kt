package org.oppia.android.testing.story

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
import org.mockito.Mockito.reset
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
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
import org.oppia.android.domain.topic.TEST_EXPLORATION_ID_0
import org.oppia.android.domain.topic.TEST_EXPLORATION_ID_1
import org.oppia.android.domain.topic.TEST_EXPLORATION_ID_2
import org.oppia.android.domain.topic.TEST_EXPLORATION_ID_3
import org.oppia.android.domain.topic.TEST_EXPLORATION_ID_4
import org.oppia.android.domain.topic.TEST_EXPLORATION_ID_5
import org.oppia.android.domain.topic.TEST_STORY_ID_0
import org.oppia.android.domain.topic.TEST_STORY_ID_1
import org.oppia.android.domain.topic.TEST_STORY_ID_2
import org.oppia.android.domain.topic.TEST_TOPIC_ID_0
import org.oppia.android.domain.topic.TEST_TOPIC_ID_1
import org.oppia.android.domain.topic.TopicController
import org.oppia.android.testing.RobolectricModule
import org.oppia.android.testing.TestCoroutineDispatchers
import org.oppia.android.testing.TestDispatcherModule
import org.oppia.android.testing.TestLogReportingModule
import org.oppia.android.testing.time.FakeOppiaClock
import org.oppia.android.testing.time.FakeOppiaClockModule
import org.oppia.android.util.caching.testing.CachingTestModule
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.parser.ImageParsingModule
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.util.concurrent.TimeUnit
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
  lateinit var persistentCacheStoreFactory: PersistentCacheStore.Factory

  @Inject
  lateinit var testCoroutineDispatchers: TestCoroutineDispatchers

  @Inject
  lateinit var fakeOppiaClock: FakeOppiaClock

  @Mock
  lateinit var mockTopicObserver: Observer<AsyncResult<Topic>>

  @Captor
  lateinit var topicResultCaptor: ArgumentCaptor<AsyncResult<Topic>>

  @Mock
  lateinit var mockTopicProgressDatabaseObserver: Observer<AsyncResult<TopicProgressDatabase>>

  @Captor
  lateinit var topicProgressDatabaseResultCaptor: ArgumentCaptor<AsyncResult<TopicProgressDatabase>>

  private val profileId0: ProfileId by lazy { ProfileId.newBuilder().setInternalId(0).build() }
  private val profileId1: ProfileId by lazy { ProfileId.newBuilder().setInternalId(1).build() }

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
  fun testMarkChapterDone_testTopic0_story0_exp5_chapterIsDone() {
    storyProgressTestHelper.markCompletedTestTopic0Story0Exp1(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    val story0 = testTopic0.getStory(TEST_STORY_ID_0)
    val exp5 = story0.getChapter(TEST_EXPLORATION_ID_5)
    assertThat(exp5.isCompleted()).isTrue()
  }

  @Test
  fun testMarkChapterDone_testTopic0_story0_exp5_story0IsDone() {
    storyProgressTestHelper.markCompletedTestTopic0Story0Exp1(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    val story0 = testTopic0.getStory(TEST_STORY_ID_0)
    // The story is completed since exp 5 requires exp 2 to be finished first.
    assertThat(story0.isCompleted()).isTrue()
  }

  @Test
  fun testMarkChapterDone_testTopic0_story1_exp1_chapterIsDone() {
    storyProgressTestHelper.markCompletedTestTopic0Story1Exp0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    val story1 = testTopic0.getStory(TEST_STORY_ID_1)
    val exp1 = story1.getChapter(TEST_EXPLORATION_ID_1)
    assertThat(exp1.isCompleted()).isTrue()
  }

  @Test
  fun testMarkChapterDone_testTopic0_story1_exp1_story1IsNotDone() {
    storyProgressTestHelper.markCompletedTestTopic0Story1Exp0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    val story1 = testTopic0.getStory(TEST_STORY_ID_1)
    assertThat(story1.isCompleted()).isFalse()
  }

  @Test
  fun testMarkChapterDone_testTopic0_story1_exp0_chapterIsDone() {
    storyProgressTestHelper.markCompletedTestTopic0Story1Exp1(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    val story1 = testTopic0.getStory(TEST_STORY_ID_1)
    val exp0 = story1.getChapter(TEST_EXPLORATION_ID_0)
    assertThat(exp0.isCompleted()).isTrue()
  }

  @Test
  fun testMarkChapterDone_testTopic0_story1_exp0_story1IsNotDone() {
    storyProgressTestHelper.markCompletedTestTopic0Story1Exp1(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    val story1 = testTopic0.getStory(TEST_STORY_ID_1)
    assertThat(story1.isCompleted()).isFalse()
  }

  @Test
  fun testMarkChapterDone_testTopic0_story1_exp3_chapterIsDone() {
    storyProgressTestHelper.markCompletedTestTopic0Story1Exp2(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    val story1 = testTopic0.getStory(TEST_STORY_ID_1)
    val exp3 = story1.getChapter(TEST_EXPLORATION_ID_3)
    assertThat(exp3.isCompleted()).isTrue()
  }

  @Test
  fun testMarkChapterDone_testTopic0_story1_exp3_story1IsDone() {
    storyProgressTestHelper.markCompletedTestTopic0Story1Exp2(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    val story1 = testTopic0.getStory(TEST_STORY_ID_1)
    // The story is completed since exp 3 requires explorations 1 & 0 to be finished first.
    assertThat(story1.isCompleted()).isTrue()
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
  fun testMarkStoryDone_testTopic0_story0_topicIsNotDone() {
    storyProgressTestHelper.markCompletedTestTopic0Story0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    assertThat(testTopic0.isCompleted()).isFalse()
  }

  @Test
  fun testMarkStoryDone_testTopic0_story1_storyIsDone() {
    storyProgressTestHelper.markCompletedTestTopic0Story1(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    val story1 = testTopic0.getStory(TEST_STORY_ID_1)
    assertThat(story1.isCompleted()).isTrue()
  }

  @Test
  fun testMarkStoryDone_testTopic0_story1_topicIsNotDone() {
    storyProgressTestHelper.markCompletedTestTopic0Story1(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    assertThat(testTopic0.isCompleted()).isFalse()
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

  @Test
  fun testMarkStoryDone_testTopic0_story1_topicIsPartiallyDone() {
    storyProgressTestHelper.markCompletedTestTopic0Story1(
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
  fun testMarkChapterRecentlyPlayed_testTopic0_story0_exp2_chapterIsStarted() {
    storyProgressTestHelper.markRecentlyPlayedTestTopic0Story0Exp0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    val story0 = testTopic0.getStory(TEST_STORY_ID_0)
    val exp2 = story0.getChapter(TEST_EXPLORATION_ID_2)
    assertThat(exp2.isStarted()).isTrue()
  }

  @Test
  fun testMarkChapterRecentlyPlayed_testTopic0_story0_exp2_story0IsNotDone() {
    storyProgressTestHelper.markRecentlyPlayedTestTopic0Story0Exp0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    val story0 = testTopic0.getStory(TEST_STORY_ID_0)
    assertThat(story0.isCompleted()).isFalse()
  }

  @Test
  fun testMarkChapterRecentlyPlayed_testTopic0_story0_exp5_chapterIsStarted() {
    storyProgressTestHelper.markRecentlyPlayedTestTopic0Story0Exp1(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    val story0 = testTopic0.getStory(TEST_STORY_ID_0)
    val exp5 = story0.getChapter(TEST_EXPLORATION_ID_5)
    assertThat(exp5.isStarted()).isTrue()
  }

  @Test
  fun markRecentlyPlayedForTestTopic0Story0Exp5() {
    storyProgressTestHelper.markRecentlyPlayedTestTopic0Story0Exp0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    val story0 = testTopic0.getStory(TEST_STORY_ID_0)
    assertThat(story0.isCompleted()).isFalse()
  }

  @Test
  fun testMarkChapterRecentlyPlayed_testTopic0_story1_exp1_chapterIsStarted() {
    storyProgressTestHelper.markRecentlyPlayedTestTopic0Story1Exp0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    val story1 = testTopic0.getStory(TEST_STORY_ID_1)
    val exp1 = story1.getChapter(TEST_EXPLORATION_ID_1)
    assertThat(exp1.isStarted()).isTrue()
  }

  @Test
  fun testMarkChapterRecentlyPlayed_testTopic0_story1_exp1_story1IsNotDone() {
    storyProgressTestHelper.markRecentlyPlayedTestTopic0Story1Exp0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    val story1 = testTopic0.getStory(TEST_STORY_ID_1)
    assertThat(story1.isCompleted()).isFalse()
  }

  @Test
  fun testMarkChapterRecentlyPlayed_testTopic0_story1_exp0_chapterIsStarted() {
    storyProgressTestHelper.markRecentlyPlayedTestTopic0Story1Exp1(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    val story1 = testTopic0.getStory(TEST_STORY_ID_1)
    val exp0 = story1.getChapter(TEST_EXPLORATION_ID_0)
    assertThat(exp0.isStarted()).isTrue()
  }

  @Test
  fun testMarkChapterRecentlyPlayed_testTopic0_story1_exp0_story1IsNotDone() {
    storyProgressTestHelper.markRecentlyPlayedTestTopic0Story1Exp1(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    val story1 = testTopic0.getStory(TEST_STORY_ID_1)
    assertThat(story1.isCompleted()).isFalse()
  }

  @Test
  fun testMarkChapterRecentlyPlayed_testTopic0_story1_exp3_chapterIsStarted() {
    storyProgressTestHelper.markRecentlyPlayedTestTopic0Story1Exp2(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    val story1 = testTopic0.getStory(TEST_STORY_ID_1)
    val exp3 = story1.getChapter(TEST_EXPLORATION_ID_3)
    assertThat(exp3.isStarted()).isTrue()
  }

  @Test
  fun testMarkChapterRecentlyPlayed_testTopic0_story1_exp3_story1IsNotDone() {
    storyProgressTestHelper.markRecentlyPlayedTestTopic0Story1Exp2(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    val story1 = testTopic0.getStory(TEST_STORY_ID_1)
    assertThat(story1.isCompleted()).isFalse()
  }

  @Test
  fun testMarkChapterRecentlyPlayed_testTopic1_story2_exp4_chapterIsStarted() {
    storyProgressTestHelper.markRecentlyPlayedTestTopic1Story2Exp0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic1 = getTopic(profileId0, TEST_TOPIC_ID_1)
    val story2 = testTopic1.getStory(TEST_STORY_ID_2)
    val exp4 = story2.getChapter(TEST_EXPLORATION_ID_4)
    assertThat(exp4.isStarted()).isTrue()
  }

  @Test
  fun testMarkChapterRecentlyPlayed_testTopic1_story2_exp4_story2IsNotDone() {
    storyProgressTestHelper.markRecentlyPlayedTestTopic1Story2Exp0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic1 = getTopic(profileId0, TEST_TOPIC_ID_1)
    val story2 = testTopic1.getStory(TEST_STORY_ID_2)
    assertThat(story2.isCompleted()).isFalse()
  }

  /* Test topic/story started tests. */

  @Test
  fun testMarkStoryRecentlyPlayed_testTopic0_story0_storyIsStarted() {
    storyProgressTestHelper.markRecentlyPlayedTestTopic0Story0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    val story0 = testTopic0.getStory(TEST_STORY_ID_0)
    assertThat(story0.isStarted()).isTrue()
  }

  @Test
  fun testMarkStoryRecentlyPlayed_testTopic0_story0_topicIsNotDone() {
    storyProgressTestHelper.markRecentlyPlayedTestTopic0Story0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    assertThat(testTopic0.isCompleted()).isFalse()
  }

  @Test
  fun testMarkStoryRecentlyPlayed_testTopic0_story1_storyIsStarted() {
    storyProgressTestHelper.markRecentlyPlayedTestTopic0Story1(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    val story1 = testTopic0.getStory(TEST_STORY_ID_1)
    assertThat(story1.isStarted()).isTrue()
  }

  @Test
  fun testMarkStoryRecentlyPlayed_testTopic0_story1_topicIsNotDone() {
    storyProgressTestHelper.markRecentlyPlayedTestTopic0Story1(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    assertThat(testTopic0.isCompleted()).isFalse()
  }

  @Test
  fun testMarkStoryRecentlyPlayed_testTopic1_story2_storyIsStarted() {
    storyProgressTestHelper.markRecentlyPlayedTestTopic1Story0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic1 = getTopic(profileId0, TEST_TOPIC_ID_1)
    val story2 = testTopic1.getStory(TEST_STORY_ID_2)
    assertThat(story2.isStarted()).isTrue()
  }

  @Test
  fun testMarkStoryRecentlyPlayed_testTopic1_story2_topicIsNotDone() {
    storyProgressTestHelper.markRecentlyPlayedTestTopic1Story0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic1 = getTopic(profileId0, TEST_TOPIC_ID_1)
    assertThat(testTopic1.isCompleted()).isFalse()
  }

  @Test
  fun testMarkTopicRecentlyPlayed_testTopic0_topicIsStarted() {
    storyProgressTestHelper.markRecentlyPlayedTestTopic0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    val testTopic1 = getTopic(profileId0, TEST_TOPIC_ID_1)
    assertThat(testTopic0.isStarted()).isTrue()
    assertThat(testTopic1.isStarted()).isFalse()
  }

  @Test
  fun testMarkTopicRecentlyPlayed_testTopic1_topicIsStarted() {
    storyProgressTestHelper.markRecentlyPlayedTestTopic1(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    val testTopic1 = getTopic(profileId0, TEST_TOPIC_ID_1)
    assertThat(testTopic0.isStarted()).isFalse()
    assertThat(testTopic1.isStarted()).isTrue()
  }

  @Test
  fun testMarkTopicsRecentlyPlayed_testTopics_oneTopicIsStarted() {
    storyProgressTestHelper.markRecentlyPlayedTestTopics(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    val testTopic1 = getTopic(profileId0, TEST_TOPIC_ID_1)
    // At least one of the topics is started.
    assertThat(listOf(testTopic0, testTopic1).any { it.isStarted() }).isTrue()
    // But neither is completed.
    assertThat(testTopic0.isCompleted()).isFalse()
    assertThat(testTopic1.isCompleted()).isFalse()
  }

  /* Ratios chapter started tests. */

  @Test
  fun testMarkChapterRecentlyPlayed_ratiosTopic_story0_exp0_chapterIsStarted() {
    storyProgressTestHelper.markRecentlyPlayedRatiosStory0Exp0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val ratiosTopic = getTopic(profileId0, RATIOS_TOPIC_ID)
    val story0 = ratiosTopic.getStory(RATIOS_STORY_ID_0)
    val exp0 = story0.getChapter(RATIOS_EXPLORATION_ID_0)
    assertThat(exp0.isStarted()).isTrue()
  }

  @Test
  fun testMarkChapterRecentlyPlayed_ratiosTopic_story0_exp0_story0IsNotDone() {
    storyProgressTestHelper.markRecentlyPlayedRatiosStory0Exp0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val ratiosTopic = getTopic(profileId0, RATIOS_TOPIC_ID)
    val story0 = ratiosTopic.getStory(RATIOS_STORY_ID_0)
    assertThat(story0.isCompleted()).isFalse()
  }

  @Test
  fun testMarkChapterRecentlyPlayed_ratiosTopic_story0_exp1_chapterIsStarted() {
    storyProgressTestHelper.markRecentlyPlayedRatiosStory0Exp1(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val ratiosTopic = getTopic(profileId0, RATIOS_TOPIC_ID)
    val story0 = ratiosTopic.getStory(RATIOS_STORY_ID_0)
    val exp1 = story0.getChapter(RATIOS_EXPLORATION_ID_1)
    assertThat(exp1.isStarted()).isTrue()
  }

  @Test
  fun testMarkChapterRecentlyPlayed_ratiosTopic_story0_exp1_story0IsNotDone() {
    storyProgressTestHelper.markRecentlyPlayedRatiosStory0Exp1(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val ratiosTopic = getTopic(profileId0, RATIOS_TOPIC_ID)
    val story0 = ratiosTopic.getStory(RATIOS_STORY_ID_0)
    assertThat(story0.isCompleted()).isFalse()
  }

  @Test
  fun testMarkChapterRecentlyPlayed_ratiosTopic_story1_exp2_chapterIsStarted() {
    storyProgressTestHelper.markRecentlyPlayedRatiosStory1Exp0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val ratiosTopic = getTopic(profileId0, RATIOS_TOPIC_ID)
    val story1 = ratiosTopic.getStory(RATIOS_STORY_ID_1)
    val exp2 = story1.getChapter(RATIOS_EXPLORATION_ID_2)
    assertThat(exp2.isStarted()).isTrue()
  }

  @Test
  fun testMarkChapterRecentlyPlayed_ratiosTopic_story1_exp2_story1IsNotDone() {
    storyProgressTestHelper.markRecentlyPlayedRatiosStory1Exp0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val ratiosTopic = getTopic(profileId0, RATIOS_TOPIC_ID)
    val story1 = ratiosTopic.getStory(RATIOS_STORY_ID_1)
    assertThat(story1.isCompleted()).isFalse()
  }

  @Test
  fun testMarkChapterRecentlyPlayed_ratiosTopic_story1_exp3_chapterIsStarted() {
    storyProgressTestHelper.markRecentlyPlayedRatiosStory1Exp1(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val ratiosTopic = getTopic(profileId0, RATIOS_TOPIC_ID)
    val story1 = ratiosTopic.getStory(RATIOS_STORY_ID_1)
    val exp3 = story1.getChapter(RATIOS_EXPLORATION_ID_3)
    assertThat(exp3.isStarted()).isTrue()
  }

  @Test
  fun testMarkChapterRecentlyPlayed_ratiosTopic_story1_exp3_story1IsNotDone() {
    storyProgressTestHelper.markRecentlyPlayedRatiosStory1Exp1(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val ratiosTopic = getTopic(profileId0, RATIOS_TOPIC_ID)
    val story1 = ratiosTopic.getStory(RATIOS_STORY_ID_1)
    assertThat(story1.isCompleted()).isFalse()
  }

  /* Ratios topic/story started tests. */

  @Test
  fun testMarkStoryRecentlyPlayed_ratiosTopic_story0_storyIsStarted() {
    storyProgressTestHelper.markRecentlyPlayedRatiosStory0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val ratiosTopic = getTopic(profileId0, RATIOS_TOPIC_ID)
    val story0 = ratiosTopic.getStory(RATIOS_STORY_ID_0)
    val story1 = ratiosTopic.getStory(RATIOS_STORY_ID_1)
    assertThat(story0.isStarted()).isTrue()
    assertThat(story1.isStarted()).isFalse()
  }

  @Test
  fun testMarkStoryRecentlyPlayed_ratiosTopic_story0_topicIsNotDone() {
    storyProgressTestHelper.markRecentlyPlayedRatiosStory0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val ratiosTopic = getTopic(profileId0, RATIOS_TOPIC_ID)
    assertThat(ratiosTopic.isCompleted()).isFalse()
  }

  @Test
  fun testMarkStoryRecentlyPlayed_ratiosTopic_story1_storyIsStarted() {
    storyProgressTestHelper.markRecentlyPlayedRatiosStory1(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val ratiosTopic = getTopic(profileId0, RATIOS_TOPIC_ID)
    val story0 = ratiosTopic.getStory(RATIOS_STORY_ID_0)
    val story1 = ratiosTopic.getStory(RATIOS_STORY_ID_1)
    assertThat(story0.isStarted()).isFalse()
    assertThat(story1.isStarted()).isTrue()
  }

  @Test
  fun testMarkStoryRecentlyPlayed_ratiosTopic_story1_topicIsNotDone() {
    storyProgressTestHelper.markRecentlyPlayedRatiosStory1(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val ratiosTopic = getTopic(profileId0, RATIOS_TOPIC_ID)
    assertThat(ratiosTopic.isCompleted()).isFalse()
  }

  @Test
  fun testMarkTopicRecentlyPlayed_ratiosTopic_topicIsStarted() {
    storyProgressTestHelper.markRecentlyPlayedRatios(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val ratiosTopic = getTopic(profileId0, RATIOS_TOPIC_ID)
    assertThat(ratiosTopic.isStarted()).isTrue()
  }

  /* Fractions topic/story/chapter started tests. */

  @Test
  fun testMarkChapterRecentlyPlayed_fractionsTopic_story0_exp0_chapterIsStarted() {
    storyProgressTestHelper.markRecentlyPlayedFractionsStory0Exp0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val fractionsTopic = getTopic(profileId0, FRACTIONS_TOPIC_ID)
    val story0 = fractionsTopic.getStory(FRACTIONS_STORY_ID_0)
    val exp0 = story0.getChapter(FRACTIONS_EXPLORATION_ID_0)
    assertThat(exp0.isStarted()).isTrue()
  }

  @Test
  fun testMarkChapterRecentlyPlayed_fractionsTopic_story0_exp0_story0IsNotDone() {
    storyProgressTestHelper.markRecentlyPlayedFractionsStory0Exp0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val fractionsTopic = getTopic(profileId0, FRACTIONS_TOPIC_ID)
    val story0 = fractionsTopic.getStory(FRACTIONS_STORY_ID_0)
    assertThat(story0.isCompleted()).isFalse()
  }

  @Test
  fun testMarkChapterRecentlyPlayed_fractionsTopic_story0_exp1_chapterIsStarted() {
    storyProgressTestHelper.markRecentlyPlayedFractionsStory0Exp1(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val fractionsTopic = getTopic(profileId0, FRACTIONS_TOPIC_ID)
    val story0 = fractionsTopic.getStory(FRACTIONS_STORY_ID_0)
    val exp1 = story0.getChapter(FRACTIONS_EXPLORATION_ID_1)
    assertThat(exp1.isStarted()).isTrue()
  }

  @Test
  fun testMarkChapterRecentlyPlayed_fractionsTopic_story0_exp1_story0IsNotDone() {
    storyProgressTestHelper.markRecentlyPlayedFractionsStory0Exp1(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val fractionsTopic = getTopic(profileId0, FRACTIONS_TOPIC_ID)
    val story0 = fractionsTopic.getStory(FRACTIONS_STORY_ID_0)
    assertThat(story0.isCompleted()).isFalse()
  }

  @Test
  fun testMarkStoryRecentlyPlayed_fractionsTopic_story0_storyIsStarted() {
    storyProgressTestHelper.markRecentlyPlayedFractionsStory0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val fractionsTopic = getTopic(profileId0, FRACTIONS_TOPIC_ID)
    val story0 = fractionsTopic.getStory(FRACTIONS_STORY_ID_0)
    assertThat(story0.isStarted()).isTrue()
  }

  @Test
  fun testMarkStoryRecentlyPlayed_fractionsTopic_story0_topicIsStarted() {
    storyProgressTestHelper.markRecentlyPlayedFractionsStory0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val fractionsTopic = getTopic(profileId0, FRACTIONS_TOPIC_ID)
    assertThat(fractionsTopic.isStarted()).isTrue()
  }

  @Test
  fun testMarkTopicRecentlyPlayed_fractionsTopic_topicIsStarted() {
    storyProgressTestHelper.markRecentlyPlayedFractions(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val fractionsTopic = getTopic(profileId0, FRACTIONS_TOPIC_ID)
    assertThat(fractionsTopic.isStarted()).isTrue()
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
  fun testMarkAllTopicsRecentlyPlayed_allTopicsAreStarted() {
    storyProgressTestHelper.markAllTopicsAsRecentlyPlayed(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId0, TEST_TOPIC_ID_0)
    val testTopic1 = getTopic(profileId0, TEST_TOPIC_ID_1)
    val ratiosTopic = getTopic(profileId0, RATIOS_TOPIC_ID)
    val fractionsTopic = getTopic(profileId0, FRACTIONS_TOPIC_ID)

    assertThat(testTopic0.isStarted()).isTrue()
    assertThat(testTopic1.isStarted()).isTrue()
    assertThat(ratiosTopic.isStarted()).isTrue()
    assertThat(fractionsTopic.isStarted()).isTrue()
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
  fun testRecentlyPlayedChapter_thenMarkedDone_chapterIsCompleted() {
    storyProgressTestHelper.markRecentlyPlayedTestTopic0Story0Exp0(
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
  fun testMarkChapterDone_thenRecentlyPlayed_chapterIsCompleted() {
    storyProgressTestHelper.markCompletedTestTopic0Story0Exp0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )
    storyProgressTestHelper.markRecentlyPlayedTestTopic0Story0Exp0(
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
  fun testMarkChapterRecentlyPlayed_newerThanWeek_timestampNewerThanWeek() {
    storyProgressTestHelper.markRecentlyPlayedTestTopic0Story0Exp0(
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
  fun testMarkChapterRecentlyPlayed_olderThanWeek_timestampOlderThanWeek() {
    storyProgressTestHelper.markRecentlyPlayedTestTopic0Story0Exp0(
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
  fun testMarkChapterComplete_oneOneProfile_notCompletedOnOtherProfile() {
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
  fun testMarkChapterRecentlyPlayed_oneOneProfile_notStartedOnOtherProfile() {
    storyProgressTestHelper.markRecentlyPlayedTestTopic0Story0Exp0(
      profileId = profileId0,
      timestampOlderThanOneWeek = false
    )

    val testTopic0 = getTopic(profileId1, TEST_TOPIC_ID_0)
    val story0 = testTopic0.getStory(TEST_STORY_ID_0)
    val exp2 = story0.getChapter(TEST_EXPLORATION_ID_2)
    assertThat(exp2.isStarted()).isFalse()
  }

  private fun getTopic(profileId: ProfileId, topicId: String): Topic {
    return retrieveSuccessfulResult(
      topicController.getTopic(profileId, topicId), mockTopicObserver, topicResultCaptor
    )
  }

  private fun Topic.getStory(storyId: String): StorySummary {
    return storyList.find { it.storyId == storyId } ?: error("Failed to find story: $storyId")
  }

  private fun Topic.isNotStarted(): Boolean = storyList.all { it.isNotStarted() }

  private fun Topic.isStarted(): Boolean = storyList.any { it.isStarted() }

  private fun Topic.isPartiallyCompleted(): Boolean = storyList.any { it.isCompleted() }

  private fun Topic.isCompleted(): Boolean = storyList.all { it.isCompleted() }

  private fun StorySummary.getChapter(expId: String): ChapterSummary {
    return chapterList.find { it.explorationId == expId } ?: error("Failed to find chapter: $expId")
  }

  private fun StorySummary.isNotStarted(): Boolean = chapterList.all { it.isNotStarted() }

  private fun StorySummary.isStarted(): Boolean = chapterList.any { it.isStarted() }

  private fun StorySummary.isCompleted(): Boolean = chapterList.all { it.isCompleted() }

  private fun ChapterSummary.isNotStarted(): Boolean =
    chapterPlayState in listOf(
      ChapterPlayState.NOT_STARTED, ChapterPlayState.NOT_PLAYABLE_MISSING_PREREQUISITES
    )

  private fun ChapterSummary.isStarted(): Boolean =
    chapterPlayState == ChapterPlayState.STARTED_NOT_COMPLETED

  private fun ChapterSummary.isCompleted(): Boolean = chapterPlayState == ChapterPlayState.COMPLETED

  private fun getTopicProgressDatabase(profileId: ProfileId): TopicProgressDatabase {
    // Hacky way to retrieve the current progress database.
    val persistentCacheStore =
      persistentCacheStoreFactory.createPerProfile(
        "topic_progress_database",
        TopicProgressDatabase.getDefaultInstance(),
        profileId
      )
    return retrieveSuccessfulResult(
      persistentCacheStore, mockTopicProgressDatabaseObserver, topicProgressDatabaseResultCaptor
    )
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

  private fun <T> retrieveSuccessfulResult(
    dataProvider: DataProvider<T>,
    mockObserver: Observer<AsyncResult<T>>,
    mockResultCaptor: ArgumentCaptor<AsyncResult<T>>
  ): T {
    val requestLiveData = dataProvider.toLiveData()
    reset(mockObserver)
    requestLiveData.observeForever(mockObserver)

    // Provide time for the topic retrieval to complete.
    testCoroutineDispatchers.runCurrent()

    verify(mockObserver, atLeastOnce()).onChanged(mockResultCaptor.capture())
    requestLiveData.removeObserver(mockObserver)
    val result = mockResultCaptor.value
    assertThat(result.isSuccess()).isTrue()
    return result.getOrThrow()
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
  @Component(
    modules = [
      TestModule::class, TestLogReportingModule::class, LogStorageModule::class,
      TestDispatcherModule::class, RobolectricModule::class, FakeOppiaClockModule::class,
      ImageParsingModule::class, CachingTestModule::class, LoggerModule::class
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
