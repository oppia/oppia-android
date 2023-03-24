package org.oppia.android.domain.exploration

import dagger.Module
import dagger.Provides

/** Module to provide dependencies corresponding to exploration progress. */
@Module
class ExplorationProgressModule {
  @Provides
  fun provideExplorationProgressListener(
    explorationProgressListenerImpl: ExplorationProgressListenerImpl
  ): ExplorationProgressListener = explorationProgressListenerImpl
}
