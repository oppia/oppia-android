package org.oppia.android.util.parser.html

import android.graphics.Typeface
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.LeadingMarginSpan
import android.text.style.ParagraphStyle
import android.text.style.StyleSpan
import org.oppia.android.util.logging.ConsoleLogger
import org.xml.sax.Attributes

/** The custom tag corresponding to [WorkedExampleTagHandler]. */
const val CUSTOM_WORKED_EXAMPLE_TAG = "oppia-noninteractive-workedexample"

private const val CUSTOM_WORKED_EXAMPLE_QUESTION_ATTRIBUTE = "question-with-value"
private const val CUSTOM_WORKED_EXAMPLE_ANSWER_ATTRIBUTE = "answer-with-value"

/**
 * The localized labels that are displayed before a worked example's question and answer.
 *
 * These are supplied by the caller rather than being read here since they're user-facing
 * translatable strings, and only the app layer's strings are processed by the translation pipeline.
 */
data class WorkedExampleLabels(val questionLabel: String, val answerLabel: String)

/**
 * A custom tag handler for supporting worked examples parsed with [CustomHtmlContentHandler].
 *
 * A worked example's question and answer are stored as HTML within the tag's attributes, so this
 * handler relies on [nestedHtmlParser] to parse them. That parser is provided by the caller since
 * only the caller knows which tags should be supported within a worked example.
 */
class WorkedExampleTagHandler(
  private val consoleLogger: ConsoleLogger,
  private val labels: WorkedExampleLabels,
  private val leadingMarginPx: Int,
  private val nestedHtmlParser: NestedHtmlParser
) : CustomHtmlContentHandler.CustomTagHandler,
  CustomHtmlContentHandler.ContentDescriptionProvider {
  override fun handleTag(
    attributes: Attributes,
    openIndex: Int,
    closeIndex: Int,
    output: Editable,
    imageRetriever: CustomHtmlContentHandler.ImageRetriever?
  ) {
    val questionHtml = attributes.getNestedHtmlValue(CUSTOM_WORKED_EXAMPLE_QUESTION_ATTRIBUTE)
    val answerHtml = attributes.getNestedHtmlValue(CUSTOM_WORKED_EXAMPLE_ANSWER_ATTRIBUTE)
    if (questionHtml.isNullOrBlank() || answerHtml.isNullOrBlank()) {
      logFailedToParse()
      return
    }

    val parsedQuestion = nestedHtmlParser.parseHtml(questionHtml).trimNewlines()
    val parsedAnswer = nestedHtmlParser.parseHtml(answerHtml).trimNewlines()
    if (parsedQuestion.isBlank() || parsedAnswer.isBlank()) {
      logFailedToParse()
      return
    }

    val parsedWorkedExample = SpannableStringBuilder().apply {
      // Worked examples are block content, so they're separated from whatever precedes them by a
      // blank line. Existing line breaks count towards that blank line, so an example that follows
      // a paragraph is spaced exactly like one that follows another example.
      if (openIndex > 0) {
        var precedingNewlineCount = 0
        while (precedingNewlineCount < openIndex &&
          output[openIndex - 1 - precedingNewlineCount] == '\n'
        ) {
          precedingNewlineCount++
        }
        repeat(2 - precedingNewlineCount) { append('\n') }
      }
    }
    val workedExampleStart = parsedWorkedExample.length
    // Note that the question and answer are inserted into the assembled example rather than being
    // appended to it. Appending would extend any span that ends where the appended text starts,
    // which would carry the indentation of a trailing list item through the rest of the example.
    parsedWorkedExample.appendBoldLabel(labels.questionLabel).append('\n')
    val questionIndex = parsedWorkedExample.length
    parsedWorkedExample
      .appendSeparatingNewlines(count = 2, precedingContent = parsedQuestion)
      .appendBoldLabel(labels.answerLabel)
      .append('\n')
    val answerIndex = parsedWorkedExample.length
    parsedWorkedExample.appendSeparatingNewlines(count = 1, precedingContent = parsedAnswer)
    // The answer is inserted first so that the question's insertion index stays valid.
    parsedWorkedExample.insert(answerIndex, parsedAnswer)
    parsedWorkedExample.insert(questionIndex, parsedQuestion)
    parsedWorkedExample.setSpan(
      LeadingMarginSpan.Standard(leadingMarginPx),
      /* start= */ workedExampleStart,
      /* end= */ parsedWorkedExample.length,
      Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    output.replace(openIndex, closeIndex, parsedWorkedExample)
  }

  // Note that this is implemented in addition to getContentDescription since the two are used in
  // different situations: this replaces the tag in-line when the content is being parsed without an
  // image retriever (see CustomHtmlContentHandler.handleTag), whereas getContentDescription
  // contributes to the description that's computed for the whole text view.
  override fun handleTagForContentDescription(
    attributes: Attributes,
    openIndex: Int,
    closeIndex: Int,
    output: Editable
  ) {
    computeContentDescription(attributes)?.let { contentDescription ->
      output.replace(openIndex, closeIndex, contentDescription)
    }
  }

  override fun getContentDescription(attributes: Attributes): String? =
    computeContentDescription(attributes)

  private fun computeContentDescription(attributes: Attributes): String? {
    val questionHtml = attributes.getNestedHtmlValue(CUSTOM_WORKED_EXAMPLE_QUESTION_ATTRIBUTE)
    val answerHtml = attributes.getNestedHtmlValue(CUSTOM_WORKED_EXAMPLE_ANSWER_ATTRIBUTE)
    if (questionHtml.isNullOrBlank() || answerHtml.isNullOrBlank()) return null

    val questionDescription = nestedHtmlParser.parseHtmlForContentDescription(questionHtml).trim()
    val answerDescription = nestedHtmlParser.parseHtmlForContentDescription(answerHtml).trim()
    return if (questionDescription.isNotBlank() && answerDescription.isNotBlank()) {
      // The question is always read before the answer so that the example makes sense when it's
      // read out by a screen reader.
      "${labels.questionLabel} $questionDescription\n${labels.answerLabel} $answerDescription"
    } else null
  }

  private fun logFailedToParse() {
    consoleLogger.e("WorkedExampleTagHandler", "Failed to parse worked example tag")
  }

  private fun SpannableStringBuilder.appendBoldLabel(label: String) = apply {
    val labelStart = length
    append(label)
    setSpan(StyleSpan(Typeface.BOLD), labelStart, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
  }

  private fun Spannable.trimNewlines(): CharSequence {
    val text = toString()
    val firstContentIndex = text.indexOfFirst { it != '\n' }
    if (firstContentIndex < 0) return ""
    // Any newline that a paragraph span ends on is kept since such spans must end just after a
    // newline. Trimming one would leave the span on an invalid boundary, which makes Android
    // drop it (losing a trailing list item's bullet) or grow it over the content that follows.
    val lastParagraphSpanEnd =
      getSpans(0, length, ParagraphStyle::class.java).maxOfOrNull { getSpanEnd(it) } ?: 0
    val lastContentIndex =
      maxOf(text.indexOfLast { it != '\n' } + 1, lastParagraphSpanEnd.coerceAtMost(length))
    return subSequence(firstContentIndex, lastContentIndex)
  }

  /**
   * Appends newlines to this builder such that [precedingContent], once inserted just before them,
   * ends with exactly [count] of them.
   */
  private fun SpannableStringBuilder.appendSeparatingNewlines(
    count: Int,
    precedingContent: CharSequence
  ) = apply {
    var trailingNewlineCount = 0
    while (trailingNewlineCount < precedingContent.length &&
      precedingContent[precedingContent.length - 1 - trailingNewlineCount] == '\n'
    ) {
      trailingNewlineCount++
    }
    repeat(count - trailingNewlineCount) { append('\n') }
  }

  /**
   * Parser for the HTML that's nested within a worked example's question and answer attributes.
   */
  interface NestedHtmlParser {
    /** Returns a [Spannable] with the specified [html] parsed for display. */
    fun parseHtml(html: String): Spannable

    /** Returns the content description corresponding to the specified [html]. */
    fun parseHtmlForContentDescription(html: String): String
  }
}
