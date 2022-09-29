package org.oppia.android.app.activity.route

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.app.model.DestinationScreen
import org.oppia.android.app.model.DestinationScreen.DestinationScreenCase
import org.oppia.android.util.logging.ConsoleLogger
import javax.inject.Inject

/**
 * A central router that can navigate the user to a specific activity based on a provided
 * [DestinationScreen].
 */
class ActivityRouter @Inject constructor(
  private val activity: AppCompatActivity,
  private val destinationRoutes: Map<DestinationScreenCase, @JvmSuppressWildcards Route>,
  private val consoleLogger: ConsoleLogger
) {
  /**  Opens the activity corresponding to the specified [destinationScreen]. */
  fun routeToScreen(destinationScreen: DestinationScreen) {
    // TODO: Need to handle error cases properly here.
    destinationRoutes[destinationScreen.destinationScreenCase].let { route ->
      when (destinationScreen.destinationScreenCase) {
        DestinationScreenCase.RECENTLY_PLAYED_ACTIVITY_PARAMS -> {
          activity.startActivity(
            route?.createIntent(
              activity,
              destinationScreen.recentlyPlayedActivityParams
            )
          )
        }
        DestinationScreenCase.DESTINATIONSCREEN_NOT_SET, null -> {
          consoleLogger.w("ActivityRouter", "Destination screen case is not identified.")
        }
      }
    }
  }
}
