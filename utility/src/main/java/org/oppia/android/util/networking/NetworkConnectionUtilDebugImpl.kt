package org.oppia.android.util.networking

import org.oppia.android.util.networking.NetworkConnectionUtil.ConnectionStatus
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [NetworkConnectionUtil] that gets and sets the current [ConnectionStatus] of the device in debug builds.
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
   * Forces 'connectionStatus' as the current connection status of the device and returns a
   * [Boolean] indicating result.
   */
  fun setCurrentConnectionStatus(connectionStatus: ConnectionStatus): Boolean {
    if (networkConnectionUtilProdImpl.getCurrentConnectionStatus() == ConnectionStatus.NONE &&
      (connectionStatus == ConnectionStatus.CELLULAR || connectionStatus == ConnectionStatus.LOCAL)
    ) {
      forcedConnectionStatus = null
      return false
    }
    forcedConnectionStatus = connectionStatus
    return true
  }

  /** Returns the 'forcedConnectionStatus' indicating whether the connection status was forced or not. */
  fun getForcedConnectionStatus(): ConnectionStatus? = forcedConnectionStatus
}
