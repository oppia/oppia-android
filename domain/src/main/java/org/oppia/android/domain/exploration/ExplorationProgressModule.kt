package org.oppia.android.domain.exploration

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet

/** Module to provide dependencies corresponding to exploration progress. */
@Module
interface ExplorationProgressModule {
  @Binds
  @IntoSet
  fun provideExplorationProgressListener(
    explorationSessionTimerController: ExplorationSessionTimerController
  ): ExplorationProgressListener
}
