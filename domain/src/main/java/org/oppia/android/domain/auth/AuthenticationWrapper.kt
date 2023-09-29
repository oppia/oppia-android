package org.oppia.android.domain.auth

import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CompletableDeferred
import org.oppia.android.util.data.AsyncResult

/** Wrapper for providing authentication functionality. */
interface AuthenticationWrapper {
  /** Returns the current signed in user or null if there is no authenticated user. */
  fun getCurrentSignedInUser(): FirebaseUser?

  /** Returns the authentication result. */
  fun signInAnonymously(): CompletableDeferred<AsyncResult<Any?>>
}
