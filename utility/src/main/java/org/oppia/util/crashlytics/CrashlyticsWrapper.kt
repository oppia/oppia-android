package org.oppia.util.crashlytics

import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.inject.Inject
import javax.inject.Singleton

/** Controller for providing custom crash reporting to Firebase Crashlytics */
@Singleton
open class CrashlyticsWrapper @Inject constructor(
  private var firebaseCrashlytics: FirebaseCrashlytics
) {

  /** Logs a custom non-fatal exception to Firebase Crashlytics */
  open fun logException(exception: Exception) {
      firebaseCrashlytics.recordException(exception)
  }

  /** Logs a custom log message which can be put alongside a crash report to Firebase Crashlytics */
  open fun logMessage(message: String) {
    firebaseCrashlytics.log(message)
  }

  /** Sets up a user identifier which is attached in every crash report to Firebase Crashlytics */
  open fun setUserIdentifier(identifier: String) {
    firebaseCrashlytics.setUserId(identifier)
  }
}
