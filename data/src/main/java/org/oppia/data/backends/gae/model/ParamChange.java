package org.oppia.data.backends.gae.model;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

@AutoValue
public abstract class ParamChange {

    /*
    * Parent Hierarchy: ExplorationContainer -> Exploration -> ParamChange
    * Parent Hierarchy: ExplorationContainer -> Exploration -> State -> ParamChange
    */

    //TODO: AutoJSON null data error

    @SerializedName("generator_id") public abstract String getGeneratorId();
    @SerializedName("name") public abstract String getName();
    @SerializedName("customization_args") public abstract CustomizationArgs getCustomizationArgs();
   
    public static TypeAdapter<ParamChange> createTypeAdapter(Gson gson) {
        return new AutoValue_ParamChange.GsonTypeAdapter(gson);
    }

}
