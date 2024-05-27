package org.oppia.android.scripts.gae.json

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson

@JsonClass(generateAdapter = false)
data class GaeEntityTranslations(val translations: Map<String, GaeTranslatedContent>) {
  object Adapter {
    @FromJson
    fun parseFromJson(
      jsonReader: JsonReader,
      gaeTranslatedContentAdapter: JsonAdapter<GaeTranslatedContent>
    ): GaeEntityTranslations {
      return GaeEntityTranslations(
        jsonReader.nextObject { jsonReader.nextCustomValue(gaeTranslatedContentAdapter) }
      )
    }

    @ToJson
    fun convertToJson(
      jsonWriter: JsonWriter,
      gaeEntityTranslations: GaeEntityTranslations,
      gaeTranslatedContentAdapter: JsonAdapter<GaeTranslatedContent>
    ) {
      jsonWriter.beginObject()
      for ((languageCode, gaeTranslatedContent) in gaeEntityTranslations.translations) {
        jsonWriter.name(languageCode)
        gaeTranslatedContentAdapter.toJson(jsonWriter, gaeTranslatedContent)
      }
      jsonWriter.endObject()
    }
  }
}
