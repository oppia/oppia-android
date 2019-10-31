package org.oppia.app.player.state.itemviewmodel

import android.util.Log
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import org.oppia.app.model.InteractionObject
import org.oppia.app.model.StringList
import org.oppia.app.player.state.SelectionItemInputType
import org.oppia.app.player.state.answerhandling.InteractionAnswerHandler
import org.oppia.app.player.state.answerhandling.InteractionAnswerReceiver

private const val INTERACTION_ADAPTER_TAG = "SelectionInteraction"

/** ViewModel for multiple or item-selection input choice list. */
class SelectionInteractionViewModel(
  choiceStrings: List<String>, val explorationId: String, val interactionId: String,
  private val interactionAnswerReceiver: InteractionAnswerReceiver, val maxAllowableSelectionCount: Int,
  @Suppress("unused") val minAllowableSelectionCount: Int,
  existingAnswer: InteractionObject?, val isReadOnly: Boolean
): StateItemViewModel(), InteractionAnswerHandler {
  val selectedItems = computeSelectedItems(
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

  //TODO(BenHenning): Inline this.
  fun handleItemSelected() {
    // Only push the answer if explicit submission isn't required.
    if (maxAllowableSelectionCount == 1) {
      interactionAnswerReceiver.onAnswerReadyForSubmission(getPendingAnswer())
    }
  }

  /** Returns the [SelectionItemInputType] that should be used to render items of this view model. */
  fun getSelectionItemInputType(): SelectionItemInputType {
    return if (areCheckboxesBound()) {
      SelectionItemInputType.CHECKBOXES
    } else {
      SelectionItemInputType.RADIO_BUTTONS
    }
  }

  /** Catalogs an item being clicked by the user. */
  fun handleItemClicked(itemViewModel: SelectionInteractionContentViewModel, itemIndex: Int, isSelected: Boolean) {
    if (areCheckboxesBound()) {
      if (isSelected) {
        itemViewModel.isAnswerSelected.set(false)
        selectedItems -= itemIndex
      } else {
        val selectedItemCount = selectedItems.size
        if (selectedItemCount < maxAllowableSelectionCount) {
          itemViewModel.isAnswerSelected.set(true)
          selectedItems += itemIndex
        } else {
          Log.d(
            INTERACTION_ADAPTER_TAG,
            "You cannot select more than ${maxAllowableSelectionCount} options"
          )
        }
      }
      handleItemSelected()
    } else {
      choiceItems.forEach { item -> item.isAnswerSelected.set(false) }
      selectedItems.clear()
      itemViewModel.isAnswerSelected.set(true)
      selectedItems += itemIndex
      handleItemSelected()
    }
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
