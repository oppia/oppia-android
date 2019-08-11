package org.oppia.data.backends.gae.model;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Type;
import java.util.List;

@AutoValue
public abstract class AnswerGroup<TrainingDataType> {

  /*
   * Parent Hierarchy: ExplorationContainer -> Exploration -> State -> InteractionInstance -> AnswerGroup
   */

  //TODO: ImmutableList<> and List<> error
  //ImmutableList<> does not work but List<> works
  //TODO: AutoJSON null data error

  @SerializedName("tagged_skill_misconception_id")
  public abstract String getTaggedSkillMisconceptionId();

  @SerializedName("outcome")
  public abstract Outcome getOutcome();

  @SerializedName("rule_specs")
  public abstract List<RuleSpec> getRuleSpecs();

  //TODO (Ben): Check this free form data
  @SerializedName("training_data")
  public abstract List<TrainingDataType> getTrainingData();

  public static TypeAdapter<AnswerGroup> createTypeAdapter(Gson gson, Type[] types) {
    return new AutoValue_AnswerGroup.GsonTypeAdapter(gson, types);
  }

}
