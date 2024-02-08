package org.oppia.android.util.logging.firebase

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import javax.inject.Inject

/** Implementation of [FirestoreInstanceWrapper]. */
class FirestoreInstanceWrapperImpl @Inject constructor() :
  FirestoreInstanceWrapper {

  override val firestoreInstance: FirestoreInstance
    get() = FirestoreInstance(Firebase.firestore)
}
