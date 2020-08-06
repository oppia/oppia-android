package org.oppia.domain.topic

import org.oppia.app.model.ProfileId
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

  // Returns a timestamp which is atleast a week old than current.
  private fun getOldTimestamp(): Long {
    return Date().time - EIGHT_DAYS_IN_MS
  }

  /** Creates a partial story progress for a particular profile. */
  fun markOngoingTopicList(profileId: ProfileId, timestampOlderThanAWeek: Boolean) {
    val placeValuesTopic = "iX9kYCjnouWN"
    val placeValuesStory = "RRVMHsZ5Mobh"
    val placeValuesExploration0 = "K645IfRNzpKy"
    val placeValuesExploration1 = "Knvx24p24qPO"
    val placeValuesExploration2 = "aAkDKVDR53cG"
    val placeValuesExploration3 = "avwshGklKLJE"

    val multiplicationTopic = "C4fqwrvqWpRm"
    val multiplicationStory = "vfJDB3JAdwIx"
    val multiplicationExploration0 = "R7WpsSfmDQPV"
    val multiplicationExploration1 = "zIBYaqfDJrJC"
    val multiplicationExploration2 = "1904tpP0CYwY"
    val multiplicationExploration3 = "cQDibOXQbpi7"
    val multiplicationExploration4 = "MRJeVrKafW6G"
    val multiplicationExploration5 = "hNOP3TwRJhsz"
    val multiplicationExploration6 = "zTg2hzTz37jP"

    val timestamp = if (!timestampOlderThanAWeek) {
      getCurrentTimestamp()
    } else {
      getOldTimestamp()
    }
    storyProgressController.recordCompletedChapter(
      profileId,
      placeValuesTopic,
      placeValuesStory,
      placeValuesExploration0,
      timestamp
    )

    storyProgressController.recordCompletedChapter(
      profileId,
      multiplicationTopic,
      multiplicationStory,
      multiplicationExploration0,
      timestamp
    )
  }

  fun markCompletedStoryList(profileId: ProfileId, timestampOlderThanAWeek: Boolean) {
    val placeValuesTopic = "iX9kYCjnouWN"
    val placeValuesStory = "RRVMHsZ5Mobh"
    val placeValuesExploration0 = "K645IfRNzpKy"
    val placeValuesExploration1 = "Knvx24p24qPO"
    val placeValuesExploration2 = "aAkDKVDR53cG"
    val placeValuesExploration3 = "avwshGklKLJE"

    val multiplicationTopic = "C4fqwrvqWpRm"
    val multiplicationStory = "vfJDB3JAdwIx"
    val multiplicationExploration0 = "R7WpsSfmDQPV"
    val multiplicationExploration1 = "zIBYaqfDJrJC"
    val multiplicationExploration2 = "1904tpP0CYwY"
    val multiplicationExploration3 = "cQDibOXQbpi7"
    val multiplicationExploration4 = "MRJeVrKafW6G"
    val multiplicationExploration5 = "hNOP3TwRJhsz"
    val multiplicationExploration6 = "zTg2hzTz37jP"

    val timestamp = if (!timestampOlderThanAWeek) {
      getCurrentTimestamp()
    } else {
      getOldTimestamp()
    }
    storyProgressController.recordCompletedChapter(
      profileId,
      placeValuesTopic,
      placeValuesStory,
      placeValuesExploration0,
      timestamp
    )

    storyProgressController.recordCompletedChapter(
      profileId,
      placeValuesTopic,
      placeValuesStory,
      placeValuesExploration1,
      timestamp
    )

    storyProgressController.recordCompletedChapter(
      profileId,
      placeValuesTopic,
      placeValuesStory,
      placeValuesExploration2,
      timestamp
    )

    storyProgressController.recordCompletedChapter(
      profileId,
      placeValuesTopic,
      placeValuesStory,
      placeValuesExploration3,
      timestamp
    )

    storyProgressController.recordCompletedChapter(
      profileId,
      multiplicationTopic,
      multiplicationStory,
      multiplicationExploration0,
      timestamp
    )

    storyProgressController.recordCompletedChapter(
      profileId,
      multiplicationTopic,
      multiplicationStory,
      multiplicationExploration1,
      timestamp
    )

    storyProgressController.recordCompletedChapter(
      profileId,
      multiplicationTopic,
      multiplicationStory,
      multiplicationExploration2,
      timestamp
    )

    storyProgressController.recordCompletedChapter(
      profileId,
      multiplicationTopic,
      multiplicationStory,
      multiplicationExploration3,
      timestamp
    )

    storyProgressController.recordCompletedChapter(
      profileId,
      multiplicationTopic,
      multiplicationStory,
      multiplicationExploration4,
      timestamp
    )

    storyProgressController.recordCompletedChapter(
      profileId,
      multiplicationTopic,
      multiplicationStory,
      multiplicationExploration5,
      timestamp
    )

    storyProgressController.recordCompletedChapter(
      profileId,
      multiplicationTopic,
      multiplicationStory,
      multiplicationExploration6,
      timestamp
    )
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
  fun markFullStoryPartialTopicProgressForRatios(
    profileId: ProfileId,
    timestampOlderThanAWeek: Boolean
  ) {
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
  fun markRecentlyPlayedForFractionsStory0Exploration0(
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
  }

  /** Marks exploration [RATIOS_EXPLORATION_ID_0] as recently played for a particular profile. */
  fun markRecentlyPlayedForRatiosStory0Exploration0(
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
