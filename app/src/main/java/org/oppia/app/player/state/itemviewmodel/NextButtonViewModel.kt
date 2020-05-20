package org.oppia.app.player.state.itemviewmodel

import org.oppia.app.player.state.listener.NextNavigationButtonListener
import org.oppia.app.player.state.listener.PreviousNavigationButtonListener

/** [StateItemViewModel] for navigation buttons to traverse previous and upcoming states. */
class NextButtonViewModel(
  val hasPreviousButton: Boolean, val previousNavigationButtonListener: PreviousNavigationButtonListener,
  val nextNavigationButtonListener: NextNavigationButtonListener
) : StateItemViewModel(ViewType.NEXT_NAVIGATION_BUTTON)
