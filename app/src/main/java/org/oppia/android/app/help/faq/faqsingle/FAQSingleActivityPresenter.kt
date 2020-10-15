package org.oppia.android.app.help.faq.faqsingle

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.databinding.FaqSingleActivityBinding
import org.oppia.android.util.gcsresource.DefaultResourceBucketName
import javax.inject.Inject

/** The presenter for [FAQSingleActivity]. */
@ActivityScope
class FAQSingleActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  @DefaultResourceBucketName private val resourceBucketName: String
) {

  private lateinit var faqSingleActivityToolbar: Toolbar

  fun handleOnCreate(question: String, answer: String) {
    val binding = DataBindingUtil.setContentView<FaqSingleActivityBinding>(
      activity,
      R.layout.faq_single_activity
    )
    binding.apply {
      lifecycleOwner = activity
    }

    faqSingleActivityToolbar = binding.faqSingleActivityToolbar
    activity.setSupportActionBar(faqSingleActivityToolbar)
    activity.supportActionBar!!.title = activity.resources.getString(R.string.FAQs)
    activity.supportActionBar!!.setDisplayShowHomeEnabled(true)
    activity.supportActionBar!!.setDisplayHomeAsUpEnabled(true)

    binding.faqSingleActivityToolbar.setNavigationOnClickListener {
      (activity as FAQSingleActivity).finish()
    }
    binding.questionText = question
    binding.answerText = answer
    binding.gcsResourceName = resourceBucketName
  }
}
