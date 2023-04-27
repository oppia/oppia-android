package org.oppia.android.app.administratorcontrols.appversion

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.model.ScreenName.APP_VERSION_ACTIVITY
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import javax.inject.Inject

/** Activity for App Version. */
class AppVersionActivity : InjectableAppCompatActivity() {
  @Inject
  lateinit var appVersionActivityPresenter: AppVersionActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as Injector).inject(this)
    appVersionActivityPresenter.handleOnCreate()
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == android.R.id.home) {
      onBackPressed()
    }
    return super.onOptionsItemSelected(item)
  }

  interface Injector {
    fun inject(activity: AppVersionActivity)
  }

  companion object {
    fun createIntent(context: Context): Intent {
      return Intent(context, AppVersionActivity::class.java).apply {
        decorateWithScreenName(APP_VERSION_ACTIVITY)
      }
    }
  }
}
