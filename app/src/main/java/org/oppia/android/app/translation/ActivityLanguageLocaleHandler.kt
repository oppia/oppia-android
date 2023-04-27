package org.oppia.android.app.translation

import android.content.res.Configuration
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
 *
 */
class ActivityLanguageLocaleHandler @Inject constructor(
  private val localeController: LocaleController,
  private val appLanguageLocaleHandler: AppLanguageLocaleHandler
) {

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
   * Updates the display locale to the specified locale, assuming that the handler has already been
   * initialized lazily by [AppLanguageLocaleHandler].
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
