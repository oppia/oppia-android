package org.oppia.android.domain.auth

import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import javax.inject.Inject

/** Implementation of [FirebaseAuthInstanceWrapper]. */
class FirebaseAuthInstanceWrapperImpl @Inject constructor() : FirebaseAuthInstanceWrapper {
  override val firebaseAuthInstance: FirebaseAuthInstance
    get() = FirebaseAuthInstance(Firebase.auth)
}
