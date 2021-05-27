package org.oppia.android.app.settings.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.oppia.android.R
import org.oppia.android.app.administratorcontrols.AdministratorControlsActivity
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.Profile
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.app.viewmodel.ViewModelProvider
import org.oppia.android.databinding.ProfileListFragmentBinding
import org.oppia.android.databinding.ProfileListProfileViewBinding
import javax.inject.Inject

/** The presenter for [ProfileListFragment]. */
@FragmentScope
class ProfileListFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<ProfileListViewModel>
) {
  private var isMultipane = false

  fun handleOnCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    isMultipane: Boolean,
    profileListInterface: ProfileListInterface?
  ): View? {
    this.isMultipane = isMultipane
    val binding = ProfileListFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )

    profileListInterface?.toolbarListener {
      (activity as ProfileListActivity).finish()
    }

    binding.apply {
      lifecycleOwner = fragment
      viewModel = getProfileListViewModel()
    }

    binding.profileListRecyclerView.apply {
      adapter = createRecyclerViewAdapter()
    }
    return binding.root
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<Profile> {
    return BindableAdapter.SingleTypeBuilder
      .newBuilder<Profile>()
      .registerViewDataBinderWithSameModelType(
        inflateDataBinding = ProfileListProfileViewBinding::inflate,
        setViewModel = ::bindProfileView
      )
      .build()
  }

  private fun bindProfileView(
    binding: ProfileListProfileViewBinding,
    profile: Profile
  ) {
    binding.profile = profile
    binding.root.setOnClickListener {
      if (isMultipane && activity is AdministratorControlsActivity) {
        activity.loadProfileEdit(profile.id.internalId, isMultipane)
      } else {
        fragment.parentFragmentManager.beginTransaction().add(
          R.id.profile_list_fragment_placeholder,
          ProfileEditFragment.newInstance(
            profile.id.internalId,
            isMultipane
          )
        ).commitNow()
      }
    }
  }

  private fun getProfileListViewModel(): ProfileListViewModel {
    return viewModelProvider.getForFragment(fragment, ProfileListViewModel::class.java)
  }
}
