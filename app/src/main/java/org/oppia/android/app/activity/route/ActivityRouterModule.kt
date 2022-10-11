package org.oppia.android.app.activity.route

import android.content.Context
import android.content.Intent
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import org.oppia.android.app.home.recentlyplayed.RecentlyPlayedActivity
import org.oppia.android.app.model.DestinationScreen

/** Module to bind destination screens to navigable activity routes. */
@Module
class ActivityRouterModule {
  @Provides
  @IntoMap
  @RouteKey(DestinationScreen.DestinationScreenCase.RECENTLY_PLAYED_ACTIVITY_PARAMS)
  fun provideRecentlyPlayedActivityRoute(): Route {
    return object : Route {
      override fun createIntent(
        context: Context,
        destinationScreen: DestinationScreen
      ): Intent {
        return RecentlyPlayedActivity.createRecentlyPlayedActivityIntent(
          context,
          destinationScreen.recentlyPlayedActivityParams
        )
      }
    }
  }
}
