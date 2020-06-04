package org.oppia.testing

import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.Module
import dagger.Provides
import java.lang.Exception
import javax.inject.Singleton
import org.mockito.Mockito
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

  @Provides
  @Singleton
  fun provideCrashlytics(): FirebaseCrashlytics {
    return Mockito.mock(FirebaseCrashlytics::class.java)
  }
}
