package org.oppia.android.app.story

import org.oppia.android.app.model.ExplorationCheckpoint

/** Listener for cases when the user taps on a specific chapter/exploration to play. */
interface ExplorationSelectionListener {
  /** Called when an exploration has been selected by the user. */
  fun selectExploration(
    internalProfileId: Int,
    topicId: String,
    storyId: String,
    explorationId: String,
    canExplorationBeResumed: Boolean,
    canHavePartialProgressSaved: Boolean,
    backflowId: Int?,
    explorationCheckpoint: ExplorationCheckpoint
  )
}
