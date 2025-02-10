package org.oppia.android.app.settings.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.oppia.android.app.administratorcontrols.LoadProfileEditListener
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.Profile
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.databinding.ProfileListFragmentBinding
import org.oppia.android.databinding.ProfileListProfileViewBinding
import javax.inject.Inject

/** The presenter for [ProfileListFragment]. */
@FragmentScope
class ProfileListFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val profileListViewModel: ProfileListViewModel,
  private val singleTypeBuilderFactory: BindableAdapter.SingleTypeBuilder.Factory
) {

  private var isMultipane = false

  /** Handles the onCreateView of [ProfileListFragment]. */
  fun handleOnCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    isMultipane: Boolean
  ): View? {
    this.isMultipane = isMultipane
    val binding = ProfileListFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )

    binding.profileListToolbar?.setNavigationOnClickListener {
      (activity as ProfileListActivity).finish()
    }
    binding.apply {
      viewModel = profileListViewModel
      lifecycleOwner = fragment
    }

    binding.profileListRecyclerView.apply {
      adapter = createRecyclerViewAdapter()
    }
    return binding.root
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<Profile> {
    return singleTypeBuilderFactory.create<Profile>()
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

      if (!isMultipane) {
        val routeToProfileEditListener = (activity as RouteToProfileEditListener)
        routeToProfileEditListener.routeToProfileEditActivity(profile.id.internalId)
      } else {
        val loadProfileEditListener = (activity as LoadProfileEditListener)
        loadProfileEditListener.loadProfileEdit(profile.id, profile.name)
      }
    }
  }
}
