package org.oppia.android.app.home

import org.oppia.android.app.model.ExplorationActivityParams
import org.oppia.android.app.model.LegacyProfileId

/** Listener for when an activity should route to a exploration. */
interface RouteToExplorationListener {
  fun routeToExploration(
    profileId: LegacyProfileId,
    classroomId: String,
    topicId: String,
    storyId: String,
    explorationId: String,
    parentScreen: ExplorationActivityParams.ParentScreen,
    isCheckpointingEnabled: Boolean
  )
}
