package org.oppia.app.settings.profile

import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.databinding.ProfileEditActivityBinding
import org.oppia.app.viewmodel.ViewModelProvider
import javax.inject.Inject

/** The presenter for [ProfileEditActivity]. */
@ActivityScope
class ProfileEditActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val viewModelProvider: ViewModelProvider<ProfileEditViewModel>
) {
  private val editViewModel: ProfileEditViewModel by lazy {
    getProfileEditViewModel()
  }

  fun handleOnCreate() {
    activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    activity.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp)

    val binding = DataBindingUtil.setContentView<ProfileEditActivityBinding>(activity, R.layout.profile_edit_activity)
    editViewModel.setProfileId(activity.intent.getIntExtra(KEY_PROFILE_EDIT_PROFILE_ID, 0))
    binding.apply {
      viewModel = editViewModel
      lifecycleOwner = activity
    }
  }

  private fun getProfileEditViewModel(): ProfileEditViewModel {
    return viewModelProvider.getForActivity(activity, ProfileEditViewModel::class.java)
  }
}
