package org.oppia.android.util.firestore

import org.oppia.android.app.model.EventLog
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility for creating [Documents]s specific to the NPS survey's optional response event.
 *
 * This class is only expected to be used by internal logging mechanisms and should not be called
 * directly.
 */
@Singleton
class SurveyFirestoreDocumentCreator @Inject constructor() : FirestoreDocumentCreator {
  /** Fills a HashMap with the required key value pairs needed to create a complete document. */
  override fun createDocument(eventLog: EventLog): HashMap<String, Any?> {
    val eventContext = eventLog.context.optionalResponse
    return hashMapOf(
      "survey_id" to eventContext.surveyDetails.surveyId,
      "open_feedback_answer" to eventContext.feedbackAnswer,
      "time_submitted" to eventLog.timestamp
    )
  }
}
