package org.oppia.android.domain.platformparameter

import dagger.Binds
import dagger.Module
import org.oppia.android.util.platformparameter.PlatformParameterSingleton

/** Dagger module that provides singleton state for platform parameters. */
@Module
interface PlatformParameterSingletonModule {
  @Binds
  fun providePlatformParameterSingleton(
    platformParameterSingletonImpl: PlatformParameterSingletonImpl
  ): PlatformParameterSingleton
}
