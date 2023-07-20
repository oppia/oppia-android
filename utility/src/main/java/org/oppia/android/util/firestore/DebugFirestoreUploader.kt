package org.oppia.android.util.firestore

import org.oppia.android.app.model.EventLog
import org.oppia.android.util.logging.AnalyticsEventLogger
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A debug implementation of [AnalyticsEventLogger] used in developer-only builds of the event.
 *
 * It forwards events to a production [AnalyticsEventLogger] for real logging, but it also records logged
 * events for later retrieval (e.g. via [getEventList]).
 */
@Singleton
class DebugFirestoreUploader @Inject constructor(
  factory: SurveyFirestoreDataUploader.Factory
) : DataUploader {
  private val realEventLogger by lazy { factory.createFirestoreDataLogger() }
  private val eventList = CopyOnWriteArrayList<EventLog>()

  override fun uploadData(eventLog: EventLog) {
    eventList.add(eventLog)
    realEventLogger.uploadData(eventLog)
  }

  /** Returns the list of all [EventLog]s logged since the app opened. */
  fun getEventList(): List<EventLog> = eventList
}
