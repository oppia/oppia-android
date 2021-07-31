package org.oppia.android.util.networking

import org.oppia.android.util.networking.NetworkConnectionUtil.ConnectionStatus
import org.oppia.android.util.networking.NetworkConnectionUtil.ConnectionStatus.CELLULAR
import org.oppia.android.util.networking.NetworkConnectionUtil.ConnectionStatus.DEFAULT
import org.oppia.android.util.networking.NetworkConnectionUtil.ConnectionStatus.LOCAL
import org.oppia.android.util.networking.NetworkConnectionUtil.ConnectionStatus.NONE
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

  private var forcedConnectionStatus: ConnectionStatus = DEFAULT

  override fun getCurrentConnectionStatus(): ConnectionStatus {
    val actualConnectionStatus = networkConnectionUtilProdImpl.getCurrentConnectionStatus()
    if (actualConnectionStatus == NONE) {
      forcedConnectionStatus = DEFAULT
    }
    if (forcedConnectionStatus == DEFAULT) {
      return actualConnectionStatus
    }
    return forcedConnectionStatus
  }

  /**
   * Forces [connectionStatus] as the current connection status of the device and returns a
   * [Boolean] indicating whether the operation was successful or not. The [Boolean] will be false
   * when we try to force an impossible situation, i.e., forcing [CELLULAR] or [WIFI] network when
   * there is no actual network connection. In all other cases the [Boolean] will be true.
   */
  fun setCurrentConnectionStatus(forcedStatus: ConnectionStatus): Boolean {
    val actualStatus = networkConnectionUtilProdImpl.getCurrentConnectionStatus()
    if (actualStatus == NONE && (forcedStatus == CELLULAR || forcedStatus == LOCAL)) {
      forcedConnectionStatus = DEFAULT
      return false
    }
    forcedConnectionStatus = forcedStatus
    return true
  }

  /** Returns the [forcedConnectionStatus] indicating whether the connection status was forced or not. */
  fun getForcedConnectionStatus(): ConnectionStatus = forcedConnectionStatus
}
