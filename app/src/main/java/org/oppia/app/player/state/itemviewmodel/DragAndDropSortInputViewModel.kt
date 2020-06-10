package org.oppia.app.player.state.itemviewmodel

import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.model.Interaction
import org.oppia.app.model.InteractionObject
import org.oppia.app.model.ListOfSetsOfHtmlStrings
import org.oppia.app.model.StringList
import org.oppia.app.model.UserAnswer
import org.oppia.app.player.state.answerhandling.InteractionAnswerHandler
import org.oppia.app.recyclerview.DragItemTouchHelperCallback

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

  val choiceItems: ArrayList<DragDropInteractionContentViewModel> =
    computeChoiceItems(choiceStrings, this)

  override fun onItemDragged(
    indexFrom: Int,
    indexTo: Int,
    adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
  ) {
    val item = choiceItems[indexFrom]
    choiceItems.removeAt(indexFrom)
    choiceItems.add(indexTo, item)
    adapter.notifyItemMoved(indexFrom,indexTo)
  }

  override fun getPendingAnswer(): UserAnswer {
    val userAnswerBuilder = UserAnswer.newBuilder()
    val listItems = choiceItems.map { it.htmlContent }
    val listItemsHtml = choiceItems.map {
      it.htmlContent.htmlList.joinToString(
        prefix = "<br><ul>",
        separator = "</ul><ul>",
        postfix = "</ul>"
      )
    }
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

  fun updateList(itemIndex: Int) {
    val item = choiceItems[itemIndex]
    val nextItem = choiceItems[itemIndex + 1]
    choiceItems.removeAt(itemIndex)
    nextItem.htmlContent = StringList.newBuilder().addAllHtml(nextItem.htmlContent.htmlList)
      .addAllHtml(item.htmlContent.htmlList)
      .build()
    nextItem.itemIndex = itemIndex
    nextItem.listSize = item.listSize - 1
    choiceItems[itemIndex] = nextItem
  }

  companion object {
    private fun computeChoiceItems(
      choiceStrings: List<String>,
      dragAndDropSortInputViewModel: DragAndDropSortInputViewModel
    ): ArrayList<DragDropInteractionContentViewModel> {
      val observableList = ArrayList<DragDropInteractionContentViewModel>()
      observableList += choiceStrings.mapIndexed { index, choiceString ->
        DragDropInteractionContentViewModel(
          htmlContent = StringList.newBuilder().addHtml(choiceString).build(),
          itemIndex = index,
          listSize = choiceStrings.size,
          dragAndDropSortInputViewModel = dragAndDropSortInputViewModel
        )
      }
      return observableList
    }
  }
}
