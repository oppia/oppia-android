package org.oppia.android.util.logging.firebase

import org.oppia.android.app.model.EventLog
import org.oppia.android.util.logging.ConsoleLogger
import javax.inject.Inject

/** Logger for uploading to Firestore. */
class FirestoreEventLoggerProdImpl @Inject constructor(
  private val consoleLogger: ConsoleLogger,
  private val firestoreInstanceWrapper: FirestoreInstanceWrapper
) : FirestoreEventLogger {
  /** Converts an event to a document and uploads it to Firebase Firestore. */
  override fun uploadEvent(eventLog: EventLog) {
    val eventContext = eventLog.context.optionalResponse
    val document = hashMapOf(
      "survey_id" to eventContext.surveyDetails.surveyId,
      "open_feedback_answer" to eventContext.feedbackAnswer,
      "time_submitted" to eventLog.timestamp
    )

    firestoreInstanceWrapper.firestoreInstance?.firebaseFirestore
      ?.collection("nps_survey_open_feedback")
      ?.add(document)
      ?.addOnSuccessListener {
        consoleLogger.i("FirestoreEventLoggerProdImpl", "Upload to Firestore was successful")
      }
      ?.addOnFailureListener { e ->
        consoleLogger.e("FirestoreEventLoggerProdImpl", e.toString(), e)
      }
  }
}
