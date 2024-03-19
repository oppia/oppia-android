package org.oppia.android.scripts.gae.json

import com.squareup.moshi.FromJson
import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.JsonQualifier
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson
import org.oppia.proto.v1.structure.InteractionInstanceDto.InteractionTypeCase.ALGEBRAIC_EXPRESSION_INPUT
import org.oppia.proto.v1.structure.InteractionInstanceDto.InteractionTypeCase.CONTINUE_INSTANCE
import org.oppia.proto.v1.structure.InteractionInstanceDto.InteractionTypeCase.DRAG_AND_DROP_SORT_INPUT
import org.oppia.proto.v1.structure.InteractionInstanceDto.InteractionTypeCase.END_EXPLORATION
import org.oppia.proto.v1.structure.InteractionInstanceDto.InteractionTypeCase.FRACTION_INPUT
import org.oppia.proto.v1.structure.InteractionInstanceDto.InteractionTypeCase.IMAGE_CLICK_INPUT
import org.oppia.proto.v1.structure.InteractionInstanceDto.InteractionTypeCase.INTERACTIONTYPE_NOT_SET
import org.oppia.proto.v1.structure.InteractionInstanceDto.InteractionTypeCase.ITEM_SELECTION_INPUT
import org.oppia.proto.v1.structure.InteractionInstanceDto.InteractionTypeCase.MATH_EQUATION_INPUT
import org.oppia.proto.v1.structure.InteractionInstanceDto.InteractionTypeCase.MULTIPLE_CHOICE_INPUT
import org.oppia.proto.v1.structure.InteractionInstanceDto.InteractionTypeCase.NUMERIC_EXPRESSION_INPUT
import org.oppia.proto.v1.structure.InteractionInstanceDto.InteractionTypeCase.NUMERIC_INPUT
import org.oppia.proto.v1.structure.InteractionInstanceDto.InteractionTypeCase.RATIO_EXPRESSION_INPUT
import org.oppia.proto.v1.structure.InteractionInstanceDto.InteractionTypeCase.TEXT_INPUT

@JsonClass(generateAdapter = false)
sealed class GaeInteractionObject {
  data class NormalizedString(val value: String) : GaeInteractionObject()

  data class MathExpression(val value: String) : GaeInteractionObject()

  data class SignedInt(val value: Int) : GaeInteractionObject()

  data class NonNegativeInt(val value: Int) : GaeInteractionObject()

  data class Real(val value: Double) : GaeInteractionObject()

  @JsonClass(generateAdapter = false)
  data class TranslatableHtmlContentId(val contentId: String) : GaeInteractionObject() {
    class Adapter {
      @FromJson
      fun parseFromJson(jsonReader: JsonReader): TranslatableHtmlContentId =
        TranslatableHtmlContentId(jsonReader.nextString())

      @ToJson
      fun convertToJson(
        jsonWriter: JsonWriter,
        translatableHtmlContentId: TranslatableHtmlContentId
      ) {
        jsonWriter.value(translatableHtmlContentId.contentId)
      }
    }
  }

  @JsonClass(generateAdapter = false)
  data class SetOfXlatableContentIds(
    val contentIds: List<TranslatableHtmlContentId>
  ) : GaeInteractionObject() {
    class Adapter {
      @FromJson
      fun parseFromJson(
        jsonReader: JsonReader,
        translatableHtmlContentIdAdapter: JsonAdapter<TranslatableHtmlContentId>
      ): SetOfXlatableContentIds {
        val contentIds = jsonReader.nextArray {
          jsonReader.nextCustomValue(translatableHtmlContentIdAdapter)
        }
        return SetOfXlatableContentIds(contentIds)
      }

      @ToJson
      fun convertToJson(
        jsonWriter: JsonWriter,
        setOfXlatableContentIds: SetOfXlatableContentIds,
        translatableHtmlContentIdAdapter: JsonAdapter<TranslatableHtmlContentId>
      ) {
        jsonWriter.beginArray()
        setOfXlatableContentIds.contentIds.forEach {
          translatableHtmlContentIdAdapter.toJson(jsonWriter, it)
        }
        jsonWriter.endArray()
      }
    }
  }

  @JsonClass(generateAdapter = true)
  data class TranslatableSetOfNormalizedString(
    @Json(name = "contentId") val contentId: String?,
    @Json(name = "normalizedStrSet") val normalizedStrSet: List<String>
  ) : GaeInteractionObject()

  @JsonClass(generateAdapter = true)
  data class Fraction(
    @Json(name = "isNegative") val isNegative: Boolean,
    @Json(name = "wholeNumber") val wholeNumber: Int,
    @Json(name = "numerator") val numerator: Int,
    @Json(name = "denominator") val denominator: Int
  ) : GaeInteractionObject()

  @JsonClass(generateAdapter = false)
  data class SetsOfXlatableContentIds(
    val sets: List<SetOfXlatableContentIds>
  ) : GaeInteractionObject() {
    class Adapter {
      @FromJson
      fun parseFromJson(
        jsonReader: JsonReader,
        setOfXlatableContentIdsAdapter: JsonAdapter<SetOfXlatableContentIds>
      ): SetsOfXlatableContentIds {
        val contentIdSets = jsonReader.nextArray {
          jsonReader.nextCustomValue(setOfXlatableContentIdsAdapter)
        }
        return SetsOfXlatableContentIds(contentIdSets)
      }

      @ToJson
      fun convertToJson(
        jsonWriter: JsonWriter,
        setsOfXlatableContentIds: SetsOfXlatableContentIds,
        setOfXlatableContentIdsAdapter: JsonAdapter<SetOfXlatableContentIds>
      ) {
        jsonWriter.beginArray()
        setsOfXlatableContentIds.sets.forEach {
          setOfXlatableContentIdsAdapter.toJson(jsonWriter, it)
        }
        jsonWriter.endArray()
      }
    }
  }

  @JsonClass(generateAdapter = false)
  data class RatioExpression(val ratioComponents: List<Int>) : GaeInteractionObject() {
    class Adapter {
      @FromJson
      fun parseFromJson(jsonReader: JsonReader): RatioExpression =
        RatioExpression(jsonReader.nextArray(jsonReader::nextInt))

      @ToJson
      fun convertToJson(jsonWriter: JsonWriter, ratioExpression: RatioExpression) {
        jsonWriter.beginArray()
        ratioExpression.ratioComponents.forEach { jsonWriter.value(it.toLong()) }
        jsonWriter.endArray()
      }
    }
  }

  @Retention(AnnotationRetention.RUNTIME)
  @JsonQualifier
  annotation class SolutionInteractionAnswer

  @Retention(AnnotationRetention.RUNTIME)
  @JsonQualifier
  annotation class RuleInput

  class Adapter(private val typeResolutionContext: TypeResolutionContext) {
    @RuleInput
    @FromJson
    fun parseRuleInputObjectFromJson(
      jsonReader: JsonReader,
      setOfXlatableHtmlContentIdsAdapter: JsonAdapter<SetOfXlatableContentIds>,
      fractionAdapter: JsonAdapter<Fraction>,
      listSetsOfXlatableIdsAdapter: JsonAdapter<SetsOfXlatableContentIds>,
      ratioExpressionAdapter: JsonAdapter<RatioExpression>,
      translatableStrSetAdapter: JsonAdapter<TranslatableSetOfNormalizedString>,
      translatableHtmlContentIdAdapter: JsonAdapter<TranslatableHtmlContentId>
    ): GaeInteractionObject {
      return when (val currentInteractionType = typeResolutionContext.expectedInteractionType) {
        FRACTION_INPUT -> parseFractionInputJson(jsonReader, fractionAdapter)
        ITEM_SELECTION_INPUT ->
          parseItemSelectionInputJson(jsonReader, setOfXlatableHtmlContentIdsAdapter)
        MULTIPLE_CHOICE_INPUT -> parseMultipleChoiceInputJson(jsonReader)
        NUMERIC_INPUT -> parseNumericInputJson(jsonReader)
        TEXT_INPUT -> parseTextInputJson(jsonReader, translatableStrSetAdapter)
        DRAG_AND_DROP_SORT_INPUT -> {
          parseDragAndDropSortInputJson(
            jsonReader, listSetsOfXlatableIdsAdapter, translatableHtmlContentIdAdapter
          )
        }
        IMAGE_CLICK_INPUT -> parseImageClickInputJson(jsonReader)
        RATIO_EXPRESSION_INPUT -> parseRatioExpressionInputJson(jsonReader, ratioExpressionAdapter)
        ALGEBRAIC_EXPRESSION_INPUT -> parseAlgebraicExpressionInputInputJson(jsonReader)
        MATH_EQUATION_INPUT -> parseMathEquationInputInputJson(jsonReader)
        NUMERIC_EXPRESSION_INPUT -> parseNumericExpressionInputJson(jsonReader)
        END_EXPLORATION, CONTINUE_INSTANCE, INTERACTIONTYPE_NOT_SET ->
          error("Unsupported interaction: $currentInteractionType.")
      }
    }

    @ToJson
    fun convertRuleInputToJson(
      jsonWriter: JsonWriter,
      @RuleInput gaeInteractionObject: GaeInteractionObject,
      setOfXlatableHtmlContentIdsAdapter: JsonAdapter<SetOfXlatableContentIds>,
      fractionAdapter: JsonAdapter<Fraction>,
      listSetsOfXlatableIdsAdapter: JsonAdapter<SetsOfXlatableContentIds>,
      ratioExpressionAdapter: JsonAdapter<RatioExpression>,
      translatableStrSetAdapter: JsonAdapter<TranslatableSetOfNormalizedString>,
      translatableHtmlContentIdAdapter: JsonAdapter<TranslatableHtmlContentId>
    ) {
      when (gaeInteractionObject) {
        is Fraction -> fractionAdapter.toJson(jsonWriter, gaeInteractionObject)
        is MathExpression -> jsonWriter.value(gaeInteractionObject.value)
        is NonNegativeInt -> jsonWriter.value(gaeInteractionObject.value.toLong())
        is NormalizedString -> jsonWriter.value(gaeInteractionObject.value)
        is RatioExpression -> ratioExpressionAdapter.toJson(jsonWriter, gaeInteractionObject)
        is Real -> jsonWriter.value(gaeInteractionObject.value)
        is SetOfXlatableContentIds ->
          setOfXlatableHtmlContentIdsAdapter.toJson(jsonWriter, gaeInteractionObject)
        is SetsOfXlatableContentIds ->
          listSetsOfXlatableIdsAdapter.toJson(jsonWriter, gaeInteractionObject)
        is SignedInt -> jsonWriter.value(gaeInteractionObject.value.toLong())
        is TranslatableHtmlContentId ->
          translatableHtmlContentIdAdapter.toJson(jsonWriter, gaeInteractionObject)
        is TranslatableSetOfNormalizedString ->
          translatableStrSetAdapter.toJson(jsonWriter, gaeInteractionObject)
      }
    }

    @SolutionInteractionAnswer
    @FromJson
    fun parseSolutionFromJson(
      jsonReader: JsonReader,
      setOfXlatableHtmlContentIdsAdapter: JsonAdapter<SetOfXlatableContentIds>,
      fractionAdapter: JsonAdapter<Fraction>,
      listSetsOfXlatableIdsAdapter: JsonAdapter<SetsOfXlatableContentIds>,
      ratioExpressionAdapter: JsonAdapter<RatioExpression>
    ): GaeInteractionObject {
      return when (val currentInteractionType = typeResolutionContext.expectedInteractionType) {
        FRACTION_INPUT -> jsonReader.nextCustomValue(fractionAdapter)
        ITEM_SELECTION_INPUT -> jsonReader.nextCustomValue(setOfXlatableHtmlContentIdsAdapter)
        MULTIPLE_CHOICE_INPUT -> NonNegativeInt(jsonReader.nextInt())
        NUMERIC_INPUT -> Real(jsonReader.nextDouble())
        TEXT_INPUT -> NormalizedString(jsonReader.nextString())
        DRAG_AND_DROP_SORT_INPUT -> jsonReader.nextCustomValue(listSetsOfXlatableIdsAdapter)
        RATIO_EXPRESSION_INPUT -> jsonReader.nextCustomValue(ratioExpressionAdapter)
        ALGEBRAIC_EXPRESSION_INPUT, MATH_EQUATION_INPUT, NUMERIC_EXPRESSION_INPUT ->
          MathExpression(jsonReader.nextString())
        IMAGE_CLICK_INPUT, END_EXPLORATION, CONTINUE_INSTANCE, INTERACTIONTYPE_NOT_SET ->
          error("Unsupported interaction: $currentInteractionType.")
      }
    }

    @ToJson
    fun convertInteractionAnswerToJson(
      jsonWriter: JsonWriter,
      @SolutionInteractionAnswer gaeInteractionObject: GaeInteractionObject,
      @RuleInput ruleInputObjectAdapter: JsonAdapter<GaeInteractionObject>
    ) = ruleInputObjectAdapter.toJson(jsonWriter, gaeInteractionObject)

    @SolutionInteractionAnswer
    @FromJson
    fun parseSolutionListFromJson(
      jsonReader: JsonReader,
      @SolutionInteractionAnswer solutionAdapter: JsonAdapter<GaeInteractionObject>
    ): List<@JvmSuppressWildcards GaeInteractionObject> {
      return jsonReader.nextArray { jsonReader.nextCustomValue(solutionAdapter) }
    }

    @ToJson
    fun convertSolutionListToJson(
      jsonWriter: JsonWriter,
      @SolutionInteractionAnswer
      gaeInteractionObjects: List<@JvmSuppressWildcards GaeInteractionObject>,
      @SolutionInteractionAnswer solutionAdapter: JsonAdapter<GaeInteractionObject>
    ) {
      jsonWriter.beginArray()
      gaeInteractionObjects.forEach { solutionAdapter.toJson(jsonWriter, it) }
      jsonWriter.endArray()
    }

    private fun parseDragAndDropSortInputJson(
      jsonReader: JsonReader,
      listSetsOfXlatableIdsAdapter: JsonAdapter<SetsOfXlatableContentIds>,
      translatableHtmlContentIdAdapter: JsonAdapter<TranslatableHtmlContentId>
    ): GaeInteractionObject {
      val currentInteractionType = typeResolutionContext.expectedInteractionType
      val currentInputName = typeResolutionContext.expectedRuleInputName
      return when (val currentRuleType = typeResolutionContext.expectedRuleTypeName) {
        "IsEqualToOrdering", "IsEqualToOrderingWithOneItemAtIncorrectPosition" ->
          jsonReader.nextCustomValue(listSetsOfXlatableIdsAdapter)
        "HasElementXAtPositionY" -> when (currentInputName) {
          "x" -> jsonReader.nextCustomValue(translatableHtmlContentIdAdapter)
          "y" -> NonNegativeInt(jsonReader.nextInt())
          else -> {
            error(
              "Unexpected param $currentInputName in rule type $currentRuleType " +
                "for $currentInteractionType."
            )
          }
        }
        "HasElementXBeforeElementY" -> jsonReader.nextCustomValue(translatableHtmlContentIdAdapter)
        else -> error("Unsupported rule type: $currentRuleType for: $currentInteractionType.")
      }
    }

    private fun parseFractionInputJson(
      jsonReader: JsonReader,
      fractionAdapter: JsonAdapter<Fraction>
    ): GaeInteractionObject {
      val currentInteractionType = typeResolutionContext.expectedInteractionType
      return when (val currentRuleType = typeResolutionContext.expectedRuleTypeName) {
        "IsExactlyEqualTo", "IsEquivalentTo", "IsEquivalentToAndInSimplestForm", "IsLessThan",
        "IsGreaterThan", "HasFractionalPartExactlyEqualTo" ->
          jsonReader.nextCustomValue(fractionAdapter)
        "HasNumeratorEqualTo", "HasIntegerPartEqualTo" -> SignedInt(jsonReader.nextInt())
        "HasDenominatorEqualTo" -> NonNegativeInt(jsonReader.nextInt())
        "HasNoFractionalPart" -> error("$currentRuleType should not have an answer to parse.")
        else -> error("Unsupported rule type: $currentRuleType for: $currentInteractionType.")
      }
    }

    private fun parseImageClickInputJson(jsonReader: JsonReader): GaeInteractionObject {
      val currentInteractionType = typeResolutionContext.expectedInteractionType
      return when (val currentRuleType = typeResolutionContext.expectedRuleTypeName) {
        "IsInRegion" -> NormalizedString(jsonReader.nextString())
        else -> error("Unsupported rule type: $currentRuleType for: $currentInteractionType.")
      }
    }

    private fun parseItemSelectionInputJson(
      jsonReader: JsonReader,
      setOfXlatableHtmlContentIdsAdapter: JsonAdapter<SetOfXlatableContentIds>
    ): GaeInteractionObject {
      val currentInteractionType = typeResolutionContext.expectedInteractionType
      return when (val currentRuleType = typeResolutionContext.expectedRuleTypeName) {
        "Equals", "ContainsAtLeastOneOf", "DoesNotContainAtLeastOneOf", "IsProperSubsetOf" ->
          jsonReader.nextCustomValue(setOfXlatableHtmlContentIdsAdapter)
        else -> error("Unsupported rule type: $currentRuleType for: $currentInteractionType.")
      }
    }

    private fun parseRatioExpressionInputJson(
      jsonReader: JsonReader,
      ratioExpressionAdapter: JsonAdapter<RatioExpression>
    ): GaeInteractionObject {
      val currentInteractionType = typeResolutionContext.expectedInteractionType
      return when (val currentRuleType = typeResolutionContext.expectedRuleTypeName) {
        "Equals", "IsEquivalent" -> jsonReader.nextCustomValue(ratioExpressionAdapter)
        "HasNumberOfTermsEqualTo", "HasSpecificTermEqualTo" -> NonNegativeInt(jsonReader.nextInt())
        else -> error("Unsupported rule type: $currentRuleType for: $currentInteractionType.")
      }
    }

    private fun parseMultipleChoiceInputJson(jsonReader: JsonReader): GaeInteractionObject {
      val currentInteractionType = typeResolutionContext.expectedInteractionType
      return when (val currentRuleType = typeResolutionContext.expectedRuleTypeName) {
        "Equals" -> NonNegativeInt(jsonReader.nextInt())
        else -> error("Unsupported rule type: $currentRuleType for: $currentInteractionType.")
      }
    }

    private fun parseNumericInputJson(jsonReader: JsonReader): GaeInteractionObject {
      val currentInteractionType = typeResolutionContext.expectedInteractionType
      return when (val currentRuleType = typeResolutionContext.expectedRuleTypeName) {
        "Equals", "IsLessThan", "IsGreaterThan", "IsLessThanOrEqualTo", "IsGreaterThanOrEqualTo",
        "IsInclusivelyBetween", "IsWithinTolerance" -> Real(jsonReader.nextDouble())
        else -> error("Unsupported rule type: $currentRuleType for: $currentInteractionType.")
      }
    }

    private fun parseTextInputJson(
      jsonReader: JsonReader,
      translatableStrSetAdapter: JsonAdapter<TranslatableSetOfNormalizedString>
    ): GaeInteractionObject {
      val currentInteractionType = typeResolutionContext.expectedInteractionType
      return when (val currentRuleType = typeResolutionContext.expectedRuleTypeName) {
        "Equals", "StartsWith", "Contains", "FuzzyEquals" ->
          jsonReader.nextCustomValue(translatableStrSetAdapter)
        else -> error("Unsupported rule type: $currentRuleType for: $currentInteractionType.")
      }
    }

    private fun parseNumericExpressionInputJson(jsonReader: JsonReader) =
      parseMathExpressionInputInputJson(jsonReader)

    private fun parseAlgebraicExpressionInputInputJson(jsonReader: JsonReader) =
      parseMathExpressionInputInputJson(jsonReader)

    private fun parseMathEquationInputInputJson(jsonReader: JsonReader) =
      parseMathExpressionInputInputJson(jsonReader)

    private fun parseMathExpressionInputInputJson(jsonReader: JsonReader): GaeInteractionObject {
      val currentInteractionType = typeResolutionContext.expectedInteractionType
      return when (val currentRuleType = typeResolutionContext.expectedRuleTypeName) {
        "MatchesExactlyWith", "IsEquivalentTo", "MatchesUpToTrivialManipulations" ->
          MathExpression(jsonReader.nextString())
        else -> error("Unsupported rule type: $currentRuleType for $currentInteractionType.")
      }
    }
  }
}
