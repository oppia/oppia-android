package org.oppia.android.util.parser.html

import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ClickableSpan
import android.view.View
import org.oppia.android.util.logging.ConsoleLogger
import org.xml.sax.Attributes

/** The custom tag corresponding to [PolicyPageTagHandler]. */
const val CUSTOM_POLICY_PAGE_TAG = "oppia-noninteractive-policy"
private const val PRIVACY_POLICY_PAGE = "privacy"
private const val TERMS_OF_SERVICE_PAGE = "tos"
private const val PRIVACY_POLICY = "Privacy Policy"
private const val TERMS_OF_SERVICE = "Terms of Service"

/**
 * A custom tag handler for supporting custom Oppia policies page parsed with
 * [CustomHtmlContentHandler].
 */
class PolicyPageTagHandler(
  private val listener: PolicyPageLinkClickListener,
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
    val text = attributes.getJsonStringValue("link")
    if (text != null) {
      when (text) {
        TERMS_OF_SERVICE_PAGE -> {
          addPolicyPageClickableSpan(
            TERMS_OF_SERVICE,
            output,
            openIndex,
            closeIndex,
            PolicyType.TERMS_OF_SERVICE
          )
        }
        PRIVACY_POLICY_PAGE -> {
          addPolicyPageClickableSpan(
            PRIVACY_POLICY,
            output,
            openIndex,
            closeIndex,
            PolicyType.PRIVACY_POLICY
          )
        }
      }
    } else consoleLogger.e("PolicyPageTagHandler", "Failed to parse policy page tag")
  }

  private fun addPolicyPageClickableSpan(
    policyLink: String,
    output: Editable,
    openIndex: Int,
    closeIndex: Int,
    policyType: PolicyType
  ) {
    val spannableBuilder = SpannableStringBuilder(policyLink)
    spannableBuilder.setSpan(
      object : ClickableSpan() {
        override fun onClick(view: View) {
          listener.onPolicyPageLinkClicked(policyType)
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
    fun onPolicyPageLinkClicked(policyType: PolicyType)
  }

  override fun getContentDescription(attributes: Attributes): String {
    return when (attributes.getJsonStringValue("link")) {
      TERMS_OF_SERVICE_PAGE, PRIVACY_POLICY_PAGE -> "Link to "
      else -> ""
    }
  }
}
