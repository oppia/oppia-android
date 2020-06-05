package org.oppia.util.firebase

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Singleton

/** Logger for event logging to Firebase Analytics. */
@Singleton
class EventLoggerImplementation : EventLogger {

  /** Logs a event to Firebase Analytics. */
  override fun logEvent(context: Context, bundle: Bundle, title: String) {
    FirebaseAnalytics.getInstance(context).logEvent(title, bundle)
  }
}
