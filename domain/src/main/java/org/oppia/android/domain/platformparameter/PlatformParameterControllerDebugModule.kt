package org.oppia.android.domain.platformparameter

import dagger.Binds
import dagger.Module

@Module
interface PlatformParameterControllerDebugModule {
  @Binds
  fun bindsPlatformParameterController(
    impl: PlatformParameterControllerDebugImpl
  ): PlatformParameterController

  @Binds
  fun bindsPlatformParameterDebugController(
    impl: PlatformParameterControllerDebugImpl
  ): PlatformParameterDebugController
}
