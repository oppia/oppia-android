package org.oppia.android.app.administratorcontrols.appversion

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.model.ScreenName.APP_VERSION_ACTIVITY
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import javax.inject.Inject

/** Activity for App Version. */
class AppVersionActivity : InjectableAutoLocalizedAppCompatActivity() {
  @Inject
  lateinit var appVersionActivityPresenter: AppVersionActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    appVersionActivityPresenter.handleOnCreate()
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == android.R.id.home) {
      @Suppress("DEPRECATION") // TODO(#5404): Migrate to a back pressed dispatcher.
      onBackPressed()
    }
    return super.onOptionsItemSelected(item)
  }

  companion object {
    /** Returns an [Intent] to start this activity. */
    fun createAppVersionActivityIntent(context: Context): Intent {
      return Intent(context, AppVersionActivity::class.java).apply {
        decorateWithScreenName(APP_VERSION_ACTIVITY)
      }
    }
  }
}
