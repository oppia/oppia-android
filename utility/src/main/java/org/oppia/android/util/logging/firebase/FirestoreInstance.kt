package org.oppia.android.util.logging.firebase

import com.google.firebase.firestore.FirebaseFirestore

/** Wrapper for [FirebaseFirestore], used to pass an instance of [FirebaseFirestore]. */
data class FirestoreInstance(
  val firebaseFirestore: FirebaseFirestore
)
