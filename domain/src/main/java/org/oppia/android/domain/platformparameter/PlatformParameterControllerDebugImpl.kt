package org.oppia.android.domain.platformparameter

import kotlinx.coroutines.Deferred
import org.oppia.android.util.data.DataProvider
import javax.inject.Inject

class PlatformParameterControllerDebugImpl @Inject constructor(
  private val platformParameterControllerProdImpl: PlatformParameterControllerProdImpl
) : PlatformParameterController {
  override fun loadParametersAsync(): Deferred<Any?> {
    return platformParameterControllerProdImpl.loadParametersAsync()
  }

  override fun getParameterInitializationStatus(): DataProvider<Boolean> {
    return platformParameterControllerProdImpl.getParameterInitializationStatus()
  }

  override fun downloadRemoteParameters(): DataProvider<Any?> {
    return platformParameterControllerProdImpl.downloadRemoteParameters()
  }
}
