package org.oppia.android.util.networking

import org.oppia.android.util.networking.NetworkConnectionUtil.ConnectionStatus
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [NetworkConnectionUtil] that gets and sets the current [ConnectionStatus] of the device in debug
 * builds and tests.
 */
@Singleton
class NetworkConnectionUtilDebugImpl @Inject constructor(
  private val networkConnectionUtilProdImpl: NetworkConnectionUtilProdImpl
) : NetworkConnectionUtil {

  private var forcedConnectionStatus: ConnectionStatus = ConnectionStatus.DEFAULT

  override fun getCurrentConnectionStatus(): ConnectionStatus {
    val actualConnectionStatus = networkConnectionUtilProdImpl.getCurrentConnectionStatus()
    return if (forcedConnectionStatus == ConnectionStatus.DEFAULT) actualConnectionStatus
    else forcedConnectionStatus
  }

  /** Forces [connectionStatus] as the current connection status of the device. */
  fun setCurrentConnectionStatus(connectionStatus: ConnectionStatus) {
    forcedConnectionStatus = connectionStatus
  }

  /** Returns the [forcedConnectionStatus] indicating whether the connection status was forced or not. */
  fun getForcedConnectionStatus(): ConnectionStatus = forcedConnectionStatus
}
