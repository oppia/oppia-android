package org.oppia.android.domain.auth

import com.google.firebase.auth.FirebaseUser

/** Wrapper for [FirebaseUser]. */
data class FirebaseUserWrapper(
  val uid: String,
)
