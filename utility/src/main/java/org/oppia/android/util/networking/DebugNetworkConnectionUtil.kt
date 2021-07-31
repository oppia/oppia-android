package org.oppia.android.util.networking

import org.oppia.android.util.networking.NetworkConnectionUtil.ConnectionStatus
import org.oppia.android.util.networking.NetworkConnectionUtil.ConnectionStatus.CELLULAR
import org.oppia.android.util.networking.NetworkConnectionUtil.ConnectionStatus.DEFAULT
import org.oppia.android.util.networking.NetworkConnectionUtil.ConnectionStatus.LOCAL
import org.oppia.android.util.networking.NetworkConnectionUtil.ConnectionStatus.NONE
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [NetworkConnectionUtil] that gets and sets the current [ConnectionStatus] of the device in debug builds.
 */
@Singleton
class DebugNetworkConnectionUtil @Inject constructor(
  private val prodNetworkConnectionUtil: ProdNetworkConnectionUtil
) : NetworkConnectionUtil {

  private var forcedConnectionStatus: ConnectionStatus = DEFAULT

  override fun getCurrentConnectionStatus(): ConnectionStatus {
    val actualConnectionStatus = prodNetworkConnectionUtil.getCurrentConnectionStatus()
    if (actualConnectionStatus == NONE) {
      forcedConnectionStatus = DEFAULT
    }
    if (forcedConnectionStatus == DEFAULT) {
      return actualConnectionStatus
    }
    return forcedConnectionStatus
  }

  /**
   * Forces [forcedStatus] as the current connection status of the device and returns a
   * [Boolean] indicating result.
   */
  fun setCurrentConnectionStatus(forcedStatus: ConnectionStatus): Boolean {
    val actualStatus = prodNetworkConnectionUtil.getCurrentConnectionStatus()
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
