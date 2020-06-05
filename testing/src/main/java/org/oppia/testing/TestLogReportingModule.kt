package org.oppia.testing

import dagger.Module
import dagger.Provides
import java.lang.Exception
import javax.inject.Singleton
import org.oppia.util.firebase.CrashLogger

/**
 * Provides fake crash logging dependencies.
 */
@Module
class TestLogReportingModule {
  companion object {
    var exceptionList = ArrayList<Exception>()
  }

  @Provides
  @Singleton
  fun provideCrashlyticsLogger(): CrashLogger {
    return FakeCrashLogger()
  }
}
