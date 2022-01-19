package org.oppia.android.app.administratorcontrols.learneranalytics

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import javax.inject.Inject
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.Profile
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.app.viewmodel.ViewModelProvider
import org.oppia.android.databinding.ProfileAndDeviceIdFragmentBinding
import org.oppia.android.databinding.ProfileIdListViewBinding

@FragmentScope
class ProfileAndDeviceIdFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<ProfileAndDeviceIdViewModel>
) {
  private lateinit var binding: ProfileAndDeviceIdFragmentBinding

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    binding = ProfileAndDeviceIdFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    binding.let {
      it.lifecycleOwner = fragment
      it.viewModel = getProfileAndDeviceIdViewModel()
    }

    binding.profileAndDeviceIdRecyclerView.apply {
      adapter = createRecyclerViewAdapter()
    }

    binding.deviceIdCopyButtonLayout.setOnClickListener {
      getProfileAndDeviceIdViewModel().setCurrentCopiedId(
        getProfileAndDeviceIdViewModel().deviceId
      )
    }
    return binding.root
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<Profile> {
    return BindableAdapter.SingleTypeBuilder
      .newBuilder<Profile>()
      .registerViewDataBinderWithSameModelType(
        inflateDataBinding = ProfileIdListViewBinding::inflate,
        setViewModel = ::bindProfileView
      )
      .build()
  }

  private fun bindProfileView(
    binding: ProfileIdListViewBinding,
    profile: Profile
  ) {
    binding.profile = profile
    binding.profileIdViewCopyStatusImage.setOnClickListener {
      getProfileAndDeviceIdViewModel().setCurrentCopiedId(profile.learnerId)
    }
    binding.profileIdViewCopyStatusText.setOnClickListener {
      getProfileAndDeviceIdViewModel().setCurrentCopiedId(profile.learnerId)
    }
  }

  private fun getProfileAndDeviceIdViewModel(): ProfileAndDeviceIdViewModel {
    return viewModelProvider.getForFragment(fragment, ProfileAndDeviceIdViewModel::class.java)
  }
}
