package org.oppia.util.crashlytics

import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class CrashlyticsModule {
  @Singleton
  @Provides
  fun provideFirebaseCrashlytics(): FirebaseCrashlytics {
    return FirebaseCrashlytics.getInstance()
  }
}
