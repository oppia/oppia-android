package org.oppia.util.logging.firebase

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import org.oppia.util.logging.EventLogger
import javax.inject.Singleton

/** Logger for event logging to Firebase Analytics. */
@Singleton
class FirebaseEventLogger : EventLogger {

  /** Logs an event to Firebase Analytics. */
  override fun logEvent(context: Context, bundle: Bundle, title: String) {
    FirebaseAnalytics.getInstance(context).logEvent(title, bundle)
  }
}
