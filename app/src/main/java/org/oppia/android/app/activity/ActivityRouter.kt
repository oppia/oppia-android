package org.oppia.android.app.activity

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.app.home.recentlyplayed.RecentlyPlayedActivity
import org.oppia.android.app.model.DestinationScreen
import org.oppia.android.app.model.DestinationScreen.DestinationScreenCase
import org.oppia.android.app.model.RecentlyPlayedActivityIntentExtras
import org.oppia.android.util.logging.ConsoleLogger
import javax.inject.Inject

/** Central utility to manage routing to different activities. */
class ActivityRouter @Inject constructor(
  private val activity: AppCompatActivity,
  private val consoleLogger: ConsoleLogger
) {

  /** Checks the value of [DestinationScreen] and routes to different activities accordingly. */
  fun routeToScreen(destinationScreen: DestinationScreen) {
    when (destinationScreen.destinationScreenCase) {
      DestinationScreenCase.RECENTLY_PLAYED_ACTIVITY_INTENT_EXTRAS -> {
        openRecentlyPlayedActivity(destinationScreen.recentlyPlayedActivityIntentExtras)
      }
      else -> {
        consoleLogger.w("ActivityRouter", "Destination screen case is not identified.")
      }
    }
  }

  private fun openRecentlyPlayedActivity(
    recentlyPlayedActivityIntentExtras: RecentlyPlayedActivityIntentExtras
  ) {
    activity.startActivity(
      RecentlyPlayedActivity.createRecentlyPlayedActivityIntent(
        activity,
        recentlyPlayedActivityIntentExtras
      )
    )
  }
}
