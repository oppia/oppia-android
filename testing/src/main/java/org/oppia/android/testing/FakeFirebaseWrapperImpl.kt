package org.oppia.android.testing

import javax.inject.Inject
import javax.inject.Singleton
import org.oppia.android.domain.auth.FirebaseAuthWrapper
import org.oppia.android.domain.auth.FirebaseUserWrapper

@Singleton
class FakeFirebaseWrapperImpl @Inject constructor() : FirebaseAuthWrapper {
  override val currentUser: FirebaseUserWrapper? = null

  private var simulateSuccess: Boolean = true

  fun simulateSignInSuccess() {
    simulateSuccess = true
  }

  fun simulateSignInFailure() {
    simulateSuccess = false
  }

  override fun signInAnonymously(onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
    if (simulateSuccess) {
      onSuccess.invoke()
    } else {
      onFailure.invoke(Exception("Sign-in failure"))
    }
  }
}
