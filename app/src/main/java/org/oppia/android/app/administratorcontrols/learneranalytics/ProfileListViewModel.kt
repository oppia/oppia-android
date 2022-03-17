package org.oppia.android.app.administratorcontrols.learneranalytics

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import javax.inject.Inject
import org.oppia.android.app.model.Profile
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData

class ProfileListViewModel private constructor(
  private val profileManagementController: ProfileManagementController,
  private val oppiaLogger: OppiaLogger,
  private val deviceIdItemViewModelFactory: DeviceIdItemViewModel.Factory,
  private val syncStatusItemViewModelFactory: SyncStatusItemViewModel.Factory,
  private val profileLearnerIdItemViewModelFactory: ProfileLearnerIdItemViewModel.Factory
) : ObservableViewModel() {
  val profileModels: LiveData<List<ProfileListItemViewModel>> by lazy {
    Transformations.map(
      profileManagementController.getProfiles().toLiveData(), ::processProfiles
    )
  }

  private fun processProfiles(
    profiles: AsyncResult<List<Profile>>
  ): List<ProfileListItemViewModel> {
    if (profiles.isFailure()) {
      oppiaLogger.e(
        "ProfileAndDeviceIdViewModel",
        "Failed to retrieve the list of profiles",
        profiles.getErrorOrNull()!!
      )
    }

    val deviceIdViewModel = deviceIdItemViewModelFactory.create()
    val syncStatusViewModel = syncStatusItemViewModelFactory.create()

    // Ensure that admins are listed first, then profiles should be sorted by name.
    val learnerIdModels = profiles.getOrDefault(emptyList())
      .sortedWith(compareByDescending(Profile::getIsAdmin).thenBy(Profile::getName))
      .map(profileLearnerIdItemViewModelFactory::create)

    return listOf(deviceIdViewModel) + learnerIdModels + listOf(syncStatusViewModel)
  }

  abstract class ProfileListItemViewModel(
    val viewType: ProfileListItemViewType
  ): ObservableViewModel()

  enum class ProfileListItemViewType {
    DEVICE_ID,
    LEARNER_ID,
    SYNC_STATUS
  }

  class Factory @Inject constructor(
    private val profileManagementController: ProfileManagementController,
    private val oppiaLogger: OppiaLogger,
    private val deviceIdItemViewModelFactory: DeviceIdItemViewModel.Factory,
    private val syncStatusItemViewModelFactory: SyncStatusItemViewModel.Factory,
    private val profileLearnerIdItemViewModelFactory: ProfileLearnerIdItemViewModel.Factory
  ) {
    fun create(): ProfileListViewModel {
      return ProfileListViewModel(
        profileManagementController,
        oppiaLogger,
        deviceIdItemViewModelFactory,
        syncStatusItemViewModelFactory,
        profileLearnerIdItemViewModelFactory
      )
    }
  }
}
