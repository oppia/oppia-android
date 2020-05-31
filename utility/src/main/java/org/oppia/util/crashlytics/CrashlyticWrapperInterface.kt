package org.oppia.util.crashlytics

import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlin.Exception

interface CrashlyticWrapperInterface {

  fun logException(exception: Exception)

  fun logMessage(message: String)

  fun setUserIdentifier(identifier: String)

}