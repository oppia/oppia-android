package org.oppia.app.testing

import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import org.oppia.app.R
import org.oppia.app.databinding.ExplorationActivityBinding
import org.oppia.app.databinding.ProfileChooserAddViewBinding
import org.oppia.app.databinding.ProfileChooserFragmentBinding
import org.oppia.app.databinding.ProfileChooserProfileViewBinding
import org.oppia.app.databinding.ProfileChooserTestActivityBinding
import org.oppia.app.model.AppLanguage
import org.oppia.app.model.AudioLanguage
import org.oppia.app.model.ProfileChooserUiModel
import org.oppia.app.model.StoryTextSize
import org.oppia.app.profile.ProfileChooserViewModel
import org.oppia.app.profile.ProfileChooserViewTestModel
import org.oppia.app.profileprogress.ProfileProgressViewModel
import org.oppia.app.recyclerview.BindableAdapter
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.domain.profile.ProfileManagementController
import javax.inject.Inject

class ProfileChooserFragmentTestActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val profileManagementController: ProfileManagementController,
  private val viewModelProvider: ViewModelProvider<ProfileChooserViewTestModel>
) {
  private lateinit var binding: ProfileChooserTestActivityBinding

  private val chooserViewModel: ProfileChooserViewTestModel by lazy {
    getProfileChooserViewModel()
  }

  fun handleOnCreate() {
    binding = DataBindingUtil.setContentView<ProfileChooserTestActivityBinding>(activity, R.layout.profile_chooser_test_activity)

    binding.apply {
      viewModel = chooserViewModel
      lifecycleOwner = activity
    }
    binding.profileRecyclerView.isNestedScrollingEnabled = false
    binding.profileRecyclerView.apply {
      adapter = createRecyclerViewAdapter()
    }
    profileManagementController.addProfile(
      name = "Sean",
      pin = "",
      avatarImagePath = null,
      allowDownloadAccess = true,
      colorRgb = -10710042,
      isAdmin = true,
      storyTextSize = StoryTextSize.SMALL_TEXT_SIZE,
      appLanguage = AppLanguage.ENGLISH_APP_LANGUAGE,
      audioLanguage = AudioLanguage.ENGLISH_AUDIO_LANGUAGE
    )
  }

  private fun getProfileChooserViewModel(): ProfileChooserViewTestModel {
    return viewModelProvider.getForActivity(activity, ProfileChooserViewTestModel::class.java)
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
  }

  private fun bindAddView(binding: ProfileChooserAddViewBinding, @Suppress("UNUSED_PARAMETER") model: ProfileChooserUiModel) {

  }
}
