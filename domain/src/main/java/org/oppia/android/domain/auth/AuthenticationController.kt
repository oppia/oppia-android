package org.oppia.android.domain.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CompletableDeferred
import org.oppia.android.util.data.AsyncResult
import javax.inject.Inject
import javax.inject.Singleton

/** Controller for signing in and retrieving a Firebase user. */
@Singleton
class AuthenticationController @Inject constructor(
  private val firebaseAuth: FirebaseAuth
) : AuthenticationWrapper {
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
}
