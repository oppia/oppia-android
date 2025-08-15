package org.oppia.android.app.player.state.itemviewmodel

import org.oppia.android.app.model.SubtitledHtml
import org.oppia.android.app.model.WrittenTranslationContext
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.util.parser.html.CustomHtmlContentHandler

/** [ObservableViewModel] for MultipleChoice or ItemSelection submitted answer. */
class SelectionSubmittedItemViewModel (
  val htmlContent: SubtitledHtml,
  val hasConversationView: Boolean,
  val isEnabled: Boolean,
  val customTagHandlers: Map<String, CustomHtmlContentHandler.CustomTagHandler>,
  val writtenTranslationContext: WrittenTranslationContext,
  private val translationController: TranslationController,
  val entityId: String,
  val resourceHandler: AppLanguageResourceHandler,
  val isFlashback: Boolean
) : ObservableViewModel() {

  /** Returns content description by extracting text from [htmlContent]. */
  fun getContentDescription(): String {
    val contentSubtitledHtml =
      translationController.extractString(
        htmlContent, writtenTranslationContext
      )
    return CustomHtmlContentHandler.getContentDescription(
      contentSubtitledHtml,
      customTagHandlers = customTagHandlers
    )
  }
}
