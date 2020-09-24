package org.oppia.android.app.settings.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.model.Profile
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.logging.ConsoleLogger
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.model.Profile
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.logging.ConsoleLogger
import java.util.Locale
import javax.inject.Inject

/** The ViewModel for [ProfileListActivity]. */
@ActivityScope
class ProfileListViewModel @Inject constructor(
  private val logger: ConsoleLogger,
  private val profileManagementController: ProfileManagementController
) : ObservableViewModel() {
  val profiles: LiveData<List<Profile>> by lazy {
    Transformations.map(
      profileManagementController.getProfiles().toLiveData(), ::processGetProfilesResult
    )
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
