package org.oppia.android.app.activity.route

import android.content.Context
import android.content.Intent
import com.google.protobuf.MessageLite
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import org.oppia.android.app.home.recentlyplayed.RecentlyPlayedActivity
import org.oppia.android.app.model.DestinationScreen
import org.oppia.android.app.model.RecentlyPlayedActivityParams

/** Module to bind [DestinationScreen]. */
@Module
class ActivityRouterModule {
  @Provides
  @IntoMap
  @RouteKey(DestinationScreen.DestinationScreenCase.RECENTLY_PLAYED_ACTIVITY_PARAMS)
  fun provideRecentlyPlayedActivityRoute(): Route {
    return object: Route {
      override fun <T: MessageLite> createIntent(context: Context, params: T): Intent {
        return RecentlyPlayedActivity.createRecentlyPlayedActivityIntent(context, params as RecentlyPlayedActivityParams)
      }
    }
  }
}
