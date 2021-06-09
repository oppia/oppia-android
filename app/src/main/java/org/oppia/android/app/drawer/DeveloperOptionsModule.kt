package org.oppia.android.app.drawer

import dagger.BindsOptionalOf
import dagger.Module

/** Provides dependencies corresponding to the dev mode. */
@Module
interface DeveloperOptionsModule {
  @BindsOptionalOf fun provideDeveloperOptionsStarter(): DeveloperOptionsStarter
}
