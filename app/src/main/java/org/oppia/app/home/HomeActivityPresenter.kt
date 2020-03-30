package org.oppia.app.home

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
import org.oppia.app.model.ProfileId
import org.oppia.domain.profile.ProfileManagementController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import java.util.Locale
import javax.inject.Inject

const val TAG_HOME_FRAGMENT = "HOME_FRAGMENT"

/** The presenter for [HomeActivity]. */
@ActivityScope
class HomeActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val logger: Logger,
  private val profileManagementController: ProfileManagementController
) {
  private var navigationDrawerFragment: NavigationDrawerFragment? = null

  private lateinit var profileId: ProfileId

  fun handleOnCreate(internalProfileId: Int) {
    activity.setContentView(R.layout.home_activity)
    profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()
    subscribeToProfileLiveData()
  }

  private fun setUpNavigationDrawer() {
    val toolbar = activity.findViewById<View>(R.id.home_activity_toolbar) as Toolbar
    activity.setSupportActionBar(toolbar)
    toolbar.setTitle(R.string.menu_home)
    activity.supportActionBar!!.setDisplayShowHomeEnabled(true)
    navigationDrawerFragment =
      activity.supportFragmentManager.findFragmentById(R.id.home_activity_fragment_navigation_drawer) as NavigationDrawerFragment
    navigationDrawerFragment!!.setUpDrawer(
      activity.findViewById<View>(R.id.home_activity_drawer_layout) as DrawerLayout,
      toolbar, R.id.nav_home
    )
  }

  private fun getHomeFragment(): HomeFragment? {
    return activity.supportFragmentManager.findFragmentById(R.id.home_fragment_placeholder) as HomeFragment?
  }

  private fun getProfileData(): LiveData<Profile> {
    return Transformations.map(profileManagementController.getProfile(profileId), ::processGetProfileResult)
  }

  private fun subscribeToProfileLiveData() {
    getProfileData().observe(activity, Observer<Profile> {
      setPreferredLanguage(getAppLanguageCode(it.appLanguage))
    })
  }

  private fun processGetProfileResult(profileResult: AsyncResult<Profile>): Profile {
    if (profileResult.isFailure()) {
      logger.e("HomeActivity", "Failed to retrieve profile", profileResult.getErrorOrNull()!!)
    }
    return profileResult.getOrDefault(Profile.getDefaultInstance())
  }

  private fun initialiseUI() {
    setUpNavigationDrawer()
    if (getHomeFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.home_fragment_placeholder,
        HomeFragment(),
        TAG_HOME_FRAGMENT
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
