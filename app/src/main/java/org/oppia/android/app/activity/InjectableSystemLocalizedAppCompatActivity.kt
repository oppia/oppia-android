package org.oppia.android.app.activity

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.app.translation.AppLanguageWatcherMixin

/**
 * An [AppCompatActivity] that facilitates field injection to child activities and constituent
 * fragments that extend [org.oppia.android.app.fragment.InjectableFragment].
 */
abstract class InjectableSystemLocalizedAppCompatActivity : InjectableAppCompatActivity() {

  override fun initializeMixin(appLanguageWatcherMixin: AppLanguageWatcherMixin) {
    appLanguageWatcherMixin.initialize(shouldUseSystemLanguage = true)
  }
}
