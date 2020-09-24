package org.oppia.domain.onboarding

import android.os.Bundle

/** Retriever for meta-data used when determining whether the app is in an expired state. */
interface ExpirationMetaDataRetriever {
  /**
   * Returns a [Bundle] containing data pertaining to whether the app is in an expired state. The
   * specifics of what this bundle contains is an implementation detail.
   *
   * It's expected that the returned bundle either be associated with the app's manifest for easy
   * configuration, or be replaced with a fake for testing purposes.
   */
  fun getMetaData(): Bundle?
}
