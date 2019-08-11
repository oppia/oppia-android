package org.oppia.data.backends.gae.model;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

@AutoValue
public abstract class Topic {

  /*
   * Parent Hierarchy: Topic
   */

  //TODO: List<> and List<> error
  //List<> does not work but List<> works
  //TODO: AutoJSON null data error

  @SerializedName("topic_id")
  public abstract String getTopicId();

  @SerializedName("topic_name")
  public abstract String getTopicName();

  @SerializedName("canonical_story_dicts")
  public abstract List<CanonicalStorySummary> getCanonicalStoryDicts();

  @SerializedName("additional_story_dicts")
  public abstract List<AdditionalStorySummary> getAdditionalStoryDicts();

  @SerializedName("uncategorized_skill_ids")
  public abstract List<String> getUncategorizedSkillIds();

  @SerializedName("subtopics")
  public abstract List<SubtopicSummary> getSubtopicSummaries();

  public static TypeAdapter<Topic> createTypeAdapter(Gson gson) {
    return new AutoValue_Topic.GsonTypeAdapter(gson);
  }

}
