package org.oppia.util.networking

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import androidx.annotation.VisibleForTesting
import javax.inject.Inject
import javax.inject.Singleton

/** Utility to get the current connection status of the device. */
@Singleton
class NetworkConnectionUtil @Inject constructor(private val context: Context) {
  enum class ConnectionStatus {
    LOCAL, // Connected to WIFI or Ethernet
    CELLULAR, // Connected to Mobile or WiMax
    NONE // Not connected to a network
  }
  private var testConnectionStatus: ConnectionStatus? = null

  fun getCurrentConnectionStatus(): ConnectionStatus {
    testConnectionStatus?.let {
      return it
    }
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return connectivityManager.activeNetworkInfo?.let { activeNetwork ->
      val isConnected = activeNetwork.isConnected
      val isLocal = activeNetwork.type == ConnectivityManager.TYPE_WIFI || activeNetwork.type == ConnectivityManager.TYPE_ETHERNET
      val isCellular = activeNetwork.type == ConnectivityManager.TYPE_MOBILE || activeNetwork.type == ConnectivityManager.TYPE_WIMAX
      return@let when {
        isConnected && isLocal -> ConnectionStatus.LOCAL
        isConnected && isCellular -> ConnectionStatus.CELLULAR
        else -> ConnectionStatus.NONE
      }
    } ?: ConnectionStatus.NONE
  }

  @VisibleForTesting(otherwise = VisibleForTesting.NONE)
  fun setCurrentConnectionStatus(status: ConnectionStatus) {
    testConnectionStatus = status
  }
}
