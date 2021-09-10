package org.oppia.android.app.translation

interface AppLanguageActivityInjector {
  fun getAppLanguageWatcherMixin(): AppLanguageWatcherMixin

  fun getAppLanguageResourceHandler(): AppLanguageResourceHandler
}
