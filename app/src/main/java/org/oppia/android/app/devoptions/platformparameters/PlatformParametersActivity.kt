package org.oppia.android.app.devoptions.platformparameters

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.model.ScreenName.PLATFORM_PARAMETERS_ACTIVITY
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.ui.R
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import javax.inject.Inject

/** Activity for platform parameter dashboard of the app. */
class PlatformParametersActivity : InjectableAutoLocalizedAppCompatActivity() {
  @Inject
  lateinit var PlatformParametersActivityPresenter: PlatformParametersActivityPresenter

  @Inject
  lateinit var resourceHandler: AppLanguageResourceHandler

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    PlatformParametersActivityPresenter.handleOnCreate()
    title = resourceHandler.getStringInLocale(R.string.platform_parameters_activity_title)
  }

  companion object {
    /** Returns [Intent] for [PlatformParametersActivity]. */
    fun createPlatformParametersActivityIntent(context: Context): Intent {
      return Intent(context, PlatformParametersActivity::class.java).apply {
        decorateWithScreenName(PLATFORM_PARAMETERS_ACTIVITY)
      }
    }
  }
}
