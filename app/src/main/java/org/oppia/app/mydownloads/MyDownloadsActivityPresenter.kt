package org.oppia.app.mydownloads

import androidx.appcompat.app.AppCompatActivity
import org.oppia.app.ui.R
import org.oppia.app.activity.ActivityScope
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
