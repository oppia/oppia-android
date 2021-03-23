package org.oppia.android.util.parser

import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ClickableSpan
import android.view.View
import org.oppia.android.util.logging.ConsoleLogger
import org.xml.sax.Attributes

/** The custom tag corresponding to [ConceptCardTagHandler]. */
const val CUSTOM_CONCEPT_CARD_TAG = "oppia-noninteractive-skillreview"

// https://mohammedlakkadshaw.com/blog/handling-custom-tags-in-android-using-html-taghandler.html/
class ConceptCardTagHandler(
  private val customOppiaTagActionListener: HtmlParser.CustomOppiaTagActionListener?,
  private val consoleLogger: ConsoleLogger
) : CustomHtmlContentHandler.CustomTagHandler {
  override fun handleTag(
    attributes: Attributes,
    openIndex: Int,
    closeIndex: Int,
    output: Editable,
    imageRetriever: CustomHtmlContentHandler.ImageRetriever
  ) {
    // Replace the custom tag with a clickable piece of text based on the tag's customizations.
    val skillId = attributes.getJsonStringValue("skill_id-with-value")
    val text = attributes.getJsonStringValue("text-with-value")
    if (skillId != null && text != null) {
      val spannableBuilder = SpannableStringBuilder(text)
      spannableBuilder.setSpan(
        object : ClickableSpan() {
          override fun onClick(view: View) {
            customOppiaTagActionListener?.onConceptCardLinkClicked(view, skillId)
          }
        },
        0, text.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE
      )
      output.replace(openIndex, closeIndex, spannableBuilder)
    } else consoleLogger.e("ConceptCardTagHandler", "Failed to parse concept card tag")
  }
}
