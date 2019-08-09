package org.oppia.data.backends.gae.model;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

@AutoValue
public abstract class ExplorationContainer {

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

  @SerializedName("record_playthrough_probability") public abstract float getRecordPlaythroughProbability();
  @SerializedName("exploration_id") public abstract String getExplorationId();
  //@SerializedName("state_classifier_mapping") public abstract ImmutableMap<String, StateClassifier> getStateClassifierMapping();
  @SerializedName("user_email") public abstract String getUserEmail();
  @SerializedName("version") public abstract int getVersion();
  @SerializedName("correctness_feedback_enabled") public abstract boolean isCorrectnessFeedbackEnabled();
  @SerializedName("username") public abstract String getUsername();
  @SerializedName("is_logged_in") public abstract boolean isLoggedIn();
  //@SerializedName("exploration") public abstract Exploration getExploration();
  @SerializedName("session_id") public abstract String getSessionId();

  public static TypeAdapter<ExplorationContainer> createTypeAdapter(Gson gson) {
    return new AutoValue_ExplorationContainer.GsonTypeAdapter(gson);
  }

}