package org.oppia.app.administratorcontrols

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import org.oppia.app.administratorcontrols.administratorcontrolsitemviewmodel.AdministratorControlsApplicationSettingsViewModel
import org.oppia.app.administratorcontrols.administratorcontrolsitemviewmodel.AdministratorControlsDownloadPermissionsViewModel
import org.oppia.app.administratorcontrols.administratorcontrolsitemviewmodel.AdministratorControlsGeneralProfileViewModel
import org.oppia.app.administratorcontrols.administratorcontrolsitemviewmodel.AdministratorControlsItemViewModel
import org.oppia.app.databinding.AdministratorControlsApplicationSettingsViewBinding
import org.oppia.app.databinding.AdministratorControlsDownloadPermissionsViewBinding
import org.oppia.app.databinding.AdministratorControlsFragmentBinding
import org.oppia.app.databinding.AdministratorControlsGeneralProfileViewBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.recyclerview.BindableAdapter
import org.oppia.app.viewmodel.ViewModelProvider
import javax.inject.Inject

/** The presenter for [AdministratorControlsFragment]. */
@FragmentScope
class AdministratorControlsFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment, private val viewModelProvider: ViewModelProvider<AdministratorControlsViewModel>
) {
  private lateinit var binding: AdministratorControlsFragmentBinding
  private lateinit var linearLayoutManager: LinearLayoutManager

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    binding = AdministratorControlsFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)

    val viewModel = getAdministratorControlsItemViewModel()
    linearLayoutManager = LinearLayoutManager(activity.applicationContext)

    binding.administratorControlsList.apply {
      layoutManager = linearLayoutManager
      adapter = createRecyclerViewAdapter()
    }

    binding.apply {
      this.viewModel = getAdministratorControlsItemViewModel()
      this.lifecycleOwner = fragment
    }
    return binding.root
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<AdministratorControlsItemViewModel> {
    return BindableAdapter.MultiTypeBuilder
      .newBuilder<AdministratorControlsItemViewModel, ViewType> { viewModel ->
        when (viewModel) {
          is AdministratorControlsGeneralProfileViewModel -> ViewType.VIEW_TYPE_GENERAL_PROFILE
          is AdministratorControlsDownloadPermissionsViewModel -> ViewType.VIEW_TYPE_DOWNLOAD_PERMISSIONS
          is AdministratorControlsApplicationSettingsViewModel -> ViewType.VIEW_TYPE_APPLICATION_SETTINGS
          else -> throw IllegalArgumentException("Encountered unexpected view model: $viewModel")
        }
      }
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_GENERAL_PROFILE,
        inflateDataBinding = AdministratorControlsGeneralProfileViewBinding::inflate,
        setViewModel = AdministratorControlsGeneralProfileViewBinding::setViewModel,
        transformViewModel = { it as AdministratorControlsGeneralProfileViewModel }
      )
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_DOWNLOAD_PERMISSIONS,
        inflateDataBinding = AdministratorControlsDownloadPermissionsViewBinding::inflate,
        setViewModel = AdministratorControlsDownloadPermissionsViewBinding::setViewModel,
        transformViewModel = { it as AdministratorControlsDownloadPermissionsViewModel }
      )
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_APPLICATION_SETTINGS,
        inflateDataBinding = AdministratorControlsApplicationSettingsViewBinding::inflate,
        setViewModel = AdministratorControlsApplicationSettingsViewBinding::setViewModel,
        transformViewModel = { it as AdministratorControlsApplicationSettingsViewModel }
      )
      .build()
  }

  private fun getAdministratorControlsItemViewModel(): AdministratorControlsViewModel {
    return viewModelProvider.getForFragment(fragment, AdministratorControlsViewModel::class.java)
  }

  private enum class ViewType {
    VIEW_TYPE_GENERAL_PROFILE,
    VIEW_TYPE_DOWNLOAD_PERMISSIONS,
    VIEW_TYPE_APPLICATION_SETTINGS
  }
}
