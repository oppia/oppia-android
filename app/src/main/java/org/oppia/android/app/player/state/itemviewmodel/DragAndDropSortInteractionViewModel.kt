package org.oppia.android.app.player.state.itemviewmodel

import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.recyclerview.widget.RecyclerView
import org.oppia.android.app.model.Interaction
import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.ListOfSetsOfHtmlStrings
import org.oppia.android.app.model.ListOfSetsOfTranslatableHtmlContentIds
import org.oppia.android.app.model.SetOfTranslatableHtmlContentIds
import org.oppia.android.app.model.StringList
import org.oppia.android.app.model.SubtitledHtml
import org.oppia.android.app.model.TranslatableHtmlContentId
import org.oppia.android.app.model.UserAnswer
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerErrorOrAvailabilityCheckReceiver
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerHandler
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.app.recyclerview.OnDragEndedListener
import org.oppia.android.app.recyclerview.OnItemDragListener

/** [StateItemViewModel] for drag drop & sort choice list. */
class DragAndDropSortInteractionViewModel(
  val entityId: String,
  val hasConversationView: Boolean,
  interaction: Interaction,
  private val interactionAnswerErrorOrAvailabilityCheckReceiver: InteractionAnswerErrorOrAvailabilityCheckReceiver, // ktlint-disable max-line-length
  val isSplitView: Boolean
) : StateItemViewModel(ViewType.DRAG_DROP_SORT_INTERACTION),
  InteractionAnswerHandler,
  OnItemDragListener,
  OnDragEndedListener {
  private val allowMultipleItemsInSamePosition: Boolean by lazy {
    interaction.customizationArgsMap["allowMultipleItemsInSamePosition"]?.boolValue ?: false
  }
  private val choiceSubtitledHtmls: List<SubtitledHtml> by lazy {
    interaction.customizationArgsMap["choices"]
      ?.schemaObjectList
      ?.schemaObjectList
      ?.map { schemaObject -> schemaObject.customSchemaValue.subtitledHtml }
      ?: listOf()
  }

  private val contentIdHtmlMap: Map<String, String> =
    choiceSubtitledHtmls.associate { subtitledHtml ->
      subtitledHtml.contentId to subtitledHtml.html
    }

  private val _choiceItems: MutableList<DragDropInteractionContentViewModel> =
    computeChoiceItems(contentIdHtmlMap, choiceSubtitledHtmls, this)

  val choiceItems: List<DragDropInteractionContentViewModel> = _choiceItems

  private val isAnswerAvailable = ObservableField(false)

  init {
    val callback: Observable.OnPropertyChangedCallback =
      object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable, propertyId: Int) {
          interactionAnswerErrorOrAvailabilityCheckReceiver.onPendingAnswerErrorOrAvailabilityCheck(
            pendingAnswerError = null,
            inputAnswerAvailable = true
          )
        }
      }
    isAnswerAvailable.addOnPropertyChangedCallback(callback)
    isAnswerAvailable.set(true) // For drag drop submit button will be enabled by default.
  }

  override fun onItemDragged(
    indexFrom: Int,
    indexTo: Int,
    adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
  ) {
    val item = _choiceItems[indexFrom]
    _choiceItems.removeAt(indexFrom)
    _choiceItems.add(indexTo, item)

    // Update the data of item moved for every drag if merge icons are displayed.
    if (allowMultipleItemsInSamePosition) {
      _choiceItems[indexFrom].itemIndex = indexFrom
      _choiceItems[indexTo].itemIndex = indexTo
    }
    adapter.notifyItemMoved(indexFrom, indexTo)
  }

  override fun onDragEnded(adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>) {
    // Update the data list if once drag is complete and merge icons are displayed.
    if (allowMultipleItemsInSamePosition) {
      (adapter as BindableAdapter<*>).setDataUnchecked(_choiceItems)
    }
  }

  fun onItemMoved(
    indexFrom: Int,
    indexTo: Int,
    adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
  ) {
    val item = _choiceItems[indexFrom]
    _choiceItems.removeAt(indexFrom)
    _choiceItems.add(indexTo, item)

    _choiceItems[indexFrom].itemIndex = indexFrom
    _choiceItems[indexTo].itemIndex = indexTo

    (adapter as BindableAdapter<*>).setDataUnchecked(_choiceItems)
  }

  override fun getPendingAnswer(): UserAnswer {
    val userAnswerBuilder = UserAnswer.newBuilder()
    val selectedLists = _choiceItems.map { it.htmlContent }
    val userStringLists = _choiceItems.map { it.computeStringList() }
    userAnswerBuilder.listOfHtmlAnswers = convertItemsToAnswer(userStringLists)
    userAnswerBuilder.answer =
      InteractionObject.newBuilder().apply {
        listOfSetsOfTranslatableHtmlContentIds =
          ListOfSetsOfTranslatableHtmlContentIds.newBuilder().apply {
            _choiceItems.map { }
            addAllContentIdLists(selectedLists)
          }.build()
      }.build()
    return userAnswerBuilder.build()
  }

  /** Returns an HTML list containing all of the HTML string elements as items in the list. */
  private fun convertItemsToAnswer(htmlItems: List<StringList>): ListOfSetsOfHtmlStrings {
    return ListOfSetsOfHtmlStrings.newBuilder()
      .addAllSetOfHtmlStrings(htmlItems)
      .build()
  }

  /** Returns whether the grouping is allowed or not for [DragAndDropSortInteractionViewModel]. */
  fun getGroupingStatus(): Boolean {
    return allowMultipleItemsInSamePosition
  }

  fun updateList(
    itemIndex: Int,
    adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
  ) {
    val item = _choiceItems[itemIndex]
    val nextItem = _choiceItems[itemIndex + 1]
    nextItem.htmlContent = SetOfTranslatableHtmlContentIds.newBuilder().apply {
      addAllContentIds(nextItem.htmlContent.contentIdsList)
      addAllContentIds(item.htmlContent.contentIdsList)
    }.build()
    _choiceItems[itemIndex + 1] = nextItem

    _choiceItems.removeAt(itemIndex)

    _choiceItems.forEachIndexed { index, dragDropInteractionContentViewModel ->
      dragDropInteractionContentViewModel.itemIndex = index
      dragDropInteractionContentViewModel.listSize = _choiceItems.size
    }
    // to update the content of grouped item
    (adapter as BindableAdapter<*>).setDataUnchecked(_choiceItems)
  }

  fun unlinkElement(itemIndex: Int, adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>) {
    val item = _choiceItems[itemIndex]
    _choiceItems.removeAt(itemIndex)
    item.htmlContent.contentIdsList.forEach { contentId ->
      _choiceItems.add(
        itemIndex,
        DragDropInteractionContentViewModel(
          contentIdHtmlMap = contentIdHtmlMap,
          htmlContent = SetOfTranslatableHtmlContentIds.newBuilder().apply {
            addContentIds(contentId)
          }.build(),
          itemIndex = 0,
          listSize = 0,
          dragAndDropSortInteractionViewModel = this
        )
      )
    }

    _choiceItems.forEachIndexed { index, dragDropInteractionContentViewModel ->
      dragDropInteractionContentViewModel.itemIndex = index
      dragDropInteractionContentViewModel.listSize = _choiceItems.size
    }
    // to update the list
    (adapter as BindableAdapter<*>).setDataUnchecked(_choiceItems)
  }

  companion object {
    private fun computeChoiceItems(
      contentIdHtmlMap: Map<String, String>,
      choiceStrings: List<SubtitledHtml>,
      dragAndDropSortInteractionViewModel: DragAndDropSortInteractionViewModel
    ): MutableList<DragDropInteractionContentViewModel> {
      return choiceStrings.mapIndexed { index, subtitledHtml ->
        DragDropInteractionContentViewModel(
          contentIdHtmlMap = contentIdHtmlMap,
          htmlContent = SetOfTranslatableHtmlContentIds.newBuilder().apply {
            addContentIds(
              TranslatableHtmlContentId.newBuilder().apply {
                contentId = subtitledHtml.contentId
              }
            )
          }.build(),
          itemIndex = index,
          listSize = choiceStrings.size,
          dragAndDropSortInteractionViewModel = dragAndDropSortInteractionViewModel
        )
      }.toMutableList()
    }
  }
}
