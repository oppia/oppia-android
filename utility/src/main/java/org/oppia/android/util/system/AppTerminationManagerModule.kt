package org.oppia.android.util.system

import dagger.Binds
import dagger.Module

/** Dagger module for [AppTerminationManager]. */
@Module
interface AppTerminationManagerModule {
  @Binds
  fun bindAppTerminationManager(impl: AppTerminationManagerImpl): AppTerminationManager
}
