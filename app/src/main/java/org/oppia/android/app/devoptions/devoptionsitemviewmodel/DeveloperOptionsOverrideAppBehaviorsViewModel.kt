package org.oppia.android.app.devoptions.devoptionsitemviewmodel

import androidx.databinding.ObservableField
import org.oppia.android.app.devoptions.ForceCrashButtonClickListener
import org.oppia.android.app.devoptions.ShowAllHintsAndSolutionChecker

/**
 * [DeveloperOptionsItemViewModel] to provide features to override app wide behaviors such as
 * crashing the app, changing network type and enabling all hints and solutions.
 */
class DeveloperOptionsOverrideAppBehaviorsViewModel(
  private val forceCrashButtonClickListener: ForceCrashButtonClickListener,
  private val showAllHintsAndSolutionChecker: ShowAllHintsAndSolutionChecker
) : DeveloperOptionsItemViewModel() {

  val isShowAllHintsAndSolutionEnabled =
    ObservableField<Boolean>(showAllHintsAndSolutionChecker.getShowAllHintsAndSolution())

  /** Called when the 'force crash' button is clicked by the user. */
  fun onForceCrashClicked() {
    forceCrashButtonClickListener.forceCrash()
  }

  fun onShowAllHintsAndSolutionClicked() {
    showAllHintsAndSolutionChecker.setShowAllHintsAndSolution(
      !(showAllHintsAndSolutionChecker.getShowAllHintsAndSolution())
    )
    isShowAllHintsAndSolutionEnabled.set(
      showAllHintsAndSolutionChecker.getShowAllHintsAndSolution()
    )
  }
}
