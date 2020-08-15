package org.oppia.domain.oppialogger.exceptions

import dagger.Module
import dagger.Provides

@Module
class OppiaExceptionHandlerModule {
  @Provides
  fun provideHandler(
    oppiaUncaughtExceptionHandler: OppiaUncaughtExceptionHandler
  ): Thread.UncaughtExceptionHandler {
    return oppiaUncaughtExceptionHandler
  }
}
