package org.oppia.android.app.player.state.itemviewmodel

import org.oppia.android.app.hintsandsolution.SolutionViewModel

/** [StateItemViewModel] for displaying the solution in the flashback state. */
class StateSolutionViewModel(
  val coreViewModel: SolutionViewModel
) : StateItemViewModel(ViewType.STATE_SOLUTION)
