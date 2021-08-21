package org.oppia.android.domain.exploration.lightweightcheckpointing

import dagger.Module
import dagger.Provides

/** Provider to return any constants required during the storage of exploration checkpoints. */
@Module
class ExplorationStorageModule {

  /**
   * Provides the size allocated to exploration checkpoint database.
   *
   * The current [ExplorationStorageDatabaseSize] is set to 2097152 Bytes that is equal to 2MB
   * per profile.
   *
   * Taking 20 KB per checkpoint, it is expected to store about 100 checkpoints for every profile
   * before the database exceeds the allocated limit.
   */
  @Provides
  @ExplorationStorageDatabaseSize
  fun provideExplorationStorageDatabaseSize(): Int = 2097152
}
