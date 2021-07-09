package org.oppia.android.app.devoptions

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import org.oppia.android.app.devoptions.devoptionsitemviewmodel.DeveloperOptionsItemViewModel
import org.oppia.android.app.devoptions.devoptionsitemviewmodel.DeveloperOptionsModifyLessonProgressViewModel
import org.oppia.android.app.devoptions.devoptionsitemviewmodel.DeveloperOptionsOverrideAppBehaviorsViewModel
import org.oppia.android.app.devoptions.devoptionsitemviewmodel.DeveloperOptionsViewLogsViewModel
import org.oppia.android.app.fragment.FragmentScope
import javax.inject.Inject

/**
 * [ViewModel] for [DeveloperOptionsFragment]. It populates the recyclerview with a list of
 * [DeveloperOptionsItemViewModel] which in turn implement corresponding functionalities.
 */
@FragmentScope
class DeveloperOptionsViewModel @Inject constructor(activity: AppCompatActivity) {
  private val forceCrashButtonClickListener = activity as ForceCrashButtonClickListener
  private val routeToMarkChaptersCompletedListener =
    activity as RouteToMarkChaptersCompletedListener
  private val routeToMarkStoriesCompletedListener =
    activity as RouteToMarkStoriesCompletedListener
  private val routeToMarkTopicsCompletedListener =
    activity as RouteToMarkTopicsCompletedListener
  private val routeToViewEventLogsListener = activity as RouteToViewEventLogsListener

  /**
   * List of [DeveloperOptionsItemViewModel] used to populate recyclerview of
   * [DeveloperOptionsFragment] to enable corresponding functionalities.
   */
  val developerOptionsList: List<DeveloperOptionsItemViewModel> by lazy {
    processDeveloperOptionsList()
  }

  private fun processDeveloperOptionsList(): List<DeveloperOptionsItemViewModel> {
    return listOf(
      DeveloperOptionsModifyLessonProgressViewModel(
        routeToMarkChaptersCompletedListener,
        routeToMarkStoriesCompletedListener,
        routeToMarkTopicsCompletedListener
      ),
      DeveloperOptionsViewLogsViewModel(routeToViewEventLogsListener),
      DeveloperOptionsOverrideAppBehaviorsViewModel(forceCrashButtonClickListener)
    )
  }
}
