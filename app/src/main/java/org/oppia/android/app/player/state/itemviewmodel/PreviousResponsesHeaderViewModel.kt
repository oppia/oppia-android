package org.oppia.android.app.player.state.itemviewmodel

import androidx.databinding.ObservableBoolean
import org.oppia.android.app.player.state.listener.PreviousResponsesHeaderClickListener

/** [StateItemViewModel] for the header of the section of previously submitted answers. */
class PreviousResponsesHeaderViewModel(
  val previousAnswerCount: Int,
  val hasConversationView: Boolean,
  var isExpanded: ObservableBoolean,
  private val previousResponsesHeaderClickListener: PreviousResponsesHeaderClickListener,
  val isSplitView: Boolean
) : StateItemViewModel(ViewType.PREVIOUS_RESPONSES_HEADER) {
  /** Called when the user clicks on the previous response header. */
  fun onResponsesHeaderClicked() = previousResponsesHeaderClickListener.onResponsesHeaderClicked()
}
