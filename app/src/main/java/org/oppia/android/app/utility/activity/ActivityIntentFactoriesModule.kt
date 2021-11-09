package org.oppia.android.app.utility.activity

import dagger.Binds
import dagger.Module
import org.oppia.android.app.home.recentlyplayed.RecentlyPlayedActivity
import org.oppia.android.app.topic.TopicActivity

// TODO(#59): Split this to be per-activity.

/** Module for providing [ActivityIntentFactories] factories. */
@Module
interface ActivityIntentFactoriesModule {
  @Binds
  fun provideTopicActivityIntentFactory(
    impl: TopicActivity.TopicActivityIntentFactoryImpl
  ): ActivityIntentFactories.TopicActivityIntentFactory

  @Binds
  fun provideRecentlyPlayedActivityIntentFactory(
    impl: RecentlyPlayedActivity.RecentlyPlayedActivityIntentFactoryImpl
  ): ActivityIntentFactories.RecentlyPlayedActivityIntentFactory
}
