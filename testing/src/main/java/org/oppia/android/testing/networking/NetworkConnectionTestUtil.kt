@file:Suppress("DEPRECATION") // These references are needed for testing prod classes.

package org.oppia.android.testing.networking

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build

import javax.inject.Inject

import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowNetworkInfo

/** Test utility to modify [ShadowNetworkInfo] in tests. */
class NetworkConnectionTestUtil @Inject constructor(private val context: Context) {

  private val connectivityManager by lazy {
    context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
  }

  /**
   * Sets the [ShadowNetworkInfo] during the test.
   *
   * @param status the type of network
   * @param networkState state of the network connection
   */
  fun setNetworkInfo(status: Int, networkState: NetworkInfo.State) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
      // Use ShadowNetworkInfo for older devices (API < 23)
      shadowOf(connectivityManager).setActiveNetworkInfo(
        ShadowNetworkInfo.newInstance(
          /* detailedState= */ null,
          /* type= */ status,
          /* subType= */ 0,
          /* isAvailable= */ true,
          /* state= */ networkState
        )
      )
    } else {
      // For newer devices, use NetworkCapabilities
      val transportType = getTransportTypeFromStatus(status)
      setNetworkCapabilities(networkState == NetworkInfo.State.CONNECTED, transportType)
    }
  }

  /**
   * Sets the [ShadowNetworkCapabilities] during the test for newer devices.
   *
   * @param isConnected whether the network is connected
   * @param transportType the type of transport being used (e.g., WiFi, Cellular)
   */
  private fun setNetworkCapabilities(isConnected: Boolean, transportType: Int) {
    val networkCapabilities = mock(NetworkCapabilities::class.java)

    `when`(networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET))
      .thenReturn(isConnected)

    `when`(networkCapabilities.hasTransport(transportType)).thenReturn(isConnected)

    shadowOf(connectivityManager).setNetworkCapabilities(
      mock(Network::class.java),
      networkCapabilities
    )
  }

  private fun getTransportTypeFromStatus(status: Int): Int {
    return when (status) {
      ConnectivityManager.TYPE_WIFI -> NetworkCapabilities.TRANSPORT_WIFI
      ConnectivityManager.TYPE_MOBILE -> NetworkCapabilities.TRANSPORT_CELLULAR
      ConnectivityManager.TYPE_ETHERNET -> NetworkCapabilities.TRANSPORT_ETHERNET
      ConnectivityManager.TYPE_WIMAX -> NetworkCapabilities.TRANSPORT_WIFI_AWARE
      else -> throw IllegalArgumentException("Unsupported network type: $status")
    }
  }
}
