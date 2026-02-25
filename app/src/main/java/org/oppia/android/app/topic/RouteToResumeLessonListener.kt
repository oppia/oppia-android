package org.oppia.android.app.topic

import org.oppia.android.app.model.ExplorationActivityParams
import org.oppia.android.app.model.ExplorationCheckpoint
import org.oppia.android.app.model.LegacyProfileId

/** Listener for when an activity should route to a [ResumeLessonActivity]. */
interface RouteToResumeLessonListener {
  /** Called selects an exploration that can be resumed. */
  fun routeToResumeLesson(
    profileId: LegacyProfileId,
    classroomId: String,
    topicId: String,
    storyId: String,
    explorationId: String,
    parentScreen: ExplorationActivityParams.ParentScreen,
    explorationCheckpoint: ExplorationCheckpoint
  )
}
