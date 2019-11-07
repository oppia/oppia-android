package org.oppia.app.player.state.itemviewmodel

import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import org.oppia.app.model.Interaction
import org.oppia.app.model.InteractionObject
import org.oppia.app.model.StringList
import org.oppia.app.player.state.SelectionItemInputType
import org.oppia.app.player.state.answerhandling.InteractionAnswerHandler
import org.oppia.app.player.state.answerhandling.InteractionAnswerReceiver

/** ViewModel for multiple or item-selection input choice list. */
class SelectionInteractionViewModel(
  val explorationId: String, interaction: Interaction, private val interactionAnswerReceiver: InteractionAnswerReceiver,
  existingAnswer: InteractionObject?, val isReadOnly: Boolean
): StateItemViewModel(), InteractionAnswerHandler {
  private val interactionId: String = interaction.id
  private val choiceStrings: List<String> by lazy {
    interaction.customizationArgsMap["choices"]?.setOfHtmlString?.htmlList ?: listOf()
  }
  private val minAllowableSelectionCount: Int by lazy {
    interaction.customizationArgsMap["minAllowableSelectionCount"]?.signedInt ?: 1
  }
  private val maxAllowableSelectionCount: Int by lazy {
    // Assume that at least 1 answer always needs to be submitted, and that the max can't be less than the min for cases
    // when either of the counts are not specified.
    interaction.customizationArgsMap["maxAllowableSelectionCount"]?.signedInt ?: minAllowableSelectionCount
  }
  private val selectedItems = computeSelectedItems(
    existingAnswer ?: InteractionObject.getDefaultInstance(), interactionId, choiceStrings
  )
  val choiceItems: ObservableList<SelectionInteractionContentViewModel> = computeChoiceItems(
    choiceStrings, selectedItems, isReadOnly, this
  )

  override fun isExplicitAnswerSubmissionRequired(): Boolean {
    // If more than one answer is allowed, then a submission button is needed.
    return maxAllowableSelectionCount > 1
  }

  override fun getPendingAnswer(): InteractionObject {
    val interactionObjectBuilder = InteractionObject.newBuilder()
    if (interactionId == "ItemSelectionInput") {
      interactionObjectBuilder.setOfHtmlString = StringList.newBuilder()
        .addAllHtml(selectedItems.map(choiceItems::get).map { it.htmlContent })
        .build()
    } else if (selectedItems.size == 1) {
      interactionObjectBuilder.nonNegativeInt = selectedItems.first()
    }
    return interactionObjectBuilder.build()
  }

  /** Returns the [SelectionItemInputType] that should be used to render items of this view model. */
  fun getSelectionItemInputType(): SelectionItemInputType {
    return if (areCheckboxesBound()) {
      SelectionItemInputType.CHECKBOXES
    } else {
      SelectionItemInputType.RADIO_BUTTONS
    }
  }

  /** Catalogs an item being clicked by the user and returns whether the item should be considered selected. */
  fun updateSelection(itemIndex: Int, isCurrentlySelected: Boolean): Boolean {
    if (areCheckboxesBound()) {
      if (isCurrentlySelected) {
        selectedItems -= itemIndex
        return false
      } else if (selectedItems.size < maxAllowableSelectionCount) {
        // TODO(#32): Add warning to user when they exceed the number of allowable selections or are under the minimum
        //  number required.
        selectedItems += itemIndex
        return true
      }
    } else {
      // Disable all items to simulate a radio button group.
      choiceItems.forEach { item -> item.isAnswerSelected.set(false) }
      selectedItems.clear()
      selectedItems += itemIndex

      // Only push the answer if explicit submission isn't required.
      if (maxAllowableSelectionCount == 1) {
        interactionAnswerReceiver.onAnswerReadyForSubmission(getPendingAnswer())
      }
      return true
    }

    // Do not change the current status if it isn't valid to do so.
    return isCurrentlySelected
  }

  private fun areCheckboxesBound(): Boolean {
    return interactionId == "ItemSelectionInput" && maxAllowableSelectionCount > 1
  }

  companion object {
    private fun computeSelectedItems(
      answer: InteractionObject, interactionId: String, choiceStrings: List<String>
    ): MutableList<Int> {
      return if (interactionId == "ItemSelectionInput") {
        answer.setOfHtmlString.htmlList.map(choiceStrings::indexOf).toMutableList()
      } else if (answer.objectTypeCase == InteractionObject.ObjectTypeCase.NON_NEGATIVE_INT) {
        mutableListOf(answer.nonNegativeInt)
      } else {
        mutableListOf()
      }
    }

    private fun computeChoiceItems(
      choiceStrings: List<String>, selectedItems: List<Int>, isReadOnly: Boolean,
      selectionInteractionViewModel: SelectionInteractionViewModel
    ): ObservableArrayList<SelectionInteractionContentViewModel> {
      val observableList = ObservableArrayList<SelectionInteractionContentViewModel>()
      observableList += choiceStrings.mapIndexed { index, choiceString ->
        val isAnswerSelected = index in selectedItems
        SelectionInteractionContentViewModel(
          htmlContent = choiceString, itemIndex = index, isAnswerInitiallySelected = isAnswerSelected,
          isReadOnly = isReadOnly, selectionInteractionViewModel = selectionInteractionViewModel
        )
      }
      return observableList
    }
  }
}
