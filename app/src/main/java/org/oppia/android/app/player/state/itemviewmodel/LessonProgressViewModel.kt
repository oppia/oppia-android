package org.oppia.android.app.player.state.itemviewmodel

/** [StateItemViewModel] for the lesson progress indicator above the navigation buttons. */
class LessonProgressViewModel(
  val completedCount: Int,
  val totalCount: Int,
  val isBetweenCheckpoints: Boolean,
  val isSplitView: Boolean,
  private val progressAnimationRequestConsumer: () -> Boolean,
  val contentDescription: String
) : StateItemViewModel(ViewType.LESSON_PROGRESS_INDICATOR) {
  /**
   * Returns whether progress should animate, consuming the request so RecyclerView rebinding cannot
   * replay the same transition.
   */
  fun consumeShouldAnimateProgress(): Boolean = progressAnimationRequestConsumer()

  override fun areContentsTheSame(other: StateItemViewModel): Boolean {
    if (other !is LessonProgressViewModel) return false
    return completedCount == other.completedCount &&
      totalCount == other.totalCount &&
      isBetweenCheckpoints == other.isBetweenCheckpoints &&
      isSplitView == other.isSplitView &&
      contentDescription == other.contentDescription
  }
}
