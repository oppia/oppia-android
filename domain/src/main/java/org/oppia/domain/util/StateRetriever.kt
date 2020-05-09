package org.oppia.domain.util

import org.json.JSONArray
import org.json.JSONObject
import org.oppia.app.model.AnswerGroup
import org.oppia.app.model.Fraction
import org.oppia.app.model.Interaction
import org.oppia.app.model.InteractionObject
import org.oppia.app.model.NumberUnit
import org.oppia.app.model.NumberWithUnits
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
      .setFeedback(createFeedbackSubtitledHtml(outcomeJson))
      .setLabelledAsCorrect(outcomeJson.getBoolean("labelled_as_correct"))
      .build()
  }

  // TODO(#298): Remove this and only parse SubtitledHtml according the latest schema after all test explorations are
  //  updated.
  /**
   * Returns a new [SubtitledHtml] from a specified container [JSONObject] that contains an entry keyed on 'feedback'.
   */
  private fun createFeedbackSubtitledHtml(containerObject: JSONObject): SubtitledHtml {
    val feedbackObject = containerObject.optJSONObject("feedback")
    return if (feedbackObject != null) {
      SubtitledHtml.newBuilder()
        .setContentId(feedbackObject.getString("content_id"))
        .setHtml(feedbackObject.getString("html"))
        .build()
    } else {
      SubtitledHtml.newBuilder().setHtml(containerObject.getString("feedback")).build()
    }
  }

  // Creates VoiceoverMappings from JSON and adds onto State
  private fun createVoiceOverMappingsFromJson(recordedVoiceovers: JSONObject, stateBuilder: State.Builder) {
    val voiceoverMappingJson = recordedVoiceovers.getJSONObject("voiceovers_mapping")
    voiceoverMappingJson?.let {
      for (key in it.keys()) {
        val voiceoverMapping = VoiceoverMapping.newBuilder()
        val voiceoverJson = it.getJSONObject(key)
        if (voiceoverJson.length() == 0) continue
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
          "HasNumeratorEqualTo" -> ruleSpecBuilder.putInput(
            inputName,
            InteractionObject.newBuilder()
              .setSignedInt(inputsJson.getInt(inputName))
              .build()
          )
          "HasDenominatorEqualTo" -> ruleSpecBuilder.putInput(
            inputName,
            InteractionObject.newBuilder()
              .setNonNegativeInt(inputsJson.getInt(inputName))
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
      "NumberWithUnits" -> InteractionObject.newBuilder()
        .setNumberWithUnits(parseNumberWithUnitsObject(inputJson.getJSONObject(keyName)))
        .build()
      "NumericInput" -> InteractionObject.newBuilder()
        .setReal(inputJson.getDouble(keyName))
        .build()
      "FractionInput" -> InteractionObject.newBuilder()
        .setFraction(parseFraction(inputJson.getJSONObject(keyName)))
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

  private fun parseNumberWithUnitsObject(numberWithUnitsAnswer: JSONObject): NumberWithUnits {
    val numberWithUnitsBuilder = NumberWithUnits.newBuilder()
    when (numberWithUnitsAnswer.getString("type")) {
      "real" -> numberWithUnitsBuilder.real = numberWithUnitsAnswer.getDouble("real")
      "fraction" -> numberWithUnitsBuilder.fraction = parseFraction(numberWithUnitsAnswer.getJSONObject("fraction"))
    }
    val unitsArray = numberWithUnitsAnswer.getJSONArray("units")
    for (i in 0 until unitsArray.length()) {
      val unit = unitsArray.getJSONObject(i)
      numberWithUnitsBuilder.addUnit(
        NumberUnit.newBuilder()
          .setUnit(unit.getString("unit"))
          .setExponent(unit.getInt("exponent"))
      )
    }
    return numberWithUnitsBuilder.build()
  }

  private fun parseFraction(fractionAnswer: JSONObject): Fraction {
    return Fraction.newBuilder()
      .setWholeNumber(fractionAnswer.getInt("wholeNumber"))
      .setDenominator(fractionAnswer.getInt("denominator"))
      .setNumerator(fractionAnswer.getInt("numerator"))
      .setIsNegative(fractionAnswer.getBoolean("isNegative"))
      .build()
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
      is String -> return interactionObjectBuilder.setNormalizedString(customizationArgValue).build()
      is Int -> return interactionObjectBuilder.setSignedInt(customizationArgValue).build()
      is Double -> return interactionObjectBuilder.setReal(customizationArgValue).build()
      is Boolean -> return interactionObjectBuilder.setBoolValue(customizationArgValue).build()
      is JSONArray -> {
        if (customizationArgValue.length() > 0) {
          return interactionObjectBuilder.setSetOfHtmlString(
            parseJsonStringList(customizationArgValue)
          ).build()
        }
      }
    }
    return InteractionObject.getDefaultInstance()
  }

  private fun parseJsonStringList(jsonArray: JSONArray): StringList {
    val list: MutableList<String> = ArrayList()
    for (i in 0 until jsonArray.length()) {
      list.add(jsonArray.get(i).toString())
    }
    return StringList.newBuilder().addAllHtml(list).build()
  }
}
