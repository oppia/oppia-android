package org.oppia.util.firebase

import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class FirebaseModule {
  @Singleton
  @Provides
  fun provideFirebaseCrashlytics(): FirebaseCrashlytics {
    return FirebaseCrashlytics.getInstance()
  }

  @Singleton
  @Provides
  fun provideCrashLogger(): CrashLogger {
    return CrashLoggerImplementation(provideFirebaseCrashlytics())
  }
}
