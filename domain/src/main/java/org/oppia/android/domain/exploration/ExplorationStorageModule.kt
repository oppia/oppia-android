package org.oppia.android.domain.exploration

import dagger.Module
import dagger.Provides
import org.oppia.android.domain.exploration.lightweightcheckpointing.ExplorationStorageDatabaseSize

/** Module to provide dependencies corresponding to exploration storage utilities. */
@Module
class ExplorationStorageModule {
  @Provides
  @ExplorationStorageDatabaseSize
  fun provideExplorationStorageDatabaseSize(): Int = 2097152

  @Provides
  fun provideProductionExplorationRetriever(
    impl: ExplorationRetrieverImpl
  ): ExplorationRetriever = impl
}
