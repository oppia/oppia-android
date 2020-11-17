package org.oppia.android.app.splash

import android.content.res.Configuration
import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.deprecation.DeprecationNoticeExitAppListener
import java.util.Locale
import javax.inject.Inject

/** An activity that shows a temporary loading page until the app is fully loaded then navigates to [ProfileActivity]. */
class SplashActivity : InjectableAppCompatActivity(), DeprecationNoticeExitAppListener {

  @Inject
  lateinit var splashActivityPresenter: SplashActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    // Reference: https://stackoverflow.com/a/41101742
    val locale = Locale("hie")
    val conf: Configuration = resources.configuration
    conf.setLocale(locale)
    val metrics = resources.displayMetrics
    resources.updateConfiguration(conf, metrics)
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    splashActivityPresenter.handleOnCreate()
  }

  override fun onCloseAppButtonClicked() = splashActivityPresenter.handleOnCloseAppButtonClicked()
}
