package org.oppia.android.app.drawer

import dagger.BindsOptionalOf
import dagger.Module

/** Provides dependencies corresponding to the dev mode. */
@Module
abstract class DeveloperOptionsModule {
  @BindsOptionalOf abstract fun provideDeveloperOptionsStarter(): DeveloperOptionsStarter
}
