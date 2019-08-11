package org.oppia.data.backends.gae.model;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

import java.util.Map;

@AutoValue
public abstract class WrittenTranslations {

  /*
   * Parent Hierarchy: ExplorationContainer -> Exploration -> State -> WrittenTranslations
   */

  //TODO: ImmutableMap<> and Map<> error
  //ImmutableMap<> does not work but Map<> works
  //TODO: AutoJSON null data error

  @SerializedName("translations_mapping")
  public abstract Map<String, Map<String, WrittenTranslation>> getTranslationsMapping();

  public static TypeAdapter<WrittenTranslations> createTypeAdapter(Gson gson) {
    return new AutoValue_WrittenTranslations.GsonTypeAdapter(gson);
  }

}
