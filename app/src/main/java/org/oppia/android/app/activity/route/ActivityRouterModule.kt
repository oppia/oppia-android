package org.oppia.android.app.activity.route

import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey
import org.oppia.android.app.model.DestinationScreen

/** RouterModule to bind [DestinationScreen]. */
@Module
class ActivityRouterModule {
  @Provides
  @IntoMap
  @StringKey("RecentlyPlayedActivity")
  @RouteRule
  fun bindIntent(destinationScreen: DestinationScreen): DestinationScreen.DestinationScreenCase {
    return destinationScreen.destinationScreenCase
  }
}
