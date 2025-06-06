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
  fun bindPlatformParameterConfigRetriever(
    impl: PlatformParameterConfigRetrieverProdImpl
  ): PlatformParameterConfigRetriever

  /**
   * Dagger module for providing the application-wide instance of [PlatformParameterProcessState].
   */
  // TODO(#5835): Remove this and make PlatformParameterProcessState injectable once the hack for
  //  initializing platform parameters in tests is no longer needed.
  @Module
  class PlatformParameterProcessStateModule {
    @Provides
    @Singleton
    fun providePlatformParameterProcessState() = PlatformParameterProcessState()
  }

  /**
   * Dagger module for providing the application-wide instance of
   * [PlatformParameterControllerProdImpl].
   */
  // TODO(#5835): Remove this and make PlatformParameterControllerProdImpl injectable once the hack
  //  for initializing platform parameters in tests is no longer needed.
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
