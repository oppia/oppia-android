package org.oppia.android.testing

import org.oppia.android.domain.auth.FirebaseAuthWrapper
import org.oppia.android.domain.auth.FirebaseUserWrapper
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeFirebaseWrapperImpl @Inject constructor() : FirebaseAuthWrapper {
  private var simulateSuccess: Boolean = true

  fun simulateSignInSuccess() {
    simulateSuccess = true
  }

  fun simulateSignInFailure() {
    simulateSuccess = false
  }

  override val currentUser: FirebaseUserWrapper?
    get() = if (simulateSuccess) FirebaseUserWrapper(uid = UUID.randomUUID().toString()) else null

  override fun signInAnonymously(onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
    if (simulateSuccess) {
      onSuccess.invoke()
    } else {
      onFailure.invoke(Exception("Sign-in failure"))
    }
  }
}
