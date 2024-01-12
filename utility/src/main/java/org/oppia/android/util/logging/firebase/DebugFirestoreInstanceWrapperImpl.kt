package org.oppia.android.util.logging.firebase

import javax.inject.Inject
import javax.inject.Singleton

/**
 * A test and debug fake for the [FirestoreInstanceWrapper]. This is also used in debug environments
 * to make the debug implementation testable. [FirebaseFirestore] requires an instance of [FirebaseApp],
 * which is difficult to mock or fake hence this implementation always returns null when an instance
 * of [FirebaseFirestore] is requested.
 */
@Singleton
class DebugFirestoreInstanceWrapperImpl @Inject constructor() : FirestoreInstanceWrapper {

  override val firestoreInstance: FirestoreInstance?
    get() = null
}
