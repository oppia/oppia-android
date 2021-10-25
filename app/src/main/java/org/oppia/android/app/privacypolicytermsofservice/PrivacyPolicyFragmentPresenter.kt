package org.oppia.android.app.privacypolicytermsofservice

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.databinding.PrivacyPolicyFragmentBinding
import org.oppia.android.util.gcsresource.DefaultResourceBucketName
import org.oppia.android.util.parser.html.HtmlParser
import javax.inject.Inject

/** The presenter for [PrivacyPolicyFragment]. */
@FragmentScope
class PrivacyPolicyFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val htmlParserFactory: HtmlParser.Factory,
  private val resourceHandler: AppLanguageResourceHandler,
  @DefaultResourceBucketName private val resourceBucketName: String
) {
  private lateinit var binding: PrivacyPolicyFragmentBinding
  private lateinit var privacyPolicyDescription: String
  private lateinit var privacyPolicyWebLink: String

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View {
    binding = PrivacyPolicyFragmentBinding.inflate(
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

    binding.privacyPolicyDescriptionTextView.text = htmlParserFactory.create(
      resourceBucketName,
      entityType = "PrivacyPolicy",
      entityId = "oppia",
      imageCenterAlign = false
    ).parseOppiaHtml(
      privacyPolicyDescription,
      binding.privacyPolicyDescriptionTextView
    )

    privacyPolicyWebLink = resourceHandler.getStringInLocale(R.string.privacy_policy_web_link)

    binding.privacyPolicyWebLinkTextView.text = htmlParserFactory.create(
      resourceBucketName,
      entityType = "PrivacyPolicy",
      entityId = "oppia",
      imageCenterAlign = false
    ).parseOppiaHtml(
      privacyPolicyWebLink,
      binding.privacyPolicyWebLinkTextView
    )

  }
}
