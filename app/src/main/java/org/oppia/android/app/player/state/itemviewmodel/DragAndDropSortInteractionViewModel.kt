package org.oppia.android.app.player.state.itemviewmodel

import androidx.annotation.StringRes
import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.recyclerview.widget.RecyclerView
import org.oppia.android.app.model.AnswerAndResponse
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
import org.oppia.android.app.view.models.R
import org.oppia.android.domain.translation.TranslationController
import java.util.Collections
import javax.inject.Inject

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
  wrongAnswerList: List<AnswerAndResponse>
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

  private var answerErrorCategory: AnswerErrorCategory = AnswerErrorCategory.NO_ERROR

  private var _originalChoiceItems: MutableList<DragDropInteractionContentViewModel> =
    computeOriginalChoiceItems(contentIdHtmlMap, choiceSubtitledHtmls, this, resourceHandler)

  lateinit var choiceItems: List<DragDropInteractionContentViewModel>
  private var _choiceItems: MutableList<DragDropInteractionContentViewModel> =
    computeSelectedChoiceItems(
      contentIdHtmlMap,
      this,
      resourceHandler,
      userAnswerState,
      wrongAnswerList
    )

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
    if (indexFrom == indexTo) return
    Collections.swap(_choiceItems, indexFrom, indexTo)

    _choiceItems[indexFrom].itemIndex = indexFrom
    _choiceItems[indexTo].itemIndex = indexTo

    adapter.notifyItemMoved(indexFrom, indexTo)
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
    answerErrorCategory = category
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

    // To update the list
    (adapter as BindableAdapter<*>).setDataUnchecked(_choiceItems)

    // Trigger pending answer check to re-enable submit button
    checkPendingAnswerError(AnswerErrorCategory.REAL_TIME)
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

    // Update the list
    (adapter as BindableAdapter<*>).setDataUnchecked(_choiceItems)

    // Trigger pending answer check* to re-enable submit button
    checkPendingAnswerError(AnswerErrorCategory.REAL_TIME)
  }

  private fun getSubmitTimeError(): DragAndDropSortInteractionError {
    val haveItemsChanged = _originalChoiceItems.size != _choiceItems.size ||
      _originalChoiceItems.zip(_choiceItems).any { (originalItem, currentItem) ->
        originalItem.htmlContent != currentItem.htmlContent
      }
    return if (!haveItemsChanged) {
      DragAndDropSortInteractionError.EMPTY_INPUT
    } else {
      DragAndDropSortInteractionError.VALID
    }
  }

  /** Implementation of [StateItemViewModel.InteractionItemFactory] for this view model. */
  class FactoryImpl @Inject constructor(
    private val resourceHandler: AppLanguageResourceHandler,
    private val translationController: TranslationController
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
      userAnswerState: UserAnswerState,
      wrongAnswerList: List<AnswerAndResponse>
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
        wrongAnswerList
      )
    }
  }

  override fun getUserAnswerState(): UserAnswerState {
    return UserAnswerState.newBuilder().apply {
      val htmlContentIds = _choiceItems.map { it.htmlContent }
      listOfSetsOfTranslatableHtmlContentIds =
        ListOfSetsOfTranslatableHtmlContentIds.newBuilder().apply {
          addAllContentIdLists(htmlContentIds)
        }.build()
      this.answerErrorCategory =
        this@DragAndDropSortInteractionViewModel.answerErrorCategory
    }.build()
  }

  companion object {
    private fun computeOriginalChoiceItems(
      contentIdHtmlMap: Map<String, String>,
      choiceStrings: List<SubtitledHtml>,
      dragAndDropSortInteractionViewModel: DragAndDropSortInteractionViewModel,
      resourceHandler: AppLanguageResourceHandler
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
          dragAndDropSortInteractionViewModel = dragAndDropSortInteractionViewModel,
          resourceHandler = resourceHandler
        )
      }.toMutableList()
    }
  }

  /**
   * Computes the selected choice items based on the provided [userAnswerState] and
   * [wrongAnswerList].
   *
   * If [userAnswerState] contains a saved drag-and-drop ordering (from a previous user interaction),
   * that ordering is used. Otherwise, if there is a most recent wrong answer, its ordering is used.
   * If neither is available, the default ordering from the interaction's customization args is used.
   */
  private fun computeSelectedChoiceItems(
    contentIdHtmlMap: Map<String, String>,
    dragAndDropSortInteractionViewModel: DragAndDropSortInteractionViewModel,
    resourceHandler: AppLanguageResourceHandler,
    userAnswerState: UserAnswerState,
    wrongAnswerList: List<AnswerAndResponse>
  ): MutableList<DragDropInteractionContentViewModel> {
    val savedContentIdLists = when {
      userAnswerState.hasListOfSetsOfTranslatableHtmlContentIds() -> {
        userAnswerState.listOfSetsOfTranslatableHtmlContentIds.contentIdListsList
      }
      wrongAnswerList.isNotEmpty() -> {
        val latestWrongAnswer = wrongAnswerList.last().userAnswer
        if (latestWrongAnswer.answer.hasListOfSetsOfTranslatableHtmlContentIds()) {
          latestWrongAnswer.answer.listOfSetsOfTranslatableHtmlContentIds.contentIdListsList
        } else {
          null
        }
      }
      else -> null
    }

    val items = if (savedContentIdLists != null && savedContentIdLists.isNotEmpty()) {
      savedContentIdLists.mapIndexed { index, setOfTranslatableHtmlContentIds ->
        DragDropInteractionContentViewModel(
          contentIdHtmlMap = contentIdHtmlMap,
          htmlContent = SetOfTranslatableHtmlContentIds.newBuilder().apply {
            for (contentIds in setOfTranslatableHtmlContentIds.contentIdsList) {
              addContentIds(
                TranslatableHtmlContentId.newBuilder().apply {
                  contentId = contentIds.contentId
                }
              )
            }
          }.build(),
          itemIndex = index,
          listSize = savedContentIdLists.size,
          dragAndDropSortInteractionViewModel = dragAndDropSortInteractionViewModel,
          resourceHandler = resourceHandler
        )
      }.toMutableList()
    } else {
      _originalChoiceItems.toMutableList()
    }

    // Set _originalChoiceItems based on the last submitted wrong answer so that
    // getSubmitTimeError() correctly detects whether the user has made changes since then.
    // If no answer has been submitted, keep the default ordering from interaction choices.
    if (wrongAnswerList.isNotEmpty()) {
      val latestWrongAnswer = wrongAnswerList.last().userAnswer
      if (latestWrongAnswer.answer.hasListOfSetsOfTranslatableHtmlContentIds()) {
        val lastSubmittedContentIdLists =
          latestWrongAnswer.answer.listOfSetsOfTranslatableHtmlContentIds.contentIdListsList
        _originalChoiceItems = lastSubmittedContentIdLists.mapIndexed { index, set ->
          DragDropInteractionContentViewModel(
            contentIdHtmlMap = contentIdHtmlMap,
            htmlContent = SetOfTranslatableHtmlContentIds.newBuilder().apply {
              for (contentIds in set.contentIdsList) {
                addContentIds(
                  TranslatableHtmlContentId.newBuilder().apply {
                    contentId = contentIds.contentId
                  }
                )
              }
            }.build(),
            itemIndex = index,
            listSize = lastSubmittedContentIdLists.size,
            dragAndDropSortInteractionViewModel = dragAndDropSortInteractionViewModel,
            resourceHandler = resourceHandler
          )
        }.toMutableList()
      }
    }
    choiceItems = items
    return items
  }
}
