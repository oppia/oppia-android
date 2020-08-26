package org.oppia.domain.oppialogger.exceptions

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import org.oppia.domain.oppialogger.ApplicationStartupListener

/** Returns an [ApplicationStartupListener] after binding [UncaughtExceptionLoggerStartupListener] into it. */
@Module
interface UncaughtExceptionLoggerModule {

  @Binds
  @IntoSet
  fun bindExceptionHandler(
    uncaughtExceptionLoggerStartupListener: UncaughtExceptionLoggerStartupListener
  ): ApplicationStartupListener
}
