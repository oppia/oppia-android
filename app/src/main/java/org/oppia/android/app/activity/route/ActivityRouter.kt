package org.oppia.android.app.activity.route

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.app.home.recentlyplayed.RecentlyPlayedActivity
import org.oppia.android.app.model.DestinationScreen
import org.oppia.android.app.model.RecentlyPlayedActivityParams
import org.oppia.android.util.logging.ConsoleLogger
import javax.inject.Inject

/**
 * A central router that can navigate the user to a specific activity based on a provided
 * [DestinationScreen].
 */
class ActivityRouter @Inject constructor(
  private val activity: AppCompatActivity,
  private val consoleLogger: ConsoleLogger
) {

  /**  Opens the activity corresponding to the specified [destinationScreen]. */
  fun routeToScreen(destinationScreen: DestinationScreen) {
    when (destinationScreen.destinationScreenCase) {
      DestinationScreen.DestinationScreenCase.RECENTLY_PLAYED_ACTIVITY_PARAMS -> {
        openRecentlyPlayedActivity(destinationScreen.recentlyPlayedActivityParams)
      }
      DestinationScreen.DestinationScreenCase.DESTINATIONSCREEN_NOT_SET -> {
        consoleLogger.w("ActivityRouter", "Destination screen case is not identified.")
      }
    }
  }

  private fun openRecentlyPlayedActivity(
    recentlyPlayedActivityParams: RecentlyPlayedActivityParams
  ) {
    activity.startActivity(
      RecentlyPlayedActivity.createRecentlyPlayedActivityIntent(
        activity,
        recentlyPlayedActivityParams
      )
    )
  }
}
