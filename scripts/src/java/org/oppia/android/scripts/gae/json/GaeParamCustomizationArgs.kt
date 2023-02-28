package org.oppia.android.scripts.gae.json

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonClass
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson

// TODO: Mention parsing this requires setting interaction type in the parsing context.
@JsonClass(generateAdapter = false)
sealed class GaeParamCustomizationArgs {
  data class SingleString(val value: String) : GaeParamCustomizationArgs()

  data class StringList(val value: List<String>) : GaeParamCustomizationArgs()

  class Adapter {
    @FromJson
    fun parseFromJson(jsonReader: JsonReader): GaeParamCustomizationArgs {
      return when (val token = jsonReader.peek()) {
        JsonReader.Token.STRING -> SingleString(jsonReader.nextString())
        JsonReader.Token.BEGIN_ARRAY -> StringList(jsonReader.nextArray(jsonReader::nextString))
        else -> error("Unexpected token for param customization arguments: $token.")
      }
    }

    @ToJson
    fun convertToJson(
      jsonWriter: JsonWriter,
      gaeParamCustomizationArgs: GaeParamCustomizationArgs
    ): Unit = error("Conversion to JSON is not supported.")
  }
}
