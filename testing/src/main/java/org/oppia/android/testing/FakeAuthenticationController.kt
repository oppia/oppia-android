package org.oppia.android.testing

import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CompletableDeferred
import org.mockito.Mockito.mock
import org.oppia.android.domain.auth.AuthenticationListener
import org.oppia.android.util.data.AsyncResult
import javax.inject.Inject
import javax.inject.Singleton

/** A test specific fake for the AuthenticationController. */
@Singleton
class FakeAuthenticationController @Inject constructor() : AuthenticationListener {
  private val mockFirebaseUser: FirebaseUser? = mock(FirebaseUser::class.java)

  override fun getCurrentSignedInUser(): FirebaseUser? {
    return mockFirebaseUser
  }

  override fun signInAnonymously(): CompletableDeferred<AsyncResult<Any?>> {
    val deferredResult = CompletableDeferred<AsyncResult<Any?>>()

    val isSuccess = true

    if (isSuccess) {
      deferredResult.complete(AsyncResult.Success(null))
    } else {
      val error = Exception("Authentication failed")
      deferredResult.complete(AsyncResult.Failure(error))
    }

    return deferredResult
  }
}
