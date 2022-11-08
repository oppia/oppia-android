package org.oppia.android.domain.exploration.testing

import dagger.Module
import dagger.Provides
import org.oppia.android.domain.exploration.ExplorationRetriever
import org.oppia.android.domain.exploration.lightweightcheckpointing.ExplorationStorageDatabaseSize

/** Module to provide test-only dependencies corresponding to exploration storage utilities. */
@Module
class ExplorationStorageTestModule {
  @Provides
  @ExplorationStorageDatabaseSize
  fun provideExplorationStorageDatabaseSize(): Int = 150 // Use a smaller size for testing ease.

  @Provides
  fun provideFakeExplorationRetriever(fake: FakeExplorationRetriever): ExplorationRetriever = fake
}
