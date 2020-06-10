package org.oppia.app.administratorcontrols

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.oppia.app.administratorcontrols.administratorcontrolsitemviewmodel.AdministratorControlsAccountActionsViewModel
import org.oppia.app.administratorcontrols.administratorcontrolsitemviewmodel.AdministratorControlsAppInformationViewModel
import org.oppia.app.administratorcontrols.administratorcontrolsitemviewmodel.AdministratorControlsDownloadPermissionsViewModel
import org.oppia.app.administratorcontrols.administratorcontrolsitemviewmodel.AdministratorControlsGeneralViewModel
import org.oppia.app.administratorcontrols.administratorcontrolsitemviewmodel.AdministratorControlsItemViewModel
import org.oppia.app.administratorcontrols.administratorcontrolsitemviewmodel.AdministratorControlsProfileViewModel
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.DeviceSettings
import org.oppia.app.model.ProfileId
import org.oppia.app.viewmodel.ObservableViewModel
import org.oppia.domain.profile.ProfileManagementController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import javax.inject.Inject

/** [ViewModel] for [AdministratorControlsFragment]. */
@FragmentScope
class AdministratorControlsViewModel @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val logger: Logger,
  private val profileManagementController: ProfileManagementController
) : ObservableViewModel() {
  private val routeToProfileListListener = activity as RouteToProfileListListener
  private lateinit var userProfileId: ProfileId

  private val deviceSettingsLiveData: LiveData<DeviceSettings> by lazy {
    Transformations.map(
      profileManagementController.getDeviceSettings(),
      ::processGetDeviceSettingsResult
    )
  }

  val administratorControlsLiveData: LiveData<List<AdministratorControlsItemViewModel>> by lazy {
    Transformations.map(deviceSettingsLiveData, ::processAdministratorControlsList)
  }

  private fun processGetDeviceSettingsResult(
    deviceSettingsResult: AsyncResult<DeviceSettings>
  ): DeviceSettings {
    if (deviceSettingsResult.isFailure()) {
      logger.e(
        "AdministratorControlsFragment",
        "Failed to retrieve profile",
        deviceSettingsResult.getErrorOrNull()!!
      )
    }
    return deviceSettingsResult.getOrDefault(DeviceSettings.getDefaultInstance())
  }

  private fun processAdministratorControlsList(
    deviceSettings: DeviceSettings
  ): List<AdministratorControlsItemViewModel> {
    val itemViewModelList: MutableList<AdministratorControlsItemViewModel> = mutableListOf(
      AdministratorControlsGeneralViewModel()
    )
    itemViewModelList.add(AdministratorControlsProfileViewModel(routeToProfileListListener))
    itemViewModelList.add(
      AdministratorControlsDownloadPermissionsViewModel(
        fragment, logger, profileManagementController,
        userProfileId, deviceSettings
      )
    )
    itemViewModelList.add(AdministratorControlsAppInformationViewModel(activity))
    itemViewModelList.add(AdministratorControlsAccountActionsViewModel(fragment))

    return itemViewModelList
  }

  fun setProfileId(profileId: ProfileId) {
    userProfileId = profileId
  }
}
