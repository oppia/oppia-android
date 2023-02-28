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
data class GaeTranslatedContent(
  val contentValue: Translation,
  val contentFormat: GaeTranslatableContentFormat,
  @Json(name = "needs_update") val needsUpdate: Boolean
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
      fun convertToJson(jsonWriter: JsonWriter, translation: Translation): Unit =
        error("Conversion to JSON is not supported.")
    }
  }

  @JsonClass(generateAdapter = true)
  data class ParsableGaeTranslatedContent(
    @Json(name = "content_value") val contentValue: Translation,
    @Json(name = "content_format") val contentFormat: GaeTranslatableContentFormat,
    @Json(name = "needs_update") val needsUpdate: Boolean
  ) {
    fun convertToGaeObject(): GaeTranslatedContent =
      GaeTranslatedContent(contentValue, contentFormat, needsUpdate)
  }

  class Adapter(private val typeResolutionContext: TypeResolutionContext) {
    @FromJson
    fun parseFromJson(
      jsonReader: JsonReader,
      gaeTranslatableContentFormatAdapter: JsonAdapter<GaeTranslatableContentFormat>,
      parsableGaeTranslatedContentAdapter: JsonAdapter<ParsableGaeTranslatedContent>
    ): GaeTranslatedContent {
      typeResolutionContext.currentContentFormat =
        jsonReader.peekTranslatableContentFormat(gaeTranslatableContentFormatAdapter)
      return jsonReader.nextCustomValue(
        parsableGaeTranslatedContentAdapter
      ).convertToGaeObject().also { typeResolutionContext.currentContentFormat = null }
    }

    @ToJson
    fun convertToJson(jsonWriter: JsonWriter, gaeTranslatedContent: GaeTranslatedContent): Unit =
      error("Conversion to JSON is not supported.")
  }

  private companion object {
    private fun JsonReader.peekTranslatableContentFormat(
      contentFormatAdapter: JsonAdapter<GaeTranslatableContentFormat>
    ): GaeTranslatableContentFormat {
      return peekJson().use { jsonReader ->
        jsonReader.nextObject {
          if (it == "content_format") contentFormatAdapter.fromJson(jsonReader) else null
        }["content_format"]
          ?: error("Missing translatable content format in translation JSON object.")
      }
    }
  }
}
