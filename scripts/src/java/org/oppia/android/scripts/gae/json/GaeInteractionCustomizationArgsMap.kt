package org.oppia.android.scripts.gae.json

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson

@JsonClass(generateAdapter = false)
data class GaeInteractionCustomizationArgsMap(
  val customizationArgs: Map<String, GaeCustomizationArgValue>
) {
  class Adapter(private val typeResolutionContext: TypeResolutionContext) {
    @FromJson
    fun parseFromJson(
      jsonReader: JsonReader,
      customizationArgValueAdapter: JsonAdapter<GaeCustomizationArgValue>
    ): GaeInteractionCustomizationArgsMap {
      val customizationArgs = jsonReader.nextObject { key ->
        typeResolutionContext.currentCustomizationArgKeyName = key
        jsonReader.nextObject { expectedValueKey ->
          check(expectedValueKey == "value") {
            "Only 'value' is expected for the customization args value map, encountered:" +
              " $expectedValueKey."
          }
          jsonReader.nextCustomValue(customizationArgValueAdapter)
        }.values.single()
      }
      // Reset argument names (since none are being parsed anymore).
      typeResolutionContext.currentCustomizationArgKeyName = null
      return GaeInteractionCustomizationArgsMap(customizationArgs)
    }

    @ToJson
    fun convertToJson(
      jsonWriter: JsonWriter,
      gaeInteractionCustomizationArgsMap: GaeInteractionCustomizationArgsMap,
      customizationArgValueAdapter: JsonAdapter<GaeCustomizationArgValue>
    ) {
      jsonWriter.beginObject()
      gaeInteractionCustomizationArgsMap.customizationArgs.forEach { (key, arg) ->
        jsonWriter.name(key)
        jsonWriter.beginObject()
        jsonWriter.name("value")
        customizationArgValueAdapter.toJson(jsonWriter, arg)
        jsonWriter.endObject()
      }
      jsonWriter.endObject()
    }
  }
}
