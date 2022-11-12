package org.oppia.android.domain.exploration

import org.oppia.android.app.model.Exploration

/** Internal class for actually retrieving an exploration object for uses in domain controllers. */
interface ExplorationRetriever {
  /** Loads and returns an exploration for the specified exploration ID, or fails. */
  suspend fun loadExploration(explorationId: String): Exploration
}
