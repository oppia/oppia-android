package org.oppia.android.util.firestore

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import javax.inject.Inject
import org.oppia.android.app.model.EventLog

/** Logger for uploading to Firestore. */
class FirestoreDataLogger private constructor(
  private val firebaseFirestore: FirebaseFirestore,
  private val firestoreDocumentCreator: FirestoreDocumentCreator,
) : DataLogger {

  /** Converts and saves an event to a document for Firebase Firestore. */
  override fun saveData(eventLog: EventLog) {
    firestoreDocumentCreator.createDocument(eventLog).let { document ->
      firebaseFirestore.collection("nps_survey_open_feedback")
        .add(document)
    }
  }

  /** Application-scoped injectable factory for creating a new [FirestoreDataLogger]. */
  class Factory @Inject constructor(
    private val firestoreDocumentCreator: FirestoreDocumentCreator
  ) {
    private val firestoreDatabase = Firebase.firestore

    /**
     * Returns a new [FirestoreDataLogger] for the current application context.
     */
    fun createFirestoreDataLogger(): FirestoreDataLogger =
      FirestoreDataLogger(firestoreDatabase, firestoreDocumentCreator)
  }
}
