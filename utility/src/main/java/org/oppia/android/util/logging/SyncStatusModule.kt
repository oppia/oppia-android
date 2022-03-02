package org.oppia.android.util.logging

import dagger.Module
import dagger.Provides
import org.oppia.android.util.data.AsyncDataSubscriptionManager
import org.oppia.android.util.data.DataProviders
import javax.inject.Singleton

/** Provides production-specific sync status mechanism related dependencies. */
@Module
class SyncStatusModule {
  @Singleton
  @Provides
  fun provideSyncStatusManager(
    dataProviders: DataProviders,
    asyncDataSubscriptionManager: AsyncDataSubscriptionManager
  ): SyncStatusManager = SyncStatusManagerImpl(dataProviders, asyncDataSubscriptionManager)
}
