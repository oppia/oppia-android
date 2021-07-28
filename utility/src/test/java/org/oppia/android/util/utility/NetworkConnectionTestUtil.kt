package org.oppia.android.util.utility

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowNetworkInfo

/** Test utility to modify [ShadowNetworkInfo] during the test. */
class NetworkConnectionTestUtil {

  companion object {
    /**
     * Sets the [ShadowNetworkInfo] during the test.
     *
     * @param context: the context of the dummy network connection.
     * @param status: the type of network.
     * @param networkState: [status] of the network connection.
     */
    fun setNetworkInfo(context: Context, status: Int, networkState: NetworkInfo.State) {
      Shadows.shadowOf(
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
      )
        .setActiveNetworkInfo(
          ShadowNetworkInfo.newInstance(
            /* detailedState = */ null,
            /* type = */ status,
            /* subType = */ 0,
            /* isAvailable = */ true,
            /* state = */ networkState
          )
        )
    }
  }
}
