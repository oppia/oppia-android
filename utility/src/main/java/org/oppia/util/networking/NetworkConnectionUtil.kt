package org.oppia.util.networking

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import javax.inject.Inject

class NetworkConnectionUtil @Inject constructor(private val context: Context) {
  enum class ConnectionStatus {
    WIFI, CELLULAR, NONE
  }

  fun getCurrentConnectionStatus(): ConnectionStatus {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
    val isConnected: Boolean = activeNetwork?.isConnected == true
    val isWiFi: Boolean = activeNetwork?.type == ConnectivityManager.TYPE_WIFI
    val isMob: Boolean = activeNetwork?.type == ConnectivityManager.TYPE_MOBILE
    if(isConnected) {
      if (isWiFi) {
        return ConnectionStatus.WIFI
      } else if (isMob) {
        return ConnectionStatus.CELLULAR
      }
    }
    return ConnectionStatus.NONE
  }
}