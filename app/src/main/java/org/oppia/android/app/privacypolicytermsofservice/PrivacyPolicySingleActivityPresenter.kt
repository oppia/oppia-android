package org.oppia.android.app.privacypolicytermsofservice

import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.databinding.PrivacyPolicySingleActivityBinding
import org.oppia.android.util.gcsresource.DefaultResourceBucketName
import org.oppia.android.util.parser.html.HtmlParser
import javax.inject.Inject

/** The presenter for [PrivacyPolicySingleActivity]. */
@ActivityScope
class PrivacyPolicySingleActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val htmlParserFactory: HtmlParser.Factory,
  @DefaultResourceBucketName private val resourceBucketName: String
) {

  private lateinit var privacyPolicySingleActivityToolbar: Toolbar
  private lateinit var privacyPolicyDescription: String

  fun handleOnCreate() {
    val binding = DataBindingUtil.setContentView<PrivacyPolicySingleActivityBinding>(
      activity,
      R.layout.privacy_policy_single_activity
    )
    binding.apply {
      lifecycleOwner = activity
    }

    privacyPolicySingleActivityToolbar = binding.privacyPolicySingleActivityToolbar
    activity.setSupportActionBar(privacyPolicySingleActivityToolbar)
    activity.supportActionBar!!.title = activity.getString(R.string.privacy_policy_activity_title)
    activity.supportActionBar!!.setDisplayShowHomeEnabled(true)
    activity.supportActionBar!!.setDisplayHomeAsUpEnabled(true)

    binding.privacyPolicySingleActivityToolbar.setNavigationOnClickListener {
      (activity as PrivacyPolicySingleActivity).finish()
    }

    // NOTE: Here entityType and entityId can be anything as it will actually not get used.
    // They are needed only for cases where rich-text contains images from server and in PrivacyPolicy
    // we do not have images.
    privacyPolicyDescription = activity.getString(R.string.privacy_policy_content)
    val privacyPolicydescriptionTextView =
      activity.findViewById<TextView>(R.id.privacy_policy_description_text_view)
    privacyPolicydescriptionTextView.text = htmlParserFactory.create(
      resourceBucketName,
      entityType = "PrivacyPolicy",
      entityId = "oppia",
      imageCenterAlign = false
    ).parseOppiaHtml(
      privacyPolicyDescription,
      privacyPolicydescriptionTextView
    )
  }
}
