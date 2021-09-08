package org.oppia.android.app.application

import org.oppia.android.app.translation.AppLanguageApplicationInjector
import org.oppia.android.app.translation.AppLanguageApplicationInjectorProvider
import org.oppia.android.util.data.DataProvidersInjector
import org.oppia.android.util.data.DataProvidersInjectorProvider

/** Provider for [ApplicationInjector]. The application context will implement this interface. */
interface ApplicationInjectorProvider : DataProvidersInjectorProvider,
  AppLanguageApplicationInjectorProvider {
  fun getApplicationInjector(): ApplicationInjector

  override fun getDataProvidersInjector(): DataProvidersInjector = getApplicationInjector()

  override fun getAppLanguageApplicationInjector(): AppLanguageApplicationInjector =
    getApplicationInjector()
}
