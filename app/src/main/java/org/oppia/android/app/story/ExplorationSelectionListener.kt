package org.oppia.android.app.story

import org.oppia.android.app.player.exploration.ParentScreenForExplorationEnum

/** Listener for cases when the user taps on a specific chapter/exploration to play. */
interface ExplorationSelectionListener {
  /** Called when an exploration has been selected by the user. */
  fun selectExploration(
    internalProfileId: Int,
    topicId: String,
    storyId: String,
    explorationId: String,
    parentScreenForExplorationEnum: ParentScreenForExplorationEnum
  )
}
