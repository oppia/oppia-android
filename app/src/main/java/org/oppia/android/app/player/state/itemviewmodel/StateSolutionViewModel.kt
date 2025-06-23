package org.oppia.android.app.player.state.itemviewmodel

import org.oppia.android.app.hintsandsolution.SolutionViewModel

class StateSolutionViewModel(
  val coreViewModel: SolutionViewModel
): StateItemViewModel(ViewType.STATE_SOLUTION)