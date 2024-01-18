package org.oppia.android.scripts.common.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import java.net.URI
import java.util.Date
import java.time.Instant
import java.time.format.DateTimeFormatter.ISO_INSTANT

@JsonClass(generateAdapter = true)
data class GitHubIssue(
  @Json(name = "id") val id: Long,
  @Json(name = "number") val number: Int,
  @Json(name = "title") val title: String,
  @Json(name = "body") val body: String?,
  @Json(name = "state") val state: State,
  @Json(name = "state_reason") val stateReason: StateReason?,
  @Json(name = "created_at") val creationDate: Date,
  @Json(name = "updated_at") val lastUpdatedDate: Date,
  @Json(name = "closed_at") val closeDate: Date?,
  @Json(name = "pull_request") val pullRequestMetadata: PullRequestMetadata?
) {
  val isPullRequest: Boolean get() = pullRequestMetadata != null

  @JsonClass(generateAdapter = false)
  enum class State {
    OPEN,
    CLOSED;

    object Adapter {
      @FromJson
      fun fromJson(jsonReader: JsonReader): State? {
        return when (val stateStr = jsonReader.maybeNextString() ?: return null) {
          "open" -> State.OPEN
          "closed" -> State.CLOSED
          else -> error("Invalid state value: '$stateStr'.")
        }
      }

      @ToJson
      fun toJson(jsonWriter: JsonWriter, date: State?): Unit = error("Not supported.")
    }
  }

  @JsonClass(generateAdapter = false)
  enum class StateReason {
    COMPLETED,
    REOPENED,
    NOT_PLANNED;

    object Adapter {
      @FromJson
      fun fromJson(jsonReader: JsonReader): StateReason? {
        return when (val stateReasonStr = jsonReader.maybeNextString() ?: return null) {
          "completed" -> StateReason.COMPLETED
          "reopened" -> StateReason.REOPENED
          "not_planned" -> StateReason.NOT_PLANNED
          else -> error("Invalid state reason value: '$stateReasonStr'.")
        }
      }

      @ToJson
      fun toJson(jsonWriter: JsonWriter, date: StateReason?): Unit = error("Not supported.")
    }
  }

  @JsonClass(generateAdapter = true)
  data class PullRequestMetadata(
    @Json(name = "url") val url: URI?,
    @Json(name = "merged_at") val mergeDate: Date?
  )

  object DateAdapter {
    // Reference: https://stackoverflow.com/a/60214805.
    @FromJson
    fun fromJson(jsonReader: JsonReader): Date? =
      jsonReader.maybeNextString()?.let(ISO_INSTANT::parse)?.let(Instant::from)?.let(Date::from)

    @ToJson
    fun toJson(jsonWriter: JsonWriter, date: Date?): Unit = error("Not supported.")
  }

  object UriAdapter {
    @FromJson
    fun fromJson(jsonReader: JsonReader): URI? = jsonReader.maybeNextString()?.let(::URI)

    @ToJson
    fun toJson(jsonWriter: JsonWriter, date: URI?): Unit = error("Not supported.")
  }
}

// The next value must be either a string or null.
private fun JsonReader.maybeNextString(): String? =
  if (peek() == JsonReader.Token.STRING) nextString() else nextNull()
