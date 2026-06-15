package org.oppia.android.app.player.state.itemviewmodel

/** [StateItemViewModel] for the lesson progress indicator beneath the navigation buttons. */
class LessonProgressViewModel(
  val completedCount: Int,
  val totalCount: Int,
  val progressText: String
) : StateItemViewModel(ViewType.LESSON_PROGRESS_INDICATOR)
