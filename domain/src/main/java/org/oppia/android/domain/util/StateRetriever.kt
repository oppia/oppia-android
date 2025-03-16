package org.oppia.android.domain.util

import org.json.JSONArray
import org.json.JSONObject
import org.oppia.android.app.model.AnswerGroup
import org.oppia.android.app.model.CustomSchemaValue
import org.oppia.android.app.model.Fraction
import org.oppia.android.app.model.Hint
import org.oppia.android.app.model.HtmlTranslationList
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
import org.oppia.android.app.model.Translation
import org.oppia.android.app.model.TranslationMapping
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
      if (stateJson.has("written_translations")) {
        putAllWrittenTranslations(
          createWrittenTranslationMappingsFromJson(stateJson.getJSONObject("written_translations"))
        )
      }
      stateJson.optString("linked_skill_id").takeIf {
        it.isNotEmpty() && it != "null"
      }?.let { linkedSkillId = it }
    }.build()

  // Creates an interaction from JSON
  private fun createInteractionFromJson(interactionJson: JSONObject): Interaction {
    return Interaction.newBuilder().apply {
      id = interactionJson.getStringFromObject("id")
      addAllAnswerGroups(
        createAnswerGroupsFromJson(
          interactionJson.getJSONArray("answer_groups"),
          interactionJson.getStringFromObject("id")
        )
      )
      defaultOutcome = createOutcomeFromJson(interactionJson.optJSONObject("default_outcome"))
      putAllCustomizationArgs(
        createCustomizationArgsMapFromJson(
          interactionJson.getJSONObject("customization_args"),
          interactionJson.getStringFromObject("id")
        )
      )
      addAllHint(createListOfHintsFromJson(interactionJson.getJSONArray("hints")))

      // Only set the solution if one has been defined.
      createSolutionFromJson(interactionJson.optJSONObject("solution"), id)?.let { solution = it }
    }.build()
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
    val misconceptionJson =
      if (answerGroupJson.isNull("tagged_skill_misconception_id")) null
      else answerGroupJson.getStringFromObject("tagged_skill_misconception_id")
    if (!misconceptionJson.isNullOrEmpty()) {
      val misconceptionParts = misconceptionJson.split("-")
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
      .setDestStateName(outcomeJson.getStringFromObject("dest"))
      .setFeedback(createFeedbackSubtitledHtml(outcomeJson))
      .setLabelledAsCorrect(outcomeJson.getBoolean("labelled_as_correct"))
      .build()
  }

  // Creates a solution object from JSON
  private fun createSolutionFromJson(
    optionalSolutionJson: JSONObject?,
    interactionId: String
  ): Solution? {
    return optionalSolutionJson?.let { solutionJson ->
      return Solution.newBuilder().apply {
        correctAnswer =
          parseExactSolutionObject(solutionJson.optJSONObject("correct_answer"), interactionId)
        explanation = parseSubtitledHtml(solutionJson.getJSONObject("explanation"))
        answerIsExclusive = solutionJson.getBoolean("answer_is_exclusive")
      }.build()
    }
  }

  /**
   * Returns a new [SubtitledHtml] from a specified container [JSONObject] that contains an entry
   * keyed on 'feedback'.
   */
  private fun createFeedbackSubtitledHtml(containerObject: JSONObject): SubtitledHtml {
    val feedbackObject = containerObject.getJSONObject("feedback")
    return SubtitledHtml.newBuilder()
      .setContentId(feedbackObject.getStringFromObject("content_id"))
      .setHtml(feedbackObject.getStringFromObject("html"))
      .build()
  }

  private fun createVoiceoverMappingsFromJson(
    recordedVoiceovers: JSONObject
  ): Map<String, VoiceoverMapping> {
    val voiceoverMappingJson = recordedVoiceovers.getJSONObject("voiceovers_mapping")
    return voiceoverMappingJson.keys().asSequence().filter { contentId ->
      voiceoverMappingJson.getJSONObject(contentId).length() != 0
    }.associateWith { contentId ->
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
      fileName = voiceoverJson.getStringFromObject("filename")
    }.build()

  private fun createWrittenTranslationMappingsFromJson(
    writtenTranslations: JSONObject
  ): Map<String, TranslationMapping> {
    val translationsMappingJson = writtenTranslations.getJSONObject("translations_mapping")
    return translationsMappingJson.keys().asSequence().filter { contentId ->
      translationsMappingJson.getJSONObject(contentId).length() != 0
    }.associateWith { contentId ->
      val translationJson = translationsMappingJson.getJSONObject(contentId)
      TranslationMapping.newBuilder().apply {
        putAllTranslationMapping(
          translationJson.keys().asSequence().associateWith { languageCode ->
            createTranslationFromJson(translationJson.getJSONObject(languageCode))
          }
        )
      }.build()
    }
  }

  private fun createTranslationFromJson(translatorJson: JSONObject): Translation =
    Translation.newBuilder().apply {
      val translationJson = translatorJson.getJSONObject("translation")
      needsUpdate = translatorJson.getBoolean("needs_update")
      when (val dataFormat = translatorJson.getStringFromObject("data_format")) {
        "html", "unicode" -> html = translationJson.getStringFromObject("translation")
        "set_of_normalized_string", "set_of_unicode_string" -> {
          val array = translationJson.getJSONArray("translations")
          htmlList = HtmlTranslationList.newBuilder().apply {
            for (i in 0 until array.length()) {
              addHtml(array.getStringFromArray(i))
            }
          }.build()
        }
        else -> error("Unsupported data format: $dataFormat")
      }
    }.build()

  // Creates the list of rule spec objects from JSON
  private fun convertToRuleSpec(ruleSpecJson: JSONObject, interactionId: String): RuleSpec {
    val inputJsonObject = ruleSpecJson.getJSONObject("inputs")
    return RuleSpec.newBuilder().apply {
      ruleType = ruleSpecJson.getStringFromObject("rule_type")
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
      "MultipleChoiceInput" -> {
        InteractionObject.newBuilder().apply {
          nonNegativeInt = inputJson.getInt(keyName)
        }.build()
      }
      "ItemSelectionInput" -> {
        InteractionObject.newBuilder().apply {
          setOfTranslatableHtmlContentIds =
            parseSetOfTranslatableHtmlContentIds(inputJson.getJSONArray(keyName))
        }.build()
      }
      "TextInput" -> {
        InteractionObject.newBuilder().apply {
          translatableSetOfNormalizedString =
            parseTranslatableSetOfNormalizedString(inputJson.getJSONObject(keyName))
        }.build()
      }
      "NumberWithUnits" -> {
        InteractionObject.newBuilder().apply {
          numberWithUnits = parseNumberWithUnitsObject(inputJson.getJSONObject(keyName))
        }.build()
      }
      "NumericInput" -> {
        InteractionObject.newBuilder().apply {
          real = inputJson.getDouble(keyName)
        }.build()
      }
      "FractionInput" -> createExactInputForFractionInput(inputJson, keyName, ruleType)
      "DragAndDropSortInput" -> createExactInputForDragDropAndSort(inputJson, keyName, ruleType)
      "ImageClickInput" -> {
        InteractionObject.newBuilder().apply {
          normalizedString = inputJson.getStringFromObject(keyName)
        }.build()
      }
      "RatioExpressionInput" ->
        createExactInputForRatioExpressionInput(inputJson, keyName, ruleType)
      "NumericExpressionInput", "AlgebraicExpressionInput", "MathEquationInput" -> {
        InteractionObject.newBuilder().apply {
          mathExpression = inputJson.getStringFromObject(keyName)
        }.build()
      }
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
      "HasIntegerPartEqualTo" ->
        InteractionObject.newBuilder()
          .setSignedInt(inputJson.getInt(keyName))
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
              parseTranslatableContentId(inputJson.getStringFromObject(keyName))
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
            parseTranslatableContentId(inputJson.getStringFromObject(keyName))
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

  private fun parseExactSolutionObject(
    inputJson: JSONObject?,
    interactionId: String
  ): InteractionObject {
    if (inputJson == null) return InteractionObject.getDefaultInstance()
    return when (interactionId) {
      "TextInput" -> {
        InteractionObject.newBuilder().apply {
          normalizedString = inputJson.getStringFromObject("normalized_string")
        }.build()
      }
      "NumericInput" -> {
        InteractionObject.newBuilder().apply {
          real = inputJson.getDouble("real")
        }.build()
      }
      "FractionInput" -> {
        InteractionObject.newBuilder().apply {
          fraction = parseFraction(inputJson.getJSONObject("fraction"))
        }.build()
      }
      "RatioExpressionInput" -> {
        InteractionObject.newBuilder().apply {
          ratioExpression = parseRatio(inputJson.getJSONArray("ratio_expression"))
        }.build()
      }
      "NumericExpressionInput", "AlgebraicExpressionInput", "MathEquationInput" -> {
        InteractionObject.newBuilder().apply {
          mathExpression = inputJson.getStringFromObject("math_expression")
        }.build()
      }
      else -> error("Encountered unsupported interaction ID for solutions: $interactionId")
    }
  }

  private fun parseTranslatableSetOfNormalizedString(
    translatableSetOfStringsJson: JSONObject
  ): TranslatableSetOfNormalizedString = TranslatableSetOfNormalizedString.newBuilder().apply {
    contentId = translatableSetOfStringsJson.getStringFromObject("contentId")
    val strSet = translatableSetOfStringsJson.getJSONArray("normalizedStrSet")
    for (i in 0 until strSet.length()) {
      addNormalizedStrings(strSet.getStringFromArray(i))
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
        parseTranslatableContentId(setOfContentIdsJson.getStringFromArray(i))
      )
    }
    return setOfContentIdsBuilder.build()
  }

  private fun parseTranslatableContentId(contentIdJson: String): TranslatableHtmlContentId =
    TranslatableHtmlContentId.newBuilder().apply { contentId = contentIdJson }.build()

  private fun parseNumberWithUnitsObject(numberWithUnitsAnswer: JSONObject): NumberWithUnits {
    val numberWithUnitsBuilder = NumberWithUnits.newBuilder()
    when (numberWithUnitsAnswer.getStringFromObject("type")) {
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
          .setUnit(unit.getStringFromObject("unit"))
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
      "NumericExpressionInput" -> {
        createNumericExpressionInputCustomizationArgsMap(customizationArgsJson)
      }
      "AlgebraicExpressionInput", "MathEquationInput" -> {
        createAlgebraicExpressionMathEquationInputsCustomizationArgsMap(customizationArgsJson)
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
    customizationArgsJson.optJSONObject("placeholder")?.optJSONObject("value")?.let { placeholder ->
      customizationArgsMap["placeholder"] = parseSubtitledUnicode(placeholder)
    }
    customizationArgsJson.optJSONObject("rows")?.optInt("value")?.let { rows ->
      customizationArgsMap["rows"] = parseIntegerSchemaObject(rows)
    }
    return customizationArgsMap
  }

  private fun createNumericExpressionInputCustomizationArgsMap(
    customizationArgsJson: JSONObject
  ): Map<String, SchemaObject> {
    val customizationArgsMap = mutableMapOf<String, SchemaObject>()
    if (customizationArgsJson.has("placeholder")) {
      customizationArgsMap["placeholder"] =
        parseSubtitledUnicode(
          customizationArgsJson.getJSONObject("placeholder").getJSONObject("value")
        )
    }
    if (customizationArgsJson.has("useFractionForDivision")) {
      customizationArgsMap["useFractionForDivision"] =
        parseBooleanSchemaObject(
          customizationArgsJson.getJSONObject("useFractionForDivision").getBoolean("value")
        )
    }
    return customizationArgsMap
  }

  private fun createAlgebraicExpressionMathEquationInputsCustomizationArgsMap(
    customizationArgsJson: JSONObject
  ): Map<String, SchemaObject> {
    val customizationArgsMap = mutableMapOf<String, SchemaObject>()
    if (customizationArgsJson.has("customOskLetters")) {
      customizationArgsMap["customOskLetters"] =
        parseCustomOskLetters(
          customizationArgsJson.getJSONObject("customOskLetters").getJSONArray("value")
        )
    }
    if (customizationArgsJson.has("useFractionForDivision")) {
      customizationArgsMap["useFractionForDivision"] =
        parseBooleanSchemaObject(
          customizationArgsJson.getJSONObject("useFractionForDivision").getBoolean("value")
        )
    }
    return customizationArgsMap
  }

  private fun parseSubtitledHtml(subtitledHtmlJson: JSONObject): SubtitledHtml =
    SubtitledHtml.newBuilder().apply {
      contentId = subtitledHtmlJson.getStringFromObject("content_id")
      html = subtitledHtmlJson.getStringFromObject("html")
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
    subtitledUnicodeBuilder.contentId = jsonObject.getStringFromObject("content_id")
    subtitledUnicodeBuilder.unicodeStr = jsonObject.getStringFromObject("unicode_str")
    val schemaObjectBuilder = SchemaObject.newBuilder()
    schemaObjectBuilder.setSubtitledUnicode(subtitledUnicodeBuilder)
    return schemaObjectBuilder.build()
  }

  private fun parseImageWithRegions(jsonObject: JSONObject): SchemaObject {
    val imageWithRegions = ImageWithRegions.newBuilder()
      .addAllLabelRegions(parseJsonToLabeledRegionsList(jsonObject.getJSONArray("labeledRegions")))
      .setImagePath(jsonObject.getStringFromObject("imagePath"))
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
      .setLabel(jsonObject.getStringFromObject("label"))
      .setContentDescription(jsonObject.getStringFromObject("contentDescription"))
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

  private fun parseCustomOskLetters(jsonArray: JSONArray): SchemaObject {
    val letters = mutableListOf<String>()
    for (i in 0 until jsonArray.length()) {
      letters += jsonArray.getStringFromArray(i)
    }
    return SchemaObject.newBuilder().apply {
      schemaObjectList = SchemaObjectList.newBuilder().apply {
        addAllSchemaObject(
          letters.map { letter ->
            SchemaObject.newBuilder().apply {
              normalizedString = letter
            }.build()
          }
        )
      }.build()
    }.build()
  }
}
