package org.oppia.android.util.firestore

import org.oppia.android.app.model.EventLog
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility for creating [Documents]s from [EventLog] objects.
 *
 * This class is only expected to be used by internal logging mechanisms and should not be called
 * directly.
 */
@Singleton
class SurveyFirestoreDocumentCreator @Inject constructor() {
  /**
   * Fills the specified document with the required key value pairs needed to
   * create a complete firestore document.
   */
  fun createDocument(eventLog: EventLog): HashMap<String, Any?> {
    val eventContext = eventLog.context.optionalResponse
    return hashMapOf(
      "survey_id" to eventContext.surveyDetails.surveyId,
      "open_feedback_answer" to eventContext.feedbackAnswer,
      "time_submitted" to eventLog.timestamp
    )
  }
}
