package org.oppia.android.domain.platformparameter

import org.oppia.android.app.model.SupportedFeatureFlags
import org.oppia.android.app.model.SupportedPlatformParameters

interface PlatformParameterConfigRetriever {
  /**
   * Returns the [SupportedPlatformParameters] configuration for the app, or default instance if
   * none.
   */
  fun loadSupportedPlatformParameters(): SupportedPlatformParameters

  /** Returns the [SupportedFeatureFlags] configuration for the app, or default instance if none. */
  fun loadSupportedFeatureFlags(): SupportedFeatureFlags
}
