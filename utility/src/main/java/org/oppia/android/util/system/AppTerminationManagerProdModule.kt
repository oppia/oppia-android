package org.oppia.android.util.system

import dagger.Binds
import dagger.Module

/** Production Dagger module for providing [AppTerminationManagerImpl] as [AppTerminationManager]. */
@Module
interface AppTerminationManagerProdModule {
  @Binds
  fun bindAppTerminationManager(impl: AppTerminationManagerImpl): AppTerminationManager
}
