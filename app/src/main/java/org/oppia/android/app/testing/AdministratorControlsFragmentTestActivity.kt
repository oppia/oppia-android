package org.oppia.android.app.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.administratorcontrols.LoadAppVersionListener
import org.oppia.android.app.administratorcontrols.LoadProfileEditListener
import org.oppia.android.app.administratorcontrols.LoadProfileListListener
import org.oppia.android.app.administratorcontrols.RouteToAppVersionListener
import org.oppia.android.app.administratorcontrols.RouteToProfileListListener
import org.oppia.android.app.administratorcontrols.ShowLogoutDialogListener
import org.oppia.android.app.administratorcontrols.appversion.AppVersionActivity
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.settings.profile.ProfileListActivity
import org.oppia.android.app.testing.activity.TestActivity
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import javax.inject.Inject

/** Test activity for [AdministratorControlsFragmentTest]. */
class AdministratorControlsFragmentTestActivity :
  TestActivity(),
  RouteToProfileListListener,
  RouteToAppVersionListener,
  LoadProfileListListener,
  LoadAppVersionListener,
  LoadProfileEditListener,
  ShowLogoutDialogListener {
  @Inject
  lateinit var administratorControlsFragmentTestActivityPresenter:
    AdministratorControlsFragmentTestActivityPresenter

  @Inject
  lateinit var resourceHandler: AppLanguageResourceHandler

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    administratorControlsFragmentTestActivityPresenter.handleOnCreate()
  }

  override fun routeToAppVersion() {
    startActivity(AppVersionActivity.createAppVersionActivityIntent(this))
  }

  override fun routeToProfileList() {
    startActivity(ProfileListActivity.createProfileListActivityIntent(this))
  }

  override fun loadProfileList() {}

  override fun loadAppVersion() {}

  override fun loadProfileEdit(profileId: ProfileId, profileName: String) {}

  override fun showLogoutDialog() {}

  companion object {
    /** Returns an [Intent] to start this activity. */
    fun createAdministratorControlsFragmentTestActivityIntent(
      context: Context,
      profileId: Int?
    ): Intent {
      val profileIdBuilder = profileId?.let { ProfileId.newBuilder().setInternalId(it).build() }
      val intent = Intent(context, AdministratorControlsFragmentTestActivity::class.java)
      if (profileIdBuilder != null) {
        intent.decorateWithUserProfileId(profileIdBuilder)
      }
      return intent
    }
  }
}
