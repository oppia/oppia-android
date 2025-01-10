package org.oppia.android.util.parser.html

import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ClickableSpan
import android.view.View
import org.oppia.android.util.logging.ConsoleLogger
import org.xml.sax.Attributes

/** The custom tag corresponding to [ConceptCardTagHandler]. */
const val CUSTOM_CONCEPT_CARD_TAG = "oppia-noninteractive-skillreview"
const val CUSTOM_CONCEPT_CARD_SKILL_ID = "skill_id-with-value"
const val CUSTOM_CONCEPT_CARD_TEXT_VALUE = "text-with-value"

// https://mohammedlakkadshaw.com/blog/handling-custom-tags-in-android-using-html-taghandler.html/
class ConceptCardTagHandler(
  private val listener: ConceptCardLinkClickListener,
  private val consoleLogger: ConsoleLogger
) : CustomHtmlContentHandler.CustomTagHandler, CustomHtmlContentHandler.ContentDescriptionProvider {
  override fun handleTag(
    attributes: Attributes,
    openIndex: Int,
    closeIndex: Int,
    output: Editable,
    imageRetriever: CustomHtmlContentHandler.ImageRetriever?
  ) {
    // Replace the custom tag with a clickable piece of text based on the tag's customizations.
    val skillId = attributes.getJsonStringValue(CUSTOM_CONCEPT_CARD_SKILL_ID)
    val text = attributes.getJsonStringValue(CUSTOM_CONCEPT_CARD_TEXT_VALUE)
    if (skillId != null && text != null) {
      val spannableBuilder = SpannableStringBuilder(text)
      spannableBuilder.setSpan(
        object : ClickableSpan() {
          override fun onClick(view: View) {
            listener.onConceptCardLinkClicked(view, skillId)
          }
        },
        /* start= */ 0, /* end= */ text.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE
      )
      output.replace(openIndex, closeIndex, spannableBuilder)
    } else consoleLogger.e("ConceptCardTagHandler", "Failed to parse concept card tag")
  }

  /** Listener called when concept card links are clicked. */
  interface ConceptCardLinkClickListener {
    /**
     * Called when a concept card link is called in the specified view corresponding to the
     * specified skill ID.
     */
    fun onConceptCardLinkClicked(view: View, skillId: String)
  }

  override fun getContentDescription(attributes: Attributes): String? {
    val skillId = attributes.getJsonStringValue(CUSTOM_CONCEPT_CARD_SKILL_ID)
    val text = attributes.getJsonStringValue(CUSTOM_CONCEPT_CARD_TEXT_VALUE)
    return if (!skillId.isNullOrBlank() && !text.isNullOrBlank()) {
      "$text concept card $skillId"
    } else ""
  }
}
