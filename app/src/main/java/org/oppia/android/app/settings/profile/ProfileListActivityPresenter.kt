package org.oppia.android.app.settings.profile

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import javax.inject.Inject
import org.oppia.android.app.translation.AppLanguageResourceHandler

/** The presenter for [ProfileListActivity]. */
@ActivityScope
class ProfileListActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val resourceHandler: AppLanguageResourceHandler
) {
  fun handleOnCreate() {
    activity.title = resourceHandler.getStringInLocale(R.string.profile_list_activity_title)
    activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    activity.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp)
    activity.setContentView(R.layout.profile_list_activity)

    if (getProfileListFragment() == null) {
      val profileListFragment = ProfileListFragment.newInstance()
      activity.supportFragmentManager.beginTransaction()
        .add(R.id.profile_list_container, profileListFragment).commitNow()
    }
  }

  private fun getProfileListFragment(): ProfileListFragment? {
    return activity.supportFragmentManager
      .findFragmentById(R.id.profile_list_container) as ProfileListFragment?
  }
}
