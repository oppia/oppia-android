package org.oppia.android.util.parser

import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.BulletSpan
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import org.xml.sax.Attributes
import javax.inject.Inject

private const val CUSTOM_IMG_TAG = "oppia-noninteractive-image"
private const val REPLACE_IMG_TAG = "img"
private const val CUSTOM_IMG_FILE_PATH_ATTRIBUTE = "filepath-with-value"
private const val REPLACE_IMG_FILE_PATH_ATTRIBUTE = "src"

private const val CUSTOM_CONCEPT_CARD_TAG = "oppia-noninteractive-skillreview"

/** Html Parser to parse custom Oppia tags with Android-compatible versions. */
class HtmlParser private constructor(
  private val urlImageParserFactory: UrlImageParser.Factory,
  private val gcsResourceName: String,
  private val entityType: String,
  private val entityId: String,
  private val imageCenterAlign: Boolean,
  customOppiaTagActionListener: CustomOppiaTagActionListener?
) {
  private val conceptCardTagHandler = ConceptCardTagHandler(customOppiaTagActionListener)
  private val bulletTagHandler = BulletTagHandler()

  /**
   * Parses a raw HTML string with support for custom Oppia tags.
   *
   * @param rawString raw HTML to parse
   * @param htmlContentTextView the [TextView] that will contain the returned [Spannable]
   * @param supportsLinks whether the provided [TextView] should support link forwarding (it's
   *     recommended not to use this for [TextView]s that are within other layouts that need to
   *     support clicking (default false)
   * @return a [Spannable] representing the styled text.
   */
  fun parseOppiaHtml(
    rawString: String,
    htmlContentTextView: TextView,
    supportsLinks: Boolean = false,
    supportsConceptCards: Boolean = false
  ): Spannable {
    var htmlContent = rawString
    if ("\n\t" in htmlContent) {
      htmlContent = htmlContent.replace("\n\t", "")
    }
    if ("\n\n" in htmlContent) {
      htmlContent = htmlContent.replace("\n\n", "")
    }
    if ("<li>" in htmlContent) {
      htmlContent = htmlContent.replace("<li>", "<$CUSTOM_BULLET_LIST_TAG>")
        .replace("</li>", "</$CUSTOM_BULLET_LIST_TAG>")
    }

    if (CUSTOM_IMG_TAG in htmlContent) {
      htmlContent = htmlContent.replace(CUSTOM_IMG_TAG, REPLACE_IMG_TAG)
      htmlContent =
        htmlContent.replace(CUSTOM_IMG_FILE_PATH_ATTRIBUTE, REPLACE_IMG_FILE_PATH_ATTRIBUTE)
      htmlContent = htmlContent.replace("&amp;quot;", "")
    }

    // https://stackoverflow.com/a/8662457
    if (supportsLinks) {
      htmlContentTextView.movementMethod = LinkMovementMethod.getInstance()
    }

    val imageGetter = urlImageParserFactory.create(
      htmlContentTextView, gcsResourceName, entityType, entityId, imageCenterAlign
    )
    val htmlSpannable = CustomHtmlContentHandler.fromHtml(
      htmlContent, imageGetter, computeCustomTagHandlers(supportsConceptCards)
    )

    val spannableBuilder = SpannableStringBuilder(htmlSpannable)
    val bulletSpans = spannableBuilder.getSpans(
      /* queryStart= */ 0,
      spannableBuilder.length,
      BulletSpan::class.java
    )
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

    return ensureNonEmpty(trimSpannable(spannableBuilder))
  }

  private fun computeCustomTagHandlers(
    supportsConceptCards: Boolean
  ): Map<String, CustomHtmlContentHandler.CustomTagHandler> {
    val handlersMap = mutableMapOf<String, CustomHtmlContentHandler.CustomTagHandler>()
    handlersMap[CUSTOM_BULLET_LIST_TAG] = bulletTagHandler
    if (supportsConceptCards) {
      handlersMap[CUSTOM_CONCEPT_CARD_TAG] = conceptCardTagHandler
    }
    return handlersMap
  }

  // https://mohammedlakkadshaw.com/blog/handling-custom-tags-in-android-using-html-taghandler.html/
  private class ConceptCardTagHandler(
    private val customOppiaTagActionListener: CustomOppiaTagActionListener?
  ) : CustomHtmlContentHandler.CustomTagHandler {
    override fun handleTag(
      attributes: Attributes,
      openIndex: Int,
      closeIndex: Int,
      output: Editable
    ) {
      // Replace the custom tag with a clickable piece of text based on the tag's customizations.
      val skillId = attributes.getValue("skill_id-with-value")
      val text = attributes.getValue("text-with-value")
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
    }
  }

  private fun trimSpannable(spannable: SpannableStringBuilder): SpannableStringBuilder {
    val trimmedText = spannable.toString()
    val trimStart = if (trimmedText.startsWith("\n")) 1 else 0
    val trimEnd = if (trimmedText.length > 1 && trimmedText.endsWith("\n")) 2 else 0
    return spannable.delete(0, trimStart).delete(spannable.length - trimEnd, spannable.length)
  }

  private fun ensureNonEmpty(spannable: SpannableStringBuilder): SpannableStringBuilder {
    // Per AOSP, ImageSpans are prefixed by a control character. If the string only contains this
    // control character and no other text, the ImageSpan isn't actually considered in the
    // dimensions of the image. This is likely a bug in AOSP. One hacky workaround is to add
    // whitespace around the drawable to give Android something to "draw" (or at least measure to
    // ensure the image's dimensions are measured). Note that this needs to be a visible character
    // to remedy the bug.
    // TODO(#1796): Find a better workaround for this bug.
    return if (spannable.toString().all { it == '\uFFFC' }) {
      spannable.insert(/* where= */ 0, " ").append(" ")
    } else spannable
  }

  /** Listener that's called when a custom tag triggers an event. */
  interface CustomOppiaTagActionListener {
    /**
     * Called when an embedded concept card link is clicked in the specified view with the skillId
     * corresponding to the card that should be shown.
     */
    fun onConceptCardLinkClicked(view: View, skillId: String)
  }

  /** Factory for creating new [HtmlParser]s. */
  class Factory @Inject constructor(private val urlImageParserFactory: UrlImageParser.Factory) {
    /**
     * Returns a new [HtmlParser] with the specified entity type and ID for loading images, and an
     * optionally specified [CustomOppiaTagActionListener] for handling custom Oppia tag events.
     */
    fun create(
      gcsResourceName: String,
      entityType: String,
      entityId: String,
      imageCenterAlign: Boolean,
      customOppiaTagActionListener: CustomOppiaTagActionListener? = null
    ): HtmlParser {
      return HtmlParser(
        urlImageParserFactory,
        gcsResourceName,
        entityType,
        entityId,
        imageCenterAlign,
        customOppiaTagActionListener
      )
    }
  }
}
