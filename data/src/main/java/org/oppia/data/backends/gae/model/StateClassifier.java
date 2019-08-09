package org.oppia.data.backends.gae.model;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Type;

@AutoValue
public abstract class StateClassifier<ClassifierDataType> {

  /*
   * Parent Hierarchy: ExplorationContainer -> StateClassifier
   */

  //TODO: AutoJSON null data error

  @SerializedName("algorithm_id")
  public abstract String getAlgorithmId();

  //TODO (Ben): Check this free form data
  @SerializedName("classifier_data")
  public abstract ClassifierDataType getClassifierData();

  @SerializedName("data_schema_version")
  public abstract int getDataSchemaVersion();

  public static TypeAdapter<StateClassifier> createTypeAdapter(Gson gson, Type[] types) {
    return new AutoValue_StateClassifier.GsonTypeAdapter(gson, types);
  }

}