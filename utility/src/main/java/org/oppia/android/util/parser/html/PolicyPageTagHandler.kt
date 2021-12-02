package org.oppia.android.util.parser.html

import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ClickableSpan
import android.view.View
import org.oppia.android.app.model.PolicyPage
import org.oppia.android.util.logging.ConsoleLogger
import org.xml.sax.Attributes

/** The custom tag corresponding to [PolicyPageTagHandler]. */
const val CUSTOM_POLICY_PAGE_TAG = "oppia-noninteractive-policy"

/**
 * A custom tag handler for supporting custom Oppia policies page parsed with
 * [CustomHtmlContentHandler].
 */
class PolicyPageTagHandler(
  private val listener: PolicyPageLinkClickListener,
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
    val termsOfServiceLink = attributes.getJsonStringValue("terms-of-service-link")
    val privacyPolicyLink = attributes.getJsonStringValue("privacy-policy-link")

    when {
      termsOfServiceLink != null -> {
        clickableSpanBuilder(
          termsOfServiceLink,
          output,
          openIndex,
          closeIndex,
          PolicyPage.TERMS_OF_SERVICE
        )
      }
      privacyPolicyLink != null -> {
        clickableSpanBuilder(
          privacyPolicyLink,
          output,
          openIndex,
          closeIndex,
          PolicyPage.PRIVACY_POLICY
        )
      }
      else -> consoleLogger.e("PolicyPageTagHandler", "Failed to parse policy page tag")
    }
  }

  private fun clickableSpanBuilder(
    policyLink: String,
    output: Editable,
    openIndex: Int,
    closeIndex: Int,
    policyPage: PolicyPage
  ) {
    val spannableBuilder = SpannableStringBuilder(policyLink)
    spannableBuilder.setSpan(
      object : ClickableSpan() {
        override fun onClick(view: View) {
          listener.onPolicyPageLinkClicked(policyPage)
        }
      },
      /* start= */ 0, /* end= */ policyLink.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE
    )
    output.replace(openIndex, closeIndex, spannableBuilder)
  }

  /** Listener called when policy page links are clicked. */
  interface PolicyPageLinkClickListener {
    /**
     * Called when a policy page link is called in the specified view corresponding to the
     * specified policy link.
     */
    fun onPolicyPageLinkClicked(policyPage: PolicyPage)
  }
}
