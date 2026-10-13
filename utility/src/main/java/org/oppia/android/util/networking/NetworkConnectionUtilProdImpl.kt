package org.oppia.android.util.networking

import android.annotation.TargetApi
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import org.oppia.android.util.networking.NetworkConnectionUtil.ProdConnectionStatus.CELLULAR
import org.oppia.android.util.networking.NetworkConnectionUtil.ProdConnectionStatus.LOCAL
import org.oppia.android.util.networking.NetworkConnectionUtil.ProdConnectionStatus.NONE
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
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      connectivityManager.retrieveConnectionStatusAboveApi28()
    } else connectivityManager.retrieveConnectionStatusBelowApi29()
  }

  @TargetApi(Build.VERSION_CODES.Q)
  private fun ConnectivityManager.retrieveConnectionStatusAboveApi28(): ConnectionStatus {
    val network = activeNetwork ?: return NONE
    val capabilities = getNetworkCapabilities(network) ?: return NONE
    val isConnected = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    val isLocal = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
      capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    val isCellular = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
      capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI_AWARE)
    return when {
      isConnected && isLocal -> LOCAL
      isConnected && isCellular -> CELLULAR
      else -> NONE
    }
  }

  @Suppress("DEPRECATION") // This is needed for old devices. New devices use the new API per above.
  private fun ConnectivityManager.retrieveConnectionStatusBelowApi29(): ConnectionStatus {
    return activeNetworkInfo?.let { activeNetwork ->
      val isConnected = activeNetwork.isConnected
      val isLocal = activeNetwork.type ==
        ConnectivityManager.TYPE_WIFI ||
        activeNetwork.type == ConnectivityManager.TYPE_ETHERNET
      val isCellular = activeNetwork.type ==
        ConnectivityManager.TYPE_MOBILE ||
        activeNetwork.type == ConnectivityManager.TYPE_WIMAX
      return@let when {
        isConnected && isLocal -> LOCAL
        isConnected && isCellular -> CELLULAR
        else -> NONE
      }
    } ?: NONE
  }
}
