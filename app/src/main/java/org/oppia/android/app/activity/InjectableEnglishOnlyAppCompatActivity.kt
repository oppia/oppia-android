package org.oppia.android.app.activity

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.app.translation.ActivityLanguageMode
import org.oppia.android.app.translation.AppLanguageWatcherMixin

/**
 * An [AppCompatActivity] that facilitates field injection to child activities and constituent
 * fragments that extend [org.oppia.android.app.fragment.InjectableFragment].
 *
 * This should be extended by activities which should always display in English regardless of the
 * user's selected app or system language (e.g. policies pages which show canonical English
 * content).
 */
abstract class InjectableEnglishOnlyAppCompatActivity : InjectableAppCompatActivity() {

  override fun initializeMixin(appLanguageWatcherMixin: AppLanguageWatcherMixin) {
    appLanguageWatcherMixin.initialize(ActivityLanguageMode.USE_ENGLISH)
  }
}
