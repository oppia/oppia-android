package org.oppia.android.domain.topic

import org.oppia.android.app.model.ProfileId
import java.util.Date
import javax.inject.Inject

private const val EIGHT_DAYS_IN_MS = 8 * 24 * 60 * 60 * 1000

/** This helper allows tests to easily create dummy progress per profile-basis. */
class StoryProgressTestHelper @Inject constructor(
  private val storyProgressController: StoryProgressController
) {

  private fun getCurrentTimestamp(): Long {
    return Date().time
  }

  // Returns a timestamp which is at least a week old than current.
  private fun getOldTimestamp(): Long {
    return Date().time - EIGHT_DAYS_IN_MS
  }

  /**
   * Creates a partial story progress for a particular profile.
   *
   * @param profileId the profile we are setting partial progress of the fraction story for
   * @param timestampOlderThanOneWeek if the timestamp for this topic progress is more than one week ago
   */
  fun markPartialStoryProgressForFractions(
    profileId: ProfileId,
    timestampOlderThanOneWeek: Boolean
  ) {
    val timestamp = if (!timestampOlderThanOneWeek) {
      getCurrentTimestamp()
    } else {
      getOldTimestamp()
    }
    storyProgressController.recordCompletedChapter(
      profileId,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_0,
      timestamp
    )
  }

  /**
   * Creates a partial story progress for a particular profile.
   *
   * @param profileId the profile we are setting partial progress of the fraction story for
   * @param timestampOlderThanOneWeek if the timestamp for this topic progress is more than one week ago
   */
  fun markChapDoneFrac0Story0Exp0(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    val timestamp = if (!timestampOlderThanOneWeek) {
      getCurrentTimestamp()
    } else {
      getOldTimestamp()
    }
    storyProgressController.recordCompletedChapter(
      profileId,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_0,
      timestamp
    )
  }

  /**
   * Mark a partial story progress for a particular profile.
   *
   * @param profileId the profile we are setting partial progress of the fraction story for
   * @param timestampOlderThanOneWeek if the timestamp for this topic progress is more than one week ago
   */
  fun markChapDoneFrac0Story0Expl(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    val timestamp = if (!timestampOlderThanOneWeek) {
      getCurrentTimestamp()
    } else {
      getOldTimestamp()
    }
    storyProgressController.recordCompletedChapter(
      profileId,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_1,
      timestamp
    )
  }

  /**
   * Creates a partial topic progress for a particular profile.
   *
   * @param profileId the profile we are setting partial progress of the fraction topic for
   * @param timestampOlderThanAWeek if the timestamp for this topic progress is more than one week ago
   */
  fun markPartialTopicProgressForFractions(
    profileId: ProfileId,
    timestampOlderThanOneWeek: Boolean
  ) {
    val timestamp = if (!timestampOlderThanOneWeek) {
      getCurrentTimestamp()
    } else {
      getOldTimestamp()
    }
    storyProgressController.recordCompletedChapter(
      profileId,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_0,
      timestamp
    )
  }

  /**
   *  Marks full story progress for a particular profile.
   *
   * @param profileId the profile we are setting full on the fraction story progress for
   * @param timestampOlderThanAWeek if the timestamp for completing the story is more than one week ago
   */
  fun markFullStoryProgressForFractions(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    val timestamp = if (!timestampOlderThanOneWeek) {
      getCurrentTimestamp()
    } else {
      getOldTimestamp()
    }
    storyProgressController.recordCompletedChapter(
      profileId,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_0,
      timestamp
    )

    storyProgressController.recordCompletedChapter(
      profileId,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_1,
      timestamp
    )
  }

  /**
   * Marks full topic progress for a particular profile.
   *
   * @param profileId the profile we are setting fraction topic progress for
   * @param timestampOlderThanOneWeek if the timestamp for completing the topic is more than one week ago
   */
  fun markFullTopicProgressForFractions(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    val timestamp = if (!timestampOlderThanOneWeek) {
      getCurrentTimestamp()
    } else {
      getOldTimestamp()
    }
    storyProgressController.recordCompletedChapter(
      profileId,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_0,
      timestamp
    )
    storyProgressController.recordCompletedChapter(
      profileId,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_1,
      timestamp
    )
  }

  /**
   * Marks full topic progress on all topics for a particular profile.
   *
   * @param profileId the profile we are setting topic progress for
   * @param timestampOlderThanOneWeek if the timestamp for completing the topic is from more than one week ago
   */
  fun markFullProgressForAllTopics(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    markFullTopicProgressForFractions(profileId, timestampOlderThanOneWeek)
    markFullTopicProgressForTestTopics(profileId, timestampOlderThanOneWeek)
    markFullTopicProgressForRatios(profileId, timestampOlderThanOneWeek)
  }

  /**
   * Marks full topic progress on Test Topics for a particular profile.
   *
   * @param profileId the profile we are setting topic progress for
   * @param timestampOlderThanOneWeek if the timestamp for completing the topic is from more than one week ago
   */
  fun markFullTopicProgressForTestTopics(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    val timestamp = if (!timestampOlderThanOneWeek) {
      getCurrentTimestamp()
    } else {
      getOldTimestamp()
    }
    // Stories and Explorations for "Test Topic"s are not in chronological order so we want to ensure
    // that the combinations of Topic / Story / Exploration that are visible will be marked as completed.
    storyProgressController.recordCompletedChapter(
      profileId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      timestamp
    )
    storyProgressController.recordCompletedChapter(
      profileId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_5,
      timestamp
    )
    storyProgressController.recordCompletedChapter(
      profileId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_1,
      TEST_EXPLORATION_ID_3,
      timestamp
    )
    storyProgressController.recordCompletedChapter(
      profileId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_1,
      TEST_EXPLORATION_ID_4,
      timestamp
    )
    storyProgressController.recordCompletedChapter(
      profileId,
      TEST_TOPIC_ID_1,
      TEST_STORY_ID_2,
      TEST_EXPLORATION_ID_4,
      timestamp
    )
    storyProgressController.recordCompletedChapter(
      profileId,
      TEST_TOPIC_ID_1,
      TEST_STORY_ID_2,
      TEST_EXPLORATION_ID_5,
      timestamp
    )
  }

  /**
   * Marks full topic progress on Ratios for a particular profile.
   *
   * @param profileId the profile we are setting topic progress for
   * @param timestampOlderThanOneWeek if the timestamp for completing the topic is from more than one week ago
   */
  fun markFullTopicProgressForRatios(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    val timestamp = if (!timestampOlderThanOneWeek) {
      getCurrentTimestamp()
    } else {
      getOldTimestamp()
    }
    storyProgressController.recordCompletedChapter(
      profileId,
      RATIOS_TOPIC_ID,
      RATIOS_STORY_ID_0,
      RATIOS_EXPLORATION_ID_0,
      timestamp
    )
    storyProgressController.recordCompletedChapter(
      profileId,
      RATIOS_TOPIC_ID,
      RATIOS_STORY_ID_0,
      RATIOS_EXPLORATION_ID_1,
      timestamp
    )
    storyProgressController.recordCompletedChapter(
      profileId,
      RATIOS_TOPIC_ID,
      RATIOS_STORY_ID_1,
      RATIOS_EXPLORATION_ID_2,
      timestamp
    )
    storyProgressController.recordCompletedChapter(
      profileId,
      RATIOS_TOPIC_ID,
      RATIOS_STORY_ID_1,
      RATIOS_EXPLORATION_ID_3,
      timestamp
    )
  }

  /**
   * Marks full topic progress on the second test topic for a particular profile.
   *
   * @param profileId the profile we are setting topic progress for
   * @param timestampOlderThanOneWeek if the timestamp for completing the topic is from more than one week ago
   */
  fun markFullProgressForSecondTestTopic(profileId: ProfileId, timestampOlderThanOneWeek: Boolean) {
    val timestamp = if (!timestampOlderThanOneWeek) {
      getCurrentTimestamp()
    } else {
      getOldTimestamp()
    }
    storyProgressController.recordCompletedChapter(
      profileId,
      TEST_TOPIC_ID_1,
      TEST_STORY_ID_2,
      TEST_EXPLORATION_ID_4,
      timestamp
    )
  }

  /**
   * Marks recently played on the second test topic for a particular profile.
   *
   * @param profileId the profile we are setting topic progress for
   * @param timestampOlderThanOneWeek if the timestamp for completing the topic is from more than one week ago
   */
  fun markRecentlyPlayedForSecondTestTopic(
    profileId: ProfileId,
    timestampOlderThanOneWeek: Boolean
  ) {
    val timestamp = if (!timestampOlderThanOneWeek) {
      getCurrentTimestamp()
    } else {
      getOldTimestamp()
    }
    storyProgressController.recordRecentlyPlayedChapter(
      profileId,
      TEST_TOPIC_ID_1,
      TEST_STORY_ID_2,
      TEST_EXPLORATION_ID_4,
      timestamp
    )
  }

  /**
   * Marks one story progress fully complete in the ratios topic for a particular profile.
   *
   * @param profileId the profile we are setting topic progress on ratios for
   * @param timestampOlderThanAWeek if the timestamp for this progress is from more than one week ago
   */
  fun markFullStoryPartialTopicProgressForRatios(
    profileId: ProfileId,
    timestampOlderThanOneWeek: Boolean
  ) {
    val timestamp = if (!timestampOlderThanOneWeek) {
      getCurrentTimestamp()
    } else {
      getOldTimestamp()
    }
    storyProgressController.recordCompletedChapter(
      profileId,
      RATIOS_TOPIC_ID,
      RATIOS_STORY_ID_0,
      RATIOS_EXPLORATION_ID_0,
      timestamp
    )
    storyProgressController.recordCompletedChapter(
      profileId,
      RATIOS_TOPIC_ID,
      RATIOS_STORY_ID_0,
      RATIOS_EXPLORATION_ID_1,
      timestamp
    )
  }

  /**
   * Marks one story progress full in ratios exploration for a particular profile.
   *
   * @param profileId the profile we are setting topic progress on ratios for
   * @param timestampOlderThanOneWeek if the timestamp for this progress is from more than one week ago
   */
  fun markChapDoneOfRatiosStory0Exp0(
    profileId: ProfileId,
    timestampOlderThanOneWeek: Boolean
  ) {
    val timestamp = if (!timestampOlderThanOneWeek) {
      getCurrentTimestamp()
    } else {
      getOldTimestamp()
    }
    storyProgressController.recordCompletedChapter(
      profileId,
      RATIOS_TOPIC_ID,
      RATIOS_STORY_ID_0,
      RATIOS_EXPLORATION_ID_0,
      timestamp
    )
  }

  /**
   * Marks one story progress full in ratios exploration for a particular profile.
   *
   * @param profileId the profile we are setting topic progress on ratios for
   * @param timestampOlderThanOneWeek if the timestamp for this progress is from more than one week ago
   */
  fun markChapDoneOfRatiosStory0Exp1(
    profileId: ProfileId,
    timestampOlderThanOneWeek: Boolean
  ) {
    val timestamp = if (!timestampOlderThanOneWeek) {
      getCurrentTimestamp()
    } else {
      getOldTimestamp()
    }
    storyProgressController.recordCompletedChapter(
      profileId,
      RATIOS_TOPIC_ID,
      RATIOS_STORY_ID_0,
      RATIOS_EXPLORATION_ID_1,
      timestamp
    )
  }

  /**
   * Marks two partial story progress in ratios exploration for a particular profile.
   *
   * @param profileId the profile we are setting topic progress on ratios for
   * @param timestampOlderThanAWeek if the timestamp for the progress on the two stories is from more than one week
   *        ago.
   */
  fun markTwoPartialStoryProgressForRatios(
    profileId: ProfileId,
    timestampOlderThanOneWeek: Boolean
  ) {
    val timestamp = if (!timestampOlderThanOneWeek) {
      getCurrentTimestamp()
    } else {
      getOldTimestamp()
    }
    storyProgressController.recordCompletedChapter(
      profileId,
      RATIOS_TOPIC_ID,
      RATIOS_STORY_ID_0,
      RATIOS_EXPLORATION_ID_0,
      timestamp
    )

    storyProgressController.recordCompletedChapter(
      profileId,
      RATIOS_TOPIC_ID,
      RATIOS_STORY_ID_1,
      RATIOS_EXPLORATION_ID_2,
      timestamp
    )
  }

  /**
   * Marks exploration [FRACTIONS_EXPLORATION_ID_0] as recently played for a particular profile.
   *
   * @param profileId the profile we are setting recently played for
   * @param timestampOlderThanAWeek if the timestamp for the recently played story is more than a week ago
   */
  fun markRecentlyPlayedForFractionsStory0Exploration0(
    profileId: ProfileId,
    timestampOlderThanOneWeek: Boolean
  ) {
    val timestamp = if (!timestampOlderThanOneWeek) {
      getCurrentTimestamp()
    } else {
      getOldTimestamp()
    }
    storyProgressController.recordRecentlyPlayedChapter(
      profileId,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_0,
      timestamp
    )
  }

  /**
   * Marks exploration [RATIOS_EXPLORATION_ID_0] as recently played for a particular profile.
   *
   * @param profileId the profile we are setting recently played for
   * @param timestampOlderThanAWeek if the timestamp for the recently played story is more than a week ago
   */
  fun markRecentlyPlayedForRatiosStory0Exploration0(
    profileId: ProfileId,
    timestampOlderThanOneWeek: Boolean
  ) {
    val timestamp = if (!timestampOlderThanOneWeek) {
      getCurrentTimestamp()
    } else {
      getOldTimestamp()
    }
    storyProgressController.recordRecentlyPlayedChapter(
      profileId,
      RATIOS_TOPIC_ID,
      RATIOS_STORY_ID_0,
      RATIOS_EXPLORATION_ID_0,
      timestamp
    )
  }

  /**
   *  Marks first exploration in both stories of Ratios as recently played for a particular profile.
   *
   * @param profileId the profile we are setting recently played for
   * @param timestampOlderThanOneWeek if the timestamp for the recently played story and explorations is more than
   *        a week ago
   */
  fun markRecentlyPlayedForRatiosStory0Exploration0AndStory1Exploration2(
    profileId: ProfileId,
    timestampOlderThanOneWeek: Boolean
  ) {
    val timestamp = if (!timestampOlderThanOneWeek) {
      getCurrentTimestamp()
    } else {
      getOldTimestamp()
    }

    storyProgressController.recordRecentlyPlayedChapter(
      profileId,
      RATIOS_TOPIC_ID,
      RATIOS_STORY_ID_0,
      RATIOS_EXPLORATION_ID_0,
      timestamp
    )

    storyProgressController.recordRecentlyPlayedChapter(
      profileId,
      RATIOS_TOPIC_ID,
      RATIOS_STORY_ID_1,
      RATIOS_EXPLORATION_ID_2,
      timestamp
    )
  }

  /**
   * Marks first exploration in all stories of Ratios & Fractions as recently played for a particular profile.
   *
   * @param profileId the profile we are setting recently played for.
   * @param timestampOlderThanAWeek the timestamp for the recently played explorations is more than a week ago.
   */
  fun markRecentlyPlayedForFirstExplorationInAllStoriesInFractionsAndRatios(
    profileId: ProfileId,
    timestampOlderThanOneWeek: Boolean
  ) {
    val timestamp = if (!timestampOlderThanOneWeek) {
      getCurrentTimestamp()
    } else {
      getOldTimestamp()
    }

    storyProgressController.recordRecentlyPlayedChapter(
      profileId,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_0,
      timestamp
    )

    storyProgressController.recordRecentlyPlayedChapter(
      profileId,
      RATIOS_TOPIC_ID,
      RATIOS_STORY_ID_0,
      RATIOS_EXPLORATION_ID_0,
      timestamp
    )

    storyProgressController.recordRecentlyPlayedChapter(
      profileId,
      RATIOS_TOPIC_ID,
      RATIOS_STORY_ID_1,
      RATIOS_EXPLORATION_ID_2,
      timestamp
    )
  }

  /**
   * Marks one explorations in each of the two two test topics as recently played for a particular profile.
   *
   * @param profileId the profile we are setting recently played for
   * @param timestampOlderThanAWeek if the timestamp for the recently played story is more than a week ago
   */
  fun markRecentlyPlayedForOneExplorationInTestTopics1And2(
    profileId: ProfileId,
    timestampOlderThanOneWeek: Boolean
  ) {
    val timestamp = if (!timestampOlderThanOneWeek) {
      getCurrentTimestamp()
    } else {
      getOldTimestamp()
    }
    storyProgressController.recordRecentlyPlayedChapter(
      profileId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      timestamp
    )
    storyProgressController.recordRecentlyPlayedChapter(
      profileId,
      TEST_TOPIC_ID_1,
      TEST_STORY_ID_2,
      TEST_EXPLORATION_ID_4,
      timestamp
    )
  }

  /**
   * Marks one exploration in first test topic as completed played for a particular profile.
   *
   * @param profileId the profile we are setting recently played for
   * @param timestampOlderThanOneWeek if the timestamp for the recently played story is more than a week ago
   */
  fun markChapterDoneFirstTestTopicStory0Exploration0(
    profileId: ProfileId,
    timestampOlderThanOneWeek: Boolean
  ) {
    val timestamp = if (!timestampOlderThanOneWeek) {
      getCurrentTimestamp()
    } else {
      getOldTimestamp()
    }
    storyProgressController.recordCompletedChapter(
      profileId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_2,
      timestamp
    )
  }

  /**
   * Marks one exploration in first test topic as completed played for a particular profile.
   *
   * @param profileId the profile we are setting recently played for
   * @param timestampOlderThanOneWeek if the timestamp for the recently played story is more than a week ago
   */
  fun markRecentlyPlayedFirstTestTopicStory1Exploration1(
    profileId: ProfileId,
    timestampOlderThanOneWeek: Boolean
  ) {
    val timestamp = if (!timestampOlderThanOneWeek) {
      getCurrentTimestamp()
    } else {
      getOldTimestamp()
    }
    storyProgressController.recordRecentlyPlayedChapter(
      profileId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_1,
      TEST_EXPLORATION_ID_1,
      timestamp
    )
  }

  /**
   * Marks one explorations in first test topic as completed for a particular profile.
   *
   * @param profileId the profile we are setting recently played for
   * @param timestampOlderThanOneWeek if the timestamp for the recently played story is more than a week ago
   */
  fun markChapterDoneFirstTestTopicStory0Exploration1(
    profileId: ProfileId,
    timestampOlderThanOneWeek: Boolean
  ) {
    val timestamp = if (!timestampOlderThanOneWeek) {
      getCurrentTimestamp()
    } else {
      getOldTimestamp()
    }
    storyProgressController.recordCompletedChapter(
      profileId,
      TEST_TOPIC_ID_0,
      TEST_STORY_ID_0,
      TEST_EXPLORATION_ID_5,
      timestamp
    )
  }
}
