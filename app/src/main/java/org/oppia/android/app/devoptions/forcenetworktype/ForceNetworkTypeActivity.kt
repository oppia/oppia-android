package org.oppia.android.app.devoptions.forcenetworktype

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.model.ScreenName.FORCE_NETWORK_TYPE_ACTIVITY
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import javax.inject.Inject

/** Activity for forcing the network mode for the app. */
class ForceNetworkTypeActivity : InjectableAppCompatActivity() {
  @Inject
  lateinit var forceNetworkTypeActivityPresenter: ForceNetworkTypeActivityPresenter

  @Inject
  lateinit var resourceHandler: AppLanguageResourceHandler

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    forceNetworkTypeActivityPresenter.handleOnCreate()
    title = resourceHandler.getStringInLocale(R.string.force_network_type_activity_title)
  }

  companion object {
    /** Returns [Intent] for [ForceNetworkTypeActivity]. */
    fun createForceNetworkTypeActivityIntent(context: Context): Intent {
      return Intent(context, ForceNetworkTypeActivity::class.java).apply {
        decorateWithScreenName(FORCE_NETWORK_TYPE_ACTIVITY)
      }
    }
  }
}
