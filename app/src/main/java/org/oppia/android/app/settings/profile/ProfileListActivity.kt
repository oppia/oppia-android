package org.oppia.android.app.settings.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** Activity to display all profiles to admin. */
class ProfileListActivity : InjectableAppCompatActivity(), ProfileListInterface {
  @Inject
  lateinit var profileListActivityPresenter: ProfileListActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    var internalProfileId = 0
    if (savedInstanceState != null) {
      internalProfileId = savedInstanceState.getInt(PROFILE_EDIT_PROFILE_ID_EXTRA_KEY, -1)
    }
    profileListActivityPresenter.handleOnCreate(internalProfileId)
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

  override fun updateToolbarTitle(title: String) {
    profileListActivityPresenter.updateToolbarTitle(title)
  }

  override fun toolbarListener(function: () -> Unit) {
    profileListActivityPresenter.toolbarListener(function)
  }
}
