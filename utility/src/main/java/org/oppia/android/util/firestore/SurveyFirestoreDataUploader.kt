package org.oppia.android.util.firestore

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.oppia.android.app.model.EventLog
import org.oppia.android.util.logging.ConsoleLogger
import javax.inject.Inject

/** Logger for uploading to Firestore. */
class SurveyFirestoreDataUploader private constructor(
  private val firebaseFirestore: FirebaseFirestore,
  private val documentCreator: SurveyFirestoreDocumentCreator,
  private val consoleLogger: ConsoleLogger
) : DataUploader {
  /** Converts an event to a document and uploads it to Firebase Firestore. */
  override fun uploadData(eventLog: EventLog) {
    documentCreator.createDocument(eventLog).let { document ->
      firebaseFirestore.collection("nps_survey_open_feedback")
        .add(document)
        .addOnSuccessListener {
          consoleLogger.i("SurveyFirestoreDataUploader", "Upload to Firestore was successful")
        }
        .addOnFailureListener { e ->
          consoleLogger.e("SurveyFirestoreDataUploader", e.toString(), e)
        }
    }
  }

  /** Application-scoped injectable factory for creating a new [SurveyFirestoreDataUploader]. */
  class Factory @Inject constructor(
    private val documentCreator: SurveyFirestoreDocumentCreator,
    private val consoleLogger: ConsoleLogger
  ) {
    private val firestoreDatabase = Firebase.firestore

    /** Returns a new [SurveyFirestoreDataUploader] for the current application context. */
    fun createFirestoreDataLogger(): SurveyFirestoreDataUploader =
      SurveyFirestoreDataUploader(firestoreDatabase, documentCreator, consoleLogger)
  }
}
