package org.oppia.android.util.logging

import dagger.Binds
import dagger.Module

/** Provides debug log reporting dependencies. */
@Module
interface DebugLogReportingModule {

  @Binds
  fun bindDebugExceptionLogger(debugExceptionLogger: DebugExceptionLogger): ExceptionLogger

  @Binds
  fun bindDebugEventLogger(debugEventLogger: DebugEventLogger): EventLogger
}
