package org.oppia.android.app.help.thirdparty

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import javax.inject.Inject
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.databinding.LicenseTextViewerActivityBinding

/** The presenter for [LicenseTextViewerActivity]. */
@ActivityScope
class LicenseTextViewerActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {
  private lateinit var licenseTextViewerActivityToolbar: Toolbar

  /** Handles onCreate() method of the [LicenseTextViewerActivity]. */
  fun handleOnCreate() {
    val binding =
      DataBindingUtil.setContentView<LicenseTextViewerActivityBinding>(
        activity,
        R.layout.license_text_viewer_activity
      )
    binding.apply {
      lifecycleOwner = activity
    }

    licenseTextViewerActivityToolbar = binding.licenseTextViewerActivityToolbar
    activity.setSupportActionBar(licenseTextViewerActivityToolbar)
    activity.supportActionBar!!.title = activity.getString(R.string.license_list_activity_title)
    activity.supportActionBar!!.setDisplayShowHomeEnabled(true)
    activity.supportActionBar!!.setDisplayHomeAsUpEnabled(true)

    binding.licenseTextViewerActivityToolbar.setNavigationOnClickListener {
      (activity as LicenseTextViewerActivity).finish()
    }

    if (getLicenseTextViewerFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.license_text_viewer_fragment_placeholder,
        LicenseTextViewerFragment()
      ).commitNow()
    }
  }

  private fun getLicenseTextViewerFragment(): LicenseTextViewerFragment? {
    return activity
      .supportFragmentManager
      .findFragmentById(R.id.license_text_viewer_fragment_placeholder) as
      LicenseTextViewerFragment?
  }
}
