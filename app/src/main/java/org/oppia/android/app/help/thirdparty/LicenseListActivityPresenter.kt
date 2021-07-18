package org.oppia.android.app.help.thirdparty

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.databinding.LicenseListActivityBinding
import javax.inject.Inject

/** The presenter for [LicenseListActivity]. */
@ActivityScope
class LicenseListActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {
  private lateinit var licenseListActivityToolbar: Toolbar

  fun handleOnCreate() {
    val binding =
      DataBindingUtil.setContentView<LicenseListActivityBinding>(
        activity,
        R.layout.license_list_activity
      )
    binding.apply {
      lifecycleOwner = activity
    }

    licenseListActivityToolbar = binding.thirdPartyDependencyLicensesActivityToolbar
    activity.setSupportActionBar(licenseListActivityToolbar)
    activity.supportActionBar!!.title = activity.getString(R.string.FAQs)
    activity.supportActionBar!!.setDisplayShowHomeEnabled(true)
    activity.supportActionBar!!.setDisplayHomeAsUpEnabled(true)

    binding.thirdPartyDependencyLicensesActivityToolbar.setNavigationOnClickListener {
      (activity as LicenseListActivity).finish()
    }

//    if (getThirdPartyDependencyListFragment() == null) {
//      activity.supportFragmentManager.beginTransaction().add(
//        R.id.faq_list_fragment_placeholder,
//        FAQListFragment()
//      ).commitNow()
//    }
  }

//  private fun getThirdPartyDependencyListFragment(): FAQListFragment? {
//    return activity
//      .supportFragmentManager
//      .findFragmentById(R.id.faq_list_fragment_placeholder) as FAQListFragment?
//  }
}
