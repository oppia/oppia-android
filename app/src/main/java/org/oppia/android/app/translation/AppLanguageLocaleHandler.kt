package org.oppia.android.app.translation

import android.content.res.Configuration
import android.util.Log
import org.oppia.android.domain.locale.LocaleController
import org.oppia.android.util.locale.OppiaLocale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Application-scoped handler for the current locale configured for all app layer components.
 *
 * Note that this handler acts as the single source of truth for the current display locale that
 * should be used for all app string formatting & decision making. The handler is automatically
 * initialized during app bootstrapping in splash activity, and kept up-to-date with
 * [AppLanguageWatcherMixin].
 *
 * This class should never be used directly. Instead of accessing the locale directly, use
 * [AppLanguageResourceHandler].
 */
@Singleton
class AppLanguageLocaleHandler @Inject constructor(
  private val localeController: LocaleController
) {
  private lateinit var displayLocale: OppiaLocale.DisplayLocale
  lateinit var isDisplayLocaleUpdated: String

  /**
   * Returns whether this handler's tracked locale has been initialized, that is, whether
   * [initializeLocale] has been called.
   *
   * Once this method returns true, it's guaranteed to stay true for the lifetime of this class.
   */
  fun isInitialized(): Boolean = ::displayLocale.isInitialized

  /**
   * Returns whether [isDisplayLocaleUpdated] has been initialized, that is, whether
   *
   * Once this method returns true, it's guaranteed to stay true for the lifetime of this class.
   */
  fun isDisplayLocaleUpdatedInitialized(): Boolean = ::isDisplayLocaleUpdated.isInitialized

  /**
   * Initializes this handler with the specified initial [OppiaLocale.DisplayLocale].
   *
   * This must be called before any other methods in this class, and it must only be called once for
   * the lifetime of the application.
   */
  fun initializeLocale(locale: OppiaLocale.DisplayLocale) {
    check(!isInitialized()) {
      "Expected to initialize the locale for the first time. If this is in a test, did you use" +
        " InitializeDefaultLocaleRule?"
    }
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
    verifyDisplayLocaleIsInitialized()
    localeController.setAsDefault(displayLocale, newConfiguration)
  }

  /**
   * Updates the display locale to the specified locale, assuming that the handler has already been
   * initialized using [initializeLocale].
   *
   * @return whether the new locale is actually different from the current displayed locale
   */
  fun updateLocale(newLocale: OppiaLocale.DisplayLocale): Boolean {
    verifyDisplayLocaleIsInitialized()
    Log.e(
      "AppLanguageLocaleHandle",
      "updateLocale" + "newlocale" + newLocale.localeContext.languageDefinition.language.name
    )
    Log.e(
      "AppLanguageLocaleHandle",
      "updateLocale" + "oldLocale" + displayLocale.localeContext.languageDefinition.language.name
    )
    if (isDisplayLocaleUpdatedInitialized()) {
      Log.e("isDisplayLocaleUpdated", isDisplayLocaleUpdated)
    }

    return displayLocale.let { oldLocale ->
      displayLocale = newLocale
      isDisplayLocaleUpdated = (oldLocale != newLocale).toString()
      return@let oldLocale != newLocale
    }
  }

  /** Returns the current [OppiaLocale.DisplayLocale]. */
  fun getDisplayLocale(): OppiaLocale.DisplayLocale {
    verifyDisplayLocaleIsInitialized()
    return displayLocale
  }

  private fun verifyDisplayLocaleIsInitialized() {
    check(isInitialized()) {
      "Expected locale to be initialized. If this is in a test, did you remember to include" +
        " InitializeDefaultLocaleRule?"
    }
  }
}
