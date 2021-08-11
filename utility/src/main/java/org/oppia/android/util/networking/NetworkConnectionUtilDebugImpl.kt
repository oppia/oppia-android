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

  /** Forces [connectionStatus] as the current connection status of the device. */
  override fun setCurrentConnectionStatus(connectionStatus: ConnectionStatus) {
    forcedConnectionStatus = connectionStatus
  }

  /** Returns the [forcedConnectionStatus] indicating whether the connection status was forced or not. */
  override fun getForcedConnectionStatus(): ConnectionStatus = forcedConnectionStatus
}
