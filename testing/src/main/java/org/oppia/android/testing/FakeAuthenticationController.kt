package org.oppia.android.testing

import com.google.firebase.auth.FirebaseUser
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CompletableDeferred
import org.oppia.android.util.data.AsyncResult

/** A test specific fake for the AuthenticationController. */
@Singleton
class FakeAuthenticationController @Inject constructor() {
  private var signInIsSuccessful = true
  private var currentUser: FirebaseUser? = null

  val currentFirebaseUser = currentUser

  fun signInAnonymouslyWithFirebase(): CompletableDeferred<AsyncResult<Any?>> {
    val deferredResult = CompletableDeferred<AsyncResult<Any?>>()

    if (signInIsSuccessful) {
      deferredResult.complete(AsyncResult.Success(null))
    } else {
      val error = Exception("Authentication failed")
      deferredResult.complete(AsyncResult.Failure(error))
    }

    return deferredResult
  }

  /** Sets whether sign in was successful. */
  fun setSignInSuccessStatus(signInSuccessful: Boolean) {
    signInIsSuccessful = signInSuccessful
  }

  /** Sets the current signed in user. */
  fun setSignedInUser(firebaseUser: FirebaseUser) {
    currentUser = firebaseUser
  }
}
