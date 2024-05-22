package org.oppia.android.domain.auth

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.threading.BackgroundDispatcher
import javax.inject.Inject

/** Controller for signing in and retrieving a Firebase user. */
class AuthenticationController @Inject constructor(
  private val firebaseAuthWrapper: FirebaseAuthWrapper,
  @BackgroundDispatcher private val backgroundCoroutineDispatcher: CoroutineDispatcher
) {
  /** Returns the current signed in user or null if there is no authenticated user. */
  val currentFirebaseUser: FirebaseUserWrapper? = firebaseAuthWrapper.currentUser

  /** Returns the result of an authentication task. */
  fun signInAnonymouslyWithFirebase(): CompletableDeferred<AsyncResult<Any?>> {
    val deferredResult = CompletableDeferred<AsyncResult<Any?>>()
    CoroutineScope(backgroundCoroutineDispatcher).launch {
      try {
        withTimeout(AUTHENTICATION_TIMEOUT_MILLIS) {
          firebaseAuthWrapper.signInAnonymously(
            onSuccess = {
              deferredResult.complete(AsyncResult.Success(null))
            },
            onFailure = { exception ->
              deferredResult.complete(AsyncResult.Failure(exception))
            }
          )
        }
      } catch (e: TimeoutCancellationException) {
        deferredResult.complete(
          AsyncResult.Failure(e)
        )
      }
    }
    return deferredResult
  }

  companion object {
    /** The amount of time the authentication task should run before timing out. */
    private const val AUTHENTICATION_TIMEOUT_MILLIS = 30_000L
  }
}
