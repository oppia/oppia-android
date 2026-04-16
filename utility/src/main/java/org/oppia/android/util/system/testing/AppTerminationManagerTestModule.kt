package org.oppia.android.util.system.testing

import dagger.Binds
import dagger.Module
import org.oppia.android.util.system.AppTerminationManager

/** Test Dagger module for binding [FakeAppTerminationManager] as [AppTerminationManager]. */
@Module
interface AppTerminationManagerTestModule {
  @Binds
  fun bindAppTerminationManager(impl: FakeAppTerminationManager): AppTerminationManager
}
