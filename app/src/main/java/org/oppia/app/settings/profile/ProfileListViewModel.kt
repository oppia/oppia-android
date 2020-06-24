package org.oppia.app.settings.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.app.activity.ActivityScope
import org.oppia.app.model.Profile
import org.oppia.app.viewmodel.ObservableViewModel
import org.oppia.domain.profile.ProfileManagementController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import java.util.Locale
import javax.inject.Inject

/** The ViewModel for [ProfileListActivity]. */
@ActivityScope
class ProfileListViewModel @Inject constructor(
  private val logger: Logger,
  private val profileManagementController: ProfileManagementController
) : ObservableViewModel() {
  val profiles: LiveData<List<Profile>> by lazy {
    Transformations.map(profileManagementController.getProfiles(), ::processGetProfilesResult)
  }

  private fun processGetProfilesResult(profilesResult: AsyncResult<List<Profile>>): List<Profile> {
    if (profilesResult.isFailure()) {
      logger.e(
        "ProfileListViewModel",
        "Failed to retrieve the list of profiles",
        profilesResult.getErrorOrNull()!!
      )
    }
    val profileList = profilesResult.getOrDefault(emptyList())

    val sortedProfileList = profileList.sortedBy {
      it.name.toLowerCase(Locale.getDefault())
    }.toMutableList()

    val adminProfile = sortedProfileList.find { it.isAdmin }

    adminProfile?.let {
      sortedProfileList.remove(adminProfile)
      sortedProfileList.add(0, it)
    }

    return sortedProfileList
  }
}
