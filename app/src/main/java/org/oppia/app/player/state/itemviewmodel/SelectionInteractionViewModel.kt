package org.oppia.app.player.state.itemviewmodel

import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.databinding.ObservableList
import org.oppia.app.model.Interaction
import org.oppia.app.model.InteractionObject
import org.oppia.app.model.StringList
import org.oppia.app.model.UserAnswer
import org.oppia.app.player.state.SelectionItemInputType
import org.oppia.app.player.state.answerhandling.InteractionAnswerErrorOrAvailabilityCheckReceiver
import org.oppia.app.player.state.answerhandling.InteractionAnswerHandler
import org.oppia.app.player.state.answerhandling.InteractionAnswerReceiver
import org.oppia.app.viewmodel.ObservableArrayList

/** [StateItemViewModel] for multiple or item-selection input choice list. */
class SelectionInteractionViewModel(
  val entityId: String,
  val hasConversationView: Boolean,
  interaction: Interaction,
  private val interactionAnswerReceiver: InteractionAnswerReceiver,
  private val interactionAnswerErrorOrAvailabilityCheckReceiver: InteractionAnswerErrorOrAvailabilityCheckReceiver, // ktlint-disable max-line-length
  val isSplitView: Boolean
) : StateItemViewModel(ViewType.SELECTION_INTERACTION), InteractionAnswerHandler {
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
    interaction.customizationArgsMap["maxAllowableSelectionCount"]?.signedInt
      ?: minAllowableSelectionCount
  }
  private val selectedItems: MutableList<Int> = mutableListOf()
  val choiceItems: ObservableList<SelectionInteractionContentViewModel> =
    computeChoiceItems(choiceStrings, hasConversationView, this)

  private val isAnswerAvailable = ObservableField<Boolean>(false)

  init {
    val callback: Observable.OnPropertyChangedCallback =
      object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable, propertyId: Int) {
          interactionAnswerErrorOrAvailabilityCheckReceiver.onPendingAnswerErrorOrAvailabilityCheck(
            pendingAnswerError = null,
            inputAnswerAvailable = selectedItems.isNotEmpty()
          )
        }
      }
    isAnswerAvailable.addOnPropertyChangedCallback(callback)
  }

  override fun isExplicitAnswerSubmissionRequired(): Boolean {
    // If more than one answer is allowed, then a submission button is needed.
    return maxAllowableSelectionCount > 1
  }

  override fun getPendingAnswer(): UserAnswer {
    val userAnswerBuilder = UserAnswer.newBuilder()
    val selectedItemsHtml = selectedItems.map(choiceItems::get).map { it.htmlContent }
    if (interactionId == "ItemSelectionInput") {
      userAnswerBuilder.answer = InteractionObject.newBuilder().setSetOfHtmlString(
        StringList.newBuilder().addAllHtml(selectedItemsHtml)
      ).build()
      userAnswerBuilder.htmlAnswer = convertSelectedItemsToHtmlString(selectedItemsHtml)
    } else if (selectedItems.size == 1) {
      userAnswerBuilder.answer =
        InteractionObject.newBuilder().setNonNegativeInt(selectedItems.first()).build()
      userAnswerBuilder.htmlAnswer = convertSelectedItemsToHtmlString(selectedItemsHtml)
    }
    return userAnswerBuilder.build()
  }

  /** Returns an HTML list containing all of the HTML string elements as items in the list. */
  private fun convertSelectedItemsToHtmlString(htmlItems: Collection<String>): String {
    return when (htmlItems.size) {
      0 -> ""
      1 -> htmlItems.first()
      else -> "<ul><li>${htmlItems.joinToString(separator = "</li><li>")}</li></ul>"
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

  /** Catalogs an item being clicked by the user and returns whether the item should be considered selected. */
  fun updateSelection(itemIndex: Int, isCurrentlySelected: Boolean): Boolean {
    if (areCheckboxesBound()) {
      if (isCurrentlySelected) {
        selectedItems -= itemIndex
        val wasSelectedItemListEmpty = isAnswerAvailable.get()
        if (selectedItems.isNotEmpty() != wasSelectedItemListEmpty) {
          isAnswerAvailable.set(selectedItems.isNotEmpty())
        }
        return false
      } else if (selectedItems.size < maxAllowableSelectionCount) {
        // TODO(#32): Add warning to user when they exceed the number of allowable selections or are under the minimum
        //  number required.
        selectedItems += itemIndex
        val wasSelectedItemListEmpty = isAnswerAvailable.get()
        if (selectedItems.isNotEmpty() != wasSelectedItemListEmpty) {
          isAnswerAvailable.set(selectedItems.isNotEmpty())
        }
        return true
      }
    } else {
      // Disable all items to simulate a radio button group.
      choiceItems.forEach { item -> item.isAnswerSelected.set(false) }
      selectedItems.clear()
      selectedItems += itemIndex
      val wasSelectedItemListEmpty = isAnswerAvailable.get()
      if (selectedItems.isNotEmpty() != wasSelectedItemListEmpty) {
        isAnswerAvailable.set(selectedItems.isNotEmpty())
      }
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
    private fun computeChoiceItems(
      choiceStrings: List<String>,
      hasConversationView: Boolean,
      selectionInteractionViewModel: SelectionInteractionViewModel
    ): ObservableArrayList<SelectionInteractionContentViewModel> {
      val observableList = ObservableArrayList<SelectionInteractionContentViewModel>()
      observableList += choiceStrings.mapIndexed { index, choiceString ->
        SelectionInteractionContentViewModel(
          htmlContent = choiceString,
          hasConversationView = hasConversationView,
          itemIndex = index,
          selectionInteractionViewModel = selectionInteractionViewModel
        )
      }
      return observableList
    }
  }
}
