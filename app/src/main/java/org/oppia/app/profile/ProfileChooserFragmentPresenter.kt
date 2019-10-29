package org.oppia.app.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.oppia.app.databinding.ProfileChooserAddViewBinding
import org.oppia.app.databinding.ProfileChooserFragmentBinding
import org.oppia.app.databinding.ProfileChooserProfileViewBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.ProfileChooserModel
import org.oppia.app.recyclerview.BindableAdapter
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
    binding.recyclerView.apply {
      adapter = createRecyclerViewAdapter()
    }
    return binding.root
  }

  private fun getProfileChooserViewModel(): ProfileChooserViewModel {
    return viewModelProvider.getForFragment(fragment, ProfileChooserViewModel::class.java)
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<ProfileChooserModel> {
    return BindableAdapter.Builder
      .newBuilder<ProfileChooserModel>()
      .registerViewTypeComputer { value ->
        value.modelTypeCase.number
      }
      .registerViewDataBinder(
        viewType = ProfileChooserModel.PROFILE_FIELD_NUMBER,
        inflateDataBinding = ProfileChooserProfileViewBinding::inflate,
        setViewModel = ProfileChooserProfileViewBinding::setViewModel)
      .registerViewDataBinder(
        viewType = ProfileChooserModel.ADDPROFILE_FIELD_NUMBER,
        inflateDataBinding = ProfileChooserAddViewBinding::inflate,
        setViewModel = ProfileChooserAddViewBinding::setViewModel)
      .build()
  }
}
