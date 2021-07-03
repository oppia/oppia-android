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

  /** Called when the 'mark chapters completed' button is clicked by the user. */
  fun onMarkChaptersCompletedClicked() {
    routeToMarkChaptersCompletedListener.routeToMarkChaptersCompleted()
  }

  /** Called when the 'mark stories completed' button is clicked by the user. */
  fun onMarkStoriesCompletedClicked() {
    routeToMarkStoriesCompletedListener.routeToMarkStoriesCompleted()
  }

  /** Called when the 'mark topics completed' button is clicked by the user. */
  fun onMarkTopicsCompletedClicked() {
    routeToMarkTopicsCompletedListener.routeToMarkTopicsCompleted()
  }
}
