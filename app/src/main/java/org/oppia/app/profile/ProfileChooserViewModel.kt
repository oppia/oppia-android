package org.oppia.app.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.Profile
import org.oppia.app.model.ProfileChooserUiModel
import org.oppia.app.viewmodel.ObservableViewModel
import org.oppia.domain.profile.ProfileManagementController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import java.util.Locale
import javax.inject.Inject

/** The ViewModel for [ProfileChooserFragment]. */
@FragmentScope
class ProfileChooserViewModel @Inject constructor(
  private val profileManagementController: ProfileManagementController, private val logger: Logger
) : ObservableViewModel() {
  val profiles: LiveData<List<ProfileChooserUiModel>> by lazy {
    Transformations.map(profileManagementController.getProfiles(), ::processGetProfilesResult)
  }

  lateinit var adminPin: String

  /** Sorts profiles alphabetically by name and put Admin in front. */
  private fun processGetProfilesResult(profilesResult: AsyncResult<List<Profile>>): List<ProfileChooserUiModel> {
    if (profilesResult.isFailure()) {
      logger.e(
        "ProfileChooserViewModel",
        "Failed to retrieve the list of profiles: ",
        profilesResult.getErrorOrNull()!!
      )
    }
    val profileList = profilesResult.getOrDefault(emptyList()).map {
      ProfileChooserUiModel.newBuilder().setProfile(it).build()
    }.toMutableList()

    val sortedProfileList = profileList.sortedBy {
      it.profile.name.toLowerCase(Locale.getDefault())
    }.toMutableList()

    val adminProfile = sortedProfileList.find { it.profile.isAdmin }
    adminProfile?.let {
      sortedProfileList.remove(adminProfile)
      adminPin = it.profile.pin
      sortedProfileList.add(0, it)
    }

    if (sortedProfileList.size < 10) {
      sortedProfileList.add(ProfileChooserUiModel.newBuilder().setAddProfile(true).build())
    }

    return sortedProfileList
  }
}
