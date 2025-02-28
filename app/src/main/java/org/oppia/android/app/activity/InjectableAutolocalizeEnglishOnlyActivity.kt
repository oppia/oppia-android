package org.oppia.android.app.activity

import org.oppia.android.app.translation.AppLanguageWatcherMixin
import org.oppia.android.app.translation.LanguageSource

/**
 * An [AppCompatActivity] that always uses English as its locale, ensuring left-to-right alignment.
 */
abstract class InjectableAutolocalizeEnglishOnlyActivity : InjectableAppCompatActivity() {
  override fun initializeMixin(appLanguageWatcherMixin: AppLanguageWatcherMixin) {
    appLanguageWatcherMixin.initialize(LanguageSource.ENGLISH)
  }
}
