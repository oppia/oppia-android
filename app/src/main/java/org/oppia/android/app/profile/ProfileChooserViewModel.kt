package org.oppia.android.app.profile

import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.Profile
import org.oppia.android.app.model.ProfileAvatar
import org.oppia.android.app.model.ProfileChooserUiModel
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.logging.ConsoleLogger
import java.util.*
import javax.inject.Inject

/** The ViewModel for [ProfileChooserFragment]. */
@FragmentScope
class ProfileChooserViewModel @Inject constructor(
  fragment: Fragment,
  private val logger: ConsoleLogger,
  private val profileManagementController: ProfileManagementController
) : ObservableViewModel() {

  private val routeToAdminPinListener = fragment as RouteToAdminPinListener

  val profiles: LiveData<List<ProfileChooserUiModel>> by lazy {
    Transformations.map(
      profileManagementController.getProfiles().toLiveData(), ::processGetProfilesResult
    )
  }

  lateinit var adminPin: String
  lateinit var adminProfileId: ProfileId

  val usedColors = mutableListOf<Int>()

  /** Sorts profiles alphabetically by name and put Admin in front. */
  private fun processGetProfilesResult(
    profilesResult: AsyncResult<List<Profile>>
  ): List<ProfileChooserUiModel> {
    if (profilesResult.isFailure()) {
      logger.e(
        "ProfileChooserViewModel",
        "Failed to retrieve the list of profiles",
        profilesResult.getErrorOrNull()!!
      )
    }
    val profileList = profilesResult.getOrDefault(emptyList()).map {
      ProfileChooserUiModel.newBuilder().setProfile(it).build()
    }.toMutableList()

    profileList.forEach {
      if (it.profile.avatar.avatarTypeCase == ProfileAvatar.AvatarTypeCase.AVATAR_COLOR_RGB) {
        usedColors.add(it.profile.avatar.avatarColorRgb)
      }
    }

    val sortedProfileList = profileList.sortedBy {
      it.profile.name.toLowerCase(Locale.getDefault())
    }.toMutableList()

    val adminProfile = sortedProfileList.find { it.profile.isAdmin }!!

    sortedProfileList.remove(adminProfile)
    adminPin = adminProfile.profile.pin
    adminProfileId = adminProfile.profile.id
    sortedProfileList.add(0, adminProfile)

    if (sortedProfileList.size < 10) {
      sortedProfileList.add(ProfileChooserUiModel.newBuilder().setAddProfile(true).build())
    }

    return sortedProfileList
  }

  fun onAdministratorControlsButtonClicked() {
    routeToAdminPinListener.routeToAdminPin()
  }
}
