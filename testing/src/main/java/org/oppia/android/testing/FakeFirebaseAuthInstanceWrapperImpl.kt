package org.oppia.android.testing

import org.oppia.android.domain.auth.FirebaseAuthInstance
import org.oppia.android.domain.auth.FirebaseAuthInstanceWrapper
import javax.inject.Inject

/** Implementation of [FirebaseAuthInstanceWrapper]. */
class FakeFirebaseAuthInstanceWrapperImpl @Inject constructor() : FirebaseAuthInstanceWrapper {
  override val firebaseAuthInstance: FirebaseAuthInstance?
    get() = null
}
