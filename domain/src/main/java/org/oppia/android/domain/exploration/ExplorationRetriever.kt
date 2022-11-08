package org.oppia.android.domain.exploration

import org.oppia.android.app.model.Exploration

interface ExplorationRetriever {
  /** Loads and returns an exploration for the specified exploration ID, or fails. */
  suspend fun loadExploration(explorationId: String): Exploration
}
