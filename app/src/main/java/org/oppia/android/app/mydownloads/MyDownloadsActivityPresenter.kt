package org.oppia.android.app.mydownloads

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.ui.R
import org.oppia.android.app.utility.edgetoedge.EdgeToEdgeHelper
import org.oppia.android.util.platformparameter.EnableEdgeToEdge
import org.oppia.android.util.platformparameter.PlatformParameterValue
import javax.inject.Inject

/** The presenter for [MyDownloadsActivity]. */
@ActivityScope
class MyDownloadsActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  @EnableEdgeToEdge private val enableEdgeToEdge: PlatformParameterValue<Boolean>
) {
  fun handleOnCreate() {
    if (enableEdgeToEdge.value) {
      EdgeToEdgeHelper.enableEdgeToEdgeDispatch(activity)
    }
    activity.setContentView(R.layout.my_downloads_activity)
    if (getMyDownloadsFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.my_downloads_fragment_placeholder,
        MyDownloadsFragment()
      ).commitNow()
    }
  }

  private fun getMyDownloadsFragment(): MyDownloadsFragment? {
    return activity
      .supportFragmentManager
      .findFragmentById(
        R.id.my_downloads_fragment_placeholder
      ) as MyDownloadsFragment?
  }
}
