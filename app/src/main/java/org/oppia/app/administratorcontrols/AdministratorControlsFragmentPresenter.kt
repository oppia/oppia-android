package org.oppia.app.administratorcontrols

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import androidx.recyclerview.widget.LinearLayoutManager
import org.oppia.app.administratorcontrols.administratorcontrolsitemviewmodel.AdministratorControlsAccountActionsViewModel
import org.oppia.app.administratorcontrols.administratorcontrolsitemviewmodel.AdministratorControlsAppInformationViewModel
import org.oppia.app.administratorcontrols.administratorcontrolsitemviewmodel.AdministratorControlsDownloadPermissionsViewModel
import org.oppia.app.administratorcontrols.administratorcontrolsitemviewmodel.AdministratorControlsGeneralViewModel
import org.oppia.app.administratorcontrols.administratorcontrolsitemviewmodel.AdministratorControlsProfileViewModel
import org.oppia.app.administratorcontrols.administratorcontrolsitemviewmodel.AdministratorControlsItemViewModel
import org.oppia.app.databinding.AdministratorControlsAccountActionsViewBinding
import org.oppia.app.databinding.AdministratorControlsAppInformationViewBinding
import org.oppia.app.databinding.AdministratorControlsDownloadPermissionsViewBinding
import org.oppia.app.databinding.AdministratorControlsFragmentBinding
import org.oppia.app.databinding.AdministratorControlsGeneralViewBinding
import org.oppia.app.databinding.AdministratorControlsProfileViewBinding
import org.oppia.app.drawer.KEY_NAVIGATION_PROFILE_ID
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.DeviceSettings
import org.oppia.app.model.ProfileId
import org.oppia.app.recyclerview.BindableAdapter
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.domain.profile.ProfileManagementController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import javax.inject.Inject

/** The presenter for [AdministratorControlsFragment]. */
@FragmentScope
class AdministratorControlsFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val logger: Logger,
  private val profileManagementController: ProfileManagementController,
  private val viewModelProvider: ViewModelProvider<AdministratorControlsViewModel>
) {
  private lateinit var binding: AdministratorControlsFragmentBinding
  private lateinit var linearLayoutManager: LinearLayoutManager
  private var internalProfileId: Int = -1
  private lateinit var profileId: ProfileId
  private lateinit var administratorControlsDownloadPermissionsViewModel: AdministratorControlsDownloadPermissionsViewModel

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    binding = AdministratorControlsFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)

    val viewModel = getAdministratorControlsViewModel()
    internalProfileId = activity.intent.getIntExtra(KEY_NAVIGATION_PROFILE_ID, -1)
    profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()
    viewModel.setProfileId(profileId)

    administratorControlsDownloadPermissionsViewModel = AdministratorControlsDownloadPermissionsViewModel(fragment, logger, profileManagementController, profileId)
    linearLayoutManager = LinearLayoutManager(activity.applicationContext)

    binding.administratorControlsList.apply {
      layoutManager = linearLayoutManager
      adapter = createRecyclerViewAdapter()
    }

    binding.apply {
      this.viewModel = viewModel
      this.lifecycleOwner = fragment
    }

    subscribeToDeviceSettingsLiveData()
    return binding.root
  }

  private fun getDeviceSettingsLiveData(): LiveData<DeviceSettings> {
    return Transformations.map(profileManagementController.getDeviceSettings(), ::processGetDeviceSettingsResult)
  }

  private fun subscribeToDeviceSettingsLiveData() {
    getDeviceSettingsLiveData().observe(fragment, Observer<DeviceSettings> {
      administratorControlsDownloadPermissionsViewModel.setTopicAutoUpdatePermission(it.automaticallyUpdateTopics)
      administratorControlsDownloadPermissionsViewModel.setTopicWifiUpdatePermission(it.allowDownloadAndUpdateOnlyOnWifi)
    })
  }

  private fun processGetDeviceSettingsResult(deviceSettingsResult: AsyncResult<DeviceSettings>): DeviceSettings {
    if (deviceSettingsResult.isFailure()) {
      logger.e("AdministratorControlsFragmentPresenter", "Failed to retrieve profile", deviceSettingsResult.getErrorOrNull()!!)
    }
    return deviceSettingsResult.getOrDefault(DeviceSettings.getDefaultInstance())
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<AdministratorControlsItemViewModel> {
    return BindableAdapter.MultiTypeBuilder
      .newBuilder<AdministratorControlsItemViewModel, ViewType> { viewModel ->
        when (viewModel) {
          is AdministratorControlsGeneralViewModel -> ViewType.VIEW_TYPE_GENERAL
          is AdministratorControlsProfileViewModel -> ViewType.VIEW_TYPE_PROFILE
          is AdministratorControlsDownloadPermissionsViewModel -> ViewType.VIEW_TYPE_DOWNLOAD_PERMISSIONS
          is AdministratorControlsAppInformationViewModel -> ViewType.VIEW_TYPE_APP_INFORMATION
          is AdministratorControlsAccountActionsViewModel -> ViewType.VIEW_TYPE_ACCOUNT_ACTIONS
          else -> throw IllegalArgumentException("Encountered unexpected view model: $viewModel")
        }
      }
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_GENERAL,
        inflateDataBinding = AdministratorControlsGeneralViewBinding::inflate,
        setViewModel = AdministratorControlsGeneralViewBinding::setViewModel,
        transformViewModel = { it as AdministratorControlsGeneralViewModel }
      )
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_PROFILE,
        inflateDataBinding = AdministratorControlsProfileViewBinding::inflate,
        setViewModel = AdministratorControlsProfileViewBinding::setViewModel,
        transformViewModel = { it as AdministratorControlsProfileViewModel }
      )
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_DOWNLOAD_PERMISSIONS,
        inflateDataBinding = AdministratorControlsDownloadPermissionsViewBinding::inflate,
        setViewModel = AdministratorControlsDownloadPermissionsViewBinding::setViewModel,
        transformViewModel = { it as AdministratorControlsDownloadPermissionsViewModel }
      )
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_APP_INFORMATION,
        inflateDataBinding = AdministratorControlsAppInformationViewBinding::inflate,
        setViewModel = AdministratorControlsAppInformationViewBinding::setViewModel,
        transformViewModel = { it as AdministratorControlsAppInformationViewModel }
      )
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_ACCOUNT_ACTIONS,
        inflateDataBinding = AdministratorControlsAccountActionsViewBinding::inflate,
        setViewModel = AdministratorControlsAccountActionsViewBinding::setViewModel,
        transformViewModel = { it as AdministratorControlsAccountActionsViewModel }
      )
      .build()
  }

  private fun getAdministratorControlsDownloadPermissionsViewModel(): AdministratorControlsDownloadPermissionsViewModel {
    return AdministratorControlsDownloadPermissionsViewModel(fragment, logger, profileManagementController, userProfileId = profileId)
  }

  private fun getAdministratorControlsViewModel(): AdministratorControlsViewModel {
    return viewModelProvider.getForFragment(fragment, AdministratorControlsViewModel::class.java)
  }

  private enum class ViewType {
    VIEW_TYPE_GENERAL,
    VIEW_TYPE_PROFILE,
    VIEW_TYPE_DOWNLOAD_PERMISSIONS,
    VIEW_TYPE_APP_INFORMATION,
    VIEW_TYPE_ACCOUNT_ACTIONS
  }
}
