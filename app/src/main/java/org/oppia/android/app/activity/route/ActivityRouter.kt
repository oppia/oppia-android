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
  /** Opens the activity corresponding to the specified [destinationScreen]. */
  fun routeToScreen(destinationScreen: DestinationScreen) {
    val routeIntent = destinationRoutes[destinationScreen.destinationScreenCase]
      ?.createIntent(
        activity,
        destinationScreen
      )
    if (routeIntent != null) {
      activity.startActivity(routeIntent)
    } else {
      consoleLogger.w("ActivityRouter", "Destination screen case is not identified.")
    }
  }
}
