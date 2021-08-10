package org.oppia.android.util.parser.html

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.BulletSpan
import android.text.style.LeadingMarginSpan
import android.view.View
import android.widget.TextView
import androidx.core.view.ViewCompat
import org.oppia.android.util.R
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
  customOppiaTagActionListener: CustomOppiaTagActionListener?
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

    var htmlContent = rawString
    // Canvas does not support RTL, it always starts from left to right in RTL due to which compound drawables are
    // not center aligned. To avoid this situation check if RTL is enabled and set the textDirection.
    // First check, whether layout direction is resolved. If it is, you may work with the value.If layout
    // direction is not resolved, delay the check.
    if (ViewCompat.isLayoutDirectionResolved(htmlContentTextView)) {
     htmlContent = setTextDirection(htmlContentTextView, htmlContent)

    } else {
      htmlContentTextView.post {
        htmlContent =  setTextDirection(htmlContentTextView, htmlContent)
      }
    }
    htmlContentTextView.invalidate()


    if ("\n\t" in htmlContent) {
      htmlContent = htmlContent.replace("\n\t", "")
    }
    if ("\n\n" in htmlContent) {
      htmlContent = htmlContent.replace("\n\n", "")
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

    val spannableBuilder = setBulletSpan(
      SpannableStringBuilder(htmlSpannable),
      htmlContentTextView
    )

    return ensureNonEmpty(trimSpannable(spannableBuilder))
  }

  private fun setBulletSpan(
    spannableBuilder: SpannableStringBuilder,
    htmlContentTextView: TextView
  ): SpannableStringBuilder {
    val resources = htmlContentTextView.context.resources
    val bulletRadius = resources.getDimensionPixelSize(R.dimen.bullet_radius)
    val gapWidth = resources.getDimensionPixelSize(R.dimen.bullet_gap_width)
    // The space between the start of the line and the bullet.
    val spacingBeforeBullet = resources.getDimensionPixelSize(R.dimen.spacing_before_bullet)

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
        LeadingMarginSpan.Standard(spacingBeforeBullet), start, end,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
      )
      spannableBuilder.setSpan(
        BulletSpan(gapWidth, Color.BLACK, bulletRadius),
        start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
      )
    }
    return spannableBuilder
  }

  private fun setTextDirection(htmlContentTextView: TextView, htmlContent: String): String {
    when (getLayoutDirection(htmlContentTextView)) {
      ViewCompat.LAYOUT_DIRECTION_RTL -> {
        htmlContentTextView.textDirection = View.TEXT_DIRECTION_ANY_RTL
        if ("<li>" in htmlContent) {
         return  htmlContent .replace("<li>", "<$CUSTOM_BULLET_LIST_TAG dir=\"rtl\">")
            .replace("</li>", "</$CUSTOM_BULLET_LIST_TAG>")
        }
      }
      ViewCompat.LAYOUT_DIRECTION_LTR -> {
        htmlContentTextView.textDirection = View.TEXT_DIRECTION_LTR
        if ("<li>" in htmlContent) {
        return htmlContent.replace("<li>", "<$CUSTOM_BULLET_LIST_TAG>")
            .replace("</li>", "</$CUSTOM_BULLET_LIST_TAG>")
        }
      }
    }
    return htmlContent
  }

  private fun getLayoutDirection(view: View): Int {
    return ViewCompat.getLayoutDirection(view)
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
      customOppiaTagActionListener: CustomOppiaTagActionListener? = null
    ): HtmlParser {
      return HtmlParser(
        urlImageParserFactory,
        gcsResourceName,
        entityType,
        entityId,
        imageCenterAlign,
        consoleLogger,
        customOppiaTagActionListener
      )
    }
  }
}
