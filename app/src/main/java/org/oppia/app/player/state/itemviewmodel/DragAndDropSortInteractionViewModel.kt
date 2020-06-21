package org.oppia.app.player.state.itemviewmodel

import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.model.Interaction
import org.oppia.app.model.InteractionObject
import org.oppia.app.model.ListOfSetsOfHtmlStrings
import org.oppia.app.model.StringList
import org.oppia.app.model.UserAnswer
import org.oppia.app.player.state.answerhandling.InteractionAnswerHandler
import org.oppia.app.recyclerview.BindableAdapter
import org.oppia.app.recyclerview.OnItemDragListener

/** [StateItemViewModel] for drag drop & sort choice list. */
class DragAndDropSortInteractionViewModel(
  val entityId: String,
  interaction: Interaction
) : StateItemViewModel(ViewType.DRAG_DROP_SORT_INTERACTION), InteractionAnswerHandler,
  OnItemDragListener {
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
    if (allowMultipleItemsInSamePosition) {
      choiceItems[indexFrom].itemIndex = indexFrom
      choiceItems[indexTo].itemIndex = indexTo
      (adapter as BindableAdapter<*>).setDataUnchecked(choiceItems)
    }else{
      (adapter as BindableAdapter<*>).notifyItemMoved(indexFrom, indexTo)
    }
  }

  override fun getPendingAnswer(): UserAnswer {
    val userAnswerBuilder = UserAnswer.newBuilder()
    val listItems = choiceItems.map { it.htmlContent }
    val listItemsHtml = choiceItems.map { it.htmlContent.htmlList.joinToString() }
    userAnswerBuilder.htmlAnswer = convertSelectedItemsToHtmlString(listItemsHtml)
    userAnswerBuilder.answer =
      InteractionObject.newBuilder().setListOfSetsOfHtmlString(
        ListOfSetsOfHtmlStrings.newBuilder().addAllSetOfHtmlStrings(listItems).build()
      ).build()
    return userAnswerBuilder.build()
  }

  /** Returns an HTML list containing all of the HTML string elements as items in the list. */
  private fun convertSelectedItemsToHtmlString(htmlItems: Collection<String>): String {
    return htmlItems.joinToString(separator = "<br>")
  }

  /** Returns whether the grouping is allowed or not for [DragAndDropSortInteractionViewModel]. */
  fun getGroupingStatus(): Boolean {
    return allowMultipleItemsInSamePosition
  }

  fun updateList(
    itemIndex: Int,
    adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
  ) {
    val item = choiceItems[itemIndex]
    val nextItem = choiceItems[itemIndex + 1]
    nextItem.htmlContent = StringList.newBuilder().addAllHtml(nextItem.htmlContent.htmlList)
      .addAllHtml(item.htmlContent.htmlList)
      .build()
    choiceItems[itemIndex + 1] = nextItem

    choiceItems.removeAt(itemIndex)

    choiceItems.forEachIndexed { index, dragDropInteractionContentViewModel ->
      dragDropInteractionContentViewModel.itemIndex = index
      dragDropInteractionContentViewModel.listSize = choiceItems.size
    }
    //to update the content of grouped item
    (adapter as BindableAdapter<*>).setDataUnchecked(choiceItems)
  }

  companion object {
    private fun computeChoiceItems(
      choiceStrings: List<String>,
      dragAndDropSortInteractionViewModel: DragAndDropSortInteractionViewModel
    ): ArrayList<DragDropInteractionContentViewModel> {
      val itemList = ArrayList<DragDropInteractionContentViewModel>()
      itemList += choiceStrings.mapIndexed { index, choiceString ->
        DragDropInteractionContentViewModel(
          htmlContent = StringList.newBuilder().addHtml(choiceString).build(),
          itemIndex = index,
          listSize = choiceStrings.size,
          dragAndDropSortInteractionViewModel = dragAndDropSortInteractionViewModel
        )
      }
      return itemList
    }
  }
}
