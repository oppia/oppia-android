package org.oppia.android.app.topic

import org.oppia.android.app.model.LegacyProfileId

/** Listener for when a [TopicActivity] should route to a study guide. */
interface RouteToStudyGuideListener {
  /** Routes to the study guide for the given subtopic of the given topic. */
  fun routeToStudyGuide(
    profileId: LegacyProfileId,
    topicId: String,
    subtopicId: Int,
    subtopicListSize: Int
  )
}
