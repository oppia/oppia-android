package org.oppia.android.domain.platformparameter

import dagger.Binds
import dagger.Module

@Module
interface PlatformParameterControllerProdModule {
  @Binds
  fun bindPlatformParameterController(
    impl: PlatformParameterControllerProdImpl
  ): PlatformParameterController
}