package org.oppia.app.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.Profile
import org.oppia.app.model.ProfileChooserModel
import org.oppia.app.viewmodel.ObservableViewModel
import org.oppia.domain.profile.ProfileManagementController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import javax.inject.Inject

/** The ViewModel for [ProfileChooserFragment]. */
@FragmentScope
class ProfileChooserViewModel @Inject constructor(
  private val profileManagementController: ProfileManagementController, private val logger: Logger
): ObservableViewModel() {
  val profiles: LiveData<List<ProfileChooserModel>> by lazy {
    Transformations.map(profileManagementController.getProfiles(), ::processGetProfilesResult)
  }

  lateinit var adminPin: String

  private fun processGetProfilesResult(profilesResult: AsyncResult<List<Profile>>): List<ProfileChooserModel> {
    if (profilesResult.isFailure()) {
      logger.e("ProfileChooserViewModel", "Failed to retrieve the list of profiles: ", profilesResult.getErrorOrNull()!!)
    }
    val profileList = profilesResult.getOrDefault(emptyList()).map {
      ProfileChooserModel.newBuilder().setProfile(it).build()
    }.toMutableList()

    val sortedProfileList = profileList.sortedByDescending {
      it.profile.lastLoggedInTimestampMs
    }.toMutableList()

    val adminProfile = sortedProfileList.find { it.profile.isAdmin }
    adminProfile?.let {
      sortedProfileList.remove(adminProfile)
      adminPin = it.profile.pin
      sortedProfileList.add(0, it)
    }

    if (sortedProfileList.size < 10) {
      sortedProfileList.add(ProfileChooserModel.newBuilder().setAddProfile(true).build())
    }

    return sortedProfileList
  }
}
