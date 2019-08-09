package org.oppia.data.backends.gae.model;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

@AutoValue
public abstract class WrittenTranslation {

    /*
    * Parent Hierarchy: ExplorationContainer -> Exploration -> State -> WrittenTranslations -> WrittenTranslation
    */

    //TODO: AutoJSON null data error

    //TODO: HTML-String data type
    @SerializedName("html") public abstract String getHtml();
    @SerializedName("needs_update") public abstract boolean isUpdateNeeded();

    public static TypeAdapter<WrittenTranslation> createTypeAdapter(Gson gson) {
        return new AutoValue_WrittenTranslation.GsonTypeAdapter(gson);
    }

}
