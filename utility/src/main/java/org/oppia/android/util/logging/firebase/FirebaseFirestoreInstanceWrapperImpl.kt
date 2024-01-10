package org.oppia.android.util.logging.firebase

import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

/** Implementation of [FirebaseFirestoreInstanceWrapper]. */
class FirebaseFirestoreInstanceWrapperImpl @Inject constructor() :
  FirebaseFirestoreInstanceWrapper {

  override val firebaseFirestoreInstance: FirebaseFirestoreInstance
    get() = FirebaseFirestoreInstance(FirebaseFirestore.getInstance())
}
