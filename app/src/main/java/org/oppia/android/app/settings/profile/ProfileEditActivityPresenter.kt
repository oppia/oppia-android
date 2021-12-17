package org.oppia.android.app.settings.profile

import android.content.Intent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import javax.inject.Inject

/** The presenter for [ProfileEditActivity]. */
@ActivityScope
class ProfileEditActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {

  private lateinit var toolbar: Toolbar

  /** Handles onCreate function of [ProfileEditActivity]. */
  fun handleOnCreate() {
    activity.setContentView(R.layout.profile_edit_activity)
    setUpToolbar()

    val profileId = activity.intent.getIntExtra(PROFILE_EDIT_PROFILE_ID_EXTRA_KEY, 0)
    val isMultipane = activity.intent.getBooleanExtra(IS_MULTIPANE_EXTRA_KEY, false)

    toolbar.setNavigationOnClickListener {
      if (isMultipane) {
        activity.onBackPressed()
      } else {
        val intent = Intent(activity, ProfileListActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        activity.startActivity(intent)
      }
    }

    if (getProfileEditFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.profile_edit_fragment_placeholder,
        ProfileEditFragment.newInstance(profileId, isMultipane)
      ).commitNow()
    }
  }

  private fun setUpToolbar() {
    toolbar = activity.findViewById<View>(R.id.profile_edit_toolbar) as Toolbar
    activity.setSupportActionBar(toolbar)
  }

  private fun getProfileEditFragment(): ProfileEditFragment? {
    return activity
      .supportFragmentManager
      .findFragmentById(R.id.profile_edit_fragment_placeholder) as ProfileEditFragment?
  }
}
