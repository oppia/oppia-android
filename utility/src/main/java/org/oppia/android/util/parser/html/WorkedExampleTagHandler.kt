package org.oppia.android.util.parser.html

import android.graphics.Typeface
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.LeadingMarginSpan
import android.text.style.StyleSpan
import org.oppia.android.util.logging.ConsoleLogger
import org.xml.sax.Attributes

/** The custom tag corresponding to [WorkedExampleTagHandler]. */
const val CUSTOM_WORKED_EXAMPLE_TAG = "oppia-noninteractive-workedexample"

private const val CUSTOM_WORKED_EXAMPLE_QUESTION_ATTRIBUTE = "question-with-value"
private const val CUSTOM_WORKED_EXAMPLE_ANSWER_ATTRIBUTE = "answer-with-value"

/**
 * A custom tag handler for supporting worked examples parsed with [CustomHtmlContentHandler].
 */
class WorkedExampleTagHandler(
  private val consoleLogger: ConsoleLogger,
  private val questionLabel: String,
  private val answerLabel: String,
  private val leadingMarginPx: Int
) : CustomHtmlContentHandler.CustomTagHandler,
  CustomHtmlContentHandler.ContentDescriptionProvider {
  override fun handleTag(
    attributes: Attributes,
    openIndex: Int,
    closeIndex: Int,
    output: Editable,
    imageRetriever: CustomHtmlContentHandler.ImageRetriever?,
    customHtmlParser: CustomHtmlContentHandler.CustomHtmlParser
  ) {
    val questionHtml = attributes.getJsonStringValue(CUSTOM_WORKED_EXAMPLE_QUESTION_ATTRIBUTE)
    val answerHtml = attributes.getJsonStringValue(CUSTOM_WORKED_EXAMPLE_ANSWER_ATTRIBUTE)

    if (!questionHtml.isNullOrBlank() && !answerHtml.isNullOrBlank()) {
      val parsedQuestion = customHtmlParser.parseHtml(questionHtml).trimNewlines()
      val parsedAnswer = customHtmlParser.parseHtml(answerHtml).trimNewlines()
      if (parsedQuestion.isBlank() || parsedAnswer.isBlank()) {
        consoleLogger.e("WorkedExampleTagHandler", "Failed to parse worked example tag")
        return
      }
      val parsedWorkedExample = SpannableStringBuilder().apply {
        if (openIndex > 0 && output[openIndex - 1] != '\n') append('\n')
      }
      val workedExampleStart = parsedWorkedExample.length
      parsedWorkedExample
        .appendBoldLabel(questionLabel)
        .append('\n')
        .append(parsedQuestion)
        .append("\n\n")
        .appendBoldLabel(answerLabel)
        .append('\n')
        .append(parsedAnswer)
        .append('\n')
        .apply {
          setSpan(
            LeadingMarginSpan.Standard(leadingMarginPx),
            /* start= */ workedExampleStart,
            /* end= */ length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
          )
        }
      output.replace(openIndex, closeIndex, parsedWorkedExample)
    } else {
      consoleLogger.e("WorkedExampleTagHandler", "Failed to parse worked example tag")
    }
  }

  override fun handleTagForContentDescription(
    attributes: Attributes,
    openIndex: Int,
    closeIndex: Int,
    output: Editable,
    customHtmlParser: CustomHtmlContentHandler.CustomHtmlParser
  ) {
    createContentDescription(attributes, customHtmlParser)?.let { contentDescription ->
      output.replace(openIndex, closeIndex, contentDescription)
    }
  }

  override fun getContentDescription(
    attributes: Attributes,
    customHtmlParser: CustomHtmlContentHandler.CustomHtmlParser
  ): String? = createContentDescription(attributes, customHtmlParser)

  override fun shouldHandleNestedHtml(): Boolean = false

  private fun createContentDescription(
    attributes: Attributes,
    customHtmlParser: CustomHtmlContentHandler.CustomHtmlParser
  ): String? {
    val questionHtml = attributes.getJsonStringValue(CUSTOM_WORKED_EXAMPLE_QUESTION_ATTRIBUTE)
    val answerHtml = attributes.getJsonStringValue(CUSTOM_WORKED_EXAMPLE_ANSWER_ATTRIBUTE)
    if (questionHtml.isNullOrBlank() || answerHtml.isNullOrBlank()) return null

    val questionDescription =
      customHtmlParser.parseHtmlForContentDescription(questionHtml).trim()
    val answerDescription = customHtmlParser.parseHtmlForContentDescription(answerHtml).trim()
    return if (questionDescription.isNotBlank() && answerDescription.isNotBlank()) {
      "$questionLabel $questionDescription\n$answerLabel $answerDescription"
    } else null
  }

  private fun SpannableStringBuilder.appendBoldLabel(label: String) = apply {
    val labelStart = length
    append(label)
    setSpan(
      StyleSpan(Typeface.BOLD),
      labelStart,
      length,
      Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
  }

  private fun Spannable.trimNewlines(): CharSequence {
    val text = toString()
    val firstContentIndex = text.indexOfFirst { it != '\n' }
    if (firstContentIndex < 0) return ""
    val lastContentIndex = text.indexOfLast { it != '\n' } + 1
    return subSequence(firstContentIndex, lastContentIndex)
  }
}
