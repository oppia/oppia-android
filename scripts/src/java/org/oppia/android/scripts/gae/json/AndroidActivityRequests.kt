package org.oppia.android.scripts.gae.json

import com.squareup.moshi.FromJson
import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson

@JsonClass(generateAdapter = false)
sealed class AndroidActivityRequests {
  abstract val requests: List<ActivityRequest>

  data class Latest(val latestVersion: ActivityRequest.LatestVersion): AndroidActivityRequests() {
    override val requests = listOf(latestVersion)
  }

  data class NonLocalized(
    override val requests: List<ActivityRequest.NonLocalized>
  ): AndroidActivityRequests()

  data class Localized(
    override val requests: List<ActivityRequest.Localized>
  ): AndroidActivityRequests()

  class Adapter {
    @FromJson
    fun parseFromJson(jsonReader: JsonReader): AndroidActivityRequests =
      error("Conversion from JSON is not supported.")

    @ToJson
    fun convertToJson(
      jsonWriter: JsonWriter,
      androidActivityRequests: AndroidActivityRequests,
      activityRequestAdapter: JsonAdapter<ActivityRequest>
    ) {
      jsonWriter.beginArray()
      androidActivityRequests.requests.forEach { activityRequestAdapter.toJson(jsonWriter, it) }
      jsonWriter.endArray()
    }
  }

  @JsonClass(generateAdapter = false)
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

    class Adapter {
      @FromJson
      fun parseFromJson(jsonReader: JsonReader): ActivityRequest =
        error("Conversion from JSON is not supported.")

      @ToJson
      fun convertToJson(
        jsonWriter: JsonWriter,
        activityRequest: ActivityRequest,
        latestVersionAdapter: JsonAdapter<LatestVersion>,
        nonLocalizedAdapter: JsonAdapter<NonLocalized>,
        localizedAdapter: JsonAdapter<Localized>
      ) {
        when (activityRequest) {
          is LatestVersion -> latestVersionAdapter.toJson(jsonWriter, activityRequest)
          is NonLocalized -> nonLocalizedAdapter.toJson(jsonWriter, activityRequest)
          is Localized -> localizedAdapter.toJson(jsonWriter, activityRequest)
        }
      }
    }
  }
}
