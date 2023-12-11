package org.oppia.android.domain.auth

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser

/** Wrapper for FirebaseAuth. */
interface FirebaseAuthWrapper {

  /** Returns the current signed in user or null if there is no authenticated user. */
  val currentUser: FirebaseUser?

  /** Returns the authentication result. */
  fun signInAnonymously(): Task<AuthResult>
}
