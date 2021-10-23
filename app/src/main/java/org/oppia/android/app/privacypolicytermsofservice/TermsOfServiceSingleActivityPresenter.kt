package org.oppia.android.app.TermsOfServicetermsofservice

import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.privacypolicytermsofservice.TermsOfServiceSingleActivity
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.databinding.TermsOfServiceSingleActivityBinding
import org.oppia.android.util.gcsresource.DefaultResourceBucketName
import org.oppia.android.util.parser.html.HtmlParser
import javax.inject.Inject

/** The presenter for [TermsOfServiceSingleActivity]. */
@ActivityScope
class TermsOfServiceSingleActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val htmlParserFactory: HtmlParser.Factory,
  private val resourceHandler: AppLanguageResourceHandler,
  @DefaultResourceBucketName private val resourceBucketName: String
) {

  private lateinit var termsOfServiceSingleActivityToolbar: Toolbar
  private lateinit var termsOfServiceDescription: String
  private lateinit var termsOfServiceWebLink: String

  /** Handles onCreate() method of the [TermsOfServiceSingleActivity]. */
  fun handleOnCreate() {
    val binding = DataBindingUtil.setContentView<TermsOfServiceSingleActivityBinding>(
      activity,
      R.layout.terms_of_service_single_activity
    )
    binding.apply {
      lifecycleOwner = activity
    }

    termsOfServiceSingleActivityToolbar = binding.termsOfServiceSingleActivityToolbar
    activity.setSupportActionBar(termsOfServiceSingleActivityToolbar)
    activity.supportActionBar!!.title =
      resourceHandler.getStringInLocale(R.string.terms_of_service_activity_title)
    activity.supportActionBar!!.setDisplayShowHomeEnabled(true)
    activity.supportActionBar!!.setDisplayHomeAsUpEnabled(true)

    binding.termsOfServiceSingleActivityToolbar.setNavigationOnClickListener {
      (activity as TermsOfServiceSingleActivity).finish()
    }

    // NOTE: Here entityType and entityId can be anything as it will actually not get used.
    // They are needed only for cases where rich-text contains images from server and in TermsOfService
    // we do not have images.
    termsOfServiceDescription = resourceHandler.getStringInLocale(R.string.terms_of_service_content)
    val termsOfServiceDescriptionTextView =
      activity.findViewById<TextView>(R.id.terms_of_service_description_text_view)
    termsOfServiceDescriptionTextView.text = htmlParserFactory.create(
      resourceBucketName,
      entityType = "TermsOfService",
      entityId = "oppia",
      imageCenterAlign = false
    ).parseOppiaHtml(
      termsOfServiceDescription,
      termsOfServiceDescriptionTextView
    )

    val termsOfServiceWebLinkTextView =
      activity.findViewById<TextView>(R.id.terms_of_service_web_link_text_view)
    termsOfServiceWebLink = resourceHandler.getStringInLocale(R.string.terms_of_service_web_link)

    termsOfServiceWebLinkTextView.text = htmlParserFactory.create(
      resourceBucketName,
      entityType = "TermsOfService",
      entityId = "oppia",
      imageCenterAlign = false
    ).parseOppiaHtml(
      termsOfServiceWebLink,
      termsOfServiceWebLinkTextView
    )
  }
}

