package org.oppia.android.domain.exploration

import org.json.JSONObject
import org.oppia.android.app.model.Exploration
import org.oppia.android.app.model.State
import org.oppia.android.app.model.SubtitledHtml
import org.oppia.android.domain.util.JsonAssetRetriever
import org.oppia.android.domain.util.StateRetriever
import org.oppia.android.domain.util.getStringFromObject
import org.oppia.android.util.caching.AssetRepository
import org.oppia.android.util.caching.LoadLessonProtosFromAssets
import javax.inject.Inject

// TODO(#59): Make this class inaccessible outside of the domain package except for tests. UI code should not be allowed
//  to depend on this utility.

/** Implementation of [ExplorationRetriever] that loads explorations from the app's assets. */
// TODO(#1580): Re-restrict access using Bazel visibilities
class ExplorationRetrieverImpl @Inject constructor(
  private val jsonAssetRetriever: JsonAssetRetriever,
  private val stateRetriever: StateRetriever,
  private val assetRepository: AssetRepository,
  @LoadLessonProtosFromAssets private val loadLessonProtosFromAssets: Boolean
) : ExplorationRetriever {
  override suspend fun loadExploration(explorationId: String): Exploration {
    return if (loadLessonProtosFromAssets) {
      assetRepository.loadProtoFromLocalAssets(explorationId, Exploration.getDefaultInstance())
    } else {
      val explorationObject =
        jsonAssetRetriever.loadJsonFromAsset("$explorationId.json")
          ?: return Exploration.getDefaultInstance()
      loadExplorationFromAsset(explorationObject)
    }
  }

  override suspend fun loadExplorationPosition(explorationId: String): Pair<Int, MutableMap<String, Int>> {
    val explorationObject = jsonAssetRetriever.loadJsonFromAsset("$explorationId.json")
    return parseJsonObjectToCalculatePosition(explorationObject!!)
  }

  // Returns an exploration given an assetName
  private fun loadExplorationFromAsset(explorationObject: JSONObject): Exploration {
    val innerExplorationObject = explorationObject.getJSONObject("exploration")
    return Exploration.newBuilder()
      .setId(explorationObject.getStringFromObject("exploration_id"))
      .setTranslatableTitle(
        SubtitledHtml.newBuilder().apply {
          contentId = "title"
          html = innerExplorationObject.getStringFromObject("title")
        }.build()
      )
      .setLanguageCode(innerExplorationObject.getStringFromObject("language_code"))
      .setInitStateName(innerExplorationObject.getStringFromObject("init_state_name"))
      .setDescription(
        SubtitledHtml.newBuilder().apply {
          contentId = "description"
          html = innerExplorationObject.getStringFromObject("objective")
        }.build()
      )
      .putAllStates(createStatesFromJsonObject(innerExplorationObject.getJSONObject("states")))
      .setVersion(explorationObject.getInt("version"))
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

  private fun parseJsonObjectToCalculatePosition(explorationObject: JSONObject): Pair<Int, MutableMap<String, Int>> {
    val states = explorationObject.getJSONObject("exploration").getJSONObject("states")
    val initialState = explorationObject.getJSONObject("exploration").getString("init_state_name")

    val statePosition = mutableMapOf<String, Int>()
    var position = 0

    // Function to recursively traverse the states and set their positions
    fun getStatePosition(stateName: String) {
      if (!statePosition.containsKey(stateName)) {
        statePosition[stateName] = ++position

        val state = states.getJSONObject(stateName)
        val interaction = state.getJSONObject("interaction")

        if (interaction.has("default_outcome") && !interaction.isNull("default_outcome")) {
          val defaultOutcome = interaction.getJSONObject("default_outcome")
          val dest = defaultOutcome.getString("dest")
          getStatePosition(dest)
        }

        if (interaction.has("answer_groups") && interaction.getJSONArray("answer_groups").length() > 0) {
          val answerGroups = interaction.getJSONArray("answer_groups")
          for (i in answerGroups.length() - 1 downTo 0) {
            val outcome = answerGroups.getJSONObject(i).getJSONObject("outcome")
            val dest = outcome.getString("dest")
            getStatePosition(dest)
          }
        }
      }
    }

    getStatePosition(initialState)
    return position to statePosition

  }
}
