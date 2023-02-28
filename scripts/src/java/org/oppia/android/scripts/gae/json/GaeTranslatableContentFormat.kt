package org.oppia.android.scripts.gae.json

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonClass
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson

@JsonClass(generateAdapter = false)
enum class GaeTranslatableContentFormat {
  HTML,
  UNICODE_STRING,
  SET_OF_NORMALIZED_STRING,
  SET_OF_UNICODE_STRING;

  class Adapter {
    @FromJson
    fun parseFromJson(jsonReader: JsonReader): GaeTranslatableContentFormat {
      return when (val rawFormatStr = jsonReader.nextString()) {
        "html" -> HTML
        "unicode" -> UNICODE_STRING
        "set_of_normalized_string" -> SET_OF_NORMALIZED_STRING
        "set_of_unicode_string" -> SET_OF_UNICODE_STRING
        else -> error("Unsupported translatable content format: $rawFormatStr.")
      }
    }

    @ToJson
    fun convertToJson(
      jsonWriter: JsonWriter,
      gaeTranslatableContentFormat: GaeTranslatableContentFormat
    ): Unit = error("Conversion to JSON is not supported.")
  }
}
