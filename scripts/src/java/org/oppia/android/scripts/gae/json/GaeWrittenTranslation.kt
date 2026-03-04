package org.oppia.android.scripts.gae.json

import com.squareup.moshi.FromJson
import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson
import org.oppia.android.scripts.gae.json.GaeTranslatableContentFormat.HTML
import org.oppia.android.scripts.gae.json.GaeTranslatableContentFormat.SET_OF_NORMALIZED_STRING
import org.oppia.android.scripts.gae.json.GaeTranslatableContentFormat.SET_OF_UNICODE_STRING
import org.oppia.android.scripts.gae.json.GaeTranslatableContentFormat.UNICODE_STRING

@JsonClass(generateAdapter = false)
data class GaeWrittenTranslation(
  val dataFormat: GaeTranslatableContentFormat,
  val translation: Translation,
  val needsUpdate: Boolean
) {
  @JsonClass(generateAdapter = false)
  sealed class Translation {
    data class SingleString(val value: String) : Translation()

    data class StringList(val value: List<String>) : Translation()

    class Adapter(private val typeResolutionContext: TypeResolutionContext) {
      @FromJson
      fun parseFromJson(jsonReader: JsonReader): Translation {
        return when (typeResolutionContext.expectedContentFormat) {
          HTML, UNICODE_STRING -> SingleString(jsonReader.nextString())
          SET_OF_NORMALIZED_STRING, SET_OF_UNICODE_STRING ->
            StringList(jsonReader.nextArray(jsonReader::nextString))
        }
      }

      @ToJson
      fun convertToJson(jsonWriter: JsonWriter, translation: Translation) {
        when (translation) {
          is SingleString -> jsonWriter.value(translation.value)
          is StringList ->
            jsonWriter.beginArray().also { translation.value.forEach(jsonWriter::value) }.endArray()
        }
      }
    }
  }

  @JsonClass(generateAdapter = true)
  data class ParsableWrittenTranslation(
    @Json(name = "data_format") val dataFormat: GaeTranslatableContentFormat,
    @Json(name = "translation") val translation: Translation,
    @Json(name = "needs_update") val needsUpdate: Boolean
  ) {
    fun convertToGaeObject(): GaeWrittenTranslation =
      GaeWrittenTranslation(dataFormat, translation, needsUpdate)
  }

  @JsonClass(generateAdapter = true)
  data class PeekableWrittenTranslation(
    @Json(name = "data_format") val dataFormat: GaeTranslatableContentFormat,
    @Json(name = "translation") val translation: Any,
    @Json(name = "needs_update") val needsUpdate: Any
  )

  class Adapter(private val typeResolutionContext: TypeResolutionContext) {
    @FromJson
    fun parseFromJson(
      jsonReader: JsonReader,
      parsableWrittenTranslationAdapter: JsonAdapter<ParsableWrittenTranslation>,
      peekableWrittenTranslationAdapter: JsonAdapter<PeekableWrittenTranslation>
    ): GaeWrittenTranslation {
      typeResolutionContext.currentContentFormat =
        jsonReader.peekTranslatableContentFormat(peekableWrittenTranslationAdapter)
      return jsonReader.nextCustomValue(
        parsableWrittenTranslationAdapter
      ).convertToGaeObject().also { typeResolutionContext.currentContentFormat = null }
    }

    @ToJson
    fun convertToJson(
      jsonWriter: JsonWriter,
      gaeWrittenTranslation: GaeWrittenTranslation,
      parsableWrittenTranslationAdapter: JsonAdapter<ParsableWrittenTranslation>
    ) {
      val parsable = ParsableWrittenTranslation(
        dataFormat = gaeWrittenTranslation.dataFormat,
        translation = gaeWrittenTranslation.translation,
        needsUpdate = gaeWrittenTranslation.needsUpdate
      )
      parsableWrittenTranslationAdapter.toJson(jsonWriter, parsable)
    }
  }

  private companion object {
    private fun JsonReader.peekTranslatableContentFormat(
      peekableWrittenTranslationAdapter: JsonAdapter<PeekableWrittenTranslation>
    ): GaeTranslatableContentFormat {
      return peekJson().use { jsonReader ->
        jsonReader.nextCustomValue(peekableWrittenTranslationAdapter)
      }.dataFormat
    }
  }
}
