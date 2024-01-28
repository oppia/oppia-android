package org.oppia.android.testing

import org.oppia.android.util.logging.firebase.FirestoreInstance
import org.oppia.android.util.logging.firebase.FirestoreInstanceWrapper
import javax.inject.Inject

/**
 * A test specific fake for the [FirestoreInstanceWrapper]. [FirebaseFirestore] requires an instance
 * of [FirebaseApp], which is difficult to mock or fake hence this implementation always returns
 * null when an instance of [FirebaseFirestore] is requested.
 */
class FakeFirestoreInstanceWrapperImpl @Inject constructor() : FirestoreInstanceWrapper {

  override val firestoreInstance: FirestoreInstance?
    get() = null
}
