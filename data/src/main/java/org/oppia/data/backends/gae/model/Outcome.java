package org.oppia.data.backends.gae.model;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

import java.util.List;

@AutoValue
public abstract class Outcome {

    /*
    * Parent Hierarchy: ExplorationContainer -> Exploration -> State -> InteractionInstance -> Outcome
    * Parent Hierarchy: ExplorationContainer -> Exploration -> State -> InteractionInstance -> AnswerGroup -> Outcome
    */

    //TODO: ImmutableList<> and List<> error
    //ImmutableList<> does not work but List<> works
    //TODO: AutoJSON null data error

    @SerializedName("dest") public abstract String getDest();
    @SerializedName("refresher_exploration_id") public abstract String getRefresherExplorationId();
    @SerializedName("feedback") public abstract SubtitledHtml getFeedback();
    @SerializedName("param_changes") public abstract List<ParamChange> getParamChanges();
    @SerializedName("missing_prerequisite_skill_id") public abstract String getMissingPrerequisiteSkillId();
    @SerializedName("labelled_as_correct") public abstract boolean isLabelledAsCorrect();
    
    public static TypeAdapter<Outcome> createTypeAdapter(Gson gson) {
        return new AutoValue_Outcome.GsonTypeAdapter(gson);
    }

}
