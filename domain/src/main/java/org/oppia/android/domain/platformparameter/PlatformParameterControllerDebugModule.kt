package org.oppia.android.domain.platformparameter

import dagger.Binds
import dagger.Module

/** Provides debug implementation of [PlatformParameterController]. */
@Module
interface PlatformParameterControllerDebugModule {

  /** Binds [PlatformParameterControllerDebugImpl] to [PlatformParameterController]. */
  @Binds
  fun bindsPlatformParameterController(
    impl: PlatformParameterControllerDebugImpl
  ): PlatformParameterController

  /** Binds [PlatformParameterDebugController] to [PlatformParameterControllerDebugImpl]. */
  @Binds
  fun bindsPlatformParameterDebugController(
    impl: PlatformParameterControllerDebugImpl
  ): PlatformParameterDebugController
}