package org.oppia.android.util.networking

import android.content.Context
import android.net.ConnectivityManager
import org.oppia.android.util.networking.NetworkConnectionUtil.ConnectionStatus
import javax.inject.Inject

/**
 * [NetworkConnectionUtil] which gets the current [ConnectionStatus] of the device in production builds.
 */
class NetworkConnectionUtilProdImpl @Inject constructor(
  private val context: Context
) : NetworkConnectionUtil {

  override fun getCurrentConnectionStatus(): ConnectionStatus {
    val connectivityManager = context.getSystemService(
      Context.CONNECTIVITY_SERVICE
    ) as ConnectivityManager
    // TODO(#3616): Update to use correct methods according to the SDK version. We can use the
    //  older(current) method for SDK versions <28 and the newer method for SDK versions >=29 and
    //  use an if-statement to choose.
    return connectivityManager.activeNetworkInfo?.let { activeNetwork ->
      val isConnected = activeNetwork.isConnected
      val isLocal = activeNetwork.type ==
        ConnectivityManager.TYPE_WIFI ||
        activeNetwork.type == ConnectivityManager.TYPE_ETHERNET
      val isCellular = activeNetwork.type ==
        ConnectivityManager.TYPE_MOBILE ||
        activeNetwork.type == ConnectivityManager.TYPE_WIMAX
      return@let when {
        isConnected && isLocal -> ConnectionStatus.LOCAL
        isConnected && isCellular -> ConnectionStatus.CELLULAR
        else -> ConnectionStatus.NONE
      }
    } ?: ConnectionStatus.NONE
  }
}
