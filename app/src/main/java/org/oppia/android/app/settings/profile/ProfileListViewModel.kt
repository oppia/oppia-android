package org.oppia.android.app.settings.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.model.Profile
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.locale.OppiaLocale
import javax.inject.Inject

/** The ViewModel for [ProfileListActivity]. */
@ActivityScope
class ProfileListViewModel @Inject constructor(
  private val oppiaLogger: OppiaLogger,
  private val profileManagementController: ProfileManagementController,
  private val machineLocale: OppiaLocale.MachineLocale
) : ObservableViewModel() {

  /** The list of the current profiles registered in the app [ProifleListFragment]. */
  val profiles: LiveData<List<Profile>> by lazy {
    Transformations.map(
      profileManagementController.getProfiles().toLiveData(), ::processGetProfilesResult
    )
  }

  private fun processGetProfilesResult(profilesResult: AsyncResult<List<Profile>>): List<Profile> {
    val profileList = when (profilesResult) {
      is AsyncResult.Failure -> {
        oppiaLogger.e(
          "ProfileListViewModel", "Failed to retrieve the list of profiles", profilesResult.error
        )
        emptyList()
      }
      is AsyncResult.Pending -> emptyList()
      is AsyncResult.Success -> profilesResult.value
    }

    val sortedProfileList = profileList.sortedBy {
      machineLocale.run { it.name.toMachineLowerCase() }
    }.toMutableList()

    val adminProfile = sortedProfileList.find { it.isAdmin }

    adminProfile?.let {
      sortedProfileList.remove(adminProfile)
      sortedProfileList.add(0, it)
    }

    return sortedProfileList
  }
}
