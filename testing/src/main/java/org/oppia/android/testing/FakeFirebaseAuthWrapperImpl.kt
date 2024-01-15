package org.oppia.android.testing

import org.oppia.android.domain.auth.FirebaseAuthWrapper
import org.oppia.android.domain.auth.FirebaseUserWrapper
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/** A test specific fake for the [FirebaseAuthWrapper]. */
@Singleton
class FakeFirebaseAuthWrapperImpl @Inject constructor(
  private val fakeAuthInstance: FakeFirebaseAuthInstanceWrapperImpl
) :
  FirebaseAuthWrapper {
  private var fakeAuthState: FakeAuthState = FakeAuthState.Success

  /** Fake a successful auth response. */
  fun simulateSignInSuccess() {
    fakeAuthState = FakeAuthState.Success
  }

  /** Fake a failed auth response. */
  fun simulateSignInFailure() {
    fakeAuthState = FakeAuthState.Failure
  }

  /** Returns the [fakeAuthState] of the controller. */
  fun getAuthState(): FakeAuthState = fakeAuthState

  override val currentUser: FirebaseUserWrapper?
    get() = if (fakeAuthState == FakeAuthState.Success)
      FirebaseUserWrapper(uid = UUID.randomUUID().toString())
    else
      null

  override fun signInAnonymously(onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
    if (fakeAuthState == FakeAuthState.Success) {
      onSuccess.invoke()
    } else {
      onFailure.invoke(Exception("Sign-in failure"))
    }
  }

  sealed class FakeAuthState {
    object Success : FakeAuthState()
    object Failure : FakeAuthState()
  }
}
