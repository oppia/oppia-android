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
import org.oppia.android.app.administratorcontrols.administratorcontrolsitemviewmodel.AdministratorControlsProfileAndDeviceIdViewModel
import org.oppia.android.app.administratorcontrols.administratorcontrolsitemviewmodel.AdministratorControlsProfileViewModel
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.databinding.AdministratorControlsAccountActionsViewBinding
import org.oppia.android.databinding.AdministratorControlsAppInformationViewBinding
import org.oppia.android.databinding.AdministratorControlsDownloadPermissionsViewBinding
import org.oppia.android.databinding.AdministratorControlsFragmentBinding
import org.oppia.android.databinding.AdministratorControlsGeneralViewBinding
import org.oppia.android.databinding.AdministratorControlsLearnerAnalyticsViewBinding
import org.oppia.android.databinding.AdministratorControlsProfileViewBinding
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import java.security.InvalidParameterException
import javax.inject.Inject

/** The presenter for [AdministratorControlsFragment]. */
@FragmentScope
class AdministratorControlsFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val multiTypeBuilderFactory: BindableAdapter.MultiTypeBuilder.Factory
) {
  private lateinit var binding: AdministratorControlsFragmentBinding
  private lateinit var linearLayoutManager: LinearLayoutManager
  private lateinit var profileId: ProfileId

  @Inject
  lateinit var administratorControlsViewModel: AdministratorControlsViewModel

  /** Initializes and creates the views for the [AdministratorControlsFragment]. */
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

    profileId = activity.intent.extractCurrentUserProfileId()
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

  /** Returns the recycler view adapter for the controls panel in  administrator controls fragment. */
  private fun createRecyclerViewAdapter(isMultipane: Boolean):
    BindableAdapter<AdministratorControlsItemViewModel> {
      return multiTypeBuilderFactory
        .create<AdministratorControlsItemViewModel, ViewType> { viewModel ->
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
            is AdministratorControlsProfileAndDeviceIdViewModel -> {
              viewModel.itemIndex.set(2)
              ViewType.VIEW_TYPE_LEARNER_ANALYTICS
            }
            is AdministratorControlsDownloadPermissionsViewModel -> {
              viewModel.itemIndex.set(3)
              ViewType.VIEW_TYPE_DOWNLOAD_PERMISSIONS
            }
            is AdministratorControlsAppInformationViewModel -> {
              viewModel.itemIndex.set(4)
              ViewType.VIEW_TYPE_APP_INFORMATION
            }
            is AdministratorControlsAccountActionsViewModel -> {
              viewModel.itemIndex.set(5)
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
          viewType = ViewType.VIEW_TYPE_LEARNER_ANALYTICS,
          inflateDataBinding = AdministratorControlsLearnerAnalyticsViewBinding::inflate,
          setViewModel = this::bindLearnerAnalytics,
          transformViewModel = { it as AdministratorControlsProfileAndDeviceIdViewModel }
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

  /** Binds the profile list to the view. */
  private fun bindProfileList(
    binding: AdministratorControlsProfileViewBinding,
    model: AdministratorControlsProfileViewModel
  ) {
    binding.commonViewModel = administratorControlsViewModel
    binding.viewModel = model
  }

  /** Binds the app version to the view. */
  private fun bindAppVersion(
    binding: AdministratorControlsAppInformationViewBinding,
    model: AdministratorControlsAppInformationViewModel
  ) {
    binding.commonViewModel = administratorControlsViewModel
    binding.viewModel = model
  }

  private fun bindLearnerAnalytics(
    binding: AdministratorControlsLearnerAnalyticsViewBinding,
    model: AdministratorControlsProfileAndDeviceIdViewModel
  ) {
    binding.commonViewModel = administratorControlsViewModel
    binding.viewModel = model
  }

  /** Sets the selected fragment Argument as the selected fragment in the view model. */
  fun setSelectedFragment(selectedFragment: String) {
    administratorControlsViewModel.selectedFragmentIndex.set(
      getSelectedFragmentIndex(selectedFragment)
    )
  }

  private fun getSelectedFragmentIndex(selectedFragment: String): Int {
    return when (selectedFragment) {
      PROFILE_LIST_FRAGMENT -> 1
      PROFILE_AND_DEVICE_ID_FRAGMENT -> 2
      APP_VERSION_FRAGMENT -> 4
      else -> throw InvalidParameterException("Not a valid fragment in getSelectedFragmentIndex.")
    }
  }

  private enum class ViewType {
    /** Represents [View] for the general section. */
    VIEW_TYPE_GENERAL,

    /** Represents [View] for the profile section. */
    VIEW_TYPE_PROFILE,

    /** Represents [View] for the download permissions section. */
    VIEW_TYPE_DOWNLOAD_PERMISSIONS,

    /** Represents [View] for the app information section. */
    VIEW_TYPE_APP_INFORMATION,

    /** Represents [View] for the account actions section. */
    VIEW_TYPE_ACCOUNT_ACTIONS,

    /** Represents [View] for the learner analytics section. */
    VIEW_TYPE_LEARNER_ANALYTICS
  }
}
