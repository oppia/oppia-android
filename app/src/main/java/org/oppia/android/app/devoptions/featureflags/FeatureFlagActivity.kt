package org.oppia.android.app.devoptions.featureflags

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.model.ScreenName.FEATURE_FLAG_ACTIVITY
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.ui.R
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import javax.inject.Inject

/** Activity for feature flag dashboard of the app. */
class FeatureFlagActivity : InjectableAutoLocalizedAppCompatActivity() {
  @Inject
  lateinit var featureFlagActivityPresenter: FeatureFlagActivityPresenter

  @Inject
  lateinit var resourceHandler: AppLanguageResourceHandler

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    featureFlagActivityPresenter.handleOnCreate()
    title = resourceHandler.getStringInLocale(R.string.feature_flag_activity_title)
  }

  companion object {
    /** Returns [Intent] for [FeatureFlagActivity]. */
    fun createFeatureFlagActivityIntent(context: Context): Intent {
      return Intent(context, FeatureFlagActivity::class.java).apply {
        decorateWithScreenName(FEATURE_FLAG_ACTIVITY)
      }
    }
  }
}
