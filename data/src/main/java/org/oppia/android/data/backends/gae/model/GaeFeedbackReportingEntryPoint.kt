package org.oppia.android.data.backends.gae.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/** Data class for the feedback reporting entry point represented in the backend storage model. */
// TODO(#3016): Link backend domain model
@JsonClass(generateAdapter = true)
data class GaeFeedbackReportingEntryPoint(

  /** Corresponds to a location in the app that a user can access feedback reporting. */
  @Json(name = "entry_point_name") val entryPointName: String,
  /** Corresponds to the topic ID if the entry point is from a lesson or revision session */
  @Json(name = "topic_id") val topicId: String?,
  /** Corresponds to the subtopic ID if the entry point is from a lesson. */
  @Json(name = "story_id") val storyId: String?,
  /** Corresponds to the exploration ID if the entry point is from a lesson. */
  @Json(name = "exploration_id") val explorationId: String?,
  /** Corresponds to the subtopic ID if the entry point is from a revision session. */
  @Json(name = "subtopic_id") val subtopicId: String?

)
