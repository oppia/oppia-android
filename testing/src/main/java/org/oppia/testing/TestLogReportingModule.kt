package org.oppia.testing

import dagger.Binds
import dagger.Module
import org.oppia.util.logging.EventLogger
import org.oppia.util.logging.ExceptionLogger

/** Provides fake crash and event logging dependencies. */
@Module
abstract class TestLogReportingModule {

  @Binds
  abstract fun bindFakeCrashLogger(fakeExceptionLogger: FakeExceptionLogger): ExceptionLogger

  @Binds
  abstract fun bindFakeEventLogger(fakeEventLogger: FakeEventLogger): EventLogger

}
