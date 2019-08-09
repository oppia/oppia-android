package org.oppia.data.backends.gae.model;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

@AutoValue
public abstract class ParamSpec {

    /*
    * Parent Hierarchy: ExplorationContainer -> Exploration -> ParamSpec
    */

    //TODO: AutoJSON null data error

    @SerializedName("obj_type") public abstract String getObjType();

    public static TypeAdapter<ParamSpec> createTypeAdapter(Gson gson) {
        return new AutoValue_ParamSpec.GsonTypeAdapter(gson);
    }

}
