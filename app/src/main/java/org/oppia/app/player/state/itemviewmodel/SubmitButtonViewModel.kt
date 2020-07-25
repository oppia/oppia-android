package org.oppia.app.player.state.itemviewmodel

import androidx.databinding.ObservableField
import org.oppia.app.player.state.listener.PreviousNavigationButtonListener
import org.oppia.app.player.state.listener.SubmitNavigationButtonListener

/** [StateItemViewModel] for navigation to previous states and submitting new answers. */
class SubmitButtonViewModel(
  val canSubmitAnswer: ObservableField<Boolean>,
  val hasPreviousButton: Boolean,
  val previousNavigationButtonListener: PreviousNavigationButtonListener,
  val submitNavigationButtonListener: SubmitNavigationButtonListener,
  val isSplitView: Boolean
) : StateItemViewModel(ViewType.SUBMIT_ANSWER_BUTTON)
