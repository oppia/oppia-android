package org.oppia.android.app.settings.profile

import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.databinding.databinding.ProfileRenameActivityBinding
import org.oppia.android.app.ui.R
import org.oppia.android.app.utility.edgetoedge.EdgeToEdgeHelper
import org.oppia.android.util.platformparameter.EnableEdgeToEdge
import org.oppia.android.util.platformparameter.PlatformParameterValue
import javax.inject.Inject

/** The presenter for [ProfileRenameActivity]. */
@ActivityScope
class ProfileRenameActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  @EnableEdgeToEdge
  private val enableEdgeToEdge: PlatformParameterValue<Boolean>
) {

  /** Handles onCreate() of [ProfileRenameActivity]. */
  fun handleOnCreate(profileId: Int) {
    if (enableEdgeToEdge.value) {
      EdgeToEdgeHelper.enableEdgeToEdgeDispatch(activity)
    }
    activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    activity.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp)

    val binding =
      DataBindingUtil.setContentView<ProfileRenameActivityBinding>(
        activity,
        R.layout.profile_rename_activity
      )

    binding.apply {
      lifecycleOwner = activity
    }

    binding.profileRenameToolbar.setNavigationOnClickListener {
      (activity as ProfileRenameActivity).finish()
    }

    if (enableEdgeToEdge.value) {
      EdgeToEdgeHelper.applyToAppBarLayout(
        activity,
        binding.profileRenameToolbar,
        R.color.component_color_shared_activity_status_bar_color
      )
    }

    if (getProfileRenameFragment() == null) {
      val profileRenameFragment = ProfileRenameFragment.newInstance(profileId)
      activity.supportFragmentManager.beginTransaction()
        .add(R.id.profile_rename_fragment_placeholder, profileRenameFragment).commitNow()
    }
  }

  private fun getProfileRenameFragment(): ProfileRenameFragment? {
    return activity.supportFragmentManager
      .findFragmentById(
        R.id.profile_rename_fragment_placeholder
      ) as ProfileRenameFragment?
  }
}
