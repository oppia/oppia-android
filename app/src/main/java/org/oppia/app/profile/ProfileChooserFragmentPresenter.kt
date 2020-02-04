package org.oppia.app.profile

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import org.oppia.app.R
import org.oppia.app.databinding.ProfileChooserAddViewBinding
import org.oppia.app.databinding.ProfileChooserFragmentBinding
import org.oppia.app.databinding.ProfileChooserProfileViewBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.home.HomeActivity
import org.oppia.app.model.ProfileChooserUiModel
import org.oppia.app.recyclerview.BindableAdapter
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.domain.profile.ProfileManagementController
import javax.inject.Inject

private val COLORS_LIST = listOf(
  R.color.avatar_background_1,
  R.color.avatar_background_2,
  R.color.avatar_background_3,
  R.color.avatar_background_4,
  R.color.avatar_background_5,
  R.color.avatar_background_6,
  R.color.avatar_background_7,
  R.color.avatar_background_8,
  R.color.avatar_background_9,
  R.color.avatar_background_10,
  R.color.avatar_background_11,
  R.color.avatar_background_12,
  R.color.avatar_background_13,
  R.color.avatar_background_14,
  R.color.avatar_background_15,
  R.color.avatar_background_16,
  R.color.avatar_background_17,
  R.color.avatar_background_18,
  R.color.avatar_background_19,
  R.color.avatar_background_20,
  R.color.avatar_background_21,
  R.color.avatar_background_22,
  R.color.avatar_background_23,
  R.color.avatar_background_24
)

/** The presenter for [ProfileChooserFragment]. */
@FragmentScope
class ProfileChooserFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val activity: AppCompatActivity,
  private val context: Context,
  private val viewModelProvider: ViewModelProvider<ProfileChooserViewModel>,
  private val profileManagementController: ProfileManagementController
) {
  private val chooserViewModel: ProfileChooserViewModel by lazy {
    getProfileChooserViewModel()
  }

  /** Binds ViewModel and sets up RecyclerView Adapter. */
  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    val binding =
      ProfileChooserFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    binding.apply {
      viewModel = chooserViewModel
      lifecycleOwner = fragment
    }
    binding.profileRecyclerView.isNestedScrollingEnabled = false
    binding.profileRecyclerView.apply {
      adapter = createRecyclerViewAdapter()
    }
    return binding.root
  }

  /** Randomly selects a color for the new profile that is not already in use. */
  private fun selectUniqueRandomColor(): Int {
    return COLORS_LIST.map {
      ContextCompat.getColor(context, it)
    }.minus(chooserViewModel.usedColors).random()
  }

  private fun getProfileChooserViewModel(): ProfileChooserViewModel {
    return viewModelProvider.getForFragment(fragment, ProfileChooserViewModel::class.java)
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<ProfileChooserUiModel> {
    return BindableAdapter.MultiTypeBuilder
      .newBuilder<ProfileChooserUiModel, ProfileChooserUiModel.ModelTypeCase>(ProfileChooserUiModel::getModelTypeCase)
      .registerViewDataBinderWithSameModelType(
        viewType = ProfileChooserUiModel.ModelTypeCase.PROFILE,
        inflateDataBinding = ProfileChooserProfileViewBinding::inflate,
        setViewModel = this::bindProfileView
      )
      .registerViewDataBinderWithSameModelType(
        viewType = ProfileChooserUiModel.ModelTypeCase.ADD_PROFILE,
        inflateDataBinding = ProfileChooserAddViewBinding::inflate,
        setViewModel = this::bindAddView
      )
      .build()
  }

  private fun bindProfileView(
    binding: ProfileChooserProfileViewBinding,
    model: ProfileChooserUiModel
  ) {
    binding.viewModel = model
    binding.root.setOnClickListener {
      if (model.profile.pin.isEmpty()) {
        profileManagementController.loginToProfile(model.profile.id).observe(fragment, Observer {
          if (it.isSuccess()) {
            activity.startActivity((HomeActivity.createHomeActivity(activity, model.profile.id.internalId)))
          }
        })
      } else {
        val pinPasswordIntent = PinPasswordActivity.createPinPasswordActivityIntent(
          activity,
          chooserViewModel.adminPin,
          model.profile.id.internalId
        )
        activity.startActivity(pinPasswordIntent)
      }
    }
  }

  private fun bindAddView(binding: ProfileChooserAddViewBinding, @Suppress("UNUSED_PARAMETER") model: ProfileChooserUiModel) {
    binding.root.setOnClickListener {
      if (chooserViewModel.adminPin.isEmpty()) {
        activity.startActivity(
          AdminPinActivity.createAdminPinActivityIntent(activity, chooserViewModel.adminProfileId.internalId, selectUniqueRandomColor())
        )
      } else {
        activity.startActivity(
          AdminAuthActivity.createAdminAuthActivityIntent(activity, chooserViewModel.adminPin, selectUniqueRandomColor())
        )
      }
    }
  }
}
