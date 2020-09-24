package org.oppia.domain.onboarding.testing

import android.os.Bundle
import org.oppia.domain.onboarding.ExpirationMetaDataRetriever
import javax.inject.Inject
import javax.inject.Singleton

/** Fake implementation of [ExpirationMetaDataRetriever] for use in tests. */
@Singleton // Ensure the bundle is properly reused at all injection sites of this class.
class FakeExpirationMetaDataRetriever @Inject constructor() : ExpirationMetaDataRetriever {
  private val metaData = Bundle()

  override fun getMetaData(): Bundle? {
    return metaData
  }

  /** Convenience method for setting a string in the expiration meta data. */
  fun putMetaDataString(keyName: String, value: String) {
    metaData.putString(keyName, value)
  }

  /** Convenience method for setting a boolean in the expiration meta data. */
  fun putMetaDataBoolean(keyName: String, value: Boolean) {
    metaData.putBoolean(keyName, value)
  }
}
