package org.oppia.util.firebase

import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.inject.Inject
import javax.inject.Singleton

/** Logger for providing custom crash reporting to Firebase Crashlytics. */
@Singleton
open class CrashLogger @Inject constructor(
  private var firebaseCrashlytics: FirebaseCrashlytics
): CrashLoggerInterface {

  /** Logs a custom non-fatal exception to Firebase Crashlytics. */
  override fun logException(exception: Exception) {
      firebaseCrashlytics.recordException(exception)
  }

  /** Logs a custom log message which can be put alongside a crash report to Firebase Crashlytics. */
  override fun logMessage(message: String) {
    firebaseCrashlytics.log(message)
  }

  /** Sets up a user identifier which is attached in every crash report to Firebase Crashlytics. */
  override fun setUserIdentifier(identifier: String) {
    firebaseCrashlytics.setUserId(identifier)
  }
}
