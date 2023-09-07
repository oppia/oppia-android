package org.oppia.android.domain.auth

import com.google.firebase.auth.FirebaseAuth
import org.mockito.Mockito.mock
import javax.inject.Inject

/** A test specific fake for the AuthenticationController.Factory. */
class FakeAuthenticationControllerFactory @Inject constructor() {
  private val firebaseAuth: FirebaseAuth = mock(FirebaseAuth::class.java)

  /** Returns a new [AuthenticationController] for the current application context. */
  fun create(): AuthenticationController = AuthenticationController(firebaseAuth)
}
