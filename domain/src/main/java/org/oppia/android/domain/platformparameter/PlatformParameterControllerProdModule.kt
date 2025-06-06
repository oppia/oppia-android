package org.oppia.android.domain.platformparameter

import dagger.Binds
import dagger.Module

/** Provides production implementation of [PlatformParameterController]. */
@Module
interface PlatformParameterControllerProdModule {

  /** Binds [PlatformParameterControllerDebugImpl] to [PlatformParameterController]. */
  @Binds
  fun bindPlatformParameterController(
    impl: PlatformParameterControllerProdImpl
  ): PlatformParameterController
}