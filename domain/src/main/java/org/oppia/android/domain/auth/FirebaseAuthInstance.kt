package org.oppia.android.domain.auth

import com.google.firebase.auth.FirebaseAuth

/** Wrapper for [FirebaseAuth], used to pass an instance of [FirebaseAuth]. */
data class FirebaseAuthInstance(
  val firebaseAuth: FirebaseAuth
)
