package org.oppia.android.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/** Data class for feedback reporting app info represented in the backend domain model. */
// TODO(#3016): Link backend domain model
@JsonClass(generateAdapter = true)
data class GaeFeedbackReportingAppContext(

  /** Corresponds to a location in the app that a user can access feedback reporting. */
  @Json(name = "entry_point") val entryPoint: GaeFeedbackReportingEntryPoint,
  /** The text size used in the app. */
  @Json(name = "text_size") val textSize: String,
  /** The text language of the app. */
  @Json(name = "text_language_code") val textLanguageCode: String,
  /** The audio language set in the app. */
  @Json(name = "audio_language_code") val audioLanguageCode: String,
  /** Whether the app downloads items and updates only when connected to wifi. */
  @Json(name = "download_and_update_only_on_wifi") val downloadAndUpdateOnlyOnWifi: Boolean,
  /** Whether the app automatically updates topics. */
  @Json(name = "automatically_update_topics") val automaticallyUpdateTopics: Boolean,
  /** Whether the profile sending the report is an admin account. */
  @Json(name = "is_admin") val isAdmin: Boolean,
  /** The event log as recorded in the app, where each item in the list corresponds to a single event. */
  @Json(name = "event_logs") val eventLogs: List<String>,
  /** The logcat log as recorded in the app, where each item in the list corresponds to a single item logged. */
  @Json(name = "logcat_logs") val logcatLogs: List<String>

)
