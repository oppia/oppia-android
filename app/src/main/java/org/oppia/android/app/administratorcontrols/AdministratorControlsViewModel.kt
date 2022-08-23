package org.oppia.android.app.administratorcontrols

import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ObservableField
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.oppia.android.app.administratorcontrols.administratorcontrolsitemviewmodel.AdministratorControlsAccountActionsViewModel
import org.oppia.android.app.administratorcontrols.administratorcontrolsitemviewmodel.AdministratorControlsAppInformationViewModel
import org.oppia.android.app.administratorcontrols.administratorcontrolsitemviewmodel.AdministratorControlsDownloadPermissionsViewModel
import org.oppia.android.app.administratorcontrols.administratorcontrolsitemviewmodel.AdministratorControlsGeneralViewModel
import org.oppia.android.app.administratorcontrols.administratorcontrolsitemviewmodel.AdministratorControlsItemViewModel
import org.oppia.android.app.administratorcontrols.administratorcontrolsitemviewmodel.AdministratorControlsProfileAndDeviceIdViewModel
import org.oppia.android.app.administratorcontrols.administratorcontrolsitemviewmodel.AdministratorControlsProfileViewModel
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.DeviceSettings
import org.oppia.android.app.model.ProfileId
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.platformparameter.AutomaticallyUpdateTopic
import org.oppia.android.util.platformparameter.EnableEditAccountsOptionsUi
import org.oppia.android.util.platformparameter.LearnerStudyAnalytics
import org.oppia.android.util.platformparameter.PlatformParameterValue
import javax.inject.Inject

/** [ViewModel] for [AdministratorControlsFragment]. */
@FragmentScope
class AdministratorControlsViewModel @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val oppiaLogger: OppiaLogger,
  private val profileManagementController: ProfileManagementController,

  @LearnerStudyAnalytics private val learnerStudyAnalytics: PlatformParameterValue<Boolean>,
  @AutomaticallyUpdateTopic private val automaticallyUpdateTopic: PlatformParameterValue<Boolean>

  @EnableEditAccountsOptionsUi
  private val enableEditAccountsOptionsUi: PlatformParameterValue<Boolean>,
  @LearnerStudyAnalytics private val learnerStudyAnalytics: PlatformParameterValue<Boolean>

) {
  private val routeToProfileListListener = activity as RouteToProfileListListener
  private val loadProfileListListener = activity as LoadProfileListListener
  private val showLogoutDialogListener = activity as ShowLogoutDialogListener
  private lateinit var userProfileId: ProfileId

  /** Sets the index for the currently selected fragment. */
  val selectedFragmentIndex = ObservableField<Int>(1)

  private val deviceSettingsLiveData: LiveData<DeviceSettings> by lazy {
    Transformations.map(
      profileManagementController.getDeviceSettings().toLiveData(),
      ::processGetDeviceSettingsResult
    )
  }

  /** This temporarily stores the list of the controls in the [AdministratorControlsFragment]. */
  val administratorControlsLiveData: LiveData<List<AdministratorControlsItemViewModel>> by lazy {
    Transformations.map(deviceSettingsLiveData, ::processAdministratorControlsList)
  }

  private fun processGetDeviceSettingsResult(
    deviceSettingsResult: AsyncResult<DeviceSettings>
  ): DeviceSettings {
    return when (deviceSettingsResult) {
      is AsyncResult.Failure -> {
        oppiaLogger.e(
          "AdministratorControlsFragment", "Failed to retrieve profile", deviceSettingsResult.error
        )
        DeviceSettings.getDefaultInstance()
      }
      is AsyncResult.Pending -> DeviceSettings.getDefaultInstance()
      is AsyncResult.Success -> deviceSettingsResult.value
    }
  }

  private fun processAdministratorControlsList(
    deviceSettings: DeviceSettings
  ): List<AdministratorControlsItemViewModel> {

    val itemViewModelList = mutableListOf<AdministratorControlsItemViewModel>()

    if (enableEditAccountsOptionsUi.value) {
      itemViewModelList.add(AdministratorControlsGeneralViewModel())
    }

    itemViewModelList.add(
      AdministratorControlsProfileViewModel(
        routeToProfileListListener,
        loadProfileListListener
      )
    )
    // TODO(#4345): Add tests to verify this behavior both for the study flag being on & off.
    if (learnerStudyAnalytics.value) {
      itemViewModelList.add(AdministratorControlsProfileAndDeviceIdViewModel(activity))
    }

    itemViewModelList.add(
      AdministratorControlsDownloadPermissionsViewModel(
        fragment,
        oppiaLogger,
        profileManagementController,
        userProfileId,
        deviceSettings,
        automaticallyUpdateTopic.value
      )
    )

    itemViewModelList.add(AdministratorControlsAppInformationViewModel(activity))
    itemViewModelList.add(
      AdministratorControlsAccountActionsViewModel(
        showLogoutDialogListener
      )
    )

    return itemViewModelList
  }

  /** Sets the user profile id. */
  fun setProfileId(profileId: ProfileId) {
    userProfileId = profileId
  }
}
