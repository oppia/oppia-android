package org.oppia.app.player.state.itemviewmodel

import androidx.databinding.ObservableList
import org.oppia.app.model.Interaction
import org.oppia.app.model.InteractionObject
import org.oppia.app.model.ListOfSetsOfHtmlStrings
import org.oppia.app.model.StringList
import org.oppia.app.model.UserAnswer
import org.oppia.app.player.state.answerhandling.InteractionAnswerHandler
import org.oppia.app.recyclerview.DragItemTouchHelperCallback
import org.oppia.app.viewmodel.ObservableArrayList

class DragAndDropSortInputViewModel(
  val entityId: String,
  interaction: Interaction
) : StateItemViewModel(ViewType.DRAG_DROP_SORT_INTERACTION), InteractionAnswerHandler,
  DragItemTouchHelperCallback.OnItemDragListener {
  private val allowMultipleItemsInSamePosition: Boolean by lazy {
    interaction.customizationArgsMap["allowMultipleItemsInSamePosition"]?.boolValue ?: false
  }
  private val choiceStrings: List<String> by lazy {
    interaction.customizationArgsMap["choices"]?.setOfHtmlString?.htmlList ?: listOf()
  }

  val choiceItems: ObservableList<DragDropInteractionContentViewModel> =
    computeChoiceItems(choiceStrings)

  override fun onItemDragged(indexFrom: Int, indexTo: Int) {
    val item = choiceItems[indexFrom]
    choiceItems.removeAt(indexFrom)
    choiceItems.add(indexTo, item)
  }

  override fun getPendingAnswer(): UserAnswer {
    val userAnswerBuilder = UserAnswer.newBuilder()
    val listItems = choiceItems.map { it.htmlContent }
    val listItemsHtml = choiceItems.map { it.htmlContent.htmlList.first() }
    userAnswerBuilder.htmlAnswer = convertSelectedItemsToHtmlString(listItemsHtml)
    userAnswerBuilder.answer =
      InteractionObject.newBuilder().setListOfSetsOfHtmlString(
        ListOfSetsOfHtmlStrings.newBuilder().addAllSetOfHtmlStrings(listItems).build()
      ).build()
    return userAnswerBuilder.build()
  }

  /** Returns an HTML list containing all of the HTML string elements as items in the list. */
  private fun convertSelectedItemsToHtmlString(htmlItems: Collection<String>): String {
    return "<li>${htmlItems.joinToString(separator = "</li><li>")}</li>"
  }

  /** Returns the [SelectionItemInputType] that should be used to render items of this view model. */
  fun getSortType(): Boolean {
    return allowMultipleItemsInSamePosition
  }

  companion object {
    private fun computeChoiceItems(
      choiceStrings: List<String>
    ): ObservableArrayList<DragDropInteractionContentViewModel> {
      val observableList = ObservableArrayList<DragDropInteractionContentViewModel>()
      observableList += choiceStrings.mapIndexed { index, choiceString ->
        DragDropInteractionContentViewModel(
          htmlContent = StringList.newBuilder().addHtml(choiceString).build(), itemIndex = index
        )
      }
      return observableList
    }
  }
}
