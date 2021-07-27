package org.oppia.android.util.utility

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowNetworkInfo
import javax.inject.Inject
import javax.inject.Singleton

/** Test utility to modify [ShadowNetworkInfo] during the test. */
@Singleton
class NetworkConnectionTestUtil @Inject constructor() {

  /**
   * Sets the [ShadowNetworkInfo] during the test.
   *
   * @param context: the context for which the dummy network connection status needs to be set.
   * @param status: the network type which needs to be set.
   * @param networkState: refers to whether the [status] should be connected or disconnected.
   */
  fun setNetworkInfo(context: Context, status: Int, networkState: NetworkInfo.State) {
    Shadows.shadowOf(context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
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
