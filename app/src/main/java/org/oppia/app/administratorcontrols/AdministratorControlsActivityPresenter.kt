package org.oppia.app.administratorcontrols

import android.content.res.Configuration
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.drawer.NavigationDrawerFragment
import org.oppia.app.model.AppLanguage
import org.oppia.app.model.Profile
import org.oppia.domain.profile.ProfileManagementController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import java.util.Locale
import javax.inject.Inject

/** The presenter for [AdministratorControlsActivity]. */
@ActivityScope
class AdministratorControlsActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val logger: Logger,
  private val profileManagementController: ProfileManagementController
) {
  private lateinit var navigationDrawerFragment: NavigationDrawerFragment

  fun handleOnCreate() {
    activity.setContentView(R.layout.administrator_controls_activity)
    subscribeToProfileLiveData()
  }

  private fun setUpNavigationDrawer() {
    val toolbar = activity.findViewById<View>(R.id.administrator_controls_activity_toolbar) as Toolbar
    activity.setSupportActionBar(toolbar)
    toolbar.setTitle(R.string.administrator_controls)
    activity.supportActionBar!!.setDisplayShowHomeEnabled(true)
    navigationDrawerFragment =
      activity.supportFragmentManager.findFragmentById(R.id.administrator_controls_activity_fragment_navigation_drawer) as NavigationDrawerFragment
    navigationDrawerFragment.setUpDrawer(
      activity.findViewById<View>(R.id.administrator_controls_activity_drawer_layout) as DrawerLayout,
      toolbar, /* menuItemId = */ 0
    )
  }

  private fun getAdministratorControlsFragment(): AdministratorControlsFragment? {
    return activity.supportFragmentManager.findFragmentById(R.id.administrator_controls_fragment_placeholder) as AdministratorControlsFragment?
  }

  private fun getProfileData(): LiveData<Profile> {
    return Transformations.map(profileManagementController.getProfile(profileManagementController.getCurrentProfileId()), ::processGetProfileResult)
  }

  private fun subscribeToProfileLiveData() {
    getProfileData().observe(activity, Observer<Profile> {
      setPreferredLanguage(getAppLanguageCode(it.appLanguage))
    })
  }

  private fun processGetProfileResult(profileResult: AsyncResult<Profile>): Profile {
    if (profileResult.isFailure()) {
      logger.e("AdministratorControlsActivity", "Failed to retrieve profile", profileResult.getErrorOrNull()!!)
    }
    return profileResult.getOrDefault(Profile.getDefaultInstance())
  }

  private fun initialiseUI() {
    setUpNavigationDrawer()
    if (getAdministratorControlsFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.administrator_controls_fragment_placeholder,
        AdministratorControlsFragment()
      ).commitNow()
    }
  }

  private fun setPreferredLanguage(appLanguageCode: String) {
    val locale = Locale(appLanguageCode)
    Locale.setDefault(locale)
    val config = Configuration()
    config.locale = locale
    activity.resources.updateConfiguration(
      config,
      activity.baseContext.resources.displayMetrics
    )
    initialiseUI()
  }

  private fun getAppLanguageCode(appLanguage: AppLanguage): String {
    return when (appLanguage) {
      AppLanguage.ENGLISH_APP_LANGUAGE -> "en"
      AppLanguage.HINDI_APP_LANGUAGE -> "hi"
      AppLanguage.FRENCH_APP_LANGUAGE -> "fr"
      else -> "en"
    }
  }
}
