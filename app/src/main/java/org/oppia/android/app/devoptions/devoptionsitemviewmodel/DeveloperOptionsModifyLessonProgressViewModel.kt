package org.oppia.android.app.devoptions.devoptionsitemviewmodel

import org.oppia.android.app.devoptions.RouteToMarkChaptersCompletedListener
import org.oppia.android.app.devoptions.RouteToMarkStoriesCompletedListener
import org.oppia.android.app.devoptions.RouteToMarkTopicsCompletedListener

/**
 * [DeveloperOptionsItemViewModel] to provide features to modify lesson progress such as
 * marking chapters completed, marking stories completed and marking topics completed.
 */
class DeveloperOptionsModifyLessonProgressViewModel(
  private val routeToMarkChaptersCompletedListener: RouteToMarkChaptersCompletedListener,
  private val routeToMarkStoriesCompletedListener: RouteToMarkStoriesCompletedListener,
  private val routeToMarkTopicsCompletedListener: RouteToMarkTopicsCompletedListener
) : DeveloperOptionsItemViewModel() {

  /** Routes user to [MarkChaptersCompletedActivity] screen for modifying the progress of chapters. */
  fun onMarkChaptersCompletedClicked() {
    routeToMarkChaptersCompletedListener.routeToMarkChaptersCompleted()
  }

  /** Routes user to [MarkStoriesCompletedActivity] screen for modifying the progress of stories. */
  fun onMarkStoriesCompletedClicked() {
    routeToMarkStoriesCompletedListener.routeToMarkStoriesCompleted()
  }

  /** Routes user to [MarkTopicsCompletedActivity] screen for modifying the progress of topics. */
  fun onMarkTopicsCompletedClicked() {
    routeToMarkTopicsCompletedListener.routeToMarkTopicsCompleted()
  }
}
