package org.oppia.android.app.settings.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.activity.route.ActivityRouter
import org.oppia.android.app.model.DestinationScreen
import org.oppia.android.app.model.ProfileEditActivityParams
import org.oppia.android.app.model.ScreenName.PROFILE_LIST_ACTIVITY
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import javax.inject.Inject

/** Activity to display all profiles to admin. */
class ProfileListActivity :
  InjectableAppCompatActivity(),
  RouteToProfileEditListener {
  @Inject lateinit var profileListActivityPresenter: ProfileListActivityPresenter
  @Inject lateinit var activityRouter: ActivityRouter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as Injector).inject(this)
    profileListActivityPresenter.handleOnCreate()
  }

  override fun onSupportNavigateUp(): Boolean {
    finish()
    return false
  }

  override fun routeToProfileEditActivity(profileId: Int) {
    activityRouter.routeToScreen(
      DestinationScreen.newBuilder().apply {
        profileEditActivityParams = ProfileEditActivityParams.newBuilder().apply {
          this.internalProfileId = profileId
          this.isMultipane = false
        }.build()
      }.build()
    )
  }

  /** Dagger injector for [ProfileListActivity]. */
  interface Injector {
    /** Injects dependencies into the [activity]. */
    fun inject(activity: ProfileListActivity)
  }

  companion object {
    /** Returns a new [Intent] to route to [ProfileListActivity]. */
    fun createIntent(context: Context, clearTop: Boolean = false): Intent {
      return Intent(context, ProfileListActivity::class.java).apply {
        if (clearTop) addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        decorateWithScreenName(PROFILE_LIST_ACTIVITY)
      }
    }
  }
}
