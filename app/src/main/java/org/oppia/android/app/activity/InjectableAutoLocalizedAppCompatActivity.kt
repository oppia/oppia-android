package org.oppia.android.app.activity

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.app.translation.AppLanguageActivityInjector
import org.oppia.android.app.translation.AppLanguageApplicationInjectorProvider

/**
 * An [AppCompatActivity] that facilitates field injection to child activities and constituent
 * fragments that extend [org.oppia.android.app.fragment.InjectableFragment].
 */
abstract class InjectableAutoLocalizedAppCompatActivity : InjectableAppCompatActivity() {

  override fun onInitializeLocalization(
    applicationContext: Context,
    newBase: Context?
  ): Configuration {
    // Given how DataProviders work (i.e. by resolving data races using eventual consistency), it's
    // possible to miss some updates in really unlikely situations. No additional work will be done
    // to prevent these data races unless they're actually hit by users. It shouldn't, in practice,
    // be possible since it requires changing the system language between activity transitions, and
    // in most cases that should result in an activity recreation by the mixin, anyway.
    val appLanguageAppInjectorProvider =
      applicationContext as AppLanguageApplicationInjectorProvider
    val appLanguageAppInjector = appLanguageAppInjectorProvider.getAppLanguageApplicationInjector()
    val appLanguageActivityInjector = activityComponent as AppLanguageActivityInjector
    val appLanguageLocaleHandler = appLanguageAppInjector.getAppLanguageHandler()
    val appLanguageWatcherMixin = appLanguageActivityInjector.getAppLanguageWatcherMixin()
    appLanguageWatcherMixin.initialize(false)

    return Configuration(newBase?.resources?.configuration).also { newConfiguration ->
      appLanguageLocaleHandler.initializeLocaleForActivity(newConfiguration)
    }
  }
}
