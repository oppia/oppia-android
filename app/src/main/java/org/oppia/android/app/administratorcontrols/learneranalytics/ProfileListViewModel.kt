package org.oppia.android.app.administratorcontrols.learneranalytics

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.android.app.model.Profile
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject

/**
 * [ObservableViewModel] which represents a list of profiles on a particular device that may have
 * learner analytics events logged for them.
 *
 * Elements present in the list managed by this view model correspond to [ProfileListItemViewModel]
 * and are intended to show analytics information corresponding to the device or a specific profile
 * to help user study facilitators who need to correspond events to a particular participant.
 */
class ProfileListViewModel private constructor(
  private val profileManagementController: ProfileManagementController,
  private val oppiaLogger: OppiaLogger,
  private val deviceIdItemViewModelFactory: DeviceIdItemViewModel.Factory,
  private val syncStatusItemViewModelFactory: SyncStatusItemViewModel.Factory,
  private val profileLearnerIdItemViewModelFactory: ProfileLearnerIdItemViewModel.Factory
) : ObservableViewModel() {
  /** The list of [ProfileListItemViewModel] to display. */
  val profileModels: LiveData<List<ProfileListItemViewModel>> by lazy {
    Transformations.map(
      profileManagementController.getProfiles().toLiveData(), ::processProfiles
    )
  }

  private fun processProfiles(
    profilesResult: AsyncResult<List<Profile>>
  ): List<ProfileListItemViewModel> {
    val deviceIdViewModel = deviceIdItemViewModelFactory.create()
    val syncStatusViewModel = syncStatusItemViewModelFactory.create()

    val learnerIdModels = when (profilesResult) {
      is AsyncResult.Pending -> listOf()
      is AsyncResult.Failure -> {
        oppiaLogger.e(
          "ProfileAndDeviceIdViewModel",
          "Failed to retrieve the list of profiles",
          profilesResult.error
        )
        listOf()
      }
      is AsyncResult.Success -> {
        // Ensure that admins are listed first, then profiles should be sorted by name.
        profilesResult.value
          .sortedWith(compareByDescending(Profile::getIsAdmin).thenBy(Profile::getName))
          .map(profileLearnerIdItemViewModelFactory::create)
      }
    }

    return listOf(deviceIdViewModel) + learnerIdModels + listOf(syncStatusViewModel)
  }

  /**
   * [ObservableViewModel] that represents an element in the profile list shown by
   * [ProfileListViewModel].
   *
   * See this class's subclasses for the specific view models that may show content the list.
   *
   * @property viewType the [ProfileListItemViewType] corresponding to this model.
   */
  abstract class ProfileListItemViewModel(
    val viewType: ProfileListItemViewType
  ) : ObservableViewModel()

  /** Represents the different types of views that may be shown by [ProfileListViewModel]. */
  enum class ProfileListItemViewType {
    /** Corresponds to [DeviceIdItemViewModel]. */
    DEVICE_ID,

    /** Corresponds to [ProfileLearnerIdItemViewModel]. */
    LEARNER_ID,

    /** Corresponds to [SyncStatusItemViewModel]. */
    SYNC_STATUS
  }

  /** Factory to create new [ProfileListViewModel]s. */
  class Factory @Inject constructor(
    private val profileManagementController: ProfileManagementController,
    private val oppiaLogger: OppiaLogger,
    private val deviceIdItemViewModelFactory: DeviceIdItemViewModel.Factory,
    private val syncStatusItemViewModelFactory: SyncStatusItemViewModel.Factory,
    private val profileLearnerIdItemViewModelFactory: ProfileLearnerIdItemViewModel.Factory
  ) {
    /** Returns a new [ProfileListViewModel]. */
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
