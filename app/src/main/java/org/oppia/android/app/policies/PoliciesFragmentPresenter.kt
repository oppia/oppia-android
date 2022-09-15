package org.oppia.android.app.policies

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
): HtmlParser.PolicyOppiaTagActionListener {

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
      policyDescription =
        resourceHandler.getStringInLocale(R.string.privacy_policy_content)
      policyWebLink = resourceHandler.getStringInLocale(R.string.privacy_policy_web_link)
    } else if (policyPage == PolicyPage.TERMS_OF_SERVICE) {
      policyDescription =
        resourceHandler.getStringInLocale(R.string.terms_of_service_content)
      policyWebLink = resourceHandler.getStringInLocale(R.string.terms_of_service_web_link)
    }

    binding.policyDescriptionTextView.text = htmlParserFactory.create(
      policyOppiaTagActionListener = this,
      displayLocale = resourceHandler.getDisplayLocale()
    ).parseOppiaHtml(
      policyDescription,
      binding.policyDescriptionTextView,
      supportsLinks = true,
      supportsConceptCards = false
    )

    binding.policyWebLinkTextView.text = htmlParserFactory.create(
      gcsResourceName = "",
      entityType = "",
      entityId = "",
      imageCenterAlign = false,
      customOppiaTagActionListener = null,
      resourceHandler.getDisplayLocale()
    ).parseOppiaHtml(
      policyWebLink,
      binding.policyWebLinkTextView,
      supportsLinks = true,
      supportsConceptCards = false
    )
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
