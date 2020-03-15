package org.oppia.app.help.faq

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.databinding.FaqActivityBinding
import javax.inject.Inject

/** The presenter for [FAQActivity]. */
@ActivityScope
class FAQActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {
  private lateinit var faqActivityToolbar: Toolbar

  fun handleOnCreate() {
    val binding = DataBindingUtil.setContentView<FaqActivityBinding>(activity, R.layout.faq_activity)
    binding.apply {
      lifecycleOwner = activity
    }

    faqActivityToolbar = binding.faqActivityToolbar
    activity.setSupportActionBar(faqActivityToolbar)
    activity.supportActionBar!!.title = activity.getString(R.string.FAQs)
    activity.supportActionBar!!.setDisplayShowHomeEnabled(true)
    activity.supportActionBar!!.setDisplayHomeAsUpEnabled(true)

    binding.faqActivityToolbar.setNavigationOnClickListener {
      (activity as FAQActivity).finish()
    }

    if (getFAQFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.faq_fragment_placeholder,
        FAQFragment()
      ).commitNow()
    }
  }

  private fun getFAQFragment(): FAQFragment? {
    return activity.supportFragmentManager.findFragmentById(R.id.faq_fragment_placeholder) as FAQFragment?
  }
}
