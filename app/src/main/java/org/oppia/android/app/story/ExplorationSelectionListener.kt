package org.oppia.android.app.story

import org.oppia.android.app.model.ExplorationActivityParams
import org.oppia.android.app.model.ExplorationCheckpoint
import org.oppia.android.app.model.ProfileId

/** Listener for cases when the user taps on a specific chapter/exploration to play. */
interface ExplorationSelectionListener {
  /** Called when an exploration has been selected by the user. */
  fun selectExploration(
    profileId: ProfileId,
    classroomId: String,
    topicId: String,
    storyId: String,
    explorationId: String,
    canExplorationBeResumed: Boolean,
    canHavePartialProgressSaved: Boolean,
    parentScreen: ExplorationActivityParams.ParentScreen,
    explorationCheckpoint: ExplorationCheckpoint
  )
}
