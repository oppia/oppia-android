package org.oppia.util.logging.firebase

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import org.oppia.app.model.EventLog
import org.oppia.util.logging.EventBundleCreator
import org.oppia.util.logging.EventLogger
import javax.inject.Singleton

/** Logger for event logging to Firebase Analytics. */
@Singleton
class FirebaseEventLogger(
  private val firebaseAnalytics: FirebaseAnalytics,
  private val eventBundleCreator: EventBundleCreator
) : EventLogger {
  private var bundle = Bundle()

  /** Logs an event to Firebase Analytics. */
  override fun logEvent(eventLog: EventLog) {
    bundle = eventBundleCreator.createEventBundle(eventLog)
    firebaseAnalytics.logEvent(eventLog.actionName.toString(), bundle)
  }
}
