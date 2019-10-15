package org.oppia.domain.exploration

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import org.oppia.app.model.AnswerGroup
import org.oppia.app.model.Exploration
import org.oppia.app.model.Interaction
import org.oppia.app.model.InteractionObject
import org.oppia.app.model.Outcome
import org.oppia.app.model.RuleSpec
import org.oppia.app.model.State
import org.oppia.app.model.StringList
import org.oppia.app.model.SubtitledHtml
import org.oppia.app.model.Voiceover
import org.oppia.app.model.VoiceoverMapping
import java.io.IOException
import javax.inject.Inject

const val TEST_EXPLORATION_ID_5 = "DIWZiVgs0km-"
const val TEST_EXPLORATION_ID_6 = "test_exp_id_6"

// TODO(#59): Make this class inaccessible outside of the domain package except for tests. UI code should not be allowed
//  to depend on this utility.

/** Internal class for actually retrieving an exploration object for uses in domain controllers. */
class ExplorationRetriever @Inject constructor(private val context: Context) {
  /** Loads and returns an exploration for the specified exploration ID, or fails. */
  @Suppress("RedundantSuspendModifier") // Force callers to call this on a background thread.
  internal suspend fun loadExploration(explorationId: String): Exploration {
    return when (explorationId) {
      TEST_EXPLORATION_ID_5 -> loadExplorationFromAsset("welcome.json")
      TEST_EXPLORATION_ID_6 -> loadExplorationFromAsset("about_oppia.json")
      else -> throw IllegalStateException("Invalid exploration ID: $explorationId")
    }
  }

  // Returns an exploration given an assetName
  private fun loadExplorationFromAsset(assetName: String): Exploration {
    try {
      val explorationObject = loadJsonFromAsset(assetName) ?: return Exploration.getDefaultInstance()
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

  // Returns a JSON Object if it exists, else returns null
  private fun getJsonObject(parentObject: JSONObject, key: String): JSONObject? {
    return parentObject.optJSONObject(key)
  }

  // Loads the JSON string from an asset and converts it to a JSONObject
  @Throws(IOException::class)
  private fun loadJsonFromAsset(assetName: String): JSONObject? {
    val assetManager = context.assets
    val jsonContents = assetManager.open(assetName).bufferedReader().use { it.readText() }
    return JSONObject(jsonContents)
  }

  // Creates the states map from JSON
  private fun createStatesFromJsonObject(statesJsonObject: JSONObject?): MutableMap<String, State> {
    val statesMap: MutableMap<String, State> = mutableMapOf()
    val statesKeys = statesJsonObject?.keys() ?: return statesMap
    val statesIterator = statesKeys.iterator()
    while (statesIterator.hasNext()) {
      val key = statesIterator.next()
      statesMap[key] = createStateFromJson(key, statesJsonObject.getJSONObject(key))
    }
    return statesMap
  }

  // Creates a single state object from JSON
  private fun createStateFromJson(stateName: String, stateJson: JSONObject?): State {
    val state = State.newBuilder()
      .setName(stateName)
      .setContent(
        SubtitledHtml.newBuilder().setHtml(
          stateJson?.getJSONObject("content")?.getString("html")
        ).setContentId(
          stateJson?.getJSONObject("content")?.optString("content_id")
        )
      )
      .setInteraction(createInteractionFromJson(stateJson?.getJSONObject("interaction")))

    if (stateJson != null && stateJson.has("recorded_voiceovers")) {
      addVoiceOverMappings(stateJson.getJSONObject("recorded_voiceovers"), state)
    }

    return state.build()
  }

  // Adds VoiceoverMappings to state builder
  private fun addVoiceOverMappings(recordedVoiceovers: JSONObject, stateBuilder: State.Builder) {
    val voiceoverMappingJson = recordedVoiceovers.getJSONObject("voiceovers_mapping")
    voiceoverMappingJson?.let {
      for (key in it.keys()) {
        val voiceoverMapping = VoiceoverMapping.newBuilder()
        val voiceoverJson = it.getJSONObject(key)
        for (lang in voiceoverJson.keys()) {
          voiceoverMapping.putVoiceoverMapping(lang, createVoiceOverFromJson(voiceoverJson.getJSONObject(lang)))
        }
        stateBuilder.putRecordedVoiceovers(key, voiceoverMapping.build())
      }
    }
  }

  // Creates a Voiceover from Json
  private fun createVoiceOverFromJson(voiceoverJson: JSONObject): Voiceover {
    return Voiceover.newBuilder()
      .setNeedsUpdate(voiceoverJson.getBoolean("needs_update"))
      .setFileName(voiceoverJson.getString("filename"))
      .build()
  }

  // Creates an interaction from JSON
  private fun createInteractionFromJson(interactionJson: JSONObject?): Interaction {
    if (interactionJson == null) {
      return Interaction.getDefaultInstance()
    }
    return Interaction.newBuilder()
      .setId(interactionJson.getString("id"))
      .addAllAnswerGroups(
        createAnswerGroupsFromJson(
          interactionJson.getJSONArray("answer_groups"),
          interactionJson.getString("id")
        )
      )
      .addAllConfirmedUnclassifiedAnswers(
        createAnswerGroupsFromJson(
          interactionJson.getJSONArray("confirmed_unclassified_answers"),
          interactionJson.getString("id")
        )
      )
      .setDefaultOutcome(
        createOutcomeFromJson(
          getJsonObject(interactionJson, "default_outcome")
        )
      )
      .putAllCustomizationArgs(
        createCustomizationArgsMapFromJson(
          getJsonObject(interactionJson, "customization_args")
        )
      )
      .build()
  }

  // Creates the list of answer group objects from JSON
  private fun createAnswerGroupsFromJson(
    answerGroupsJson: JSONArray?, interactionId: String
  ): MutableList<AnswerGroup> {
    val answerGroups = mutableListOf<AnswerGroup>()
    if (answerGroupsJson == null) {
      return answerGroups
    }
    for (i in 0 until answerGroupsJson.length()) {
      answerGroups.add(
        createSingleAnswerGroupFromJson(
          answerGroupsJson.getJSONObject(i), interactionId
        )
      )
    }
    return answerGroups
  }

  // Creates a single answer group object from JSON
  private fun createSingleAnswerGroupFromJson(
    answerGroupJson: JSONObject, interactionId: String
  ): AnswerGroup {
    return AnswerGroup.newBuilder()
      .setOutcome(
        createOutcomeFromJson(answerGroupJson.getJSONObject("outcome"))
      )
      .addAllRuleSpecs(
        createRuleSpecsFromJson(
          answerGroupJson.getJSONArray("rule_specs"), interactionId
        )
      )
      .build()
  }

  // Creates an outcome object from JSON
  private fun createOutcomeFromJson(outcomeJson: JSONObject?): Outcome {
    if (outcomeJson == null) {
      return Outcome.getDefaultInstance()
    }
    return Outcome.newBuilder()
      .setDestStateName(outcomeJson.getString("dest"))
      .setFeedback(
        SubtitledHtml.newBuilder()
          .setHtml(outcomeJson.getString("feedback"))
      )
      .setLabelledAsCorrect(outcomeJson.getBoolean("labelled_as_correct"))
      .build()
  }

  // Creates the list of rule spec objects from JSON
  private fun createRuleSpecsFromJson(
    ruleSpecJson: JSONArray?, interactionId: String
  ): MutableList<RuleSpec> {
    val ruleSpecList = mutableListOf<RuleSpec>()
    if (ruleSpecJson == null) {
      return ruleSpecList
    }
    for (i in 0 until ruleSpecJson.length()) {
      val ruleSpecBuilder = RuleSpec.newBuilder()
      ruleSpecBuilder.ruleType = ruleSpecJson.getJSONObject(i).getString("rule_type")
      val inputsJson = ruleSpecJson.getJSONObject(i).getJSONObject("inputs")
      val inputKeysIterator = inputsJson.keys()
      while (inputKeysIterator.hasNext()) {
        val inputName = inputKeysIterator.next()
        ruleSpecBuilder.putInput(inputName, createInputFromJson(inputsJson, inputName, interactionId))
      }
      ruleSpecList.add(ruleSpecBuilder.build())
    }
    return ruleSpecList
  }

  // Creates an input interaction object from JSON
  private fun createInputFromJson(
    inputJson: JSONObject?, keyName: String, interactionId: String
  ): InteractionObject {
    if (inputJson == null) {
      return InteractionObject.getDefaultInstance()
    }
    return when (interactionId) {
      "MultipleChoiceInput" -> InteractionObject.newBuilder()
        .setNonNegativeInt(inputJson.getInt(keyName))
        .build()
      "TextInput" -> InteractionObject.newBuilder()
        .setNormalizedString(inputJson.getString(keyName))
        .build()
      "NumericInput" -> InteractionObject.newBuilder()
        .setReal(inputJson.getDouble(keyName))
        .build()
      else -> throw IllegalStateException("Encountered unexpected interaction ID: $interactionId")
    }
  }

  // Creates a customization arg mapping from JSON
  private fun createCustomizationArgsMapFromJson(
    customizationArgsJson: JSONObject?
  ): MutableMap<String, InteractionObject> {
    val customizationArgsMap: MutableMap<String, InteractionObject> = mutableMapOf()
    if (customizationArgsJson == null) {
      return customizationArgsMap
    }
    val customizationArgsKeys = customizationArgsJson.keys() ?: return customizationArgsMap
    val customizationArgsIterator = customizationArgsKeys.iterator()
    while (customizationArgsIterator.hasNext()) {
      val key = customizationArgsIterator.next()
      customizationArgsMap[key] = createCustomizationArgValueFromJson(
        customizationArgsJson.getJSONObject(key).get("value")
      )
    }
    return customizationArgsMap
  }

  // Creates a customization arg value interaction object from JSON
  private fun createCustomizationArgValueFromJson(customizationArgValue: Any): InteractionObject {
    val interactionObjectBuilder = InteractionObject.newBuilder()
    when (customizationArgValue) {
      is String -> return interactionObjectBuilder
        .setNormalizedString(customizationArgValue).build()
      is Int -> return interactionObjectBuilder
        .setSignedInt(customizationArgValue).build()
      is Double -> return interactionObjectBuilder
        .setReal(customizationArgValue).build()
      is List<*> -> if (customizationArgValue.size > 0) {
        return interactionObjectBuilder.setSetOfHtmlString(
          createStringList(customizationArgValue)
        ).build()
      }
    }
    return InteractionObject.getDefaultInstance()
  }

  @Suppress("UNCHECKED_CAST") // Checked cast in the if statement
  private fun createStringList(value: List<*>): StringList {
    val stringList = mutableListOf<String>()
    if (value[0] is String) {
      stringList.addAll(value as List<String>)
      return StringList.newBuilder().addAllHtml(stringList).build()
    }
    return StringList.getDefaultInstance()
  }
}
