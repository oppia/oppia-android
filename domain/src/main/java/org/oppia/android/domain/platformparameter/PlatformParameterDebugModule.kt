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
    PlatformParameterDebugModule.PlatformParameterProcessStateModule::class,
    PlatformParameterDebugModule.PlatformParameterControllerProdImplModule::class
  ]
)
interface PlatformParameterDebugModule {
  @Binds
  @Singleton
  fun bindPlatformParameterController(
    impl: PlatformParameterControllerDebugImpl
  ): PlatformParameterController

  @Binds
  @Singleton
  fun bindPlatformParameterConfigRetriever(
    impl: PlatformParameterConfigRetrieverProdImpl
  ): PlatformParameterConfigRetriever

  // TODO(#5835): Remove this and make PlatformParameterProcessState injectable once the hack for
  //  initializing platform parameters in tests is no longer needed.
  /**
   * Dagger module for providing the application-wide instance of [PlatformParameterProcessState].
   */
  @Module
  class PlatformParameterProcessStateModule {
    @Provides
    @Singleton
    fun providePlatformParameterProcessState() = PlatformParameterProcessState()
  }

  // TODO(#5835): Remove this and make PlatformParameterControllerProdImpl injectable once the hack
  //  for initializing platform parameters in tests is no longer needed.
  /**
   * Dagger module for providing the application-wide instance of
   * [PlatformParameterControllerProdImpl].
   */
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
