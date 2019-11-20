package org.oppia.util.parser

import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import org.xml.sax.Attributes
import javax.inject.Inject

private const val CUSTOM_IMG_TAG = "oppia-noninteractive-image"
private const val REPLACE_IMG_TAG = "img"
private const val CUSTOM_IMG_FILE_PATH_ATTRIBUTE = "filepath-with-value"
private const val REPLACE_IMG_FILE_PATH_ATTRIBUTE = "src"

private const val CUSTOM_CONCEPT_CARD_TAG = "oppia-concept-card-link"

/** Html Parser to parse custom Oppia tags with Android-compatible versions. */
class HtmlParser private constructor(
  private val urlImageParserFactory: UrlImageParser.Factory,
  private val entityType: String,
  private val entityId: String,
  customOppiaTagActionListener: CustomOppiaTagActionListener?
) {
  private val conceptCardTagHandler = ConceptCardTagHandler(customOppiaTagActionListener)

  /**
   * Parses a raw HTML string with support for custom Oppia tags.
   *
   * @param rawString raw HTML to parse
   * @param htmlContentTextView the [TextView] that will contain the returned [Spannable]
   * @param supportsLinks whether the provided [TextView] should support link forwarding (it's recommended not to use
   *     this for [TextView]s that are within other layouts that need to support clicking (default false)
   * @return a [Spannable] representing the styled text.
   */
  fun parseOppiaHtml(rawString: String, htmlContentTextView: TextView, imageCenterAlign: Boolean = true, supportsLinks: Boolean = false): Spannable {
    var htmlContent = rawString
    if (htmlContent.contains("\n\t")) {
      htmlContent = htmlContent.replace("\n\t", "")
    }
    if (htmlContent.contains("\n\n")) {
      htmlContent = htmlContent.replace("\n\n", "")
    }
    if (htmlContent.contains(CUSTOM_IMG_TAG)) {
      htmlContent = htmlContent.replace(CUSTOM_IMG_TAG, REPLACE_IMG_TAG)
      htmlContent = htmlContent.replace( CUSTOM_IMG_FILE_PATH_ATTRIBUTE, REPLACE_IMG_FILE_PATH_ATTRIBUTE)
      htmlContent = htmlContent.replace("&amp;quot;", "")
    }

    // https://stackoverflow.com/a/8662457
    if (supportsLinks) {
      htmlContentTextView.movementMethod = LinkMovementMethod.getInstance()
    }

    val imageGetter =  urlImageParserFactory.create(htmlContentTextView, entityType, entityId, imageCenterAlign)
    return trimSpannable(CustomHtmlContentHandler.fromHtml(
      htmlContent, imageGetter, mapOf(CUSTOM_CONCEPT_CARD_TAG to conceptCardTagHandler)
    )as SpannableStringBuilder)
  }

  // https://mohammedlakkadshaw.com/blog/handling-custom-tags-in-android-using-html-taghandler.html/
  private class ConceptCardTagHandler(
    private val customOppiaTagActionListener: CustomOppiaTagActionListener?
  ): CustomHtmlContentHandler.CustomTagHandler {
    override fun handleTag(attributes: Attributes, openIndex: Int, closeIndex: Int, output: Editable) {
      val skillId = attributes.getValue("skill-id")
      output.setSpan(object : ClickableSpan() {
        override fun onClick(view: View) {
          customOppiaTagActionListener?.onConceptCardLinkClicked(view, skillId)
        }
      }, openIndex, closeIndex, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
    }
  }

  /** Listener that's called when a custom tag triggers an event. */
  interface CustomOppiaTagActionListener {
    /**
     * Called when an embedded concept card link is clicked in the specified view with the skillId corresponding to the
     * card that should be shown.
     */
    fun onConceptCardLinkClicked(view: View, skillId: String)
  }

  /** Factory for creating new [HtmlParser]s. */
  class Factory @Inject constructor(private val urlImageParserFactory: UrlImageParser.Factory) {
    /**
     * Returns a new [HtmlParser] with the specified entity type and ID for loading images, and an optionally specified
     * [CustomOppiaTagActionListener] for handling custom Oppia tag events.
     */
    fun create(
      entityType: String, entityId: String, customOppiaTagActionListener: CustomOppiaTagActionListener? = null
    ): HtmlParser {
      return HtmlParser(urlImageParserFactory, entityType, entityId, customOppiaTagActionListener)
    }
  }

  private fun trimSpannable(spannable: SpannableStringBuilder): SpannableStringBuilder {
    var trimStart = 0
    var trimEnd = 0

    var text = spannable.toString()

    while (text.isNotEmpty() && text.startsWith("\n")) {
      text = text.substring(1)
      trimStart += 1
    }

    while (text.length > 0 && text.endsWith("\n")) {
      text = text.substring(0, text.length - 1)
      trimEnd += 1
    }

    return spannable.delete(0, trimStart).delete(spannable.length - trimEnd, spannable.length)
  }
}
