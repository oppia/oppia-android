package org.oppia.android.app.devoptions.vieweventlogs.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.R
import org.oppia.android.app.devoptions.vieweventlogs.ViewEventLogsFragment
import org.oppia.android.app.testing.activity.TestActivity
import org.oppia.android.app.utility.activity.ActivityComponentImpl

/** Activity for testing [ViewEventLogsFragment]. */
class ViewEventLogsTestActivity : TestActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp)
    setContentView(R.layout.view_event_logs_activity)
    if (getViewEventLogsFragment() == null) {
      val viewEventLogsFragment = ViewEventLogsFragment.newInstance()
      supportFragmentManager.beginTransaction().add(
        R.id.view_event_logs_container,
        viewEventLogsFragment
      ).commitNow()
    }
  }

  private fun getViewEventLogsFragment(): ViewEventLogsFragment? {
    return supportFragmentManager
      .findFragmentById(R.id.view_event_logs_container) as ViewEventLogsFragment?
  }

  companion object {
    /** Returns [Intent] for [ViewEventLogsTestActivity]. */
    fun createViewEventLogsTestIntent(context: Context): Intent {
      return Intent(context, ViewEventLogsTestActivity::class.java)
    }
  }
}
