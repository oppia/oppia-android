package org.oppia.android.scripts.release.model

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter

/**
 * Represents the (empty) request body for the Google Play Developer API's `edits/insert` endpoint.
 *
 * The Play Console API accepts an empty JSON object `{}` as the body for edit creation. This class
 * exists to keep the Retrofit service interface consistent with other endpoints that use typed
 * Moshi objects for their request bodies.
 *
 * API reference: https://developers.google.com/android-publisher/api-ref/rest/v3/edits/insert
 */
class InsertEditRequest {
  companion object {
    /**
     * [JsonAdapter] that serialises [InsertEditRequest] as an empty JSON object `{}`.
     *
     * This avoids Moshi code-generation for an empty class (which produces an unused
     * `moshi` constructor parameter that fails under `-Werror`).
     */
    val Adapter: JsonAdapter<InsertEditRequest> = object : JsonAdapter<InsertEditRequest>() {
      override fun fromJson(reader: JsonReader): InsertEditRequest {
        reader.beginObject()
        while (reader.hasNext()) reader.skipValue()
        reader.endObject()
        return InsertEditRequest()
      }

      override fun toJson(writer: JsonWriter, value: InsertEditRequest?) {
        writer.beginObject()
        writer.endObject()
      }
    }
  }
}
