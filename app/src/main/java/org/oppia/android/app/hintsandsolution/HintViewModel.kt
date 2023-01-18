package org.oppia.android.app.hintsandsolution

import androidx.databinding.ObservableBoolean
import org.oppia.android.util.parser.html.CustomHtmlContentHandler

class HintViewModel(
  val title: String, val hintSummary: String, val isHintRevealed: ObservableBoolean
): HintsAndSolutionItemViewModel() {
  val hintContentDescription: String by lazy {
    CustomHtmlContentHandler.fromHtml(
      hintSummary,
      imageRetriever = null,
      customTagHandlers = mapOf()
    ).toString()
  }
}
