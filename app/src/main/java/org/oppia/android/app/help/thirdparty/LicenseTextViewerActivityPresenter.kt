package org.oppia.android.app.help.thirdparty

import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.databinding.LicenseTextViewerActivityBinding
import javax.inject.Inject

/** The presenter for [LicenseTextViewerActivity]. */
@ActivityScope
class LicenseTextViewerActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val resourceHandler: AppLanguageResourceHandler
) {

  /** Handles onCreate() method of the [LicenseTextViewerActivity]. */
  fun handleOnCreate(dependencyIndex: Int, licenseIndex: Int) {
    val binding =
      DataBindingUtil.setContentView<LicenseTextViewerActivityBinding>(
        activity,
        R.layout.license_text_viewer_activity
      )
    binding.apply {
      lifecycleOwner = activity
    }
    val dependenciesWithLicenseNames =
      activity.resources.obtainTypedArray(R.array.third_party_dependency_license_names_array)
    val licenseNamesArrayResId = dependenciesWithLicenseNames.getResourceId(
      dependencyIndex,
      0
    )
    val licenseNames = resourceHandler.getStringArrayInLocale(licenseNamesArrayResId)
    val licenseTextViewerActivityToolbar = binding.licenseTextViewerActivityToolbar
    binding.licenseTextViewerActivityToolbarTitle.text = licenseNames[licenseIndex]
    activity.title = licenseNames[licenseIndex]
    dependenciesWithLicenseNames.recycle()
    activity.setSupportActionBar(licenseTextViewerActivityToolbar)
    activity.supportActionBar!!.setDisplayShowHomeEnabled(true)
    activity.supportActionBar!!.setDisplayHomeAsUpEnabled(true)

    binding.licenseTextViewerActivityToolbar.setNavigationOnClickListener {
      (activity as LicenseTextViewerActivity).finish()
    }

    binding.licenseTextViewerActivityToolbarTitle.setOnClickListener {
      binding.licenseTextViewerActivityToolbarTitle.isSelected = true
    }

    if (getLicenseTextViewerFragment() == null) {
      val licenseTextViewerFragment =
        LicenseTextViewerFragment.newInstance(dependencyIndex, licenseIndex)
      activity.supportFragmentManager.beginTransaction()
        .add(R.id.license_text_viewer_fragment_placeholder, licenseTextViewerFragment).commitNow()
    }
  }

  private fun getLicenseTextViewerFragment(): LicenseTextViewerFragment? {
    return activity.supportFragmentManager
      .findFragmentById(R.id.license_text_viewer_fragment_placeholder) as LicenseTextViewerFragment?
  }
}
