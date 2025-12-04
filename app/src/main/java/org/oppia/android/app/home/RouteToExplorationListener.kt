package org.oppia.android.app.home

import org.oppia.android.app.model.ExplorationActivityParams
import org.oppia.android.app.model.ProfileId

/** Listener for when an activity should route to a exploration. */
interface RouteToExplorationListener {
  fun routeToExploration(
    profileId: ProfileId,
    classroomId: String,
    topicId: String,
    storyId: String,
    explorationId: String,
    parentScreen: ExplorationActivityParams.ParentScreen,
    isCheckpointingEnabled: Boolean
  )
}
