package org.oppia.android.util.logging.firebase

/** Interface for providing an implementation of [FirebaseFirestoreInstance]. */
interface FirebaseFirestoreInstanceWrapper {
  /** Returns a wrapped instance of FirebaseFirestore. */
  val firebaseFirestoreInstance: FirebaseFirestoreInstance
}
