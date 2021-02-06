package org.oppia.android.testing.story

import androidx.lifecycle.Observer
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.reset
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.oppia.android.app.model.ProfileId
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
import org.oppia.android.domain.topic.StoryProgressController
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
import org.oppia.android.testing.TestCoroutineDispatchers
import org.oppia.android.testing.time.FakeOppiaClock
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

private val EIGHT_DAYS_IN_MS = TimeUnit.DAYS.toMillis(8)

/**
 * This helper allows tests to easily create dummy progress per profile-basis.
 *
 * Note that it's required that tests depending on this helper change their fake Oppia clock to use
 * uptime millis rather than wall clock time & leverage the test coroutine dispatcher's ability to
 * move forward Robolectric's fake time clock for better reliability when recording chapter progress
 * or completion.
 */
@Singleton
class StoryProgressTestHelper @Inject constructor(
  private val storyProgressController: StoryProgressController,
  private val fakeOppiaClock: FakeOppiaClock,
  private val testCoroutineDispatchers: TestCoroutineDispatchers
) {
  @Mock lateinit var mockLiveDataObserver: Observer<AsyncResult<Any?>>
  @Captor lateinit var liveDataResultCaptor: ArgumentCaptor<AsyncResult<Any?>>

  init {
    MockitoAnnotations.initMocks(this)
  }

  /**
   * Marks full topic progress on all topics for a particular profile.
   *
   * Note that this function will advance the clock & synchronize all background execution to ensure
   * that any operations needed to achieve the requested state are completed and successful.
   *
   * @param profileId the profile we are setting topic progress for
   * @param timestampOlderThanOneWeek if the timestamp for this topic progress is more than one week
   *     ago
   */
  fun markFullProgressForAllTopics(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    markFullTopicProgressForFractions(profileId, timestampOlderThanOneWeek)
    markFullTopicProgressForTestTopics(profileId, timestampOlderThanOneWeek)
    markFullTopicProgressForRatios(profileId, timestampOlderThanOneWeek)
  }

  /**
   * Creates a partial story progress for a particular profile.
   *
   * Note that this function will advance the clock & synchronize all background execution to ensure
   * that any operations needed to achieve the requested state are completed and successful.
   *
   * @param profileId the profile we are setting partial progress of the fraction story for
   * @param timestampOlderThanOneWeek if the timestamp for this topic progress is more than one week
   *     ago
   */
  fun markPartialStoryProgressForFractions(
    profileId: ProfileId,
    timestampOlderThanOneWeek: Boolean
  ) {
    markChapDoneFracStory0Exp0(profileId, timestampOlderThanOneWeek)
  }

  /**
   * Creates a partial story progress for a particular profile.
   *
   * Note that this function will advance the clock & synchronize all background execution to ensure
   * that any operations needed to achieve the requested state are completed and successful.
   *
   * @param profileId the profile we are setting partial progress of the fraction story for
   * @param timestampOlderThanOneWeek if the timestamp for this topic progress is more than one week
   *     ago
   */
  fun markChapDoneFracStory0Exp0(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    recordCompletedChapter(
      profileId,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_0,
      timestampOlderThanOneWeek
    )
  }

  /**
   * Creates a partial story progress for a particular profile.
   *
   * Note that this function will advance the clock & synchronize all background execution to ensure
   * that any operations needed to achieve the requested state are completed and successful.
   *
   * @param profileId the profile we are setting partial progress of the fraction story for
   * @param timestampOlderThanOneWeek if the timestamp for this topic progress is more than one week
   *     ago
   */
  fun markChapDoneFracStory0Exp1(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    recordCompletedChapter(
      profileId,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_1,
      timestampOlderThanOneWeek
    )
  }

  /**
   * Creates a partial topic progress for a particular profile.
   *
   * Note that this function will advance the clock & synchronize all background execution to ensure
   * that any operations needed to achieve the requested state are completed and successful.
   *
   * @param profileId the profile we are setting partial progress of the fraction topic for
   * @param timestampOlderThanOneWeek if the timestamp for this topic progress is more than one week
   *     ago
   */
  fun markPartialTopicProgressForFractions(
    profileId: ProfileId,
    timestampOlderThanOneWeek: Boolean
  ) {
    markPartialStoryProgressForFractions(profileId, timestampOlderThanOneWeek)
  }

  /**
   *  Marks full story progress for a particular profile.
   *
   * Note that this function will advance the clock & synchronize all background execution to ensure
   * that any operations needed to achieve the requested state are completed and successful.
   *
   * @param profileId the profile we are setting full on the fraction story progress for
   * @param timestampOlderThanOneWeek if the timestamp for this topic progress is more than one week
   *     ago
   */
  fun markFullStoryProgressForFractions(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    markChapDoneFracStory0Exp0(profileId, timestampOlderThanOneWeek)
    markChapDoneFracStory0Exp1(profileId, timestampOlderThanOneWeek)
  }

  /**
   * Marks full topic progress for a particular profile.
   *
   * Note that this function will advance the clock & synchronize all background execution to ensure
   * that any operations needed to achieve the requested state are completed and successful.
   *
   * @param profileId the profile we are setting fraction topic progress for
   * @param timestampOlderThanOneWeek if the timestamp for this topic progress is more than one week
   *     ago
   */
  fun markFullTopicProgressForFractions(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    markFullStoryProgressForFractions(profileId, timestampOlderThanOneWeek)
  }

  /**
   * Marks full topic progress on Test Topics for a particular profile.
   *
   * Note that this function will advance the clock & synchronize all background execution to ensure
   * that any operations needed to achieve the requested state are completed and successful.
   *
   * @param profileId the profile we are setting topic progress for
   * @param timestampOlderThanOneWeek if the timestamp for this topic progress is more than one week
   *     ago
   */
  fun markFullTopicProgressForTestTopics(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    // Stories and Explorations for "Test Topic"s are not in chronological order so we want to
    // ensure that the combinations of Topic / Story / Exploration that are visible will be marked
    // as completed.
    markFullTopicProgressForTestTopic0(profileId, timestampOlderThanOneWeek)
    markFullTopicProgressForTestTopic1(profileId, timestampOlderThanOneWeek)
  }

  /**
   * Creates a partial story progress for a particular profile.
   *
   * Note that this function will advance the clock & synchronize all background execution to ensure
   * that any operations needed to achieve the requested state are completed and successful.
   *
   * @param profileId the profile we are setting partial progress of the fraction story for
   * @param timestampOlderThanOneWeek if the timestamp for this topic progress is more than one week
   *     ago
   */
  fun markChapDoneTestTopic0Story0Exp2(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    recordCompletedChapter(
      profileId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      timestampOlderThanOneWeek
    )
  }

  /**
   * Creates a partial story progress for a particular profile.
   *
   * Note that this function will advance the clock & synchronize all background execution to ensure
   * that any operations needed to achieve the requested state are completed and successful.
   *
   * @param profileId the profile we are setting partial progress of the fraction story for
   * @param timestampOlderThanOneWeek if the timestamp for this topic progress is more than one week
   *     ago
   */
  fun markChapDoneTestTopic0Story0Exp5(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    recordCompletedChapter(
      profileId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_5,
      timestampOlderThanOneWeek
    )
  }

  /**
   * Creates a partial story progress for a particular profile.
   *
   * Note that this function will advance the clock & synchronize all background execution to ensure
   * that any operations needed to achieve the requested state are completed and successful.
   *
   * @param profileId the profile we are setting partial progress of the fraction story for
   * @param timestampOlderThanOneWeek if the timestamp for this topic progress is more than one week
   *     ago
   */
  fun markChapDoneTestTopic0Story1Exp1(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    recordCompletedChapter(
      profileId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_1,
      TEST_EXPLORATION_ID_1,
      timestampOlderThanOneWeek
    )
  }

  /**
   * Creates a partial story progress for a particular profile.
   *
   * Note that this function will advance the clock & synchronize all background execution to ensure
   * that any operations needed to achieve the requested state are completed and successful.
   *
   * @param profileId the profile we are setting partial progress of the fraction story for
   * @param timestampOlderThanOneWeek if the timestamp for this topic progress is more than one week
   *     ago
   */
  fun markChapDoneTestTopic0Story1Exp0(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    recordCompletedChapter(
      profileId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_1,
      TEST_EXPLORATION_ID_0,
      timestampOlderThanOneWeek
    )
  }

  /**
   * Creates a partial story progress for a particular profile.
   *
   * Note that this function will advance the clock & synchronize all background execution to ensure
   * that any operations needed to achieve the requested state are completed and successful.
   *
   * @param profileId the profile we are setting partial progress of the fraction story for
   * @param timestampOlderThanOneWeek if the timestamp for this topic progress is more than one week
   *     ago
   */
  fun markChapDoneTestTopic0Story1Exp3(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    recordCompletedChapter(
      profileId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_1,
      TEST_EXPLORATION_ID_3,
      timestampOlderThanOneWeek
    )
  }

  /**
   * Creates a partial story progress for a particular profile.
   *
   * Note that this function will advance the clock & synchronize all background execution to ensure
   * that any operations needed to achieve the requested state are completed and successful.
   *
   * @param profileId the profile we are setting partial progress of the fraction story for
   * @param timestampOlderThanOneWeek if the timestamp for this topic progress is more than one week
   *     ago
   */
  fun markChapDoneTestTopic1Story2Exp4(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    recordCompletedChapter(
      profileId,
      TEST_TOPIC_ID_1,
      TEST_STORY_ID_2,
      TEST_EXPLORATION_ID_4,
      timestampOlderThanOneWeek
    )
  }

  /**
   * Creates a partial story progress for a particular profile.
   *
   * Note that this function will advance the clock & synchronize all background execution to ensure
   * that any operations needed to achieve the requested state are completed and successful.
   *
   * @param profileId the profile we are setting partial progress of the fraction story for
   * @param timestampOlderThanOneWeek if the timestamp for this topic progress is more than one week
   *     ago
   */
  fun markTestTopic0Story0Done(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    markChapDoneTestTopic0Story0Exp2(profileId, timestampOlderThanOneWeek)
    markChapDoneTestTopic0Story0Exp5(profileId, timestampOlderThanOneWeek)
  }

  /**
   * Creates a partial story progress for a particular profile.
   *
   * Note that this function will advance the clock & synchronize all background execution to ensure
   * that any operations needed to achieve the requested state are completed and successful.
   *
   * @param profileId the profile we are setting partial progress of the fraction story for
   * @param timestampOlderThanOneWeek if the timestamp for this topic progress is more than one week
   *     ago
   */
  fun markTestTopic0Story1Done(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    markChapDoneTestTopic0Story1Exp1(profileId, timestampOlderThanOneWeek)
    markChapDoneTestTopic0Story1Exp0(profileId, timestampOlderThanOneWeek)
    markChapDoneTestTopic0Story1Exp3(profileId, timestampOlderThanOneWeek)
  }

  /**
   * Creates a partial story progress for a particular profile.
   *
   * Note that this function will advance the clock & synchronize all background execution to ensure
   * that any operations needed to achieve the requested state are completed and successful.
   *
   * @param profileId the profile we are setting partial progress of the fraction story for
   * @param timestampOlderThanOneWeek if the timestamp for this topic progress is more than one week
   *     ago
   */
  fun markTestTopic1Story2Done(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    markChapDoneTestTopic1Story2Exp4(profileId, timestampOlderThanOneWeek)
  }

  /**
   * Marks full topic progress on Ratios for a particular profile.
   *
   * Note that this function will advance the clock & synchronize all background execution to ensure
   * that any operations needed to achieve the requested state are completed and successful.
   *
   * @param profileId the profile we are setting topic progress for
   * @param timestampOlderThanOneWeek if the timestamp for this topic progress is more than one week
   *     ago
   */
  fun markFullTopicProgressForTestTopic0(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    markTestTopic0Story0Done(profileId, timestampOlderThanOneWeek)
    markTestTopic0Story1Done(profileId, timestampOlderThanOneWeek)
  }

  /**
   * Marks full topic progress on Ratios for a particular profile.
   *
   * Note that this function will advance the clock & synchronize all background execution to ensure
   * that any operations needed to achieve the requested state are completed and successful.
   *
   * @param profileId the profile we are setting topic progress for
   * @param timestampOlderThanOneWeek if the timestamp for this topic progress is more than one week
   *     ago
   */
  fun markFullTopicProgressForTestTopic1(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    markTestTopic1Story2Done(profileId, timestampOlderThanOneWeek)
  }

  /**
   * Marks full topic progress on Ratios for a particular profile.
   *
   * Note that this function will advance the clock & synchronize all background execution to ensure
   * that any operations needed to achieve the requested state are completed and successful.
   *
   * @param profileId the profile we are setting topic progress for
   * @param timestampOlderThanOneWeek if the timestamp for this topic progress is more than one week
   *     ago
   */
  fun markFullTopicProgressForRatios(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    markRatiosStory0Done(profileId, timestampOlderThanOneWeek)
    markRatiosStory1Done(profileId, timestampOlderThanOneWeek)
  }

  /**
   * Marks full topic progress on the second test topic for a particular profile.
   *
   * Note that this function will advance the clock & synchronize all background execution to ensure
   * that any operations needed to achieve the requested state are completed and successful.
   *
   * @param profileId the profile we are setting topic progress for
   * @param timestampOlderThanOneWeek if the timestamp for this topic progress is more than one week
   *     ago
   */
  fun markFullProgressForSecondTestTopic(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    markTestTopic1Story2Done(profileId, timestampOlderThanOneWeek)
  }

  /**
   * Creates a partial story progress for a particular profile.
   *
   * Note that this function will advance the clock & synchronize all background execution to ensure
   * that any operations needed to achieve the requested state are completed and successful.
   *
   * @param profileId the profile we are setting partial progress of the fraction story for
   * @param timestampOlderThanOneWeek if the timestamp for this topic progress is more than one week
   *     ago
   */
  fun markChapDoneRatiosStory0Exp0(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    recordCompletedChapter(
      profileId,
      RATIOS_TOPIC_ID,
      RATIOS_STORY_ID_0,
      RATIOS_EXPLORATION_ID_0,
      timestampOlderThanOneWeek
    )
  }

  /**
   * Creates a partial story progress for a particular profile.
   *
   * Note that this function will advance the clock & synchronize all background execution to ensure
   * that any operations needed to achieve the requested state are completed and successful.
   *
   * @param profileId the profile we are setting partial progress of the fraction story for
   * @param timestampOlderThanOneWeek if the timestamp for this topic progress is more than one week
   *     ago
   */
  fun markChapDoneRatiosStory0Exp1(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    recordCompletedChapter(
      profileId,
      RATIOS_TOPIC_ID,
      RATIOS_STORY_ID_0,
      RATIOS_EXPLORATION_ID_1,
      timestampOlderThanOneWeek
    )
  }

  /**
   * Creates a partial story progress for a particular profile.
   *
   * Note that this function will advance the clock & synchronize all background execution to ensure
   * that any operations needed to achieve the requested state are completed and successful.
   *
   * @param profileId the profile we are setting partial progress of the fraction story for
   * @param timestampOlderThanOneWeek if the timestamp for this topic progress is more than one week
   *     ago
   */
  fun markChapDoneRatiosStory1Exp2(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    recordCompletedChapter(
      profileId,
      RATIOS_TOPIC_ID,
      RATIOS_STORY_ID_1,
      RATIOS_EXPLORATION_ID_2,
      timestampOlderThanOneWeek
    )
  }

  /**
   * Creates a partial story progress for a particular profile.
   *
   * Note that this function will advance the clock & synchronize all background execution to ensure
   * that any operations needed to achieve the requested state are completed and successful.
   *
   * @param profileId the profile we are setting partial progress of the fraction story for
   * @param timestampOlderThanOneWeek if the timestamp for this topic progress is more than one week
   *     ago
   */
  fun markChapDoneRatiosStory1Exp3(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    recordCompletedChapter(
      profileId,
      RATIOS_TOPIC_ID,
      RATIOS_STORY_ID_1,
      RATIOS_EXPLORATION_ID_3,
      timestampOlderThanOneWeek
    )
  }

  /**
   * Creates a partial story progress for a particular profile.
   *
   * Note that this function will advance the clock & synchronize all background execution to ensure
   * that any operations needed to achieve the requested state are completed and successful.
   *
   * @param profileId the profile we are setting partial progress of the fraction story for
   * @param timestampOlderThanOneWeek if the timestamp for this topic progress is more than one week
   *     ago
   */
  fun markRatiosStory0Done(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    markChapDoneRatiosStory0Exp0(profileId, timestampOlderThanOneWeek)
    markChapDoneRatiosStory0Exp1(profileId, timestampOlderThanOneWeek)
  }

  /**
   * Creates a partial story progress for a particular profile.
   *
   * Note that this function will advance the clock & synchronize all background execution to ensure
   * that any operations needed to achieve the requested state are completed and successful.
   *
   * @param profileId the profile we are setting partial progress of the fraction story for
   * @param timestampOlderThanOneWeek if the timestamp for this topic progress is more than one week
   *     ago
   */
  fun markRatiosStory1Done(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    markChapDoneRatiosStory1Exp2(profileId, timestampOlderThanOneWeek)
    markChapDoneRatiosStory1Exp3(profileId, timestampOlderThanOneWeek)
  }

  /**
   * Marks one story progress fully complete in the ratios topic for a particular profile.
   *
   * Note that this function will advance the clock & synchronize all background execution to ensure
   * that any operations needed to achieve the requested state are completed and successful.
   *
   * @param profileId the profile we are setting topic progress on ratios for
   * @param timestampOlderThanOneWeek if the timestamp for this topic progress is more than one week
   *     ago
   */
  fun markFullStoryPartialTopicProgressForRatios(
    profileId: ProfileId,
    timestampOlderThanOneWeek: Boolean
  ) {
    markRatiosStory0Done(profileId, timestampOlderThanOneWeek)
  }

  /**
   * Marks two partial story progress in ratios exploration for a particular profile.
   *
   * Note that this function will advance the clock & synchronize all background execution to ensure
   * that any operations needed to achieve the requested state are completed and successful.
   *
   * @param profileId the profile we are setting topic progress on ratios for
   * @param timestampOlderThanOneWeek if the timestamp for this topic progress is more than one week
   *     ago
   */
  fun markTwoPartialStoryProgressForRatios(
    profileId: ProfileId,
    timestampOlderThanOneWeek: Boolean
  ) {
    markChapDoneRatiosStory0Exp0(profileId, timestampOlderThanOneWeek)
    markChapDoneRatiosStory1Exp2(profileId, timestampOlderThanOneWeek)
  }

  /**
   * Marks an exploration as recently played for a particular profile.
   *
   * Note that this function will advance the clock & synchronize all background execution to ensure
   * that any operations needed to achieve the requested state are completed and successful.
   *
   * @param profileId the profile we are setting recently played for
   * @param timestampOlderThanOneWeek if the timestamp for this topic progress is more than one week
   *     ago
   */
  fun markRecentlyPlayedForTestTopic0Story0Exp2(
    profileId: ProfileId,
    timestampOlderThanOneWeek: Boolean
  ) {
    recordRecentlyPlayedChapter(
      profileId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      timestampOlderThanOneWeek
    )
  }

  /**
   * Marks an exploration as recently played for a particular profile.
   *
   * Note that this function will advance the clock & synchronize all background execution to ensure
   * that any operations needed to achieve the requested state are completed and successful.
   *
   * @param profileId the profile we are setting recently played for
   * @param timestampOlderThanOneWeek if the timestamp for this topic progress is more than one week
   *     ago
   */
  fun markRecentlyPlayedForTestTopic0Story1Exp1(
    profileId: ProfileId,
    timestampOlderThanOneWeek: Boolean
  ) {
    recordRecentlyPlayedChapter(
      profileId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_1,
      TEST_EXPLORATION_ID_1,
      timestampOlderThanOneWeek
    )
  }

  /**
   * Marks exploration [FRACTIONS_EXPLORATION_ID_0] as recently played for a particular profile.
   *
   * Note that this function will advance the clock & synchronize all background execution to ensure
   * that any operations needed to achieve the requested state are completed and successful.
   *
   * @param profileId the profile we are setting recently played for
   * @param timestampOlderThanOneWeek if the timestamp for this topic progress is more than one week
   *     ago
   */
  fun markRecentlyPlayedForFractionsStory0Exploration0(
    profileId: ProfileId,
    timestampOlderThanOneWeek: Boolean
  ) {
    recordRecentlyPlayedChapter(
      profileId,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_0,
      timestampOlderThanOneWeek
    )
  }

  /**
   * Marks exploration [FRACTIONS_EXPLORATION_ID_1] as recently played for a particular profile.
   *
   * Note that this function will advance the clock & synchronize all background execution to ensure
   * that any operations needed to achieve the requested state are completed and successful.
   *
   * @param profileId the profile we are setting recently played for
   * @param timestampOlderThanOneWeek if the timestamp for this topic progress is more than one week
   *     ago
   */
  fun markRecentlyPlayedForFractionsStory0Exploration1(
    profileId: ProfileId,
    timestampOlderThanOneWeek: Boolean
  ) {
    recordRecentlyPlayedChapter(
      profileId,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_1,
      timestampOlderThanOneWeek
    )
  }

  /**
   * Marks exploration [RATIOS_EXPLORATION_ID_0] as recently played for a particular profile.
   *
   * Note that this function will advance the clock & synchronize all background execution to ensure
   * that any operations needed to achieve the requested state are completed and successful.
   *
   * @param profileId the profile we are setting recently played for
   * @param timestampOlderThanOneWeek if the timestamp for this topic progress is more than one week
   *     ago
   */
  fun markRecentlyPlayedForRatiosStory0Exploration0(
    profileId: ProfileId,
    timestampOlderThanOneWeek: Boolean
  ) {
    recordRecentlyPlayedChapter(
      profileId,
      RATIOS_TOPIC_ID,
      RATIOS_STORY_ID_0,
      RATIOS_EXPLORATION_ID_0,
      timestampOlderThanOneWeek
    )
  }

  /**
   * Marks first exploration in both stories of Ratios as recently played for a particular profile.
   *
   * Note that this function will advance the clock & synchronize all background execution to ensure
   * that any operations needed to achieve the requested state are completed and successful.
   *
   * @param profileId the profile we are setting recently played for
   * @param timestampOlderThanOneWeek if the timestamp for this topic progress is more than one week
   *     ago
   */
  fun markRecentlyPlayedForRatiosStory0Exploration0AndStory1Exploration2(
    profileId: ProfileId,
    timestampOlderThanOneWeek: Boolean
  ) {
    recordRecentlyPlayedChapter(
      profileId,
      RATIOS_TOPIC_ID,
      RATIOS_STORY_ID_0,
      RATIOS_EXPLORATION_ID_0,
      timestampOlderThanOneWeek
    )

    recordRecentlyPlayedChapter(
      profileId,
      RATIOS_TOPIC_ID,
      RATIOS_STORY_ID_1,
      RATIOS_EXPLORATION_ID_2,
      timestampOlderThanOneWeek
    )
  }

  /**
   * Marks first exploration in all stories of Ratios & Fractions as recently played for a
   * particular profile.
   *
   * Note that this function will advance the clock & synchronize all background execution to ensure
   * that any operations needed to achieve the requested state are completed and successful.
   *
   * @param profileId the profile we are setting recently played for.
   * @param timestampOlderThanOneWeek if the timestamp for this topic progress is more than one week
   *     ago
   */
  fun markRecentlyPlayedForFirstExplorationInAllStoriesInFractionsAndRatios(
    profileId: ProfileId,
    timestampOlderThanOneWeek: Boolean
  ) {
    recordRecentlyPlayedChapter(
      profileId,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_0,
      timestampOlderThanOneWeek
    )

    recordRecentlyPlayedChapter(
      profileId,
      RATIOS_TOPIC_ID,
      RATIOS_STORY_ID_0,
      RATIOS_EXPLORATION_ID_0,
      timestampOlderThanOneWeek
    )

    recordRecentlyPlayedChapter(
      profileId,
      RATIOS_TOPIC_ID,
      RATIOS_STORY_ID_1,
      RATIOS_EXPLORATION_ID_2,
      timestampOlderThanOneWeek
    )
  }

  /**
   * Marks one explorations in each of the two two test topics as recently played for a
   * particular profile.
   *
   * Note that this function will advance the clock & synchronize all background execution to ensure
   * that any operations needed to achieve the requested state are completed and successful.
   *
   * @param profileId the profile we are setting recently played for
   * @param timestampOlderThanOneWeek if the timestamp for this topic progress is more than one week
   *     ago
   */
  fun markRecentlyPlayedForOneExplorationInTestTopics1And2(
    profileId: ProfileId,
    timestampOlderThanOneWeek: Boolean
  ) {
    recordRecentlyPlayedChapter(
      profileId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      timestampOlderThanOneWeek
    )
    recordRecentlyPlayedChapter(
      profileId,
      TEST_TOPIC_ID_1,
      TEST_STORY_ID_2,
      TEST_EXPLORATION_ID_4,
      timestampOlderThanOneWeek
    )
  }

  private fun recordCompletedChapter(
    profileId: ProfileId,
    topicId: String,
    storyId: String,
    explorationId: String,
    timestampOlderThanOneWeek: Boolean
  ) {
    primeClockForRecordingProgress()
    val resultProvider = storyProgressController.recordCompletedChapter(
      profileId,
      topicId,
      storyId,
      explorationId,
      completionTimestamp = computeTimestamp(timestampOlderThanOneWeek)
    )
    verifyProviderFinishesWithSuccess(resultProvider)
  }

  private fun recordRecentlyPlayedChapter(
    profileId: ProfileId,
    topicId: String,
    storyId: String,
    explorationId: String,
    timestampOlderThanOneWeek: Boolean
  ) {
    primeClockForRecordingProgress()
    val resultProvider = storyProgressController.recordRecentlyPlayedChapter(
      profileId,
      topicId,
      storyId,
      explorationId,
      lastPlayedTimestamp = computeTimestamp(timestampOlderThanOneWeek)
    )
    verifyProviderFinishesWithSuccess(resultProvider)
  }

  private fun primeClockForRecordingProgress() {
    verifyClockMode()

    // Advancing time by 1 millisecond ensures that each recording has a different timestamp so that
    // functionality depending on recording order can operate properly in Robolectric tests.
    testCoroutineDispatchers.advanceTimeBy(delayTimeMillis = 1)
  }

  private fun verifyProviderFinishesWithSuccess(dataProvider: DataProvider<Any?>) {
    // Ensure interactions with LiveData occur on the main thread for Espresso compatibility.
    InstrumentationRegistry.getInstrumentation().runOnMainSync {
      reset(mockLiveDataObserver)
      dataProvider.toLiveData().observeForever(mockLiveDataObserver)
    }

    // Provide time for the data provider to finish.
    testCoroutineDispatchers.runCurrent()

    // Verify that the observer was called, and that the result was successful.
    InstrumentationRegistry.getInstrumentation().runOnMainSync {
      verify(mockLiveDataObserver, atLeastOnce()).onChanged(liveDataResultCaptor.capture())
      assertThat(liveDataResultCaptor.value.isSuccess()).isTrue()
    }
  }

  private fun verifyClockMode() {
    check(fakeOppiaClock.getFakeTimeMode() == FakeOppiaClock.FakeTimeMode.USE_UPTIME_MILLIS) {
      "Proper usage of StoryProgressTestHelper requires using uptime millis otherwise it's " +
        "highly likely tests depending on this utility will be flaky."
    }
  }

  private fun computeTimestamp(timestampOlderThanOneWeek: Boolean): Long {
    return if (!timestampOlderThanOneWeek) {
      retrieveCurrentTimestamp()
    } else {
      retrieveOldTimestamp()
    }
  }

  private fun retrieveCurrentTimestamp(): Long = fakeOppiaClock.getCurrentTimeMs()

  // Returns a timestamp which is at least a week old than current.
  private fun retrieveOldTimestamp(): Long = retrieveCurrentTimestamp() - EIGHT_DAYS_IN_MS
}
