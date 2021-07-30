package org.oppia.android.util.networking

import org.oppia.android.util.networking.NetworkConnectionUtil.ConnectionStatus
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [NetworkConnectionUtil] that gets and sets the current [ConnectionStatus] of the device in debug
 * builds and tests.
 */
@Singleton
class DebugNetworkConnectionUtil @Inject constructor(
  private val prodNetworkConnectionUtil: ProdNetworkConnectionUtil
) : NetworkConnectionUtil {

  private var forcedConnectionStatus: ConnectionStatus? = null

  override fun getCurrentConnectionStatus(): ConnectionStatus {
    val actualConnectionStatus = prodNetworkConnectionUtil.getCurrentConnectionStatus()
    if (actualConnectionStatus == ConnectionStatus.NONE) {
      forcedConnectionStatus = null
    }
    return forcedConnectionStatus ?: actualConnectionStatus
  }

  /**
   * Forces [connectionStatus] as the current connection status of the device and returns a
   * [Boolean] indicating whether the operation was successful or not. The [Boolean] will be false
   * when we try to force an impossible situation, i.e., forcing [CELLULAR] or [WIFI] network when
   * there is no actual network connection. In all other cases the [Boolean] will be true.
   */
  fun setCurrentConnectionStatus(connectionStatus: ConnectionStatus): Boolean {
    if (prodNetworkConnectionUtil.getCurrentConnectionStatus() == ConnectionStatus.NONE &&
      (connectionStatus == ConnectionStatus.CELLULAR || connectionStatus == ConnectionStatus.LOCAL)
    ) {
      forcedConnectionStatus = null
      return false
    }
    forcedConnectionStatus = connectionStatus
    return true
  }

  /** Returns the [forcedConnectionStatus] indicating whether the connection status was forced or not. */
  fun getForcedConnectionStatus(): ConnectionStatus? = forcedConnectionStatus
}
