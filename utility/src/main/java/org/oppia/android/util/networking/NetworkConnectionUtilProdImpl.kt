package org.oppia.android.util.networking

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.annotation.RequiresApi
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
    // TODO(#3616): Update to use correct methods according to the SDK version. We can use the
    //  older (current) method for SDK versions < 28 and the newer method for SDK versions >= 29 and
    //  use an if-statement to choose.

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      getConnectionStatusForNewerDevices(connectivityManager)
    } else {
      getConnectionStatusForOlderDevices(connectivityManager)
    }
  }

  /** Uses NetworkCapabilities for SDK versions >= 23. */
  @RequiresApi(Build.VERSION_CODES.M)
  private fun getConnectionStatusForNewerDevices(
    connectivityManager: ConnectivityManager
  ): ConnectionStatus {

    val network = connectivityManager.activeNetwork
    val capabilities = connectivityManager.getNetworkCapabilities(network)
    return capabilities?.let {
      when {
        it.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
          it.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> LOCAL
        it.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
          it.hasTransport(NetworkCapabilities.TRANSPORT_WIFI_AWARE) -> CELLULAR
        else -> NONE
      }
    } ?: NONE
  }

  /** Uses the older `activeNetworkInfo` for SDK versions < 23. */
  @Suppress("DEPRECATION")
  private fun getConnectionStatusForOlderDevices(
    connectivityManager: ConnectivityManager
  ): ConnectionStatus {

    val activeNetworkInfo = connectivityManager.activeNetworkInfo
    return activeNetworkInfo?.let { activeNetwork ->
      val isConnected = activeNetwork.isConnected
      val isLocal = activeNetwork.type == ConnectivityManager.TYPE_WIFI ||
        activeNetwork.type == ConnectivityManager.TYPE_ETHERNET
      val isCellular = activeNetwork.type == ConnectivityManager.TYPE_MOBILE ||
        activeNetwork.type == ConnectivityManager.TYPE_WIMAX
      when {
        isConnected && isLocal -> LOCAL
        isConnected && isCellular -> CELLULAR
        else -> NONE
      }
    } ?: NONE
  }
}
