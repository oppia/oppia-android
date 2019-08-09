package org.oppia.data.backends.gae.model;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

@AutoValue
public abstract class InteractionInstance<ConfirmedUnclassifiedAnswersDataTYpe> {

    /*
    * Parent Hierarchy: ExplorationContainer -> Exploration -> State -> InteractionInstance
    */

    //TODO: ImmutableMap<> and Map<> error
    //ImmutableMap<> does not work but Map<> works
    //TODO: ImmutableList<> and List<> error
    //ImmutableList<> does not work but List<> works
    //TODO: AutoJSON null data error

    @SerializedName("id") public abstract String getId();
    @SerializedName("answer_groups") public abstract List<AnswerGroup> getAnswerGroups();
    @SerializedName("solution") public abstract Solution getSolution();
    //TODO (Ben): Check this free form data
    @SerializedName("confirmed_unclassified_answers") public abstract List<ConfirmedUnclassifiedAnswersDataTYpe> getConfirmedUnclassifiedAnswers();
    @SerializedName("hints") public abstract List<Hint> getHints();
    @SerializedName("default_outcome") public abstract Outcome getOutcome();
    @SerializedName("customization_args") public abstract Map<String, CustomizationArgs> getCustomizationArgs();

    public static TypeAdapter<InteractionInstance> createTypeAdapter(Gson gson, Type[] type) {
        return new AutoValue_InteractionInstance.GsonTypeAdapter(gson, type);
    }

}
