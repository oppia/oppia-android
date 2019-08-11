package org.oppia.data.backends.gae.model;

import com.squareup.moshi.Json;

import java.util.Map;

public class ExplorationContainer {

  /*
   * Parent Hierarchy: ExplorationContainer
   */

  /*
   * Ignore below params
   * is_admin, iframed, is_moderator,
   * is_super_admin, state_classifier_mapping,
   * preferred_audio_language_code, can_edit,
   * is_topic_manager, additional_angular_modules
   * auto_tts_enabled
   */

  //TODO: ImmutableMap<> and Map<> error
  //ImmutableMap<> does not work but Map<> works

  @Json(name = "record_playthrough_probability")
  public float recordPlaythroughProbability;

  @Json(name = "exploration_id")
  public String explorationId;

  @Json(name = "state_classifier_mapping")
  public Map<String, StateClassifier> stateClassifierMapping;

  @Json(name = "user_email")
  public String userEmail;

  @Json(name = "version")
  public int version;

  @Json(name = "correctness_feedback_enabled")
  public boolean correctnessFeedbackEnabled;

  @Json(name = "username")
  public String username;

  @Json(name = "is_logged_in")
  public boolean isLoggedIn;

  @Json(name = "exploration")
  public Exploration exploration;

  @Json(name = "session_id")
  public String sessionId;

}