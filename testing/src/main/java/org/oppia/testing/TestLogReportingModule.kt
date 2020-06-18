package org.oppia.testing

import dagger.Binds
import dagger.Module
import org.oppia.util.logging.EventLogger
import org.oppia.util.logging.ExceptionLogger

/** Provides fake log reporting dependencies. */
@Module
interface TestLogReportingModule {

  @Binds
  fun bindFakeExceptionLogger(fakeExceptionLogger: FakeExceptionLogger): ExceptionLogger

  @Binds
  fun bindFakeEventLogger(fakeEventLogger: FakeEventLogger): EventLogger

}
