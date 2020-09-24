package org.oppia.android.app.application

import org.oppia.util.data.DataProvidersInjector
import org.oppia.util.data.DataProvidersInjectorProvider

/** Provider for [ApplicationInjector]. The application context will implement this interface. */
interface ApplicationInjectorProvider : DataProvidersInjectorProvider {
  fun getApplicationInjector(): ApplicationInjector

  override fun getDataProvidersInjector(): DataProvidersInjector = getApplicationInjector()
}
