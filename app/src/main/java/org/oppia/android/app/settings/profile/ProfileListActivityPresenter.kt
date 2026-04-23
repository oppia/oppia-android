package org.oppia.android.app.settings.profile

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.ui.R
import org.oppia.android.app.utility.EdgeToEdgeHelper
import org.oppia.android.util.platformparameter.EnableEdgeToEdge
import org.oppia.android.util.platformparameter.PlatformParameterValue
import javax.inject.Inject

/** The presenter for [ProfileListActivity]. */
@ActivityScope
class ProfileListActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val resourceHandler: AppLanguageResourceHandler,
  @EnableEdgeToEdge private val enableEdgeToEdge: PlatformParameterValue<Boolean>
) {

  /** Handles onCreate method of [ProfileListActivity]. */
  fun handleOnCreate() {
    if (enableEdgeToEdge.value) {
      EdgeToEdgeHelper.enableEdgeToEdgeDispatch(activity)
    }
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
