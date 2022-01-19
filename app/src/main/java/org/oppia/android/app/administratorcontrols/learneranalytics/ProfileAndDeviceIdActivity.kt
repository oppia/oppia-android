package org.oppia.android.app.administratorcontrols.learneranalytics

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import javax.inject.Inject
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAppCompatActivity

class ProfileAndDeviceIdActivity : InjectableAppCompatActivity() {

  @Inject
  lateinit var profileAndDeviceIdActivityPresenter: ProfileAndDeviceIdActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    profileAndDeviceIdActivityPresenter.handleOnCreate()
  }

  override fun onOptionsItemSelected(item: MenuItem?): Boolean {
    if (item?.itemId == android.R.id.home) {
      onBackPressed()
    }
    return super.onOptionsItemSelected(item)
  }

  companion object {
    fun createProfileAndDeviceActivityIntent(context: Context): Intent {
      return Intent(context, ProfileAndDeviceIdActivity::class.java)
    }
  }
}
