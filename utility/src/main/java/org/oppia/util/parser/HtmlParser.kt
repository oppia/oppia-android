package org.oppia.util.parser

import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.BulletSpan
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
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
  private val imageCenterAlign: Boolean,
  customOppiaTagActionListener: CustomOppiaTagActionListener?
) {

  private val conceptCardTagHandler = ConceptCardTagHandler(customOppiaTagActionListener)

  /**
   * This method replaces custom Oppia tags with Android-compatible versions for a given raw HTML string, and returns the HTML [Spannable].
   * @param rawString rawString argument is the string from the string-content
   * @param htmlContentTextView htmlContentTextView argument is the TextView, that need to be passed as argument to ImageGetter class for image parsing
   * @return Spannable Spannable represents the styled text.
   */
  fun parseOppiaHtml(rawString: String, htmlContentTextView: TextView, supportsLinks: Boolean = false): Spannable {
    var htmlContent = rawString
    if (htmlContent.contains("\n\t")) {
      htmlContent = htmlContent.replace("\n\t", "")
    }
    if (htmlContent.contains("\n\n")) {
      htmlContent = htmlContent.replace("\n\n", "")
    }

    if (htmlContent.contains(CUSTOM_IMG_TAG)) {
      htmlContent = htmlContent.replace(CUSTOM_IMG_TAG, REPLACE_IMG_TAG)
      htmlContent = htmlContent.replace(CUSTOM_IMG_FILE_PATH_ATTRIBUTE, REPLACE_IMG_FILE_PATH_ATTRIBUTE)
      htmlContent = htmlContent.replace("&amp;quot;", "")
    }

    // https://stackoverflow.com/a/8662457
    if (supportsLinks) {
      htmlContentTextView.movementMethod = LinkMovementMethod.getInstance()
    }

    val imageGetter = urlImageParserFactory.create(htmlContentTextView, entityType, entityId, imageCenterAlign)

    val htmlSpannable = CustomHtmlContentHandler.fromHtml(htmlContent,imageGetter, mapOf(CUSTOM_CONCEPT_CARD_TAG to conceptCardTagHandler)) as Spannable

    val spannableBuilder = SpannableStringBuilder(htmlSpannable)
    val bulletSpans = spannableBuilder.getSpans(0, spannableBuilder.length, BulletSpan::class.java)
    bulletSpans.forEach {
      val start = spannableBuilder.getSpanStart(it)
      val end = spannableBuilder.getSpanEnd(it)
      spannableBuilder.removeSpan(it)
      spannableBuilder.setSpan(
        CustomBulletSpan(htmlContentTextView.context),
        start,
        end,
        Spanned.SPAN_INCLUSIVE_EXCLUSIVE
      )
    }
    return trimSpannable(spannableBuilder)
  }

  private fun trimSpannable(spannable: SpannableStringBuilder): SpannableStringBuilder {
    var trimStart = 0
    var trimEnd = 0

    var text = spannable.toString()

    if (text.startsWith("\n")) {
      text = text.substring(1)
      trimStart += 1
    }

    if (text.endsWith("\n")) {
      text = text.substring(0, text.length - 1)
      trimEnd += 2
    }

    return spannable.delete(0, trimStart).delete(spannable.length - trimEnd, spannable.length)
  }

  // https://mohammedlakkadshaw.com/blog/handling-custom-tags-in-android-using-html-taghandler.html/
  private class ConceptCardTagHandler(
    private val customOppiaTagActionListener: CustomOppiaTagActionListener?
  ): CustomHtmlContentHandler.CustomTagHandler {
    override fun handleTag(attributes: org.xml.sax.Attributes, openIndex: Int, closeIndex: Int, output: Editable) {
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

  class Factory @Inject constructor(private val urlImageParserFactory: UrlImageParser.Factory) {
    fun create(entityType: String, entityId: String, imageCenterAlign: Boolean, customOppiaTagActionListener: CustomOppiaTagActionListener? = null): HtmlParser {
      return HtmlParser(urlImageParserFactory, entityType, entityId, imageCenterAlign, customOppiaTagActionListener)
    }
  }
}
