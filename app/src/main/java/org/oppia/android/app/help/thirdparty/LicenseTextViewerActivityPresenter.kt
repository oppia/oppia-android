package org.oppia.android.app.help.thirdparty

import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.databinding.databinding.LicenseTextViewerActivityBinding
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.ui.R
import org.oppia.android.app.utility.EdgeToEdgeHelper
import org.oppia.android.util.accessibility.AccessibilityService
import org.oppia.android.util.platformparameter.EnableEdgeToEdge
import org.oppia.android.util.platformparameter.PlatformParameterValue
import javax.inject.Inject

/** The presenter for [LicenseTextViewerActivity]. */
@ActivityScope
class LicenseTextViewerActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val resourceHandler: AppLanguageResourceHandler,
  @EnableEdgeToEdge
  private val enableEdgeToEdge: PlatformParameterValue<Boolean>
) {
  @Inject lateinit var accessibilityService: AccessibilityService

  /** Handles onCreate() method of the [LicenseTextViewerActivity]. */
  fun handleOnCreate(dependencyIndex: Int, licenseIndex: Int) {
    if (enableEdgeToEdge.value) {
      EdgeToEdgeHelper.enableEdgeToEdgeDispatch(activity)
    }
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
    if (enableEdgeToEdge.value) {
      EdgeToEdgeHelper.applyToAppBarLayout(
        activity,
        licenseTextViewerActivityToolbar,
        R.color.component_color_shared_activity_status_bar_color
      )
    }

    if (!accessibilityService.isScreenReaderEnabled()) {
      binding.licenseTextViewerActivityToolbarTitle.setOnClickListener {
        binding.licenseTextViewerActivityToolbarTitle.isSelected = true
      }
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
