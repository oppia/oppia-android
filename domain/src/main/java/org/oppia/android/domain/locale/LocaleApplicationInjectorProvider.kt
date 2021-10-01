package org.oppia.android.domain.locale

/** Provider for [LocaleApplicationInjector]. */
interface LocaleApplicationInjectorProvider {
  /** Returns an [LocaleApplicationInjector]. */
  fun getLocaleApplicationInjector(): LocaleApplicationInjector
}
