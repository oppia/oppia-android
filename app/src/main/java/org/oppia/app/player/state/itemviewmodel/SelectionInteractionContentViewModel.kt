package org.oppia.app.player.state.itemviewmodel

import androidx.lifecycle.ViewModel
import org.oppia.app.fragment.FragmentScope
import javax.inject.Inject

/** [ViewModel] for MultipleChoiceInput values or ItemSelectionInput values. */
class SelectionInteractionContentViewModel(
  isAnswerInitiallySelected: Boolean, val isReadOnly: Boolean
): ViewModel() {
  var htmlContent: String = ""
  var isAnswerSelected = isAnswerInitiallySelected
}
