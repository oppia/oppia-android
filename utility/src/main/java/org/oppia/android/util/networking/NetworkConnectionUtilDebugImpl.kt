package org.oppia.android.util.networking

import org.oppia.android.util.networking.NetworkConnectionDebugUtil.ConnectionStatus.DEFAULT
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [NetworkConnectionDebugUtil] that gets and sets the current [ConnectionStatus] of the device in debug
 * builds and tests.
 */
@Singleton
class NetworkConnectionUtilDebugImpl @Inject constructor(
  private val networkConnectionUtilProdImpl: NetworkConnectionUtilProdImpl
) : NetworkConnectionUtil, NetworkConnectionDebugUtil {

  private var forcedConnectionStatus: ConnectionStatus = DEFAULT

  override fun getCurrentConnectionStatus(): ConnectionStatus {
    val actualConnectionStatus = networkConnectionUtilProdImpl.getCurrentConnectionStatus()
    return if (forcedConnectionStatus == DEFAULT) actualConnectionStatus
    else forcedConnectionStatus
  }

  override fun setCurrentConnectionStatus(connectionStatus: ConnectionStatus) {
    forcedConnectionStatus = connectionStatus
  }

  override fun getForcedConnectionStatus(): ConnectionStatus = forcedConnectionStatus
}
