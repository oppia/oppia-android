package org.oppia.domain.util

import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONObject
import org.oppia.app.model.AnswerGroup
import org.oppia.app.model.Fraction
import org.oppia.app.model.Interaction
import org.oppia.app.model.InteractionObject
import org.oppia.app.model.Outcome
import org.oppia.app.model.RuleSpec
import org.oppia.app.model.State
import org.oppia.app.model.StringList
import org.oppia.app.model.SubtitledHtml
import org.oppia.app.model.Voiceover
import org.oppia.app.model.VoiceoverMapping
import javax.inject.Inject

/** Utility that helps create a [State] object given its JSON representation. */
class StateRetriever @Inject constructor(
  private val jsonAssetRetriever: JsonAssetRetriever
) {

  /** Creates a single state object from JSON */
  fun createStateFromJson(stateName: String, stateJson: JSONObject?): State {
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
      createVoiceOverMappingsFromJson(stateJson.getJSONObject("recorded_voiceovers"), state)
    }

    return state.build()
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

  // Returns a JSON Object if it exists, else returns null
  private fun getJsonObject(parentObject: JSONObject, key: String): JSONObject? {
    return parentObject.optJSONObject(key)
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

  // Creates VoiceoverMappings from JSON and adds onto State
  private fun createVoiceOverMappingsFromJson(recordedVoiceovers: JSONObject, stateBuilder: State.Builder) {
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
        when (ruleSpecBuilder.ruleType) {
          "HasDenominatorEqualTo", "HasNumeratorEqualTo" -> ruleSpecBuilder.putInput(
            inputName,
            InteractionObject.newBuilder()
              .setReal(inputsJson.getDouble(inputName))
              .build()
          )
          else -> ruleSpecBuilder.putInput(inputName, createExactInputFromJson(inputsJson, inputName, interactionId))
        }

      }
      ruleSpecList.add(ruleSpecBuilder.build())
    }
    return ruleSpecList
  }

  // Creates an input interaction object from JSON
  private fun createExactInputFromJson(
    inputJson: JSONObject?, keyName: String, interactionId: String
  ): InteractionObject {
    if (inputJson == null) {
      return InteractionObject.getDefaultInstance()
    }
    return when (interactionId) {
      "MultipleChoiceInput" -> InteractionObject.newBuilder()
        .setNonNegativeInt(inputJson.getInt(keyName))
        .build()
      "ItemSelectionInput" -> InteractionObject.newBuilder()
        .setSetOfHtmlString(parseStringList(inputJson.getJSONArray(keyName)))
        .build()
      "TextInput" -> InteractionObject.newBuilder()
        .setNormalizedString(inputJson.getString(keyName))
        .build()
      "NumericInput" -> InteractionObject.newBuilder()
        .setReal(inputJson.getDouble(keyName))
        .build()
      "FractionInput" -> InteractionObject.newBuilder()
        .setFraction(
          Fraction.newBuilder()
            .setDenominator(inputJson.getJSONObject(keyName).getInt("denominator"))
            .setNumerator(inputJson.getJSONObject(keyName).getInt("numerator"))
            .setIsNegative(inputJson.getJSONObject(keyName).getBoolean("isNegative"))
            .setWholeNumber(inputJson.getJSONObject(keyName).getInt("wholeNumber"))
        ).build()
      "ItemSelectionInput" -> InteractionObject.newBuilder()
        .setSetOfHtmlString(
          StringList.newBuilder().addAllHtml(
            jsonAssetRetriever.getStringsFromJSONArray(inputJson.getJSONArray(keyName))
          )
        )
        .build()
      else -> throw IllegalStateException("Encountered unexpected interaction ID: $interactionId")
    }
  }

  private fun parseStringList(itemSelectionAnswer: JSONArray): StringList {
    val stringListBuilder = StringList.newBuilder()
    for (i in 0 until itemSelectionAnswer.length()) {
      stringListBuilder.addHtml(itemSelectionAnswer.getString(i))
    }
    return stringListBuilder.build()
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
      else -> {
        val customizationArgValueTemp: ArrayList<*> =
          Gson().fromJson(customizationArgValue.toString(), ArrayList::class.java)
        if (customizationArgValueTemp is List<*> && customizationArgValueTemp.size > 0) {
          return interactionObjectBuilder.setSetOfHtmlString(
            createStringList(customizationArgValueTemp)
          ).build()
        }
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
