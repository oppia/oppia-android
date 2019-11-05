package org.oppia.app.player.state

/**
 * Interface to keep track of selected options in MultipleChoiceInput and ItemSelectionInput.
 */
interface SelectInputItemsListener {
  fun onInputItemSelection(indexList: ArrayList<Int>)
}
