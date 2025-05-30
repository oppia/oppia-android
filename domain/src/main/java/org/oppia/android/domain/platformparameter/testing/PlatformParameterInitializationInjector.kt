package org.oppia.android.domain.platformparameter.testing

import org.oppia.android.domain.platformparameter.PlatformParameterController
import org.oppia.android.testing.data.DataProviderTestMonitor
import org.oppia.android.testing.threading.TestCoroutineDispatchers

interface PlatformParameterInitializationInjector {
  fun getTestCoroutineDispatchers(): TestCoroutineDispatchers

  fun getDataProviderTestMonitorFactory(): DataProviderTestMonitor.Factory

  fun getPlatformParameterController(): PlatformParameterController
}
