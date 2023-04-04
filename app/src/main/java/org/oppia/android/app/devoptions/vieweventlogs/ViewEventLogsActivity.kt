package org.oppia.android.app.devoptions.vieweventlogs

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ScreenName.VIEW_EVENT_LOGS_ACTIVITY
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import javax.inject.Inject

/** Activity for View Event Logs. */
class ViewEventLogsActivity : InjectableAppCompatActivity() {
  @Inject
  lateinit var viewEventLogsActivityPresenter: ViewEventLogsActivityPresenter

  @Inject
  lateinit var resourceHandler: AppLanguageResourceHandler

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    viewEventLogsActivityPresenter.handleOnCreate()
    title = resourceHandler.getStringInLocale(R.string.view_event_logs_activity_title)
  }

  companion object {
    fun createViewEventLogsActivityIntent(context: Context, profileId: ProfileId): Intent {
      return Intent(context, ViewEventLogsActivity::class.java).apply {
        decorateWithScreenName(VIEW_EVENT_LOGS_ACTIVITY)
        decorateWithUserProfileId(profileId)
      }
    }
  }
}
