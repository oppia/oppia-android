package org.oppia.android.app.administratorcontrols.appversion

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import javax.inject.Inject

/** The presenter for [AppVersionActivity]. */
@ActivityScope
class AppVersionActivityPresenter @Inject constructor(private val activity: AppCompatActivity) {

  /** Initializes the [AppVersionActivity] views and binds [AppVersionFragment]. */
  fun handleOnCreate() {
    activity.setContentView(R.layout.app_version_activity)
    setToolbar()
    if (getAppVersionFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.app_version_fragment_placeholder,
        AppVersionFragment()
      ).commitNow()
    }
  }

  private fun setToolbar() {
    val appVersionToolbar: Toolbar = activity.findViewById(R.id.app_version_toolbar) as Toolbar
    activity.setSupportActionBar(appVersionToolbar)
    activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
  }

  private fun getAppVersionFragment(): AppVersionFragment? {
    return activity
      .supportFragmentManager
      .findFragmentById(R.id.app_version_fragment_placeholder) as AppVersionFragment?
  }
}
