package org.oppia.android.app.player.state.itemviewmodel

import androidx.databinding.ObservableBoolean
import org.oppia.android.app.model.SubtitledHtml
import org.oppia.android.app.model.WrittenTranslationContext
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.util.parser.html.CustomHtmlContentHandler

/** [ObservableViewModel] for MultipleChoiceInput values or ItemSelectionInput values. */
class SelectionInteractionContentViewModel(
  val htmlContent: SubtitledHtml,
  val hasConversationView: Boolean,
  private val itemIndex: Int,
  private val selectionInteractionViewModel: SelectionInteractionViewModel,
  val isEnabled: ObservableBoolean,
  val customTagHandlers: Map<String, CustomHtmlContentHandler.CustomTagHandler>,
  private val writtenTranslationContext: WrittenTranslationContext,
  private val translationController: TranslationController,
) : ObservableViewModel() {
  var isAnswerSelected = ObservableBoolean()

  /** Returns content description by extracting text from [htmlContent]. */
  fun getContentDescription(): String {
    val contentSubtitledHtml =
      translationController.extractString(
        htmlContent, writtenTranslationContext
      )
    return CustomHtmlContentHandler.getContentDescription(
      contentSubtitledHtml,
      imageRetriever = null,
      customTagHandlers = customTagHandlers
    )
  }

  /** Handles item click by updating the selection state based on user interaction. */
  fun handleItemClicked() {
    val isCurrentlySelected = isAnswerSelected.get()
    val shouldNowBeSelected =
      selectionInteractionViewModel.updateSelection(itemIndex, isCurrentlySelected)
    if (isCurrentlySelected != shouldNowBeSelected) {
      isAnswerSelected.set(shouldNowBeSelected)
    }
  }
}
