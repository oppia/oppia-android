package org.oppia.android.app.drawer

import dagger.Binds
import dagger.Module

@Module
interface DeveloperOptionsStarterModule {
  @Binds
  fun bindsDeveloperOptionsStarter(
    impl: DeveloperOptionsStarterImpl
  ): DeveloperOptionsStarter
}
