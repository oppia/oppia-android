package org.oppia.domain.topic

import org.oppia.app.model.ProfileId
import java.util.Date
import javax.inject.Inject

private const val EIGHT_DAYS_IN_MS = 8 * 24 * 60 * 60 * 1000

/** This helper allows tests to easily create dummy progress per profile-basis. */
class StoryProgressTestHelper @Inject constructor(private val storyProgressController: StoryProgressController) {

  private fun getCurrentTimestamp(): Long {
    return Date().time
  }

  // Returns a timestamp which is atleast a week old than current.
  private fun getOldTimestamp(): Long {
    return Date().time - EIGHT_DAYS_IN_MS
  }

  /** Creates a partial story progress for a particular profile. */
  fun markPartialStoryProgressForFractions(profileId: ProfileId, timestampOlderThanAWeek: Boolean) {
    val timestamp = if (!timestampOlderThanAWeek) {
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

  /** Creates a partial topic progress for a particular profile. */
  fun markPartialTopicProgressForFractions(profileId: ProfileId, timestampOlderThanAWeek: Boolean) {
    val timestamp = if (!timestampOlderThanAWeek) {
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

  /** Marks full story progress for a particular profile. */
  fun markFullStoryProgressForFractions(profileId: ProfileId, timestampOlderThanAWeek: Boolean) {
    val timestamp = if (!timestampOlderThanAWeek) {
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

  /** Marks full topic progress for a particular profile. */
  fun markFullTopicProgressForFractions(profileId: ProfileId, timestampOlderThanAWeek: Boolean) {
    val timestamp = if (!timestampOlderThanAWeek) {
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

  /** Marks one story progress full in ratios exploration for a particular profile. */
  fun markFullStoryPartialTopicProgressForRatios(profileId: ProfileId, timestampOlderThanAWeek: Boolean) {
    val timestamp = if (!timestampOlderThanAWeek) {
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

  /** Marks two partial story progress in ratios exploration for a particular profile. */
  fun markTwoPartialStoryProgressForRatios(profileId: ProfileId, timestampOlderThanAWeek: Boolean) {
    val timestamp = if (!timestampOlderThanAWeek) {
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

  /** Marks exploration [FRACTIONS_EXPLORATION_ID_0] as recently played for a particular profile. */
  fun markRecentlyPlayedForFractionsStory0Exploration0(profileId: ProfileId, timestampOlderThanAWeek: Boolean) {
    val timestamp = if (!timestampOlderThanAWeek) {
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

  /** Marks exploration [RATIOS_EXPLORATION_ID_0] as recently played for a particular profile. */
  fun markRecentlyPlayedForRatiosStory0Exploration0(profileId: ProfileId, timestampOlderThanAWeek: Boolean) {
    val timestamp = if (!timestampOlderThanAWeek) {
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

  /** Marks first exploration in both stories of Ratios as recently played for a particular profile. */
  fun markRecentlyPlayedForRatiosStory0Exploration0AndStory1Exploration2(
    profileId: ProfileId,
    timestampOlderThanAWeek: Boolean
  ) {
    val timestamp = if (!timestampOlderThanAWeek) {
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

  /** Marks first exploration in all stories of Ratios & Fractions as recently played for a particular profile. */
  fun markRecentlyPlayedForFirstExplorationInAllStoriesInFractionsAndRatios(
    profileId: ProfileId,
    timestampOlderThanAWeek: Boolean
  ) {
    val timestamp = if (!timestampOlderThanAWeek) {
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
}
