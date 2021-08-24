package org.oppia.android.domain.devoptions

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Controller to identify whether all hints and solution should be shown by default. This controller
 * is expected to be called on the main thread only.
 */
@Singleton
class ShowAllHintsAndSolutionController @Inject constructor() {

  private var showAllHintsAndSolution = false

  /** Returns whether all hints and solutions should be shown by default. */
  fun getShowAllHintsAndSolution(): Boolean = showAllHintsAndSolution

  /** Sets whether all hints and solutions should be shown by default. */
  fun setShowAllHintsAndSolution(isEnabled: Boolean) {
    showAllHintsAndSolution = isEnabled
  }
}
