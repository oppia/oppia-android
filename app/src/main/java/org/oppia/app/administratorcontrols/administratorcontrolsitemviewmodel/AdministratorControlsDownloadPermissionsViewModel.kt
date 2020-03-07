package org.oppia.app.administratorcontrols.administratorcontrolsitemviewmodel

import androidx.databinding.ObservableField
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import org.oppia.app.model.ProfileId
import org.oppia.domain.profile.ProfileManagementController
import org.oppia.util.logging.Logger

/** [ViewModel] for the recycler view in [AdministratorControlsFragment]. */
class AdministratorControlsDownloadPermissionsViewModel(
  private val fragment: Fragment,
  private val logger: Logger,
  private val profileManagementController: ProfileManagementController,
  private val userProfileId: ProfileId
) : AdministratorControlsItemViewModel() {

  var isTopicWifiUpdatePermission = ObservableField<Boolean>(false)
  var isTopicAutoUpdatePermission = ObservableField<Boolean>(false)

  fun setTopicWifiUpdatePermission(isEnabled: Boolean) {
    isTopicWifiUpdatePermission.set(isEnabled)
  }

  fun setTopicAutoUpdatePermission(isEnabled: Boolean) {
    isTopicAutoUpdatePermission.set(isEnabled)
  }

  fun onTopicWifiUpdatePermissionChanged(checked: Boolean) {
    setTopicWifiUpdatePermission(checked)
    profileManagementController.updateWifiPermissionDeviceSettings(userProfileId, checked).observe(fragment, Observer {
      if (it.isFailure()) {
        logger.e("AdministratorControlsDownloadPermissionsViewModel", "Failed to update topic update on wifi permission", it.getErrorOrNull()!!)
      }
    })
  }

  fun onTopicAutoUpdatePermissionChanged(checked: Boolean) {
    setTopicAutoUpdatePermission(checked)
    profileManagementController.updateTopicAutomaticallyPermissionDeviceSettings(userProfileId, checked).observe(fragment, Observer {
      if (it.isFailure()) {
        logger.e("AdministratorControlsDownloadPermissionsViewModel", "Failed to update topic auto update permission", it.getErrorOrNull()!!)
      }
    })
  }
}
