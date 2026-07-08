package org.oppia.android.app.profileprogress

import android.content.Intent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.model.LegacyProfileId
import org.oppia.android.app.ui.R
import org.oppia.android.app.utility.EdgeToEdgeHelper
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.platformparameter.EnableEdgeToEdge
import org.oppia.android.util.platformparameter.PlatformParameterValue
import org.oppia.android.util.profile.toProfileIdPreservingZero
import javax.inject.Inject

/** The presenter for [ProfileProgressActivity]. */
@ActivityScope
class ProfileProgressActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val profileManagementController: ProfileManagementController,
  @EnableEdgeToEdge private val enableEdgeToEdge: PlatformParameterValue<Boolean>
) {
  private lateinit var profileId: LegacyProfileId

  fun handleOnCreate(internalProfileId: Int) {
    profileId = LegacyProfileId.newBuilder().setInternalId(internalProfileId).build()
    if (enableEdgeToEdge.value) {
      EdgeToEdgeHelper.enableEdgeToEdgeDispatch(activity)
    }
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
    if (enableEdgeToEdge.value) {
      EdgeToEdgeHelper.applyToToolbarContainer(
        activity,
        toolbar,
        R.color.component_color_shared_activity_status_bar_color
      )
    }
  }

  private fun getProfileProgressFragment(): ProfileProgressFragment? {
    return activity.supportFragmentManager.findFragmentById(
      R.id.profile_progress_fragment_placeholder
    ) as ProfileProgressFragment?
  }

  fun updateProfileAvatar(intent: Intent?) {
    profileManagementController.updateProfileAvatar(
      profileId.toProfileIdPreservingZero(),
      intent?.data,
      /* colorRgb= */ 10710042
    )
  }
}
