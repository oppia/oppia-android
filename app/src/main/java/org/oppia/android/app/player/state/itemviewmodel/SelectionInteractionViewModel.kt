package org.oppia.android.app.player.state.itemviewmodel

import androidx.databinding.Observable
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableList
import javax.inject.Inject
import org.oppia.android.R
import org.oppia.android.app.model.Interaction
import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.SetOfTranslatableHtmlContentIds
import org.oppia.android.app.model.SubtitledHtml
import org.oppia.android.app.model.TranslatableHtmlContentId
import org.oppia.android.app.model.UserAnswer
import org.oppia.android.app.model.WrittenTranslationContext
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerErrorOrAvailabilityCheckReceiver
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerHandler
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerReceiver
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.viewmodel.ObservableArrayList
import org.oppia.android.domain.translation.TranslationController

/** Corresponds to the type of input that should be used for an item selection interaction view. */
enum class SelectionItemInputType {
  CHECKBOXES,
  RADIO_BUTTONS
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
  private val resourceHandler: AppLanguageResourceHandler
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
  val enabledItemsList by lazy {
    List(choiceSubtitledHtmls.size) {
      ObservableBoolean(true)
    }
  }
  val choiceItems: ObservableList<SelectionInteractionContentViewModel> =
    computeChoiceItems(choiceSubtitledHtmls, hasConversationView, this, enabledItemsList)

  private val isAnswerAvailable = ObservableField(false)
  val selectedItemText =
    ObservableField(resourceHandler.getStringInLocale(R.string.please_select_all_correct_choices))

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
    return when {
      isCurrentlySelected -> {
        selectedItems -= itemIndex
        updateIsAnswerAvailable()
        updateSelectionText(isCurrentlySelected)
        false
      }
      !areCheckboxesBound() -> {
        // Disable all items to simulate a radio button group.
        choiceItems.forEach { item -> item.isAnswerSelected.set(false) }
        selectedItems.clear()
        selectedItems += itemIndex
        updateIsAnswerAvailable()
        true
      }
      selectedItems.size < maxAllowableSelectionCount -> {
        // TODO(#3624): Add warning to user when they exceed the number of allowable selections or are under the minimum
        //  number required.
        selectedItems += itemIndex
        updateIsAnswerAvailable()
        updateSelectionText(isCurrentlySelected)
        true
      }
      else -> {
        // Do not change the current status if it isn't valid to do so.
        isCurrentlySelected
      }
    }
  }

  private fun updateSelectionText(isCurrentlySelected: Boolean) {
    if (selectedItems.size < maxAllowableSelectionCount) {
      selectedItemText.set(
        resourceHandler.getStringInLocale(
          R.string.you_may_select_more_choices
        )
      )
    }
    if (selectedItems.size == 0) {
      selectedItemText.set(
        resourceHandler.getStringInLocale(
          R.string.please_select_all_correct_choices
        )
      )
    }
    if (selectedItems.size == maxAllowableSelectionCount) {
      selectedItemText.set(
        resourceHandler.getStringInLocaleWithWrapping(
          R.string.no_more_than_choices_may_be_selected,
          maxAllowableSelectionCount.toString()
        )
      )
      enabledItemsList.forEach {
        it.set(isCurrentlySelected)
      }
    } else {
      enabledItemsList.forEach {
        it.set(true)
      }
    }
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
      timeToStartNoticeAnimationMs: Long?
    ): StateItemViewModel {
      return SelectionInteractionViewModel(
        entityId,
        hasConversationView,
        interaction,
        answerErrorReceiver,
        isSplitView,
        writtenTranslationContext,
        translationController,
        resourceHandler
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
          enabledItem = enabledItemsList[index]
        )
      }
      return observableList
    }
  }
}
