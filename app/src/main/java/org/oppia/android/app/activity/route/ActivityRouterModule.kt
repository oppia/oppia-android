package org.oppia.android.app.activity.route

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey
import org.oppia.android.app.model.DestinationScreen

@Module
abstract class ActivityRouterModule {
  @Binds
  @IntoMap
  @StringKey("RecentlyPlayedActivity")
  @RecentlyPlayedActivity
  abstract fun bindIntent(destinationScreen: DestinationScreen)
    : DestinationScreen.DestinationScreenCase
}
