package org.oppia.app.player.state.itemviewmodel

import org.oppia.app.viewmodel.ObservableViewModel

/** [ObservableViewModel] for MultipleChoiceInput values or ItemSelectionInput values. */
class DragDropInteractionContentViewModel(
  val htmlContent: String, private val itemIndex: Int
) : ObservableViewModel() {

}