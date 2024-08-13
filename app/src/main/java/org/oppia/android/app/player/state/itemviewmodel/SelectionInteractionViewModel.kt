package org.oppia.android.app.player.state.itemviewmodel

import androidx.annotation.StringRes
import androidx.databinding.Observable
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableList
import org.oppia.android.R
import org.oppia.android.app.model.AnswerErrorCategory
import org.oppia.android.app.model.Interaction
import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.ItemSelectionAnswerState
import org.oppia.android.app.model.SetOfTranslatableHtmlContentIds
import org.oppia.android.app.model.SubtitledHtml
import org.oppia.android.app.model.TranslatableHtmlContentId
import org.oppia.android.app.model.UserAnswer
import org.oppia.android.app.model.UserAnswerState
import org.oppia.android.app.model.WrittenTranslationContext
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerErrorOrAvailabilityCheckReceiver
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerHandler
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerReceiver
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.viewmodel.ObservableArrayList
import org.oppia.android.domain.translation.TranslationController
import javax.inject.Inject

/** Corresponds to the type of input that should be used for an item selection interaction view. */
enum class SelectionItemInputType {
  CHECKBOXES,
  RADIO_BUTTONS
}

/** Enum to the store the errors of selection input. */
enum class SelectionInputError(@StringRes private var error: Int?) {
  VALID(error = null),
  EMPTY_INPUT(error = R.string.selection_error_empty_input);

  /**
   * Returns the string corresponding to this error's string resources, or null if there is none.
   */
  fun getErrorMessageFromStringRes(resourceHandler: AppLanguageResourceHandler): String? =
    error?.let(resourceHandler::getStringInLocale)
}

/** [StateItemViewModel] for multiple or item-selection input choice list. */
class SelectionInteractionViewModel private constructor(
  val entityId: String,
  val hasConversationView: Boolean,
  interaction: Interaction,
  private val interactionAnswerErrorOrAvailabilityCheckReceiver: InteractionAnswerErrorOrAvailabilityCheckReceiver, // ktlint-disable max-line-length
  val isSplitView: Boolean,
  val writtenTranslationContext: WrittenTranslationContext,
  private val translationController: TranslationController,
  private val resourceHandler: AppLanguageResourceHandler,
  userAnswerState: UserAnswerState
) : StateItemViewModel(ViewType.SELECTION_INTERACTION), InteractionAnswerHandler {
  private val interactionId: String = interaction.id

  private val choiceSubtitledHtmls: List<SubtitledHtml> by lazy {
    interaction.customizationArgsMap["choices"]
      ?.schemaObjectList
      ?.schemaObjectList
      ?.map { schemaObject -> schemaObject.customSchemaValue.subtitledHtml }
      ?: listOf()
  }

  private var answerErrorCetegory: AnswerErrorCategory = AnswerErrorCategory.NO_ERROR

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
  private val enabledItemsList by lazy {
    List(choiceSubtitledHtmls.size) {
      ObservableBoolean(true)
    }
  }
  val choiceItems: ObservableList<SelectionInteractionContentViewModel> =
    computeChoiceItems(choiceSubtitledHtmls, hasConversationView, this, enabledItemsList)

  private var pendingAnswerError: String? = null
  private val isAnswerAvailable = ObservableField(false)
  val errorMessage = ObservableField<String>("")
  val selectedItemText =
    ObservableField(
      resourceHandler.getStringInLocale(
        R.string.state_fragment_item_selection_no_items_selected_hint_text
      )
    )

  init {
    val callback: Observable.OnPropertyChangedCallback =
      object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable, propertyId: Int) {
          interactionAnswerErrorOrAvailabilityCheckReceiver.onPendingAnswerErrorOrAvailabilityCheck(
            pendingAnswerError,
            inputAnswerAvailable = true // Allow blank answer submission.
          )
        }
      }
    errorMessage.addOnPropertyChangedCallback(callback)
    isAnswerAvailable.addOnPropertyChangedCallback(callback)

    // Initializing with default values so that submit button is enabled by default.
    interactionAnswerErrorOrAvailabilityCheckReceiver.onPendingAnswerErrorOrAvailabilityCheck(
      pendingAnswerError = null,
      inputAnswerAvailable = true
    )

    if (userAnswerState.itemSelection.selectedIndexesCount != 0) {
      userAnswerState.itemSelection.selectedIndexesList.forEach { selectedIndex ->
        selectedItems += selectedIndex
        choiceItems[selectedIndex].isAnswerSelected.set(true)
      }
      updateItemSelectability()
      updateSelectionText()
      updateIsAnswerAvailable()
    }

    checkPendingAnswerError(userAnswerState.answerErrorCategory)
  }

  override fun getUserAnswerState(): UserAnswerState {
    return UserAnswerState.newBuilder().apply {
      this.itemSelection = ItemSelectionAnswerState.newBuilder().addAllSelectedIndexes(
        selectedItems
      ).build()
      this.answerErrorCategory = answerErrorCetegory
    }.build()
  }

  override fun getPendingAnswer(): UserAnswer = UserAnswer.newBuilder().apply {
    val translationContext = this@SelectionInteractionViewModel.writtenTranslationContext
    val selectedItemSubtitledHtmls = selectedItems.map(choiceItems::get).map { it.htmlContent }
    val itemHtmls = selectedItemSubtitledHtmls.map { subtitledHtml ->
      translationController.extractString(subtitledHtml, translationContext)
    }
    if (interactionId == "ItemSelectionInput") {
      answer = InteractionObject.newBuilder().apply {
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
      htmlAnswer = convertSelectedItemsToHtmlString(itemHtmls)
    } else if (selectedItems.size == 1) {
      answer = InteractionObject.newBuilder().apply {
        nonNegativeInt = selectedItems.first()
      }.build()
      htmlAnswer = convertSelectedItemsToHtmlString(itemHtmls)
    }
    writtenTranslationContext = translationContext
  }.build()

  /**
   * It checks the pending error for the current selection input, and correspondingly
   * updates the error string based on the specified error category.
   */
  override fun checkPendingAnswerError(category: AnswerErrorCategory): String? {
    answerErrorCetegory = category
    pendingAnswerError = when (category) {
      AnswerErrorCategory.REAL_TIME -> {
        null
      }
      AnswerErrorCategory.SUBMIT_TIME ->
        getSubmitTimeError().getErrorMessageFromStringRes(resourceHandler)
      else -> null
    }
    errorMessage.set(pendingAnswerError)
    return pendingAnswerError
  }

  /** Returns an HTML list containing all of the HTML string elements as items in the list. */
  private fun convertSelectedItemsToHtmlString(itemHtmls: Collection<String>): String {
    return when (itemHtmls.size) {
      0 -> ""
      1 -> itemHtmls.first()
      else -> {
        "<ul><li>${itemHtmls.joinToString(separator = "</li><li>")}</li></ul>"
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
    checkPendingAnswerError(AnswerErrorCategory.REAL_TIME)
    return when {
      isCurrentlySelected -> {
        selectedItems -= itemIndex
        updateIsAnswerAvailable()
        updateSelectionText()
        updateItemSelectability()
        false
      }
      !areCheckboxesBound() -> {
        // De-select all other items to simulate a radio button group.
        choiceItems.forEach { item -> item.isAnswerSelected.set(false) }
        selectedItems.clear()
        selectedItems += itemIndex
        updateIsAnswerAvailable()
        true
      }
      selectedItems.size < maxAllowableSelectionCount -> {
        selectedItems += itemIndex
        updateIsAnswerAvailable()
        updateSelectionText()
        updateItemSelectability()
        true
      }
      else -> {
        // Do not change the current status if it isn't valid to do so.
        isCurrentlySelected
      }
    }
  }

  private fun updateSelectionText() {
    if (selectedItems.size < maxAllowableSelectionCount) {
      selectedItemText.set(
        resourceHandler.getStringInLocale(
          R.string.state_fragment_item_selection_some_items_selected_hint_text
        )
      )
    }
    if (selectedItems.size == 0) {
      selectedItemText.set(
        resourceHandler.getStringInLocale(
          R.string.state_fragment_item_selection_no_items_selected_hint_text
        )
      )
    }
    if (selectedItems.size == maxAllowableSelectionCount) {
      selectedItemText.set(
        resourceHandler.getStringInLocaleWithWrapping(
          R.string.state_fragment_item_selection_max_items_selected_hint_text,
          maxAllowableSelectionCount.toString()
        )
      )
    }
  }

  private fun updateItemSelectability() {
    if (selectedItems.size == maxAllowableSelectionCount) {
      // All non-selected items should be disabled when the limit is reached.
      enabledItemsList.filterIndexed { idx, _ -> idx !in selectedItems }.forEach { it.set(false) }
    } else enabledItemsList.forEach { it.set(true) } // Otherwise, all items are available.
  }

  private fun areCheckboxesBound(): Boolean {
    return interactionId == "ItemSelectionInput" && maxAllowableSelectionCount > 1
  }

  private fun updateIsAnswerAvailable() {
    val wasSelectedItemListEmpty = isAnswerAvailable.get()
    if (selectedItems.isNotEmpty() != wasSelectedItemListEmpty) {
      isAnswerAvailable.set(selectedItems.isNotEmpty())
    }
  }

  private fun getSubmitTimeError(): SelectionInputError {
    return if (selectedItems.isEmpty())
      SelectionInputError.EMPTY_INPUT
    else
      SelectionInputError.VALID
  }

  /** Implementation of [StateItemViewModel.InteractionItemFactory] for this view model. */
  class FactoryImpl @Inject constructor(
    private val translationController: TranslationController,
    private val resourceHandler: AppLanguageResourceHandler
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
      return SelectionInteractionViewModel(
        entityId,
        hasConversationView,
        interaction,
        answerErrorReceiver,
        isSplitView,
        writtenTranslationContext,
        translationController,
        resourceHandler,
        userAnswerState
      )
    }
  }

  companion object {
    private fun computeChoiceItems(
      choiceSubtitledHtmls: List<SubtitledHtml>,
      hasConversationView: Boolean,
      selectionInteractionViewModel: SelectionInteractionViewModel,
      enabledItemsList: List<ObservableBoolean>
    ): ObservableArrayList<SelectionInteractionContentViewModel> {
      val observableList = ObservableArrayList<SelectionInteractionContentViewModel>()
      observableList += choiceSubtitledHtmls.mapIndexed { index, subtitledHtml ->
        SelectionInteractionContentViewModel(
          htmlContent = subtitledHtml,
          hasConversationView = hasConversationView,
          itemIndex = index,
          selectionInteractionViewModel = selectionInteractionViewModel,
          isEnabled = enabledItemsList[index]
        )
      }
      return observableList
    }
  }
}
