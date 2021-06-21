package org.oppia.android.app.devoptions.devoptionsitemviewmodel

import org.oppia.android.app.devoptions.RouteToMarkChaptersCompletedListener
import org.oppia.android.app.devoptions.RouteToMarkStoriesCompletedListener

/** [ViewModel] for the recycler view in [DeveloperOptionsFragment]. */
class DeveloperOptionsModifyLessonProgressViewModel(
  private val routeToMarkChaptersCompletedListener: RouteToMarkChaptersCompletedListener,
  private val routeToMarkStoriesCompletedListener: RouteToMarkStoriesCompletedListener
) : DeveloperOptionsItemViewModel() {

  fun onMarkChaptersCompletedClicked() {
    routeToMarkChaptersCompletedListener.routeToMarkChaptersCompleted()
  }

  fun onMarkStoriesCompletedClicked() {
    routeToMarkStoriesCompletedListener.routeToMarkStoriesCompleted()
  }
}
