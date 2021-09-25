package org.oppia.android.app.translation

/** Activity-level injector for language-specific app components. */
interface AppLanguageActivityInjector {
  /** Returns an [AppLanguageWatcherMixin] from the Dagger graph. */
  fun getAppLanguageWatcherMixin(): AppLanguageWatcherMixin

  /** Returns an [AppLanguageResourceHandler] from the Dagger graph. */
  fun getAppLanguageResourceHandler(): AppLanguageResourceHandler
}
