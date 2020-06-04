package org.oppia.testing

import dagger.Module
import dagger.Provides
import java.lang.Exception
import javax.inject.Singleton
import org.oppia.util.firebase.CrashLogger

@Module
class TestFirebaseModule {
  companion object {
    var exceptionList = ArrayList<Exception>()
  }

  @Provides
  @Singleton
  fun provideCrashlyticsLogger(): CrashLogger {
    return FakeCrashLogger()
  }
}
