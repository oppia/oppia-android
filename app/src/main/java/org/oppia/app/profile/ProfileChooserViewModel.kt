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

  var adminPin = ""

  private fun processGetProfilesResult(profilesResult: AsyncResult<List<Profile>>): List<ProfileChooserModel> {
    if (profilesResult.isFailure()) {
      logger.e("ProfileChooserViewModel", "Failed to retrieve the list of profiles: ", profilesResult.getErrorOrNull()!!)
    }
    val profileList = profilesResult.getOrDefault(emptyList()).map {
      if (it.isAdmin) {
        adminPin = it.pin
      }
      ProfileChooserModel.newBuilder().setProfile(it).build()
    }
    if (profileList.size < 10) {
      val mutableProfileList = profileList.toMutableList()
      mutableProfileList.add(ProfileChooserModel.newBuilder().setAddProfile(true).build())
      return mutableProfileList
    }
    return profileList
  }
}
