package org.oppia.android.app.profileprogress

import android.content.Intent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.model.ProfileId
import org.oppia.android.domain.profile.ProfileManagementController
import javax.inject.Inject

/** The presenter for [ProfileProgressActivity]. */
@ActivityScope
class ProfileProgressActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val profileManagementController: ProfileManagementController
) {
  private lateinit var profileId: ProfileId

  fun handleOnCreate(internalProfileId: Int) {
    profileId = ProfileId.newBuilder().setLoggedInInternalProfileId(internalProfileId).build()
    activity.setContentView(R.layout.profile_progress_activity)
    if (getProfileProgressFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.profile_progress_fragment_placeholder,
        ProfileProgressFragment.newInstance(internalProfileId)
      ).commitNow()
    }
    setUpNavigationDrawer()
  }

  private fun setUpNavigationDrawer() {
    val toolbar = activity.findViewById<View>(
      R.id.profile_progress_activity_toolbar
    ) as Toolbar
    activity.setSupportActionBar(toolbar)
    (activity.supportActionBar ?: return).setTitle(R.string.profile)
    (activity.supportActionBar ?: return).setDisplayShowHomeEnabled(true)
    (activity.supportActionBar ?: return).setDisplayHomeAsUpEnabled(true)
    toolbar.setNavigationOnClickListener {
      activity.finish()
    }
  }

  private fun getProfileProgressFragment(): ProfileProgressFragment? {
    return activity.supportFragmentManager.findFragmentById(
      R.id.profile_progress_fragment_placeholder
    ) as ProfileProgressFragment?
  }

  fun updateProfileAvatar(intent: Intent?) {
    profileManagementController.updateProfileAvatar(
      profileId,
      intent?.data,
      /* colorRgb= */ 10710042
    )
  }
}
