package org.oppia.android.app.settings.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** Activity to display all profiles to admin. */
class ProfileListActivity : InjectableAppCompatActivity() {
  @Inject
  lateinit var profileListActivityPresenter: ProfileListActivityPresenter

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
