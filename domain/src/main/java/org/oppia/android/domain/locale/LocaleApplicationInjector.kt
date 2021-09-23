package org.oppia.android.domain.locale

/** Application-level injector for locale-related domain components. */
interface LocaleApplicationInjector {
  /** Returns an [LocaleController] from the Dagger graph. */
  fun getLocaleController(): LocaleController
}
