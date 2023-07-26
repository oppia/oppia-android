package org.oppia.android.domain.auth

import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CompletableDeferred
import org.oppia.android.util.data.AsyncResult

/** Listener for getting the authentication state of the default FirebaseApp. */
interface AuthenticationListener {
  /** Returns the current signed in user or null if there is no authenticated user. */
  fun getCurrentSignedInUser(): FirebaseUser?

  /** Returns the authentication result. */
  fun signInAnonymously(): CompletableDeferred<AsyncResult<Any?>>
}
