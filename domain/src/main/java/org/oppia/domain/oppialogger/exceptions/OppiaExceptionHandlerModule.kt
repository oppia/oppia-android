package org.oppia.domain.oppialogger.exceptions

import dagger.Binds
import dagger.Module

/** Provides custom uncaught exception handler. */
@Module
interface OppiaExceptionHandlerModule {

  @Binds
  fun bindExceptionHandler(
    oppiaUncaughtExceptionHandler: OppiaUncaughtExceptionHandler
  ): Thread.UncaughtExceptionHandler
}
