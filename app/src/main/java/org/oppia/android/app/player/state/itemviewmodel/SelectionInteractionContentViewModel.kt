package org.oppia.android.app.player.state.itemviewmodel

import androidx.databinding.ObservableBoolean
import org.oppia.android.app.model.SubtitledHtml
import org.oppia.android.app.viewmodel.ObservableViewModel

/** [ObservableViewModel] for MultipleChoiceInput values or ItemSelectionInput values. */
class SelectionInteractionContentViewModel(
  val htmlContent: SubtitledHtml,
  val hasConversationView: Boolean,
  private val itemIndex: Int,
  private val selectionInteractionViewModel: SelectionInteractionViewModel,
  val isEnabled: ObservableBoolean
) : ObservableViewModel() {
  var isAnswerSelected = ObservableBoolean() //it should be false for disable the submit_button... find how it can be false

  fun handleItemClicked() {
    val isCurrentlySelected = isAnswerSelected.get()
    val shouldNowBeSelected =
      selectionInteractionViewModel.updateSelection(itemIndex, isCurrentlySelected)
    if (isCurrentlySelected != shouldNowBeSelected) {
      isAnswerSelected.set(shouldNowBeSelected)
    }
  }
}
