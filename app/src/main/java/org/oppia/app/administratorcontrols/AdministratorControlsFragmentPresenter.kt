package org.oppia.app.administratorcontrols

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import org.oppia.app.administratorcontrols.administratorcontrolsitemviewmodel.AdministratorControlsAccountActionsViewModel
import org.oppia.app.administratorcontrols.administratorcontrolsitemviewmodel.AdministratorControlsAppInformationViewModel
import org.oppia.app.administratorcontrols.administratorcontrolsitemviewmodel.AdministratorControlsDownloadPermissionsViewModel
import org.oppia.app.administratorcontrols.administratorcontrolsitemviewmodel.AdministratorControlsGeneralViewModel
import org.oppia.app.administratorcontrols.administratorcontrolsitemviewmodel.AdministratorControlsItemViewModel
import org.oppia.app.administratorcontrols.administratorcontrolsitemviewmodel.AdministratorControlsProfileViewModel
import org.oppia.app.databinding.AdministratorControlsAccountActionsViewBinding
import org.oppia.app.databinding.AdministratorControlsAppInformationViewBinding
import org.oppia.app.databinding.AdministratorControlsDownloadPermissionsViewBinding
import org.oppia.app.databinding.AdministratorControlsFragmentBinding
import org.oppia.app.databinding.AdministratorControlsGeneralViewBinding
import org.oppia.app.databinding.AdministratorControlsProfileViewBinding
import org.oppia.app.drawer.KEY_NAVIGATION_PROFILE_ID
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.ProfileId
import org.oppia.app.recyclerview.BindableAdapter
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

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
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
      adapter = createRecyclerViewAdapter()
    }

    binding.apply {
      this.viewModel = administratorControlsViewModel
      this.lifecycleOwner = fragment
    }

    return binding.root
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<AdministratorControlsItemViewModel> {
    return BindableAdapter.MultiTypeBuilder
      .newBuilder<AdministratorControlsItemViewModel, ViewType> { viewModel ->
        when (viewModel) {
          is AdministratorControlsGeneralViewModel ->
            ViewType.VIEW_TYPE_GENERAL
          is AdministratorControlsProfileViewModel ->
            ViewType.VIEW_TYPE_PROFILE
          is AdministratorControlsDownloadPermissionsViewModel ->
            ViewType.VIEW_TYPE_DOWNLOAD_PERMISSIONS
          is AdministratorControlsAppInformationViewModel ->
            ViewType.VIEW_TYPE_APP_INFORMATION
          is AdministratorControlsAccountActionsViewModel ->
            ViewType.VIEW_TYPE_ACCOUNT_ACTIONS
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

  private enum class ViewType {
    VIEW_TYPE_GENERAL,
    VIEW_TYPE_PROFILE,
    VIEW_TYPE_DOWNLOAD_PERMISSIONS,
    VIEW_TYPE_APP_INFORMATION,
    VIEW_TYPE_ACCOUNT_ACTIONS
  }
}
