package org.oppia.android.app.mydownloads

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.app.R
import org.oppia.android.app.activity.ActivityScope
import javax.inject.Inject

/** The presenter for [MyDownloadsActivity]. */
@ActivityScope
class MyDownloadsActivityPresenter @Inject constructor(private val activity: AppCompatActivity) {
  fun handleOnCreate() {
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
