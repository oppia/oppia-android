package org.oppia.android.app.settings.profile

import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.model.ProfileResetPinActivityParams
import org.oppia.android.app.settings.profile.ProfileResetPinActivity.Companion.PROFILE_RESET_PIN_ACTIVITY_PARAMS_KEY
import org.oppia.android.databinding.ProfileResetPinActivityBinding
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.extensions.getProtoExtra
import javax.inject.Inject

/** The presenter for [ProfileResetPinActivity]. */
@ActivityScope
class ProfileResetPinActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val profileManagementController: ProfileManagementController,
) {

  /** Handles onCreate() method of the [ProfileResetPinActivity]. */
  fun handleOnCreate() {
    activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    activity.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp)

    val binding =
      DataBindingUtil.setContentView<ProfileResetPinActivityBinding>(
        activity,
        R.layout.profile_reset_pin_activity
      )

    val args = activity.intent.getProtoExtra(
      PROFILE_RESET_PIN_ACTIVITY_PARAMS_KEY,
      ProfileResetPinActivityParams.getDefaultInstance()
    )

    val profileId = args?.internalProfileId ?: 0
    val isAdmin = args?.isAdmin ?: false

    binding.profileResetPinToolbar.setNavigationOnClickListener {
      (activity as ProfileResetPinActivity).finish()
    }

    binding.apply {
      lifecycleOwner = activity
    }
    if (getProfileResetPinFragment() == null) {
      val profileResetPinFragment =
        ProfileResetPinFragment.newInstance(profileId, isAdmin)
      activity.supportFragmentManager.beginTransaction()
        .add(R.id.profile_reset_pin_fragment_placeholder, profileResetPinFragment).commitNow()
    }
  }

  private fun getProfileResetPinFragment(): ProfileResetPinFragment? {
    return activity.supportFragmentManager
      .findFragmentById(R.id.profile_reset_pin_fragment_placeholder) as ProfileResetPinFragment?
  }
}
