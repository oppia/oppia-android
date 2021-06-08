package org.oppia.android.app.drawer

import dagger.Binds
import dagger.Module

@Module
abstract class DeveloperOptionsStarterModule {
  @Binds
  abstract fun bindsDeveloperOptionsStarter(
    impl: DeveloperOptionsStarterImpl
  ): DeveloperOptionsStarter
}
