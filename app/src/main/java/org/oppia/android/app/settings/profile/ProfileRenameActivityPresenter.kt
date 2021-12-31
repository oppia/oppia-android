package org.oppia.android.app.settings.profile

import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.viewmodel.ViewModelProvider
import org.oppia.android.databinding.ProfileRenameActivityBinding
import javax.inject.Inject

/** The presenter for [ProfileRenameActivity]. */
@ActivityScope
class ProfileRenameActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val viewModelProvider: ViewModelProvider<ProfileRenameViewModel>,
) {

  /** Handles onCreate() of [ProfileRenameActivity]. */
  fun handleOnCreate(profileId: Int) {
    activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    activity.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp)

    val binding =
      DataBindingUtil.setContentView<ProfileRenameActivityBinding>(
        activity,
        R.layout.profile_rename_activity
      )

    binding.apply {
      lifecycleOwner = activity
    }

    binding.profileRenameToolbar.setNavigationOnClickListener {
      (activity as ProfileRenameActivity).finish()
    }
    if (getProfileRenameFragment() == null) {
      val profileRenameFragment = ProfileRenameFragment.newInstance(profileId)
      activity.supportFragmentManager.beginTransaction()
        .add(R.id.profile_rename_fragment_placeholder, profileRenameFragment).commitNow()
    }
  }

  private fun getProfileRenameFragment(): ProfileRenameFragment? {
    return activity.supportFragmentManager
      .findFragmentById(
        R.id.profile_rename_fragment_placeholder
      ) as ProfileRenameFragment?
  }

  private fun getProfileRenameViewModel(): ProfileRenameViewModel {
    return viewModelProvider.getForActivity(activity, ProfileRenameViewModel::class.java)
  }
}
