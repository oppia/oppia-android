package org.oppia.android.app.help.faq

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.databinding.databinding.FaqListActivityBinding
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.ui.R
import org.oppia.android.app.utility.EdgeToEdgeHelper
import org.oppia.android.util.platformparameter.EnableEdgeToEdge
import org.oppia.android.util.platformparameter.PlatformParameterValue
import javax.inject.Inject

/** The presenter for [FAQListActivity]. */
@ActivityScope
class FAQListActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val resourceHandler: AppLanguageResourceHandler,
  @EnableEdgeToEdge private val enableEdgeToEdge: PlatformParameterValue<Boolean>
) {
  private lateinit var faqListActivityToolbar: Toolbar

  fun handleOnCreate() {
    if (enableEdgeToEdge.value) {
      EdgeToEdgeHelper.enableEdgeToEdgeDispatch(activity)
    }
    val binding =
      DataBindingUtil.setContentView<FaqListActivityBinding>(activity, R.layout.faq_list_activity)
    binding.apply {
      lifecycleOwner = activity
    }

    faqListActivityToolbar = binding.faqListActivityToolbar
    activity.setSupportActionBar(faqListActivityToolbar)
    activity.supportActionBar!!.title = resourceHandler.getStringInLocale(R.string.FAQs)
    activity.supportActionBar!!.setDisplayShowHomeEnabled(true)
    activity.supportActionBar!!.setDisplayHomeAsUpEnabled(true)

    binding.faqListActivityToolbar.setNavigationOnClickListener {
      (activity as FAQListActivity).finish()
    }

    if (enableEdgeToEdge.value) {
      EdgeToEdgeHelper.applyToAppBarLayout(
        activity,
        faqListActivityToolbar,
        R.color.component_color_shared_activity_status_bar_color
      )
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
