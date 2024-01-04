package org.oppia.android.domain.auth

/** Interface for providing an implementation of [FirebaseAuthInstance]. */
interface FirebaseAuthInstanceWrapper {
  val firebaseAuthInstance: FirebaseAuthInstance
}
