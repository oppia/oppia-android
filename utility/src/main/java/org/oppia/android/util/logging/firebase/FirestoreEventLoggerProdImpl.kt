package org.oppia.android.util.logging.firebase

import com.google.firebase.firestore.FirebaseFirestore
import org.oppia.android.app.model.EventLog
import org.oppia.android.util.logging.ConsoleLogger
import javax.inject.Inject

/** Logger for uploading to Firestore. */
class FirestoreEventLoggerProdImpl private constructor(
  private val firebaseFirestore: FirebaseFirestore,
  private val consoleLogger: ConsoleLogger
) : FirestoreEventLogger {
  /** Converts an event to a document and uploads it to Firebase Firestore. */
  override fun uploadEvent(eventLog: EventLog) {
    uploadOptionalResponseDocument(eventLog)
  }

  override fun getEventList(): List<EventLog> = listOf()

  private fun uploadOptionalResponseDocument(eventLog: EventLog) {
    val eventContext = eventLog.context.optionalResponse
    val document = hashMapOf(
      "survey_id" to eventContext.surveyDetails.surveyId,
      "open_feedback_answer" to eventContext.feedbackAnswer,
      "time_submitted" to eventLog.timestamp
    )

    firebaseFirestore.collection("nps_survey_open_feedback")
      .add(document)
      .addOnSuccessListener {
        consoleLogger.i("FirestoreEventLoggerProdImpl", "Upload to Firestore was successful")
      }
      .addOnFailureListener { e ->
        consoleLogger.e("FirestoreEventLoggerProdImpl", e.toString(), e)
      }
  }

  /** Application-scoped injectable factory for creating a new [FirestoreEventLoggerProdImpl]. */
  class Factory @Inject constructor(
    private val consoleLogger: ConsoleLogger
  ) {
    private val firestoreDatabase by lazy { FirebaseFirestore.getInstance() }

    /** Returns a new [FirestoreEventLoggerProdImpl] for the current application context. */
    fun createFirestoreEventLogger(): FirestoreEventLoggerProdImpl =
      FirestoreEventLoggerProdImpl(firestoreDatabase, consoleLogger)
  }
}
