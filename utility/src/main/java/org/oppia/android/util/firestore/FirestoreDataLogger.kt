package org.oppia.android.util.firestore

import android.app.Application
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import javax.inject.Inject
import org.oppia.android.util.networking.NetworkConnectionUtil

/** Logger for uploading to Firestore. */
class FirestoreDataLogger private constructor(
  private val firebaseFirestore: FirebaseFirestore,
  private val networkConnectionUtil: NetworkConnectionUtil,
  private val firestoreDocumentCreator: FirestoreDocumentCreator,
) : DataLogger {

  override fun saveData(dataObject: Any?){}

  /** Application-scoped injectable factory for creating a new [FirestoreDataLogger]. */
  class Factory @Inject constructor(
    private val application: Application,
    private val networkConnectionUtil: NetworkConnectionUtil,
    private val firestoreDocumentCreator: FirestoreDocumentCreator
  ) {
    private val firestoreDatabase = Firebase.firestore
    /**
     * Returns a new [FirestoreDataLogger] for the current application context.
     */
    fun createFirestoreDataLogger(): FirestoreDataLogger =
      FirestoreDataLogger(firestoreDatabase, networkConnectionUtil,  firestoreDocumentCreator)
  }
}