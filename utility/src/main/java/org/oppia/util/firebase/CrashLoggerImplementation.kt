package org.oppia.util.firebase

import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.inject.Singleton

/** Logger for providing custom crash reporting to Firebase Crashlytics. */
@Singleton
open class CrashLoggerImplementation(
  private var firebaseCrashlytics: FirebaseCrashlytics
) : CrashLogger {

  /** Logs a custom non-fatal exception to Firebase Crashlytics. */
  override fun logException(exception: Exception) {
    firebaseCrashlytics.recordException(exception)
  }
}
