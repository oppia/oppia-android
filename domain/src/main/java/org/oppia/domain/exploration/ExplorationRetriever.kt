package org.oppia.domain.exploration

import org.json.JSONObject
import org.oppia.app.model.Exploration
import org.oppia.app.model.State
import org.oppia.domain.topic.FRACTIONS_EXPLORATION_ID_0
import org.oppia.domain.topic.FRACTIONS_EXPLORATION_ID_1
import org.oppia.domain.topic.RATIOS_EXPLORATION_ID_0
import org.oppia.domain.topic.RATIOS_EXPLORATION_ID_1
import org.oppia.domain.topic.RATIOS_EXPLORATION_ID_2
import org.oppia.domain.topic.RATIOS_EXPLORATION_ID_3
import org.oppia.domain.util.JsonAssetRetriever
import org.oppia.domain.util.StateRetriever
import java.io.IOException
import javax.inject.Inject

const val TEST_EXPLORATION_ID_5 = "DIWZiVgs0km-"
const val TEST_EXPLORATION_ID_6 = "test_exp_id_6"
const val TEST_EXPLORATION_ID_30 = "30"
const val TEST_EXPLORATION_ID_7 = "test_exp_id_7"

// TODO(#59): Make this class inaccessible outside of the domain package except for tests. UI code should not be allowed
//  to depend on this utility.

/** Internal class for actually retrieving an exploration object for uses in domain controllers. */
class ExplorationRetriever @Inject constructor(
  private val jsonAssetRetriever: JsonAssetRetriever,
  private val stateRetriever: StateRetriever
) {
  /** Loads and returns an exploration for the specified exploration ID, or fails. */
  @Suppress("RedundantSuspendModifier") // Force callers to call this on a background thread.
  internal suspend fun loadExploration(explorationId: String): Exploration {
    return when (explorationId) {
      TEST_EXPLORATION_ID_5 -> loadExplorationFromAsset("welcome.json")
      TEST_EXPLORATION_ID_6 -> loadExplorationFromAsset("about_oppia.json")
      TEST_EXPLORATION_ID_30 -> loadExplorationFromAsset("prototype_exploration.json")
      TEST_EXPLORATION_ID_7 -> loadExplorationFromAsset("oppia_exploration.json")
      FRACTIONS_EXPLORATION_ID_0 -> loadExplorationFromAsset("fractions_exploration0.json")
      FRACTIONS_EXPLORATION_ID_1 -> loadExplorationFromAsset("fractions_exploration1.json")
      RATIOS_EXPLORATION_ID_0 -> loadExplorationFromAsset("ratios_exploration0.json")
      RATIOS_EXPLORATION_ID_1 -> loadExplorationFromAsset("ratios_exploration1.json")
      RATIOS_EXPLORATION_ID_2 -> loadExplorationFromAsset("ratios_exploration2.json")
      RATIOS_EXPLORATION_ID_3 -> loadExplorationFromAsset("ratios_exploration3.json")
      else -> throw IllegalStateException("Invalid exploration ID: $explorationId")
    }
  }

  // Returns an exploration given an assetName
  private fun loadExplorationFromAsset(assetName: String): Exploration {
    try {
      val explorationObject = jsonAssetRetriever.loadJsonFromAsset(assetName) ?: return Exploration.getDefaultInstance()
      return Exploration.newBuilder()
        .setTitle(explorationObject.getString("title"))
        .setLanguageCode(explorationObject.getString("language_code"))
        .setInitStateName(explorationObject.getString("init_state_name"))
        .setObjective(explorationObject.getString("objective"))
        .putAllStates(createStatesFromJsonObject(explorationObject.getJSONObject("states")))
        .build()
    } catch (e: IOException) {
      throw(Throwable("Failed to load and parse the json asset file. %s", e))
    }
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
