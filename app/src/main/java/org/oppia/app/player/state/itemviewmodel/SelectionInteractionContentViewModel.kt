package org.oppia.app.player.state.itemviewmodel

import androidx.lifecycle.ViewModel

/** [ViewModel] for MultipleChoiceInput values or ItemSelectionInput values. */
class SelectionInteractionContentViewModel : ViewModel() {
  var htmlContent: String = ""
  var isAnswerSelected = false
}
