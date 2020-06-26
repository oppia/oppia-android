package org.oppia.app.player.state.itemviewmodel

import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.model.Interaction
import org.oppia.app.model.InteractionObject
import org.oppia.app.model.ListOfSetsOfHtmlStrings
import org.oppia.app.model.StringList
import org.oppia.app.model.UserAnswer
import org.oppia.app.player.state.answerhandling.InteractionAnswerErrorOrAvailabilityCheckReceiver
import org.oppia.app.player.state.answerhandling.InteractionAnswerHandler
import org.oppia.app.recyclerview.OnItemDragListener

/** [StateItemViewModel] for drag drop & sort choice list. */
class DragAndDropSortInteractionViewModel(
  val entityId: String,
  interaction: Interaction,
  private val interactionAnswerErrorOrAvailabilityCheckReceiver: InteractionAnswerErrorOrAvailabilityCheckReceiver
) : StateItemViewModel(ViewType.DRAG_DROP_SORT_INTERACTION), InteractionAnswerHandler,
  OnItemDragListener {
  private val choiceStrings: List<String> by lazy {
    interaction.customizationArgsMap["choices"]?.setOfHtmlString?.htmlList ?: listOf()
  }

  val choiceItems: ArrayList<DragDropInteractionContentViewModel> =
    computeChoiceItems(choiceStrings)

  var isAnswerAvailable = ObservableField<Boolean>(false)

  init {
    val callback: Observable.OnPropertyChangedCallback = object : Observable.OnPropertyChangedCallback() {
      override fun onPropertyChanged(sender: Observable, propertyId: Int) {
        interactionAnswerErrorOrAvailabilityCheckReceiver.onPendingAnswerErrorOrAvailabilityCheck( /* pendingAnswerError= */ null, true)
      }
    }
    isAnswerAvailable.addOnPropertyChangedCallback(callback)
    isAnswerAvailable.set(true) // For Drag Drop Button will be enabled by default.
  }
  override fun onItemDragged(
    indexFrom: Int,
    indexTo: Int,
    adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
  ) {
    val item = choiceItems[indexFrom]
    choiceItems.removeAt(indexFrom)
    choiceItems.add(indexTo, item)
    adapter.notifyItemMoved(indexFrom, indexTo)
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

  companion object {
    private fun computeChoiceItems(
      choiceStrings: List<String>
    ): ArrayList<DragDropInteractionContentViewModel> {
      val itemList = ArrayList<DragDropInteractionContentViewModel>()
      itemList += choiceStrings.map { choiceString ->
        DragDropInteractionContentViewModel(
          htmlContent = StringList.newBuilder().addHtml(choiceString).build()
        )
      }
      return itemList
    }
  }
}
