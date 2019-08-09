package org.oppia.data.backends.gae.model;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

@AutoValue
public abstract class Exploration {

    /*
    * Parent Hierarchy: ExplorationContainer -> Exploration
    */

    /*
    * Ignore below params
    * language_code
    */

    //TODO: ImmutableMap<> and Map<> error
    //ImmutableMap<> does not work but Map<> works
    //TODO: ImmutableList<> and List<> error
    //ImmutableList<> does not work but List<> works
    //TODO: AutoJSON null data error

    @SerializedName("states") public abstract Map<String,State> getStates();
    @SerializedName("param_changes") public abstract List<ParamChange> getParamChanges();
    @SerializedName("param_specs") public abstract Map<String,ParamSpec> getParamSpecs();
    @SerializedName("init_state_name") public abstract String getInitStateName();
    @SerializedName("objective") public abstract String getObjective();
    @SerializedName("correctness_feedback_enabled") public abstract boolean isCorrectnessFeedbackEnabled();
    @SerializedName("title") public abstract String getTitle();

    public static TypeAdapter<Exploration> createTypeAdapter(Gson gson) {
        return new AutoValue_Exploration.GsonTypeAdapter(gson);
    }

}
