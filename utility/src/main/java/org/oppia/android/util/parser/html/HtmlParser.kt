package org.oppia.android.util.parser.html

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.view.View
import android.widget.TextView
import androidx.core.view.ViewCompat
import org.oppia.android.app.model.PolicyPage
import org.oppia.android.util.logging.ConsoleLogger
import org.oppia.android.util.parser.image.UrlImageParser
import javax.inject.Inject

/** Html Parser to parse custom Oppia tags with Android-compatible versions. */
class HtmlParser private constructor(
  private val urlImageParserFactory: UrlImageParser.Factory,
  private val gcsResourceName: String,
  private val entityType: String,
  private val entityId: String,
  private val imageCenterAlign: Boolean,
  private val consoleLogger: ConsoleLogger,
  customOppiaTagActionListener: CustomOppiaTagActionListener?,
  policyOppiaTagActionListener: PolicyOppiaTagActionListener?
) {
  private val conceptCardTagHandler by lazy {
    ConceptCardTagHandler(
      object : ConceptCardTagHandler.ConceptCardLinkClickListener {
        override fun onConceptCardLinkClicked(view: View, skillId: String) {
          customOppiaTagActionListener?.onConceptCardLinkClicked(view, skillId)
        }
      },
      consoleLogger
    )
  }

  private val policyPageTagHandler by lazy {
    PolicyPageTagHandler(
      object : PolicyPageTagHandler.PolicyPageLinkClickListener {
        override fun onPolicyPageLinkClicked(policyPage: PolicyPage) {
          policyOppiaTagActionListener?.onPolicyPageLinkClicked(policyPage)
        }
      },
      consoleLogger
    )
  }
  private val bulletTagHandler by lazy { BulletTagHandler() }
  private val imageTagHandler by lazy { ImageTagHandler(consoleLogger) }
  private val mathTagHandler by lazy { MathTagHandler(consoleLogger) }

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

    // Canvas does not support RTL, it always starts from left to right in RTL due to which compound drawables are
    // not center aligned. To avoid this situation check if RTL is enabled and set the textDirection.
    when (getLayoutDirection(htmlContentTextView)) {
      ViewCompat.LAYOUT_DIRECTION_RTL -> {
        htmlContentTextView.textDirection = View.TEXT_DIRECTION_ANY_RTL
      }
      ViewCompat.LAYOUT_DIRECTION_LTR -> {
        htmlContentTextView.textDirection = View.TEXT_DIRECTION_LTR
      }
    }
    htmlContentTextView.invalidate()

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

    htmlContentTextView.handleUrlClicks {}

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

    val spannableBuilder = CustomBulletSpan.replaceBulletSpan(
      SpannableStringBuilder(htmlSpannable),
      htmlContentTextView.context
    )
    return ensureNonEmpty(trimSpannable(spannableBuilder))
  }

  private fun getLayoutDirection(view: View): Int {
    return ViewCompat.getLayoutDirection(view)
  }

  /**
   * Searches for all URLSpans in current text replaces them with our own ClickableSpans
   * forwards clicks to provided function.
   */
  private fun TextView.handleUrlClicks(onClicked: ((String) -> Unit)? = null) {
    // create span builder and replaces current text with it
    text = SpannableStringBuilder.valueOf(text).apply {
      // search for all URL spans and replace all spans with our own clickable spans
      getSpans(0, length, URLSpan::class.java).forEach {
        // add new clickable span at the same position
        setSpan(
          object : ClickableSpan() {
            override fun onClick(widget: View) {
              onClicked?.invoke(it.url)
            }
          },
          getSpanStart(it),
          getSpanEnd(it),
          Spanned.SPAN_INCLUSIVE_EXCLUSIVE
        )
        // remove old URLSpan
        removeSpan(it)
      }
    }
    // make sure movement method is set
    movementMethod = LinkMovementMethod.getInstance()
  }

  private fun computeCustomTagHandlers(
    supportsConceptCards: Boolean
  ): Map<String, CustomHtmlContentHandler.CustomTagHandler> {
    val handlersMap = mutableMapOf<String, CustomHtmlContentHandler.CustomTagHandler>()
    handlersMap[CUSTOM_BULLET_LIST_TAG] = bulletTagHandler
    handlersMap[CUSTOM_IMG_TAG] = imageTagHandler
    handlersMap[CUSTOM_MATH_TAG] = mathTagHandler
    if (supportsConceptCards) {
      handlersMap[CUSTOM_CONCEPT_CARD_TAG] = conceptCardTagHandler
    }
    handlersMap[CUSTOM_TERMS_OF_SERVICE_PAGE_TAG] = policyPageTagHandler
    handlersMap[CUSTOM_PRIVACY_POLICY_PAGE_TAG] = policyPageTagHandler
    return handlersMap
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

  /** Listener that's called when a custom tag triggers an event. */
  interface PolicyOppiaTagActionListener {
    /**
     * Called when an policy page link is clicked that corresponds to the page that should be shown.
     */

    fun onPolicyPageLinkClicked(policyPage: PolicyPage)
  }

  /** Factory for creating new [HtmlParser]s. */
  class Factory @Inject constructor(
    private val urlImageParserFactory: UrlImageParser.Factory,
    private val consoleLogger: ConsoleLogger
  ) {
    /**
     * Returns a new [HtmlParser] with the specified entity type and ID for loading images, and an
     * optionally specified [CustomOppiaTagActionListener] for handling custom Oppia tag events.
     */
    fun create(
      gcsResourceName: String,
      entityType: String,
      entityId: String,
      imageCenterAlign: Boolean,
      customOppiaTagActionListener: CustomOppiaTagActionListener? = null,
      policyOppiaTagActionListener: PolicyOppiaTagActionListener? = null
    ): HtmlParser {
      return HtmlParser(
        urlImageParserFactory,
        gcsResourceName,
        entityType,
        entityId,
        imageCenterAlign,
        consoleLogger,
        customOppiaTagActionListener,
        policyOppiaTagActionListener
      )
    }
  }
}
