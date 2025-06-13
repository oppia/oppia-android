package org.oppia.android.domain.platformparameter

import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/** Dagger module that provides bindings for platform parameters (debug variant). */
@Module(
  includes = [
    FeatureFlagsMapBindingModule::class,
    FeatureFlagBindingModule::class,
    PlatformParameterBindingModule::class,
    PlatformParameterControllerDebugModule.PlatformParameterProcessStateModule::class,
    PlatformParameterControllerDebugModule.PlatformParameterControllerProdImplModule::class
  ]
)
interface PlatformParameterControllerDebugModule {
  @Binds
  fun bindPlatformParameterController(
    impl: PlatformParameterControllerDebugImpl
  ): PlatformParameterController

  @Binds
  fun bindPlatformParameterDebugController(
    impl: PlatformParameterControllerDebugImpl
  ): PlatformParameterDebugController

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
