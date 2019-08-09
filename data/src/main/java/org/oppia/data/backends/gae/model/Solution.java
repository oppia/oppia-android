package org.oppia.data.backends.gae.model;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

@AutoValue
public abstract class Solution {

  /*
   * Parent Hierarchy: ExplorationContainer -> Exploration -> State -> InteractionInstance -> Solution
   */

  //TODO: AutoJSON null data error

  @SerializedName("interaction_id")
  public abstract String getInteractionId();

  @SerializedName("answer_is_exclusive")
  public abstract boolean isAnswerExclusive();

  @SerializedName("correct_answer")
  public abstract String getCorrectAnswer();

  @SerializedName("explanation")
  public abstract SubtitledHtml getExplanation();

  public static TypeAdapter<Solution> createTypeAdapter(Gson gson) {
    return new AutoValue_Solution.GsonTypeAdapter(gson);
  }

}
