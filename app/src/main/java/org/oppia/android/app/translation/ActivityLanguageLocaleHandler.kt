package org.oppia.android.app.translation

import android.content.res.Configuration
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.domain.locale.LocaleController
import org.oppia.android.util.locale.OppiaLocale
import javax.inject.Inject

/**
 * Activity-scoped handler for the current locale configured for all app layer components.
 *
 * Note that this handler acts as the single source of truth for the current display locale that
 * should be used for all activity string formatting. The handler is automatically
 * initialized by [AppLanguageLocaleHandler] with global app language and updated automatically if user
 * changes language preferences and kept up-to-date with
 * [AppLanguageWatcherMixin].
 */
@ActivityScope
class ActivityLanguageLocaleHandler @Inject constructor(
  private val localeController: LocaleController,
  private val appLanguageLocaleHandler: AppLanguageLocaleHandler
) {

  /**
   * Holds an Activity localized state of [OppiaLocale.DisplayLocale] which is loaded
   * lazily from [AppLanguageLocaleHandler.getDisplayLocale] and updated when language
   * preference changes via [ActivityLanguageLocaleHandler.updateLocale].
   */
  val displayLocale: OppiaLocale.DisplayLocale by lazy {
    appLanguageLocaleHandler.getDisplayLocale()
  }

  /**
   * Initializes the specified [Configuration] to utilize the current display locale.
   *
   * Note that this may change the Android system default locale & trigger some data provider
   * changes for anything that relies on languages or locales (including for content strings & audio
   * translations).
   */
  fun initializeLocaleForActivity(newConfiguration: Configuration) {
    localeController.setAsDefault(displayLocale, newConfiguration)
  }

  /**
   * Attempts to change the locale for the system-level and won't actually change this handler's locale.
   * This handler never changes its locale since an activity requires re-creation in order to apply a new language.
   *
   * @return whether the new locale is actually different from the current displayed locale
   */
  fun updateLocale(newLocale: OppiaLocale.DisplayLocale): Boolean {
    return displayLocale.let { oldLocale ->
      appLanguageLocaleHandler.updateLocale(newLocale)
      return@let oldLocale != newLocale
    }
  }
}
