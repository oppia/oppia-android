package org.oppia.android.app.administratorcontrols.learneranalytics

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/**
 * Activity for showing a list of analytics IDs corresponding to the user's device and profiles of
 * the app.
 *
 * These IDs are meant to help facilitators of app user studies correspond specific logged events to
 * a particular user or group.
 */
class ProfileAndDeviceIdActivity : InjectableAppCompatActivity() {
  @Inject lateinit var profileAndDeviceIdActivityPresenter: ProfileAndDeviceIdActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    profileAndDeviceIdActivityPresenter.handleOnCreate()
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == android.R.id.home) {
      onBackPressed()
    }
    return super.onOptionsItemSelected(item)
  }

  companion object {
    /** Returns an [Intent] to launch [ProfileAndDeviceIdActivity]. */
    fun createIntent(context: Context): Intent {
      return Intent(context, ProfileAndDeviceIdActivity::class.java)
    }
  }
}
