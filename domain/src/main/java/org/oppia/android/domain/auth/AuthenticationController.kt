package org.oppia.android.domain.auth

import kotlinx.coroutines.CompletableDeferred
import org.oppia.android.util.data.AsyncResult
import javax.inject.Inject

/** Controller for signing in and retrieving a Firebase user. */
class AuthenticationController @Inject constructor(
  private val firebaseAuthWrapper: FirebaseAuthWrapper
) {
  /** Returns the current signed in user or null if there is no authenticated user. */
  val currentFirebaseUser: FirebaseUserWrapper? = firebaseAuthWrapper.currentUser

  /** Returns the result of an authentication task. */
  fun signInAnonymouslyWithFirebase(): CompletableDeferred<AsyncResult<Any?>> {
    val deferredResult = CompletableDeferred<AsyncResult<Any?>>()
    firebaseAuthWrapper.signInAnonymously(
      onSuccess = {
        deferredResult.complete(AsyncResult.Success(null))
      },
      onFailure = { exception ->
        deferredResult.complete(AsyncResult.Failure(exception))
      }
    )

    return deferredResult
  }
}
