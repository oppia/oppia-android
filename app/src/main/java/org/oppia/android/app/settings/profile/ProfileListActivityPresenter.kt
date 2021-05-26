package org.oppia.android.app.settings.profile

import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
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
  fun handleOnCreate(internalProfileId: Int) {
    binding = DataBindingUtil.setContentView(
      activity,
      R.layout.profile_list_activity
    )
    activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    activity.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp)
    binding.profileListToolbar.title = activity.getString(R.string.profile_list_activity_title)

    var fragment = getProfileActivityFragment()
    fragment = if (fragment == null || fragment is ProfileListFragment) {
      ProfileListFragment.newInstance()
    } else {
      // use saved instance for profile id
      ProfileEditFragment.newInstance(activity, internalProfileId)
    }
    activity.supportFragmentManager.beginTransaction()
      .add(R.id.profile_list_fragment_placeholder, fragment).commitNow()
  }

  private fun getProfileActivityFragment(): Fragment? {
    val fragment = activity
      .supportFragmentManager
      .findFragmentById(R.id.profile_list_fragment_placeholder)
    return if (fragment is ProfileListFragment?) {
      fragment
    } else {
      fragment as ProfileEditFragment?
    }
  }

  fun updateToolbarTitle(title: String) {
    binding.profileListToolbar.title = title
  }

  fun toolbarListener(function: () -> Unit) {
    binding.profileListToolbar.setNavigationOnClickListener {
      function()
    }
  }
}
