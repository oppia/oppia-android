package org.oppia.android.scripts.gae.json

import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.lang.reflect.Type

@JsonClass(generateAdapter = false)
data class AndroidActivityRequests<T: AndroidActivityRequests.ActivityRequest>(
  val requests: List<T>
) {
  class Adapter<T: ActivityRequest> private constructor(
    private val activityRequestAdapter: JsonAdapter<T>
  ): JsonAdapter<AndroidActivityRequests<T>>() {
    override fun fromJson(jsonReader: JsonReader): AndroidActivityRequests<T> {
      error("Conversion from JSON is not supported.")
    }

    override fun toJson(
      jsonWriter: JsonWriter, androidActivityRequests: AndroidActivityRequests<T>?
    ) {
      jsonWriter.beginArray()
      androidActivityRequests?.requests?.forEach { activityRequestAdapter.toJson(jsonWriter, it) }
      jsonWriter.endArray()
    }

    class Factory<T: ActivityRequest> private constructor(
      private val requestType: Class<T>,
      private val fetchAdapter: Moshi.() -> JsonAdapter<T>
    ): JsonAdapter.Factory {
      private val requestsType by lazy {
        Types.newParameterizedType(AndroidActivityRequests::class.java, requestType)
      }

      override fun create(
        type: Type, anotations: MutableSet<out Annotation>, moshi: Moshi
      ): Adapter<*>? = if (type == requestsType) Adapter(moshi.fetchAdapter()) else null

      companion object {
        inline fun <reified T: ActivityRequest> create(): Factory<T> = create(T::class.java)

        fun <T: ActivityRequest> create(requestType: Class<T>) =
          Factory(requestType) { adapter(requestType) }
      }
    }
  }

  sealed class ActivityRequest {
    @JsonClass(generateAdapter = true)
    data class LatestVersion(@Json(name = "id") val id: String): ActivityRequest()

    @JsonClass(generateAdapter = true)
    data class NonLocalized(
      @Json(name = "id") val id: String,
      @Json(name = "version") val version: Int
    ): ActivityRequest()

    @JsonClass(generateAdapter = true)
    data class Localized(
      @Json(name = "id") val id: String,
      @Json(name = "version") val version: Int,
      @Json(name = "language_code") val languageCode: String
    ): ActivityRequest()
  }
}
