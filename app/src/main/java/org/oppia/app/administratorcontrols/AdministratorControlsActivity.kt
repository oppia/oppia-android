package org.oppia.app.administratorcontrols

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import org.oppia.app.ui.R
import org.oppia.app.activity.InjectableAppCompatActivity
import org.oppia.app.administratorcontrols.appversion.AppVersionActivity
import org.oppia.app.drawer.KEY_NAVIGATION_PROFILE_ID
import org.oppia.app.settings.profile.ProfileListActivity
import javax.inject.Inject

/** Activity for Administrator Controls. */
class AdministratorControlsActivity :
  InjectableAppCompatActivity(),
  RouteToProfileListListener,
  RouteToAppVersionListener {
  @Inject
  lateinit var administratorControlsActivityPresenter: AdministratorControlsActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    administratorControlsActivityPresenter.handleOnCreate()
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
}
