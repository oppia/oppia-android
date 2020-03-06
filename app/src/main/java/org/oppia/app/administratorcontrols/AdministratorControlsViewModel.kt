package org.oppia.app.administratorcontrols

import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ObservableList
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import org.oppia.app.administratorcontrols.administratorcontrolsitemviewmodel.AdministratorControlsAccountActionsViewModel
import org.oppia.app.administratorcontrols.administratorcontrolsitemviewmodel.AdministratorControlsAppInformationViewModel
import org.oppia.app.administratorcontrols.administratorcontrolsitemviewmodel.AdministratorControlsDownloadPermissionsViewModel
import org.oppia.app.administratorcontrols.administratorcontrolsitemviewmodel.AdministratorControlsGeneralViewModel
import org.oppia.app.administratorcontrols.administratorcontrolsitemviewmodel.AdministratorControlsProfileViewModel
import org.oppia.app.administratorcontrols.administratorcontrolsitemviewmodel.AdministratorControlsItemViewModel
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.ProfileId
import org.oppia.app.viewmodel.ObservableArrayList
import org.oppia.app.viewmodel.ObservableViewModel
import org.oppia.domain.profile.ProfileManagementController
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
  private val itemViewModelList: ObservableList<AdministratorControlsItemViewModel> = ObservableArrayList()
  private val routeToProfileListListener = activity as RouteToProfileListListener
  private lateinit var userProfileId: ProfileId

  fun processAdministratorControlsList(): ObservableList<AdministratorControlsItemViewModel> {
    itemViewModelList.add(AdministratorControlsGeneralViewModel())
    itemViewModelList.add(AdministratorControlsProfileViewModel(routeToProfileListListener))
    itemViewModelList.add(AdministratorControlsDownloadPermissionsViewModel(fragment, logger, profileManagementController, userProfileId))
    itemViewModelList.add(AdministratorControlsAppInformationViewModel())
    itemViewModelList.add(AdministratorControlsAccountActionsViewModel())

    return itemViewModelList
  }

  fun setProfileId(profileId: ProfileId) {
    userProfileId = profileId
  }
}
