package org.oppia.app.settings.profile

import android.content.Context
import android.content.Intent
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

  companion object {
    /** Returns a new [Intent] to route to [ProfileListActivity]. */
    fun createProfileListActivityIntent(context: Context): Intent {
      return Intent(context, ProfileListActivity::class.java)
    }
  }
}
