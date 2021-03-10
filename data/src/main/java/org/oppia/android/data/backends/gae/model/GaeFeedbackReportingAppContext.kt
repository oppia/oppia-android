package org.oppia.android.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data class for feedback reporting app info represented in the backend domain model
 * TODO(#2801): Link backend domain model
 */
@JsonClass(generateAdapter = true)
data class GaeFeedbackReportingAppContext(

  @Json(name = "entry_point") val entryPoint: String,
  // Maps a string representing the topic progress to the number of topics with that status.
  @Json(name = "topic_progress") val topicProgress: Map<String, Int>,
  @Json(name = "text_size") val textSize: String,
  @Json(name = "text_lang") val textLang: String,
  @Json(name = "audio_lang") val audioLang: String,
  @Json(name = "download_and_update_only_on_wifi") val downloadAndUpdateOnlyOnWifi: Boolean,
  @Json(name = "automatically_update_topics") val automaticallyUpdateTopics: Boolean,
  @Json(name = "is_admin") val isAdmin: Boolean,
  @Json(name = "event_logs") val eventLogs: String,
  @Json(name = "logcat_logs") val logcatLogs: String

)
