package org.oppia.android.app.help.thirdparty

import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.oppia.android.R
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.utility.activity.ActivityScope
import org.oppia.android.databinding.LicenseListActivityBinding
import javax.inject.Inject

/** The presenter for [LicenseListActivity]. */
@ActivityScope
class LicenseListActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val resourceHandler: AppLanguageResourceHandler
) {

  /** Handles onCreate() method of the [LicenseListActivity]. */
  fun handleOnCreate(dependencyIndex: Int, isMultipane: Boolean) {
    val binding =
      DataBindingUtil.setContentView<LicenseListActivityBinding>(
        activity,
        R.layout.license_list_activity
      )
    binding.apply {
      lifecycleOwner = activity
    }

    val licenseListActivityToolbar = binding.licenseListActivityToolbar
    activity.setSupportActionBar(licenseListActivityToolbar)
    activity.supportActionBar!!.title =
      resourceHandler.getStringInLocale(R.string.license_list_activity_title)
    activity.supportActionBar!!.setDisplayShowHomeEnabled(true)
    activity.supportActionBar!!.setDisplayHomeAsUpEnabled(true)

    binding.licenseListActivityToolbar.setNavigationOnClickListener {
      (activity as LicenseListActivity).finish()
    }
    if (getLicenseListFragment() == null) {
      val licenseListFragment = LicenseListFragment.newInstance(dependencyIndex, isMultipane)
      activity.supportFragmentManager.beginTransaction()
        .add(R.id.license_list_fragment_placeholder, licenseListFragment).commitNow()
    }
  }

  private fun getLicenseListFragment(): LicenseListFragment? {
    return activity
      .supportFragmentManager
      .findFragmentById(R.id.license_list_fragment_placeholder) as LicenseListFragment?
  }
}
