package org.oppia.app.player.state.itemviewmodel

/** [ViewModel] for MultipleChoiceInput values or ItemSelectionInput values. */
class SelectionInteractionContentViewModel(
  val htmlContent: String, isAnswerInitiallySelected: Boolean, val isReadOnly: Boolean
): StateItemViewModel() {
  var isAnswerSelected = isAnswerInitiallySelected
}
