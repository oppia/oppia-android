package org.oppia.android.scripts.gae.json

import com.squareup.moshi.FromJson
import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson
import org.oppia.proto.v1.structure.InteractionInstanceDto.InteractionTypeCase

@JsonClass(generateAdapter = false)
data class GaeInteractionInstance(
  val id: String?,
  val customizationArgs: GaeInteractionCustomizationArgsMap,
  val answerGroups: List<GaeAnswerGroup>,
  val defaultOutcome: GaeOutcome?,
  val confirmedUnclassifiedAnswers: List<@JvmSuppressWildcards GaeInteractionObject>,
  val hints: List<GaeHint>,
  val solution: GaeSolution?
) {
  fun computeReferencedSkillIds(): List<String> {
    return answerGroups.flatMap { it.computeReferencedSkillIds() } +
      listOfNotNull(defaultOutcome?.missingPrerequisiteSkillId)
  }

  class Adapter(private val typeResolutionContext: TypeResolutionContext) {
    @FromJson
    fun parseFromJson(
      jsonReader: JsonReader,
      parsableInteractionInstanceAdapter: JsonAdapter<ParsableInteractionInstance>
    ): GaeInteractionInstance {
      typeResolutionContext.currentInteractionType = jsonReader.peekInteractionId()
      return jsonReader.nextCustomValue(parsableInteractionInstanceAdapter).also {
        // Reset the interaction type now that parsing has completed.
        typeResolutionContext.currentInteractionType = null
      }.convertToGaeObject()
    }

    @ToJson
    fun convertToJson(
      jsonWriter: JsonWriter,
      gaeInteractionInstance: GaeInteractionInstance
    ): Unit = error("Conversion to JSON is not supported.")
  }

  @JsonClass(generateAdapter = true)
  data class ParsableInteractionInstance(
    @Json(name = "id") val id: String?,
    @Json(name = "customization_args") val customizationArgs: GaeInteractionCustomizationArgsMap,
    @Json(name = "answer_groups") val answerGroups: List<GaeAnswerGroup>,
    @Json(name = "default_outcome") val defaultOutcome: GaeOutcome?,
    @Json(name = "confirmed_unclassified_answers")
    @GaeInteractionObject.SolutionInteractionAnswer // TODO: Document that this is wrong (and can fail if Oppia ever uses it).
    val confirmedUnclassifiedAnswers: List<@JvmSuppressWildcards GaeInteractionObject>,
    @Json(name = "hints") val hints: List<GaeHint>,
    @Json(name = "solution") val solution: GaeSolution?
  ) {
    fun convertToGaeObject(): GaeInteractionInstance {
      return GaeInteractionInstance(
        id, customizationArgs, answerGroups, defaultOutcome, confirmedUnclassifiedAnswers, hints,
        solution
      )
    }
  }

  private companion object {
    private fun JsonReader.peekInteractionId(): InteractionTypeCase {
      return peekJson().use { jsonReader ->
        jsonReader.nextObject {
          if (it == "id") jsonReader.nextString() else null
        }["id"]?.let(::parseInteractionId) ?: error("Missing ID in interaction JSON object.")
      }
    }

    private fun parseInteractionId(id: String): InteractionTypeCase {
      return when (id) {
        "Continue" -> InteractionTypeCase.CONTINUE_INSTANCE
        "FractionInput" -> InteractionTypeCase.FRACTION_INPUT
        "ItemSelectionInput" -> InteractionTypeCase.ITEM_SELECTION_INPUT
        "MultipleChoiceInput" -> InteractionTypeCase.MULTIPLE_CHOICE_INPUT
        "NumericInput" -> InteractionTypeCase.NUMERIC_INPUT
        "TextInput" -> InteractionTypeCase.TEXT_INPUT
        "DragAndDropSortInput" -> InteractionTypeCase.DRAG_AND_DROP_SORT_INPUT
        "ImageClickInput" -> InteractionTypeCase.IMAGE_CLICK_INPUT
        "RatioExpressionInput" -> InteractionTypeCase.RATIO_EXPRESSION_INPUT
        "NumericExpressionInput" -> InteractionTypeCase.NUMERIC_EXPRESSION_INPUT
        "AlgebraicExpressionInput" -> InteractionTypeCase.ALGEBRAIC_EXPRESSION_INPUT
        "MathEquationInput" -> InteractionTypeCase.MATH_EQUATION_INPUT
        "EndExploration" -> InteractionTypeCase.END_EXPLORATION
        else -> error("Unsupported interaction ID: $id.")
      }
    }
  }
}
