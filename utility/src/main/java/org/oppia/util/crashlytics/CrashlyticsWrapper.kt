package org.oppia.util.crashlytics

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.inject.Inject
import javax.inject.Singleton

/** Controller for providing custom crash reporting to Firebase Crashlytics */
@Singleton
open class CrashlyticsWrapper @Inject constructor(
  private var firebaseCrashlytics : FirebaseCrashlytics
)
  : CrashlyticWrapperInterface {

  /** Logs a custom non-fatal exception to Firebase Crashlytics */
  override fun logException(exception: java.lang.Exception) {
      firebaseCrashlytics.recordException(exception)
      Log.i("TAAGGG", exception.message)
  }

  /** Logs a custom log message which can be put alongside a crash report to Firebase Crashlytics */
  override fun logMessage(message: String){
    firebaseCrashlytics.log(message)
    Log.i("TAG", message)
  }

  /** Sets up a user identifier which is attached in every crash report to Firebase Crashlytics */
  override fun setUserIdentifier(identifier: String){
    firebaseCrashlytics.setUserId(identifier)
  }
}