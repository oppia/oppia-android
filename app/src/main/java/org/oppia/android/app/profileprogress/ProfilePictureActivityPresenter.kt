package org.oppia.android.app.profileprogress

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.model.Profile
import org.oppia.android.app.model.ProfileAvatar
import org.oppia.android.app.model.ProfileId
import org.oppia.android.databinding.ProfilePictureActivityBinding
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.statusbar.StatusBarColor
import javax.inject.Inject

/** The presenter for [ProfilePictureActivity]. */
@ActivityScope
class ProfilePictureActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val profileManagementController: ProfileManagementController,
  private val oppiaLogger: OppiaLogger
) {
  private lateinit var profilePictureActivityViewModel: ProfilePictureActivityViewModel
  private lateinit var profileId: ProfileId

  fun handleOnCreate(internalProfileId: Int) {
    StatusBarColor.statusBarColorUpdate(
      R.color.component_color_shared_profile_status_bar_color, activity, false
    )
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
    profileId = ProfileId.newBuilder().setLoggedInInternalProfileId(internalProfileId).build()

    subscribeToProfileLiveData()
    setUpToolbar()
  }

  private fun setUpToolbar() {
    val toolbar = activity.findViewById<View>(
      R.id.profile_picture_activity_toolbar
    ) as Toolbar
    activity.setSupportActionBar(toolbar)
    toolbar.setNavigationOnClickListener {
      activity.finish()
    }
  }

  private val profileLiveData: LiveData<Profile> by lazy {
    getProfileData()
  }

  private fun getProfileData(): LiveData<Profile> {
    return Transformations.map(
      profileManagementController.getProfile(profileId).toLiveData(),
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
    return when (profileResult) {
      is AsyncResult.Failure -> {
        oppiaLogger.e("ProfilePictureActivity", "Failed to retrieve profile", profileResult.error)
        Profile.getDefaultInstance()
      }
      is AsyncResult.Pending -> Profile.getDefaultInstance()
      is AsyncResult.Success -> profileResult.value
    }
  }

  private fun setProfileAvatar(avatar: ProfileAvatar) {
    if (::profilePictureActivityViewModel.isInitialized) {
      profilePictureActivityViewModel.profileAvatar.set(avatar)
    }
  }
}
