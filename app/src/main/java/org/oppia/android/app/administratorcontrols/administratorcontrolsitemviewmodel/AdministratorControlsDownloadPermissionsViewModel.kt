package org.oppia.android.app.administratorcontrols.administratorcontrolsitemviewmodel

import androidx.databinding.ObservableField
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import org.oppia.android.app.model.DeviceSettings
import org.oppia.android.app.model.ProfileId
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData

/** [ViewModel] for the recycler view in [AdministratorControlsFragment]. */
class AdministratorControlsDownloadPermissionsViewModel(
  private val fragment: Fragment,
  private val oppiaLogger: OppiaLogger,
  private val profileManagementController: ProfileManagementController,
  private val userProfileId: ProfileId,
  deviceSettings: DeviceSettings
) : AdministratorControlsItemViewModel() {

  val isTopicWifiUpdatePermission =
    ObservableField<Boolean>(deviceSettings.allowDownloadAndUpdateOnlyOnWifi)
  val isTopicAutoUpdatePermission =
    ObservableField<Boolean>(deviceSettings.automaticallyUpdateTopics)

  fun onTopicWifiUpdatePermissionChanged() {
    profileManagementController.updateWifiPermissionDeviceSettings(
      userProfileId,
      !isTopicWifiUpdatePermission.get()!!
    ).toLiveData()
      .observe(
        fragment,
        Observer {
          if (it is AsyncResult.Failure) {
            oppiaLogger.e(
              "AdministratorControlsFragment",
              "Failed to update topic update on wifi permission",
              it.error
            )
          }
        }
      )
  }

  fun onTopicAutoUpdatePermissionChanged() {
    profileManagementController.updateTopicAutomaticallyPermissionDeviceSettings(
      userProfileId,
      !isTopicAutoUpdatePermission.get()!!
    ).toLiveData().observe(
      fragment,
      Observer {
        if (it is AsyncResult.Failure) {
          oppiaLogger.e(
            "AdministratorControlsFragment",
            "Failed to update topic auto update permission",
            it.error
          )
        }
      }
    )
  }
}
