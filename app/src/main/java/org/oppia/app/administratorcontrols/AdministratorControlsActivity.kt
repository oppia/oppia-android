package org.oppia.app.administratorcontrols

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.widget.TextView
import org.oppia.app.R
import org.oppia.app.activity.InjectableAppCompatActivity
import org.oppia.app.administratorcontrols.appversion.AppVersionActivity
import org.oppia.app.drawer.KEY_NAVIGATION_PROFILE_ID
import org.oppia.app.settings.profile.ProfileListActivity
import javax.inject.Inject

const val SELECTED_CONTROLS_TITLE_KEY = "SELECTED_CONTROLS_TITLE_KEY"
const val LAST_LOADED_FRAGMENT_KEY = "LAST_LOADED_FRAGMENT_KEY"
const val PROFILE_LIST_FRAGMENT = "PROFILE_LIST_FRAGMENT"
const val APP_VERSION_FRAGMENT = "APP_VERSION_FRAGMENT"

/** Activity for Administrator Controls. */
class AdministratorControlsActivity :
  InjectableAppCompatActivity(),
  RouteToProfileListListener,
  RouteToAppVersionListener,
  LoadProfileListListener,
  LoadAppVersionListener {
  @Inject
  lateinit var administratorControlsActivityPresenter: AdministratorControlsActivityPresenter
  private lateinit var lastLoadedFragment: String

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    val extraControlsTitle = savedInstanceState?.getString(SELECTED_CONTROLS_TITLE_KEY)
    lastLoadedFragment = if (savedInstanceState != null) {
      savedInstanceState.get(LAST_LOADED_FRAGMENT_KEY) as String
    } else {
      // TODO(#661): Change the default fragment in the right hand side to be EditAccount fragment in the case of multipane controls.
      PROFILE_LIST_FRAGMENT
    }
    administratorControlsActivityPresenter.handleOnCreate(extraControlsTitle, lastLoadedFragment)
    title = getString(R.string.administrator_controls)
  }

  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    menuInflater.inflate(R.menu.menu_administrator_controls_activity, menu)
    return super.onCreateOptionsMenu(menu)
  }

  override fun routeToAppVersion() {
    startActivity(AppVersionActivity.createAppVersionActivityIntent(this))
  }

  override fun routeToProfileList() {
    startActivity(ProfileListActivity.createProfileListActivityIntent(this))
  }

  companion object {
    fun createAdministratorControlsActivityIntent(context: Context, profileId: Int?): Intent {
      val intent = Intent(context, AdministratorControlsActivity::class.java)
      intent.putExtra(KEY_NAVIGATION_PROFILE_ID, profileId)
      return intent
    }

    fun getIntentKey(): String {
      return KEY_NAVIGATION_PROFILE_ID
    }
  }

  override fun loadProfileList() {
    lastLoadedFragment = PROFILE_LIST_FRAGMENT
    administratorControlsActivityPresenter
      .setExtraControlsTitle(getString(R.string.administrator_controls_edit_profiles))
    administratorControlsActivityPresenter.loadProfileList()
  }

  override fun loadAppVersion() {
    lastLoadedFragment = APP_VERSION_FRAGMENT
    administratorControlsActivityPresenter
      .setExtraControlsTitle(getString(R.string.administrator_controls_app_version))
    administratorControlsActivityPresenter.loadAppVersion()
  }

  override fun onSaveInstanceState(outState: Bundle) {
    administratorControlsActivityPresenter.handleOnSaveInstanceState(outState)
    super.onSaveInstanceState(outState)
  }
}
