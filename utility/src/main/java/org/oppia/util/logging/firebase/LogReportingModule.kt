package org.oppia.util.logging.firebase

import android.app.Application
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.Module
import dagger.Provides
import org.oppia.util.logging.EnableDataCollection
import org.oppia.util.logging.EventBundleCreator
import org.oppia.util.logging.EventLogger
import org.oppia.util.logging.ExceptionLogger
import javax.inject.Singleton

/** Provides Firebase-specific logging implementations. */
@Module
class LogReportingModule {

  @Provides
  @EnableDataCollection
  @Singleton
  fun provideDataCollection(): Boolean{
    return false
  }

  @Singleton
  @Provides
  fun provideCrashLogger(): ExceptionLogger {
    return if(provideDataCollection()){
      FirebaseExceptionLogger(FirebaseCrashlytics.getInstance())
    } else{
      TempEventLogger()
    }
  }

  @Singleton
  @Provides
  fun provideEventLogger(): EventLogger {
    return if(provideDataCollection()){
      FirebaseEventLogger(
        FirebaseAnalytics.getInstance(Application()),
        EventBundleCreator()
      )
    } else {
      TempEventLogger()
    }
  }
}
