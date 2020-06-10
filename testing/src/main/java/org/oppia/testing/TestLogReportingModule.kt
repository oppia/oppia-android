package org.oppia.testing

import dagger.Binds
import dagger.Module
import org.oppia.util.logging.ExceptionLogger

/**
 * Provides fake crash logging dependencies.
 */
@Module
abstract class TestLogReportingModule {

  @Binds
  abstract fun bindFakeCrashLogger(fakeExceptionLogger: FakeExceptionLogger): ExceptionLogger
}
