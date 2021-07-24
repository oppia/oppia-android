package org.oppia.android.app.help.thirdparty

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** The activity that will show list of licenses corresponding to a third-party dependency. */
class LicenseListActivity : InjectableAppCompatActivity(), RouteToLicenseTextListener {

  @Inject
  lateinit var licenseListActivityPresenter: LicenseListActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    val index = intent.getIntExtra(THIRD_PARTY_DEPENDENCY_INDEX, 0)
    licenseListActivityPresenter.handleOnCreate(index)
  }

  companion object {
    /** Returns [Intent] for [LicenseListActivity]. */
    const val THIRD_PARTY_DEPENDENCY_INDEX = "ThirdPartyListActivity.dependencyIndex"
    fun createLicenseListActivityIntent(
      context: Context,
      index: Int
    ): Intent {
      val intent = Intent(context, LicenseListActivity::class.java)
      intent.putExtra(THIRD_PARTY_DEPENDENCY_INDEX, index)
      return intent
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
