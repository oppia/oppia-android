package org.oppia.android.app.activity

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.app.translation.AppLanguageWatcherMixin

/**
 * An [AppCompatActivity] that facilitates field injection to child activities and constituent
 * fragments that extend [org.oppia.android.app.fragment.InjectableFragment].
 *
 * This should be extended by all activities which should be system localized or use the
 * device default language.
 */
abstract class InjectableSystemLocalizedAppCompatActivity : InjectableAppCompatActivity() {

  override fun initializeMixin(appLanguageWatcherMixin: AppLanguageWatcherMixin) {
    appLanguageWatcherMixin.initialize(shouldOnlyUseSystemLanguage = true)
  }
}
