package org.oppia.android.util.networking

import org.oppia.android.util.networking.NetworkConnectionUtil.ConnectionStatus
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Debug implementation of [NetworkConnectionUtil] which gets and sets the current
 * [ConnectionStatus] of the device.
 */
@Singleton
class NetworkConnectionUtilDebugImpl @Inject constructor(
  private val networkConnectionUtilProdImpl: NetworkConnectionUtilProdImpl
) : NetworkConnectionUtil {

  private var forcedConnectionStatus: ConnectionStatus? = null

  override fun getCurrentConnectionStatus(): ConnectionStatus {
    forcedConnectionStatus?.let {
      return it
    }
    return networkConnectionUtilProdImpl.getCurrentConnectionStatus()
  }

  /**
   * Sets the current connection status of the device.
   *
   * @param connectionStatus: refers to the [ConnectionStatus] which needs to be set.
   */
  fun setCurrentConnectionStatus(connectionStatus: ConnectionStatus) {
    forcedConnectionStatus = connectionStatus
  }

  /** Returns the 'forcedConnectionStatus' indicating whether the connection status was forced or not. */
  fun getForcedConnectionStatus(): ConnectionStatus? = forcedConnectionStatus
}
