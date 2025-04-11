package org.oppia.android.data.backends.gae.model

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonClass
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson

/**
 * Represents a platform parameter value that can be received from Google App Engine.
 *
 * This can be a number of different values--see the subclasses.
 */
@JsonClass(generateAdapter = false)
sealed class GaePlatformParameterValue {
  /** Represents a platform parameter with a string-based [value]. */
  data class StringValue(val value: String) : GaePlatformParameterValue()

  /** Represents a platform parameter with an integer-based [value]. */
  data class IntValue(val value: Int) : GaePlatformParameterValue()

  /** Represents a platform parameter with a boolean-based [value]. */
  data class BooleanValue(val value: Boolean) : GaePlatformParameterValue()

  /** Represents a platform parameter with an unsupported or invalid type. */
  object UnsupportedValue : GaePlatformParameterValue()

  /** Moshi adapter for converting [GaePlatformParameterValue] to/from JSON. */
  object Adapter {
    /**
     * Returns the [GaePlatformParameterValue] extracted from the specified [jsonReader].
     *
     * This should never be used directly--use a Moshi `JsonAdapter`, instead.
     */
    @FromJson
    fun convertFromJson(jsonReader: JsonReader): GaePlatformParameterValue {
      return when (jsonReader.peek()) {
        JsonReader.Token.STRING -> StringValue(jsonReader.nextString())
        JsonReader.Token.NUMBER -> IntValue(jsonReader.nextInt())
        JsonReader.Token.BOOLEAN -> BooleanValue(jsonReader.nextBoolean())
        else -> UnsupportedValue.also { jsonReader.skipValue() }
      }
    }

    /**
     * Converts the provided [value] into JSON using [jsonWriter].
     *
     * This should never be used directly--use a Moshi `JsonAdapter`, instead.
     */
    @ToJson
    fun convertToJson(jsonWriter: JsonWriter, value: GaePlatformParameterValue) {
      when (value) {
        is StringValue -> jsonWriter.value(value.value)
        is IntValue -> jsonWriter.value(value.value)
        is BooleanValue -> jsonWriter.value(value.value)
        UnsupportedValue -> error("Cannot serialize unsupported value to JSON.")
      }
    }
  }
}
