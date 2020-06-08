package org.oppia.testing

import dagger.Module
import dagger.Provides
import java.lang.Exception
import javax.inject.Singleton
import org.oppia.util.firebase.CrashLogger
import org.oppia.util.firebase.EventLogger

/**
 * Provides fake crash logging dependencies.
 */
@Module
class TestLogReportingModule {
  companion object {
    var exceptionList = ArrayList<Exception>()
    var eventList = ArrayList<Event>()
  }

  @Provides
  @Singleton
  fun provideCrashlyticsLogger(): CrashLogger {
    return FakeCrashLogger()
  }

  @Singleton
  @Provides
  fun provideEventLogger(): EventLogger {
    return FakeEventLogger()
  }
}
