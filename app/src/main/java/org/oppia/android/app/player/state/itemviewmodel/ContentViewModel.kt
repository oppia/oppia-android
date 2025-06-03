package org.oppia.android.app.player.state.itemviewmodel

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

  /** Returns content description by extracting text from [htmlContent]. */
  fun getContentDescription(): String {
    val contentDescription = CustomHtmlContentHandler.getContentDescription(
      htmlContent.toString(),
      imageRetriever = null,
      customTagHandlers = customTagHandlers
    )
    return replaceRegexWithBlank(contentDescription)
  }

  /**
   * Replaces "2+ underscores, with space/punctuation on both sides" in the input text with a
   * replacement string "blank", returning a Spannable.
   * Adjusts offsets to handle text length changes during replacements.
   */
  private fun replaceRegexWithBlank(inputText: CharSequence): String =
    underscoreRegex.replace(inputText, replacementText)
}
