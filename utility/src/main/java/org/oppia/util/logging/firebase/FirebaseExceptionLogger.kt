package org.oppia.util.logging.firebase

import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.oppia.util.logging.ExceptionLogger

/** Logger for providing custom crash reporting to Firebase Crashlytics. */
class FirebaseExceptionLogger(
  private var firebaseCrashlytics: FirebaseCrashlytics
) : ExceptionLogger {

  /** Logs a custom non-fatal exception to Firebase Crashlytics. */
  override fun logException(exception: Exception) {
    firebaseCrashlytics.recordException(exception)
  }
}
