package org.oppia.app.application

import dagger.Module
import dagger.Provides
import org.oppia.domain.oppialogger.exceptions.OppiaUncaughtExceptionHandler

@Module
class OppiaExceptionHandlerModule {
  @Provides
  fun provideHandler(
    oppiaUncaughtExceptionHandler: OppiaUncaughtExceptionHandler
  ): Thread.UncaughtExceptionHandler {
    return oppiaUncaughtExceptionHandler
  }
}