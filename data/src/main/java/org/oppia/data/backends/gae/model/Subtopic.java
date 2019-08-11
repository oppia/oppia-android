package org.oppia.data.backends.gae.model;

import com.squareup.moshi.Json;

public class Subtopic {

  /*
   * Parent Hierarchy: Subtopic
   */

  @Json(name = "topic_id")
  public String topicId;

  @Json(name = "subtopic_id")
  public String subtopicId;

  @Json(name = "page_contents")
  public SubtopicPageContents pageContents;

}
