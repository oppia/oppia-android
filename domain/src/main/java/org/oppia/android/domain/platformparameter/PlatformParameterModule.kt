package org.oppia.android.domain.platformparameter

import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/** Dagger module that provides bindings for platform parameters. */
@Module(
  includes = [
    FeatureFlagsMapBindingModule::class,
    FeatureFlagBindingModule::class,
    PlatformParameterBindingModule::class,
    PlatformParameterModule.PlatformParameterProcessStateModule::class,
    PlatformParameterModule.PlatformParameterControllerProdImplModule::class
  ]
)
interface PlatformParameterModule {
  @Binds
  fun bindPlatformParameterController(
    impl: PlatformParameterControllerProdImpl
  ): PlatformParameterController

  @Binds
  fun bindPlatformParameterConfigRetriever(
    impl: PlatformParameterConfigRetrieverProdImpl
  ): PlatformParameterConfigRetriever

  @Module
  class PlatformParameterProcessStateModule {
    @Provides
    @Singleton
    fun providePlatformParameterProcessState() = PlatformParameterProcessState()
  }

  @Module
  class PlatformParameterControllerProdImplModule {
    @Provides
    @Singleton
    fun providePlatformParameterControllerProdImpl(
      platformParameterProcessState: PlatformParameterProcessState,
      factory: PlatformParameterControllerProdImpl.Factory
    ) = factory.create(platformParameterProcessState)
  }
}
