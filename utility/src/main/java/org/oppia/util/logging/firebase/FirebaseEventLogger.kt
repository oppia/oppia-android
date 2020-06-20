package org.oppia.util.logging.firebase

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import org.oppia.app.model.EventLog
import org.oppia.util.logging.EventBundleCreator
import org.oppia.util.logging.EventLogger
import org.oppia.util.logging.ExceptionLogger
import java.lang.Exception
import javax.inject.Singleton

/** Logger for event logging to Firebase Analytics. */
@Singleton
class FirebaseEventLogger(
  private val firebaseAnalytics: FirebaseAnalytics,
  private val eventBundleCreator: EventBundleCreator
) : EventLogger {
  private var bundle = Bundle()

  /** Logs an event to Firebase Analytics. */
  override fun logEvent(context: Context, eventLog: EventLog) {
    bundle = eventBundleCreator.createEventBundle(eventLog)
    firebaseAnalytics.logEvent(eventLog.actionName.toString(), bundle)
  }
}

/** Temporary logger class that performs no action when called upon.*/
class TempEventLogger : EventLogger, ExceptionLogger {
  override fun logEvent(context: Context, eventLog: EventLog) {
  }

  override fun logException(exception: Exception) {
  }
}
