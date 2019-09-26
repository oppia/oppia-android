package org.oppia.domain.exploration

import android.content.Context
import androidx.lifecycle.LiveData
import javax.inject.Inject
import javax.inject.Singleton
import org.oppia.app.model.Exploration
import org.oppia.util.data.AsyncResult
import java.io.IOException
import org.json.JSONObject
import org.oppia.app.model.Interaction
import org.oppia.app.model.State
import org.oppia.app.model.SubtitledHtml
import org.oppia.util.data.DataProviders

const val TEST_EXPLORATION_ID_0 = "test_exp_id_0"
const val TEST_EXPLORATION_ID_1 = "test_exp_id_1"
private const val WELCOME_EXPLORATION_DATA_PROVIDER_ID = "WelcomeExplorationDataProvider"
private const val ABBOUT_OPPIA_EXPLORATION_DATA_PROVIDER_ID = "AbboutOppiaExplorationDataProvider"

/** Controller for retrieving an exploration. */
@Singleton
class ExplorationDataController @Inject constructor(private val context: Context,
                                                    private val dataProviders: DataProviders
) {

  private val welcomeExplorationDataProvider =
    dataProviders.createInMemoryDataProviderAsync(
      WELCOME_EXPLORATION_DATA_PROVIDER_ID, this::retrieveWelcomeExplorationAsync)
  private val abboutOppiaExplorationDataProvider =
    dataProviders.createInMemoryDataProviderAsync(
      WELCOME_EXPLORATION_DATA_PROVIDER_ID, this::retrieveAbboutOppiaExplorationAsync)

  /**
   * Returns an  [Exploration] given an ID.
   */
  fun getExplorationById(ID: String): LiveData<AsyncResult<Exploration>>? {
    if (ID == TEST_EXPLORATION_ID_0) {
      return dataProviders.convertToLiveData(welcomeExplorationDataProvider)
    }
    if (ID == TEST_EXPLORATION_ID_1) {
      return dataProviders.convertToLiveData(abboutOppiaExplorationDataProvider)
    }
    return null
  }

  @Suppress("RedundantSuspendModifier") // DataProviders expects this function to be a suspend function.
  private suspend fun retrieveWelcomeExplorationAsync(): AsyncResult<Exploration> {
    return AsyncResult.success(createExploration0())
  }

  @Suppress("RedundantSuspendModifier") // DataProviders expects this function to be a suspend function.
  private suspend fun retrieveAbboutOppiaExplorationAsync(): AsyncResult<Exploration> {
    return AsyncResult.success(createExploration1())
  }

  // Returns the "welcome" exploration
  private fun createExploration0(): Exploration {
    val welcomeObject = loadJSONFromAsset("welcome.json")
    return Exploration.newBuilder()
      .setTitle(welcomeObject?.getString("title"))
      .setLanguageCode(welcomeObject?.getString("language_code"))
      .setInitStateName(welcomeObject?.getString("init_state_name"))
      .setObjective(welcomeObject?.getString("objective"))
      .putAllStates(createStatesFromJsonObject(welcomeObject?.getJSONObject("states")))
      .build()
  }

  private fun createExploration1(): Exploration {
    val aboutOppiaObject = loadJSONFromAsset("about_oppia.json")
    return Exploration.newBuilder()
     // Add fields
      .build()
  }

  private fun loadJSONFromAsset(assetName: String): JSONObject? {
    val am = context.assets

    var jsonObject: JSONObject?
    try {
      val `is` = am.open(assetName)
      val size = `is`.available()
      val buffer = ByteArray(size)
      `is`.read(buffer)
      `is`.close()
      val json = String(buffer, Charsets.UTF_8)
      jsonObject = JSONObject(json)
    } catch (ex: IOException) {
      ex.printStackTrace()
      return null
    }

    return jsonObject
  }

  private fun createStatesFromJsonObject(statesJsonObject: JSONObject): MutableMap<String, State> {
    val statesMap: MutableMap<String, State> = mutableMapOf()
    val statesIterator = statesJsonObject.keys().iterator()
    while(statesIterator.hasNext()) {
      val key = statesIterator.next()
      statesMap.put(key, createStateFromJson(statesJsonObject.getJSONObject(key)))
    }
    return statesMap
  }

  private fun createStateFromJson(stateJson: JSONObject): State {
    return State.newBuilder()
      .setContent(
        SubtitledHtml.newBuilder().setHtml(
          stateJson.getJSONObject("content")?.getString("html")))
      .setInteraction(createInteractionFromJson(stateJson.getJSONObject("interaction")))
      .build()
  }

  private fun createInteractionFromJson(interactionJson: JSONObject?): Interaction {
    return Interaction.newBuilder()
      // Add data
      .build()
  }

}
