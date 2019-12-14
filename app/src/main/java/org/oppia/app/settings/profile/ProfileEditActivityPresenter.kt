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
  fun handleOnCreate() {
    val binding = DataBindingUtil.setContentView<ProfileEditActivityBinding>(activity, R.layout.profile_edit_activity)
  }

  private fun getProfileEditViewModel(): ProfileEditViewModel {
    return viewModelProvider.getForActivity(activity, ProfileEditViewModel::class.java)
  }
}
