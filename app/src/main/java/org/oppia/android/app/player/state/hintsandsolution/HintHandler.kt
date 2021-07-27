package org.oppia.android.app.player.state.hintsandsolution

import org.oppia.android.app.model.PendingState
import org.oppia.android.app.model.State

/** Handler for showing hints to the learner. */
interface HintHandler {

  /** Resets this handler to prepare it for a new state, cancelling any pending hints. */
  fun reset()

  /** Hide hint when moving to any previous state. */
  fun hideHint()

  /** Schedules a hint to be shown to the user if hints are available. */
  fun maybeScheduleShowHint(state: State, pendingState: PendingState)
}
