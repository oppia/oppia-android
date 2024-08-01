package org.oppia.android.app.profile

import androidx.databinding.ObservableField
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.Profile
import org.oppia.android.app.model.ProfileAvatar
import org.oppia.android.app.model.ProfileChooserUiModel
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.locale.OppiaLocale
import org.oppia.android.util.platformparameter.EnableOnboardingFlowV2
import org.oppia.android.util.platformparameter.PlatformParameterValue
import javax.inject.Inject

/** The ViewModel for [ProfileActionChooserFragment]. */
@FragmentScope
class ProfileChooserViewModel @Inject constructor(
  fragment: Fragment,
  private val oppiaLogger: OppiaLogger,
  private val profileManagementController: ProfileManagementController,
  private val machineLocale: OppiaLocale.MachineLocale,
  @EnableOnboardingFlowV2 private val enableOnboardingFlowV2: PlatformParameterValue<Boolean>
) : ObservableViewModel() {

  private val routeToAdminPinListener = fragment as RouteToAdminPinListener
  private val profileClickListener = fragment as ProfileClickListener

  /** Observable field to track if the add profile button should be shown. */
  val canAddProfile = ObservableField(true)

  /** Livedata representing the list of profiles on the app, and a model for the 'add' view. */
  val profiles: LiveData<List<ProfileChooserUiModel>> by lazy {
    Transformations.map(
      profileManagementController.getProfiles().toLiveData(), ::processGetProfilesResult
    )
  }

  /** Livedata representing the list of profiles on the app, to be bound to the recyclerview. */
  val profilesList: LiveData<List<ProfileItemViewModel>> by lazy {
    Transformations.map(
      profileManagementController.getProfiles().toLiveData(), ::retrieveProfiles
    )
  }

  private fun retrieveProfiles(profilesResult: AsyncResult<List<Profile>>):
    List<ProfileItemViewModel> {
      val profileList = when (profilesResult) {
        is AsyncResult.Failure -> {
          oppiaLogger.e(
            "ProfileChooserViewModel",
            "Failed to retrieve the list of profiles", profilesResult.error
          )
          emptyList()
        }
        is AsyncResult.Pending -> emptyList()
        is AsyncResult.Success -> profilesResult.value
      }.map {
        ProfileItemViewModel(it, profileClickListener::onProfileClicked)
      }

      profileList.forEach { profileItemViewModel ->
        if (profileItemViewModel.profile.avatar.avatarTypeCase
          == ProfileAvatar.AvatarTypeCase.AVATAR_COLOR_RGB
        ) {
          usedColors.add(profileItemViewModel.profile.avatar.avatarColorRgb)
        }
      }

      val sortedProfileList = profileList.sortedBy { profileItemViewModel ->
        machineLocale.run { profileItemViewModel.profile.name.toMachineLowerCase() }
      }.toMutableList()

      val adminProfileViewModel = sortedProfileList.find { it.profile.isAdmin } ?: return listOf()

      sortedProfileList.remove(adminProfileViewModel)
      adminPin = adminProfileViewModel.profile.pin
      adminProfileId = adminProfileViewModel.profile.id
      sortedProfileList.add(0, adminProfileViewModel)

      if (sortedProfileList.size == 10) {
        canAddProfile.set(false)
      }
      return sortedProfileList
    }

  /** The admin profile's PIN. */
  lateinit var adminPin: String

  /** The [ProfileId] of the admin profile. */
  lateinit var adminProfileId: ProfileId

  /** List of RGB colors that have already been assigned to a profile. */
  val usedColors = mutableListOf<Int>()

  /** Sorts profiles alphabetically by name and put Admin in front. */
  private fun processGetProfilesResult(
    profilesResult: AsyncResult<List<Profile>>
  ): List<ProfileChooserUiModel> {
    val profileList = when (profilesResult) {
      is AsyncResult.Failure -> {
        oppiaLogger.e(
          "ProfileChooserViewModel", "Failed to retrieve the list of profiles", profilesResult.error
        )
        emptyList()
      }
      is AsyncResult.Pending -> emptyList()
      is AsyncResult.Success -> profilesResult.value
    }.map {
      ProfileChooserUiModel.newBuilder().setProfile(it).build()
    }.toMutableList()

    profileList.forEach {
      if (it.profile.avatar.avatarTypeCase == ProfileAvatar.AvatarTypeCase.AVATAR_COLOR_RGB) {
        usedColors.add(it.profile.avatar.avatarColorRgb)
      }
    }

    val sortedProfileList = profileList.sortedBy {
      machineLocale.run { it.profile.name.toMachineLowerCase() }
    }.toMutableList()

    val adminProfile = sortedProfileList.find { it.profile.isAdmin } ?: return listOf()

    sortedProfileList.remove(adminProfile)
    adminPin = adminProfile.profile.pin
    adminProfileId = adminProfile.profile.id
    sortedProfileList.add(0, adminProfile)

    if (sortedProfileList.size < 10) {
      sortedProfileList.add(ProfileChooserUiModel.newBuilder().setAddProfile(true).build())
    }

    return sortedProfileList
  }

  /** Handles click events for the administrator controls button. */
  fun onAdministratorControlsButtonClicked() {
    routeToAdminPinListener.routeToAdminPin()
  }
}
