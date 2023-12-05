package org.oppia.android.app.help.thirdparty

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.model.LicenseListActivityArguments
import org.oppia.android.app.model.ScreenName.LICENSE_LIST_ACTIVITY
import org.oppia.android.util.extensions.getProtoExtra
import org.oppia.android.util.extensions.putProtoExtra
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import javax.inject.Inject

/** The activity that will show list of licenses corresponding to a third-party dependency. */
class LicenseListActivity : InjectableAutoLocalizedAppCompatActivity(), RouteToLicenseTextListener {

  @Inject
  lateinit var licenseListActivityPresenter: LicenseListActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    val args = intent.getProtoExtra(
      LICENSE_LIST_ACTIVITY_ARGUMENTS_KEY,
      LicenseListActivityArguments.getDefaultInstance()
    )
    val dependencyIndex = args?.dependencyIndex ?: 0
    licenseListActivityPresenter.handleOnCreate(dependencyIndex, false)
  }

  companion object {
    /** Argument key for LicenseListActivity. */
    private const val LICENSE_LIST_ACTIVITY_ARGUMENTS_KEY = "LicenseListActivity.arguments"

    /** Returns [Intent] for [LicenseListActivity]. */
    fun createLicenseListActivityIntent(
      context: Context,
      dependencyIndex: Int
    ): Intent {
      return Intent(context, LicenseListActivity::class.java).apply {
        val args = LicenseListActivityArguments.newBuilder().apply {
          this.dependencyIndex = dependencyIndex
        }.build()
        putProtoExtra(LICENSE_LIST_ACTIVITY_ARGUMENTS_KEY, args)
        decorateWithScreenName(LICENSE_LIST_ACTIVITY)
      }
    }
  }

  override fun onRouteToLicenseText(dependencyIndex: Int, licenseIndex: Int) {
    startActivity(
      LicenseTextViewerActivity.createLicenseTextViewerActivityIntent(
        this,
        dependencyIndex,
        licenseIndex
      )
    )
  }
}
