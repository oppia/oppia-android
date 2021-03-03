package org.oppia.android.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data class for feedback reporting app info represented in the backend domain model
 * TODO(#2801): Link backend domain model
 */
@JsonClass(generateAdapter = true)
data class GaeFeedbackReportingAppContext(

  @Json(name = "entry_point") val entry_point: String?,
  // Maps the topic ID to a string representing the progress status of that topic.
  @Json(name = "topic_progress") val topic_progress: Map<String, String>?,
  @Json(name = "text_size") val text_size: String?,
  @Json(name = "text_lang") val text_lang: String?,
  @Json(name = "audio_lang") val audio_lang: String?,
  @Json(name = "download_and_update_only_on_wifi") val download_and_update_only_on_wifi: Boolean?,
  @Json(name = "automatically_update_topics") val automatically_update_topics: Boolean?,
  @Json(name = "is_admin") val is_admin: Boolean?

)
