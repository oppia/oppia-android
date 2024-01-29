package org.oppia.android.domain.auth

/** Interface for providing an implementation of [FirebaseAuthInstance]. */
interface FirebaseAuthInstanceWrapper {
  /** Returns a wrapped instance of FirebaseAuth. */
  val firebaseAuthInstance: FirebaseAuthInstance?
}
