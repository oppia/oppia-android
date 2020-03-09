package org.oppia.app.profileprogress

import androidx.appcompat.app.AppCompatActivity
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import javax.inject.Inject

/** The presenter for [ProfileProgressActivity]. */
@ActivityScope
class ProfileProgressActivityPresenter @Inject constructor(private val activity: AppCompatActivity) {
  fun handleOnCreate() {
    activity.setContentView(R.layout.profile_progress_activity)
    activity.supportFragmentManager.beginTransaction().add(
      R.id.profile_progress_fragment_placeholder,
      ProfileProgressFragment()
    ).commitNow()
  }
}
