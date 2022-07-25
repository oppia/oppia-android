package org.oppia.android.testing.networking

import android.content.Context
import android.net.ConnectivityManager
import org.robolectric.Shadows.shadowOf
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
  @Suppress("DEPRECATION") // Deprecated NetworkInfo is used for older SDK interoperability.
  fun setNetworkInfo(status: Int, networkState: android.net.NetworkInfo.State) {
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
}
