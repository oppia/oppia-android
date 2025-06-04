package org.oppia.android.domain.platformparameter

import dagger.Binds
import dagger.Module

@Module
interface PlatformParameterControllerDebugModule {
  @Binds
  fun bindsPlatformParameterControllerDebugImpl(
    impl: PlatformParameterControllerDebugImpl
  ): PlatformParameterController
}
