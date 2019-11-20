package org.oppia.app.settings.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.oppia.app.databinding.ProfileEditFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.viewmodel.ViewModelProvider
import javax.inject.Inject

/** The presenter for [ProfileEditFragment]. */
@FragmentScope
class ProfileEditFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<ProfileEditViewModel>
) {
  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    val binding = ProfileEditFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    binding.apply {
      viewModel = getProfileEditViewModel()
    }
    return binding.root
  }

  private fun getProfileEditViewModel(): ProfileEditViewModel {
    return viewModelProvider.getForFragment(fragment, ProfileEditViewModel::class.java)
  }
}
