package org.oppia.android.app.translation

import android.content.res.Configuration
import android.util.Log
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
  private lateinit var displayLocale: OppiaLocale.DisplayLocale

  /**
   * Returns whether this handler's tracked locale has been initialized, that is, whether
   * [initializeLocale] has been called.
   *
   * Once this method returns true, it's guaranteed to stay true for the lifetime of this class.
   */
  fun isInitialized(): Boolean = ::displayLocale.isInitialized

  /**
   * Initializes this handler with the specified initial [OppiaLocale.DisplayLocale].
   *
   * This must be called before any other methods in this class, and it must only be called once for
   * the lifetime of the application.
   */
  private fun initializeLocale(locale: OppiaLocale.DisplayLocale) {
    displayLocale = locale
  }

  /**
   * Initializes the specified [Configuration] to utilize the current display locale.
   *
   * Note that this may change the Android system default locale & trigger some data provider
   * changes for anything that relies on languages or locales (including for content strings & audio
   * translations).
   */
  fun initializeLocaleForActivity(newConfiguration: Configuration) {
    checkIfDisplayLocaleIsInitialized()
    localeController.setAsDefault(displayLocale, newConfiguration)
  }

  /**
   * Updates the display locale to the specified locale, assuming that the handler has already been
   * initialized using [initializeLocale].
   *
   * @return whether the new locale is actually different from the current displayed locale
   */
  fun updateLocale(newLocale: OppiaLocale.DisplayLocale): Boolean {
    checkIfDisplayLocaleIsInitialized()
    Log.e("OLD LOCALE", displayLocale.localeContext.languageDefinition.language.name)
    Log.e("NEW LOCALE", newLocale.localeContext.languageDefinition.language.name)
    return displayLocale.let { oldLocale ->
      displayLocale = newLocale
      return@let oldLocale != newLocale
    }
  }

  /** Returns the current [OppiaLocale.DisplayLocale]. */
  fun getDisplayLocale(): OppiaLocale.DisplayLocale {
    checkIfDisplayLocaleIsInitialized()
    return displayLocale
  }

  private fun checkIfDisplayLocaleIsInitialized() {
    if (!isInitialized()) {
      Log.e("locale not initialized", "locale not initialized")
      initializeLocale(appLanguageLocaleHandler.getDisplayLocale())
    }
  }
}
