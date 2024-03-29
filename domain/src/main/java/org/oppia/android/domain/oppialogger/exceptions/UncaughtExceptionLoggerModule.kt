package org.oppia.android.domain.oppialogger.exceptions

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import org.oppia.android.domain.oppialogger.ApplicationStartupListener

/** Binds [UncaughtExceptionLoggerStartupListener] as an [ApplicationStartupListener]. */
@Module
interface UncaughtExceptionLoggerModule {

  @Binds
  @IntoSet
  fun bindExceptionHandler(
    uncaughtExceptionLoggerStartupListener: UncaughtExceptionLoggerStartupListener
  ): ApplicationStartupListener
}
