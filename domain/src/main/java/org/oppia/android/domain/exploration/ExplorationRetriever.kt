package org.oppia.android.domain.exploration

import org.oppia.android.app.model.Exploration

/** Internal class for actually retrieving an exploration object for uses in domain controllers. */
interface ExplorationRetriever {
  /** Loads and returns an exploration for the specified exploration ID, or fails. */
  suspend fun loadExploration(explorationId: String): Exploration
  /** Loads and returns Pair of Total number of states for the specified exploration ID
   * and a Mutable Map of State Names and their position in the exploration list
   */
  suspend fun loadExplorationPosition(explorationId: String): Pair<Int, MutableMap<String, Int>>
}
