package org.oppia.android.app.devoptions.devoptionsitemviewmodel

import org.oppia.android.app.devoptions.RouteToMarkChaptersCompletedListener

/** [ViewModel] for the recycler view in [DeveloperOptionsFragment]. */
class DeveloperOptionsModifyLessonProgressViewModel(
  private val routeToMarkChaptersCompletedListener: RouteToMarkChaptersCompletedListener
) : DeveloperOptionsItemViewModel() {

  fun onMarkChaptersCompletedClicked() {
    routeToMarkChaptersCompletedListener.routeToMarkChaptersCompleted()
  }
}
