package org.oppia.android.app.help.thirdparty

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.model.ScreenName.LICENSE_LIST_ACTIVITY
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/** The activity that will show list of licenses corresponding to a third-party dependency. */
class LicenseListActivity : InjectableAutoLocalizedAppCompatActivity(), RouteToLicenseTextListener {

  @Inject
  lateinit var licenseListActivityPresenter: LicenseListActivityPresenter
  private lateinit var profileId: ProfileId

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    val dependencyIndex = intent.getIntExtra(THIRD_PARTY_DEPENDENCY_INDEX, 0)
    profileId = intent.extractCurrentUserProfileId()
    licenseListActivityPresenter.handleOnCreate(dependencyIndex, false)
  }

  companion object {
    private const val THIRD_PARTY_DEPENDENCY_INDEX = "LicenseListActivity.dependency_index"

    /** Returns [Intent] for [LicenseListActivity]. */
    fun createLicenseListActivityIntent(
      context: Context,
      dependencyIndex: Int,
      profileId: ProfileId
    ): Intent {
      val intent = Intent(context, LicenseListActivity::class.java)
      intent.putExtra(THIRD_PARTY_DEPENDENCY_INDEX, dependencyIndex)
      intent.decorateWithScreenName(LICENSE_LIST_ACTIVITY)
      intent.decorateWithUserProfileId(profileId)
      return intent
    }
  }

  override fun onRouteToLicenseText(dependencyIndex: Int, licenseIndex: Int) {
    startActivity(
      LicenseTextViewerActivity.createLicenseTextViewerActivityIntent(
        this,
        dependencyIndex,
        licenseIndex,
        profileId
      )
    )
  }
}
