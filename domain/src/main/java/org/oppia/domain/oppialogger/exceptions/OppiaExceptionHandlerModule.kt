package org.oppia.domain.oppialogger.exceptions

import dagger.Module
import dagger.Provides
import org.oppia.util.logging.ConsoleLogger
import org.oppia.util.system.OppiaClock

/** Provides custom uncaught exception handler. */
@Module
class OppiaExceptionHandlerModule {

  @Provides
  fun provideExceptionHandler(
    exceptionsController: ExceptionsController,
    oppiaClock: OppiaClock,
    consoleLogger: ConsoleLogger
  ) = OppiaUncaughtExceptionHandler(exceptionsController, oppiaClock, consoleLogger)
}
