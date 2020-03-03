package org.oppia.app.administratorcontrols

import androidx.databinding.ObservableList
import androidx.lifecycle.ViewModel
import org.oppia.app.administratorcontrols.administratorcontrolsitemviewmodel.AdministratorControlsAccountActionsViewModel
import org.oppia.app.administratorcontrols.administratorcontrolsitemviewmodel.AdministratorControlsAppInformationViewModel
import org.oppia.app.administratorcontrols.administratorcontrolsitemviewmodel.AdministratorControlsDownloadPermissionsViewModel
import org.oppia.app.administratorcontrols.administratorcontrolsitemviewmodel.AdministratorControlsGeneralViewModel
import org.oppia.app.administratorcontrols.administratorcontrolsitemviewmodel.AdministratorControlsProfileViewModel
import org.oppia.app.administratorcontrols.administratorcontrolsitemviewmodel.AdministratorControlsItemViewModel
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.viewmodel.ObservableArrayList
import org.oppia.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/** [ViewModel] for [AdministratorControlsFragment]. */
@FragmentScope
class AdministratorControlsViewModel @Inject constructor() : ObservableViewModel() {
  private val itemViewModelList: ObservableList<AdministratorControlsItemViewModel> = ObservableArrayList()

  fun processAdministratorControlsList(): ObservableList<AdministratorControlsItemViewModel> {
    itemViewModelList.add(AdministratorControlsGeneralViewModel())
    itemViewModelList.add(AdministratorControlsProfileViewModel())
    itemViewModelList.add(AdministratorControlsDownloadPermissionsViewModel())
    itemViewModelList.add(AdministratorControlsAppInformationViewModel())
    itemViewModelList.add(AdministratorControlsAccountActionsViewModel())

    return itemViewModelList
  }
}
