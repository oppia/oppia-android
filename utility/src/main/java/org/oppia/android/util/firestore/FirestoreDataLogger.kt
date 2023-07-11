package org.oppia.android.util.firestore

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.oppia.android.app.model.EventLog
import org.oppia.android.util.data.AsyncResult
import javax.inject.Inject

/** Logger for uploading to Firestore. */
class FirestoreDataLogger private constructor(
  private val firebaseFirestore: FirebaseFirestore,
  private val documentCreator: SurveyFirestoreDocumentCreator,
) : DataLogger {
  /** Converts and saves an event to a document for Firebase Firestore. */
  override fun saveData(eventLog: EventLog) {
    documentCreator.createDocument(eventLog).let { document ->
      firebaseFirestore.collection("nps_survey_open_feedback")
        .document()
        .set(document)
        .addOnSuccessListener { AsyncResult.Success(null) }
        .addOnFailureListener {
          AsyncResult.Failure<String>(it)
        }
    }
  }

  /** Application-scoped injectable factory for creating a new [FirestoreDataLogger]. */
  class Factory @Inject constructor(
    private val documentCreator: SurveyFirestoreDocumentCreator
  ) {
    private val firestoreDatabase = Firebase.firestore

    /**
     * Returns a new [FirestoreDataLogger] for the current application context.
     */
    fun createFirestoreDataLogger(): FirestoreDataLogger =
      FirestoreDataLogger(firestoreDatabase, documentCreator)
  }
}
