package org.oppia.android.app.administratorcontrols.administratorcontrolsitemviewmodel

import androidx.databinding.ObservableField
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
<<<<<<< HEAD:app/src/main/java/org/oppia/android/app/administratorcontrols/administratorcontrolsitemviewmodel/AdministratorControlsDownloadPermissionsViewModel.kt
import org.oppia.android.app.model.DeviceSettings
import org.oppia.android.app.model.ProfileId
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.logging.ConsoleLogger
=======
import org.oppia.app.model.DeviceSettings
import org.oppia.app.model.ProfileId
import org.oppia.domain.profile.ProfileManagementController
import org.oppia.util.data.DataProviders.Companion.toLiveData
import org.oppia.util.logging.ConsoleLogger
>>>>>>> develop:app/src/main/java/org/oppia/app/administratorcontrols/administratorcontrolsitemviewmodel/AdministratorControlsDownloadPermissionsViewModel.kt

/** [ViewModel] for the recycler view in [AdministratorControlsFragment]. */
class AdministratorControlsDownloadPermissionsViewModel(
  private val fragment: Fragment,
  private val logger: ConsoleLogger,
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
            logger.e(
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
          logger.e(
            "AdministratorControlsFragment",
            "Failed to update topic auto update permission",
            it.getErrorOrNull()!!
          )
        }
      }
    )
  }
}
