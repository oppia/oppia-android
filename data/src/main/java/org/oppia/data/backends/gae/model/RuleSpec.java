package org.oppia.data.backends.gae.model;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Type;
import java.util.Map;

@AutoValue
public abstract class RuleSpec<InputDatatype> {

  /*
   * Parent Hierarchy: ExplorationContainer -> Exploration -> State -> InteractionInstance -> AnswerGroups -> RuleSpec
   */

  //TODO: ImmutableMap<> and Map<> error
  //ImmutableMap<> does not work but Map<> works
  //TODO: AutoJSON null data error

  //TODO (Ben): Check this free form data
  @SerializedName("inputs")
  public abstract Map<String, InputDatatype> getInputs();

  @SerializedName("rule_type")
  public abstract String getRuleType();

  public static TypeAdapter<RuleSpec> createTypeAdapter(Gson gson, Type[] types) {
    return new AutoValue_RuleSpec.GsonTypeAdapter(gson, types);
  }

}
