package org.oppia.android.app.devoptions.vieweventlogs

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import javax.inject.Inject

/** The presenter for [ViewEventLogsActivity]. */
@ActivityScope
class ViewEventLogsActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {

  fun handleOnCreate() {
    activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    activity.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp)
    activity.setContentView(R.layout.view_event_logs_activity)

    if (getViewEventLogsFragment() == null) {
      val viewEventLogsFragment = ViewEventLogsFragment
        .newInstance()
      activity.supportFragmentManager.beginTransaction().add(
        R.id.view_event_logs_container,
        viewEventLogsFragment
      ).commitNow()
    }
  }

  private fun getViewEventLogsFragment(): ViewEventLogsFragment? {
    return activity.supportFragmentManager
      .findFragmentById(R.id.view_event_logs_container) as ViewEventLogsFragment?
  }
}
