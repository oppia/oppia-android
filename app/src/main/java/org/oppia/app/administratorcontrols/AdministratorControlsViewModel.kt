package org.oppia.app.administratorcontrols

import androidx.databinding.ObservableList
import androidx.lifecycle.ViewModel
import org.oppia.app.administratorcontrols.administratorcontrolsitemviewmodel.AdministratorControlsApplicationSettingsViewModel
import org.oppia.app.administratorcontrols.administratorcontrolsitemviewmodel.AdministratorControlsDownloadPermissionsViewModel
import org.oppia.app.administratorcontrols.administratorcontrolsitemviewmodel.AdministratorControlsGeneralProfileViewModel
import org.oppia.app.administratorcontrols.administratorcontrolsitemviewmodel.AdministratorControlsItemViewModel
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.viewmodel.ObservableArrayList
import javax.inject.Inject

/** [ViewModel] for [AdministratorControlsFragment]. */
@FragmentScope
class AdministratorControlsViewModel @Inject constructor() : ViewModel() {
  private val itemViewModelList: ObservableList<AdministratorControlsItemViewModel> = ObservableArrayList()

  fun processAdministratorControlsList(): ObservableList<AdministratorControlsItemViewModel> {
    itemViewModelList.add(AdministratorControlsGeneralProfileViewModel())

    itemViewModelList.add(AdministratorControlsDownloadPermissionsViewModel())

    itemViewModelList.add(AdministratorControlsApplicationSettingsViewModel())

    return itemViewModelList
  }
}
