package org.oppia.android.app.settings.profile

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import javax.inject.Inject
import org.oppia.android.app.activity.route.ActivityRouter
import org.oppia.android.app.model.DestinationScreen
import org.oppia.android.app.model.ProfileListActivityParams

// TODO: Consolidate with ones in ProfileEditActivity & clean up.
private const val IS_MULTIPANE_EXTRA_KEY = "ProfileEditActivity.isMultipane"
private const val PROFILE_EDIT_PROFILE_ID_EXTRA_KEY =
  "ProfileEditActivity.profile_edit_profile_id"

/** The presenter for [ProfileEditActivity]. */
@ActivityScope
class ProfileEditActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val activityRouter: ActivityRouter
) {

  private lateinit var toolbar: Toolbar

  /** Handles onCreate function of [ProfileEditActivity]. */
  fun handleOnCreate() {
    activity.setContentView(R.layout.profile_edit_activity)
    setUpToolbar()

    val profileId = activity.intent.getIntExtra(PROFILE_EDIT_PROFILE_ID_EXTRA_KEY, 0)
    val isMultipane = activity.intent.getBooleanExtra(IS_MULTIPANE_EXTRA_KEY, false)

    toolbar.setNavigationOnClickListener {
      if (isMultipane) {
        activity.onBackPressed()
      } else {
        activityRouter.routeToScreen(
          DestinationScreen.newBuilder().apply {
            profileListActivityParams = ProfileListActivityParams.newBuilder().apply {
              clearTop = true
            }.build()
          }.build()
        )
      }
    }

    if (getProfileEditFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.profile_edit_fragment_placeholder,
        ProfileEditFragment.newInstance(profileId, isMultipane)
      ).commitNow()
    }
  }

  private fun setUpToolbar() {
    toolbar = activity.findViewById<View>(R.id.profile_edit_toolbar) as Toolbar
    activity.setSupportActionBar(toolbar)
  }

  private fun getProfileEditFragment(): ProfileEditFragment? {
    return activity
      .supportFragmentManager
      .findFragmentById(R.id.profile_edit_fragment_placeholder) as ProfileEditFragment?
  }
}
