package org.oppia.app.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.oppia.app.databinding.ProfileChooserFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.viewmodel.ViewModelProvider
import javax.inject.Inject

/** The presenter for [ProfileChooserFragment]. */
@FragmentScope
class ProfileChooserFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<ProfileChooserViewModel>
) {
  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    val binding = ProfileChooserFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    binding.apply {
      viewModel = getProfileChooserViewModel()
    }
    return binding.root
  }

  private fun getProfileChooserViewModel(): ProfileChooserViewModel {
    return viewModelProvider.getForFragment(fragment, ProfileChooserViewModel::class.java)
  }
}
