package org.oppia.android.domain.util

import org.json.JSONArray
import org.json.JSONObject
import org.oppia.android.app.model.AnswerGroup
import org.oppia.android.app.model.CorrectAnswer
import org.oppia.android.app.model.CustomSchemaValue
import org.oppia.android.app.model.Fraction
import org.oppia.android.app.model.Hint
import org.oppia.android.app.model.ImageWithRegions
import org.oppia.android.app.model.ImageWithRegions.LabeledRegion
import org.oppia.android.app.model.ImageWithRegions.LabeledRegion.Region.NormalizedRectangle2d
import org.oppia.android.app.model.ImageWithRegions.LabeledRegion.Region.RegionType
import org.oppia.android.app.model.Interaction
import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.ListOfSetsOfTranslatableHtmlContentIds
import org.oppia.android.app.model.Misconception
import org.oppia.android.app.model.NumberUnit
import org.oppia.android.app.model.NumberWithUnits
import org.oppia.android.app.model.Outcome
import org.oppia.android.app.model.Point2d
import org.oppia.android.app.model.RatioExpression
import org.oppia.android.app.model.RuleSpec
import org.oppia.android.app.model.SchemaObject
import org.oppia.android.app.model.SchemaObjectList
import org.oppia.android.app.model.SetOfTranslatableHtmlContentIds
import org.oppia.android.app.model.Solution
import org.oppia.android.app.model.State
import org.oppia.android.app.model.SubtitledHtml
import org.oppia.android.app.model.SubtitledUnicode
import org.oppia.android.app.model.TranslatableHtmlContentId
import org.oppia.android.app.model.TranslatableSetOfNormalizedString
import org.oppia.android.app.model.Voiceover
import org.oppia.android.app.model.VoiceoverMapping
import javax.inject.Inject

/** Utility that helps create a [State] object given its JSON representation. */
class StateRetriever @Inject constructor() {

  /** Returns a new [State] with the specified name, parsed from the specified [JSONObject]. */
  fun createStateFromJson(stateName: String, stateJson: JSONObject): State =
    State.newBuilder().apply {
      name = stateName
      content = parseSubtitledHtml(stateJson.getJSONObject("content"))
      interaction = createInteractionFromJson(stateJson.getJSONObject("interaction"))
      if (stateJson.has("recorded_voiceovers")) {
        putAllRecordedVoiceovers(
          createVoiceoverMappingsFromJson(stateJson.getJSONObject("recorded_voiceovers"))
        )
      }
    }.build()

  // Creates an interaction from JSON
  private fun createInteractionFromJson(interactionJson: JSONObject): Interaction {
    return Interaction.newBuilder()
      .setId(interactionJson.getString("id"))
      .addAllAnswerGroups(
        createAnswerGroupsFromJson(
          interactionJson.getJSONArray("answer_groups"),
          interactionJson.getString("id")
        )
      )
      .setDefaultOutcome(
        createOutcomeFromJson(
          interactionJson.optJSONObject("default_outcome")
        )
      )
      .putAllCustomizationArgs(
        createCustomizationArgsMapFromJson(
          interactionJson.getJSONObject("customization_args"),
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
          interactionJson.optJSONObject("solution")
        )
      )
      .build()
  }

  // Creates the list of answer group objects from JSON
  private fun createAnswerGroupsFromJson(
    answerGroupsJson: JSONArray,
    interactionId: String
  ): List<AnswerGroup> {
    val answerGroups = mutableListOf<AnswerGroup>()
    for (i in 0 until answerGroupsJson.length()) {
      answerGroups +=
        createSingleAnswerGroupFromJson(answerGroupsJson.getJSONObject(i), interactionId)
    }
    return answerGroups
  }

  // Creates the list of hints objects from JSON
  private fun createListOfHintsFromJson(
    hintsJson: JSONArray
  ): List<Hint> {
    val hints = mutableListOf<Hint>()
    for (i in 0 until hintsJson.length()) {
      hints += createSingleHintFromJson(hintsJson.getJSONObject(i))
    }
    return hints
  }

  // Creates an hint object from JSON
  private fun createSingleHintFromJson(hintJson: JSONObject): Hint = Hint.newBuilder().apply {
    hintContent = parseSubtitledHtml(hintJson.getJSONObject("hint_content"))
  }.build()

  // Creates a single answer group object from JSON
  private fun createSingleAnswerGroupFromJson(
    answerGroupJson: JSONObject,
    interactionId: String
  ): AnswerGroup = AnswerGroup.newBuilder().apply {
    outcome = createOutcomeFromJson(answerGroupJson.getJSONObject("outcome"))
    val ruleSpecsArrayJson = answerGroupJson.getJSONArray("rule_specs")
    val ruleSpecsJson = mutableListOf<JSONObject>()
    for (i in 0 until ruleSpecsArrayJson.length()) {
      ruleSpecsJson += ruleSpecsArrayJson.getJSONObject(i)
    }
    addAllRuleSpecs(ruleSpecsJson.map { convertToRuleSpec(it, interactionId) })
    val misconceptionJson = answerGroupJson.optJSONObject("tagged_skill_misconception_id")
    if (misconceptionJson != null) {
      val misconceptionParts = misconceptionJson.toString().split("-")
      taggedSkillMisconception =
        Misconception.newBuilder().apply {
          skillId = misconceptionParts[0]
          misconceptionId = misconceptionParts[1]
        }.build()
    }
  }.build()

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
    return Solution.newBuilder().apply {
      correctAnswer = createCorrectAnswer(solutionJson)
      explanation = parseSubtitledHtml(solutionJson.getJSONObject("explanation"))
      answerIsExclusive = solutionJson.getBoolean("answer_is_exclusive")
    }.build()
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

  /**
   * Returns a new [SubtitledHtml] from a specified container [JSONObject] that contains an entry
   * keyed on 'feedback'.
   */
  private fun createFeedbackSubtitledHtml(containerObject: JSONObject): SubtitledHtml {
    val feedbackObject = containerObject.getJSONObject("feedback")
    return SubtitledHtml.newBuilder()
      .setContentId(feedbackObject.getString("content_id"))
      .setHtml(feedbackObject.getString("html"))
      .build()
  }

  private fun createVoiceoverMappingsFromJson(
    recordedVoiceovers: JSONObject
  ): Map<String, VoiceoverMapping> {
    val voiceoverMappingJson = recordedVoiceovers.getJSONObject("voiceovers_mapping")
    return voiceoverMappingJson.keys().asSequence().associateWith { contentId ->
      val voiceoverJson = voiceoverMappingJson.getJSONObject(contentId)
      VoiceoverMapping.newBuilder().apply {
        putAllVoiceoverMapping(
          voiceoverJson.keys().asSequence().associateWith { languageCode ->
            createVoiceoverFromJson(voiceoverJson.getJSONObject(languageCode))
          }
        )
      }.build()
    }
  }

  // Creates a Voiceover from Json
  private fun createVoiceoverFromJson(voiceoverJson: JSONObject): Voiceover =
    Voiceover.newBuilder().apply {
      needsUpdate = voiceoverJson.getBoolean("needs_update")
      fileName = voiceoverJson.getString("filename")
    }.build()

  // Creates the list of rule spec objects from JSON
  private fun convertToRuleSpec(ruleSpecJson: JSONObject, interactionId: String): RuleSpec {
    val inputJsonObject = ruleSpecJson.getJSONObject("inputs")
    return RuleSpec.newBuilder().apply {
      ruleType = ruleSpecJson.getString("rule_type")
      putAllInput(
        inputJsonObject.keys().asSequence().associateWith { inputName ->
          createExactInputFromJson(inputJsonObject, inputName, interactionId, ruleType)
        }
      )
    }.build()
  }

  // Creates an input interaction object from JSON
  private fun createExactInputFromJson(
    inputJson: JSONObject,
    keyName: String,
    interactionId: String,
    ruleType: String
  ): InteractionObject {
    return when (interactionId) {
      "MultipleChoiceInput" ->
        InteractionObject.newBuilder()
          .setNonNegativeInt(inputJson.getInt(keyName))
          .build()
      "ItemSelectionInput" ->
        InteractionObject.newBuilder()
          .setSetOfTranslatableHtmlContentIds(
            parseSetOfTranslatableHtmlContentIds(inputJson.getJSONArray(keyName))
          )
          .build()
      "TextInput" ->
        InteractionObject.newBuilder()
          .setTranslatableSetOfNormalizedString(
            parseTranslatableSetOfNormalizedString(inputJson.getJSONObject(keyName))
          )
          .build()
      "NumberWithUnits" ->
        InteractionObject.newBuilder()
          .setNumberWithUnits(parseNumberWithUnitsObject(inputJson.getJSONObject(keyName)))
          .build()
      "NumericInput" ->
        InteractionObject.newBuilder()
          .setReal(inputJson.getDouble(keyName))
          .build()
      "FractionInput" -> createExactInputForFractionInput(inputJson, keyName, ruleType)
      "DragAndDropSortInput" -> createExactInputForDragDropAndSort(inputJson, keyName, ruleType)
      "ImageClickInput" ->
        InteractionObject.newBuilder()
          .setNormalizedString(inputJson.getString(keyName))
          .build()
      "RatioExpressionInput" ->
        createExactInputForRatioExpressionInput(inputJson, keyName, ruleType)
      else -> throw IllegalStateException("Encountered unexpected interaction ID: $interactionId")
    }
  }

  private fun createExactInputForFractionInput(
    inputJson: JSONObject,
    keyName: String,
    ruleType: String
  ): InteractionObject {
    return when (ruleType) {
      "HasNumeratorEqualTo" ->
        InteractionObject.newBuilder()
          .setSignedInt(inputJson.getInt(keyName))
          .build()
      "HasDenominatorEqualTo" ->
        InteractionObject.newBuilder()
          .setNonNegativeInt(inputJson.getInt(keyName))
          .build()
      else ->
        InteractionObject.newBuilder()
          .setFraction(parseFraction(inputJson.getJSONObject(keyName)))
          .build()
    }
  }

  /**
   * Returns a Drag-and-Drop-specific [InteractionObject] parsed from the specified input
   * [JSONObject] for the given key name. This method makes assumptions about how to interpret the
   * input type represented by the [JSONObject].
   */
  private fun createExactInputForDragDropAndSort(
    inputJson: JSONObject,
    keyName: String,
    ruleType: String
  ): InteractionObject {
    return when (ruleType) {
      "HasElementXAtPositionY" -> return when (keyName) {
        "x" ->
          InteractionObject.newBuilder()
            .setTranslatableHtmlContentId(
              parseTranslatableContentId(inputJson.getString(keyName))
            )
            .build()
        "y" ->
          InteractionObject.newBuilder()
            .setNonNegativeInt(inputJson.getInt(keyName))
            .build()
        else -> throw IllegalStateException("Encountered unexpected key name: $keyName")
      }
      "HasElementXBeforeElementY" ->
        InteractionObject.newBuilder()
          .setTranslatableHtmlContentId(
            parseTranslatableContentId(inputJson.getString(keyName))
          )
          .build()
      else ->
        InteractionObject.newBuilder()
          .setListOfSetsOfTranslatableHtmlContentIds(
            parseListOfSetsOfTranslatableHtmlContentIds(inputJson.getJSONArray(keyName))
          )
          .build()
    }
  }

  /**
   * Returns a Ratio Expression Input specific [InteractionObject] parsed from the specified input
   * [JSONObject] for the given key name. This method makes assumptions about how to interpret the
   * input type represented by the [JSONObject].
   */
  private fun createExactInputForRatioExpressionInput(
    inputJson: JSONObject,
    keyName: String,
    ruleType: String
  ): InteractionObject {
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

  private fun parseTranslatableSetOfNormalizedString(
    translatableSetOfStringsJson: JSONObject
  ): TranslatableSetOfNormalizedString = TranslatableSetOfNormalizedString.newBuilder().apply {
    contentId = translatableSetOfStringsJson.getString("contentId")
    val strSet = translatableSetOfStringsJson.getJSONArray("normalizedStrSet")
    for (i in 0 until strSet.length()) {
      addNormalizedStrings(strSet.getString(i))
    }
  }.build()

  private fun parseListOfSetsOfTranslatableHtmlContentIds(
    listOfSetsOfContentIdsJson: JSONArray
  ): ListOfSetsOfTranslatableHtmlContentIds {
    val listOfSetsOfContentIdsBuilder = ListOfSetsOfTranslatableHtmlContentIds.newBuilder()
    for (i in 0 until listOfSetsOfContentIdsJson.length()) {
      listOfSetsOfContentIdsBuilder.addContentIdLists(
        parseSetOfTranslatableHtmlContentIds(listOfSetsOfContentIdsJson.getJSONArray(i))
      )
    }
    return listOfSetsOfContentIdsBuilder.build()
  }

  private fun parseSetOfTranslatableHtmlContentIds(
    setOfContentIdsJson: JSONArray
  ): SetOfTranslatableHtmlContentIds {
    val setOfContentIdsBuilder = SetOfTranslatableHtmlContentIds.newBuilder()
    for (i in 0 until setOfContentIdsJson.length()) {
      setOfContentIdsBuilder.addContentIds(
        parseTranslatableContentId(setOfContentIdsJson.getString(i))
      )
    }
    return setOfContentIdsBuilder.build()
  }

  private fun parseTranslatableContentId(contentIdJson: String): TranslatableHtmlContentId =
    TranslatableHtmlContentId.newBuilder().apply { contentId = contentIdJson }.build()

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
    customizationArgsJson: JSONObject,
    interactionId: String
  ): Map<String, SchemaObject> {
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
      "RatioExpressionInput" -> {
        createRatioExpressionInputCustomizationArgsMap(customizationArgsJson)
      }
      else -> mutableMapOf()
    }
  }

  private fun createRatioExpressionInputCustomizationArgsMap(
    customizationArgsJson: JSONObject
  ): Map<String, SchemaObject> {
    val customizationArgsMap = mutableMapOf<String, SchemaObject>()
    customizationArgsMap["placeholder"] =
      parseSubtitledUnicode(
        customizationArgsJson.getJSONObject("placeholder").getJSONObject("value")
      )
    customizationArgsMap["numberOfTerms"] =
      parseIntegerSchemaObject(
        customizationArgsJson.getJSONObject("numberOfTerms").getInt("value")
      )
    return customizationArgsMap
  }

  private fun createDragAndDropSortInputCustomizationArgsMap(
    customizationArgsJson: JSONObject
  ): Map<String, SchemaObject> {
    val customizationArgsMap = mutableMapOf<String, SchemaObject>()
    customizationArgsMap["allowMultipleItemsInSamePosition"] =
      parseBooleanSchemaObject(
        customizationArgsJson.getJSONObject("allowMultipleItemsInSamePosition").getBoolean("value")
      )
    customizationArgsMap["choices"] =
      parseSubtitledHtmlListForCustomizationArgs(
        customizationArgsJson.getJSONObject("choices").getJSONArray("value")
      )
    return customizationArgsMap
  }

  private fun createFractionInputCustomizationArgsMap(
    customizationArgsJson: JSONObject
  ): Map<String, SchemaObject> {
    val customizationArgsMap = mutableMapOf<String, SchemaObject>()
    customizationArgsMap["allowNonzeroIntegerPart"] =
      parseBooleanSchemaObject(
        customizationArgsJson.getJSONObject("allowNonzeroIntegerPart").getBoolean("value")
      )
    customizationArgsMap["requireSimplestForm"] =
      parseBooleanSchemaObject(
        customizationArgsJson.getJSONObject("requireSimplestForm").getBoolean("value")
      )
    customizationArgsMap["allowImproperFraction"] =
      parseBooleanSchemaObject(
        customizationArgsJson.getJSONObject("allowImproperFraction").getBoolean("value")
      )
    customizationArgsMap["customPlaceholder"] =
      parseSubtitledUnicode(
        customizationArgsJson.getJSONObject("customPlaceholder").getJSONObject("value")
      )
    return customizationArgsMap
  }

  private fun createImageClickInputCustomizationArgsMap(
    customizationArgsJson: JSONObject
  ): Map<String, SchemaObject> {
    val customizationArgsMap = mutableMapOf<String, SchemaObject>()
    customizationArgsMap["highlightRegionsOnHover"] =
      parseBooleanSchemaObject(
        customizationArgsJson.getJSONObject("highlightRegionsOnHover").getBoolean("value")
      )
    customizationArgsMap["imageAndRegions"] =
      parseImageWithRegions(
        customizationArgsJson.getJSONObject("imageAndRegions").getJSONObject("value")
      )
    return customizationArgsMap
  }

  private fun createItemSelectionInputCustomizationArgsMap(
    customizationArgsJson: JSONObject
  ): Map<String, SchemaObject> {
    val customizationArgsMap = mutableMapOf<String, SchemaObject>()
    customizationArgsMap["minAllowableSelectionCount"] =
      parseIntegerSchemaObject(
        customizationArgsJson.getJSONObject("minAllowableSelectionCount").getInt("value")
      )
    customizationArgsMap["maxAllowableSelectionCount"] =
      parseIntegerSchemaObject(
        customizationArgsJson.getJSONObject("maxAllowableSelectionCount").getInt("value")
      )
    customizationArgsMap["choices"] =
      parseSubtitledHtmlListForCustomizationArgs(
        customizationArgsJson.getJSONObject("choices").getJSONArray("value")
      )
    return customizationArgsMap
  }

  private fun createMultipleChoiceInputCustomizationArgsMap(
    customizationArgsJson: JSONObject
  ): Map<String, SchemaObject> {
    val customizationArgsMap = mutableMapOf<String, SchemaObject>()
    customizationArgsMap["choices"] =
      parseSubtitledHtmlListForCustomizationArgs(
        customizationArgsJson.getJSONObject("choices").getJSONArray("value")
      )
    return customizationArgsMap
  }

  private fun createTextInputCustomizationArgsMap(
    customizationArgsJson: JSONObject
  ): Map<String, SchemaObject> {
    val customizationArgsMap = mutableMapOf<String, SchemaObject>()
    customizationArgsMap["placeholder"] =
      parseSubtitledUnicode(
        customizationArgsJson.getJSONObject("placeholder").getJSONObject("value")
      )
    customizationArgsMap["rows"] =
      parseIntegerSchemaObject(
        customizationArgsJson.getJSONObject("rows").getInt("value")
      )
    return customizationArgsMap
  }

  private fun parseSubtitledHtml(subtitledHtmlJson: JSONObject): SubtitledHtml =
    SubtitledHtml.newBuilder().apply {
      contentId = subtitledHtmlJson.getString("content_id")
      html = subtitledHtmlJson.getString("html")
    }.build()

  private fun parseIntegerSchemaObject(value: Int): SchemaObject {
    return SchemaObject.newBuilder().setSignedInt(value).build()
  }

  private fun parseBooleanSchemaObject(value: Boolean): SchemaObject {
    return SchemaObject.newBuilder().setBoolValue(value).build()
  }

  private fun parseSubtitledHtmlListForCustomizationArgs(jsonArray: JSONArray): SchemaObject {
    val schemaObjectListBuilder = SchemaObjectList.newBuilder()
    for (i in 0 until jsonArray.length()) {
      schemaObjectListBuilder.addSchemaObject(
        SchemaObject.newBuilder().apply {
          customSchemaValue = CustomSchemaValue.newBuilder().apply {
            subtitledHtml = parseSubtitledHtml(jsonArray.getJSONObject(i))
          }.build()
        }.build()
      )
    }
    return SchemaObject.newBuilder().setSchemaObjectList(
      schemaObjectListBuilder.build()
    ).build()
  }

  private fun parseSubtitledUnicode(jsonObject: JSONObject): SchemaObject {
    val subtitledUnicodeBuilder = SubtitledUnicode.newBuilder()
    subtitledUnicodeBuilder.contentId = jsonObject.getString("content_id")
    subtitledUnicodeBuilder.unicodeStr = jsonObject.getString("unicode_str")
    val schemaObjectBuilder = SchemaObject.newBuilder()
    schemaObjectBuilder.setSubtitledUnicode(subtitledUnicodeBuilder)
    return schemaObjectBuilder.build()
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
      regionList += parseLabeledRegion(jsonArray.getJSONObject(i))
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
