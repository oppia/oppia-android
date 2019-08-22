package org.oppia.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ExplorationContainer(

  /*
    * Ignore below params
    * is_admin, iframed, is_moderator,
    * is_super_admin, state_classifier_mapping,
    * preferred_audio_language_code, can_edit,
    * is_topic_manager, additional_angular_modules
    * auto_tts_enabled
    */

  @Json(name = "record_playthrough_probability") val recordPlaythroughProbability: Float?,
  @Json(name = "exploration_id") val explorationId: String?,
  @Json(name = "state_classifier_mapping") val stateClassifierMapping: Map<String, StateClassifier>?,
  @Json(name = "user_email") val userEmail: String?,
  @Json(name = "version") val version: Int?,
  @Json(name = "correctness_feedback_enabled") val isCorrectnessFeedbackEnabled: Boolean?,
  @Json(name = "username") val username: String?,
  @Json(name = "is_logged_in") val isLoggedIn: Boolean?,
  @Json(name = "exploration") val exploration: Exploration?,
  @Json(name = "session_id") val sessionId: String?

)
