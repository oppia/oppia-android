@file:Suppress("DEPRECATION") // These references are needed for testing prod classes.

package org.oppia.android.testing.networking

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowNetwork
import org.robolectric.shadows.ShadowNetworkCapabilities
import org.robolectric.shadows.ShadowNetworkInfo
import javax.inject.Inject

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
    shadowOf(connectivityManager).setActiveNetworkInfo(
      ShadowNetworkInfo.newInstance(
        /* detailedState= */ null,
        /* type= */ status,
        /* subType= */ 0,
        /* isAvailable= */ true,
        /* state= */ networkState
      )
    )
  }

  /**
   * Sets the capabilities of the active network during the test.
   *
   * @param transportType the transport type backing the active network
   */
  fun setNetworkCapabilities(transportType: Int) {
    val network = ShadowNetwork.newInstance(/* netId= */ transportType)
    val networkInfo = ShadowNetworkInfo.newInstance(
      /* detailedState= */ null,
      /* type= */ transportType,
      /* subType= */ 0,
      /* isAvailable= */ true,
      /* state= */ NetworkInfo.State.CONNECTED
    )
    val capabilities = ShadowNetworkCapabilities.newInstance()
    shadowOf(capabilities).addTransportType(transportType)
    shadowOf(connectivityManager).apply {
      addNetwork(network, networkInfo)
      setActiveNetworkInfo(networkInfo)
      setNetworkCapabilities(network, capabilities)
      setDefaultNetworkActive(true)
    }
  }

  /** Removes the active network during the test, leaving the device without a connection. */
  fun setNoActiveNetwork() {
    shadowOf(connectivityManager).setDefaultNetworkActive(false)
  }
}
