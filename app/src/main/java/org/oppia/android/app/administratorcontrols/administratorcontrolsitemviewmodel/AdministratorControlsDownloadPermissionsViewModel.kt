package org.oppia.android.app.administratorcontrols.administratorcontrolsitemviewmodel

import androidx.databinding.ObservableField
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import org.oppia.android.app.model.DeviceSettings
import org.oppia.android.app.model.ProfileId
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.data.DataProviders.Companion.toLiveData

/** [ViewModel] for the recycler view in [AdministratorControlsFragment]. */
class AdministratorControlsDownloadPermissionsViewModel(
  private val fragment: Fragment,
  private val oppiaLogger: OppiaLogger,
  private val profileManagementController: ProfileManagementController,
  private val userProfileId: ProfileId,
  deviceSettings: DeviceSettings
) : AdministratorControlsItemViewModel() {

  var isTopicWifiUpdatePermission =
    ObservableField<Boolean>(deviceSettings.allowDownloadAndUpdateOnlyOnWifi)
  var isTopicAutoUpdatePermission =
    ObservableField<Boolean>(deviceSettings.automaticallyUpdateTopics)

  fun onTopicWifiUpdatePermissionChanged(checked: Boolean) {
    profileManagementController.updateWifiPermissionDeviceSettings(userProfileId, checked)
      .toLiveData()
      .observe(
        fragment,
        Observer {
          if (it.isFailure()) {
            oppiaLogger.e(
              "AdministratorControlsFragment",
              "Failed to update topic update on wifi permission",
              it.getErrorOrNull()!!
            )
          }
        }
      )
  }

  fun onTopicAutoUpdatePermissionChanged(checked: Boolean) {
    profileManagementController.updateTopicAutomaticallyPermissionDeviceSettings(
      userProfileId,
      checked
    ).toLiveData().observe(
      fragment,
      Observer {
        if (it.isFailure()) {
          oppiaLogger.e(
            "AdministratorControlsFragment",
            "Failed to update topic auto update permission",
            it.getErrorOrNull()!!
          )
        }
      }
    )
  }
}
