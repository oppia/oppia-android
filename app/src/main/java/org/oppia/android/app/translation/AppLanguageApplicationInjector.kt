package org.oppia.android.app.translation

/** Application-level injector for language-specific app components. */
interface AppLanguageApplicationInjector {
  /** Returns an [AppLanguageLocaleHandler] from the Dagger graph. */
  fun getAppLanguageHandler(): AppLanguageLocaleHandler
}
