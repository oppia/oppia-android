package org.oppia.android.domain.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CompletableDeferred
import org.oppia.android.util.data.AsyncResult
import javax.inject.Inject

class AuthenticationController private constructor(
  private val firebaseAuth: FirebaseAuth
) : AuthenticationListener {
  /** Returns the current signed in user or null if there is no authenticated user. */
  override fun getCurrentSignedInUser(): FirebaseUser? {
    return firebaseAuth.currentUser
  }

  /** Returns the result of an authentication task. */
  override fun signInAnonymously(): CompletableDeferred<AsyncResult<Any?>> {
    val deferredResult = CompletableDeferred<AsyncResult<Any?>>()
    firebaseAuth.signInAnonymously()
      .addOnSuccessListener {
        deferredResult.complete(AsyncResult.Success(null))
      }
      .addOnFailureListener {
        deferredResult.complete(AsyncResult.Failure(it))
      }

    return deferredResult
  }

  /** Application-scoped injectable factory for creating a new [AuthenticationController]. */
  class Factory @Inject constructor() {
    private val firebaseAuth = Firebase.auth

    /** Returns a new [AuthenticationController] for the current application context. */
    fun create(): AuthenticationController = AuthenticationController(firebaseAuth)
  }
}
