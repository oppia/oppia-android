package org.oppia.android.domain.auth

import javax.inject.Inject
import javax.inject.Singleton

/** Production implementation of FirebaseAuthWrapper. */
@Singleton
class FirebaseAuthWrapperImpl @Inject constructor(
  private val firebaseWrapper: FirebaseAuthInstanceWrapper
) : FirebaseAuthWrapper {
  override val currentUser: FirebaseUserWrapper?
    get() = firebaseWrapper.firebaseAuthInstance?.firebaseAuth?.currentUser?.let {
      FirebaseUserWrapper(it.uid)
    }

  override fun signInAnonymously(onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
    firebaseWrapper.firebaseAuthInstance?.firebaseAuth?.signInAnonymously()
      ?.addOnSuccessListener {
        onSuccess.invoke()
      }
      ?.addOnFailureListener { task ->
        val exception = task.cause
        if (exception != null) {
          onFailure.invoke(exception)
        }
      }
  }
}

