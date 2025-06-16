package org.oppia.android.app.devoptions.platformparameters

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.model.ScreenName.PLATFORM_PARAMETER_ACTIVITY
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.ui.R
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import javax.inject.Inject

/** Activity for platform parameter dashboard of the app. */
class PlatformParameterActivity : InjectableAutoLocalizedAppCompatActivity() {
  @Inject
  lateinit var PlatformParameterActivityPresenter: PlatformParameterActivityPresenter

  @Inject
  lateinit var resourceHandler: AppLanguageResourceHandler

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    PlatformParameterActivityPresenter.handleOnCreate()
    title = resourceHandler.getStringInLocale(R.string.feature_flag_activity_title)
  }

  companion object {
    /** Returns [Intent] for [PlatformParameterActivity]. */
    fun createPlatformParameterActivityIntent(context: Context): Intent {
      return Intent(context, PlatformParameterActivity::class.java).apply {
        decorateWithScreenName(PLATFORM_PARAMETER_ACTIVITY)
      }
    }
  }
}
