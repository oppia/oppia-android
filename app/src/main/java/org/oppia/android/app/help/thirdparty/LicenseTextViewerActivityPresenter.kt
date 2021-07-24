package org.oppia.android.app.help.thirdparty

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.databinding.LicenseTextViewerActivityBinding
import javax.inject.Inject

/** The presenter for [LicenseTextViewerActivity]. */
@ActivityScope
class LicenseTextViewerActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {
  private lateinit var licenseTextViewerActivityToolbar: Toolbar

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
    Log.d("Activity", "Dependency Index : $dependencyIndex.")
    Log.d("Activity", "License Index : $licenseIndex.")
    licenseTextViewerActivityToolbar = binding.licenseTextViewerActivityToolbar
    activity.setSupportActionBar(licenseTextViewerActivityToolbar)
    activity.supportActionBar!!.title = activity.getString(R.string.license_list_activity_title)
    activity.supportActionBar!!.setDisplayShowHomeEnabled(true)
    activity.supportActionBar!!.setDisplayHomeAsUpEnabled(true)

    binding.licenseTextViewerActivityToolbar.setNavigationOnClickListener {
      (activity as LicenseTextViewerActivity).finish()
    }

    if (getLicenseTextViewerFragment() == null) {
      val licenseTextViewerFragment =
        LicenseTextViewerFragment.newInstance(dependencyIndex, licenseIndex)
      activity.supportFragmentManager.beginTransaction()
        .add(R.id.license_text_viewer_fragment_placeholder, licenseTextViewerFragment).commitNow()
    }
  }

  private fun getLicenseTextViewerFragment(): LicenseTextViewerFragment? {
    return activity
      .supportFragmentManager
      .findFragmentById(R.id.license_text_viewer_fragment_placeholder) as
      LicenseTextViewerFragment?
  }
}
