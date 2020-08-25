package org.oppia.domain.util

import org.json.JSONArray
import org.json.JSONObject
import org.oppia.app.model.AnswerGroup
import org.oppia.app.model.CorrectAnswer
import org.oppia.app.model.CustomSchemaValue
import org.oppia.app.model.Fraction
import org.oppia.app.model.Hint
import org.oppia.app.model.ImageWithRegions
import org.oppia.app.model.ImageWithRegions.LabeledRegion
import org.oppia.app.model.ImageWithRegions.LabeledRegion.Region.NormalizedRectangle2d
import org.oppia.app.model.ImageWithRegions.LabeledRegion.Region.RegionType
import org.oppia.app.model.Interaction
import org.oppia.app.model.InteractionObject
import org.oppia.app.model.ListOfSetsOfHtmlStrings
import org.oppia.app.model.NumberUnit
import org.oppia.app.model.NumberWithUnits
import org.oppia.app.model.Outcome
import org.oppia.app.model.Point2d
import org.oppia.app.model.RatioExpression
import org.oppia.app.model.RuleSpec
import org.oppia.app.model.SchemaObject
import org.oppia.app.model.SchemaObjectList
import org.oppia.app.model.Solution
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
          getJsonObject(interactionJson, "customization_args"),
          interactionJson.getString("id")
        )
      )
      .addAllHint(
        createListOfHintsFromJson(
          interactionJson.getJSONArray("hints")
        )
      )
      .setSolution(
        createSolutionFromJson(
          getJsonObject(interactionJson, "solution")
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
    answerGroupsJson: JSONArray?,
    interactionId: String
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

  // Creates the list of hints objects from JSON
  private fun createListOfHintsFromJson(
    hintsJson: JSONArray?
  ): MutableList<Hint> {
    val hints = mutableListOf<Hint>()
    if (hintsJson == null) {
      return hints
    }
    for (i in 0 until hintsJson.length()) {
      hints.add(
        createSingleHintFromJson(
          hintsJson.getJSONObject(i)
        )
      )
    }
    return hints
  }

  // Creates an hint object from JSON
  private fun createSingleHintFromJson(hintJson: JSONObject?): Hint {
    if (hintJson == null) {
      return Hint.getDefaultInstance()
    }
    return Hint.newBuilder()
      .setHintContent(
        SubtitledHtml.newBuilder().setHtml(
          hintJson.getJSONObject("hint_content")?.getString("html")
        ).setContentId(
          hintJson.getJSONObject("hint_content")?.optString("content_id")
        )
      )
      .build()
  }

  // Creates a single answer group object from JSON
  private fun createSingleAnswerGroupFromJson(
    answerGroupJson: JSONObject,
    interactionId: String
  ): AnswerGroup {
    return AnswerGroup.newBuilder()
      .setOutcome(
        createOutcomeFromJson(answerGroupJson.getJSONObject("outcome"))
      )
      .addAllRuleSpecs(
        createRuleSpecsFromJson(
          answerGroupJson.optJSONObject("rule_types_to_inputs"), interactionId
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

  // Creates a solution object from JSON
  private fun createSolutionFromJson(solutionJson: JSONObject?): Solution {
    if (solutionJson == null) {
      return Solution.getDefaultInstance()
    }
    return Solution.newBuilder()
      .setCorrectAnswer(createCorrectAnswer(solutionJson))
      .setExplanation(
        SubtitledHtml.newBuilder().setHtml(
          solutionJson.getJSONObject("explanation")?.getString("html")
        ).setContentId(
          solutionJson.getJSONObject("explanation")?.optString("content_id")
        )
      )
      .setAnswerIsExclusive(solutionJson.getBoolean("answer_is_exclusive"))
      .build()
  }

  private fun createCorrectAnswer(containerObject: JSONObject): CorrectAnswer {
    val correctAnswerObject = containerObject.optJSONObject("correct_answer")
    return if (correctAnswerObject != null) {
      CorrectAnswer.newBuilder()
        .setNumerator(correctAnswerObject.getInt("numerator"))
        .setDenominator(correctAnswerObject.getInt("denominator"))
        .setWholeNumber(correctAnswerObject.getInt("wholeNumber"))
        .setIsNegative(correctAnswerObject.getBoolean("isNegative"))
        .build()
    } else {
      CorrectAnswer.newBuilder()
        .setCorrectAnswer(containerObject.getString("correct_answer"))
        .build()
    }
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
  private fun createVoiceOverMappingsFromJson(
    recordedVoiceovers: JSONObject,
    stateBuilder: State.Builder
  ) {
    val voiceoverMappingJson = recordedVoiceovers
      .getJSONObject("voiceovers_mapping")
    voiceoverMappingJson?.let {
      for (key in it.keys()) {
        val voiceoverMapping = VoiceoverMapping.newBuilder()
        val voiceoverJson = it.getJSONObject(key)
        if (voiceoverJson.length() == 0) continue
        for (lang in voiceoverJson.keys()) {
          voiceoverMapping.putVoiceoverMapping(
            lang,
            createVoiceOverFromJson(voiceoverJson.getJSONObject(lang))
          )
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
    ruleSpecJson: JSONObject?,
    interactionId: String
  ): MutableList<RuleSpec> {
    val ruleSpecList = mutableListOf<RuleSpec>()
    if (ruleSpecJson == null) {
      return ruleSpecList
    }

    for (ruleType in ruleSpecJson.keys()) {
      val inputJsonArray = ruleSpecJson.getJSONArray(ruleType)
      for (i in 0 until inputJsonArray.length()) {
        val ruleSpecBuilder = RuleSpec.newBuilder()
        ruleSpecBuilder.ruleType = ruleType
        val inputJsonObject = inputJsonArray.getJSONObject(i)
        val inputKeysIterator = inputJsonObject.keys()
        while (inputKeysIterator.hasNext()) {
          val inputName = inputKeysIterator.next()
          when (ruleSpecBuilder.ruleType) {
            "HasNumeratorEqualTo" -> ruleSpecBuilder.putInput(
              inputName,
              InteractionObject.newBuilder()
                .setSignedInt(inputJsonObject.getInt(inputName))
                .build()
            )
            "HasDenominatorEqualTo" -> ruleSpecBuilder.putInput(
              inputName,
              InteractionObject.newBuilder()
                .setNonNegativeInt(inputJsonObject.getInt(inputName))
                .build()
            )
            else -> ruleSpecBuilder.putInput(
              inputName,
              createExactInputFromJson(
                inputJsonObject,
                inputName,
                interactionId,
                ruleSpecBuilder.ruleType
              )
            )
          }
        }
        ruleSpecList.add(ruleSpecBuilder.build())
      }
    }
    return ruleSpecList
  }

  // Creates an input interaction object from JSON
  private fun createExactInputFromJson(
    inputJson: JSONObject?,
    keyName: String,
    interactionId: String,
    ruleType: String
  ): InteractionObject {
    if (inputJson == null) {
      return InteractionObject.getDefaultInstance()
    }
    return when (interactionId) {
      "MultipleChoiceInput" ->
        InteractionObject.newBuilder()
          .setNonNegativeInt(inputJson.getInt(keyName))
          .build()
      "ItemSelectionInput" ->
        InteractionObject.newBuilder()
          .setSetOfHtmlString(parseStringList(inputJson.getJSONArray(keyName)))
          .build()
      "TextInput" ->
        InteractionObject.newBuilder()
          .setNormalizedString(inputJson.getString(keyName))
          .build()
      "NumberWithUnits" ->
        InteractionObject.newBuilder()
          .setNumberWithUnits(parseNumberWithUnitsObject(inputJson.getJSONObject(keyName)))
          .build()
      "NumericInput" ->
        InteractionObject.newBuilder()
          .setReal(inputJson.getDouble(keyName))
          .build()
      "FractionInput" ->
        InteractionObject.newBuilder()
          .setFraction(parseFraction(inputJson.getJSONObject(keyName)))
          .build()
      "DragAndDropSortInput" -> createExactInputForDragDropAndSort(inputJson, keyName, ruleType)
      "ImageClickInput" ->
        InteractionObject.newBuilder()
          .setNormalizedString(inputJson.getString(keyName))
          .build()
      "RatioExpression" -> createExactInputForRatioExpressionInput(inputJson, keyName, ruleType)
      else -> throw IllegalStateException("Encountered unexpected interaction ID: $interactionId")
    }
  }

  /**
   * Returns a Drag-and-Drop-specific [InteractionObject] parsed from the specified input [JSONObject] for the given key name.
   * This method makes assumptions about how to interpret the input type represented by the [JSONObject].
   */
  private fun createExactInputForDragDropAndSort(
    inputJson: JSONObject?,
    keyName: String,
    ruleType: String
  ): InteractionObject {
    if (inputJson == null) {
      return InteractionObject.getDefaultInstance()
    }
    return when (ruleType) {
      "HasElementXAtPositionY" -> return when (keyName) {
        "x" ->
          InteractionObject.newBuilder()
            .setNormalizedString(inputJson.getString(keyName))
            .build()
        "y" ->
          InteractionObject.newBuilder()
            .setNonNegativeInt(inputJson.getInt(keyName))
            .build()
        else -> throw IllegalStateException("Encountered unexpected key name: $keyName")
      }

      "HasElementXBeforeElementY" ->
        InteractionObject.newBuilder()
          .setNormalizedString(inputJson.getString(keyName))
          .build()
      else ->
        InteractionObject.newBuilder()
          .setListOfSetsOfHtmlString(parseListOfSetsOfHtmlStrings(inputJson.getJSONArray(keyName)))
          .build()
    }
  }

  /**
   * Returns a Ratio Expression Input specific [InteractionObject] parsed from the specified input [JSONObject]
   * for the given key name.
   * This method makes assumptions about how to interpret the input type represented by the [JSONObject].
   */
  private fun createExactInputForRatioExpressionInput(
    inputJson: JSONObject?,
    keyName: String,
    ruleType: String
  ): InteractionObject {
    if (inputJson == null) {
      return InteractionObject.getDefaultInstance()
    }
    return when (ruleType) {
      "HasNumberOfTermsEqualTo" ->
        InteractionObject.newBuilder()
          .setNonNegativeInt(inputJson.getInt(keyName))
          .build()
      else ->
        InteractionObject.newBuilder()
          .setRatioExpression(parseRatio(inputJson.getJSONArray(keyName)))
          .build()
    }
  }

  private fun parseListOfSetsOfHtmlStrings(
    listOfSetsOfHtmlStringsAnswer: JSONArray
  ): ListOfSetsOfHtmlStrings {
    val listOfSetsOfHtmlStringsBuilder =
      ListOfSetsOfHtmlStrings.newBuilder()
    for (i in 0 until listOfSetsOfHtmlStringsAnswer.length()) {
      listOfSetsOfHtmlStringsBuilder.addSetOfHtmlStrings(
        parseStringList(
          listOfSetsOfHtmlStringsAnswer.getJSONArray(i)
        )
      )
    }
    return listOfSetsOfHtmlStringsBuilder.build()
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
      "fraction" ->
        numberWithUnitsBuilder.fraction =
          parseFraction(numberWithUnitsAnswer.getJSONObject("fraction"))
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

  private fun parseRatio(ratioAnswer: JSONArray): RatioExpression {
    val ratioExpression = RatioExpression.newBuilder()
    for (i in 0 until ratioAnswer.length()) {
      ratioExpression.addRatioComponent(ratioAnswer.getInt(i))
    }
    return ratioExpression.build()
  }

  // Creates a customization arg mapping from JSON
  private fun createCustomizationArgsMapFromJson(
    customizationArgsJson: JSONObject?,
    interactionId: String
  ): MutableMap<String, SchemaObject> {
    if (customizationArgsJson == null) {
      return mutableMapOf()
    }
    return when (interactionId) {
      "DragAndDropSortInput" -> {
        createDragAndDropSortInputCustomizationArgsMap(customizationArgsJson)
      }
      "FractionInput" -> {
        createFractionInputCustomizationArgsMap(customizationArgsJson)
      }
      "ImageClickInput" -> {
        createImageClickInputCustomizationArgsMap(customizationArgsJson)
      }
      "ItemSelectionInput" -> {
        createItemSelectionInputCustomizationArgsMap(customizationArgsJson)
      }
      "MultipleChoiceInput" -> {
        createMultipleChoiceInputCustomizationArgsMap(customizationArgsJson)
      }
      "NumericInput" -> {
        mutableMapOf()
      }
      "TextInput" -> {
        createTextInputCustomizationArgsMap(customizationArgsJson)
      }
      else -> mutableMapOf()
    }
  }

  private fun createDragAndDropSortInputCustomizationArgsMap(
    customizationArgsJson: JSONObject
  ): MutableMap<String, SchemaObject> {
    val customizationArgsMap: MutableMap<String, SchemaObject> = mutableMapOf()
    customizationArgsMap["allowMultipleItemsInSamePosition"] =
      parseBooleanSchemaObject(
        getJsonObject(
          customizationArgsJson,
          "allowMultipleItemsInSamePosition"
        )!!.getBoolean("value")
      )
    customizationArgsMap["choices"] =
      parseSubtitledHtmlList(
        getJsonObject(
          customizationArgsJson, "choices"
        )!!.getJSONArray("value")
      )
    return customizationArgsMap
  }

  private fun createFractionInputCustomizationArgsMap(
    customizationArgsJson: JSONObject
  ): MutableMap<String, SchemaObject> {
    val customizationArgsMap: MutableMap<String, SchemaObject> = mutableMapOf()
    customizationArgsMap["allowNonzeroIntegerPart"] =
      parseBooleanSchemaObject(
        getJsonObject(
          customizationArgsJson, "allowNonzeroIntegerPart"
        )!!.getBoolean("value")
      )
    customizationArgsMap["requireSimplestForm"] =
      parseBooleanSchemaObject(
        getJsonObject(
          customizationArgsJson, "requireSimplestForm"
        )!!.getBoolean("value")
      )
    customizationArgsMap["allowImproperFraction"] =
      parseBooleanSchemaObject(
        getJsonObject(
          customizationArgsJson, "allowImproperFraction"
        )!!.getBoolean("value")
      )
    customizationArgsMap["customPlaceholder"] =
      parseNormalizedStringSchemaObject(
        getJsonObject(
          customizationArgsJson, "customPlaceholder"
        )!!.getString("value")
      )
    return customizationArgsMap
  }

  private fun createImageClickInputCustomizationArgsMap(
    customizationArgsJson: JSONObject
  ): MutableMap<String, SchemaObject> {
    val customizationArgsMap: MutableMap<String, SchemaObject> = mutableMapOf()
    customizationArgsMap["highlightRegionsOnHover"] =
      parseBooleanSchemaObject(
        getJsonObject(
          customizationArgsJson, "highlightRegionsOnHover"
        )!!.getBoolean("value")
      )
    customizationArgsMap["imageAndRegions"] =
      parseImageWithRegions(
        getJsonObject(
          customizationArgsJson, "imageAndRegions"
        )!!.getJSONObject("value")
      )
    return customizationArgsMap
  }

  private fun createItemSelectionInputCustomizationArgsMap(
    customizationArgsJson: JSONObject
  ): MutableMap<String, SchemaObject> {
    val customizationArgsMap: MutableMap<String, SchemaObject> = mutableMapOf()
    customizationArgsMap["minAllowableSelectionCount"] =
      parseIntegerSchemaObject(
        getJsonObject(
          customizationArgsJson, "minAllowableSelectionCount"
        )!!.getInt("value")
      )
    customizationArgsMap["maxAllowableSelectionCount"] =
      parseIntegerSchemaObject(
        getJsonObject(
          customizationArgsJson, "maxAllowableSelectionCount"
        )!!.getInt("value")
      )
    customizationArgsMap["choices"] =
      parseSubtitledHtmlList(
        getJsonObject(
          customizationArgsJson, "choices"
        )!!.getJSONArray("value")
      )
    return customizationArgsMap
  }

  private fun createMultipleChoiceInputCustomizationArgsMap(
    customizationArgsJson: JSONObject
  ): MutableMap<String, SchemaObject> {
    val customizationArgsMap: MutableMap<String, SchemaObject> = mutableMapOf()
    customizationArgsMap["choices"] =
      parseSubtitledHtmlList(
        getJsonObject(
          customizationArgsJson, "choices"
        )!!.getJSONArray("value")
      )
    return customizationArgsMap
  }

  private fun createTextInputCustomizationArgsMap(
    customizationArgsJson: JSONObject
  ): MutableMap<String, SchemaObject> {
    val customizationArgsMap: MutableMap<String, SchemaObject> = mutableMapOf()
    customizationArgsMap["placeholder"] =
      parseNormalizedStringSchemaObject(
        getJsonObject(
          customizationArgsJson, "placeholder"
        )!!.getString("value")
      )
    customizationArgsMap["rows"] =
      parseIntegerSchemaObject(
        getJsonObject(
          customizationArgsJson, "rows"
        )!!.getInt("value")
      )
    return customizationArgsMap
  }

  private fun parseIntegerSchemaObject(value: Int): SchemaObject {
    return SchemaObject.newBuilder().setSignedInt(value).build()
  }

  private fun parseNormalizedStringSchemaObject(value: String): SchemaObject {
    return SchemaObject.newBuilder().setNormalizedString(value).build()
  }

  private fun parseBooleanSchemaObject(value: Boolean): SchemaObject {
    return SchemaObject.newBuilder().setBoolValue(value).build()
  }

  private fun parseSubtitledHtmlList(jsonArray: JSONArray): SchemaObject {
    val schemaObjectListBuilder = SchemaObjectList.newBuilder()
    for (i in 0 until jsonArray.length()) {
      val subtitledHtmlJsonObject = jsonArray.getJSONObject(i)
      val subtitledHtmlBuilder = SubtitledHtml.newBuilder()
      subtitledHtmlBuilder.contentId = subtitledHtmlJsonObject.getString("content_id")
      subtitledHtmlBuilder.html = subtitledHtmlJsonObject.getString("html")
      val schemaObjectBuilder = SchemaObject.newBuilder()
      schemaObjectBuilder.setSubtitledHtml(subtitledHtmlBuilder)
      schemaObjectListBuilder.addSchemaObject(schemaObjectBuilder.build())
    }
    return SchemaObject.newBuilder().setSchemaObjectList(
      schemaObjectListBuilder.build()
    ).build()
  }

  private fun parseImageWithRegions(jsonObject: JSONObject): SchemaObject {
    val imageWithRegions = ImageWithRegions.newBuilder()
      .addAllLabelRegions(parseJsonToLabeledRegionsList(jsonObject.getJSONArray("labeledRegions")))
      .setImagePath(jsonObject.getString("imagePath"))
      .build()

    return SchemaObject.newBuilder().setCustomSchemaValue(
      CustomSchemaValue.newBuilder().setImageWithRegions(imageWithRegions).build()
    ).build()
  }

  private fun parseJsonToLabeledRegionsList(jsonArray: JSONArray): List<LabeledRegion> {
    val regionList = mutableListOf<LabeledRegion>()
    for (i in 0 until jsonArray.length()) {
      regionList.add(parseLabeledRegion(jsonArray.getJSONObject(i)))
    }
    return regionList
  }

  private fun parseLabeledRegion(jsonObject: JSONObject): LabeledRegion {
    return LabeledRegion.newBuilder()
      .setLabel(jsonObject.getString("label"))
      .setRegion(parseRegion(jsonObject.getJSONObject("region")))
      .build()
  }

  private fun parseRegion(jsonObject: JSONObject): LabeledRegion.Region {
    return LabeledRegion.Region.newBuilder()
      .setRegionType(parseRegionType(jsonObject.get("regionType")))
      .setArea(parseNormalizedRectangle2d(jsonObject.getJSONArray("area")))
      .build()
  }

  private fun parseRegionType(regionTypeStr: Any): RegionType {
    return when (regionTypeStr) {
      "Rectangle" -> RegionType.RECTANGLE
      else -> RegionType.UNRECOGNIZED
    }
  }

  private fun parseNormalizedRectangle2d(jsonArray: JSONArray): NormalizedRectangle2d {
    return NormalizedRectangle2d.newBuilder()
      .setUpperLeft(parsePoint2d(jsonArray.getJSONArray(0)))
      .setLowerRight(parsePoint2d(jsonArray.getJSONArray(1)))
      .build()
  }

  private fun parsePoint2d(points: JSONArray): Point2d {
    return Point2d.newBuilder()
      .setX(points.getDouble(0).toFloat())
      .setY(points.getDouble(1).toFloat())
      .build()
  }
}
