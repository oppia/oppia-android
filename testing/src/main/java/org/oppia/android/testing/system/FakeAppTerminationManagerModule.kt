package org.oppia.android.testing.system

import dagger.Binds
import dagger.Module
import org.oppia.android.util.system.AppTerminationManager

/** Dagger module for [FakeAppTerminationManager]. */
@Module
interface FakeAppTerminationManagerModule {
  @Binds
  fun bindAppTerminationManager(impl: FakeAppTerminationManager): AppTerminationManager
}
