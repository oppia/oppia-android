package org.oppia.app.help.faq

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.databinding.FaqListActivityBinding
import javax.inject.Inject

/** The presenter for [FAQListActivity]. */
@ActivityScope
class FAQListActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {
  private lateinit var faqListActivityToolbar: Toolbar

  fun handleOnCreate() {
    val binding =
      DataBindingUtil.setContentView<FaqListActivityBinding>(activity, R.layout.faq_list_activity)
    binding.apply {
      lifecycleOwner = activity
    }

    faqListActivityToolbar = binding.faqListActivityToolbar
    activity.setSupportActionBar(faqListActivityToolbar)
    activity.supportActionBar!!.title = activity.getString(R.string.FAQs)
    activity.supportActionBar!!.setDisplayShowHomeEnabled(true)
    activity.supportActionBar!!.setDisplayHomeAsUpEnabled(true)

    binding.faqListActivityToolbar.setNavigationOnClickListener {
      (activity as FAQListActivity).finish()
    }

    if (getFAQListFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.faq_list_fragment_placeholder,
        FAQListFragment()
      ).commitNow()
    }
  }

  private fun getFAQListFragment(): FAQListFragment? {
    return activity
      .supportFragmentManager
      .findFragmentById(R.id.faq_list_fragment_placeholder) as FAQListFragment?
  }
}
