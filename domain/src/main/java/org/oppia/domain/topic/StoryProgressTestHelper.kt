package org.oppia.domain.topic

import org.oppia.app.model.ProfileId
import javax.inject.Inject

/** This helper allows tests to easily create dummy progress per profile-basis. */
class StoryProgressTestHelper @Inject constructor(private val storyProgressController: StoryProgressController) {

  /** Creates a partial story progress for a particular profile. */
  fun markPartialStoryProgressForFractions(profileId: ProfileId) {
    storyProgressController.recordCompletedChapter(
      profileId,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_0
    )
  }

  /** Creates a partial topic progress for a particular profile. */
  fun markPartialTopicProgressForFractions(profileId: ProfileId) {
    storyProgressController.recordCompletedChapter(
      profileId,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_0
    )
  }

  /** Marks full story progress for a particular profile. */
  fun markFullStoryProgressForFractions(profileId: ProfileId) {
    storyProgressController.recordCompletedChapter(
      profileId,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_0
    )

    storyProgressController.recordCompletedChapter(
      profileId,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_1
    )
  }

  /** Marks full topic progress for a particular profile. */
  fun markFullTopicProgressForFractions(profileId: ProfileId) {
    storyProgressController.recordCompletedChapter(
      profileId,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_0
    )

    storyProgressController.recordCompletedChapter(
      profileId,
      FRACTIONS_TOPIC_ID,
      FRACTIONS_STORY_ID_0,
      FRACTIONS_EXPLORATION_ID_1
    )
  }

  /** Marks one story progress full in ratios exploration for a particular profile. */
  fun markFullStoryPartialTopicProgressForRatios(profileId: ProfileId) {
    storyProgressController.recordCompletedChapter(
      profileId,
      RATIOS_TOPIC_ID,
      RATIOS_STORY_ID_0,
      RATIOS_EXPLORATION_ID_0
    )

    storyProgressController.recordCompletedChapter(
      profileId,
      RATIOS_TOPIC_ID,
      RATIOS_STORY_ID_0,
      RATIOS_EXPLORATION_ID_1
    )
  }

  /** Marks two partial story progress in ratios exploration for a particular profile. */
  fun markTwoPartialStoryProgressForRatios(profileId: ProfileId) {
    storyProgressController.recordCompletedChapter(
      profileId,
      RATIOS_TOPIC_ID,
      RATIOS_STORY_ID_0,
      RATIOS_EXPLORATION_ID_0
    )

    storyProgressController.recordCompletedChapter(
      profileId,
      RATIOS_TOPIC_ID,
      RATIOS_STORY_ID_1,
      RATIOS_EXPLORATION_ID_2
    )
  }
}
