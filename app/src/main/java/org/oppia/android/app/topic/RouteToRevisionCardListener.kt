package org.oppia.android.app.topic

import org.oppia.android.app.model.LegacyProfileId

/** Listener for when an [TopicActivity] should route to a [RevisionCardFragment]. */
interface RouteToRevisionCardListener {
  fun routeToRevisionCard(
    profileId: LegacyProfileId,
    topicId: String,
    subtopicId: Int,
    subtopicListSize: Int
  )
}
