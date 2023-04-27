package org.oppia.android.app.activity

import dagger.Binds
import dagger.Module
import org.oppia.android.app.home.recentlyplayed.RecentlyPlayedActivity
import org.oppia.android.app.topic.TopicActivity

// TODO(#59): Move uses to ActivityRouter and remove this intent factory pattern.

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
