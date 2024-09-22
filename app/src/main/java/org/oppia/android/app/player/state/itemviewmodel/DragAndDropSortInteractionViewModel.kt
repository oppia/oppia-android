package org.oppia.android.app.player.state.itemviewmodel

import android.util.Log
import androidx.annotation.StringRes
import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.recyclerview.widget.RecyclerView
import org.oppia.android.R
import org.oppia.android.app.model.AnswerErrorCategory
import org.oppia.android.app.model.Interaction
import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.ListOfSetsOfHtmlStrings
import org.oppia.android.app.model.ListOfSetsOfTranslatableHtmlContentIds
import org.oppia.android.app.model.SetOfTranslatableHtmlContentIds
import org.oppia.android.app.model.StringList
import org.oppia.android.app.model.SubtitledHtml
import org.oppia.android.app.model.TranslatableHtmlContentId
import org.oppia.android.app.model.UserAnswer
import org.oppia.android.app.model.UserAnswerState
import org.oppia.android.app.model.WrittenTranslationContext
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerErrorOrAvailabilityCheckReceiver
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerHandler
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerReceiver
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.app.recyclerview.OnDragEndedListener
import org.oppia.android.app.recyclerview.OnItemDragListener
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.domain.exploration.ExplorationProgressController
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.util.data.AsyncResult
import javax.inject.Inject
import kotlinx.coroutines.runBlocking

/** Represents the type of errors that can be thrown by drag and drop sort interaction. */
enum class DragAndDropSortInteractionError(@StringRes private var error: Int?) {
  VALID(error = null),
  EMPTY_INPUT(error = R.string.drag_and_drop_interaction_empty_input);

  /**
   * Returns the string corresponding to this error's string resources, or null if there is none.
   */
  fun getErrorMessageFromStringRes(resourceHandler: AppLanguageResourceHandler): String? =
    error?.let(resourceHandler::getStringInLocale)
}

/** [StateItemViewModel] for drag drop & sort choice list. */
class DragAndDropSortInteractionViewModel private constructor(
  val entityId: String,
  val hasConversationView: Boolean,
  interaction: Interaction,
  private val interactionAnswerErrorOrAvailabilityCheckReceiver: InteractionAnswerErrorOrAvailabilityCheckReceiver, // ktlint-disable max-line-length
  val isSplitView: Boolean,
  private val writtenTranslationContext: WrittenTranslationContext,
  private val resourceHandler: AppLanguageResourceHandler,
  private val translationController: TranslationController,
  userAnswerState: UserAnswerState,
  explorationProgressController: ExplorationProgressController
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
      val translatedHtml =
        translationController.extractString(subtitledHtml, writtenTranslationContext)
      subtitledHtml.contentId to translatedHtml
    }

  private var answerErrorCetegory: AnswerErrorCategory = AnswerErrorCategory.NO_ERROR

  private val _originalChoiceItems: MutableList<DragDropInteractionContentViewModel> =
    computeOriginalChoiceItems(
      contentIdHtmlMap,
      choiceSubtitledHtmls,
      this,
      resourceHandler,
      explorationProgressController
    )

  private val _choiceItems = computeSelectedChoiceItems(
    contentIdHtmlMap,
    choiceSubtitledHtmls,
    this,
    resourceHandler,
    userAnswerState
  )
  val choiceItems: List<DragDropInteractionContentViewModel> = _choiceItems

  private var pendingAnswerError: String? = null
  private val isAnswerAvailable = ObservableField(false)
  var errorMessage = ObservableField<String>("")

  init {
    val callback: Observable.OnPropertyChangedCallback =
      object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable, propertyId: Int) {
          interactionAnswerErrorOrAvailabilityCheckReceiver.onPendingAnswerErrorOrAvailabilityCheck(
            pendingAnswerError,
            inputAnswerAvailable = true // Allow submission without arranging or merging items.
          )
        }
      }
    isAnswerAvailable.addOnPropertyChangedCallback(callback)
    errorMessage.addOnPropertyChangedCallback(callback)

    // Initializing with default values so that submit button is enabled by default.
    interactionAnswerErrorOrAvailabilityCheckReceiver.onPendingAnswerErrorOrAvailabilityCheck(
      pendingAnswerError = null,
      inputAnswerAvailable = true
    )
    checkPendingAnswerError(userAnswerState.answerErrorCategory)
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
    checkPendingAnswerError(AnswerErrorCategory.REAL_TIME)
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

  override fun getPendingAnswer(): UserAnswer = UserAnswer.newBuilder().apply {
    val selectedLists = _choiceItems.map { it.htmlContent }
    val userStringLists = _choiceItems.map { it.computeStringList() }
    listOfHtmlAnswers = convertItemsToAnswer(userStringLists)
    answer = InteractionObject.newBuilder().apply {
      listOfSetsOfTranslatableHtmlContentIds =
        ListOfSetsOfTranslatableHtmlContentIds.newBuilder().apply {
          addAllContentIdLists(selectedLists)
        }.build()
    }.build()
    this.writtenTranslationContext =
      this@DragAndDropSortInteractionViewModel.writtenTranslationContext
  }.build()

  /**
   * It checks the pending error for the current drag and drop sort interaction, and correspondingly
   * updates the error string based on the specified error category.
   */
  override fun checkPendingAnswerError(category: AnswerErrorCategory): String? {
    answerErrorCetegory = category
    pendingAnswerError = when (category) {
      AnswerErrorCategory.REAL_TIME -> null
      AnswerErrorCategory.SUBMIT_TIME ->
        getSubmitTimeError().getErrorMessageFromStringRes(resourceHandler)
      else -> null
    }
    errorMessage.set(pendingAnswerError)
    return pendingAnswerError
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
          dragAndDropSortInteractionViewModel = this,
          resourceHandler = resourceHandler
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

  private fun getSubmitTimeError(): DragAndDropSortInteractionError {
    return if (_originalChoiceItems == _choiceItems) {
      DragAndDropSortInteractionError.EMPTY_INPUT
    } else
      DragAndDropSortInteractionError.VALID
  }

  /** Implementation of [StateItemViewModel.InteractionItemFactory] for this view model. */
  class FactoryImpl @Inject constructor(
    private val resourceHandler: AppLanguageResourceHandler,
    private val translationController: TranslationController,
    private val explorationProgressController: ExplorationProgressController
  ) : InteractionItemFactory {
    override fun create(
      entityId: String,
      hasConversationView: Boolean,
      interaction: Interaction,
      interactionAnswerReceiver: InteractionAnswerReceiver,
      answerErrorReceiver: InteractionAnswerErrorOrAvailabilityCheckReceiver,
      hasPreviousButton: Boolean,
      isSplitView: Boolean,
      writtenTranslationContext: WrittenTranslationContext,
      timeToStartNoticeAnimationMs: Long?,
      userAnswerState: UserAnswerState
    ): StateItemViewModel {
      return DragAndDropSortInteractionViewModel(
        entityId,
        hasConversationView,
        interaction,
        answerErrorReceiver,
        isSplitView,
        writtenTranslationContext,
        resourceHandler,
        translationController,
        userAnswerState,
        explorationProgressController
      )
    }
  }

  override fun getUserAnswerState(): UserAnswerState {
    if (_choiceItems == _originalChoiceItems) {
      return UserAnswerState.newBuilder().apply {
        this.answerErrorCategory = answerErrorCetegory
      }.build()
    }
    return UserAnswerState.newBuilder().apply {
      val htmlContentIds = _choiceItems.map { it.htmlContent }
      listOfSetsOfTranslatableHtmlContentIds =
        ListOfSetsOfTranslatableHtmlContentIds.newBuilder().apply {
          addAllContentIdLists(htmlContentIds)
        }.build()
      answerErrorCategory = answerErrorCetegory
    }.build()
  }

  companion object {
    private fun computeOriginalChoiceItems(
      contentIdHtmlMap: Map<String, String>,
      choiceStrings: List<SubtitledHtml>,
      dragAndDropSortInteractionViewModel: DragAndDropSortInteractionViewModel,
      resourceHandler: AppLanguageResourceHandler,
      explorationProgressController: ExplorationProgressController
    ): MutableList<DragDropInteractionContentViewModel> {
      return runBlocking {
        when(val result = explorationProgressController.getCurrentState().retrieveData()) {
          is AsyncResult.Success -> {
            val ephemeralState = result.value
            val wrongAnswerList = ephemeralState.pendingState.wrongAnswerList

            choiceStrings.mapIndexed { index, subtitledHtml ->
              val contentIdFromWrongAnswer = wrongAnswerList?.lastOrNull()
                ?.userAnswer
                ?.answer
                ?.listOfSetsOfTranslatableHtmlContentIds
                ?.contentIdListsList
                ?.getOrNull(index)
                ?.contentIdsList
                ?.firstOrNull()
                ?.contentId

              val contentHtmlFromWrongAnswer = wrongAnswerList?.lastOrNull()
                ?.userAnswer
                ?.listOfHtmlAnswers
                ?.setOfHtmlStringsList
                ?.get(index)
                ?.htmlList
                ?.firstOrNull()

              val updatedContentIdMap = mapOf(
                contentIdFromWrongAnswer to contentHtmlFromWrongAnswer
              ).filterKeys { it != null }
                .filterValues { it != null }
                .mapKeys { it.key as String }
                .mapValues { it.value as String }

              DragDropInteractionContentViewModel(
                contentIdHtmlMap = updatedContentIdMap.ifEmpty {
                  contentIdHtmlMap
                },
                htmlContent = SetOfTranslatableHtmlContentIds.newBuilder().apply {
                  addContentIds(
                    TranslatableHtmlContentId.newBuilder().apply {
                      contentId = contentIdFromWrongAnswer ?: subtitledHtml.contentId
                    }
                  )
                }.build(),
                itemIndex = index,
                listSize = choiceStrings.size,
                dragAndDropSortInteractionViewModel = dragAndDropSortInteractionViewModel,
                resourceHandler = resourceHandler
              )
            }.toMutableList()
          }
          else -> choiceStrings.mapIndexed { index, subtitledHtml ->
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
              dragAndDropSortInteractionViewModel = dragAndDropSortInteractionViewModel,
              resourceHandler = resourceHandler
            )
          }.toMutableList()
        }
      }
    }
  }

  private fun computeSelectedChoiceItems(
    contentIdHtmlMap: Map<String, String>,
    choiceStrings: List<SubtitledHtml>,
    dragAndDropSortInteractionViewModel: DragAndDropSortInteractionViewModel,
    resourceHandler: AppLanguageResourceHandler,
    userAnswerState: UserAnswerState
  ): MutableList<DragDropInteractionContentViewModel> {
    return if (userAnswerState.listOfSetsOfTranslatableHtmlContentIds.contentIdListsCount == 0) {
      _originalChoiceItems.toMutableList()
    } else {
      userAnswerState.listOfSetsOfTranslatableHtmlContentIds.contentIdListsList
        .mapIndexed { index, contentId ->
          DragDropInteractionContentViewModel(
            contentIdHtmlMap = contentIdHtmlMap,
            htmlContent = contentId,
            itemIndex = index,
            listSize = choiceStrings.size,
            dragAndDropSortInteractionViewModel = dragAndDropSortInteractionViewModel,
            resourceHandler = resourceHandler
          )
        }.toMutableList()
    }
  }
}
