package org.oppia.android.app.settings.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.model.ScreenName.PROFILE_EDIT_ACTIVITY
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import javax.inject.Inject
import org.oppia.android.app.activity.route.ActivityRouter
import org.oppia.android.app.model.DestinationScreen
import org.oppia.android.app.model.ProfileListActivityParams

/** Argument key for the Profile Id in [ProfileEditActivity]. */
private const val PROFILE_EDIT_PROFILE_ID_EXTRA_KEY = "ProfileEditActivity.profile_edit_profile_id"

/** Argument key for the Multipane in tablet mode for [ProfileEditActivity]. */
private const val IS_MULTIPANE_EXTRA_KEY = "ProfileEditActivity.is_multipane"

/** Activity that allows admins to edit a profile. */
class ProfileEditActivity : InjectableAppCompatActivity() {
  @Inject lateinit var profileEditActivityPresenter: ProfileEditActivityPresenter
  @Inject lateinit var activityRouter: ActivityRouter

  companion object {
    /** Returns an [Intent] for opening the [ProfileEditActivity]. */
    fun createIntent(
      context: Context,
      profileId: Int,
      isMultipane: Boolean = false,
      clearTop: Boolean = false
    ): Intent {
      return Intent(context, ProfileEditActivity::class.java).apply {
        putExtra(PROFILE_EDIT_PROFILE_ID_EXTRA_KEY, profileId)
        putExtra(IS_MULTIPANE_EXTRA_KEY, isMultipane)
        if (clearTop) addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        decorateWithScreenName(PROFILE_EDIT_ACTIVITY)
      }
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as Injector).inject(this)
    profileEditActivityPresenter.handleOnCreate()
  }

  override fun onBackPressed() {
    val isMultipane = intent.extras!!.getBoolean(IS_MULTIPANE_EXTRA_KEY, false)
    if (isMultipane) {
      super.onBackPressed()
    } else {
      activityRouter.routeToScreen(
        DestinationScreen.newBuilder().apply {
          profileListActivityParams = ProfileListActivityParams.newBuilder().apply {
            this.clearTop = true
          }.build()
        }.build()
      )
    }
  }

  interface Injector {
    fun inject(activity: ProfileEditActivity)
  }
}
