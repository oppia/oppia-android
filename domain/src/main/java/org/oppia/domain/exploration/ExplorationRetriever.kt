package org.oppia.domain.exploration

import org.json.JSONObject
import org.oppia.app.model.Exploration
import org.oppia.app.model.State
import org.oppia.domain.util.JsonAssetRetriever
import org.oppia.domain.util.StateRetriever
import javax.inject.Inject

// TODO(#59): Make this class inaccessible outside of the domain package except for tests. UI code should not be allowed
//  to depend on this utility.

/** Internal class for actually retrieving an exploration object for uses in domain controllers. */
class ExplorationRetriever @Inject constructor(
  private val jsonAssetRetriever: JsonAssetRetriever,
  private val stateRetriever: StateRetriever
) {
  // TODO(#169): Force callers of this method on a background thread.
  /** Loads and returns an exploration for the specified exploration ID, or fails. */
  internal fun loadExploration(explorationId: String): Exploration {
    val explorationObject =
      jsonAssetRetriever.loadJsonFromAsset("$explorationId.json")
        ?: return Exploration.getDefaultInstance()
    return loadExplorationFromAsset(explorationObject)
  }

  // Returns an exploration given an assetName
  private fun loadExplorationFromAsset(explorationObject: JSONObject): Exploration {
    val innerExplorationObject = explorationObject.getJSONObject("exploration")
    return Exploration.newBuilder()
      .setId(explorationObject.getString("exploration_id"))
      .setTitle(innerExplorationObject.getString("title"))
      .setLanguageCode(innerExplorationObject.getString("language_code"))
      .setInitStateName(innerExplorationObject.getString("init_state_name"))
      .setObjective(innerExplorationObject.getString("objective"))
      .putAllStates(createStatesFromJsonObject(innerExplorationObject.getJSONObject("states")))
      .build()
  }

  // Creates the states map from JSON
  private fun createStatesFromJsonObject(statesJsonObject: JSONObject?): MutableMap<String, State> {
    val statesMap: MutableMap<String, State> = mutableMapOf()
    val statesKeys = statesJsonObject?.keys() ?: return statesMap
    val statesIterator = statesKeys.iterator()
    while (statesIterator.hasNext()) {
      val key = statesIterator.next()
      statesMap[key] = stateRetriever.createStateFromJson(key, statesJsonObject.getJSONObject(key))
    }
    return statesMap
  }
}
