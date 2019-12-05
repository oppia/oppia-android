package org.oppia.app.player.state.itemviewmodel

import org.oppia.app.player.state.listener.PreviousNavigationButtonListener
import org.oppia.app.player.state.listener.SubmitNavigationButtonListener

/** [StateItemViewModel] for navigation to previous cards and submitting new answers. */
class SubmitButtonViewModel(
  val hasPreviousButton: Boolean, val previousNavigationButtonListener: PreviousNavigationButtonListener,
  val submitNavigationButtonListener: SubmitNavigationButtonListener
) : StateItemViewModel(ViewType.SUBMIT_ANSWER_BUTTON)
