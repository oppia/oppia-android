package org.oppia.android.app.devoptions.vieweventlogs

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.R
import org.oppia.android.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** Activity for View Event Logs. */
class ViewEventLogsActivity : InjectableAppCompatActivity() {
  @Inject
  lateinit var viewEventLogsActivityPresenter: ViewEventLogsActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    viewEventLogsActivityPresenter.handleOnCreate()
    title = getString(R.string.view_event_logs_activity_title)
  }

  companion object {
    fun createViewEventLogsActivityIntent(context: Context): Intent {
      return Intent(context, ViewEventLogsActivity::class.java)
    }
  }
}
