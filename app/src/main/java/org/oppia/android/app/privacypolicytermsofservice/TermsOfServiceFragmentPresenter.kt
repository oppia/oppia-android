package org.oppia.android.app.privacypolicytermsofservice

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.databinding.TermsOfServiceFragmentBinding
import org.oppia.android.util.gcsresource.DefaultResourceBucketName
import org.oppia.android.util.parser.html.HtmlParser
import javax.inject.Inject

/** The presenter for [PrivacyPolicyFragment]. */
@FragmentScope
class TermsOfServiceFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val htmlParserFactory: HtmlParser.Factory,
  private val resourceHandler: AppLanguageResourceHandler,
  @DefaultResourceBucketName private val resourceBucketName: String
) {
  private lateinit var binding: TermsOfServiceFragmentBinding
  private lateinit var privacyPolicyDescription: String
  private lateinit var privacyPolicyWebLink: String

  /** Handles onCreate() method of the [TermsOfServiceFragment]. */
  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View {
    binding = TermsOfServiceFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    setUpContentForTextViews()

    return binding.root
  }

  private fun setUpContentForTextViews() {
    // NOTE: Here entityType and entityId can be anything as it will actually not get used.
    // They are needed only for cases where rich-text contains images from server and in PrivacyPolicy
    // we do not have images.
    privacyPolicyDescription = resourceHandler.getStringInLocale(R.string.privacy_policy_content)

    binding.termsOfServiceDescriptionTextView.text = htmlParserFactory.create(
      resourceBucketName,
      entityType = "PrivacyPolicy",
      entityId = "oppia",
      imageCenterAlign = false
    ).parseOppiaHtml(
      privacyPolicyDescription,
      binding.termsOfServiceDescriptionTextView
    )

    privacyPolicyWebLink = resourceHandler.getStringInLocale(R.string.privacy_policy_web_link)

    binding.termsOfServiceWebLinkTextView.text = htmlParserFactory.create(
      resourceBucketName,
      entityType = "PrivacyPolicy",
      entityId = "oppia",
      imageCenterAlign = false
    ).parseOppiaHtml(
      privacyPolicyWebLink,
      binding.termsOfServiceWebLinkTextView
    )
  }
}
