package org.oppia.android.app.player.state.itemviewmodel

import android.text.SpannableStringBuilder
import org.oppia.android.util.parser.html.CustomHtmlContentHandler

/** [StateItemViewModel] for content-card state. */
class ContentViewModel(
  val htmlContent: CharSequence,
  val gcsEntityId: String,
  val hasConversationView: Boolean,
  val isSplitView: Boolean,
  val supportsConceptCards: Boolean,
  val customTagHandlers: Map<String, CustomHtmlContentHandler.CustomTagHandler>
) : StateItemViewModel(ViewType.CONTENT) {

  private val underscoreRegex = Regex("(?<=\\s|[,.;?!])_{3,}(?=\\s|[,.;?!])")
  private val replacementText = "Blank"

  fun getContentDescription(inputText: CharSequence): String {
    return CustomHtmlContentHandler.getContentDescription(
      replaceRegexWithBlank(inputText),
      imageRetriever = null,
      customTagHandlers = customTagHandlers
    )
  }

  /**
   * Replaces "2+ underscores, with space/punctuation on both sides" in the input text with a
   * replacement string "blank", returning a Spannable.
   * Adjusts offsets to handle text length changes during replacements.
   */
  private fun replaceRegexWithBlank(inputText: CharSequence): String =
    SpannableStringBuilder(inputText).apply {
      underscoreRegex.findAll(inputText).forEach {
        replace(it.range.first, it.range.last + 1, replacementText)
      }
    }.toString()
}
