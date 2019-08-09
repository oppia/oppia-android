package org.oppia.data.backends.gae.model;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Type;
import java.util.List;

@AutoValue
public abstract class CustomizationArgs<ValueDataType> {

    /*
    * Parent Hierarchy: ExplorationContainer -> Exploration -> State -> ParamChange -> CustomizationArgs
    * Parent Hierarchy: ExplorationContainer -> Exploration -> ParamChange -> CustomizationArgs
    */

    //TODO: ImmutableList<> and List<> error
    //ImmutableList<> does not work but List<> works
    //TODO: AutoJSON null data error

    @SerializedName("parse_with_jinja") public abstract boolean isParseWithJinja();
    //TODO (Ben): Check this free form data
    @SerializedName("value") public abstract List<ValueDataType> getValue();
    
    public static TypeAdapter<CustomizationArgs> createTypeAdapter(Gson gson, Type[] types) {
        return new AutoValue_CustomizationArgs.GsonTypeAdapter(gson, types);
    }

}
