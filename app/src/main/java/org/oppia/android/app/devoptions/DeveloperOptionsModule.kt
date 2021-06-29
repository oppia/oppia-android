package org.oppia.android.app.devoptions

import dagger.BindsOptionalOf
import dagger.Module

/** Provides optional dependencies corresponding to the DeveloperOptionsStarter. */
@Module
interface DeveloperOptionsModule {
  @BindsOptionalOf fun provideDeveloperOptionsStarter(): DeveloperOptionsStarter
}
