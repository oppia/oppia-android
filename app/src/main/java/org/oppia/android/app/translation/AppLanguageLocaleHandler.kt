package org.oppia.android.app.translation

import android.content.res.Configuration
import javax.inject.Inject
import javax.inject.Singleton
import org.oppia.android.domain.locale.LocaleController
import org.oppia.android.domain.locale.OppiaLocale

@Singleton
class AppLanguageLocaleHandler @Inject constructor(
  private val localeController: LocaleController
) {
  private lateinit var displayLocale: OppiaLocale.DisplayLocale

  // TODO: document that this should only be called by bootstrapping activities (like splash).
  fun initializeLocale(locale: OppiaLocale.DisplayLocale) {
    // TODO: think about this check more. It won't work correctly for intent-based entrypoints in
    //  the future.
    check(!::displayLocale.isInitialized) { "Expected to initialize the locale for the first time" }
    displayLocale = locale
  }

  fun notifyPotentialLocaleChange() {
    localeController.notifyPotentialLocaleChange()
  }

  fun initializeLocaleForActivity(newConfiguration: Configuration) {
    localeController.setAsDefault(displayLocale, newConfiguration)
  }

  // TODO: document that this returns whether the locale has changed.
  fun updateLocale(newLocale: OppiaLocale.DisplayLocale): Boolean {
    check(::displayLocale.isInitialized) {
      "Expected locale to already be initialized before being updated"
    }
    return displayLocale.let { oldLocale ->
      displayLocale = newLocale
      return@let oldLocale != newLocale
    }
  }

  fun getDisplayLocale(): OppiaLocale.DisplayLocale = displayLocale
}
