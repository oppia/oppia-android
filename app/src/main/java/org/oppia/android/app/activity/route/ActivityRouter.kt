package org.oppia.android.app.activity.route

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.app.home.recentlyplayed.RecentlyPlayedActivity
import org.oppia.android.app.model.DestinationScreen
import org.oppia.android.app.model.RecentlyPlayedActivityParams
import org.oppia.android.util.logging.ConsoleLogger
import javax.inject.Inject

/**
 * ActivityRouter used to route to given [DestinationScreen].
 */
class ActivityRouter @Inject constructor(
  private val activity: AppCompatActivity,
  private val consoleLogger: ConsoleLogger
) {

  /** Checks the value of [DestinationScreen] and routes to different activities accordingly. */
  fun routeToScreen(destinationScreen: DestinationScreen) {
    when (destinationScreen.destinationScreenCase) {
      DestinationScreen.DestinationScreenCase.RECENTLY_PLAYED_ACTIVITY_PARAMS -> {
        openRecentlyPlayedActivity(destinationScreen.recentlyPlayedActivityParams)
      }
      else -> {
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
