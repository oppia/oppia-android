package org.oppia.android.app.devoptions.devoptionsitemviewmodel

import androidx.databinding.ObservableField
import org.oppia.android.app.devoptions.ForceCrashButtonClickListener
import org.oppia.android.app.devoptions.RouteToForceNetworkTypeListener
import org.oppia.android.domain.devoptions.ShowAllHintsAndSolutionController

/**
 * [DeveloperOptionsItemViewModel] to provide features to override app wide behaviors such as
 * crashing the app, changing network type and enabling all hints and solutions.
 */
class DeveloperOptionsOverrideAppBehaviorsViewModel(
  private val forceCrashButtonClickListener: ForceCrashButtonClickListener,
  private val forceNetworkTypeListener: RouteToForceNetworkTypeListener,
  private val showAllHintsAndSolutionController: ShowAllHintsAndSolutionController
) : DeveloperOptionsItemViewModel() {

  /** Identifies whether the feature to show all hints and solution is enabled or disabled. */
  val isShowAllHintsAndSolutionEnabled =
    ObservableField<Boolean>(showAllHintsAndSolutionController.getShowAllHintsAndSolution())

  /** Called when the 'force crash' button is clicked by the user. */
  fun onForceCrashClicked() {
    forceCrashButtonClickListener.forceCrash()
  }

  /** Routes the user to [ForceNetworkTypeActivity] for forcing the network type of the app. */
  fun onForceNetworkTypeClicked() {
    forceNetworkTypeListener.routeToForceNetworkType()
  }

  /**
   * Called when the state of 'show all hints/solution' switch is changed by the user.
   * Enables or disables the feature to show all hints and solution.
   */
  fun onShowAllHintsAndSolutionClicked() {
    showAllHintsAndSolutionController.setShowAllHintsAndSolution(
      !(showAllHintsAndSolutionController.getShowAllHintsAndSolution())
    )
    isShowAllHintsAndSolutionEnabled.set(
      showAllHintsAndSolutionController.getShowAllHintsAndSolution()
    )
  }
}
