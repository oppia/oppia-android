package org.oppia.android.domain.auth

import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject
import javax.inject.Singleton

/** Production implementation of FirebaseAuthWrapper. */
@Singleton
class FirebaseAuthWrapperImpl @Inject constructor(
  private val firebaseAuth: FirebaseAuth
) : FirebaseAuthWrapper {
  override val currentUser: FirebaseUserWrapper?
    get() = firebaseAuth.currentUser?.let {
      FirebaseUserWrapper(it.uid)
    }

  override fun signInAnonymously(onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
    firebaseAuth.signInAnonymously()
      .addOnSuccessListener {
        // onSuccess.invoke()
        onFailure.invoke(Exception())
      }
      .addOnFailureListener { task ->
        val exception = task.cause
        if (exception != null) {
          onFailure.invoke(exception)
        }
      }
  }
}
