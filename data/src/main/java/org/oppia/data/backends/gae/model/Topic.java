package org.oppia.data.backends.gae.model;

import com.squareup.moshi.Json;

import java.util.List;

public class Topic {

  /*
   * Parent Hierarchy: Topic
   */

  //TODO: List<> and List<> error
  //List<> does not work but List<> works

  @Json(name = "topic_id")
  public String topicId;

  @Json(name = "topic_name")
  public String topicName;

  @Json(name = "canonical_story_dicts")
  public List<CanonicalStorySummary> canonical_story_dicts;

  @Json(name = "additional_story_dicts")
  public List<AdditionalStorySummary> additional_story_dicts;

  @Json(name = "uncategorized_skill_ids")
  public List<String> uncategorized_skill_ids;

  @Json(name = "subtopics")
  public List<SubtopicSummary> subtopics;

  @Json(name = "filename")
  public String filename;

}
