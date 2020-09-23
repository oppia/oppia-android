package org.oppia.android.app.administratorcontrols

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import org.oppia.android.app.administratorcontrols.administratorcontrolsitemviewmodel.AdministratorControlsAccountActionsViewModel
import org.oppia.android.app.administratorcontrols.administratorcontrolsitemviewmodel.AdministratorControlsAppInformationViewModel
import org.oppia.android.app.administratorcontrols.administratorcontrolsitemviewmodel.AdministratorControlsDownloadPermissionsViewModel
import org.oppia.android.app.administratorcontrols.administratorcontrolsitemviewmodel.AdministratorControlsGeneralViewModel
import org.oppia.android.app.administratorcontrols.administratorcontrolsitemviewmodel.AdministratorControlsItemViewModel
import org.oppia.android.app.administratorcontrols.administratorcontrolsitemviewmodel.AdministratorControlsProfileViewModel
import org.oppia.android.app.databinding.AdministratorControlsAccountActionsViewBinding
import org.oppia.android.app.databinding.AdministratorControlsAppInformationViewBinding
import org.oppia.android.app.databinding.AdministratorControlsDownloadPermissionsViewBinding
import org.oppia.android.app.databinding.AdministratorControlsFragmentBinding
import org.oppia.android.app.databinding.AdministratorControlsGeneralViewBinding
import org.oppia.android.app.databinding.AdministratorControlsProfileViewBinding
import org.oppia.android.app.drawer.KEY_NAVIGATION_PROFILE_ID
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.recyclerview.BindableAdapter
import java.security.InvalidParameterException
import javax.inject.Inject

/** The presenter for [AdministratorControlsFragment]. */
@FragmentScope
class AdministratorControlsFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment
) {
  private lateinit var binding: AdministratorControlsFragmentBinding
  private lateinit var linearLayoutManager: LinearLayoutManager
  private var internalProfileId: Int = -1
  private lateinit var profileId: ProfileId

  @Inject
  lateinit var administratorControlsViewModel: AdministratorControlsViewModel

  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    isMultipane: Boolean
  ): View? {
    binding =
      AdministratorControlsFragmentBinding
        .inflate(
          inflater,
          container,
          /* attachToRoot= */ false
        )

    internalProfileId = activity.intent.getIntExtra(KEY_NAVIGATION_PROFILE_ID, -1)
    profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()
    administratorControlsViewModel.setProfileId(profileId)

    linearLayoutManager = LinearLayoutManager(activity.applicationContext)

    binding.administratorControlsList.apply {
      layoutManager = linearLayoutManager
      adapter = createRecyclerViewAdapter(isMultipane)
    }

    binding.apply {
      this.viewModel = administratorControlsViewModel
      this.lifecycleOwner = fragment
    }

    return binding.root
  }

  private fun createRecyclerViewAdapter(isMultipane: Boolean):
    BindableAdapter<AdministratorControlsItemViewModel> {
      return BindableAdapter.MultiTypeBuilder
        .newBuilder<AdministratorControlsItemViewModel, ViewType> { viewModel ->
          viewModel.isMultipane.set(isMultipane)
          when (viewModel) {
            is AdministratorControlsGeneralViewModel -> {
              viewModel.itemIndex.set(0)
              ViewType.VIEW_TYPE_GENERAL
            }
            is AdministratorControlsProfileViewModel -> {
              viewModel.itemIndex.set(1)
              ViewType.VIEW_TYPE_PROFILE
            }
            is AdministratorControlsDownloadPermissionsViewModel -> {
              viewModel.itemIndex.set(2)
              ViewType.VIEW_TYPE_DOWNLOAD_PERMISSIONS
            }
            is AdministratorControlsAppInformationViewModel -> {
              viewModel.itemIndex.set(3)
              ViewType.VIEW_TYPE_APP_INFORMATION
            }
            is AdministratorControlsAccountActionsViewModel -> {
              viewModel.itemIndex.set(4)
              ViewType.VIEW_TYPE_ACCOUNT_ACTIONS
            }
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
          setViewModel = this::bindProfileList,
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
          setViewModel = this::bindAppVersion,
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

  private fun bindProfileList(
    binding: AdministratorControlsProfileViewBinding,
    model: AdministratorControlsProfileViewModel
  ) {
    binding.commonViewModel = administratorControlsViewModel
    binding.viewModel = model
  }

  private fun bindAppVersion(
    binding: AdministratorControlsAppInformationViewBinding,
    model: AdministratorControlsAppInformationViewModel
  ) {
    binding.commonViewModel = administratorControlsViewModel
    binding.viewModel = model
  }

  fun setSelectedFragment(selectedFragment: String) {
    administratorControlsViewModel.selectedFragmentIndex.set(
      getSelectedFragmentIndex(
        selectedFragment
      )
    )
  }

  private fun getSelectedFragmentIndex(selectedFragment: String): Int {
    return when (selectedFragment) {
      PROFILE_LIST_FRAGMENT -> 1
      APP_VERSION_FRAGMENT -> 3
      else -> throw InvalidParameterException("Not a valid fragment in getSelectedFragmentIndex.")
    }
  }

  private enum class ViewType {
    VIEW_TYPE_GENERAL,
    VIEW_TYPE_PROFILE,
    VIEW_TYPE_DOWNLOAD_PERMISSIONS,
    VIEW_TYPE_APP_INFORMATION,
    VIEW_TYPE_ACCOUNT_ACTIONS
  }
}
