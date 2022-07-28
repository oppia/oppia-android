package org.oppia.android.app.administratorcontrols.learneranalytics

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.oppia.android.app.administratorcontrols.learneranalytics.ProfileListViewModel.ProfileListItemViewModel
import org.oppia.android.app.administratorcontrols.learneranalytics.ProfileListViewModel.ProfileListItemViewType
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.databinding.ProfileAndDeviceIdFragmentBinding
import org.oppia.android.databinding.ProfileListDeviceIdItemBinding
import org.oppia.android.databinding.ProfileListLearnerIdItemBinding
import org.oppia.android.databinding.ProfileListSyncStatusItemBinding
import javax.inject.Inject

/** Presenter for arranging [ProfileAndDeviceIdFragment]'s UI. */
class ProfileAndDeviceIdFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val profileListViewModelFactory: ProfileListViewModel.Factory,
  private val multiTypeBuilderFactory: BindableAdapter.MultiTypeBuilder.Factory
) {

  private lateinit var binding: ProfileAndDeviceIdFragmentBinding

  /** Handles [ProfileAndDeviceIdFragment]'s creation flow. */
  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View {
    binding = ProfileAndDeviceIdFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    binding.apply {
      lifecycleOwner = fragment
      this.viewModel = profileListViewModelFactory.create()
    }
    binding.profileAndDeviceIdRecyclerView.apply {
      adapter = createRecyclerViewAdapter()
    }
    return binding.root
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<ProfileListItemViewModel> {
    return multiTypeBuilderFactory.create<ProfileListItemViewModel,
      ProfileListItemViewType> { viewModel ->
      when (viewModel) {
        is DeviceIdItemViewModel -> ProfileListItemViewType.DEVICE_ID
        is ProfileLearnerIdItemViewModel -> ProfileListItemViewType.LEARNER_ID
        is SyncStatusItemViewModel -> ProfileListItemViewType.SYNC_STATUS
        else -> error("Encountered unexpected view model: $viewModel")
      }
    }
      .registerViewDataBinder(
        viewType = ProfileListItemViewType.DEVICE_ID,
        inflateDataBinding = ProfileListDeviceIdItemBinding::inflate,
        setViewModel = ProfileListDeviceIdItemBinding::setViewModel,
        transformViewModel = { it as DeviceIdItemViewModel }
      )
      .registerViewDataBinder(
        viewType = ProfileListItemViewType.LEARNER_ID,
        inflateDataBinding = ProfileListLearnerIdItemBinding::inflate,
        setViewModel = ProfileListLearnerIdItemBinding::setViewModel,
        transformViewModel = { it as ProfileLearnerIdItemViewModel }
      )
      .registerViewDataBinder(
        viewType = ProfileListItemViewType.SYNC_STATUS,
        inflateDataBinding = ProfileListSyncStatusItemBinding::inflate,
        setViewModel = ProfileListSyncStatusItemBinding::setViewModel,
        transformViewModel = { it as SyncStatusItemViewModel }
      )
      .build()
  }
}
