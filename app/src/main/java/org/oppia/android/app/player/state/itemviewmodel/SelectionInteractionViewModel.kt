package org.oppia.android.app.player.state.itemviewmodel

import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.databinding.ObservableList
import org.oppia.android.app.model.Interaction
import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.SetOfTranslatableHtmlContentIds
import org.oppia.android.app.model.SubtitledHtml
import org.oppia.android.app.model.TranslatableHtmlContentId
import org.oppia.android.app.model.UserAnswer
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerErrorOrAvailabilityCheckReceiver
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerHandler
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerReceiver
import org.oppia.android.app.viewmodel.ObservableArrayList

/** Corresponds to the type of input that should be used for an item selection interaction view. */
enum class SelectionItemInputType {
  CHECKBOXES,
  RADIO_BUTTONS
}

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

  private val choiceSubtitledHtmls: List<SubtitledHtml> by lazy {
    interaction.customizationArgsMap["choices"]
      ?.schemaObjectList
      ?.schemaObjectList
      ?.map { schemaObject -> schemaObject.customSchemaValue.subtitledHtml }
      ?: listOf()
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

  /**Number of selected answers. It updates on answer selection.*/
  val selectedItemsCount = ObservableInt()
  val choiceItems: ObservableList<SelectionInteractionContentViewModel> =
    computeChoiceItems(choiceSubtitledHtmls, hasConversationView, this)

  private val isAnswerAvailable = ObservableField(false)

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
    val selectedItemSubtitledHtmls = selectedItems.map(choiceItems::get).map { it.htmlContent }
    if (interactionId == "ItemSelectionInput") {
      userAnswerBuilder.answer = InteractionObject.newBuilder().apply {
        setOfTranslatableHtmlContentIds = SetOfTranslatableHtmlContentIds.newBuilder().apply {
          addAllContentIds(
            selectedItemSubtitledHtmls.map { subtitledHtml ->
              TranslatableHtmlContentId.newBuilder().apply {
                contentId = subtitledHtml.contentId
              }.build()
            }
          )
        }.build()
      }.build()
      userAnswerBuilder.htmlAnswer = convertSelectedItemsToHtmlString(selectedItemSubtitledHtmls)
    } else if (selectedItems.size == 1) {
      userAnswerBuilder.answer =
        InteractionObject.newBuilder().setNonNegativeInt(selectedItems.first()).build()
      userAnswerBuilder.htmlAnswer = convertSelectedItemsToHtmlString(selectedItemSubtitledHtmls)
    }
    return userAnswerBuilder.build()
  }

  /** Returns an HTML list containing all of the HTML string elements as items in the list. */
  private fun convertSelectedItemsToHtmlString(subtitledHtmls: Collection<SubtitledHtml>): String {
    return when (subtitledHtmls.size) {
      0 -> ""
      1 -> subtitledHtmls.first().html
      else -> {
        val htmlList = subtitledHtmls.map { it.html }
        "<ul><li>${htmlList.joinToString(separator = "</li><li>")}</li></ul>"
      }
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
        selectedItemsCount.set(selectedItems.size)
        val wasSelectedItemListEmpty = isAnswerAvailable.get()
        if (selectedItems.isNotEmpty() != wasSelectedItemListEmpty) {
          isAnswerAvailable.set(selectedItems.isNotEmpty())
        }
        return false
      } else if (selectedItems.size < maxAllowableSelectionCount) {
        // TODO(#3624): Add warning to user when they exceed the number of allowable selections or are under the minimum
        //  number required.
        selectedItems += itemIndex
        selectedItemsCount.set(selectedItems.size)
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
      selectedItemsCount.set(selectedItems.size)
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
      choiceSubtitledHtmls: List<SubtitledHtml>,
      hasConversationView: Boolean,
      selectionInteractionViewModel: SelectionInteractionViewModel
    ): ObservableArrayList<SelectionInteractionContentViewModel> {
      val observableList = ObservableArrayList<SelectionInteractionContentViewModel>()
      observableList += choiceSubtitledHtmls.mapIndexed { index, subtitledHtml ->
        SelectionInteractionContentViewModel(
          htmlContent = subtitledHtml,
          hasConversationView = hasConversationView,
          itemIndex = index,
          selectionInteractionViewModel = selectionInteractionViewModel
        )
      }
      return observableList
    }
  }
}
