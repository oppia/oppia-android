package org.oppia.app.settings.profile

import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.databinding.ProfileListActivityBinding
import org.oppia.app.viewmodel.ViewModelProvider
import javax.inject.Inject

/** The presenter for [ProfileListActivity]. */
@ActivityScope
class ProfileListActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val viewModelProvider: ViewModelProvider<ProfileListViewModel>
) {
  fun handleOnCreate() {
    activity.title = "Profiles"
    activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    activity.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp)

    val binding = DataBindingUtil.setContentView<ProfileListActivityBinding>(activity, R.layout.profile_list_activity)

  }

  private fun getProfileListViewModel(): ProfileListViewModel {
    return viewModelProvider.getForActivity(activity, ProfileListViewModel::class.java)
  }
}
