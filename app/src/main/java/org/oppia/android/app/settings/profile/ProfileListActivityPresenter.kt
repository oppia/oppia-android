package org.oppia.android.app.settings.profile

import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.databinding.ProfileListActivityBinding
import javax.inject.Inject

/** The presenter for [ProfileListActivity]. */
@ActivityScope
class ProfileListActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {
  private lateinit var binding: ProfileListActivityBinding
  fun handleOnCreate() {
    binding = DataBindingUtil.setContentView(
      activity,
      R.layout.profile_list_activity
    )
    activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    activity.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp)
    binding.profileListToolbar.title = activity.getString(R.string.profile_list_activity_title)

    if (getProfileListFragment() == null) {
      val profileListFragment = ProfileListFragment.newInstance()
      activity.supportFragmentManager.beginTransaction()
        .add(R.id.profile_list_fragment_placeholder, profileListFragment).commitNow()
    }
  }

  private fun getProfileListFragment(): ProfileListFragment? {
    return activity.supportFragmentManager
      .findFragmentById(R.id.profile_list_fragment_placeholder) as ProfileListFragment?
  }

  fun updateToolbarTitle(title: String) {
    binding.profileListToolbar.title = title
  }

  fun toolbarListener(function: () -> Unit) {
    binding.profileListToolbar.setNavigationOnClickListener({
      function()
    })
  }
}
