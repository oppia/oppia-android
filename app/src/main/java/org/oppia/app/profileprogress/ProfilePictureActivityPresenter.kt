package org.oppia.app.profileprogress

import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.databinding.ProfilePictureActivityBinding
import org.oppia.app.model.Profile
import org.oppia.app.model.ProfileAvatar
import org.oppia.app.model.ProfileId
import org.oppia.domain.profile.ProfileManagementController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.ConsoleLogger
import org.oppia.util.statusbar.StatusBarColor
import javax.inject.Inject

/** The presenter for [ProfilePictureActivity]. */
@ActivityScope
class ProfilePictureActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val profileManagementController: ProfileManagementController,
  private val logger: ConsoleLogger
) {
  private lateinit var profilePictureActivityViewModel: ProfilePictureActivityViewModel
  private lateinit var profileId: ProfileId

  fun handleOnCreate(internalProfileId: Int) {
    StatusBarColor.statusBarColorUpdate(R.color.profileStatusBar, activity, false)
    val binding = DataBindingUtil
      .setContentView<ProfilePictureActivityBinding>(
        activity,
        R.layout.profile_picture_activity
      )
    profilePictureActivityViewModel = ProfilePictureActivityViewModel()

    binding.apply {
      viewModel = profilePictureActivityViewModel
      lifecycleOwner = activity
    }
    profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()

    subscribeToProfileLiveData()
  }

  private val profileLiveData: LiveData<Profile> by lazy {
    getProfileData()
  }

  private fun getProfileData(): LiveData<Profile> {
    return Transformations.map(
      profileManagementController.getProfile(profileId),
      ::processGetProfileResult
    )
  }

  private fun subscribeToProfileLiveData() {
    profileLiveData.observe(
      activity,
      Observer<Profile> { result ->
        setProfileAvatar(result.avatar)
      }
    )
  }

  private fun processGetProfileResult(profileResult: AsyncResult<Profile>): Profile {
    if (profileResult.isFailure()) {
      logger.e(
        "ProfilePictureActivity",
        "Failed to retrieve profile",
        profileResult.getErrorOrNull()!!
      )
    }
    return profileResult.getOrDefault(Profile.getDefaultInstance())
  }

  private fun setProfileAvatar(avatar: ProfileAvatar) {
    if (::profilePictureActivityViewModel.isInitialized) {
      profilePictureActivityViewModel.profileAvatar.set(avatar)
    }
  }
}
