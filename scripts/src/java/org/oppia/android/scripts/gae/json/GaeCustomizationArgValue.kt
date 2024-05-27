package org.oppia.android.scripts.gae.json

import com.squareup.moshi.FromJson
import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson
import org.oppia.android.scripts.gae.proto.CustomizationArgValue
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

// TODO: Mention parsing this requires setting customization key name & interaction type in the parsing context.
sealed class GaeCustomizationArgValue {
  // TODO: Remove CustomizationArgValue.
  protected abstract val valueType: CustomizationArgValue.ValueTypeCase

  protected open fun populateValue(builder: CustomizationArgValue.Builder) {}

  data class SingleInteger(val value: Int) : GaeCustomizationArgValue() {
    override val valueType = CustomizationArgValue.ValueTypeCase.INTEGER

    override fun populateValue(builder: CustomizationArgValue.Builder) {
      builder.integer = value
    }
  }

  data class SingleBoolean(val value: Boolean) : GaeCustomizationArgValue() {
    override val valueType = CustomizationArgValue.ValueTypeCase.BOOLEAN

    override fun populateValue(builder: CustomizationArgValue.Builder) {
      builder.boolean = value
    }
  }

  data class SubtitledUnicode(val value: GaeSubtitledUnicode) : GaeCustomizationArgValue() {
    override val valueType = CustomizationArgValue.ValueTypeCase.SUBTITLED_TEXT_DTO
  }

  data class StringList(val value: List<String>) : GaeCustomizationArgValue() {
    override val valueType = CustomizationArgValue.ValueTypeCase.STRING_LIST
  }

  data class SubtitledTextList(val value: List<GaeSubtitledHtml>) : GaeCustomizationArgValue() {
    override val valueType = CustomizationArgValue.ValueTypeCase.SUBTITLED_TEXT_LIST
  }

  @JsonClass(generateAdapter = true)
  data class GaeImageWithRegions(
    @Json(name = "imagePath") val imagePath: String,
    @Json(name = "labeledRegions") val labeledRegions: List<GaeLabeledRegion>
  ) : GaeCustomizationArgValue() {
    override val valueType = CustomizationArgValue.ValueTypeCase.IMAGE_WITH_REGIONS_DTO

    @JsonClass(generateAdapter = true)
    data class GaeLabeledRegion(
      @Json(name = "label") val label: String,
      @Json(name = "region") val region: GaeImageRegion,
      @Json(name = "contentDescription") val contentDescription: String?
    ) {
      @JsonClass(generateAdapter = true)
      data class GaeImageRegion(
        @Json(name = "regionType") val regionType: String,
        @Json(name = "area") val area: GaeNormalizedRectangle2d
      )

      @JsonClass(generateAdapter = false)
      data class GaeNormalizedRectangle2d(val items: List<List<Double>>) {
        class Adapter {
          @FromJson
          fun parseFromJson(jsonReader: JsonReader): GaeNormalizedRectangle2d {
            val (upperLeftPoint, lowerRightPoint) = jsonReader.nextArray {
              jsonReader.nextArray(jsonReader::nextDouble)
            }
            val (upperLeftX, upperLeftY) = upperLeftPoint
            val (lowerRightX, lowerRightY) = lowerRightPoint
            return GaeNormalizedRectangle2d(
              listOf(listOf(upperLeftX, upperLeftY), listOf(lowerRightX, lowerRightY))
            )
          }

          @ToJson
          fun convertToJson(
            jsonWriter: JsonWriter,
            gaeNormalizedRectangle2d: GaeNormalizedRectangle2d
          ) {
            val (upperLeftPoint, lowerRightPoint) = gaeNormalizedRectangle2d.items
            val (upperLeftX, upperLeftY) = upperLeftPoint
            val (lowerRightX, lowerRightY) = lowerRightPoint

            jsonWriter.beginArray()
            jsonWriter.beginArray().value(upperLeftX).value(upperLeftY).endArray()
            jsonWriter.beginArray().value(lowerRightX).value(lowerRightY).endArray()
            jsonWriter.endArray()
          }
        }
      }
    }
  }

  class Adapter(private val typeResolutionContext: TypeResolutionContext) {
    @FromJson
    fun parseFromJson(
      jsonReader: JsonReader,
      subtitledHtmlAdapter: JsonAdapter<GaeSubtitledHtml>,
      subtitledUnicodeAdapter: JsonAdapter<GaeSubtitledUnicode>,
      imageWithRegionsAdapter: JsonAdapter<GaeImageWithRegions>
    ): GaeCustomizationArgValue {
      val key = typeResolutionContext.expectedCustomizationArgKeyName
      return when (val interactionType = typeResolutionContext.expectedInteractionType) {
        CONTINUE_INSTANCE -> when (key) {
          "buttonText" -> jsonReader.nextSubtitledUnicodeArgValue(subtitledUnicodeAdapter)
          else -> null
        }
        FRACTION_INPUT -> when (key) {
          "requireSimplestForm", "allowImproperFraction", "allowNonzeroIntegerPart" ->
            jsonReader.nextBooleanArgValue()
          "customPlaceholder" -> jsonReader.nextSubtitledUnicodeArgValue(subtitledUnicodeAdapter)
          else -> null
        }
        ITEM_SELECTION_INPUT -> when (key) {
          "minAllowableSelectionCount", "maxAllowableSelectionCount" ->
            jsonReader.nextIntArgValue()
          "choices" -> jsonReader.nextSubtitledHtmlListArgValue(subtitledHtmlAdapter)
          else -> null
        }
        MULTIPLE_CHOICE_INPUT -> when (key) {
          "choices" -> jsonReader.nextSubtitledHtmlListArgValue(subtitledHtmlAdapter)
          "showChoicesInShuffledOrder" -> jsonReader.nextBooleanArgValue()
          else -> null
        }
        NUMERIC_INPUT -> when (key) {
          "requireNonnegativeInput" -> jsonReader.nextBooleanArgValue()
          else -> null
        }
        TEXT_INPUT -> when (key) {
          "placeholder" -> jsonReader.nextSubtitledUnicodeArgValue(subtitledUnicodeAdapter)
          "rows" -> jsonReader.nextIntArgValue()
          "catchMisspellings" -> jsonReader.nextBooleanArgValue()
          else -> null
        }
        DRAG_AND_DROP_SORT_INPUT -> when (key) {
          "choices" -> jsonReader.nextSubtitledHtmlListArgValue(subtitledHtmlAdapter)
          "allowMultipleItemsInSamePosition" -> jsonReader.nextBooleanArgValue()
          else -> null
        }
        IMAGE_CLICK_INPUT -> when (key) {
          "imageAndRegions" -> jsonReader.nextImageWithRegions(imageWithRegionsAdapter)
          "highlightRegionsOnHover" -> jsonReader.nextBooleanArgValue()
          else -> null
        }
        RATIO_EXPRESSION_INPUT -> when (key) {
          "placeholder" -> jsonReader.nextSubtitledUnicodeArgValue(subtitledUnicodeAdapter)
          "numberOfTerms" -> jsonReader.nextIntArgValue()
          else -> null
        }
        ALGEBRAIC_EXPRESSION_INPUT, MATH_EQUATION_INPUT -> when (key) {
          "allowedVariables" -> jsonReader.nextStringList()
          "useFractionForDivision" -> jsonReader.nextBooleanArgValue()
          else -> null
        }
        NUMERIC_EXPRESSION_INPUT -> when (key) {
          "placeholder" -> jsonReader.nextSubtitledUnicodeArgValue(subtitledUnicodeAdapter)
          "useFractionForDivision" -> jsonReader.nextBooleanArgValue()
          else -> null
        }
        END_EXPLORATION -> when (key) {
          "recommendedExplorationIds" -> jsonReader.nextStringList()
          else -> null
        }
        INTERACTIONTYPE_NOT_SET -> error("Interaction has no customization args: $interactionType.")
      } ?: error(
        "${typeResolutionContext.currentInteractionType} interaction doesn't expect" +
          " customization arg with key: $key."
      )
    }

    @ToJson
    fun convertToJson(
      jsonWriter: JsonWriter,
      gaeCustomizationArgValue: GaeCustomizationArgValue,
      subtitledUnicodeAdapter: JsonAdapter<GaeSubtitledUnicode>,
      subtitledHtmlAdapter: JsonAdapter<GaeSubtitledHtml>,
      imageWithRegionsAdapter: JsonAdapter<GaeImageWithRegions>
    ) {
      when (gaeCustomizationArgValue) {
        is GaeImageWithRegions ->
          jsonWriter.value(gaeCustomizationArgValue, imageWithRegionsAdapter)
        is SingleBoolean -> jsonWriter.value(gaeCustomizationArgValue)
        is SingleInteger -> jsonWriter.value(gaeCustomizationArgValue)
        is StringList -> jsonWriter.value(gaeCustomizationArgValue)
        is SubtitledTextList -> jsonWriter.value(gaeCustomizationArgValue, subtitledHtmlAdapter)
        is SubtitledUnicode -> jsonWriter.value(gaeCustomizationArgValue, subtitledUnicodeAdapter)
      }
    }

    private companion object {
      private fun JsonReader.nextBooleanArgValue() = SingleBoolean(nextBoolean())

      private fun JsonReader.nextIntArgValue() = SingleInteger(nextInt())

      private fun JsonReader.nextSubtitledUnicodeArgValue(
        subtitledUnicodeAdapter: JsonAdapter<GaeSubtitledUnicode>
      ) = SubtitledUnicode(nextCustomValue(subtitledUnicodeAdapter))

      private fun JsonReader.nextSubtitledHtmlListArgValue(
        subtitledHtmlAdapter: JsonAdapter<GaeSubtitledHtml>
      ) = SubtitledTextList(nextArray { nextCustomValue(subtitledHtmlAdapter) })

      private fun JsonReader.nextImageWithRegions(
        imageWithRegionsAdapter: JsonAdapter<GaeImageWithRegions>
      ) = nextCustomValue(imageWithRegionsAdapter)

      private fun JsonReader.nextStringList() = StringList(nextArray(::nextString))

      private fun JsonWriter.value(boolean: SingleBoolean): JsonWriter = value(boolean.value)

      private fun JsonWriter.value(int: SingleInteger): JsonWriter = value(int.value.toLong())

      private fun JsonWriter.value(
        subtitledUnicode: SubtitledUnicode,
        subtitledUnicodeAdapter: JsonAdapter<GaeSubtitledUnicode>
      ): JsonWriter = this.also { subtitledUnicodeAdapter.toJson(it, subtitledUnicode.value) }

      private fun JsonWriter.value(
        subtitledHtml: GaeSubtitledHtml,
        subtitledHtmlAdapter: JsonAdapter<GaeSubtitledHtml>
      ): JsonWriter = this.also { subtitledHtmlAdapter.toJson(it, subtitledHtml) }

      private fun JsonWriter.value(
        subtitledTextList: SubtitledTextList,
        subtitledHtmlAdapter: JsonAdapter<GaeSubtitledHtml>
      ): JsonWriter {
        return beginArray().also {
          subtitledTextList.value.forEach { value(it, subtitledHtmlAdapter) }
        }.endArray()
      }

      private fun JsonWriter.value(
        imageWithRegions: GaeImageWithRegions,
        imageWithRegionsAdapter: JsonAdapter<GaeImageWithRegions>
      ): JsonWriter = this.also { imageWithRegionsAdapter.toJson(it, imageWithRegions) }

      private fun JsonWriter.value(strs: StringList): JsonWriter =
        beginArray().also { strs.value.forEach(::value) }.endArray()
    }
  }
}
