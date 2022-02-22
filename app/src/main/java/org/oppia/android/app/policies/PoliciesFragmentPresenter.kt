package org.oppia.android.app.policies

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.PoliciesArguments
import org.oppia.android.app.model.PoliciesArguments.PolicyPage
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.databinding.PoliciesFragmentBinding
import org.oppia.android.util.parser.html.HtmlParser
import javax.inject.Inject

/** The presenter for [PoliciesFragment]. */
@FragmentScope
class PoliciesFragmentPresenter @Inject constructor(
  private val htmlParserFactory: HtmlParser.Factory,
  private val resourceHandler: AppLanguageResourceHandler
) {

  /** Handles onCreate() method of the [PoliciesFragment]. */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    policiesArguments: PoliciesArguments
  ): View {
    val binding = PoliciesFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    setUpContentForTextViews(policiesArguments.policyPage, binding)

    return binding.root
  }

  private fun setUpContentForTextViews(
    policyPage: PolicyPage,
    binding: PoliciesFragmentBinding
  ) {
    var policiesDescription = ""
    var policiesWebLink = ""

    if (policyPage == PolicyPage.PRIVACY_POLICY) {
      policiesDescription =
        resourceHandler.getStringInLocale(R.string.privacy_policy_content)
      policiesWebLink = resourceHandler.getStringInLocale(R.string.privacy_policy_web_link)
    } else if (policyPage == PolicyPage.TERMS_OF_SERVICE) {
      policiesDescription =
        resourceHandler.getStringInLocale(R.string.terms_of_service_content)
      policiesWebLink = resourceHandler.getStringInLocale(R.string.terms_of_service_web_link)
    }

    binding.policiesDescriptionTextView.text = htmlParserFactory.create().parseOppiaHtml(
      policiesDescription,
      binding.policiesDescriptionTextView,
      supportsLinks = true,
      supportsConceptCards = false
    )

    binding.policiesWebLinkTextView.text = htmlParserFactory.create().parseOppiaHtml(
      policiesWebLink,
      binding.policiesWebLinkTextView,
      supportsLinks = true,
      supportsConceptCards = false
    )
  }
}
