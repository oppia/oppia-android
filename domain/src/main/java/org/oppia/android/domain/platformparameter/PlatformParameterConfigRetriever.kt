package org.oppia.android.domain.platformparameter

import org.oppia.android.app.model.SupportedPlatformParameters
import org.oppia.android.app.model.SupportedFeatureFlags
import org.oppia.android.util.caching.AssetRepository
import javax.inject.Inject

/** Retriever for platform parameter and feature flag definitions from the app's embedded assets. */
class PlatformParameterConfigRetriever @Inject constructor(
  private val assetRepository: AssetRepository
) {
  /**
   * Returns the [SupportedPlatformParameters] configuration for the app, or default instance if
   * none.
   */
  fun loadSupportedPlatformParameters(): SupportedPlatformParameters {
    return assetRepository.tryLoadProtoFromLocalAssets(
      "supported_platform_parameters", SupportedPlatformParameters.getDefaultInstance()
    )
  }

  /** Returns the [SupportedFeatureFlags] configuration for the app, or default instance if none. */
  fun loadSupportedFeatureFlags(): SupportedFeatureFlags {
    return assetRepository.tryLoadProtoFromLocalAssets(
      "supported_feature_flags", SupportedFeatureFlags.getDefaultInstance()
    )
  }
}
