package org.oppia.android.domain.auth

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.threading.BackgroundDispatcher
import java.util.concurrent.TimeoutException
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
    val timeoutMillis = 3000L
    CoroutineScope(backgroundCoroutineDispatcher).launch {
      try {
        withTimeout(timeoutMillis) {
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
        // Handle timeout exception
        deferredResult.complete(
          AsyncResult.Failure(TimeoutException("Authentication attempt timed out"))
        )
      }
    }
    return deferredResult
  }
}
