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
import org.oppia.android.domain.topic.TEST_EXPLORATION_ID_13
import org.oppia.android.domain.topic.TEST_EXPLORATION_ID_2
import org.oppia.android.domain.topic.TEST_EXPLORATION_ID_4
import org.oppia.android.domain.topic.TEST_EXPLORATION_ID_5
import org.oppia.android.domain.topic.TEST_STORY_ID_0
import org.oppia.android.domain.topic.TEST_STORY_ID_2
import org.oppia.android.domain.topic.TEST_TOPIC_ID_0
import org.oppia.android.domain.topic.TEST_TOPIC_ID_1
import org.oppia.android.testing.threading.TestCoroutineDispatchers
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

  /* Test topic completion methods. */

  /**
   * Marks the first chapter of test topic 0 story 0 as completed.
   *
   * Note that this function will advance the clock & synchronize all background execution to ensure
   * that any operations needed to achieve the requested state are completed and successful.
   *
   * @param profileId the profile we are setting partial progress of the fraction story for
   * @param timestampOlderThanOneWeek if the timestamp for this topic progress is more than one week
   *     ago
   */
  fun markCompletedTestTopic0Story0Exp0(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    recordCompletedChapter(
      profileId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      timestampOlderThanOneWeek
    )
  }

  /**
   * Marks the second chapter of test topic 0 story 0 as completed, and any needed prerequisites.
   * See [markCompletedTestTopic0Story0Exp0] for specifics on the parameters passed to this method,
   * and any other nuances.
   */
  fun markCompletedTestTopic0Story0Exp1(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    // Must complete prerequisite chapter first.
    markCompletedTestTopic0Story0Exp0(profileId, timestampOlderThanOneWeek)
    recordCompletedChapter(
      profileId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_13,
      timestampOlderThanOneWeek
    )
  }

  /**
   * Marks the third chapter of test topic 0 story 0 as completed, and any needed prerequisites.
   * See [markCompletedTestTopic0Story0Exp0] for specifics on the parameters passed to this method,
   * and any other nuances.
   */
  fun markCompletedTestTopic0Story0Exp2(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    // Must complete prerequisite chapter first.
    markCompletedTestTopic0Story0Exp1(profileId, timestampOlderThanOneWeek)
    recordCompletedChapter(
      profileId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_5,
      timestampOlderThanOneWeek
    )
  }

  /**
   * Marks the only chapter of test topic 1 story 2 as completed, and any needed prerequisites.
   * See [markCompletedTestTopic0Story0Exp0] for specifics on the parameters passed to this method,
   * and any other nuances.
   */
  fun markCompletedTestTopic1Story0Exp0(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    recordCompletedChapter(
      profileId,
      TEST_TOPIC_ID_1,
      TEST_STORY_ID_2,
      TEST_EXPLORATION_ID_4,
      timestampOlderThanOneWeek
    )
  }

  /**
   * Marks test topic 0's story 0 as completed. See [markCompletedTestTopic0Story0Exp0] for
   * specifics on the parameters passed to this method, and any other nuances.
   */
  fun markCompletedTestTopic0Story0(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    // Complete last chapter (+ previous automatically).
    markCompletedTestTopic0Story0Exp2(profileId, timestampOlderThanOneWeek)
  }

  /**
   * Marks test topic 1's story 2 as completed. See [markCompletedTestTopic0Story0Exp0] for
   * specifics on the parameters passed to this method, and any other nuances.
   */
  fun markCompletedTestTopic1Story0(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    markCompletedTestTopic1Story0Exp0(profileId, timestampOlderThanOneWeek)
  }

  /**
   * Marks all stories in topic 0 as completed. See [markCompletedTestTopic0Story0Exp0] for
   * specifics on the parameters passed to this method, and any other nuances.
   */
  fun markCompletedTestTopic0(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    markCompletedTestTopic0Story0(profileId, timestampOlderThanOneWeek)
  }

  /**
   * Marks all stories in topic 1 as completed. See [markCompletedTestTopic0Story0Exp0] for
   * specifics on the parameters passed to this method, and any other nuances.
   */
  fun markCompletedTestTopic1(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    markCompletedTestTopic1Story0(profileId, timestampOlderThanOneWeek)
  }

  /**
   * Marks all test topics as completed. See [markCompletedTestTopic0Story0Exp0] for specifics on
   * the parameters passed to this method, and any other nuances.
   */
  fun markCompletedTestTopics(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    // Stories and Explorations for "Test Topic"s are not in chronological order so we want to
    // ensure that the combinations of Topic / Story / Exploration that are visible will be marked
    // as completed.
    markCompletedTestTopic0(profileId, timestampOlderThanOneWeek)
    markCompletedTestTopic1(profileId, timestampOlderThanOneWeek)
  }

  /* Ratios completion methods. */

  /**
   * Marks the first chapter of ratios topic story 0 as completed, and any needed prerequisites. See
   * [markCompletedTestTopic0Story0Exp0] for specifics on the parameters passed to this method, and
   * any other nuances.
   */
  fun markCompletedRatiosStory0Exp0(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    recordCompletedChapter(
      profileId,
      RATIOS_TOPIC_ID,
      RATIOS_STORY_ID_0,
      RATIOS_EXPLORATION_ID_0,
      timestampOlderThanOneWeek
    )
  }

  /**
   * Marks the second chapter of ratios topic story 0 as completed, and any needed prerequisites.
   * See [markCompletedTestTopic0Story0Exp0] for specifics on the parameters passed to this method,
   * and any other nuances.
   */
  fun markCompletedRatiosStory0Exp1(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    // Must complete prerequisite chapter first.
    markCompletedRatiosStory0Exp0(profileId, timestampOlderThanOneWeek)
    recordCompletedChapter(
      profileId,
      RATIOS_TOPIC_ID,
      RATIOS_STORY_ID_0,
      RATIOS_EXPLORATION_ID_1,
      timestampOlderThanOneWeek
    )
  }

  /**
   * Marks the first chapter of ratios topic story 1 as completed, and any needed prerequisites. See
   * [markCompletedTestTopic0Story0Exp0] for specifics on the parameters passed to this method, and
   * any other nuances.
   */
  fun markCompletedRatiosStory1Exp0(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    recordCompletedChapter(
      profileId,
      RATIOS_TOPIC_ID,
      RATIOS_STORY_ID_1,
      RATIOS_EXPLORATION_ID_2,
      timestampOlderThanOneWeek
    )
  }

  /**
   * Marks the second chapter of ratios topic story 1 as completed, and any needed prerequisites.
   * See [markCompletedTestTopic0Story0Exp0] for specifics on the parameters passed to this method,
   * and any other nuances.
   */
  fun markCompletedRatiosStory1Exp1(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    // Must complete prerequisite chapter first.
    markCompletedRatiosStory1Exp0(profileId, timestampOlderThanOneWeek)
    recordCompletedChapter(
      profileId,
      RATIOS_TOPIC_ID,
      RATIOS_STORY_ID_1,
      RATIOS_EXPLORATION_ID_3,
      timestampOlderThanOneWeek
    )
  }

  /**
   * Marks ratios topic story 0 as completed. See [markCompletedTestTopic0Story0Exp0] for specifics
   * on the parameters passed to this method, and any other nuances.
   */
  fun markCompletedRatiosStory0(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    // Complete last chapter (+ previous automatically).
    markCompletedRatiosStory0Exp1(profileId, timestampOlderThanOneWeek)
  }

  /**
   * Marks ratios topic story 1 as completed. See [markCompletedTestTopic0Story0Exp0] for specifics
   * on the parameters passed to this method, and any other nuances.
   */
  fun markCompletedRatiosStory1(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    // Complete last chapter (+ previous automatically).
    markCompletedRatiosStory1Exp1(profileId, timestampOlderThanOneWeek)
  }

  /**
   * Marks all ratios stories as completed. See [markCompletedTestTopic0Story0Exp0] for specifics on
   * the parameters passed to this method, and any other nuances.
   */
  fun markCompletedRatiosTopic(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    markCompletedRatiosStory0(profileId, timestampOlderThanOneWeek)
    markCompletedRatiosStory1(profileId, timestampOlderThanOneWeek)
  }

  /* Fractions completion methods. */

  /**
   * Marks the first chapter of fractions topic story 0 as completed, and any needed prerequisites.
   * See [markCompletedTestTopic0Story0Exp0] for specifics on the parameters passed to this method,
   * and any other nuances.
   */
  fun markCompletedFractionsStory0Exp0(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    recordCompletedChapter(
      profileId,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_0,
      timestampOlderThanOneWeek
    )
  }

  /**
   * Marks the second chapter of fractions topic story 0 as completed, and any needed prerequisites.
   * See [markCompletedTestTopic0Story0Exp0] for specifics on the parameters passed to this method,
   * and any other nuances.
   */
  fun markCompletedFractionsStory0Exp1(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    // Must complete prerequisite chapter first.
    markCompletedFractionsStory0Exp0(profileId, timestampOlderThanOneWeek)
    recordCompletedChapter(
      profileId,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_1,
      timestampOlderThanOneWeek
    )
  }

  /**
   * Marks fractions topic story 0 as completed. See [markCompletedTestTopic0Story0Exp0] for
   * specifics on the parameters passed to this method, and any other nuances.
   */
  fun markCompletedFractionsStory0(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    // Complete last chapter (+ previous automatically).
    markCompletedFractionsStory0Exp1(profileId, timestampOlderThanOneWeek)
  }

  /**
   * Marks all fractions stories as completed. See [markCompletedTestTopic0Story0Exp0] for specifics
   * on the parameters passed to this method, and any other nuances.
   */
  fun markCompletedFractionsTopic(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    markCompletedFractionsStory0(profileId, timestampOlderThanOneWeek)
  }

  /* Test topic partial completion methods. */

  /**
   * Marks the first chapter of test topic 0 story 0 as in progress saved. Note that this may require
   * completing prerequisite chapters before the chapter can be marked as a prerequisite. See
   * [markCompletedTestTopic0Story0Exp0] for specifics on the parameters passed to this method, and
   * any other nuances.
   *
   * @param profileId the ID corresponding to the profile for which the exploration will be marked
   * @param timestampOlderThanOneWeek if the timestamp for this topic progress is more than one week
   *     ago
   */
  fun markInProgressSavedTestTopic0Story0Exp0(
    profileId: ProfileId,
    timestampOlderThanOneWeek: Boolean
  ) {
    recordChapterAsInProgressSaved(
      profileId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      timestampOlderThanOneWeek
    )
  }

  /**
   * Marks the first chapter of test topic 0 story 0 as in progress not saved. Note that this may require
   * completing prerequisite chapters before the chapter can be marked as a prerequisite. See
   * [markCompletedTestTopic0Story0Exp0] for specifics on the parameters passed to this method, and
   * any other nuances.
   *
   * @param profileId the ID corresponding to the profile for which the exploration will be marked
   * @param timestampOlderThanOneWeek if the timestamp for this topic progress is more than one week
   *     ago
   */
  fun markInProgressNotSavedTestTopic0Story0Exp0(
    profileId: ProfileId,
    timestampOlderThanOneWeek: Boolean
  ) {
    recordChapterAsInProgressNotSaved(
      profileId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      timestampOlderThanOneWeek
    )
  }

  /**
   * Marks the second chapter of test topic 0 story 0 as in progress saved. For specifics on
   * parameters and nuances, see: [markInProgressSavedTestTopic0Story0Exp0].
   */
  fun markInProgressSavedTestTopic0Story0Exp1(
    profileId: ProfileId,
    timestampOlderThanOneWeek: Boolean
  ) {
    // Must complete the previous chapter first.
    markCompletedTestTopic0Story0Exp0(profileId, timestampOlderThanOneWeek)
    recordChapterAsInProgressSaved(
      profileId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_13,
      timestampOlderThanOneWeek
    )
  }

  /**
   * Marks the second chapter of test topic 0 story 0 as in progress not saved. For specifics on
   * parameters and nuances, see: [markInProgressNotSavedTestTopic0Story0Exp0].
   */
  fun markInProgressNotSavedTestTopic0Story0Exp1(
    profileId: ProfileId,
    timestampOlderThanOneWeek: Boolean
  ) {
    // Must complete the previous chapter first.
    markCompletedTestTopic0Story0Exp0(profileId, timestampOlderThanOneWeek)
    recordChapterAsInProgressNotSaved(
      profileId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_13,
      timestampOlderThanOneWeek
    )
  }

  /**
   * Marks the third chapter of test topic 0 story 0 as in progress saved. For specifics on
   * parameters and nuances, see: [markInProgressSavedTestTopic0Story0Exp0].
   */
  fun markInProgressSavedTestTopic0Story0Exp2(
    profileId: ProfileId,
    timestampOlderThanOneWeek: Boolean
  ) {
    // Must complete the previous chapter first.
    markCompletedTestTopic0Story0Exp1(profileId, timestampOlderThanOneWeek)
    recordChapterAsInProgressSaved(
      profileId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_5,
      timestampOlderThanOneWeek
    )
  }

  /**
   * Marks the third chapter of test topic 0 story 0 as in progress not saved. For specifics on
   * parameters and nuances, see: [markInProgressNotSavedTestTopic0Story0Exp0].
   */
  fun markInProgressNotSavedTestTopic0Story0Exp2(
    profileId: ProfileId,
    timestampOlderThanOneWeek: Boolean
  ) {
    // Must complete the previous chapter first.
    markCompletedTestTopic0Story0Exp1(profileId, timestampOlderThanOneWeek)
    recordChapterAsInProgressNotSaved(
      profileId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_5,
      timestampOlderThanOneWeek
    )
  }

  /**
   * Marks the only chapter of test topic 1 story 2 as in progress saved. For specifics on parameters
   * and nuances, see: [markInProgressSavedTestTopic0Story0Exp0].
   */
  fun markInProgressSavedTestTopic1Story2Exp0(
    profileId: ProfileId,
    timestampOlderThanOneWeek: Boolean
  ) {
    recordChapterAsInProgressSaved(
      profileId,
      TEST_TOPIC_ID_1,
      TEST_STORY_ID_2,
      TEST_EXPLORATION_ID_4,
      timestampOlderThanOneWeek
    )
  }

  /**
   * Marks the only chapter of test topic 1 story 2 as in progress not saved. For specifics on parameters
   * and nuances, see: [markInProgressNotSavedTestTopic0Story0Exp0].
   */
  fun markInProgressNotSavedTestTopic1Story2Exp0(
    profileId: ProfileId,
    timestampOlderThanOneWeek: Boolean
  ) {
    recordChapterAsInProgressNotSaved(
      profileId,
      TEST_TOPIC_ID_1,
      TEST_STORY_ID_2,
      TEST_EXPLORATION_ID_4,
      timestampOlderThanOneWeek
    )
  }

  /**
   * Marks test topic 0's story 0 as in progress saved. For specifics on parameters and nuances, see:
   * [markInProgressSavedTestTopic0Story0Exp0].
   */
  fun markInProgressSavedTestTopic0Story0(
    profileId: ProfileId,
    timestampOlderThanOneWeek: Boolean
  ) {
    markInProgressSavedTestTopic0Story0Exp0(profileId, timestampOlderThanOneWeek)
  }

  /**
   * Marks test topic 0's story 0 as in progress not saved. For specifics on parameters and nuances, see:
   * [markInProgressNotSavedTestTopic0Story0Exp0].
   */
  fun markInProgressNotSavedTestTopic0Story0(
    profileId: ProfileId,
    timestampOlderThanOneWeek: Boolean
  ) {
    markInProgressNotSavedTestTopic0Story0Exp0(profileId, timestampOlderThanOneWeek)
  }

  /**
   * Marks test topic 1's story 2 as in progress saved. For specifics on parameters and nuances, see:
   * [markInProgressSavedTestTopic0Story0Exp0].
   */
  fun markInProgressSavedTestTopic1Story0(
    profileId: ProfileId,
    timestampOlderThanOneWeek: Boolean
  ) {
    markInProgressSavedTestTopic1Story2Exp0(profileId, timestampOlderThanOneWeek)
  }

  /**
   * Marks test topic 1's story 2 as in progress not saved. For specifics on parameters and nuances, see:
   * [markInProgressNotSavedTestTopic0Story0Exp0].
   */
  fun markInProgressNotSavedTestTopic1Story0(
    profileId: ProfileId,
    timestampOlderThanOneWeek: Boolean
  ) {
    markInProgressNotSavedTestTopic1Story2Exp0(profileId, timestampOlderThanOneWeek)
  }

  /**
   * Marks test topic 0 as in progress saved. For specifics on parameters and nuances, see:
   * [markInProgressSavedTestTopic0Story0Exp0].
   */
  fun markInProgressSavedTestTopic0(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    markInProgressSavedTestTopic0Story0(profileId, timestampOlderThanOneWeek)
  }

  /**
   * Marks test topic 0 as in progress not saved. For specifics on parameters and nuances, see:
   * [markInProgressNotSavedTestTopic0Story0Exp0].
   */
  fun markInProgressNotSavedTestTopic0(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    markInProgressNotSavedTestTopic0Story0(profileId, timestampOlderThanOneWeek)
  }

  /**
   * Marks test topic 1 as in progress saved. For specifics on parameters and nuances, see:
   * [markInProgressSavedTestTopic0Story0Exp0].
   */
  fun markInProgressSavedTestTopic1(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    markInProgressSavedTestTopic1Story0(profileId, timestampOlderThanOneWeek)
  }

  /**
   * Marks test topic 1 as in progress not saved. For specifics on parameters and nuances, see:
   * [markInProgressNotSavedTestTopic0Story0Exp0].
   */
  fun markInProgressNotSavedTestTopic1(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    markInProgressNotSavedTestTopic1Story0(profileId, timestampOlderThanOneWeek)
  }

  /**
   * Marks all test topics as in progress saved. See [markCompletedTestTopic0Story0Exp0] for specifics
   * on the parameters passed to this method, and any other nuances.
   */
  fun markInProgressSavedTestTopics(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    markInProgressSavedTestTopic0(profileId, timestampOlderThanOneWeek)
  }

  /**
   * Marks all test topics as in progress not saved. See [markCompletedTestTopic0Story0Exp0] for specifics
   * on the parameters passed to this method, and any other nuances.
   */
  fun markInProgressNotSavedTestTopics(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    markInProgressNotSavedTestTopic0(profileId, timestampOlderThanOneWeek)
  }

  /* Ratios partial completion methods. */

  /**
   * Marks the first chapter of ratios story 0 as in progress saved. For specifics on parameters and
   * nuances, see: [markInProgressSavedTestTopic0Story0Exp0].
   */
  fun markInProgressSavedRatiosStory0Exp0(
    profileId: ProfileId,
    timestampOlderThanOneWeek: Boolean
  ) {
    recordChapterAsInProgressSaved(
      profileId,
      RATIOS_TOPIC_ID,
      RATIOS_STORY_ID_0,
      RATIOS_EXPLORATION_ID_0,
      timestampOlderThanOneWeek
    )
  }

  /**
   * Marks the first chapter of ratios story 0 as in progress not saved. For specifics on parameters and
   * nuances, see: [markInProgressNotSavedTestTopic0Story0Exp0].
   */
  fun markInProgressNotSavedRatiosStory0Exp0(
    profileId: ProfileId,
    timestampOlderThanOneWeek: Boolean
  ) {
    recordChapterAsInProgressNotSaved(
      profileId,
      RATIOS_TOPIC_ID,
      RATIOS_STORY_ID_0,
      RATIOS_EXPLORATION_ID_0,
      timestampOlderThanOneWeek
    )
  }

  /**
   * Marks the second chapter of ratios story 0 as in progress saved. For specifics on parameters and
   * nuances, see: [markInProgressSavedTestTopic0Story0Exp0].
   */
  fun markInProgressSavedRatiosStory0Exp1(
    profileId: ProfileId,
    timestampOlderThanOneWeek: Boolean
  ) {
    // Must complete the previous chapters first.
    markCompletedRatiosStory0Exp0(profileId, timestampOlderThanOneWeek)
    recordChapterAsInProgressSaved(
      profileId,
      RATIOS_TOPIC_ID,
      RATIOS_STORY_ID_0,
      RATIOS_EXPLORATION_ID_1,
      timestampOlderThanOneWeek
    )
  }

  /**
   * Marks the second chapter of ratios story 0 as in progress not saved. For specifics on parameters and
   * nuances, see: [markInProgressNotSavedTestTopic0Story0Exp0].
   */
  fun markInProgressNotSavedRatiosStory0Exp1(
    profileId: ProfileId,
    timestampOlderThanOneWeek: Boolean
  ) {
    // Must complete the previous chapters first.
    markCompletedRatiosStory0Exp0(profileId, timestampOlderThanOneWeek)
    recordChapterAsInProgressNotSaved(
      profileId,
      RATIOS_TOPIC_ID,
      RATIOS_STORY_ID_0,
      RATIOS_EXPLORATION_ID_1,
      timestampOlderThanOneWeek
    )
  }

  /**
   * Marks the first chapter of ratios story 1 as in progress saved. For specifics on parameters and
   * nuances, see: [markInProgressSavedTestTopic0Story0Exp0].
   */
  fun markInProgressSavedRatiosStory1Exp0(
    profileId: ProfileId,
    timestampOlderThanOneWeek: Boolean
  ) {
    recordChapterAsInProgressSaved(
      profileId,
      RATIOS_TOPIC_ID,
      RATIOS_STORY_ID_1,
      RATIOS_EXPLORATION_ID_2,
      timestampOlderThanOneWeek
    )
  }

  /**
   * Marks the first chapter of ratios story 1 as in progress not saved. For specifics on parameters and
   * nuances, see: [markInProgressNotSavedTestTopic0Story0Exp0].
   */
  fun markInProgressNotSavedRatiosStory1Exp0(
    profileId: ProfileId,
    timestampOlderThanOneWeek: Boolean
  ) {
    recordChapterAsInProgressNotSaved(
      profileId,
      RATIOS_TOPIC_ID,
      RATIOS_STORY_ID_1,
      RATIOS_EXPLORATION_ID_2,
      timestampOlderThanOneWeek
    )
  }

  /**
   * Marks the second chapter of ratios story 1 as in progress saved. For specifics on parameters and
   * nuances, see: [markInProgressSavedTestTopic0Story0Exp0].
   */
  fun markInProgressSavedRatiosStory1Exp1(
    profileId: ProfileId,
    timestampOlderThanOneWeek: Boolean
  ) {
    // Must complete the previous chapters first.
    markCompletedRatiosStory1Exp0(profileId, timestampOlderThanOneWeek)
    recordChapterAsInProgressSaved(
      profileId,
      RATIOS_TOPIC_ID,
      RATIOS_STORY_ID_1,
      RATIOS_EXPLORATION_ID_3,
      timestampOlderThanOneWeek
    )
  }

  /**
   * Marks the second chapter of ratios story 1 as in progress not saved. For specifics on parameters and
   * nuances, see: [markInProgressNotSavedTestTopic0Story0Exp0].
   */
  fun markInProgressNotSavedRatiosStory1Exp1(
    profileId: ProfileId,
    timestampOlderThanOneWeek: Boolean
  ) {
    // Must complete the previous chapters first.
    markCompletedRatiosStory1Exp0(profileId, timestampOlderThanOneWeek)
    recordChapterAsInProgressNotSaved(
      profileId,
      RATIOS_TOPIC_ID,
      RATIOS_STORY_ID_1,
      RATIOS_EXPLORATION_ID_3,
      timestampOlderThanOneWeek
    )
  }

  /**
   * Marks ratios story 0 as in progress saved. For specifics on parameters and nuances, see:
   * [markInProgressSavedTestTopic0Story0Exp0].
   */
  fun markInProgressSavedRatiosStory0(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    markInProgressSavedRatiosStory0Exp0(profileId, timestampOlderThanOneWeek)
  }

  /**
   * Marks ratios story 0 as in progress not saved. For specifics on parameters and nuances, see:
   * [markInProgressNotSavedTestTopic0Story0Exp0].
   */
  fun markInProgressNotSavedRatiosStory0(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    markInProgressNotSavedRatiosStory0Exp0(profileId, timestampOlderThanOneWeek)
  }

  /**
   * Marks ratios story 1 as in progress saved. For specifics on parameters and nuances, see:
   * [markInProgressSavedTestTopic0Story0Exp0].
   */
  fun markInProgressSavedRatiosStory1(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    markInProgressSavedRatiosStory1Exp0(profileId, timestampOlderThanOneWeek)
  }

  /**
   * Marks ratios story 1 as in progress not saved. For specifics on parameters and nuances, see:
   * [markInProgressNotSavedTestTopic0Story0Exp0].
   */
  fun markInProgressNotSavedRatiosStory1(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    markInProgressNotSavedRatiosStory1Exp0(profileId, timestampOlderThanOneWeek)
  }

  /**
   * Marks the ratios topic as in progress saved. For specifics on parameters and nuances, see:
   * [markInProgressSavedTestTopic0Story0Exp0].
   */
  fun markInProgressSavedRatios(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    markInProgressSavedRatiosStory0(profileId, timestampOlderThanOneWeek)
  }

  /**
   * Marks the ratios topic as in progress not saved. For specifics on parameters and nuances, see:
   * [markInProgressNotSavedTestTopic0Story0Exp0].
   */
  fun markInProgressNotSavedRatios(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    markInProgressNotSavedRatiosStory0(profileId, timestampOlderThanOneWeek)
  }

  /* Fractions partial completion methods. */

  /**
   * Marks the first chapter of fractions story 0 as in progress saved. For specifics on parameters
   * and nuances, see: [markInProgressSavedTestTopic0Story0Exp0].
   */
  fun markInProgressSavedFractionsStory0Exp0(
    profileId: ProfileId,
    timestampOlderThanOneWeek: Boolean
  ) {
    recordChapterAsInProgressSaved(
      profileId,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_0,
      timestampOlderThanOneWeek
    )
  }

  /**
   * Marks the first chapter of fractions story 0 as in progress not saved. For specifics on parameters
   * and nuances, see: [markInProgressNotSavedTestTopic0Story0Exp0].
   */
  fun markInProgressNotSavedFractionsStory0Exp0(
    profileId: ProfileId,
    timestampOlderThanOneWeek: Boolean
  ) {
    recordChapterAsInProgressNotSaved(
      profileId,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_0,
      timestampOlderThanOneWeek
    )
  }

  /**
   * Marks the second chapter of fractions story 0 as in progress saved. For specifics on parameters
   * and nuances, see: [markInProgressSavedTestTopic0Story0Exp0].
   */
  fun markInProgressSavedFractionsStory0Exp1(
    profileId: ProfileId,
    timestampOlderThanOneWeek: Boolean
  ) {
    // Must complete the previous chapters first.
    markCompletedFractionsStory0Exp0(profileId, timestampOlderThanOneWeek)
    recordChapterAsInProgressSaved(
      profileId,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_1,
      timestampOlderThanOneWeek
    )
  }

  /**
   * Marks the second chapter of fractions story 0 as in progress not saved. For specifics on parameters
   * and nuances, see: [markInProgressNotSavedTestTopic0Story0Exp0].
   */
  fun markInProgressNotSavedFractionsStory0Exp1(
    profileId: ProfileId,
    timestampOlderThanOneWeek: Boolean
  ) {
    // Must complete the previous chapters first.
    markCompletedFractionsStory0Exp0(profileId, timestampOlderThanOneWeek)
    recordChapterAsInProgressNotSaved(
      profileId,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_1,
      timestampOlderThanOneWeek
    )
  }

  /**
   * Marks fractions story 0 as in progress saved. For specifics on parameters and nuances, see:
   * [markInProgressSavedTestTopic0Story0Exp0].
   */
  fun markInProgressSavedFractionsStory0(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    markInProgressSavedFractionsStory0Exp0(profileId, timestampOlderThanOneWeek)
  }

  /**
   * Marks fractions story 0 as in progress not saved. For specifics on parameters and nuances, see:
   * [markInProgressNotSavedTestTopic0Story0Exp0].
   */
  fun markInProgressNotSavedFractionsStory0(
    profileId: ProfileId,
    timestampOlderThanOneWeek: Boolean
  ) {
    markInProgressNotSavedFractionsStory0Exp0(profileId, timestampOlderThanOneWeek)
  }

  /**
   * Marks the fractions topic as in progress saved. For specifics on parameters and nuances, see:
   * [markInProgressSavedTestTopic0Story0Exp0].
   */
  fun markInProgressSavedFractions(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    markInProgressSavedFractionsStory0(profileId, timestampOlderThanOneWeek)
  }

  /**
   * Marks the fractions topic as in progress not saved. For specifics on parameters and nuances, see:
   * [markInProgressNotSavedTestTopic0Story0Exp0].
   */
  fun markInProgressNotSavedFractions(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    markInProgressNotSavedFractionsStory0(profileId, timestampOlderThanOneWeek)
  }

  /* Cross-topics functions. */

  /**
   * Marks all lessons as completed. See [markCompletedTestTopic0Story0Exp0] for specifics on the
   * parameters passed to this method, and any other nuances.
   */
  fun markAllTopicsAsCompleted(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    markCompletedTestTopics(profileId, timestampOlderThanOneWeek)
    markCompletedRatiosTopic(profileId, timestampOlderThanOneWeek)
    markCompletedFractionsTopic(profileId, timestampOlderThanOneWeek)
  }

  /**
   * Marks all lessons as in_progress_saved. For specifics on parameters and nuances, see:
   * [markInProgressSavedTestTopic0Story0Exp0].
   */
  fun markAllTopicsAsInProgressSaved(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    markInProgressSavedTestTopic0(profileId, timestampOlderThanOneWeek)
    markInProgressSavedTestTopic1(profileId, timestampOlderThanOneWeek)
    markInProgressSavedRatios(profileId, timestampOlderThanOneWeek)
    markInProgressSavedFractions(profileId, timestampOlderThanOneWeek)
  }

  /**
   * Marks all lessons as in_progress_not_saved. For specifics on parameters and nuances, see:
   * [markInProgressNotSavedTestTopic0Story0Exp0].
   */
  fun markAllTopicsAsInProgressNotSaved(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    markInProgressNotSavedTestTopic0(profileId, timestampOlderThanOneWeek)
    markInProgressNotSavedTestTopic1(profileId, timestampOlderThanOneWeek)
    markInProgressNotSavedRatios(profileId, timestampOlderThanOneWeek)
    markInProgressNotSavedFractions(profileId, timestampOlderThanOneWeek)
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

  private fun recordChapterAsInProgressNotSaved(
    profileId: ProfileId,
    topicId: String,
    storyId: String,
    explorationId: String,
    timestampOlderThanOneWeek: Boolean
  ) {
    primeClockForRecordingProgress()
    val resultProvider = storyProgressController.recordChapterAsInProgressNotSaved(
      profileId,
      topicId,
      storyId,
      explorationId,
      lastPlayedTimestamp = computeTimestamp(timestampOlderThanOneWeek)
    )
    verifyProviderFinishesWithSuccess(resultProvider)
  }

  private fun recordChapterAsInProgressSaved(
    profileId: ProfileId,
    topicId: String,
    storyId: String,
    explorationId: String,
    timestampOlderThanOneWeek: Boolean
  ) {
    primeClockForRecordingProgress()
    val resultProvider = storyProgressController.recordChapterAsInProgressSaved(
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
      assertThat(liveDataResultCaptor.value is AsyncResult.Success).isTrue()
    }
  }

  private fun verifyClockMode() {
    check(fakeOppiaClock.getFakeTimeMode() == FakeOppiaClock.FakeTimeMode.MODE_UPTIME_MILLIS) {
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
