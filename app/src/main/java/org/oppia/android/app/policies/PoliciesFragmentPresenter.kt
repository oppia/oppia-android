package org.oppia.android.app.policies


import android.os.Build
import android.text.Html
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.PoliciesFragmentArguments
import org.oppia.android.app.model.PolicyPage
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.databinding.PoliciesFragmentBinding
import org.oppia.android.util.parser.html.HtmlParser
import org.oppia.android.util.parser.html.PolicyType
import javax.inject.Inject

/** The presenter for [PoliciesFragment]. */
@FragmentScope
class PoliciesFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val htmlParserFactory: HtmlParser.Factory,
  private val resourceHandler: AppLanguageResourceHandler
) : HtmlParser.PolicyOppiaTagActionListener {

  /** Handles onCreate() method of the [PoliciesFragment]. */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    policiesFragmentArguments: PoliciesFragmentArguments
  ): View {
    val binding = PoliciesFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    setUpContentForTextViews(policiesFragmentArguments.policyPage, binding)

    return binding.root
  }

  private fun setUpContentForTextViews(
    policyPage: PolicyPage,
    binding: PoliciesFragmentBinding
  ) {
    var policyDescription = ""
    var policyWebLink = ""
    if (policyPage == PolicyPage.PRIVACY_POLICY) {
      policyDescription = resourceHandler.getStringInLocale(R.string.privacy_policy_content)
      val spannedContent: Spanned = if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N){
        Html.fromHtml(policyDescription,Html.FROM_HTML_MODE_COMPACT)
      } else {
        @Suppress("DEPRECATION")
        Html.fromHtml(policyDescription)
      }
      binding.policyDescriptionTextView.text = spannedContent
      binding.policyDescriptionTextView.movementMethod = LinkMovementMethod.getInstance()

      policyWebLink = resourceHandler.getStringInLocale(R.string.privacy_policy_web_link)
      val policyWebLinkSpanned: Spanned = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
        Html.fromHtml(policyWebLink,Html.FROM_HTML_MODE_LEGACY)
      } else {
        @Suppress("DEPRECATION")
        Html.fromHtml(policyWebLink)
      }
      binding.policyWebLinkTextView.text = policyWebLinkSpanned
      binding.policyWebLinkTextView.movementMethod = LinkMovementMethod.getInstance()
    } else if (policyPage == PolicyPage.TERMS_OF_SERVICE) {
      policyDescription = resourceHandler.getStringInLocale(R.string.terms_of_service_content)
      val spannedContent: Spanned = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
        Html.fromHtml(policyDescription,Html.FROM_HTML_MODE_COMPACT)
      } else {
        @Suppress("DEPRECATION")
        Html.fromHtml(policyDescription)
      }
      binding.policyDescriptionTextView.text = spannedContent
      binding.policyDescriptionTextView.movementMethod = LinkMovementMethod.getInstance()

      policyWebLink = resourceHandler.getStringInLocale(R.string.terms_of_service_web_link)
      val policyWebLinkSpanned: Spanned = if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.N){
        Html.fromHtml(policyWebLink,Html.FROM_HTML_MODE_LEGACY)
      } else {
        @Suppress("DEPRECATION")
        Html.fromHtml(policyWebLink)
      }
      binding.policyWebLinkTextView.text = policyWebLinkSpanned
      binding.policyWebLinkTextView.movementMethod = LinkMovementMethod.getInstance()
    }
  }

  override fun onPolicyPageLinkClicked(policyType: PolicyType) {
    when (policyType) {
      PolicyType.PRIVACY_POLICY ->
        (activity as RouteToPoliciesListener).onRouteToPolicies(PolicyPage.PRIVACY_POLICY)
      PolicyType.TERMS_OF_SERVICE ->
        (activity as RouteToPoliciesListener).onRouteToPolicies(PolicyPage.TERMS_OF_SERVICE)
    }
  }
}
