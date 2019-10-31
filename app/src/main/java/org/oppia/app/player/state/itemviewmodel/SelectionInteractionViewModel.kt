package org.oppia.app.player.state.itemviewmodel

import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import org.oppia.app.model.InteractionObject
import org.oppia.app.model.StringList
import org.oppia.app.player.state.SelectionItemInputType
import org.oppia.app.player.state.answerhandling.InteractionAnswerHandler
import org.oppia.app.player.state.answerhandling.InteractionAnswerReceiver

/** ViewModel for multiple or item-selection input choice list. */
class SelectionInteractionViewModel(
  choiceStrings: List<String>, val interactionId: String,
  private val interactionAnswerReceiver: InteractionAnswerReceiver, val maxAllowableSelectionCount: Int,
  @Suppress("unused") val minAllowableSelectionCount: Int,
  existingAnswer: InteractionObject?, val isReadOnly: Boolean
): StateItemViewModel(), InteractionAnswerHandler {
  val selectedItems = computeSelectedItems(
    existingAnswer ?: InteractionObject.getDefaultInstance(), interactionId, choiceStrings
  )
  val choiceItems: ObservableList<SelectionInteractionContentViewModel> = computeChoiceItems(
    choiceStrings, selectedItems, isReadOnly
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

  fun handleItemSelected() {
    // Only push the answer if explicit submission isn't required.
    if (maxAllowableSelectionCount == 1) {
      interactionAnswerReceiver.onAnswerReadyForSubmission(getPendingAnswer())
    }
  }

  /** Returns the [SelectionItemInputType] that should be used to render items of this view model. */
  fun getSelectionItemInputType(): SelectionItemInputType {
    return if (interactionId == "ItemSelectionInput") {
      if (maxAllowableSelectionCount > 1) {
        SelectionItemInputType.CHECKBOXES
      } else {
        SelectionItemInputType.RADIO_BUTTONS
      }
    } else {
      SelectionItemInputType.RADIO_BUTTONS
    }
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
      choiceStrings: List<String>, selectedItems: List<Int>, isReadOnly: Boolean
    ): ObservableArrayList<SelectionInteractionContentViewModel> {
      val observableList = ObservableArrayList<SelectionInteractionContentViewModel>()
      observableList += choiceStrings.mapIndexed { index, choiceString ->
        val isAnswerSelected = index in selectedItems
        SelectionInteractionContentViewModel(
          htmlContent = choiceString, isAnswerInitiallySelected = isAnswerSelected, isReadOnly = isReadOnly
        )
      }
      return observableList
    }
  }
}
