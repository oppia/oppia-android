package org.oppia.android.domain.exploration

import dagger.Module
import dagger.Provides
import org.oppia.android.domain.exploration.lightweightcheckpointing.ExplorationStorageDatabaseSize

/** Provider to return any constants required during the storage of exploration checkpoints. */
@Module
class ExplorationStorageModule {
  @Provides
  @ExplorationStorageDatabaseSize
  fun provideExplorationStorageDatabaseSize(): Int = 2097152

  // TODO: Move this module.
  @Provides
  fun provideProductionExplorationRetriever(
    impl: ExplorationRetrieverImpl
  ): ExplorationRetriever = impl
}
