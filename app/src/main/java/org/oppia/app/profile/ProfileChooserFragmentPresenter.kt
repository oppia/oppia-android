package org.oppia.app.profile

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.oppia.app.R
import org.oppia.app.databinding.ProfileChooserAddViewBinding
import org.oppia.app.databinding.ProfileChooserFragmentBinding
import org.oppia.app.databinding.ProfileChooserProfileViewBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.home.HomeActivity
import org.oppia.app.model.ProfileChooserModel
import org.oppia.app.recyclerview.BindableAdapter
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.domain.profile.ProfileManagementController
import javax.inject.Inject

/** The presenter for [ProfileChooserFragment]. */
@FragmentScope
class ProfileChooserFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<ProfileChooserViewModel>,
  private val profileManagementController: ProfileManagementController
) {
  /** Binds ViewModel and sets up RecyclerView Adapter. */
  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    val binding =
      ProfileChooserFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    binding.apply {
      viewModel = getProfileChooserViewModel()
      lifecycleOwner = fragment
    }
    binding.profileRecyclerView.apply {
      adapter = createRecyclerViewAdapter()
    }
    return binding.root
  }

  private fun getProfileChooserViewModel(): ProfileChooserViewModel {
    return viewModelProvider.getForFragment(fragment, ProfileChooserViewModel::class.java)
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<ProfileChooserModel> {
    return BindableAdapter.MultiTypeBuilder
      .newBuilder<ProfileChooserModel, ProfileChooserModel.ModelTypeCase>(ProfileChooserModel::getModelTypeCase)
      .registerViewDataBinderWithSameModelType(
        viewType = ProfileChooserModel.ModelTypeCase.PROFILE,
        inflateDataBinding = ProfileChooserProfileViewBinding::inflate,
        setViewModel = this::bindProfileView
      )
      .registerViewDataBinderWithSameModelType(
        viewType = ProfileChooserModel.ModelTypeCase.ADDPROFILE,
        inflateDataBinding = ProfileChooserAddViewBinding::inflate,
        setViewModel = this::bindAddView
      )
      .build()
  }

  private fun bindProfileView(
    binding: ProfileChooserProfileViewBinding,
    data: ProfileChooserModel
  ) {
    binding.viewModel = data
    binding.root.setOnClickListener {
      profileManagementController.loginToProfile(data.profile.id).observe(fragment, Observer {
        if (it.isSuccess()) {
          fragment.requireActivity()
            .startActivity(Intent(fragment.context, HomeActivity::class.java))
        }
      })
    }
  }

  private fun bindAddView(binding: ProfileChooserAddViewBinding, data: ProfileChooserModel) {
    binding.root.setOnClickListener {
      if (getAdminAuthFragment() == null) {
        fragment.requireActivity().supportFragmentManager.beginTransaction()
          .setCustomAnimations(
            R.anim.slide_up,
            R.anim.slide_down,
            R.anim.slide_up,
            R.anim.slide_down
          ).add(
            R.id.profile_chooser_fragment_placeholder,
            AdminAuthFragment()
          ).addToBackStack(null).commit()
      }
    }
  }

  private fun getAdminAuthFragment(): AdminAuthFragment? {
    return fragment.requireActivity().supportFragmentManager.findFragmentById(R.id.profile_chooser_fragment_placeholder) as? AdminAuthFragment?
  }
}
