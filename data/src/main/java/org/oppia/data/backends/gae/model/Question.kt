package org.oppia.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Question(

  /*
   * Ignore below params
   * is_admin, iframed, is_moderator,
   * is_super_admin, state_classifier_mapping,
   * preferred_audio_language_code, can_edit,
   * is_topic_manager, additional_angular_modules
   * auto_tts_enabled
   */

  @Json(name = "id") val id: String?,
  @Json(name = "question_state_data") val state: State?,
  @Json(name = "version") val version: Int?

)
