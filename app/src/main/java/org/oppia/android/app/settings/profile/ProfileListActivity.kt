package org.oppia.android.app.settings.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.model.ScreenName.PROFILE_LIST_ACTIVITY
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import javax.inject.Inject

/** Activity to display all profiles to admin. */
class ProfileListActivity :
  InjectableAppCompatActivity(),
  RouteToProfileEditListener {
  @Inject
  lateinit var profileListActivityPresenter: ProfileListActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    profileListActivityPresenter.handleOnCreate()
  }

  override fun onSupportNavigateUp(): Boolean {
    finish()
    return false
  }

  override fun routeToProfileEditActivity(profileId: Int) {
    startActivity(
      ProfileEditActivity.createProfileEditActivity(
        context = this,
        profileId = profileId,
        isMultipane = false
      )
    )
  }

  companion object {
    /** Returns a new [Intent] to route to [ProfileListActivity]. */
    fun createProfileListActivityIntent(context: Context): Intent {
      return Intent(context, ProfileListActivity::class.java).apply {
        decorateWithScreenName(PROFILE_LIST_ACTIVITY)
      }
    }
  }
}
