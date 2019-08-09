package org.oppia.data.backends.gae.model;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

import java.util.List;

@AutoValue
public abstract class State {

  /*
   * Parent Hierarchy: ExplorationContainer -> Exploration -> State
   */

  //TODO: ImmutableList<> and List<> error
  //ImmutableList<> does not work but List<> works
  //TODO: AutoJSON null data error

  @SerializedName("recorded_voiceovers")
  public abstract RecordedVoiceovers getRecordedVoiceovers();

  @SerializedName("content")
  public abstract SubtitledHtml getContent();

  @SerializedName("written_translations")
  public abstract WrittenTranslations getWrittenTranslations();

  @SerializedName("param_changes")
  public abstract List<ParamChange> getParamChanges();

  @SerializedName("classifier_model_id")
  public abstract String getClassifierModelId();

  @SerializedName("interaction")
  public abstract InteractionInstance getInteractionInstance();

  @SerializedName("solicit_answer_details")
  public abstract boolean isSolicitAnswerDetails();

  public static TypeAdapter<State> createTypeAdapter(Gson gson) {
    return new AutoValue_State.GsonTypeAdapter(gson);
  }

}