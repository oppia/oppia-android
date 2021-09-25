package org.oppia.android.app.translation

/** Provider for [AppLanguageActivityInjector]. */
interface AppLanguageActivityInjectorProvider {
  /** Returns an [AppLanguageActivityInjector]. */
  fun getAppLanguageActivityInjector(): AppLanguageActivityInjector
}
