package org.oppia.android.domain.auth

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import javax.inject.Inject
import javax.inject.Singleton

/** Production implementation of FirebaseAuthWrapper. */
@Singleton
class FirebaseAuthWrapperImpl @Inject constructor(
  private val firebaseAuth: FirebaseAuth
) : FirebaseAuthWrapper {
  /** Returns the current signed in user or null if there is no authenticated user. */
  override val currentUser: FirebaseUser?
    get() = firebaseAuth.currentUser

  /** Returns the result of an authentication task. */
  override fun signInAnonymously() : Task<AuthResult>  =  firebaseAuth.signInAnonymously()
}
