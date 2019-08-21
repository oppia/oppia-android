package org.oppia.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Subtopic(

  /*
   * Ignore:
   * is_moderator, is_admin, username, user_email, iframed, is_topic_manager, is_super_admin,
   * additional_angular_modules
   */

  //@Json(name = "topic_id") val topicId: String,
  //@Json(name = "subtopic_id") val subtopicId: String,
  @Json(name = "subtopic_title") val subtopicTitle: String,
  @Json(name = "page_contents") val pageContents: SubtopicPageContents

)
