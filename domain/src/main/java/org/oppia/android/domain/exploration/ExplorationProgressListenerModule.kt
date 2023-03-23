package org.oppia.android.domain.exploration

import dagger.Module
import dagger.multibindings.Multibinds

/** Binds multiple dependencies that implement [ExplorationProgressListener] into a set. */
@Module
interface ExplorationProgressListenerModule {
  @Multibinds
  fun bindExplorationProgressListenerSet(): Set<ExplorationProgressListener>
}