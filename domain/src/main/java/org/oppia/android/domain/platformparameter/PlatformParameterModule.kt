package org.oppia.android.domain.platformparameter

import dagger.Binds
import dagger.Module

@Module(includes = [PlatformParameterGeneratedModule::class])
interface PlatformParameterModule {
  @Binds
  fun providePlatformParameterController(
    impl: PlatformParameterControllerProdImpl
  ): PlatformParameterController

  @Binds
  fun providePlatformParameterConfigRetriever(
    impl: PlatformParameterConfigRetrieverProdImpl
  ): PlatformParameterConfigRetriever
}
