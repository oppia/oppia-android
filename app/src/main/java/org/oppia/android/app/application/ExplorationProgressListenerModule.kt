package org.oppia.android.app.application

import dagger.Module
import dagger.multibindings.Multibinds
import org.oppia.android.domain.exploration.ExplorationProgressListener

/** Binds multiple dependencies that implement [ExplorationProgressListener] into a set. */
@Module
interface ExplorationProgressListenerModule {
  @Multibinds
  fun bindExplorationProgressListenerSet(): Set<ExplorationProgressListener>
}
