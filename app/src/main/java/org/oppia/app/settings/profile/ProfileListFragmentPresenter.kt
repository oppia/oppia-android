package org.oppia.app.settings.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.oppia.app.databinding.ProfileListFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.viewmodel.ViewModelProvider
import javax.inject.Inject

/** The presenter for [ProfileListFragment] */
@FragmentScope
class ProfileListFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<ProfileListViewModel>
) {
  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    val binding = ProfileListFragmentBinding.inflate(inflater, container, /** attachToRoot= */ false)
    binding.apply {
      viewModel = getProfileListViewModel()
    }
    return binding.root
  }

  private fun getProfileListViewModel(): ProfileListViewModel {
    return viewModelProvider.getForFragment(fragment, ProfileListViewModel::class.java)
  }
}