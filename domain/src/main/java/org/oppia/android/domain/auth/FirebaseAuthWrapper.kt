package org.oppia.android.domain.auth

/** Wrapper for FirebaseAuth. */
interface FirebaseAuthWrapper {
  /** Returns the current signed in user or null if there is no authenticated user. */
  val currentUser: FirebaseUserWrapper?

  /** Returns the authentication result. */
  fun signInAnonymously(onSuccess: () -> Unit, onFailure: (Throwable) -> Unit)
}
