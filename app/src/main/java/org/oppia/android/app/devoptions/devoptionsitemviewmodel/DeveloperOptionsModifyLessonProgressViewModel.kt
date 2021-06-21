package org.oppia.android.app.devoptions.devoptionsitemviewmodel

import org.oppia.android.app.devoptions.RouteToMarkChaptersCompletedListener
import org.oppia.android.app.devoptions.RouteToMarkStoriesCompletedListener
import org.oppia.android.app.devoptions.RouteToMarkTopicsCompletedListener

/** [ViewModel] for the recycler view in [DeveloperOptionsFragment]. */
class DeveloperOptionsModifyLessonProgressViewModel(
  private val routeToMarkChaptersCompletedListener: RouteToMarkChaptersCompletedListener,
  private val routeToMarkStoriesCompletedListener: RouteToMarkStoriesCompletedListener,
  private val routeToMarkTopicsCompletedListener: RouteToMarkTopicsCompletedListener
) : DeveloperOptionsItemViewModel() {

  fun onMarkChaptersCompletedClicked() {
    routeToMarkChaptersCompletedListener.routeToMarkChaptersCompleted()
  }

  fun onMarkStoriesCompletedClicked() {
    routeToMarkStoriesCompletedListener.routeToMarkStoriesCompleted()
  }

  fun onMarkTopicsCompletedClicked() {
    routeToMarkTopicsCompletedListener.routeToMarkTopicsCompleted()
  }
}
