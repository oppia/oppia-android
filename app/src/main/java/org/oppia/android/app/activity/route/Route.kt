package org.oppia.android.app.activity.route

import android.content.Context
import android.content.Intent
import org.oppia.android.app.model.DestinationScreen

/** Represents a possible navigation route to a specific activity in the app. */
interface Route {
  /**
   * Creates an [Intent] to route to the activity corresponding to this route.
   *
   * @param context the context to be used for creating the [Intent]
   * @param params the parameters to pass to the activity via its extras bundle
   * @return the intent that can be used to navigate to this route's activity
   */
  fun createIntent(
    context: Context,
    destinationScreen: DestinationScreen
  ): Intent
}
