package org.oppia.android.app.activity

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.app.translation.AppLanguageWatcherMixin

/**
 * An [AppCompatActivity] that facilitates field injection to child activities and constituent
 * fragments that extend [org.oppia.android.app.fragment.InjectableFragment].
 *
 * This should be extended by all activities which should be automatically localized based on the
 * user's selected app language.
 */
abstract class InjectableAutoLocalizedAppCompatActivity : InjectableAppCompatActivity() {

  override fun initializeMixin(appLanguageWatcherMixin: AppLanguageWatcherMixin) {
    appLanguageWatcherMixin.initialize(shouldOnlyUseSystemLanguage = false)
  }
}
