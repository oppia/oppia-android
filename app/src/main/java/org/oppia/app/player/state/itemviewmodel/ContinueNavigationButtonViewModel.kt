package org.oppia.app.player.state.itemviewmodel

import org.oppia.app.player.state.listener.ContinueNavigationButtonListener
import org.oppia.app.player.state.listener.PreviousNavigationButtonListener

/**
 * [StateItemViewModel] for navigating to previous states and continuing to a new state. This differs from
 * [NextButtonViewModel] in that the latter is for navigating to existing states rather than a new state. This differs
 * from [ContinueNavigationButtonViewModel] in that the latter is for the continue interaction whereas this is for
 * navigating past a recently completed state.
 */
class ContinueNavigationButtonViewModel(
  val hasPreviousButton: Boolean,
  val previousNavigationButtonListener: PreviousNavigationButtonListener,
  val continueNavigationButtonListener: ContinueNavigationButtonListener,
  val isSplitView: Boolean
) : StateItemViewModel(ViewType.CONTINUE_NAVIGATION_BUTTON)
