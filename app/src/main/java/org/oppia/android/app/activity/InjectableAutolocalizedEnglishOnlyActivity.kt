package org.oppia.android.app.activity

import org.oppia.android.app.model.ForcedActivityLanguageMode
import org.oppia.android.app.translation.AppLanguageWatcherMixin

/**
 * An [InjectableAppCompatActivity] that facilitates field injection to child activities and constituent
 * fragments that extend [org.oppia.android.app.fragment.InjectableFragment].
 *
 * This should be extended by all activities which should be automatically localized to English only.
 */
abstract class InjectableAutolocalizedEnglishOnlyActivity : InjectableAppCompatActivity() {

  override fun initializeMixin(appLanguageWatcherMixin: AppLanguageWatcherMixin) {
    appLanguageWatcherMixin.initialize(ForcedActivityLanguageMode.USE_ENGLISH)
  }
}