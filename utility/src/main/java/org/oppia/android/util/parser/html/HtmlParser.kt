package org.oppia.android.util.parser.html

import android.app.Application
import android.content.Context
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.text.util.Linkify
import android.util.Patterns
import android.view.View
import android.widget.TextView
import androidx.core.text.util.LinkifyCompat
import androidx.core.view.ViewCompat
import org.oppia.android.util.locale.OppiaLocale
import org.oppia.android.util.logging.ConsoleLogger
import org.oppia.android.util.parser.image.UrlImageParser
import org.oppia.android.util.platformparameter.CacheLatexRendering
import org.oppia.android.util.platformparameter.PlatformParameterValue
import javax.inject.Inject

/** Html Parser to parse custom Oppia tags with Android-compatible versions. */
class HtmlParser private constructor(
  private val context: Context,
  private val urlImageParserFactory: UrlImageParser.Factory?,
  private val gcsResourceName: String,
  private val entityType: String,
  private val entityId: String,
  private val imageCenterAlign: Boolean,
  private val consoleLogger: ConsoleLogger,
  private val cacheLatexRendering: Boolean,
  customOppiaTagActionListener: CustomOppiaTagActionListener?,
  policyOppiaTagActionListener: PolicyOppiaTagActionListener?,
  displayLocale: OppiaLocale.DisplayLocale
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
        override fun onPolicyPageLinkClicked(policyType: PolicyType) {
          policyOppiaTagActionListener?.onPolicyPageLinkClicked(policyType)
        }
      },
      consoleLogger
    )
  }
  private val bulletTagHandler by lazy { LiTagHandler(context, displayLocale) }
  private val imageTagHandler by lazy { ImageTagHandler(consoleLogger) }

  private val isRtl by lazy {
    displayLocale.getLayoutDirection() == ViewCompat.LAYOUT_DIRECTION_RTL
  }

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
    val regex = Regex("""<oppia-noninteractive-image [^>]*>.*?</oppia-noninteractive-image>""")

    // Canvas does not support RTL, it always starts from left to right in RTL due to which compound drawables are
    // not center aligned. To avoid this situation check if RTL is enabled and set the textDirection.
    if (isRtl) {
      htmlContentTextView.textDirection = View.TEXT_DIRECTION_RTL

      val modifiedHtmlContent = rawString.replace(regex) {
        val oppiaImageTag = it.value
        """<div style="text-align: center;">$oppiaImageTag</div>"""
      }
      htmlContent = modifiedHtmlContent
    } else {
      htmlContentTextView.textDirection = View.TEXT_DIRECTION_LTR

      // Images are wrapped inside a <div> tag, because the <div> tag is a block-level element,
      // so that all images display in block mode and on a new line."
      val modifiedHtmlContent = rawString.replace(regex) {
        val oppiaImageTag = it.value
        """<div>$oppiaImageTag</div>"""
      }
      htmlContent = modifiedHtmlContent
    }

    htmlContentTextView.invalidate()

    if ("\n\t" in htmlContent) {
      htmlContent = htmlContent.replace("\n\t", "")
    }
    if ("\n\n" in htmlContent) {
      htmlContent = htmlContent.replace("\n\n", "")
    }
    if ("<li>" in htmlContent) {
      htmlContent = htmlContent.replace("<li>", "<$CUSTOM_LIST_LI_TAG>")
        .replace("</li>", "</$CUSTOM_LIST_LI_TAG>")
    }
    if ("<ul>" in htmlContent) {
      htmlContent = htmlContent.replace("<ul>", "<$CUSTOM_LIST_UL_TAG>")
        .replace("</ul>", "</$CUSTOM_LIST_UL_TAG>")
    }
    if ("<ol>" in htmlContent) {
      htmlContent = htmlContent.replace("<ol>", "<$CUSTOM_LIST_OL_TAG>")
        .replace("</ol>", "</$CUSTOM_LIST_OL_TAG>")
    }

    // https://stackoverflow.com/a/8662457
    if (supportsLinks) {
      htmlContentTextView.movementMethod = LinkMovementMethod.getInstance()
      LinkifyCompat.addLinks(htmlContentTextView, Linkify.WEB_URLS)
    }

    val imageGetter = urlImageParserFactory?.create(
      htmlContentTextView,
      gcsResourceName,
      entityType,
      entityId,
      imageCenterAlign
    )

    val htmlSpannable = CustomHtmlContentHandler.fromHtml(
      htmlContent,
      imageGetter,
      computeCustomTagHandlers(supportsConceptCards, htmlContentTextView)
    )

    val urlPattern = Patterns.WEB_URL
    val matcher = urlPattern.matcher(htmlSpannable)
    while (matcher.find()) {
      val start = matcher.start()
      val end = matcher.end()
      val url = htmlSpannable.subSequence(start, end).toString()
      val urlSpan = URLSpan(url)
      htmlSpannable.setSpan(urlSpan, start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
    }

    return ensureNonEmpty(trimSpannable(htmlSpannable as SpannableStringBuilder))
  }

  private fun computeCustomTagHandlers(
    supportsConceptCards: Boolean,
    htmlContentTextView: TextView
  ): Map<String, CustomHtmlContentHandler.CustomTagHandler> {
    val handlersMap = mutableMapOf<String, CustomHtmlContentHandler.CustomTagHandler>()
    handlersMap[CUSTOM_LIST_LI_TAG] = bulletTagHandler
    handlersMap[CUSTOM_LIST_UL_TAG] = bulletTagHandler
    handlersMap[CUSTOM_LIST_OL_TAG] = bulletTagHandler
    handlersMap[CUSTOM_IMG_TAG] = imageTagHandler
    handlersMap[CUSTOM_MATH_TAG] =
      MathTagHandler(
        consoleLogger,
        context.assets,
        htmlContentTextView.lineHeight.toFloat(),
        cacheLatexRendering,
        context as? Application ?: context.applicationContext as Application
      )
    if (supportsConceptCards) {
      handlersMap[CUSTOM_CONCEPT_CARD_TAG] = conceptCardTagHandler
    }
    handlersMap[CUSTOM_POLICY_PAGE_TAG] = policyPageTagHandler
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
     * Called when a policy page link is clicked that corresponds to the page that should be shown.
     */
    fun onPolicyPageLinkClicked(policyType: PolicyType)
  }

  /** Factory for creating new [HtmlParser]s. */
  class Factory @Inject constructor(
    private val urlImageParserFactory: UrlImageParser.Factory,
    private val consoleLogger: ConsoleLogger,
    private val context: Context,
    @CacheLatexRendering private val enableCacheLatexRendering: PlatformParameterValue<Boolean>
  ) {
    /**
     * Returns a new [HtmlParser] with the specified entity type and ID for loading images, and an
     * optionally specified [CustomOppiaTagActionListener] and [PolicyOppiaTagActionListener] for
     * handling custom Oppia tag events.
     */
    fun create(
      gcsResourceName: String,
      entityType: String,
      entityId: String,
      imageCenterAlign: Boolean,
      customOppiaTagActionListener: CustomOppiaTagActionListener? = null,
      displayLocale: OppiaLocale.DisplayLocale
    ): HtmlParser {
      return HtmlParser(
        context = context,
        urlImageParserFactory = urlImageParserFactory,
        gcsResourceName = gcsResourceName,
        entityType = entityType,
        entityId = entityId,
        imageCenterAlign = imageCenterAlign,
        consoleLogger = consoleLogger,
        cacheLatexRendering = enableCacheLatexRendering.value,
        customOppiaTagActionListener = customOppiaTagActionListener,
        policyOppiaTagActionListener = null,
        displayLocale = displayLocale
      )
    }

    /**
     * Returns a new [HtmlParser] with the empty entity type and ID for loading images,
     * doesn't require GCS properties and imageCenterAlign set to false
     * optionally specified [CustomOppiaTagActionListener] for handling custom Oppia tag events.
     */
    fun create(
      displayLocale: OppiaLocale.DisplayLocale
    ): HtmlParser {
      return HtmlParser(
        context = context,
        urlImageParserFactory = urlImageParserFactory,
        gcsResourceName = "",
        entityType = "",
        entityId = "",
        imageCenterAlign = false,
        consoleLogger = consoleLogger,
        cacheLatexRendering = enableCacheLatexRendering.value,
        customOppiaTagActionListener = null,
        policyOppiaTagActionListener = null,
        displayLocale = displayLocale
      )
    }

    /**
     * Returns a new [HtmlParser] with an optionally specified [CustomOppiaTagActionListener] and
     * [PolicyOppiaTagActionListener] for handling custom Oppia tag events. Note that Oppia image
     * loading is specifically not supported (see the other [create] method if image support is
     * needed).
     */
    fun create(
      policyOppiaTagActionListener: PolicyOppiaTagActionListener? = null,
      displayLocale: OppiaLocale.DisplayLocale
    ): HtmlParser {
      return HtmlParser(
        context = context,
        urlImageParserFactory = null,
        gcsResourceName = "",
        entityType = "",
        entityId = "",
        imageCenterAlign = false,
        consoleLogger = consoleLogger,
        cacheLatexRendering = false,
        customOppiaTagActionListener = null,
        policyOppiaTagActionListener = policyOppiaTagActionListener,
        displayLocale = displayLocale
      )
    }
  }
}
