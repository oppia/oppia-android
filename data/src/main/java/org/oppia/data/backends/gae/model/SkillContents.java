package org.oppia.data.backends.gae.model;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

import java.util.List;

@AutoValue
public abstract class SkillContents {

  /*
   * Parent Hierarchy: ConceptCard -> SkillContents
   */

  //TODO: ImmutableList<> and List<> error
  //ImmutableList<> does not work but List<> works
  //TODO: AutoJSON null data error

  @SerializedName("explanation")
  public abstract SubtitledHtml getExplanation();

  @SerializedName("worked_examples")
  public abstract List<SubtitledHtml> getWorkedExamples();

  @SerializedName("recorded_voiceovers")
  public abstract RecordedVoiceovers getRecordedVoiceovers();

  @SerializedName("written_translations")
  public abstract WrittenTranslations getWrittenTranslations();

  public static TypeAdapter<SkillContents> createTypeAdapter(Gson gson) {
    return new AutoValue_SkillContents.GsonTypeAdapter(gson);
  }

}
