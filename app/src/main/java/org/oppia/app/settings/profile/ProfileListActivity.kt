package org.oppia.app.settings.profile

import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** Activity that allows users to select a profile to edit from settings. */
class ProfileListActivity : InjectableAppCompatActivity() {
  @Inject lateinit var profileListActivityPresenter: ProfileListActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    profileListActivityPresenter.handleOnCreate()
  }

  override fun onSupportNavigateUp(): Boolean {
    finish()
    return false
  }
}
