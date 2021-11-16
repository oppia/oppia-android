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
const val CUSTOM_PRIVACY_POLICY_PAGE_TAG = "link-to-privacy-policy"
const val CUSTOM_TERMS_OF_SERVICE_PAGE_TAG = "link-to-terms-of-service"

// https://mohammedlakkadshaw.com/blog/handling-custom-tags-in-android-using-html-taghandler.html/
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
    val spannableBuilder = SpannableStringBuilder("Privacy Policy")
      spannableBuilder.setSpan(
        object : ClickableSpan() {
          override fun onClick(view: View) {
            consoleLogger.e("PolicyPageTagHandler", "Clicked")

            listener.onPolicyPageLinkClicked(PolicyPage.PRIVACY_POLICY)
          }
        },
        /* start= */ 0, /* end= */ "Privacy Policy".length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE
      )
      output.replace(openIndex, closeIndex, spannableBuilder)
     consoleLogger.e("PolicyPageTagHandler", "Failed to parse concept card tag"+ PolicyPage.PRIVACY_POLICY.name)
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
