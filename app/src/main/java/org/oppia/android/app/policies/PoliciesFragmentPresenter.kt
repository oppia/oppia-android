package org.oppia.android.app.policies

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.PoliciesArguments
import org.oppia.android.app.model.PolicyPage
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.databinding.PoliciesFragmentBinding
import org.oppia.android.util.gcsresource.DefaultResourceBucketName
import org.oppia.android.util.parser.html.HtmlParser
import javax.inject.Inject

/** The presenter for [PoliciesFragment]. */
@FragmentScope
class PoliciesFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val htmlParserFactory: HtmlParser.Factory,
  private val resourceHandler: AppLanguageResourceHandler,
  @DefaultResourceBucketName private val resourceBucketName: String
) {
  private lateinit var binding: PoliciesFragmentBinding
  private lateinit var privacyPolicyDescription: String
  private lateinit var privacyPolicyWebLink: String

  /** Handles onCreate() method of the [PoliciesFragment]. */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    policiesArguments: PoliciesArguments
  ): View {
    binding = PoliciesFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    setUpContentForTextViews(policiesArguments.policyPage)

    return binding.root
  }

  private fun setUpContentForTextViews(policyPage: PolicyPage) {
    // NOTE: Here entityType and entityId can be anything as it will actually not get used.
    // They are needed only for cases where rich-text contains images from server and in PrivacyPolicy
    // we do not have images.
    when (policyPage) {
      PolicyPage.PRIVACY_POLICY -> {
        privacyPolicyDescription =
          resourceHandler.getStringInLocale(R.string.privacy_policy_content)
        privacyPolicyWebLink = resourceHandler.getStringInLocale(R.string.privacy_policy_web_link)
      }
      PolicyPage.TERMS_OF_SERVICE -> {
        privacyPolicyDescription =
          resourceHandler.getStringInLocale(R.string.terms_of_service_content)
        privacyPolicyWebLink = resourceHandler.getStringInLocale(R.string.terms_of_service_web_link)
      }
      else -> PolicyPage.POLICY_PAGE_UNSPECIFIED
    }

    binding.policiesDescriptionTextView.text = htmlParserFactory.create(
      resourceBucketName,
      entityType = "Policies",
      entityId = "oppia",
      imageCenterAlign = false
    ).parseOppiaHtml(
      privacyPolicyDescription,
      binding.policiesDescriptionTextView
    )

    binding.policiesWebLinkTextView.text = htmlParserFactory.create(
      resourceBucketName,
      entityType = "Policies",
      entityId = "oppia",
      imageCenterAlign = false
    ).parseOppiaHtml(
      privacyPolicyWebLink,
      binding.policiesWebLinkTextView
    )
  }
}
