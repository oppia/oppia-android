package org.oppia.android.app.profileprogress

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.databinding.databinding.ProfilePictureActivityBinding
import org.oppia.android.app.model.LegacyProfileId
import org.oppia.android.app.model.Profile
import org.oppia.android.app.model.ProfileAvatar
import org.oppia.android.app.ui.R
import org.oppia.android.app.utility.edgetoedge.EdgeToEdgeHelper
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.platformparameter.EnableEdgeToEdge
import org.oppia.android.util.platformparameter.PlatformParameterValue
import org.oppia.android.util.profile.toProfileIdPreservingZero
import javax.inject.Inject

/** The presenter for [ProfilePictureActivity]. */
@ActivityScope
class ProfilePictureActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val profileManagementController: ProfileManagementController,
  private val oppiaLogger: OppiaLogger,
  @EnableEdgeToEdge
  private val enableEdgeToEdge: PlatformParameterValue<Boolean>
) {
  private lateinit var profilePictureActivityViewModel: ProfilePictureActivityViewModel
  private lateinit var profileId: LegacyProfileId

  fun handleOnCreate(internalProfileId: Int) {
    if (enableEdgeToEdge.value) {
      EdgeToEdgeHelper.enableEdgeToEdgeDispatch(activity)
    }
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
    profileId = LegacyProfileId.newBuilder().setInternalId(internalProfileId).build()

    subscribeToProfileLiveData()
    val toolbar = setUpToolbar()
    if (enableEdgeToEdge.value) {
      EdgeToEdgeHelper.applyToAppBarLayout(
        activity,
        toolbar,
        R.color.component_color_shared_profile_status_bar_color
      )
    }
  }

  private fun setUpToolbar(): Toolbar {
    val toolbar = activity.findViewById<View>(
      R.id.profile_picture_activity_toolbar
    ) as Toolbar
    activity.setSupportActionBar(toolbar)
    toolbar.setNavigationOnClickListener {
      activity.finish()
    }
    return toolbar
  }

  private val profileLiveData: LiveData<Profile> by lazy {
    getProfileData()
  }

  private fun getProfileData(): LiveData<Profile> {
    return Transformations.map(
      profileManagementController.getProfile(profileId.toProfileIdPreservingZero()).toLiveData(),
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
