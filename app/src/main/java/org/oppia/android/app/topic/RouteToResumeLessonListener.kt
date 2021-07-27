package org.oppia.android.app.topic

interface RouteToResumeLessonListener {
  fun routeToResumeLesson(
    internalProfileId: Int,
    topicId: String,
    storyId: String,
    explorationId: String,
    backflowScreen: Int?
  )
}