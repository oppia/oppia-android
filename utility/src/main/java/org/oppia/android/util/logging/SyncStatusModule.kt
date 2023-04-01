package org.oppia.android.util.logging

import dagger.Binds
import dagger.Module

/** Provides production-specific sync status mechanism related dependencies. */
@Module
interface SyncStatusModule {
  @Binds
  fun provideSyncStatusManager(impl: SyncStatusManagerImpl): SyncStatusManager
}
