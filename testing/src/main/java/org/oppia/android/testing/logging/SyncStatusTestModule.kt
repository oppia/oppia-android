package org.oppia.android.testing.logging

import dagger.Binds
import dagger.Module
import org.oppia.android.util.logging.SyncStatusManager

/** Module for providing test-only sync status utilities. */
@Module
interface SyncStatusTestModule {
  @Binds
  fun bindSyncStatusManager(impl: FakeSyncStatusManager): SyncStatusManager
}
