package org.oppia.android.domain.devoptions

import javax.inject.Inject
import javax.inject.Singleton

/** Monitor to identify whether all hints and solution should be shown. */
@Singleton
class ShowAllHintsAndSolutionHelper @Inject constructor() {

  private var showAllHintsAndSolution = false

  /** Returns [showAllHintsAndSolution] indicating whether showing all hints and solution feature is enabled. */
  fun getShowAllHintsAndSolution(): Boolean = showAllHintsAndSolution

  /** Sets [showAllHintsAndSolution]. */
  fun setShowAllHintsAndSolution(isEnabled: Boolean) {
    showAllHintsAndSolution = isEnabled
  }
}
