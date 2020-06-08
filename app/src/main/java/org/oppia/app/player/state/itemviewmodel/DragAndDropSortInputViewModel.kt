package org.oppia.app.player.state.itemviewmodel

import androidx.databinding.ObservableList
import org.oppia.app.model.Interaction
import org.oppia.app.player.state.answerhandling.InteractionAnswerErrorReceiver
import org.oppia.app.player.state.answerhandling.InteractionAnswerHandler
import org.oppia.app.player.state.answerhandling.InteractionAnswerReceiver
import org.oppia.app.viewmodel.ObservableArrayList

class DragAndDropSortInputViewModel(
  val explorationId: String,
  interaction: Interaction,
  interactionAnswerReceiver: InteractionAnswerReceiver,
  interactionAnswerErrorReceiver: InteractionAnswerErrorReceiver
) : StateItemViewModel(ViewType.DRAG_DROP_SORT_INTERACTION), InteractionAnswerHandler {
  private val allowMultipleItemsInSamePosition: Boolean by lazy {
    interaction.customizationArgsMap["allowMultipleItemsInSamePosition"]?.boolValue ?: false
  }
  private val choiceStrings: List<String> by lazy {
    interaction.customizationArgsMap["choices"]?.setOfHtmlString?.htmlList ?: listOf()
  }

  val choiceItems: ObservableList<DragDropInteractionContentViewModel> =
    computeChoiceItems(choiceStrings, this)

  fun onItemDragged(indexFrom: Int, indexTo: Int) {

  }

  companion object {
    private fun computeChoiceItems(
      choiceStrings: List<String>, dragAndDropSortInputViewModel: DragAndDropSortInputViewModel
    ): ObservableArrayList<DragDropInteractionContentViewModel> {
      val observableList = ObservableArrayList<DragDropInteractionContentViewModel>()
      observableList += choiceStrings.mapIndexed { index, choiceString ->
        DragDropInteractionContentViewModel(
          htmlContent = choiceString, itemIndex = index
        )
      }
      return observableList
    }
  }
}
