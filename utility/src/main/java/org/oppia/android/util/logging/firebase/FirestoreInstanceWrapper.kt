package org.oppia.android.util.logging.firebase

/** Interface for providing an implementation of [FirestoreInstance]. */
interface FirestoreInstanceWrapper {
  /** Returns a wrapped instance of FirebaseFirestore. */
  val firestoreInstance: FirestoreInstance?
}
