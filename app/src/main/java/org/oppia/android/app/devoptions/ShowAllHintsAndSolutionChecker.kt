package org.oppia.android.app.devoptions

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShowAllHintsAndSolutionChecker @Inject constructor() {
  private var showAllHintsAndSolution = false

  /** Returns [showAllHintsAndSolution] indicating whether showing all hints and solution feature is enabled. */
  fun getShowAllHintsAndSolution(): Boolean = showAllHintsAndSolution

  /** Sets [showAllHintsAndSolution]. */
  fun setShowAllHintsAndSolution(isEnabled: Boolean) {
    showAllHintsAndSolution = isEnabled
  }
}
