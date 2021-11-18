package org.oppia.android.app.player.exploration

import org.oppia.android.app.model.State
import org.oppia.android.app.model.WrittenTranslationContext

/** Listener for fetching current exploration state data. */
interface HintsAndSolutionExplorationManagerListener {
  fun onExplorationStateLoaded(state: State, writtenTranslationContext: WrittenTranslationContext)
}
