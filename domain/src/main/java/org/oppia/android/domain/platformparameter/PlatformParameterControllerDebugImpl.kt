package org.oppia.android.domain.platformparameter

import javax.inject.Inject
import kotlinx.coroutines.Deferred
import org.oppia.android.util.data.DataProvider

/**
 * Debug implementation for the controller to manage and synchronize platform parameters and
 * feature flags.
 */
class PlatformParameterControllerDebugImpl @Inject constructor(
  private val platformParameterControllerProdImpl: PlatformParameterControllerProdImpl
) : PlatformParameterController {
  override fun loadParametersAsync(): Deferred<Unit> {
    return platformParameterControllerProdImpl.loadParametersAsync()
  }

  override fun getParameterInitializationStatus(): DataProvider<Boolean> {
    return platformParameterControllerProdImpl.getParameterInitializationStatus()
  }

  override fun downloadRemoteParameters(): DataProvider<Unit> {
    return platformParameterControllerProdImpl.downloadRemoteParameters()
  }

}