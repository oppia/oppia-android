package org.oppia.android.app.devoptions

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import org.oppia.android.app.devoptions.devoptionsitemviewmodel.DeveloperOptionsAddAndDeleteProfilesViewModel
import org.oppia.android.app.devoptions.devoptionsitemviewmodel.DeveloperOptionsItemViewModel
import org.oppia.android.app.devoptions.devoptionsitemviewmodel.DeveloperOptionsModifyLessonProgressViewModel
import org.oppia.android.app.devoptions.devoptionsitemviewmodel.DeveloperOptionsOverrideAppBehaviorsViewModel
import org.oppia.android.app.devoptions.devoptionsitemviewmodel.DeveloperOptionsTestParsersViewModel
import org.oppia.android.app.devoptions.devoptionsitemviewmodel.DeveloperOptionsViewLogsViewModel
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.domain.devoptions.ShowAllHintsAndSolutionController
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.profile.ProfileManagementController
import javax.inject.Inject

/**
 * [ViewModel] for [DeveloperOptionsFragment]. It populates the recyclerview with a list of
 * [DeveloperOptionsItemViewModel] which in turn implement corresponding functionalities.
 */
@FragmentScope
class DeveloperOptionsViewModel @Inject constructor(
  activity: AppCompatActivity,
  private val showAllHintsAndSolutionController: ShowAllHintsAndSolutionController,
  private val profileManagementController: ProfileManagementController,
  private val oppiaLogger: OppiaLogger
) {
  private val forceCrashButtonClickListener = activity as ForceCrashButtonClickListener
  private val routeToMarkChaptersCompletedListener =
    activity as RouteToMarkChaptersCompletedListener
  private val routeToMarkStoriesCompletedListener =
    activity as RouteToMarkStoriesCompletedListener
  private val routeToMarkTopicsCompletedListener =
    activity as RouteToMarkTopicsCompletedListener
  private val routeToViewEventLogsListener = activity as RouteToViewEventLogsListener
  private val routeToForceNetworkTypeListener = activity as RouteToForceNetworkTypeListener
  private val routeToMathExpressionParserTestListener =
    activity as RouteToMathExpressionParserTestListener
  private val addProfileButtonClickListener = activity as AddOneProfileButtonClickListener
  private val addThreeProfilesButtonClickListener = activity as AddThreeProfilesButtonClickListener
  private val deleteAllNonAdminProfilesButtonClickListener =
    activity as DeleteAllNonAdminProfilesButtonClickListener

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
      DeveloperOptionsOverrideAppBehaviorsViewModel(
        forceCrashButtonClickListener,
        routeToForceNetworkTypeListener,
        showAllHintsAndSolutionController
      ),
      DeveloperOptionsTestParsersViewModel(routeToMathExpressionParserTestListener),
      DeveloperOptionsAddAndDeleteProfilesViewModel(
        addProfileButtonClickListener,
        addThreeProfilesButtonClickListener,
        deleteAllNonAdminProfilesButtonClickListener,
        profileManagementController,
        oppiaLogger
      )
    )
  }
}
