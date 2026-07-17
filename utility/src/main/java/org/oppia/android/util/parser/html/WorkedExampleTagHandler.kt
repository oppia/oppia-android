package org.oppia.android.util.parser.html

import android.text.Editable
import android.text.SpannableStringBuilder
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
  private val consoleLogger: ConsoleLogger
) : CustomHtmlContentHandler.CustomTagHandler {
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
      val parsedWorkedExample = SpannableStringBuilder()
        .append(customHtmlParser.parseHtml(questionHtml))
        .append('\n')
        .append(customHtmlParser.parseHtml(answerHtml))
      output.replace(openIndex, closeIndex, parsedWorkedExample)
    } else {
      consoleLogger.e("WorkedExampleTagHandler", "Failed to parse worked example tag")
    }
  }
}
